import java.io.*;
import java.util.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class IMECEPathFinder {
	public int[][] grid; // 2D array to store the grid data
	private int[][] scaledGrid; // 2D array to store the scaled grid data
	public int height; // Number of rows in the grid
	public int width; // Number of columns in the grid
	public int maxFlyingHeight; // Maximum flying height
	public double fuelCostPerUnit; // Fuel cost per unit distance
	public double climbingCostPerUnit; // Climbing cost per unit height difference

	public IMECEPathFinder(String filename, int rows, int cols, int maxFlyingHeight, double fuelCostPerUnit,
			double climbingCostPerUnit) {
		// Initialize the grid and scaledGrid arrays with the specified number of rows
		// and columns
		grid = new int[rows][cols];
		scaledGrid = new int[rows][cols];

		// Set the height and width of the grid
		this.height = rows;
		this.width = cols;

		// Set the maximum flying height, fuel cost per unit, and climbing cost per unit
		this.maxFlyingHeight = maxFlyingHeight;
		this.fuelCostPerUnit = fuelCostPerUnit;
		this.climbingCostPerUnit = climbingCostPerUnit;

		try {
			// Create a Scanner object to read from the file
			Scanner scanner = new Scanner(new File(filename));

			// Read the grid values from the file and store them in the grid array
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					grid[i][j] = scanner.nextInt();
				}
			}

			// Close the scanner
			scanner.close();

			// Save a copy of the original grid by calling the saveGrid method
			saveGrid();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void saveGrid() {

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				if (grid[i][j] < min) {
					min = grid[i][j];
				}
				if (grid[i][j] > max) {
					max = grid[i][j];
				}
			}
		}
		// find the min and max values in the grid

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				scaledGrid[i][j] = (int) ((grid[i][j] - min) * 255.0 / (max - min));
			}
		}
		// scale the grid values to 0-255 using the min and max values
		try {
			PrintWriter writer = new PrintWriter("grayscaleMap.dat");
			for (int i = 0; i < scaledGrid.length; i++) {
				for (int j = 0; j < scaledGrid[0].length; j++) {
					if (j != scaledGrid[0].length - 1)
						writer.print(scaledGrid[i][j] + " ");
					else
						writer.print(scaledGrid[i][j]);
				}
				if (i < scaledGrid.length - 1)
					writer.println();
			}
			writer.close();
			// write the scaled grid to a file called "grayscaleMap.dat"
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Draws the grid using the given Graphics object.
	 * Colors should be grayscale values 0-255, scaled based on min/max elevation
	 * values in the grid
	 */
	public void drawGrayscaleMap(Graphics g) {
		// Find the minimum and maximum elevation values in the grid
		int minElevation = Integer.MAX_VALUE;
		int maxElevation = Integer.MIN_VALUE;
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				int elevation = grid[i][j];
				if (elevation < minElevation) {
					minElevation = elevation;
				}
				if (elevation > maxElevation) {
					maxElevation = elevation;
				}
			}
		}

		// Draw the grid using grayscale colors based on the elevation values
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				int elevation = grid[i][j];
				int grayscale = mapToGrayscale(elevation, minElevation, maxElevation);
				g.setColor(new Color(grayscale, grayscale, grayscale));
				g.fillRect(j, i, 1, 1);
			}
		}
	}

	// Helper method to map an elevation value to a grayscale color value
	private int mapToGrayscale(int value, int minValue, int maxValue) {
		// Calculate the range of values
		int range = maxValue - minValue;

		// Scale the value by subtracting the minimum value
		int scaledValue = value - minValue;

		// Map the scaled value to the grayscale range (0-255)
		int grayscale = (int) (scaledValue / (double) range * 255);

		// Return the grayscale value
		return grayscale;
	}

	private double calculateCost(Point p1, Point p2) {
		// Calculate the horizontal and vertical distance between the two points
		int dx = p2.x - p1.x;
		int dy = p2.y - p1.y;
		double dist = Math.sqrt(dx * dx + dy * dy);

		// Get the current height and neighbor height from the grid
		int currentHeight = grid[p1.y][p1.x];
		int neighborHeight = grid[p2.y][p2.x];

		// Calculate the height impact based on the difference between the current and
		// neighbor heights
		double heightImpact = (currentHeight >= neighborHeight) ? 0 : (neighborHeight - currentHeight);

		// Calculate the cost as the distance multiplied by the fuel cost per unit plus
		// the climbing cost per unit multiplied by the height impact
		return (dist * fuelCostPerUnit) + (climbingCostPerUnit * heightImpact);
	}

	/**
	 * Get the most cost-efficient path from the source Point start to the
	 * destination Point end
	 * using Dijkstra's algorithm on pixels.
	 * 
	 * @return the List of Points on the most cost-efficient path from start to end
	 */

	public List<Point> getMostEfficientPath(Point start, Point end) {
		// grid dimensions
		int height = grid.length;
		int width = grid[0].length;

		// create cost grid and parent grid
		double[][] costGrid = new double[height][width];
		for (double[] row : costGrid)
			Arrays.fill(row, Double.MAX_VALUE);

		Point[][] parentGrid = new Point[height][width];

		// movement directions
		int[][] dirs = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }, { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };

		// start point cost
		costGrid[start.y][start.x] = 0;

		// using a priority queue for Dijkstra's algorithm
		PriorityQueue<Point> queue = new PriorityQueue<>(
				(a, b) -> Double.compare(costGrid[a.y][a.x], costGrid[b.y][b.x]));

		queue.add(start);

		while (!queue.isEmpty()) {
			Point current = queue.poll();

			// if we reached the end point
			if (current.equals(end))
				break;

			for (int[] dir : dirs) {
				int newX = current.x + dir[0];
				int newY = current.y + dir[1];

				// check grid boundaries and maxFlyingHeight
				if (newX < 0 || newY < 0 || newX >= width || newY >= height || grid[newY][newX] > maxFlyingHeight)
					continue;

				// calculate cost
				double newCost = calculateCost(current, new Point(newX, newY));

				if (costGrid[current.y][current.x] + newCost < costGrid[newY][newX]) {
					costGrid[newY][newX] = costGrid[current.y][current.x] + newCost;
					parentGrid[newY][newX] = current;

					// only add the point to the queue if the cost is updated
					queue.add(new Point(newX, newY));
				}
			}
		}

		// construct path from end to start
		List<Point> path = new ArrayList<>();
		for (Point p = end; p != null; p = parentGrid[p.y][p.x]) {
			path.add(0, p); // add at the start to reverse the path
		}

		return path;
	}

	/**
	 * Calculate the most cost-efficient path from source to destination.
	 * 
	 * @return the total cost of this most cost-efficient path when traveling from
	 *         source to destination
	 */

	public double getMostEfficientPathCost(List<Point> path) {
		double totalCost = 0.0;

		for (int i = 0; i < path.size() - 1; i++) {
			Point current = path.get(i);
			Point next = path.get(i + 1);

			// calculate cost between current and next points
			double cost = calculateCost(current, next);

			totalCost += cost;
		}

		return totalCost;
	}

	/**
	 * Draw the most cost-efficient path on top of the grayscale map from source to
	 * destination.
	 */
	public void drawMostEfficientPath(Graphics g, List<Point> path) {
		g.setColor(Color.GREEN); // Set the color of the path to green

		for (Point point : path) {
			g.fillOval(point.x, point.y, 1, 1); // Draw a small circle (dot) at each point
		}
	}

	/**
	 * Find an escape path from source towards East such that it has the lowest
	 * elevation change.
	 * Choose a forward step out of 3 possible forward locations, using greedy
	 * method described in the assignment instructions.
	 * 
	 * @return the list of Points on the path
	 */
	public List<Point> getLowestElevationEscapePath(Point start) {
		List<Point> pathPointsList = new ArrayList<>();
		Point current = start;

		// Add start point to the path
		pathPointsList.add(start);

		// Traverse towards East
		while (current.x < grid[0].length - 1) {
			int currentElevation = grid[current.y][current.x];

			// Calculate the absolute difference in elevation for the three possible next
			// points: NE, E, SE
			int northElevationChange = current.y > 0 ? Math.abs(currentElevation - grid[current.y - 1][current.x + 1])
					: Integer.MAX_VALUE;
			int eastElevationChange = Math.abs(currentElevation - grid[current.y][current.x + 1]);
			int southElevationChange = current.y < grid.length - 1
					? Math.abs(currentElevation - grid[current.y + 1][current.x + 1])
					: Integer.MAX_VALUE;

			// Choose the path with the least change in elevation
			if (eastElevationChange <= northElevationChange && eastElevationChange <= southElevationChange) {
				current = new Point(current.x + 1, current.y);
			} else if (northElevationChange <= southElevationChange) {
				current = new Point(current.x + 1, current.y - 1);
			} else {
				current = new Point(current.x + 1, current.y + 1);
			}

			// Add the chosen point to the path
			pathPointsList.add(current);
		}

		return pathPointsList;
	}

	/**
	 * Calculate the escape path from source towards East such that it has the
	 * lowest elevation change.
	 * 
	 * @return the total change in elevation for the entire path
	 */
	public int getLowestElevationEscapePathCost(List<Point> pathPointsList) {
		int totalChange = 0;

		// Iterate over each pair of adjacent points in the pathPointsList
		for (int i = 0; i < pathPointsList.size() - 1; i++) {
			Point currentPoint = pathPointsList.get(i);
			Point nextPoint = pathPointsList.get(i + 1);

			// Get the elevation of the current and next points from the grid
			int currentElevation = grid[currentPoint.y][currentPoint.x];
			int nextElevation = grid[nextPoint.y][nextPoint.x];

			// Calculate the absolute difference between the current and next elevations and
			// add it to the total change
			totalChange += Math.abs(currentElevation - nextElevation);
		}

		// Return the total elevation change along the path
		return totalChange;
	}

	/**
	 * Draw the escape path from source towards East on top of the grayscale map
	 * such that it has the lowest elevation change.
	 */
	public void drawLowestElevationEscapePath(Graphics g, List<Point> pathPointsList) {
		// Set the color of the path to yellow
		g.setColor(Color.YELLOW);

		// Iterate over each point in the pathPointsList
		for (Point point : pathPointsList) {
			// Draw a small circle (dot) at each point
			g.fillOval(point.x, point.y, 1, 1);
		}
	}

}
