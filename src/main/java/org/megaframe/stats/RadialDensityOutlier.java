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
  /** Main Function of the outlier detection
   * @param list ArrayList of N dimensional double[], all vectors must be the same size
   *             Each list item is one point, with it's axis defined by the array double[]
   * @param rsigma Radial sigma distance to set as threshold for coulding neighbors
   *        radial_limit = average_distance_to_closest_neighbor + mu_of_neighbor_distances * rsigma
   * @param minNeighbors int minimum number of neighbors to not be an outlier
   * @param ksets int number of K-Means clusters to run
   */
  public int[] categorize(ArrayList list, double rsigma, double minNeighbors, int ksets ) throws IllegalArgumentException {
    // Test Requirments of data set and throw errors were needed
    if(list.size() == 0){ throw new IllegalArgumentException("Data set cannot be empty."); }
    if(ksets < 1){ throw new IllegalArgumentException("Number of K-Means clusters must be at least 1."); }
    if(minNeighbors < 1){ throw new IllegalArgumentException("Number of minimum neighbors must be at least 1."); }
    
    int dimensions = 0;
    for (int i = 0; i < list.size(); i++) {
      double[] vector = (double[])list.get(i);
      if(dimensions == 0){ dimensions = vector.length; }
      else if (dimensions != vector.length){ throw new IllegalArgumentException("All vectors must have the same dimensions."); }
    }
    if(dimensions == 0){ throw new IllegalArgumentException("Vectors must have 1 or more dimenstions."); }

    int[] category = new int[list.size()];
    ArrayList clusterCenter = new ArrayList();

    // Collect scaling info per dimension
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

    // Scale local dataset 0.0-1.0
    int saveVector = 1;
    for (int i = 0; i < list.size(); i++) {
      double[] vector = (double[])list.get(i);
      for (int j = 0; j < vector.length; j++){
        vector[j] = (vector[j] - min[j]) / (max[j] - min[j]);
      }
      if(saveVector == 1){ clusterCenter.add((Object)vector); }
      if(clusterCenter.size() >= ksets){ saveVector = 0; }
    }
    // Find Median Radial distances for all points
    double[] closestNeighbor = new double[list.size()];
    int unset = 1;
    for (int i = 0; i < list.size(); i++) {
      for (int k = 0; k < list.size(); k++) {
        if(i == k){ continue; }
        double test_distance = distance((double[])list.get(i),(double[])list.get(k));
        if(unset == 1){ closestNeighbor[i] = test_distance; unset++; }
        else if(test_distance < closestNeighbor[i]){ closestNeighbor[i] = test_distance; }
      }
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
        if(i == k){ continue; }
        if(distance((double[])list.get(i),(double[])list.get(k)) < maxDistanceLimit){ neighbors++; }
      }
      // Outlier if Sub minimum number
      if(neighbors < minNeighbors){ category[i] = 1; }
      // Find distances to each K-Means center
      else{
        int cluster = 0;
        double clusterMinDistance = 0;
        for (int j = 0; j < ksets; j++){
          double clusterDistance = distance((double[])list.get(i),(double[])clusterCenter.get(j));
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
    double loopDelta = 2 * delta;
    while(loopDelta > delta){
      // Update cluster centers
      for(int k = 0; k < ksets; k++){
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
        for (int j = 0; j < dimensions; j++){ sumPoints[j] = sumPoints[j] / clusterCount; }
        double minDistance = 1;
        for(int i = 0; i < list.size(); i++) {
          double[] vector = (double[])list.get(i);
          double test_distance = distance(vector,sumPoints);
          if(minDistance < test_distance){
            minDistance = test_distance;
            clusterCenter.set(k,vector);
          }
        }
      }
      for(int i = 0; i < list.size(); i++) {
        if(category[i] == 1){ continue; } // Don't reassign outliers
        // Assign to cluster
        double[] vector = (double[])list.get(i);
        double minDistance = 1;
        for(int k = 0; k < ksets; k++){
          double test_distance = distance(vector,(double[])clusterCenter.get(k));
          if(minDistance < test_distance){
            minDistance = test_distance;
            category[i] = k + 2;
          }
        }
      }
    }
    return category; 
  }

  public double getDelta(){ return delta; }

  public void setDelta(double updateDelta) throws IllegalArgumentException {
    if(updateDelta <= 0){ throw new IllegalArgumentException("Delta must be greater than 0."); }
    delta = updateDelta;
  }

  private double distance(double[] vecA, double[] vecB){
    double sum = 0;
    for(int i = 0; i < vecA.length; i++){
      sum += ((vecA[i] - vecB[i]) * (vecA[i] - vecB[i]));
    }
    return sqrt(sum);
  }

  private double average(double[] vector){
    double sum = 0;
    for(int i = 0; i < vector.length; i++){ sum += vector[i]; }
    return sum / vector.length;
  }

  private double sigma(double[] vector){
    double avg = average(vector);
    double sum = 0;
    for(int i = 0; i < vector.length; i++){
      sum += ((vector[i] - avg) * (vector[i] - avg));
    }
    return sqrt(sum / vector.length);
  }
 
  public static void main( String[] args ){
    System.out.println( "Hello World!" );
  }
}
