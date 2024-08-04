//Name - G.S.J Fernando
//UoW No - w1998725
//IIT No - 20221110

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class SlidingPuzzleSolver {

    // Scanner object for reading input.
    Scanner fileScanner;

    // Variables for grid dimensions.
    int numColumns = 0, numRows = 0;

    // Grid representing the puzzle.
    char[][] puzzleGrid = null;

    // Starting coordinates in the grid.
    int[] startCoordinates = new int[2];

    // Ending coordinates in the grid.
    int[] endCoordinates = new int[2];

    // Player's current coordinates in the grid.
    int[] playerCoordinates = new int[2];


    public void readPuzzle(String filename) {
        try {
            // Construct the file path from a base directory and filename.
            File file = new File("benchmark_series/" + filename + ".txt");
            fileScanner = new Scanner(file);

            // First pass: determine the number of rows and columns in the puzzle.
            while (fileScanner.hasNextLine()) {
                numRows++; // Count the number of lines (rows).
                numColumns = fileScanner.nextLine().length(); // Length of a line sets the number of columns.
            }

            // Close scanner after first pass.
            fileScanner.close();

            // Initialize the puzzle grid with determined dimensions.
            puzzleGrid = new char[numRows][numColumns];

            // Second pass: fill the puzzle grid with characters from the file.
            fileScanner = new Scanner(file);
            int rowIndex = 0; // Index to track the current row.

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                for (int i = 0; i < numColumns; i++) {
                    puzzleGrid[rowIndex][i] = line.charAt(i); // Fill grid cell by cell.
                }
                rowIndex++;
            }

            findShortestPath(); // Function call to find the shortest path in the grid.

        } catch (FileNotFoundException e) {
            System.out.println("File not found."); // Error handling if file does not exist.
        }
    }

    public void findPosition() {
        // Initialize start and end coordinates with an invalid position (-1, -1).
        startCoordinates[0] = -1;
        startCoordinates[1] = -1;
        endCoordinates[0] = -1;
        endCoordinates[1] = -1;

        // Loop through each cell in the grid to find start and end positions.
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                // Check if the current cell contains the start character 'S'.
                if (puzzleGrid[i][j] == 'S') {
                    // Set the start coordinates and player's initial position to the location of 'S'.
                    startCoordinates[0] = j;  // Column index where 'S' is found.
                    startCoordinates[1] = i;  // Row index where 'S' is found.
                    playerCoordinates[0] = j; // Set player's starting column to 'S' position.
                    playerCoordinates[1] = i; // Set player's starting row to 'S' position.
                }
                // Check if the current cell contains the finish character 'F'.
                else if (puzzleGrid[i][j] == 'F') {
                    // Set the end coordinates to the location of 'F'.
                    endCoordinates[0] = j; // Column index where 'F' is found.
                    endCoordinates[1] = i; // Row index where 'F' is found.
                }
            }
        }

        // Check if the start position was not found and print an error message.
        if (startCoordinates[0] == -1 || startCoordinates[1] == -1) {
            System.out.println("Invalid start position."); // Alert if no valid start position exists.
        }
    }

    public void findShortestPath() {
        // Check for valid start and end positions by finding them in the grid.
        findPosition();

        // Return and print an error if either start or end positions are invalid.
        if (startCoordinates[0] == -1 || startCoordinates[1] == -1 || endCoordinates[0] == -1 || endCoordinates[1] == -1) {
            System.out.println("Invalid start or end position.");
            return;
        }

        // Queue to manage the BFS process.
        ArrayList<int[]> queue = new ArrayList<>();
        // Visited array to keep track of which cells have been checked.
        boolean[][] visitedCells = new boolean[numRows][numColumns];
        // Array to store the parent cell coordinates for each cell visited (for path reconstruction).
        int[][] parentCellCoordinates = new int[numRows][numColumns];

        // Initialize the BFS with the start position.
        queue.add(new int[]{startCoordinates[0], startCoordinates[1]});
        visitedCells[startCoordinates[1]][startCoordinates[0]] = true;

        // BFS loop.
        while (!queue.isEmpty()) {
            int[] currentPosition = queue.remove(0);  // Get and remove the element at the front of the queue.
            int column = currentPosition[0];  // Current cell column.
            int row = currentPosition[1];    // Current cell row.

            // Check if the current position is the end position.
            if (column == endCoordinates[0] && row == endCoordinates[1]) {
                // If reached the end, reconstruct and print the path.
                printShortestPath(parentCellCoordinates, startCoordinates, endCoordinates);
                return;
            }

            // Explore all four possible directions from the current cell: right, left, up, and down.
            exploreDirection(queue, visitedCells, parentCellCoordinates, column, row, 0, 1);  // Right
            exploreDirection(queue, visitedCells, parentCellCoordinates, column, row, 0, -1); // Left
            exploreDirection(queue, visitedCells, parentCellCoordinates, column, row, -1, 0); // Up
            exploreDirection(queue, visitedCells, parentCellCoordinates, column, row, 1, 0);  // Down
        }

        // If the queue is empty and no path was found, print that no path is available.
        System.out.println("No path found");
    }


    private void exploreDirection(ArrayList<int[]> queue, boolean[][] visitedCells, int[][] parentCellCoordinates, int column, int row, int horizontalStep, int verticalStep) {
        // Initialize coordinates for the new position.
        int newX = column;
        int newY = row;

        // Attempt to move in the specified direction until an invalid position is encountered.
        while (isValidPosition(newX + horizontalStep, newY + verticalStep)) {
            newX += horizontalStep;
            newY += verticalStep;

            // If the finish point 'F' is found at the new position, enqueue it and update parent coordinates.
            if (puzzleGrid[newY][newX] == 'F') {
                queue.add(new int[]{newX, newY});
                parentCellCoordinates[newY][newX] = encodePosition(column, row);
                return;
            }
        }

        // If the new position is valid and has not been visited yet, mark it as visited, enqueue it, and record its parent cell.
        if (!visitedCells[newY][newX]) {
            queue.add(new int[]{newX, newY});
            visitedCells[newY][newX] = true;
            parentCellCoordinates[newY][newX] = encodePosition(column, row);
        }
    }

    private void printShortestPath(int[][] parentCellCoordinates, int[] start, int[] end) {
        // Start with the end position.
        int x = end[0];
        int y = end[1];
        // List to store the path from end to start.
        ArrayList<int[]> path = new ArrayList<>();
        // Add the end position to the path list.
        path.add(new int[]{x, y});

        // Trace back from end to start using parent positions.
        while (x != start[0] || y != start[1]) {
            // Retrieve the encoded parent position of the current cell.
            int parentPosition = parentCellCoordinates[y][x];
            // Decode the parent's x and y coordinates.
            x = decodePosition(parentPosition) / 100;
            y = decodePosition(parentPosition) % 100;

            // Check for loop or excessively long paths, break if something seems off.
            if (path.size() <= numColumns * numRows) {
                path.add(new int[]{x, y});
            } else {
                break;
            }
        }

        // Begin printing the path.
        System.out.println("1. Start at (" + (start[0] + 1) + "," + (start[1] + 1) + ")");

        // Variables to keep track of the previous coordinates for direction calculation.
        int prevX = start[0];
        int prevY = start[1];

        // Iterate backwards through the path to print steps in the correct order.
        for (int i = path.size() - 2; i >= 0; i--) {
            int[] pos = path.get(i);
            int currX = pos[0];
            int currY = pos[1];

            // Determine the direction of movement from the previous cell to the current cell.
            String direction;
            if (currX > prevX) {
                direction = "Move right";
            } else if (currX < prevX) {
                direction = "Move left";
            } else if (currY > prevY) {
                direction = "Move down";
            } else {
                direction = "Move up";
            }

            // Print each step with its direction and position.
            int lineNumber = path.size() - i;
            System.out.println(lineNumber + ". " + direction + " to (" + (currX + 1) + "," + (currY + 1) + ")");

            // Update previous coordinates.
            prevX = currX;
            prevY = currY;
        }

        // Final statement upon completion.
        System.out.println("Done!");
    }

    private int encodePosition(int x, int y) {
        // Multiplying x by 100 and adding y to ensure unique values for each position.
        // This assumes the grid will not have 100 or more rows or columns.
        return x * 100 + y;
    }

    private int decodePosition(int pos) {
        // The method is intended to provide the encoded position directly.
        return pos;
    }

    private boolean isValidPosition(int x, int y) {
        // Check if (x, y) is within the grid bounds and the cell is not blocked ('0').
        return x >= 0 && x < numColumns && y >= 0 && y < numRows && puzzleGrid[y][x] != '0';
    }


}