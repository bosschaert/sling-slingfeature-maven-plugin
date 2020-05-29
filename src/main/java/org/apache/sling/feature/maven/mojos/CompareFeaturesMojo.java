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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.sling.feature.Extension;
import org.apache.sling.feature.Feature;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Mojo(name = "compare-features")
public class CompareFeaturesMojo extends AbstractIncludingFeatureMojo {
    @Parameter
    FeatureSelectionConfig selection;

    @Parameter
    List<String> extensions;

    @Parameter
    String failureMessage;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("**** " + failureMessage);

        Collection<Feature> featuresToCheck = getSelectedFeatures(selection).values();

        if (featuresToCheck.size() < 2) {
            getLog().error("Comparing less than 2 features makes no sense: " + selection);
            return;
        }

        Iterator<Feature> it = featuresToCheck.iterator();
        Feature goldenFeature = it.next();

        while(it.hasNext()) {
            Feature compareFeature = it.next();
            compareFeatures(goldenFeature, compareFeature);
        }
    }

    void compareFeatures(Feature goldenFeature, Feature compareFeature)
            throws MojoExecutionException {
        if (extensions != null) {
            compareExtensions(goldenFeature, compareFeature);
        }
    }

    private void compareExtensions(Feature goldenFeature, Feature compareFeature)
            throws MojoExecutionException {
        for (String extension : extensions) {
            Extension extGold = goldenFeature.getExtensions().getByName(extension);
            Extension extComp = compareFeature.getExtensions().getByName(extension);

            if (extGold == null && extComp == null) {
                continue;
            }

            if (extGold.getType() != extComp.getType()) {
                throw new MojoExecutionException("Extensions are of different type: " + extGold
                        + " and " + extComp);
            }

            if (extGold.getState() != extComp.getState()) {
                throw new MojoExecutionException("Extensions are of different state: " + extGold
                        + " and " + extComp);
            }

            switch (extGold.getType()) {
            case ARTIFACTS:
                if (!extGold.getArtifacts().equals(extComp.getArtifacts()))
                    throw new MojoExecutionException("Artifacts are not the same: " + extGold
                            + " and " + extComp);
                break;
            }
        }
    }
}
