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
import org.junit.Test;

import java.util.Collections;

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

        // mojo.execute();
    }
}
