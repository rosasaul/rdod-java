package org.megaframe.stats;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Reads in a CSV and Adds in a column "Category" with the
 * assigned K-Means Category
 * Group 1 - Is always outliers
 * Utilizes RadialDensityOutlier
 * @author Saul Rosa
 * @version 1
 */
public class rdod {
  // Setup Logger for CLI
  private static final Logger log = Logger.getLogger(rdod.class.getName());

  private static String input = null;
  //Output File
  private static String output = null;

  public static ArrayList list = null;

  public static double rsigma = 0;

  public static int minNeighbors = 0;

  public static int ksets = 0;

  public static void main(String[] args){
    // Parse Args
    rdod rdod = new rdod();
    rdod.parseArgs(args);
    rdod.processFile(input);

    RadialDensityOutlier engine = new RadialDensityOutlier();
    double[] categories = engine.categorize(list,rsigma,minNeighbors,ksets);
    dumpOutput(output,categories,list);
  }

  private void parseArgs(String[] args) {
    Options options = new Options();
    options.addOption("h", "help", false, "show help.");
  }

}


