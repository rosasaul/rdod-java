package org.megaframe.stats;

import java.util.*;
import static java.lang.Math.sqrt;

/**
 * RadialDensityOutlier Object catergorizes an 
 * N dimensional set of double's. ArrayList is used to
 * pacakge the data so it can be any size
 * @author Saul Rosa
 * @version 1
 */
public class RadialDensityOutlier {
  /** Change step minimum on K-Means convergance */
  private double delta = 0.0001;
  /** Number of itterations before stopping K-Means convergance, 0 is infinite */
  private int itter = 0;
  
  /** Main Function of the outlier detection
   * @param list ArrayList of N dimensional double[], all vectors must be the same size
   *             Each list item is one point, with it's axis defined by the array double[]
   * @param rsigma Radial sigma distance to set as threshold for coulding neighbors
   *        radial_limit = average_distance_to_closest_neighbor + mu_of_neighbor_distances * rsigma
   * @param minNeighbors int minimum number of neighbors to not be an outlier
   * @param ksets int number of K-Means clusters to run
   */
  public int[] categorize(
      ArrayList list, double rsigma, double minNeighbors, int ksets )
    throws IllegalArgumentException {

    // Test Requirments of data set and throw errors were needed
    if(list.size() == 0){
      throw new IllegalArgumentException("Data set cannot be empty."); }
    if(ksets < 1){
      throw new IllegalArgumentException("Number of K-Means clusters must be at least 1."); }
    if(minNeighbors < 1){
      throw new IllegalArgumentException("Number of minimum neighbors must be at least 1."); }
    
    int dimensions = vectorDimensions(list);

    int[] category = new int[list.size()];
    ArrayList clusterCenter = new ArrayList();

    vectorNormalize(list,dimensions,clusterCenter,ksets);

    // Find Median Radial distances for all points
    double[] closestNeighbor = new double[list.size()];
    boolean unset = true; // Used to track if the base distance was set

    for (int i = 0; i < list.size(); i++) {
      for (int k = 0; k < list.size(); k++) {
        if(i == k){ continue; }
        double test_distance = _distance((double[])list.get(i),(double[])list.get(k));
        if(unset == true){ closestNeighbor[i] = test_distance; unset = false; }
        else if(test_distance < closestNeighbor[i]){ closestNeighbor[i] = test_distance; }
      }
      unset = true; //Rest for the next vector
    }
    
    // Set a Maximum distance range
    double avg = average(closestNeighbor);
    double mu = sigma(closestNeighbor);
    double maxDistanceLimit = avg + (mu * rsigma);

    // Loop on all points
    for (int i = 0; i < list.size(); i++) {
      // Find number of neighbers inside Maximum distance
      int neighbors = 0;
      for (int k = 0; k < list.size(); k++) {
        if(i == k){ continue; } // Skip on self
        double dist = _distance((double[])list.get(i),(double[])list.get(k));
        if(dist < maxDistanceLimit){ neighbors++; }
      }
      // Outlier if Sub minimum number
      if(neighbors < minNeighbors){ category[i] = 1; }
      // Find distances to each K-Means center
      else{
        int cluster = 0;
        double clusterMinDistance = 0;
        for (int j = 0; j < ksets; j++){
          double clusterDistance = _distance((double[])list.get(i),(double[])clusterCenter.get(j));
          if(cluster == 0){
            cluster = j + 1;
            clusterMinDistance = clusterDistance;
          }
          else if( clusterDistance < clusterMinDistance){
            cluster = j + 1;
            clusterMinDistance = clusterDistance;
          }
        }
        // Assign to cluster
        category[i] = cluster + 1; //Group 1 is Outliers, so add one for offset
      }
    }
    // Loop until sub delta (delta is largest of the change in distance of cluster center points)
    double loopDelta = 2 * this.delta;
    int loopItter = this.itter;
    while(loopDelta > this.delta){
      // Update cluster centers
      for(int k = 0; k < ksets; k++){
        //Get the sum of vectors in this cluster and the sum of each vector
        int clusterCount = 0;
        double[] sumPoints = new double[dimensions];
        for(int i = 0; i < list.size(); i++) {
          double[] vector = (double[])list.get(i);
          if(category[i] == k + 2){
            clusterCount++;
            for (int j = 0; j < dimensions; j++){
              sumPoints[j] += vector[j];
            }
          }
        }
        // Average out the sum of each vector
        for (int j = 0; j < dimensions; j++){ sumPoints[j] = sumPoints[j] / clusterCount; }
        clusterCenter.set(k,sumPoints);

        /* Re-Assign to vectors instead of finding average center
        double minDistance = 1;
        for(int i = 0; i < list.size(); i++) {
          double[] vector = (double[])list.get(i);
          double test_distance = _distance(vector,sumPoints);
          if(minDistance < test_distance){
            minDistance = test_distance;
            clusterCenter.set(k,vector);
          }
        }
        */
      }

      // Go through each list vector and re-assign to their closest K-Set
      for(int i = 0; i < list.size(); i++) {
        if(category[i] == 1){ continue; } // Don't reassign outliers
        // Assign to cluster
        double[] vector = (double[])list.get(i);
        double minDistance = 1;
        for(int k = 0; k < ksets; k++){
          double test_distance = _distance(vector,(double[])clusterCenter.get(k));
          if(minDistance < test_distance){
            minDistance = test_distance;
            category[i] = k + 2;
          }
        }
      }
      // Terminate looping at a set number of itterations
      if(loopItter > 0){
        loopItter--;
        if(loopItter == 0){ break; } // Only break here so 0 means infinite
      }
    }
    return category; 
  }

