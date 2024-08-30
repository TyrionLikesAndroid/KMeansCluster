import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;

public class KMeansClusterTestHarness {

    static final String IRIS_DELIMETER = ",";    // Comma delimiter for the iris file
    static final int NUM_IRIS_DATA_COLUMNS = 4; // Total number of iris columns
    static final int NUM_IRIS_DATA_ROWS = 150; // Total number of iris rows
    static final int NUM_KMTEST_DATA_COLUMNS = 2; // Total number of kmtest columns
    static final int NUM_KMTEST_DATA_ROWS = 21; // Total number of kmtest rows

    static float [][] irisDataSet;     // Data member for our iris dataset
    static float [][] kmtestDataSet;     // Data member for our iris dataset

    public static void main(String[] args)
    {
        irisDataSet = new float[NUM_IRIS_DATA_COLUMNS][NUM_IRIS_DATA_ROWS];
        kmtestDataSet = new float[NUM_KMTEST_DATA_COLUMNS][NUM_KMTEST_DATA_ROWS];

        // Load the iris.csv file
        loadIrisFile();

        // Load the kmtest.csv file
        loadKmtestFile();
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
        for(int j = 0; j <= NUM_IRIS_DATA_ROWS-1; j++) {
            for (int i = 0; i <= NUM_IRIS_DATA_COLUMNS - 1; i++)
                System.out.print("[" + irisDataSet[i][j] + "]");
            System.out.println();
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

        // Verify the data capture was valid
        for(int j = 0; j <= NUM_KMTEST_DATA_ROWS-1; j++) {
            for (int i = 0; i <= NUM_KMTEST_DATA_COLUMNS - 1; i++)
                System.out.print("[" + kmtestDataSet[i][j] + "]");
            System.out.println();
        }

        return out;
    }
}
