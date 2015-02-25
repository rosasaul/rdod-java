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
  private static RadialDensityOutlier rdod = new RadialDensityOutlier();
  private static final double DELTA = 1e-15;

  /**
   * Test the radial distance calculation
   */
  @Test
  public void testDistance(){
    double[] vecA = {1,0};
    double[] vecB = {0,0};
    double expected = 1;
    double result = rdod.distance(vecA,vecB);
    System.out.println("Expected: "+expected+" Result: "+result);
    // Not exact because of sqrt function, floats may not match
    assertEquals(result, expected, DELTA);
  }
}
