import java.io.*;
import java.util.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class IMECEPathFinder{
	  public int[][] grid;
	  public int height, width;
	  public int maxFlyingHeight;
	  public double fuelCostPerUnit, climbingCostPerUnit;

	public IMECEPathFinder(String filename, int rows, int cols, int maxFlyingHeight, double fuelCostPerUnit, double climbingCostPerUnit) {
		grid = new int[rows][cols];
		this.height = rows;
		this.width = cols;
		this.maxFlyingHeight = maxFlyingHeight;
		this.fuelCostPerUnit = fuelCostPerUnit;
		this.climbingCostPerUnit = climbingCostPerUnit;

		try {
			Scanner scanner = new Scanner(new File(filename));
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					grid[i][j] = scanner.nextInt();
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	  /**
	   * Draws the grid using the given Graphics object.
	   * Colors should be grayscale values 0-255, scaled based on min/max elevation values in the grid
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
		int range = maxValue - minValue;
		int scaledValue = value - minValue;
		int grayscale = (int) (scaledValue / (double) range * 255);
		return grayscale;
	}

	private double calculateCost(Point s, Point e, int maxFlyingHeight) {
		double distance = Math.sqrt(Math.pow(s.x - e.x, 2) + Math.pow(s.y - e.y, 2));

		double heightImpact;
		if(grid[s.y][s.x] >= grid[e.y][e.x]) {
			heightImpact = 0;
		} else {
			heightImpact = grid[e.y][e.x] - grid[s.y][s.x];
		}

		if (heightImpact > maxFlyingHeight) {
			return Double.POSITIVE_INFINITY;
		}

		return distance * fuelCostPerUnit + climbingCostPerUnit * heightImpact;
	}

	/**
	 * Get the most cost-efficient path from the source Point start to the destination Point end
	 * using Dijkstra's algorithm on pixels.
	 * @return the List of Points on the most cost-efficient path from start to end
	 */
	public double calculateCost(Point current, Point next) {
		double heightImpact = Math.max(0, grid[next.y][next.x] - grid[current.y][current.x]);
		double dist = Math.sqrt(Math.pow(next.y - current.y, 2) + Math.pow(next.x - current.x, 2));
		return dist * fuelCostPerUnit + heightImpact * climbingCostPerUnit;
	}
	public List<Point> getNeighbors(Point point) {
		int[] directions = {-1, 0, 1};
		List<Point> neighbors = new ArrayList<>();

		for (int dy : directions) {
			for (int dx : directions) {
				if (dy == 0 && dx == 0) {
					continue; // Skip the point itself
				}

				int newY = point.y + dy;
				int newX = point.x + dx;

				// Check if the neighbor is within the grid boundaries
				if (newY >= 0 && newY < grid.length && newX >= 0 && newX < grid[0].length) {
					neighbors.add(new Point(newX, newY));
				}
			}
		}

		return neighbors;
	}

	public List<Point> getMostEfficientPath(Point start, Point end) {
		double[][] distances = new double[grid.length][grid[0].length];
		for (double[] row : distances) {
			Arrays.fill(row, Double.MAX_VALUE);
		}
		distances[start.y][start.x] = 0;

		Point[][] previousPoints = new Point[grid.length][grid[0].length];

		// Map to store points in the queue and their distances
		Map<Point, Double> queueMap = new HashMap<>();

		PriorityQueue<Point> queue = new PriorityQueue<>(Comparator.comparingDouble(p -> distances[p.y][p.x]));
		queue.offer(start);
		queueMap.put(start, 0.0);

		while (!queue.isEmpty()) {
			Point current = queue.poll();

			if (current.equals(end)) {
				List<Point> path = new ArrayList<>();
				while (current != null) {
					path.add(current);
					current = previousPoints[current.y][current.x];
				}
				Collections.reverse(path);
				return path;
			}

			for (Point neighbor : getNeighbors(current)) {
				double cost = distances[current.y][current.x] + calculateCost(current, neighbor);
				if (cost < distances[neighbor.y][neighbor.x]) {
					distances[neighbor.y][neighbor.x] = cost;
					previousPoints[neighbor.y][neighbor.x] = current;

					// Only offer the new point to the queue if it's not in the map, or if it's in the map but with a larger distance
					if (!queueMap.containsKey(neighbor) || queueMap.get(neighbor) > cost) {
						queue.offer(neighbor);
						queueMap.put(neighbor, cost);
					}
				}
			}
		}

		return new ArrayList<>();
	}



	/**
	 * Calculate the most cost-efficient path from source to destination.
	 * @return the total cost of this most cost-efficient path when traveling from source to destination
	 */

	public double getMostEfficientPathCost(List<Point> path){
		double totalCost = 0.0;

		// TODO: Your code goes here, use the output from the getMostEfficientPath() method

		return totalCost;
	}


	/**
	 * Draw the most cost-efficient path on top of the grayscale map from source to destination.
	 */
	public void drawMostEfficientPath(Graphics g, List<Point> path){
		// TODO: Your code goes here, use the output from the getMostEfficientPath() method
	}

	/**
	 * Find an escape path from source towards East such that it has the lowest elevation change.
	 * Choose a forward step out of 3 possible forward locations, using greedy method described in the assignment instructions.
	 * @return the list of Points on the path
	 */
	public List<Point> getLowestElevationEscapePath(Point start) {
		List<Point> pathPointsList = new ArrayList<>();
		Point current = start;

		// Make sure start point is valid
		if(start.y < 0 || start.y >= grid.length || start.x < 0 || start.x >= grid[0].length) {
			throw new IllegalArgumentException("Invalid start point");
		}

		// Add start point to the path
		pathPointsList.add(start);

		// Traverse towards East
		while(current.x < grid[0].length - 1) {
			int currentElevation = grid[current.y][current.x];

			// Calculate the absolute difference in elevation for the three possible next points: NE, E, SE
			int northElevationChange = current.y > 0 ? Math.abs(currentElevation - grid[current.y - 1][current.x + 1]) : Integer.MAX_VALUE;
			int eastElevationChange = Math.abs(currentElevation - grid[current.y][current.x + 1]);
			int southElevationChange = current.y < grid.length - 1 ? Math.abs(currentElevation - grid[current.y + 1][current.x + 1]) : Integer.MAX_VALUE;

			// Choose the path with the least change in elevation
			if(eastElevationChange <= northElevationChange && eastElevationChange <= southElevationChange) {
				current = new Point(current.x + 1, current.y);
			} else if(northElevationChange <= southElevationChange) {
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
	 * Calculate the escape path from source towards East such that it has the lowest elevation change.
	 * @return the total change in elevation for the entire path
	 */
	public int getLowestElevationEscapePathCost(List<Point> pathPointsList) {
		int totalChange = 0;

		for (int i = 0; i < pathPointsList.size() - 1; i++) {
			Point currentPoint = pathPointsList.get(i);
			Point nextPoint = pathPointsList.get(i + 1);

			// Ensure points are valid
			if (currentPoint.x < 0 || currentPoint.x >= grid.length || currentPoint.y < 0 || currentPoint.y >= grid[0].length
					|| nextPoint.x < 0 || nextPoint.x >= grid.length || nextPoint.y < 0 || nextPoint.y >= grid[0].length) {
				throw new IllegalArgumentException("Invalid point in path");
			}

			int currentElevation = grid[currentPoint.x][currentPoint.y];
			int nextElevation = grid[nextPoint.x][nextPoint.y];

			totalChange += Math.abs(currentElevation - nextElevation);
		}

		return totalChange;
	}



	/**
	 * Draw the escape path from source towards East on top of the grayscale map such that it has the lowest elevation change.
	 */
	public void drawLowestElevationEscapePath(Graphics g, List<Point> pathPointsList){
		g.setColor(Color.YELLOW);  // Set the color of the path to yellow

		for (Point point : pathPointsList){
			g.fillOval(point.y, point.x, 2, 2);  // Draw a small circle (dot) at each point
		}
	}



}
