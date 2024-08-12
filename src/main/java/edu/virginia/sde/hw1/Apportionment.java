package edu.virginia.sde.hw1;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;

import java.io.*;
import java.util.*;

import static java.lang.Math.sqrt;

public class Apportionment {

    public static int repNum = 0; // Sets default number of representatives available to delegate

    // Created two array lists with hash map entries. This is so we can effectively sort the hash map later.
    private List<Map.Entry<String, Integer>> stateRepresentatives = new ArrayList<>();
    private List<Map.Entry<String, Double>> statesWithRemainders = new ArrayList<>();
    private static Workbook workbook;
    public static void main(String[] args) { // Read in arguments
        String fileName = args[0];
        String calculationMethod = "--huntington"; // Default method is Huntington-Hill
        if(args.length >= 2) {
            if(args[1].equals("--hamilton") || args[1].equals("--huntington")) { // https://stackoverflow.com/questions/12558206/how-can-i-check-if-a-value-is-of-type-integer
                calculationMethod = args[1].toLowerCase();
            }
            else {
                repNum = Integer.parseInt(args[1]);
            }
        }
        if (args.length >= 3) {
            calculationMethod = args[2].toLowerCase();
        }
        Apportionment apportionment = new Apportionment();

        try {
            Map<String, Integer> statePopulations;
            if (fileName.endsWith(".xlsx")) {
                statePopulations = readWorkbook(fileName); // For XLSX files
            } else if (fileName.endsWith(".csv")) {
                statePopulations = readCsvFile(fileName); // For CSV files
            } else {
                throw new IllegalArgumentException("Unsupported file type. Please use .xlsx or .csv files.");
            }
            if (calculationMethod.equals("--huntington")) {
                apportionment.hhCalc(statePopulations);
            } else if (calculationMethod.equals("--hamilton")) {
                apportionment.appCalc(statePopulations);
            } else {
                throw new IllegalArgumentException("Invalid calculation method specified.");
            }
            apportionment.printResults();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //found on SDE reading tutorial with Apache poi
    public static void openXLSX(String filename) throws FileNotFoundException, IOException {
        try {
            FileInputStream inputStream = new FileInputStream(new File(filename));
            workbook = new XSSFWorkbook(inputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error: File " + filename + " not found");
        }
    }
    public static Map<String, Integer> readWorkbook(String fileName) throws IOException {
        Map<String, Integer> statePopulations = new HashMap<>();
        openXLSX(fileName);
        Sheet sheet = workbook.getSheetAt(0);
        int stateCol = -1;
        int popCol = -1;
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                for (Cell cell : row) {
                    String colName = cell.getStringCellValue().trim().toLowerCase();
                    if (colName.equals("state")) {
                        stateCol = cell.getColumnIndex();
                    } else if (colName.equals("population")) {
                        popCol = cell.getColumnIndex();
                    }
                }
                if (stateCol == -1 || popCol == -1) { //null check
                    continue;
                }
            }
            Cell state = row.getCell(stateCol);
            if (state == null) {
                continue;
            }
            Cell population = row.getCell(popCol);
            String stateName = state.getStringCellValue().trim();
            int statePopulation = 0;
            if (population.getCellType() == CellType.NUMERIC) {
                statePopulation = (int) population.getNumericCellValue();
            } else if (population.getCellType() == CellType.STRING) {
                // Handle cases where population is stored as a string
                try {
                    statePopulation = Integer.parseInt(population.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    // Handle invalid numeric values
                    System.err.println("Invalid numeric value in population cell for state: " + stateName);
                }
            }
            statePopulations.put(stateName, statePopulation);
        }

        workbook.close();
        return statePopulations;
    }
    public static Map<String, Integer> readCsvFile(String fileName) throws IOException { // Method to read file
        Map<String, Integer> statePopulations = new HashMap<>(); // Create empty hashmap to fill with read data
        // Read file using buffer
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            int stateCol = -1;
            int popCol = -1;
            String headerLine = br.readLine(); // Skip header line
            String[] headers = headerLine.split(",");
            for (int i = 0; i < headers.length; i++) {
                String colName = headers[i].trim().toLowerCase();
                if (colName.equals("state")) {
                    stateCol = i;
                } else if (colName.equals("population")) {
                    popCol = i;
                }
            }
            if (stateCol == -1 || popCol == -1) {
                throw new IllegalArgumentException("CSV file must contain 'State' and 'Population' columns.");
            }
            String line;
            while ((line = br.readLine()) != null) {
                String[] statePops = line.split(",");
                if (statePops.length >= 2 && !(statePops[stateCol].isEmpty())) {
                    String stateName = statePops[stateCol].trim();
                    int statePopulation = 0;
                    try {
                        statePopulation = Integer.parseInt(statePops[popCol].trim());
                        statePopulations.put(stateName, statePopulation);
                    } catch (NumberFormatException e) {
                        System.out.println("Line skipped: Invalid population value for state: " + stateName);
                    }
                } else {
                    System.out.println("Line skipped: Missing data for state");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return statePopulations;
    }

    private void appCalc(Map<String, Integer> file) { // Method for calculating apportionment

        int repAssigned = 0;

        if (repNum == 0) { // If no repNum was read, assign default repNum
            repNum = 435;
        }
        int repPop = totalPopulation(file) / repNum;  // Find total pop to total rep ratio
        for (Map.Entry<String, Integer> entry : file.entrySet()) { // For all entries
            String state = entry.getKey(); // Get cur state
            int population = entry.getValue(); // Get cur pop
            int repAssign = population / repPop; // Find how many reps to assign to state
            double remainder = population % repPop; // Calculate remainder in dividing
            repAssigned += repAssign; // Find total reps assigned
            stateRepresentatives.add(new AbstractMap.SimpleEntry<>(state, repAssign)); // Add state and reps assigned to state rep array list
            statesWithRemainders.add(new AbstractMap.SimpleEntry<>(state, remainder)); // Add state and remained assigned to state remainder array list
        }
        int diff = repNum - repAssigned; // Find number of reps that still need to be assigned
        if (diff > 0) { // If representatives still need to be assigned
            excessRep(diff); // Assign reps
        }
        sortStateRepresentatives(); // Sort alphabetically
    }

    private void hhCalc(Map<String, Integer> file) {
        if (repNum == 0) { // If no repNum was read, assign default repNum
            repNum = 435;
        }
        if(repNum >= file.size()) {

            int totalRepresentatives = repNum - file.size(); // Subtracting initial allocation of 1 for each state
            // Assign one representative to each state initially
            for (String state : file.keySet()) {
                stateRepresentatives.add(new AbstractMap.SimpleEntry<>(state, 1));
            }

            // Iteratively allocate remaining representatives
            while (totalRepresentatives > 0) {
                String highestPriorityState = "";
                double highestPriority = 0;

                for (Map.Entry<String, Integer> entry : stateRepresentatives) { // For all entries in staterep array list
                    String state = entry.getKey(); // Get the current state
                    int currentReps = entry.getValue(); // Get the reps allocated to that current state
                    double priority = calculatePriority(file.get(state), currentReps); // Calculate priority of given state

                    if (priority > highestPriority) { // If the priority of the given state is higher than the highest priority read
                        highestPriority = priority; // Set the new highest priority found
                        highestPriorityState = state; // And assign it as the highest priority state for now
                    }
                }

                // Allocate one representative to the highest priority state
                int index = findStateIndex(highestPriorityState);
                stateRepresentatives.set(index, new AbstractMap.SimpleEntry<>(highestPriorityState, stateRepresentatives.get(index).getValue() + 1));
                totalRepresentatives--;
            }

            sortStateRepresentatives();
        }
        else {
            throw new IllegalArgumentException("Not enough representatives to use selected method.");

        }
    }

    // Helper method to calculate priority
    private double calculatePriority(int population, int currentReps) {
        return population / sqrt(currentReps * (currentReps + 1));
    }

    private void excessRep(int diff) { // Assign excess reps
        statesWithRemainders.sort(Map.Entry.<String, Double>comparingByValue().reversed()); // Sort remainder array list by values
        for (int i = 0; i < diff; i++) { // For the number of reps left
            Map.Entry<String, Double> stateWithRemainder = statesWithRemainders.get(i); // Get the state that needs a rep
            String state = stateWithRemainder.getKey(); // Get key of that state
            int index = findStateIndex(state); // Find the index of that state in our main array list with hash map entries
            if (index != -1) { // If said state exists
                Map.Entry<String, Integer> entry = stateRepresentatives.get(index); // Get state name for said state
                stateRepresentatives.set(index, new AbstractMap.SimpleEntry<>(state, entry.getValue() + 1)); // Set the state entry in main array list to +1 rep
            }
        }
    }

    private int findStateIndex(String state) { // Helper method that finds state index in main array list
        for (int i = 0; i < stateRepresentatives.size(); i++) {
            if (stateRepresentatives.get(i).getKey().equals(state)) {
                return i;
            }
        }
        return -1;
    }

    private int totalPopulation(Map<String, Integer> file) { // Find total population given an array list of hash entries with state and population
        int totalPopulation = 0;
        for (int population : file.values()) {
            totalPopulation += population;
        }
        return totalPopulation;
    }
    private void sortStateRepresentatives() { // Sort print values alphabetically
        stateRepresentatives.sort(Map.Entry.<String, Integer>comparingByKey());
    }


    private void printResults() { // Print final
        for (Map.Entry<String, Integer> entry : stateRepresentatives) {
            System.out.println("State: " + entry.getKey() + ", Representatives: " + entry.getValue());
        }
    }
}


