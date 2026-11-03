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
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Repository;
import org.apache.maven.model.resolution.ModelResolver;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FlattenModelResolverTest {

    private final RepositorySystemSession session = mock(RepositorySystemSession.class);

    private final RepositorySystem repositorySystem = mock(RepositorySystem.class);

    private final FlattenModelResolver resolver = new FlattenModelResolver(
            session,
            repositorySystem,
            null,
            "flatten",
            Collections.singletonList(new RemoteRepository.Builder("central", "default", "https://central").build()),
            Collections.emptyList());

    @BeforeEach
    void setUp() throws Exception {
        when(repositorySystem.newResolutionRepositories(any(), anyList()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(repositorySystem.resolveArtifact(any(), any())).thenAnswer(invocation -> {
            ArtifactRequest request = invocation.getArgument(1);
            return new ArtifactResult(request).setArtifact(request.getArtifact().setFile(new File("pom.xml")));
        });
    }

    @Test
    void resolvesModelFromAddedRepository() throws Exception {
        resolver.addRepository(repository("parent-repo", "https://parent"));

        assertThat(repositoriesOfNextResolution(resolver))
                .extracting(RemoteRepository::getId)
                .containsExactly("central", "parent-repo");
    }

    @Test
    void replacesRepositoryWithSameIdOnlyWhenAsked() throws Exception {
        resolver.addRepository(repository("parent-repo", "https://first"));
        resolver.addRepository(repository("parent-repo", "https://second"));

        assertThat(repositoriesOfNextResolution(resolver))
                .extracting(RemoteRepository::getUrl)
                .containsExactly("https://central", "https://first");

        resolver.addRepository(repository("parent-repo", "https://third"), true);

        assertThat(repositoriesOfNextResolution(resolver))
                .extracting(RemoteRepository::getUrl)
                .containsExactly("https://central", "https://third");
    }

    @Test
    void addsMirrorOfRepositoryOnlyOnce() throws Exception {
        RemoteRepository mirror = new RemoteRepository.Builder("mirror", "default", "https://mirror").build();
        when(repositorySystem.newResolutionRepositories(any(), anyList()))
                .thenReturn(Collections.singletonList(mirror));

        resolver.addRepository(repository("parent-repo", "https://parent"));
        resolver.addRepository(repository("bom-repo", "https://bom"));

        assertThat(repositoriesOfNextResolution(resolver))
                .extracting(RemoteRepository::getId)
                .containsExactly("central", "mirror");
    }

    @Test
    void copyKeepsItsOwnRepositories() throws Exception {
        resolver.addRepository(repository("parent-repo", "https://parent"));
        ModelResolver copy = resolver.newCopy();
        copy.addRepository(repository("bom-repo", "https://bom"));

        assertThat(repositoriesOfNextResolution(copy))
                .extracting(RemoteRepository::getId)
                .containsExactly("central", "parent-repo", "bom-repo");
        assertThat(repositoriesOfNextResolution(resolver))
                .extracting(RemoteRepository::getId)
                .containsExactly("central", "parent-repo");
    }

    private List<RemoteRepository> repositoriesOfNextResolution(ModelResolver modelResolver) throws Exception {
        modelResolver.resolveModel("org.example", "example-parent", "1.0");
        ArgumentCaptor<ArtifactRequest> request = ArgumentCaptor.forClass(ArtifactRequest.class);
        verify(repositorySystem, atLeastOnce()).resolveArtifact(any(), request.capture());
        return request.getValue().getRepositories();
    }

    private static Repository repository(String id, String url) {
        Repository repository = new Repository();
        repository.setId(id);
        repository.setUrl(url);
        return repository;
    }
}
