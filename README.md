[<img src="http://sling.apache.org/res/logos/sling.png"/>](http://sling.apache.org)

 [![Build Status](https://builds.apache.org/buildStatus/icon?job=sling-slingfeature-maven-plugin-1.8)](https://builds.apache.org/view/S-Z/view/Sling/job/sling-slingfeature-maven-plugin-1.8) [![Test Status](https://img.shields.io/jenkins/t/https/builds.apache.org/view/S-Z/view/Sling/job/sling-slingfeature-maven-plugin-1.8.svg)](https://builds.apache.org/view/S-Z/view/Sling/job/sling-slingfeature-maven-plugin-1.8/test_results_analyzer/) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Apache Sling OSGi Feature Maven Plugin

This module is part of the [Apache Sling](https://sling.apache.org) project.

Maven Plugin for OSGi Applications

# Global Configuration

* features : The directory containing the feature files. The default is `src/main/features` and all files ending with `.json` are read including all sub directories.

## Supported goals

### aggregate-features

Produce an aggregated feature from a list of features. The list of features is either specified by include/exclude patterns based on the configured features directory of the project or Maven coordinates of features.

Sample configuration:

```
  <plugin>
    <groupId>org.apache.sling</groupId>
    <artifactId>slingfeature-maven-plugin</artifactId>
    <version>0.2.0-SNAPSHOT</version>
    <executions>
      <execution>
        <id>merge-features</id>
        <goals>
          <goal>aggregate-features</goal>
        </goals>
        <configuration>
          <aggregateClassifier>my-aggregated-feature</aggregateClassifier>
          <aggregates>
            <directory>
              <includes>*.json</includes>
              <excludes>exclude-me.json</excludes>
              <excludes>exclude-me-too.json</excludes>
            </directory>
            <artifact>
              <groupId>org.apache.sling</groupId>
              <artifactId>org.apache.sling.myfeatures</artifactId>
              <version>1.2.3</version>
              <type>slingfeature</type>
              <classifier>someclassifier</classifier>
            </artifact>
          </aggregates>
          <variables>
            <!-- Feature variables can be specified/overridden here -->
            <https.port>8443</https.port>
            <some.variable/> <!-- set some.variable to null -->
          </variables>
          <frameworkProperties>
            <!-- Framework property overrides go here -->
            <org.osgi.framework.bootdelegation>sun.*,com.sun.*</org.osgi.framework.bootdelegation>
          </frameworkProperties>
        </configuration>
      </execution>
    </executions>
  </plugin>  
```

All features found in the directory as well as the artifact sections of the plugin configuration are aggregated into a single feature. Includes are processed in the way they appear in the configuration. If an include contains a pattern which includes more than one feature, than the features are included based on their full alphabetical file path. The features are aggregated in the order they are included.

If an include or an exclude is not using a pattern but directly specifying a file, this file must exists. Otherwise the build fails.

The merged feature will have the same `groupId`, `artifactId` and `version` as the pom in which the aggregation is configured. It will have type `slingfeature` and as classifier the one specified in the configuration named `aggregateClassifier`.

Variables and framework properties can be overridden using the `<variables>` and
`<fraweworkProperties>` sections. If multiple definitions of the same variables are found
in the feature that are to be aggregated and the values for these variables are different,
they *must* be overridden, otherwise the aggregation will generate an error.


#### Extension merging

Merging of extensions is specific to the extension being merged. Handlers can be provided to implement the logic of extension merging. A handler needs to implement the `org.apache.sling.feature.builder.FeatureExtensionHandler` and is looked up via the Java ServiceLoader mechanism.

To provide additional handlers to the `slingfeature-maven-plugin`, list the artifacts in the `<dependencies>`
section of the plugin configuration:

```
  <plugin>
    <groupId>org.apache.sling</groupId>
    <artifactId>slingfeature-maven-plugin</artifactId>
    <version>0.2.0-SNAPSHOT</version>
    <executions>
      ...
    </executions>
    <dependencies>
      <dependency>
        <groupId>org.apache.sling</groupId>
        <artifactId>my-feature-extension-handler</artifactId>
        <version>1.0.0</version>
      </dependency>
    </dependencies>
  </plugin>  
```

### attach-features
Attach feature files found in the project to the projects produced artifacts. This includes features
found in `src/main/features` as well as features produce with the `aggregate-features` goal.
