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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.sling.feature.Extension;
import org.apache.sling.feature.Extensions;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.extension.apiregions.api.ApiExport;
import org.apache.sling.feature.extension.apiregions.api.ApiRegion;
import org.apache.sling.feature.extension.apiregions.api.ApiRegions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.json.JsonArray;


@Mojo(name = "api-regions-crossfeature-duplicates",
    defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class ApiRegionsOverlapCheckMojo extends AbstractIncludingFeatureMojo {
    private static final String PROPERTY_FILTER = ApisJarMojo.class.getName() + ".filter";

    @Parameter
    FeatureSelectionConfig selection;

    @Parameter
    Set<String> regions;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("Selected features: " + selection);
        System.out.println("Actual features: " + getSelectedFeatures(selection));
        System.out.println("Regions: " + regions);

        if (regions == null || regions.size() == 0) {
            throw new MojoExecutionException("Please specify at least one region to check for duplicate exports");
        }

        Map<FeatureIDRegion, Set<String>> featureExports = new HashMap<>();
        Map<String, Feature> fs = getSelectedFeatures(selection);
        for (Map.Entry<String, Feature> f : fs.entrySet()) {
            ApiRegions fRegions = getApiRegions(f.getValue());
            System.out.println("Feature ID: " + f.getKey());
            System.out.println("API Regions: " + fRegions);

            for(ApiRegion r : fRegions.listRegions()) {
                if (!regions.contains(r.getName())) {
                    continue;
                }

                FeatureIDRegion mapKey = new FeatureIDRegion(f.getKey(), r.getName());
                Set<String> el = featureExports.get(mapKey) ;
                if (el == null) {
                    el = new HashSet<>();
                    featureExports.put(mapKey, el);
                }
                for (ApiExport ex : r.listExports()) {
                    el.add(ex.getName());
                }
            }
        }

        if (fs.size() < 2) {
            // Not 2 or more features, so no overlap to check
            return;
        }

        boolean overlapFound = false;
        List<FeatureIDRegion> keyList = new ArrayList<>(featureExports.keySet());
        for (int i=0; i<keyList.size(); i++) {
            FeatureIDRegion key1 = keyList.get(i);
            for (int j=i+1; j<keyList.size(); j++) {
                FeatureIDRegion key2 = keyList.get(j);
                Set<String> exp1 = featureExports.get(key1);
                Set<String> exp2 = featureExports.get(key2);
                overlapFound |= checkOverlap(key1, exp1, key2, exp2);
            }
        }

        if (overlapFound) {
            throw new MojoExecutionException("Errors found, see log");
        }
    }

    private boolean checkOverlap(FeatureIDRegion key1, Set<String> exp1, FeatureIDRegion key2, Set<String> exp2) {
        if (key1.equals(key2)) {
            // Don't compare a region with itself
            return false;
        }

        Set<String> s = new HashSet<>(exp1);

        s.retainAll(exp2);
        if (s.size() == 0) {
            // no overlap
            return false;
        }

        getLog().error("Overlap found between " + key1 + " and " + key2 + ". Both export: " + s);
        return true;
    }

    // All the stuff below here is copied from the ApisJarMojo - need to refactor and share TODO
    // TODO ?
    private boolean incrementalApis = false;
    private Set<String> includeRegions = Collections.singleton("*");
    private Set<String> excludeRegions = Collections.emptySet();

    // Copied from ApisJarMojo TODO
    /**
     * Get the api regions for a feature If the feature does not have an api region
     * an artificial global region is returned.
     *
     * @param feature The feature
     * @return The api regions or {@code null} if the feature is wrongly configured
     *         or all regions are excluded
     * @throws MojoExecutionException If an error occurs
     */
    private ApiRegions getApiRegions(final Feature feature) throws MojoExecutionException {
        ApiRegions regions = new ApiRegions();

        Extensions extensions = feature.getExtensions();
        Extension apiRegionsExtension = extensions.getByName(ApiRegions.EXTENSION_NAME);
        if (apiRegionsExtension != null) {
            if (apiRegionsExtension.getJSONStructure() == null) {
                getLog().info(
                        "Feature file " + feature.getId().toMvnId() + " declares an empty '" + ApiRegions.EXTENSION_NAME
                    + "' extension, no API JAR will be created");
                regions = null;
            } else {
                ApiRegions sourceRegions;
                try {
                    sourceRegions = ApiRegions
                            .parse((JsonArray) apiRegionsExtension.getJSONStructure());
                } catch (final IOException ioe) {
                    throw new MojoExecutionException(ioe.getMessage(), ioe);
                }

                // calculate all api-regions first, taking the inheritance in account
                for (final ApiRegion r : sourceRegions.listRegions()) {
                    if (r.getParent() != null && !this.incrementalApis) {
                        for (final ApiExport exp : r.getParent().listExports()) {
                            r.add(exp);
                        }
                    }
                    if (isRegionIncluded(r.getName())) {
                        regions.add(r);
                    } else {
                        getLog().debug("API Region " + r.getName()
                            + " will not processed due to the configured include/exclude list"); // TODO
                    }
                }

                // prepare filter
                for (final ApiRegion r : regions.listRegions()) {
                    for (final ApiExport e : r.listExports()) {
                        e.getProperties().put(PROPERTY_FILTER, packageToScannerFiler(e.getName(), true));
                    }
                }

                if (regions.isEmpty()) {
                    getLog().info("Feature file " + feature.getId().toMvnId()
                            + " has no included api regions, no API JAR will be created");
                    regions = null;
                }
            }
        } else {
            // create exports on the fly
            regions.add(new ApiRegion(ApiRegion.GLOBAL) {

                @Override
                public ApiExport getExportByName(final String name) {
                    ApiExport exp = super.getExportByName(name);
                    if (exp == null) {
                        exp = new ApiExport(name);
                        this.add(exp);
                    }
                    return exp;
                }
            });
        }

        return regions;
    }

    // TODO copied from ApisJarMojo
    /**
     * Check if the region is included
     *
     * @param name The region name
     * @return {@code true} if the region is included
     */
    private boolean isRegionIncluded(final String name) {
        boolean included = false;
        for (final String i : this.includeRegions) {
            if ("*".equals(i) || i.equals(name)) {
                included = true;
                break;
            }
        }
        if (included && this.excludeRegions != null) {
            for (final String e : this.excludeRegions) {
                if (name.equals(e)) {
                    included = false;
                    break;
                }
            }
        }

        return included;
    }

    private static String packageToScannerFiler(String api, boolean strict) {
        return (strict ? "*": "**") + '/' + api.replace('.', '/') + "/*";
    }

    private static class FeatureIDRegion {
        private final String featureID;
        private final String region;

        private FeatureIDRegion(String featureID, String region) {
            this.featureID = featureID;
            this.region = region;
        }

        private String getFeatureID() {
            return featureID;
        }

        private String getRegion() {
            return region;
        }

        @Override
        public int hashCode() {
            return Objects.hash(featureID, region);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FeatureIDRegion other = (FeatureIDRegion) obj;
            return Objects.equals(featureID, other.featureID) && Objects.equals(region, other.region);
        }

        @Override
        public String toString() {
            return "Feature: " + featureID + ", Region: " + region;
        }
    }
}
