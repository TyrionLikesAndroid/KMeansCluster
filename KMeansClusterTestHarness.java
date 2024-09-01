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

    static private class ClusterAnalysisResult
    {
        public double totalSpread = 0f;
        LinkedList<FloatDataRow> centroids = new LinkedList<>();
        HashMap<Integer,LinkedList<FloatDataRow>> clusters = new HashMap<>();
    }

    static private class FloatDataRow
    {
        public int size;
        public float [] row;

        public FloatDataRow(int size)
        {
            this.size = size;
            this.row = new float[size];
        }

        public FloatDataRow(float... args)
        {
            this.size = args.length;
            this.row = new float[size];

            for(int i=0; i<args.length; i++)
                row[i] = args[i];
        }

        public double distance(FloatDataRow aRow)
        {
            double out = 0f;

            if(size != aRow.size)
            {
                System.out.println("ERROR - data row size mismatch");
            }
            else
            {
                for(int i = 0; i < size; i++)
                    out += Math.pow(row[i] - aRow.row[i], 2);

                out = Math.pow(out, 0.5);
            }

            return out;
        }

        public String toString()
        {
            String out = new String();

            for(int i = 0; i < size; i++)
                out = out.concat(row[i] + ", ");

            return out.substring(0, out.length() - 2);
        }

        public boolean equals(Object obj)
        {
            if (obj == null) {
                return false;
            }

            if (obj.getClass() != this.getClass()) {
                return false;
            }

            final FloatDataRow other = (FloatDataRow) obj;
            for(int i = 0; i < size; i++)
            {
                if(row[i] != other.row[i])
                    return false;
            }

            return true;
        }
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
        int[] kmtestKValues = { 2, 3, 4, 5 };
        analyzeDataSet(kmtestKValues, kmtestDataSet, new FloatDataRow(0f, 0f), new FloatDataRow(18f, 12f));

        // Normalize kmtest data
        normalizeDataSet(kmtestDataSet);

        // Analyze kmtest data after normalization (note the new min/max values)
        analyzeDataSet(kmtestKValues, kmtestDataSet, new FloatDataRow(-2f, -2f), new FloatDataRow(2f, 2f));

        // Analyze the iris data
        int[] irisKValues = { 3 };
        analyzeDataSet(irisKValues, irisDataSet, new FloatDataRow(4f, 1f, 0f, 0f), new FloatDataRow(8f, 5f, 7f, 3f));

        // Normalize the iris data just to see if it clusters better
        normalizeDataSet(irisDataSet);
        analyzeDataSet(irisKValues, irisDataSet, new FloatDataRow(-2f, -2f, -2f, -2f), new FloatDataRow(2f, 2f, 2f, 2f));
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

        return out;
    }

    static FloatDataRow createRandomRow(FloatDataRow min, FloatDataRow max)
    {
        FloatDataRow out = new FloatDataRow(min.size);
        for(int i=0; i < min.size; i++)
            out.row[i]=random.nextFloat(min.row[i], max.row[i]);

        return out;
    }

    static void analyzeDataSet(int[] kValues, float [][] testSet, FloatDataRow min, FloatDataRow max)
    {
        // Create a holder for the winning and losing combinations
        HashMap<Integer, ClusterAnalysisResult> winners = new HashMap<>();
        HashMap<Integer, ClusterAnalysisResult> losers = new HashMap<>();

        // Analyze the dataset for our values of K
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
                LinkedList<FloatDataRow> centroidList = new LinkedList<>();
                HashMap<Integer, LinkedList<FloatDataRow>> holdingList = new HashMap<>();
                for (int j = 0; j < kValue; j++) {
                    centroidList.add(createRandomRow(min, max));
                    holdingList.put(j, new LinkedList<>());
                }

                int safetyExit = 0;
                boolean stableCentroids = false;

                // Run the iteration until we have stable centroids.  Don't worry, there is a safety exit below
                while (! stableCentroids)
                {
                    System.out.println("\nIteration[" + loop + "-" + safetyExit + "]");

                    // Print out the current centroid for each value of K
                    for (int j = 0; j < kValue; j++)
                        System.out.println(centroidList.get(j));

                    // Iterate through the kmtest data and determine which points are closest to which centroids
                    double closestDistance = 999;
                    int numRows = testSet[0].length;

                    Iterator<FloatDataRow> iter = centroidList.iterator();
                    for (int km_y = 0; km_y <= numRows - 1; km_y++) {

                        int currentCentroidLabel = 0;
                        int closestCentroidLabel = 0;

                        int numColumns = testSet.length;
                        FloatDataRow kmPoint = new FloatDataRow(numColumns);
                        for(int col = 0; col < numColumns; col++)
                            kmPoint.row[col] = testSet[col][km_y];

                        while (iter.hasNext()) {
                            FloatDataRow centroidPoint = iter.next();
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
                    for (int j = 0; j < kValue; j++)
                    {
                        LinkedList<FloatDataRow> kList = holdingList.get(j);
                        if (!kList.isEmpty()) {
                            int size = kList.size();
                            FloatDataRow newCentroid = new FloatDataRow(kList.getFirst().size);

                            Iterator<FloatDataRow> pIter = kList.iterator();
                            while (pIter.hasNext())
                            {
                                FloatDataRow aPoint = pIter.next();
                                for(int k = 0; k < aPoint.size; k++)
                                    newCentroid.row[k] += aPoint.row[k];
                            }

                            for(int k = 0; k < kList.getFirst().size; k++)
                                newCentroid.row[k] = newCentroid.row[k]/size;

                            FloatDataRow oldCentroid = centroidList.remove(j);
                            centroidList.add(j, newCentroid);

                            if (oldCentroid.equals(newCentroid))
                                matchingCentroids++;
                        } else
                            matchingCentroids++;
                    }

                    // Check exit conditions.  If the centroids are stable or we trigger our safety exit, leave the loop
                    safetyExit++;
                    if ((matchingCentroids == kValue) || (safetyExit > 100)) {
                        stableCentroids = true;
                        System.out.println("\nExit on iteration [" + safetyExit + "] matchingCentriods = " + matchingCentroids);

                        // Measure the density of the configuration to see if we have a local max or an overall max
                        double totalSpread = 0f;
                        for (int j = 0; j < kValue; j++) {

                            FloatDataRow centroid = centroidList.get(j);
                            Iterator<FloatDataRow> pIter = holdingList.get(j).iterator();
                            while (pIter.hasNext()) {
                                FloatDataRow current = pIter.next();
                                totalSpread += centroid.distance(current);
                            }
                        }
                        System.out.println("Total Spread for this stable outcome = " + totalSpread);

                        // See if our result is valid for this value of K.  Some starting points are so random that
                        // they end up catching zero members of the dataset.  We want to avoid those because they
                        // are really only representative of a lower value of K in reality.
                        boolean validKResult = true;
                        for (int j = 0; j < kValue; j++)
                        {
                            LinkedList<FloatDataRow> list = holdingList.get(j);
                            if(list.isEmpty()) {
                                validKResult = false;
                                break;
                            }
                        }

                        // Determine if this is the best or worst combination we have seen yet
                        if ((!winners.containsKey(kValue)) || (totalSpread < winners.get(kValue).totalSpread))
                        {
                            if(validKResult)
                                winners.put(kValue, createAnalysisResult(kValue, totalSpread, centroidList, holdingList));
                        }
                        if ((!losers.containsKey(kValue)) || (totalSpread > losers.get(kValue).totalSpread)) {
                            if(validKResult)
                                losers.put(kValue, createAnalysisResult(kValue, totalSpread, centroidList, holdingList));
                        }
                    }
                    else
                    {
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
        printAnalysisResult(kValues, winners);

        System.out.println("\n********************************************************");
        System.out.println("LOSERS");
        System.out.println("********************************************************");

        //Dump the losers
        printAnalysisResult(kValues, losers);
    }

    static ClusterAnalysisResult createAnalysisResult(int kValue, double spread, LinkedList<FloatDataRow> centroidList,
                                                      HashMap<Integer, LinkedList<FloatDataRow>> holdingList)
    {
        ClusterAnalysisResult newResult = new ClusterAnalysisResult();
        newResult.totalSpread = spread;

        for (int j = 0; j < kValue; j++) {
            FloatDataRow centroid = centroidList.get(j);
            newResult.centroids.add(new FloatDataRow(centroid.row));

            LinkedList<FloatDataRow> newList = new LinkedList<>();
            Iterator<FloatDataRow> pIter = holdingList.get(j).iterator();
            while (pIter.hasNext()) {
                FloatDataRow current = pIter.next();
                newList.add(new FloatDataRow(current.row));
            }
            newResult.clusters.put(j, newList);
        }
        return newResult;
    }

    static void printAnalysisResult(int[] kValues, HashMap<Integer, ClusterAnalysisResult> results)
    {
        for(int i = 0; i < kValues.length; i++)
        {
            int kValue = kValues[i];
            ClusterAnalysisResult winner = results.get(kValue);
            System.out.println("\nResult for K=" + kValue + " density=" + winner.totalSpread);

            for(int j = 0; j < kValue; j++)
            {
                System.out.println("Centroid = " + winner.centroids.get(j) + " size = " + winner.clusters.get(j).size());
                Iterator<FloatDataRow> pIter = winner.clusters.get(j).iterator();
                while(pIter.hasNext())
                {
                    System.out.println("   Point = " + pIter.next());
                }
            }
        }
    }

    static float calculateMean(LinkedList<Float> numbers)
    {
        float out = 0f;
        int size = numbers.size();

        Iterator<Float> iter = numbers.iterator();
        while(iter.hasNext())
            out += iter.next();

        return (out/size);
    }

    static double calculateStdDev(LinkedList<Float> numbers)
    {
        double out = 0.0;
        int size = numbers.size();
        float mean = calculateMean(numbers);

        Iterator<Float> iter = numbers.iterator();
        while(iter.hasNext())
            out += Math.pow(iter.next() - mean, 2);

        out = Math.pow(out/size, 0.5);

        return out;
    }

    static double calculateZScoreNormal(float original, float mean, double stdDev)
    {
        return (original - mean)/stdDev;
    }

    static void normalizeDataSet(float [][] testSet)
    {
        int numRows = testSet[0].length;
        int numColumns = testSet.length;

        HashMap<Integer, LinkedList<Float>> coordinates = new HashMap<>();
        HashMap<Integer, Float> means = new HashMap<>();
        HashMap<Integer, Double> stdDevs = new HashMap<>();

        for(int i=0; i < numColumns; i++)
            coordinates.put(i, new LinkedList<>());

        for(int j = 0; j < numColumns; j++)
            for(int k = 0; k < numRows; k++)
                coordinates.get(j).add(testSet[j][k]);

        for(int i=0; i < numColumns; i++)
        {
            means.put(i, calculateMean(coordinates.get(i)));
            stdDevs.put(i, calculateStdDev(coordinates.get(i)));
        }

        // Transform the testSet in place with z-score normalization
        for(int j = 0; j < numColumns; j++)
            for(int k = 0; k < numRows; k++)
            {
                float original = testSet[j][k];
                testSet[j][k] = (float) calculateZScoreNormal(original, means.get(j), stdDevs.get(j));
            }
    }
}