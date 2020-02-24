/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.feature.maven.mojos;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.io.json.FeatureJSONReader;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ApiRegionsOverlapCheckMojoTest {
    @Test
    public void testMustSpecifyRegion() throws Exception {
        ApiRegionsOverlapCheckMojo mojo = new ApiRegionsOverlapCheckMojo();
        mojo.selection = new FeatureSelectionConfig();

        try {
            mojo.execute();
            fail("Expected MojoExecutionException");
        } catch (MojoExecutionException mee) {
            assertTrue(mee.getMessage().contains("Please specify at least one region to check for"));
        }

        mojo.regions = Collections.emptySet();
        try {
            mojo.execute();
            fail("Expected MojoExecutionException");
        } catch (MojoExecutionException mee) {
            assertTrue(mee.getMessage().contains("Please specify at least one region to check for"));
        }
    }

    @Test
    public void testOverlap() throws Exception {
        ApiRegionsOverlapCheckMojo mojo = new ApiRegionsOverlapCheckMojo();

        mojo.features = new File(getClass().getResource("/api-regions-crossfeature-duplicates/testOverlap").getFile());
        Map<String, Feature> featureMap = new HashMap<>();
        for (File f : mojo.features.listFiles()) {
            Feature feat = FeatureJSONReader.read(new FileReader(f), null);
            featureMap.put(f.getAbsolutePath(), feat);
        }

        mojo.project = Mockito.mock(MavenProject.class);
        Mockito.when(mojo.project.getContextValue(Feature.class.getName() + "/assembledmain.json-cache"))
            .thenReturn(featureMap);

        mojo.regions = Collections.singleton("foo");
        FeatureSelectionConfig cfg = new FeatureSelectionConfig();
        cfg.setFilesInclude("*.json");
        mojo.selection = cfg;

        try {
            mojo.execute();
            fail("Expect to fail here as there is overlap");
        } catch (MojoExecutionException mee) {
            assertTrue(mee.getMessage().contains("Errors found"));
        }
    }

    @Test
    public void testNoOverlap() throws Exception {
        ApiRegionsOverlapCheckMojo mojo = new ApiRegionsOverlapCheckMojo();

        mojo.features = new File(getClass().getResource("/api-regions-crossfeature-duplicates/testNoOverlap").getFile());
        Map<String, Feature> featureMap = new HashMap<>();
        for (File f : mojo.features.listFiles()) {
            Feature feat = FeatureJSONReader.read(new FileReader(f), null);
            featureMap.put(f.getAbsolutePath(), feat);
        }

        mojo.project = Mockito.mock(MavenProject.class);
        Mockito.when(mojo.project.getContextValue(Feature.class.getName() + "/assembledmain.json-cache"))
            .thenReturn(featureMap);

        mojo.regions = Collections.singleton("foo");
        FeatureSelectionConfig cfg = new FeatureSelectionConfig();
        cfg.setFilesInclude("*.json");
        mojo.selection = cfg;

        mojo.execute();
    }
}
