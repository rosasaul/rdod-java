package org.megaframe.stats;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test for rdod - Radial Density Outlier
 * @author Saul Rosa
 */
public class RadialDensityOutlierTest {
  private static rdod = new RadialDensityOutlier();

  /**
   * Test the radial distance calculation
   */
  @Test
  public void testDistance(){
    double[] vecA = {1,1};
    double[] vecB = {0,0};
    double result = 1; 
    assertEquals("Radial Distance computes as 1.0", result, rdod.distance(vecA,vecB);
  }
}
