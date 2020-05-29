/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.sling.feature.maven.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.sling.feature.Artifact;
import org.apache.sling.feature.ArtifactId;
import org.apache.sling.feature.Extension;
import org.apache.sling.feature.ExtensionState;
import org.apache.sling.feature.ExtensionType;
import org.apache.sling.feature.Feature;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.fail;

public class CompareFeaturesMojoTest {
    @Test
    public void testCompareNoExtensions() throws Exception {
        Feature f1 = new Feature(ArtifactId.fromMvnId("a:b:1"));
        Feature f2 = new Feature(ArtifactId.fromMvnId("a:b:2"));

        CompareFeaturesMojo cfm = new CompareFeaturesMojo();
        cfm.compareFeatures(f1, f2);
        // Should succeed

        CompareFeaturesMojo cfm1 = new CompareFeaturesMojo();
        cfm1.extensions = Collections.singletonList("lala");
        cfm1.compareFeatures(f1, f2);
        // Should succeed
    }

    @Test
    public void testCompareExtensions() throws Exception {
        Extension e1 = new Extension(ExtensionType.ARTIFACTS, "artext", ExtensionState.REQUIRED);
        e1.getArtifacts().add(new Artifact(ArtifactId.fromMvnId("g:a1:1")));
        e1.getArtifacts().add(new Artifact(ArtifactId.fromMvnId("g:a2:2")));
        Feature f1 = new Feature(ArtifactId.fromMvnId("a:b:1"));
        f1.getExtensions().add(e1);

        Extension e2 = new Extension(ExtensionType.ARTIFACTS, "artext", ExtensionState.REQUIRED);
        e2.getArtifacts().add(new Artifact(ArtifactId.fromMvnId("g:a1:1")));
        e2.getArtifacts().add(new Artifact(ArtifactId.fromMvnId("g:a2:2")));
        Feature f2 = new Feature(ArtifactId.fromMvnId("a:b:2"));
        f2.getExtensions().add(e2);

        CompareFeaturesMojo cfm = new CompareFeaturesMojo();
        cfm.extensions = Collections.singletonList("artext");
        cfm.compareFeatures(f1, f2);
        // Should succeed
    }

    @Test
    public void testCompareExtensions1() throws Exception {
        Extension e1 = new Extension(ExtensionType.ARTIFACTS, "artext", ExtensionState.REQUIRED);
        e1.getArtifacts().add(new Artifact(ArtifactId.fromMvnId("g:a1:1")));
        e1.getArtifacts().add(new Artifact(ArtifactId.fromMvnId("g:a2:2")));
        Extension e1b = new Extension(ExtensionType.ARTIFACTS, "myext", ExtensionState.OPTIONAL);
        Extension e1c = new Extension(ExtensionType.ARTIFACTS, "some-ext", ExtensionState.REQUIRED);
        Extension e1d = new Extension(ExtensionType.TEXT, "text", ExtensionState.OPTIONAL);
        Feature f1 = new Feature(ArtifactId.fromMvnId("a:b:1"));
        f1.getExtensions().add(e1);
        f1.getExtensions().add(e1b);
        f1.getExtensions().add(e1c);
        f1.getExtensions().add(e1d);

        Extension e2 = new Extension(ExtensionType.ARTIFACTS, "artext", ExtensionState.REQUIRED);
        e2.getArtifacts().add(new Artifact(ArtifactId.fromMvnId("g:a2:2")));
        e2.getArtifacts().add(new Artifact(ArtifactId.fromMvnId("g:a1:1")));
        Extension e2b = new Extension(ExtensionType.ARTIFACTS, "myext", ExtensionState.TRANSIENT);
        Extension e2c = new Extension(ExtensionType.JSON, "some-ext", ExtensionState.REQUIRED);
        Extension e2d = new Extension(ExtensionType.TEXT, "text", ExtensionState.OPTIONAL);
        Feature f2 = new Feature(ArtifactId.fromMvnId("a:b:2"));
        f2.getExtensions().add(e2);
        f2.getExtensions().add(e2b);
        f2.getExtensions().add(e2c);
        f2.getExtensions().add(e2d);

        CompareFeaturesMojo cfm = new CompareFeaturesMojo();
        cfm.extensions = Collections.singletonList("artext");

        try {
            cfm.compareFeatures(f1, f2);
            fail("The extensions are different, was expecting a Mojo Exception");
        } catch (MojoExecutionException mee) {
            // good
        }

        CompareFeaturesMojo cfm1 = new CompareFeaturesMojo();
        cfm1.extensions = Collections.singletonList("myext");

        try {
            cfm1.compareFeatures(f1, f2);
            fail("The extensions are different, was expecting a Mojo Exception");
        } catch (MojoExecutionException mee) {
            // good
        }

        CompareFeaturesMojo cfm2 = new CompareFeaturesMojo();
        cfm2.extensions = Arrays.asList("text", "some-ext");

        try {
            cfm2.compareFeatures(f1, f2);
            fail("The extensions are different, was expecting a Mojo Exception");
        } catch (MojoExecutionException mee) {
            // good
        }
    }

    @Test
    public void testCompareExtensions2() throws Exception {
        Extension e1 = new Extension(ExtensionType.ARTIFACTS, "artext", ExtensionState.REQUIRED);
        e1.getArtifacts().add(new Artifact(ArtifactId.fromMvnId("g:a1:1")));
        e1.getArtifacts().add(new Artifact(ArtifactId.fromMvnId("g:a2:2")));
        Feature f1 = new Feature(ArtifactId.fromMvnId("a:b:1"));
        f1.getExtensions().add(e1);

        Extension e2 = new Extension(ExtensionType.ARTIFACTS, "artext", ExtensionState.REQUIRED);
        e2.getArtifacts().add(new Artifact(ArtifactId.fromMvnId("g:a1:1")));
        Feature f2 = new Feature(ArtifactId.fromMvnId("a:b:2"));
        f2.getExtensions().add(e2);

        CompareFeaturesMojo cfm = new CompareFeaturesMojo();
        cfm.extensions = Collections.singletonList("artext");

        try {
            cfm.compareFeatures(f1, f2);
            fail("The extensions are different, was expecting a Mojo Exception");
        } catch (MojoExecutionException mee) {
            // good
        }
    }
}
