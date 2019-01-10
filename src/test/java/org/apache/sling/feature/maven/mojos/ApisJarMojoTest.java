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

import org.apache.maven.project.MavenProject;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.maven.Preprocessor;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ApisJarMojoTest {
    @Test
    public void testExecute() throws Exception {
        MavenProject project = Mockito.mock(MavenProject.class);
        Mockito.when(project.getContextValue(Preprocessor.class.getName())).thenReturn(new Object());
        Map<String,Feature> fm = new HashMap<>();
        Mockito.when(project.getContextValue("features-cache")).thenReturn(fm);

        FeatureSelectionConfig fsc = new FeatureSelectionConfig();

        ApisJarMojo mojo = new ApisJarMojo();
        mojo.project = project;
        setPrivateField(mojo, "selection", fsc);

        mojo.execute();
    }

    private void setPrivateField(Object obj, String name, Object val) throws Exception {
        Field field = obj.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(obj, val);
    }
}