  /** Check the incoming vector dimensions and throw exceptions 
   * All dimensions must match.
   * Dimensions must be above 0.
   * @param list ArrayList of double[] vectors
   */
  public int vectorDimensions(ArrayList list) throws IllegalArgumentException {
    int dimensions = 0;
    for (int i = 0; i < list.size(); i++) {
      double[] vector = (double[])list.get(i);
      if(dimensions == 0){ dimensions = vector.length; }
      else if (dimensions != vector.length){
        throw new IllegalArgumentException("All vectors must have the same dimensions."); }
    }
    if(dimensions == 0){
      throw new IllegalArgumentException("Vectors must have 1 or more dimenstions."); }
    return dimensions;
  }

  /** Get the set value of delta, used to determing Clusters are no longer moving */
  public double delta(){ return this.delta; }

  /** Set the value of delta, used to determing Clusters are no longer moving
   * @param delta double value to be used instead of default delta
   */
  public double delta(double delta){
    this.delta = delta;
    return this.delta;
  }

  /** Get the number of itterations the convergance loop will run */
  public int itter(){ return this.itter; }

  /** Set the number of itterations the convergance loop will run
   * @param itter interger number of steps to run, 0 is infinite, default 0
   */
  public int itter(int itter){
    this.itter = itter;
    return this.itter;
  }

  /** Normalize the vector and return center points for cluster convergence.
   * normalizing occurs on the vectors themselfs
   * @param list ArrayList of double[] vectors
   * @param dimensions integer size of the vectors from list
   * @param clusterCenter ArrayList pointer which will be used to store the clusters assignments
   * @param ksets number of cluster centers to save
   */
  private void vectorNormalize(ArrayList list, int dimensions,ArrayList clusterCenter, int ksets){
    // Collect scaling (maximum/minimum) info per dimension
    double[] max = new double[dimensions];
    double[] min = new double[dimensions];
    for (int j = 0; j < dimensions; j++){ min[j] = 1; }
    for (int i = 0; i < list.size(); i++) {
      double[] vector = (double[])list.get(i);
      for (int j = 0; j < vector.length; j++){
        if(max[j] < vector[j]){ max[j] = vector[j]; }
        if(min[j] > vector[j]){ min[j] = vector[j]; }
      }
    }

    // Scale local dataset 0.0-1.0, Not always the best choice in K-Means
    // This will depend on the type of data set, and clustering observed, but it's assumed correct here
    boolean saveClusterVector = true;
    for (int i = 0; i < list.size(); i++) {
      double[] vector = (double[])list.get(i);
      for (int j = 0; j < vector.length; j++){
        vector[j] = (vector[j] - min[j]) / (max[j] - min[j]);
      }
      // Saving the cluster centers, done here so I don't have to loop on this again
      // IMPORTANT, cluster centers must come from assigned data points to ensure convergence
      if(saveClusterVector == true){ clusterCenter.add((Object)vector); }
      if(clusterCenter.size() >= ksets){ saveClusterVector = false; }
    }
  }

  /** Compute the euclidean distance between two vectors
   * @param vecA double[] point definition
   * @param vecB double[] point definition
   * @return double distance between the two points
   */
  public double distance(double[] vecA, double[] vecB){
    if(vecA.length != vecB.length){
      throw new IllegalArgumentException("All vectors must have the same dimensions.");
    }
    return _distance(vecA,vecB);
  }

  /** Private method to compute euclidean distance between two vectors
   * Assums vector sizes are the same
   * @param vecA double[] point definition
   * @param vecB double[] point definition
   * @return double distance between the two points
   */
  private double _distance(double[] vecA, double[] vecB){
    double sum = 0;
    for(int i = 0; i < vecA.length; i++){
      sum += ((vecA[i] - vecB[i]) * (vecA[i] - vecB[i]));
    }
    return sqrt(sum);
  }

  /** Computer the average of a set of doubles
   * @param vector set of double numbers
   * @return double average
   */
  private double average(double[] vector){
    double sum = 0;
    for(int i = 0; i < vector.length; i++){ sum += vector[i]; }
    return sum / vector.length;
  }

  /** Compute the standard deviation (sigma) of a data set
   * @param vector set of double numbers
   * @return double sigma
   */
  private double sigma(double[] vector){
    double avg = average(vector);
    double sum = 0;
    for(int i = 0; i < vector.length; i++){
      sum += ((vector[i] - avg) * (vector[i] - avg));
    }
    return sqrt(sum / vector.length);
  }
}


