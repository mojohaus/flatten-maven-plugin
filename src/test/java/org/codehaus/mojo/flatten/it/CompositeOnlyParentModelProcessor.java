package org.codehaus.mojo.flatten.it;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.model.io.ModelReader;
import org.apache.maven.model.locator.ModelLocator;
import org.eclipse.sisu.Priority;
import org.eclipse.sisu.Typed;

@Named("flatten-it-composite-only-parent")
@Singleton
@Priority(1000)
@Typed(ModelProcessor.class)
public final class CompositeOnlyParentModelProcessor implements ModelProcessor {
    private final ModelReader reader;
    private final ModelLocator locator;

    @Inject
    public CompositeOnlyParentModelProcessor(ModelReader reader, ModelLocator locator) {
        this.reader = reader;
        this.locator = locator;
    }

    @Override
    public File locatePom(File projectDirectory) {
        return locator.locatePom(projectDirectory);
    }

    @Override
    public Model read(File input, Map<String, ?> options) throws IOException {
        Model model = reader.read(input, options);
        apply(model, input.toPath());
        return model;
    }

    @Override
    public Model read(InputStream input, Map<String, ?> options) throws IOException {
        Model model = reader.read(input, options);
        apply(model, source(options));
        return model;
    }

    @Override
    public Model read(Reader input, Map<String, ?> options) throws IOException {
        Model model = reader.read(input, options);
        apply(model, source(options));
        return model;
    }

    private static Path source(Map<String, ?> options) {
        if (options == null) {
            return null;
        }
        Object source = options.get(ModelProcessor.SOURCE);
        if (source instanceof FileModelSource) {
            return ((FileModelSource) source).getPomFile().toPath();
        }
        return null;
    }

    private void apply(Model model, Path pom) throws IOException {
        Parent parent = model.getParent();
        if (pom == null || parent == null || !"${it.parent.version}".equals(parent.getVersion())) {
            return;
        }
        Path composite = pom.toAbsolutePath().getParent().resolve("composite-pom.xml");
        if (!Files.isRegularFile(composite)) {
            return;
        }
        Model shared = reader.read(composite.toFile(), Collections.emptyMap());
        String version = shared.getProperties().getProperty("it.parent.version");
        if (version == null || version.trim().isEmpty()) {
            throw new IOException("Missing it.parent.version in " + composite);
        }
        model.getProperties().putAll(shared.getProperties());
        parent.setVersion(version);
    }
}
