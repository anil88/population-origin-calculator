package ac.at.tuwien;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Stack;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

public class PopulationCalculator {

    private static DefaultCategoryDataset relationDataset = new DefaultCategoryDataset();
    private final String COUNTRY_OF_ORIGIN_EU = "COUNTRY_OF_ORIGIN_EU";
    private final String COUNTRY_OF_ORIGIN_FORMER_YUGOSLAVIA = "COUNTRY_OF_ORIGIN_FORMER_YUGOSLAVIA";
    private final String COUNTRY_OF_ORIGIN_OTHERS = "COUNTRY_OF_ORIGIN_OTHERS";
    private final String COUNTRY_OF_ORIGIN_TURKEY = "COUNTRY_OF_ORIGIN_TURKEY";
    private final String COUNTRY_OF_ORIGIN_AUSTRIA = "COUNTRY_OF_ORIGIN_AUSTRIA";
    private final String COUNTRY_OF_ORIGIN_TOTAL = "COUNTRY_OF_ORIGIN_TOTAL";
    private DBCollection collection;
    private MongoClient mongoClient;
    private DB db;

    public PopulationCalculator(String mongohost, int mongoPort, String mongoDB, String mongoCollection) throws UnknownHostException {
        mongoClient = new MongoClient(mongohost, mongoPort);
        db = mongoClient.getDB(mongoDB);
        collection = db.getCollection(mongoCollection);
    }

