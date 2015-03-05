rdod-java
===========
Radial density outlier dection. implements a radial density outlier dection algo in N dimensions.
Then follows up with K-Means classification.

## Dependencies

* Built with Maven and Java 1.7

## Bugs
For bug reports and issues use the [github issues page](https://github.com/rosasaul/rdod-java/issues), try to make sure I'll be able to reproduce the issue, with example data sets.

## Installation
Jar can be compiled on your system using [Maven](http://maven.apache.org/) 
```bash
mvn package
```

Or just include the java source in your path
  
  /org/megaframe/stats/RadialDensityOutlier.java


## Usage
I've included a small command line utility for reading in and classifying data from a CSV. 
Script assumes it's standard CSV format, with the top line being a header, and comma seperated floating point numbers.
If your CSV contains additional columns that aren't vector data you can select the vector columns you want to use by using the option -match and handing it a java regex match string

To build the CL tool
```bash
mvn package 
cat javaStub.sh target/stats-1.0-SNAPSHOT.jar > rdod
```

Using the Library RadialDensityOutlier directly
```java
// Create a new instance
RadialDensityOutlier rdod = new RadialDensityOutlier();

// Optionally change the number of itterations to converge on
rdod.itter(itter);

/* Preform the categorization
 * data is an ArrayList of double[]
 * rsigma is a double which is the sigma radial distance you want to use
 * neighbors is an integer number of neighbors a data point must have to not be an outlier
 * ksets is the number of K-Means Groups you want to categorize the remaining data on
 */
int[] categories = rdod.categorize(data,rsigma,neighbors,ksets);
```

## Copyright and Licence
rdod-java simple java class for finding outliers in N dimensional clustered data.
Copyright (C) 2015 Saul Rosa

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

