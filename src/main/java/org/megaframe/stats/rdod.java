package org.megaframe.stats;

import static org.kohsuke.args4j.ExampleMode.ALL;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;

/**
 * Reads in a CSV and Adds in a column "Category" with the
 * assigned K-Means Category
 * Group 1 - Is always outliers
 * Utilizes RadialDensityOutlier
 * @author Saul Rosa
 * @version 1
 */
public class rdod {

  // Setup CLI Options
  @Option(name="-help",usage="Print This Help Menu")
  private boolean help;
  
  @Option(name="-output",usage="output to this file",metaVar="OUTPUT")
  private String output;

  @Option(name="-match",usage="Regex string match for columns to use",metaVar="REGEX")
  private String match = "";

  @Option(name="-rsigma",usage="Radial Sigma to limit by, default 3",metaVar="SIGMA")
  private double rsigma = 3;

  @Option(name="-neighbors",usage="Minimum number of neighbors, default 6",metaVar="NEIGHBORS")
  private int neighbors = 6;

  @Option(name="-rdist",usage="Radial distance to limit by, overides -rsigma, no default",metaVar="DISTANCE")
  private double rdist = 0;

  @Option(name="-ksets",usage="Number of K-Means groups to form, no default",metaVar="KSETS")
  private int ksets = 0;

  @Option(name="-itter",usage="Number of Itterations to loop, 0 is infinite, default 0",metaVar="ITTER")
  private int itter = 0;

  @Option(name="-delta",usage="Delta change limit when to stop K-Means search, default 0.0001",metaVar="DELTA")
  private double delta = 0.0001;

  // receives other command line parameters than options
  @Argument
  private List<String> inputFiles = new ArrayList<String>();

  public static void main(String[] args) throws IOException {
    new rdod().doMain(args);
  }

  public void doMain(String[] args) throws IOException {
    CmdLineParser parser = new CmdLineParser(this);
    try {
      parser.parseArgument(args);
      if( help ){
        throw new CmdLineException(parser,"Help Menu"); }
      if( inputFiles.isEmpty() ){
        throw new CmdLineException(parser,"No Input File is given"); }
      if( ksets == 0 ){
        throw new CmdLineException(parser,"ksets must be set to an integer greater than 0"); }

    } catch( CmdLineException e ) {
      System.err.println(e.getMessage());
      System.err.println("rdod [options...] files...");
      parser.printUsage(System.err);
      System.err.println("\n  Example: rdod"+parser.printExample(ALL)+" inputFile");
      return;
    }

    PrintStream ps = null;
    if(output != null){
      ps = new PrintStream(output);
      System.setOut(ps);
    }

    csvReader csv = new csvReader();
    ArrayList data = null;
    try {
      data = csv.parseCSV(inputFiles.get(0),match);
    } catch(IOException e) {
      System.err.format("Exception occurred trying to read '%s'.", inputFiles.get(0));
      System.err.println("Error : " + e.getMessage());
      if(output != null){ ps.close(); }
      return;
    }

    // Create New RadialDensityOutlier Object
    RadialDensityOutlier rdod = new RadialDensityOutlier();

    // Setup configurations
    rdod.itter(itter).neighbors(neighbors).ksets(ksets).rsigma(rsigma).delta(delta);
    if(rdist > 0){ rdod.rdist(rdist); } // RadialDensityOutlier will default to use rdistance when it's hard set

    // Run Categorization Process
    int[] categories = rdod.categorize(data);

    // Print the returned categories
    System.out.println("Category," + csv.getHeader());
    for(int row = 0; row < categories.length; row++){
      System.out.println(categories[row]+","+csv.getLine(row));
    }

    if(output != null){ ps.close(); }
  }
}

/** CSV File parser for rdod
 * Reads the input CSV grabs the required columns
 * Keeps the original Lines for dumping back content
 */
class csvReader {
  // Original Lines Coming in
  private ArrayList<String> origLines = new ArrayList<String>();
  private String lineHeader = null;
  private Hashtable<String, Integer> header = new Hashtable<String, Integer>();
  private ArrayList<String> matchedCols = new ArrayList<String>();

  public ArrayList parseCSV(String input, String matcher) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(input));
    ArrayList data = new ArrayList();
    lineHeader = reader.readLine();
    String[] headerItems = lineHeader.split(",");
    for(int i = 0; i < headerItems.length; i++){
      header.put(headerItems[i],i);
      if(headerItems[i].matches(matcher)){ matchedCols.add(headerItems[i]); }
    }

    String line;
    while ((line = reader.readLine()) != null){
      origLines.add(line);
      double[] vector = new double[matchedCols.size()];
      String[] items = line.split(",");
      int i = 0;
      for(String col : matchedCols){
        vector[i] = Double.parseDouble( items[ header.get(col) ] );
        i++;
      }
      data.add(vector);
    }
    return data;
  }

  public String getHeader(){ return this.lineHeader; }

  public String getLine(int row){ return this.origLines.get(row); }
}


