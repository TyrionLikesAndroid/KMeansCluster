import javax.swing.text.Style;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class KMeansClusterTestHarness {

    static final String IRIS_DELIMETER = ",";    // Comma delimiter for the iris file
    static final int NUM_IRIS_DATA_COLUMNS = 4; // Total number of iris columns
    static final int NUM_IRIS_DATA_ROWS = 150; // Total number of iris rows
    static final int NUM_KMTEST_DATA_COLUMNS = 2; // Total number of kmtest columns
    static final int NUM_KMTEST_DATA_ROWS = 21; // Total number of kmtest rows

    static float [][] irisDataSet;     // Data member for our iris dataset
    static float [][] kmtestDataSet;     // Data member for our iris dataset
    static Random random;

    static private class WinningCombination
    {
        public double totalSpread = 0f;
        LinkedList<Point2D.Float> centroids = new LinkedList<>();
        HashMap<Integer,LinkedList<Point2D.Float>> clusters = new HashMap<>();
    }

    public static void main(String[] args)
    {
        irisDataSet = new float[NUM_IRIS_DATA_COLUMNS][NUM_IRIS_DATA_ROWS];
        kmtestDataSet = new float[NUM_KMTEST_DATA_COLUMNS][NUM_KMTEST_DATA_ROWS];
        random = new Random();

        // Load the iris.csv file
        loadIrisFile();

        // Load the kmtest.csv file
        loadKmtestFile();

        // Analyze kmtest data
        analyzeKmtestData(0f, 18f, 0f, 12f);

        // Normalize kmtest data
        normalizeKmtestData();

        // Analyze kmtest data after normalization
        analyzeKmtestData(-2f, 2f, -2f, 2f);
    }

    private static boolean loadIrisFile()
    {
        boolean out = true;

        try
        {
            int i = 0;
            String line;
            BufferedReader br = new BufferedReader(new FileReader("iris_msdos.csv"));

            while ((line = br.readLine()) != null)
            {
                String[] values = line.split(IRIS_DELIMETER);
                for(int j = 0; j <= NUM_IRIS_DATA_COLUMNS-1; j++)
                    irisDataSet[j][i] = Float.parseFloat(values[j]);

                i++;
            }

            br.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            out = false;
        }

        // Verify the data capture was valid
       /* for(int j = 0; j <= NUM_IRIS_DATA_ROWS-1; j++) {
            for (int i = 0; i <= NUM_IRIS_DATA_COLUMNS - 1; i++)
                System.out.print("[" + irisDataSet[i][j] + "]");
            System.out.println();
        }*/

        return out;
    }

    private static boolean loadKmtestFile()
    {
        boolean out = true;

        try
        {
            int i = 0;
            String line;
            BufferedReader br = new BufferedReader(new FileReader("kmtest_msdos.csv"));

            while ((line = br.readLine()) != null)
            {
                line = line.trim();
                int firstSpace = line.indexOf(" ");

                kmtestDataSet[0][i] = Float.parseFloat(line.substring(0,firstSpace));
                kmtestDataSet[1][i] = Float.parseFloat(line.substring(firstSpace).trim());

                i++;
            }

            br.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            out = false;
        }

        // Verify the data capture was valid
        /*for(int j = 0; j <= NUM_KMTEST_DATA_ROWS-1; j++) {
            for (int i = 0; i <= NUM_KMTEST_DATA_COLUMNS - 1; i++)
                System.out.print("[" + kmtestDataSet[i][j] + "]");
            System.out.println();
        }*/

        return out;
    }

    static Point2D.Float createRandomPoint(float xMin, float xMax, float yMin, float yMax)
    {
        float x = random.nextFloat(xMin, xMax);
        float y = random.nextFloat(yMin, yMax);
        return new Point2D.Float(x,y);
    }

    static void analyzeKmtestData(float xMin, float xMax, float yMin, float yMax)
    {
        //Initialize values of K
        int[] kValues = { 2, 3, 4, 5 };

        // Create a holder for the winning combinations
        HashMap<Integer, WinningCombination> winners = new HashMap<>();

        // Analyze without normalization for our values of K
        for(int i = 0; i < kValues.length; i++)
        {
            int kValue = kValues[i];
            System.out.println("\n********************************************************");
            System.out.println("Start Iterations for K=" + kValue);
            System.out.println("********************************************************");

            // Try 100 random starting points for each iteration of K
            for(int loop = 0; loop < 100; loop++)
            {
                //Initialize our centroid list with seeds and our holding list before we iterate
                LinkedList<Point2D.Float> centroidList = new LinkedList<>();
                HashMap<Integer, LinkedList<Point2D.Float>> holdingList = new HashMap<>();
                for (int j = 0; j < kValue; j++) {
                    centroidList.add(createRandomPoint(xMin, xMax, yMin, yMax));
                    holdingList.put(j, new LinkedList<>());
                }

                int safetyExit = 0;
                boolean stableCentroids = false;
                while (!stableCentroids) {
                    System.out.println("\nIteration[" + loop + "-" + safetyExit + "]");

                    // Print out the current centroid for each value of K
                    for (int j = 0; j < kValue; j++)
                        System.out.println(centroidList.get(j));

                    // Iterate through the kmtest data and determine which points are closest to which centroids
                    double closestDistance = 999;
                    Iterator<Point2D.Float> iter = centroidList.iterator();
                    for (int km_y = 0; km_y <= NUM_KMTEST_DATA_ROWS - 1; km_y++) {

                        int currentCentroidLabel = 0;
                        int closestCentroidLabel = 0;

                        Point2D.Float kmPoint = new Point2D.Float(kmtestDataSet[0][km_y], kmtestDataSet[1][km_y]);
                        while (iter.hasNext()) {
                            Point2D.Float centroidPoint = iter.next();
                            double distance = kmPoint.distance(centroidPoint);
                            if (distance < closestDistance) {
                                closestCentroidLabel = currentCentroidLabel;
                                closestDistance = distance;
                            }

                            currentCentroidLabel++;
                        }

                        holdingList.get(closestCentroidLabel).add(kmPoint);
                        iter = centroidList.iterator();
                        closestDistance = 999;
                    }

                    // Evaluate our holding list and see what went where
                    for (int j = 0; j < kValue; j++) {
                        System.out.println("Cluster list centroid [" + centroidList.get(j) + "] size=" + holdingList.get(j).size());
                    }

                    // Calculate new centroids based on the mean of the points in each grouping
                    int matchingCentroids = 0;
                    for (int j = 0; j < kValue; j++) {
                        LinkedList<Point2D.Float> kList = holdingList.get(j);
                        if (!kList.isEmpty()) {
                            int size = kList.size();
                            float xTotal = 0f;
                            float yTotal = 0f;

                            Iterator<Point2D.Float> pIter = kList.iterator();
                            while (pIter.hasNext()) {
                                Point2D.Float aPoint = pIter.next();
                                xTotal += aPoint.x;
                                yTotal += aPoint.y;
                            }

                            Point2D.Float newCentroid = new Point2D.Float(xTotal / size, yTotal / size);
                            Point2D.Float oldCentroid = centroidList.remove(j);
                            centroidList.add(j, newCentroid);

                            if (oldCentroid.equals(newCentroid))
                                matchingCentroids++;
                        } else
                            matchingCentroids++;
                    }

                    // Check exit conditions.  If the centroids are stable or we trigger our safety exit, leave the loop
                    safetyExit++;
                    if ((matchingCentroids == kValue) || (safetyExit > 10)) {
                        stableCentroids = true;
                        System.out.println("\nExit on iteration [" + safetyExit + "] matchingCentriods = " + matchingCentroids);

                        // Measure the density of the configuration to see if we have a local max or an overall max
                        double totalSpread = 0f;
                        for (int j = 0; j < kValue; j++) {

                            Point2D.Float centroid = centroidList.get(j);
                            Iterator<Point2D.Float> pIter = holdingList.get(j).iterator();
                            while (pIter.hasNext()) {
                                Point2D.Float current = pIter.next();
                                totalSpread += centroid.distance(current);
                            }
                        }
                        System.out.println("Total Spread for this stable outcome = " + totalSpread);

                        // Determine if this is the best combination we have seen yet
                        if ((!winners.containsKey(kValue)) || (totalSpread < winners.get(kValue).totalSpread)) {
                            WinningCombination newWinner = new WinningCombination();
                            newWinner.totalSpread = totalSpread;

                            for (int j = 0; j < kValue; j++) {
                                Point2D.Float centroid = centroidList.get(j);
                                newWinner.centroids.add(new Point2D.Float(centroid.x, centroid.y));

                                LinkedList<Point2D.Float> newList = new LinkedList<>();
                                Iterator<Point2D.Float> pIter = holdingList.get(j).iterator();
                                while (pIter.hasNext()) {
                                    Point2D.Float current = pIter.next();
                                    newList.add(new Point2D.Float(current.x, current.y));
                                }
                                newWinner.clusters.put(j, newList);
                            }
                            winners.put(kValue, newWinner);
                        }
                    } else {
                        // Clear the holding list if we are about to iterate again
                        for (int j = 0; j < kValue; j++) {
                            holdingList.get(j).clear();
                        }
                    }
                }
            }
        }

        System.out.println("\n********************************************************");
        System.out.println("WINNERS");
        System.out.println("********************************************************");

        // Dump the winners
        for(int i = 0; i < kValues.length; i++)
        {
            int kValue = kValues[i];
            WinningCombination winner = winners.get(kValue);
            System.out.println("\nWinner for K=" + kValue + " density=" + winner.totalSpread);

            for(int j = 0; j < kValue; j++)
            {
                System.out.println("Centroid = " + winner.centroids.get(j));
                Iterator<Point2D.Float> pIter = winner.clusters.get(j).iterator();
                while(pIter.hasNext())
                {
                    System.out.println("   Point = " + pIter.next());
                }
            }
        }
    }

    static Float calculateMean(LinkedList<Float> numbers)
    {
        Float out = 0f;
        int size = numbers.size();

        Iterator<Float> iter = numbers.iterator();
        while(iter.hasNext())
            out += iter.next();

        return (out/size);
    }

    static Double calculateStdDev(LinkedList<Float> numbers)
    {
        Double out = 0.0;
        int size = numbers.size();
        Float mean = calculateMean(numbers);

        Iterator<Float> iter = numbers.iterator();
        while(iter.hasNext())
            out += Math.pow(iter.next() - mean, 2);

        out = Math.pow(out/size, 0.5);

        return out;
    }

    static double calculateZScoreNormal(Float original, Float mean, Double stdDev)
    {
        return (original - mean)/stdDev;
    }

    static void normalizeKmtestData()
    {
        // Print the list before normalize
        /*for(int j = 0; j <= NUM_KMTEST_DATA_ROWS-1; j++) {
            for (int i = 0; i <= NUM_KMTEST_DATA_COLUMNS - 1; i++)
                System.out.print("[" + kmtestDataSet[i][j] + "]");
            System.out.println();
        }*/

        LinkedList<Float> xCoordinates = new LinkedList<>();
        LinkedList<Float> yCoordinates = new LinkedList<>();

        for(int j = 0; j <= NUM_KMTEST_DATA_ROWS-1; j++)
        {
            xCoordinates.add(kmtestDataSet[0][j]);
            yCoordinates.add(kmtestDataSet[1][j]);
        }

        float xMean = calculateMean(xCoordinates);
        float yMean = calculateMean(yCoordinates);
        double xStdDev = calculateStdDev(xCoordinates);
        double yStdDev = calculateStdDev(yCoordinates);

   /*     System.out.println("xMean = " + xMean + " xStdDev = " + xStdDev);
        System.out.println("yMean = " + yMean + " yStdDev = " + yStdDev);
*/
        // Transform the kmDataSet in place with z-score normalization
        for(int j = 0; j <= NUM_KMTEST_DATA_ROWS-1; j++)
        {
            // x coordinates
            Float originalX = kmtestDataSet[0][j];
            kmtestDataSet[0][j] = (float) calculateZScoreNormal(originalX, xMean, xStdDev);

            // y coordinates
            Float originalY = kmtestDataSet[1][j];
            kmtestDataSet[1][j] = (float) calculateZScoreNormal(originalY, yMean, yStdDev);
        }

        // Verify the normalize was valid
        /*for(int j = 0; j <= NUM_KMTEST_DATA_ROWS-1; j++) {
            for (int i = 0; i <= NUM_KMTEST_DATA_COLUMNS - 1; i++)
                System.out.print("[" + kmtestDataSet[i][j] + "]");
            System.out.println();
        }*/
    }
}