    public static void main(String[] args) throws UnknownHostException {
        if (args.length == 0) {
            PopulationCalculator populationCalculator = new PopulationCalculator("localhost", 27017, "population", "origin");
            populationCalculator.createTotalOriginData();
            populationCalculator.createOriginOfTenBiggestAndSmallestCities();
            populationCalculator.lineChart();
            populationCalculator.createOriginChartOfAllData();
        } else if (args[0].equals("-modifyCSV")) {
            try {
                modifyCSVFile(args[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (args[0].equals("-calculateCharts")) {
            String host = args[1];
            int port = Integer.parseInt(args[2]);
            String db = args[3];
            String collection = args[4];
            PopulationCalculator populationCalculator = new PopulationCalculator(host, port, db, collection);
            populationCalculator.createTotalOriginData();
            populationCalculator.createOriginOfTenBiggestAndSmallestCities();
            populationCalculator.lineChart();
            populationCalculator.createOriginChartOfAllData();
        } else {
            System.out.println("##########################################");
            System.out.println("");
            System.out.println("Wrong Argument: " + args[0]);
        }
    }

    private static void modifyCSVFile(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.csv"));

        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            writer.write(line.replaceAll(";", ",") + "\n");
        }

        reader.close();
        writer.close();
    }

    public static void lineChart() {
        JFreeChart lineChart = ChartFactory.createLineChart(
                "Comparison Smallest and Biggest Cities",
                "People", "Percent of non Austrian [%]",
                relationDataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        File pieChart = new File("ComparisonSmallestBiggest.jpeg");
        int width = 1400;   /* Width of the image */
        int height = 800;
        try {
            ChartUtilities.saveChartAsJPEG(pieChart, lineChart, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createTotalOriginData() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        long turkey = 0;
        long eu = 0;
        long others = 0;
        long yugo = 0;
        long austria = 0;
        long total = 0;
        BasicDBObject fields = new BasicDBObject();
        fields.put("YEAR", 2015);

        DBCursor cursor = collection.find(fields);
        while (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();
            eu += obj.getInt(COUNTRY_OF_ORIGIN_EU);
            yugo += obj.getInt(COUNTRY_OF_ORIGIN_FORMER_YUGOSLAVIA);
            turkey += obj.getInt(COUNTRY_OF_ORIGIN_TURKEY);
            others += obj.getInt(COUNTRY_OF_ORIGIN_OTHERS);
            austria += obj.getInt(COUNTRY_OF_ORIGIN_AUSTRIA);
            total += obj.getInt(COUNTRY_OF_ORIGIN_TOTAL);
        }

        dataset.setValue("EU: " + eu, eu);
        dataset.setValue("Former Yugoslavien: " + yugo, yugo);
        dataset.setValue("Turkey: " + turkey, turkey);
        dataset.setValue("Others: " + others, others);
        dataset.setValue("Austria: " + austria, austria);

        printChart("PopulationOriginTotal", dataset);
    }

    private double calculateNotAustrianRelation(long austrian, long notAustrian) {
        long sum = austrian + notAustrian;
        double onPercent = sum / 100;
        return notAustrian / onPercent;
    }

    public void createOriginChartOfAllData() {
        DefaultCategoryDataset relationAllDataset = new DefaultCategoryDataset();
        BasicDBObject fields = new BasicDBObject();
        fields.put("YEAR", 2015);
        DBCursor cursor = collection.find(fields).sort(new BasicDBObject(COUNTRY_OF_ORIGIN_TOTAL, 1));
        while (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();
            DefaultPieDataset dataset = new DefaultPieDataset();
            int eu = obj.getInt(COUNTRY_OF_ORIGIN_EU);
            int turkey = obj.getInt(COUNTRY_OF_ORIGIN_TURKEY);
            int others = obj.getInt(COUNTRY_OF_ORIGIN_OTHERS);
            int yugo = obj.getInt(COUNTRY_OF_ORIGIN_FORMER_YUGOSLAVIA);
            int austria = obj.getInt(COUNTRY_OF_ORIGIN_AUSTRIA);
            int total = obj.getInt(COUNTRY_OF_ORIGIN_TOTAL);
            dataset.setValue("EU: " + eu, eu);
            dataset.setValue("Former Yugoslavien: " + yugo, yugo);
            dataset.setValue("Turkey: " + turkey, turkey);
            dataset.setValue("Others: " + others, others);
            dataset.setValue("Austria: " + austria, austria);
            double relationResult = calculateNotAustrianRelation(austria , (eu+ yugo + turkey + others));
            relationAllDataset.addValue(relationResult, "All", " " + total);

        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Percent of non Austrian",
                "People", "Percent of non Austrian [%]",
                relationAllDataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        File pieChart = new File("PercentOfNonAustrian.jpeg");
        int width = 1400;   /* Width of the image */
        int height = 800;
        try {
            ChartUtilities.saveChartAsJPEG(pieChart, lineChart, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void createOriginOfTenBiggestAndSmallestCities() {
        BasicDBObject fields = new BasicDBObject();
        fields.put("YEAR", 2015);
        DBCursor cursorSmallest = collection.find(fields).sort(new BasicDBObject(COUNTRY_OF_ORIGIN_TOTAL, 1)).limit(10);
        DBCursor cursorBiggest = collection.find(fields).sort(new BasicDBObject(COUNTRY_OF_ORIGIN_TOTAL, -1)).limit(10);

        while (cursorSmallest.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursorSmallest.next();
            createTotalOriginForCity(obj, "SMALLEST_TEN");
        }

        Stack<BasicDBObject> revertBiggest = new Stack<BasicDBObject>();
        while (cursorBiggest.hasNext()) {
            revertBiggest.push((BasicDBObject) cursorBiggest.next());
        }

        while (!revertBiggest.isEmpty()) {
            BasicDBObject obj = revertBiggest.pop();
            createTotalOriginForCity(obj, "BIGGEST_TEN");
        }
    }

    private void createTotalOriginForCity(BasicDBObject basicDBObject, String smallOrBig) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        int eu = basicDBObject.getInt(COUNTRY_OF_ORIGIN_EU);
        int turkey = basicDBObject.getInt(COUNTRY_OF_ORIGIN_TURKEY);
        int others = basicDBObject.getInt(COUNTRY_OF_ORIGIN_OTHERS);
        int yugo = basicDBObject.getInt(COUNTRY_OF_ORIGIN_FORMER_YUGOSLAVIA);
        int austria = basicDBObject.getInt(COUNTRY_OF_ORIGIN_AUSTRIA);
        int total = basicDBObject.getInt(COUNTRY_OF_ORIGIN_TOTAL);
        dataset.setValue("EU: " + eu, eu);
        dataset.setValue("Former Yugoslavien: " + yugo, yugo);
        dataset.setValue("Turkey: " + turkey, turkey);
        dataset.setValue("Others: " + others, others);
        dataset.setValue("Austria: " + austria, austria);
        double relationResult = calculateNotAustrianRelation(austria , (eu+ yugo + turkey + others));
        relationDataset.addValue(relationResult, smallOrBig, " " + total);

    }

    private void printChart(String title, PieDataset dataset) {
        JFreeChart chart = ChartFactory.createPieChart(
                title,   // chart title
                dataset,          // data
                true,             // include legend
                true,
                false);

        File pieChart = new File(title + ".jpeg");
        int width = 700;   /* Width of the image */
        int height = 600;
        try {
            ChartUtilities.saveChartAsJPEG(pieChart, chart, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}