package org.codehaus.mojo.flatten.model.resolution;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;

/**
 * This is a custom implementation of {@link ModelResolver} to emulate the maven POM resolution in order to build the
 * flattened POM.
 *
 * @see org.codehaus.mojo.flatten.FlattenMojo
 * @author Robert Scholte
 */
public class FlattenModelResolver implements ModelResolver {
    private final RepositorySystemSession session;

    private final RepositorySystem repositorySystem;

    private final RequestTrace trace;

    private final String context;

    private final List<RemoteRepository> repositories;

    private final Set<String> repositoryIds;

    /** The modules of the project being built. */
    private final ReactorModelPool reactorModelPool;

    /**
     * The constructor.
     */
    public FlattenModelResolver(
            RepositorySystemSession session,
            RepositorySystem repositorySystem,
            RequestTrace trace,
            String context,
            List<RemoteRepository> repositories,
            List<MavenProject> reactorModels) {
        this.session = session;
        this.repositorySystem = repositorySystem;
        this.trace = trace;
        this.context = context;
        this.repositories = new ArrayList<>(repositories);
        this.repositoryIds = new HashSet<>();

        this.reactorModelPool = new ReactorModelPool();
        reactorModelPool.addProjects(reactorModels);
    }

    private FlattenModelResolver(FlattenModelResolver other) {
        this.session = other.session;
        this.repositorySystem = other.repositorySystem;
        this.trace = other.trace;
        this.context = other.context;
        this.repositories = new ArrayList<>(other.repositories);
        this.repositoryIds = new HashSet<>(other.repositoryIds);
        this.reactorModelPool = other.reactorModelPool;
    }

    /**
     * {@inheritDoc}
     */
    public ModelSource resolveModel(String groupId, String artifactId, String version)
            throws UnresolvableModelException {
        File pomFile = reactorModelPool.find(groupId, artifactId, version);
        if (pomFile == null) {
            Artifact pomArtifact = new DefaultArtifact(groupId, artifactId, "", "pom", version);

            try {
                ArtifactRequest request = new ArtifactRequest(pomArtifact, repositories, context);
                request.setTrace(trace);
                pomArtifact = repositorySystem.resolveArtifact(session, request).getArtifact();
            } catch (ArtifactResolutionException e) {
                throw new UnresolvableModelException(e.getMessage(), groupId, artifactId, version, e);
            }

            pomFile = pomArtifact.getFile();
        }
        return new FileModelSource(pomFile);
    }

    /**
     * {@inheritDoc}
     */
    public void addRepository(Repository repository) {
        addRepository(repository, false);
    }

    /**
     * {@inheritDoc}
     */
    public ModelResolver newCopy() {
        return new FlattenModelResolver(this);
    }

    /**
     * Resolves the POM for the specified parent.
     *
     * @param parent the parent coordinates to resolve, must not be {@code null}
     * @return The source of the requested POM, never {@code null}
     * @since Apache-Maven-3.2.2 (MNG-5639)
     */
    public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
        parent.setVersion(resolveVersion(parent.getGroupId(), parent.getArtifactId(), parent.getVersion()));
        return resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
    }

    @Override
    public ModelSource resolveModel(Dependency dependency) throws UnresolvableModelException {
        dependency.setVersion(
                resolveVersion(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion()));
        return resolveModel(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }

    private String resolveVersion(String groupId, String artifactId, String version) throws UnresolvableModelException {
        Artifact artifact = new DefaultArtifact(groupId, artifactId, "", "pom", version);

        VersionRangeRequest versionRangeRequest = new VersionRangeRequest(artifact, repositories, context);
        versionRangeRequest.setTrace(trace);

        try {
            VersionRangeResult versionRangeResult = repositorySystem.resolveVersionRange(session, versionRangeRequest);

            if (versionRangeResult.getHighestVersion() == null) {
                throw new UnresolvableModelException(
                        "No versions matched the requested range '" + version + "'", groupId, artifactId, version);
            }

            if (versionRangeResult.getVersionConstraint() != null
                    && versionRangeResult.getVersionConstraint().getRange() != null
                    && versionRangeResult.getVersionConstraint().getRange().getUpperBound() == null) {
                throw new UnresolvableModelException(
                        "The requested version range '" + version + "' does not specify an upper bound",
                        groupId,
                        artifactId,
                        version);
            }

            return versionRangeResult.getHighestVersion().toString();
        } catch (VersionRangeResolutionException e) {
            throw new UnresolvableModelException(e.getMessage(), groupId, artifactId, version, e);
        }
    }

    /**
     * @param repository The repository to add to the internal search chain, must not be {@code null}.
     * @param replace {true} when repository with same id should be replaced, otherwise {@code false}.
     * @since Apache-Maven-3.2.3 (MNG-5663)
     */
    public void addRepository(Repository repository, boolean replace) {
        if (!repositoryIds.add(repository.getId())) {
            if (!replace) {
                return;
            }
            repositories.removeIf(r -> r.getId().equals(repository.getId()));
        }

        RemoteRepository remoteRepository = new RemoteRepository.Builder(
                        repository.getId(), repository.getLayout(), repository.getUrl())
                .setReleasePolicy(toRepositoryPolicy(repository.getReleases()))
                .setSnapshotPolicy(toRepositoryPolicy(repository.getSnapshots()))
                .build();
        // applies the mirrors, proxies and authentication of the settings
        for (RemoteRepository resolutionRepository :
                repositorySystem.newResolutionRepositories(session, Collections.singletonList(remoteRepository))) {
            if (repositories.stream().noneMatch(r -> r.getId().equals(resolutionRepository.getId()))) {
                repositories.add(resolutionRepository);
            }
        }
    }

    private static RepositoryPolicy toRepositoryPolicy(org.apache.maven.model.RepositoryPolicy policy) {
        boolean enabled = true;
        String updatePolicy = RepositoryPolicy.UPDATE_POLICY_DAILY;
        String checksumPolicy = RepositoryPolicy.CHECKSUM_POLICY_WARN;
        if (policy != null) {
            enabled = policy.isEnabled();
            if (policy.getUpdatePolicy() != null) {
                updatePolicy = policy.getUpdatePolicy();
            }
            if (policy.getChecksumPolicy() != null) {
                checksumPolicy = policy.getChecksumPolicy();
            }
        }
        return new RepositoryPolicy(enabled, updatePolicy, checksumPolicy);
    }
}
