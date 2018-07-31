Constellation
=====

[![Build Status](https://travis-ci.org/NLeSC/Constellation.svg?branch=master)](https://travis-ci.org/NLeSC/Constellation)
[![codecov.io](https://codecov.io/github/NLeSC/Constellation/coverage.svg?branch=master)](https://codecov.io/github/NLeSC/Constellation/branch/master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3d91218e97234c71a96eff191483908e)](https://www.codacy.com/app/NLeSC/Constellation?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=NLeSC/Constellation&amp;utm_campaign=Badge_Grade)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.1218876.svg)](https://doi.org/10.5281/zenodo.1218876)

Copyright 2018 The Netherlands eScience Center

## What problem does Constellation solve?

The scientific computing landscape is becoming more and more complex.
Traditional supercomputers and clusters, grid and cloud infrastructures, many-core technologies,
data distribution, specific hardware availability, software heterogeneity,
they all contribute to this complexity, and often force scientists to use multiple computing platforms simultaneously.

Constellation is a software platform/library specifically aimed at distributed, heterogeneous and hierarchical computing environments.
In Constellation, applications consist of several distinct (but somehow related) activities.
These activities can be implemented independently using existing, well understood tools (e.g. MPI, CUDA, etc.).
Constellation is then used to construct the overall application by coupling the distinct activities.
Using application defined labels in combination with context-aware work stealing, Constellation provides a
simple and efficient mechanism for automatically mapping the activities to the appropriate resources,
taking data locality and heterogeneity into account.

Additional text: TODO

## Downloading and building Constellation

Constellation is available from its repository at Github: [https://github.com/nlesc/constellation](https://github.com/nlesc/constellation).
To download and build:

```bash
git clone https://github.com/nlesc/constellation.git
cd constellation
./gradlew jar
```

To run some tests:
```bash
./gradlew test
./gradlew integrationTest
```

## Adding Constellation as a dependency to your project

To include Constellation as a dependency for Gradle, use:

```gradle
        allprojects {
                repositories {
                        jcenter()
                }
        }
```

and

```gradle
        dependencies {
                compile 'nl.junglecomputing:constellation:2.0.0'
        }

```

For Maven, use:

```maven
        <repositories>
                <repository>
                    <id>jcenter</id>
                    <url>https://jcenter.bintray.com</url>
                </repository>
        </repositories>
```

and


```maven
        <dependency>
            <groupId>nl.junclecomputing</groupId>
            <artifactId>constellation</artifactId>
            <version>2.0.0</version>
        </dependency>
```

## Documentation

Constellation's Javadoc is available from the JCenter repository, as a separate jar, [here](http://jcenter.bintray.com/nl/junglecomputing/constellation/2.0.0/constellation-2.0.0-javadoc.jar).
## Legal

The Constellation library is copyrighted by the Netherlands eScience Center and released
under the Apache License, Version 2.0. A copy of the license may be obtained
from [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0).
