import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        // Step 0 - read the input parameters
        DatReader datReader = new DatReader(args[0]);

        String grid_input_file_name = datReader.getStringVar("grid_input_file_name");
        int num_rows = datReader.getIntVar("num_rows");
        int num_cols = datReader.getIntVar("num_cols");
        Point mission_0_source = datReader.getPointVar("mission_0_source");
        Point mission_0_destination = datReader.getPointVar("mission_0_destination");
        Point mission_1_source = datReader.getPointVar("mission_1_source");
        int max_flying_height = datReader.getIntVar("max_flying_height");
        Double fuel_cost_per_unit = datReader.getDoubleVar("fuel_cost_per_unit");
        Double climbing_cost_per_unit = datReader.getDoubleVar("climbing_cost_per_unit");

        // Step 1 - construct map data
        IMECEPathFinder map = new IMECEPathFinder(grid_input_file_name,
                num_rows, num_cols, max_flying_height, fuel_cost_per_unit, climbing_cost_per_unit);

        // Step 2 - construct DrawingPanel, and get its Graphics context
        // COMMENT OUT THESE LINES BEFORE TURBO TESTING AND SUBMISSION

        // Create a new DrawingPanel object with the specified number of rows and
        // columns
        // DrawingPanel panel = new DrawingPanel(num_rows, num_cols);

        // Get the Graphics object from the panel
        // Graphics g = panel.getGraphics();

        // Call the drawGrayscaleMap method of the map object and pass the Graphics
        // object as a parameter
        // map.drawGrayscaleMap(g);

        // Step 3 - get the most cost-efficient path between source and destination
        // Points
        System.out.println("########## Mission 0 ##########");

        // Calculate the shortest path using the getMostEfficientPath method of the map
        // object
        List<Point> shortestPath = map.getMostEfficientPath(mission_0_source, mission_0_destination);

        // Check if the shortest path is empty
        if (shortestPath.size() == 0) {
            System.out.println("ERROR PathNotFound: There is no most cost-efficient path that meets all criteria!");
        } else {
            System.out.println("The most cost-efficient path's size: " + shortestPath.size());

            // Calculate the total cost of the shortest path using the
            // getMostEfficientPathCost method of the map object
            double totalCost = map.getMostEfficientPathCost(shortestPath);
            System.out.println("The most cost-efficient path has a cost of: " + totalCost);

            // map.drawMostEfficientPath(g, shortestPath); // COMMENT OUT THIS LINE BEFORE
            // TURBO TESTING AND SUBMISSION
        }

        // Step 4 - get the lowest elevation Escape Path towards the West from a source
        // Point
        System.out.println("########## Mission 1 ##########");

        // Calculate the escape path with the least elevation cost using the
        // getLowestElevationEscapePath method of the map object
        List<Point> leastElevationPath = map.getLowestElevationEscapePath(mission_1_source);

        // Print the size of the escape path with the least elevation cost
        System.out.println("The size of the escape path with the least elevation cost: " + leastElevationPath.size());

        // Calculate the total elevation change of the escape path using the
        // getLowestElevationEscapePathCost method of the map object
        int totalChange = map.getLowestElevationEscapePathCost(leastElevationPath);

        // Print the least elevation cost of the escape path
        System.out.println("The escape path has the least elevation cost of: " + totalChange);

        // map.drawLowestElevationEscapePath(g, leastElevationPath); // COMMENT OUT THIS
        // LINE BEFORE TURBO TESTING AND SUBMISSION

    }
}
