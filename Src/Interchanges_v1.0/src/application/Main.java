package application;

import exception.CmdException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private Map map;
    private boolean isSourceSet = false;
    private boolean isJunctionsDetected = false;
    private Boolean isRightHandTraffic;
    private String inputName;

    private int runCmd() {
        System.out.println("  TIDA v1.0 January-2019");
        System.out.println("  Tool for Interchanges Detection and Analysis");
        System.out.println("  by Justinas Vaidokas");
        System.out.println();
        System.out.println("  Type: 'help' - to get commands list; 'exit' - to exit program.");
        Scanner scanner = new Scanner(System.in);
        System.out.print("> ");
        for(;;) {
            if (readInput(scanner) == 1) {
                return 0;
            }
        }
    }

    private int readInput(Scanner scanner) {
        String[] input = scanner.nextLine().split(" ");
        System.out.print("> ");
        List<String[]> statements = new ArrayList<>();
        if(input.length > 0 && !input[0].equals("") && !input[0].isEmpty()){
            switch (input[0]) {
                case "help":    printHelp();
                                System.out.print("> ");
                                return 0;
                case "exit":    return 1;
                default:        if(input.length % 2 != 0) {
                                    System.err.println("One argument is missing!");
                                    System.out.print("> ");
                                    return 0;
                                } else {
                                    for (int i=1; i<input.length; i+=2) {
                                        String[] statement = new String[2];
                                        statement[0] = input[i-1];
                                        statement[1] = input[i];
                                        statements.add(statement);
                                    }
                                }
                                long start = System.currentTimeMillis();
                                try {
                                    executeInput(statements);
                                } catch (NumberFormatException e) {
                                    System.err.println("Wrong argument! " + e.getMessage());
                                }
                                long elapsedTime = System.currentTimeMillis() - start;
                                System.out.println(String.format("Execution time: %.3f s", elapsedTime/1000F));
                                System.out.print("> ");
                                return 0;
            }
        } else {
            return 0;
        }
    }

    private void executeInput(List<String[]> statements) throws NumberFormatException  {
        List<String> allParameters = new ArrayList<>();
        for(String[] statement : statements) {
            if(allParameters.contains(statement[0])) {
                System.err.println("Duplicate parameters entered!");
                System.out.print("> ");
                return;
            } else {
                allParameters.add(statement[0]);
            }
        }
        if(allParameters.contains("-x") || allParameters.contains("-s") || allParameters.contains("-l") || allParameters.contains("-i") || allParameters.contains("-s")) {
            if(allParameters.contains("-O") && allParameters.contains("-P")) {
                if(isJunctionsDetected || (allParameters.contains("-R") && allParameters.contains("-L"))) {
                    if(!isSourceSet && (!allParameters.contains("-I") || !allParameters.contains("-T"))) {
                        System.err.println("Parameter '-I' or '-T' is missing or source is not set!");
                        System.out.print("> ");
                        return;
                    }
                } else {
                    System.err.println("Parameter '-R' or '-L' is missing or junctions is not set!");
                    System.out.print("> ");
                    return;
                }
            } else {
                System.err.println("Parameter '-O' or '-P' is missing!");
                System.out.print("> ");
                return;
            }
        } else {
            if(allParameters.contains("-O") || allParameters.contains("-P")) {
                if(!allParameters.contains("-O") || !allParameters.contains("-P")) {
                    System.err.println("Parameter '-O' or '-P' is missing!");
                    System.out.print("> ");
                    return;
                } else {
                    if (!isSourceSet && (!allParameters.contains("-I") || !allParameters.contains("-T"))) {
                        System.err.println("Parameter '-I' or '-T' is missing or source is not set!");
                        System.out.print("> ");
                        return;
                    }
                }
            } else {
                if(allParameters.contains("-R") || allParameters.contains("-L")) {
                    if(!allParameters.contains("-R") || !allParameters.contains("-L")) {
                        System.err.println("Parameter '-R' or '-L' is missing!");
                        System.out.print("> ");
                        return;
                    } else {
                        if (!isSourceSet && (!allParameters.contains("-I") || !allParameters.contains("-T"))) {
                            System.err.println("Parameter '-I' or '-T' is missing or source is not set!");
                            System.out.print("> ");
                            return;
                        }
                    }
                } else {
                    if(!allParameters.contains("-I") || !allParameters.contains("-T")) {
                        System.err.println("Parameter '-I' or '-T' is missing!");
                        System.out.print("> ");
                        return;
                    }
                }
            }
        }
        int sourceChangeCheck = 0;
        Integer numberOfRoads = null;
        Double length = null;
        String outputName = null;
        Boolean printConnections = null;
        Boolean printCrossingPoints = null;
        Boolean printJunctions = null;
        Boolean printRoads = null;
        String crossingPoints = null;
        String sides = null;
        String levels = null;
        String intersections = null;
        String conflictRoads = null;
        for(String[] statement : statements) {
            switch (statement[0]) {
                case "-I":      inputName = statement[1];
                                sourceChangeCheck++;
                                break;
                case "-T":      switch (statement[1]) {
                                    case "r":   isRightHandTraffic = true;
                                                sourceChangeCheck++;
                                                break;
                                    case "l":   isRightHandTraffic = false;
                                                sourceChangeCheck++;
                                                break;
                                    default:    System.err.println(String.format("Parameter '%s' argument '%s' does not exist.", statement[0], statement[1]));
                                                System.out.print("> ");
                                                return;
                                }
                                break;
                case "-R":      numberOfRoads = Integer.parseInt(statement[1]);
                                if(numberOfRoads<0) {
                                    throw new NumberFormatException(String.format("Negative number! For input string: \"%s\"",statement[1]));
                                }
                                break;
                case "-L":      length = Double.parseDouble(statement[1]);
                                if(length<0) {
                                    throw new NumberFormatException(String.format("Negative number! For input string: \"%s\"",statement[1]));
                                }
                                break;
                case "-O":      outputName = statement[1];
                                break;
                case "-P":      if(statement[1].indexOf('c') >= 0) {
                                    printConnections = true;
                                }
                                if(statement[1].indexOf('j') >= 0) {
                                    printJunctions = true;
                                }
                                if(statement[1].indexOf('r') >= 0) {
                                    printRoads = true;
                                }
                                if(statement[1].indexOf('x') >= 0) {
                                    printCrossingPoints = true;
                                }
                                break;
                case "-x":      crossingPoints = statement[1];
                                break;
                case "-s":      sides = statement[1];
                                break;
                case "-l":      levels = statement[1];
                                break;
                case "-i":      intersections = statement[1];
                                break;
                case "-c":      conflictRoads = statement[1];
                                break;
                default:        System.err.println(String.format("Command '%s' does not exist.", statement[0]));
                                System.out.print("> ");
                                return;
            }
        }
        if(sourceChangeCheck == 2) {
            try {
                map = new Map(inputName, isRightHandTraffic);
                isSourceSet = true;
                isJunctionsDetected = false;
            } catch (IOException e) {
                isSourceSet = false;
                isJunctionsDetected = false;
                System.err.println(e.getMessage());
                System.out.print("> ");
                return;
            } catch (CmdException e) {
                isSourceSet = false;
                isJunctionsDetected = false;
                System.err.println(e.getMessage());
                System.out.print("> ");
                return;
            }
        }
        if(numberOfRoads != null && length != null) {
            if(isJunctionsDetected && isSourceSet) {
                try {
                    map = new Map(inputName, isRightHandTraffic);
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    System.out.print("> ");
                    return;
                } catch (CmdException e) {
                    System.err.println(e.getMessage());
                    System.out.print("> ");
                    return;
                }
            }
            map.findConnections(numberOfRoads, length);
            isJunctionsDetected = true;
        }
        if(outputName != null) {
            if(printRoads != null && printRoads) {
                if(map.toFileRoads(appendBeforeFormat(outputName, "roads")) == 0) {
                    System.out.println("Roads file created!");
                    System.out.print("> ");
                } else {
                    System.err.println("The system cannot find output folder!");
                    System.out.print("> ");
                    return;
                }
            }
            if(printCrossingPoints != null && printCrossingPoints) {
                if(map.toFileCrossingPoints(appendBeforeFormat(outputName, "crossing_points")) == 0) {
                    System.out.println("Crossing points file created!");
                    System.out.print("> ");
                } else {
                    System.err.println("The system cannot find output folder!");
                    System.out.print("> ");
                    return;
                }
            }
            if(printConnections != null && printConnections) {
                if(isJunctionsDetected) {
                    if(map.toFileConnections(appendBeforeFormat(outputName,"connections")) == 0) {
                        System.out.println("Connections file created!");
                        System.out.print("> ");
                    } else {
                        System.err.println("The system cannot find output folder!");
                        System.out.print("> ");
                        return;
                    }
                } else {
                    System.err.println("Connections are not printed because junctions are not detected yet!");
                    System.out.print("> ");
                }
            }
            if(printJunctions != null && printJunctions) {
                if(isJunctionsDetected) {
                    if(map.toFilesJunctions(appendBeforeFormat(outputName,"junction"), crossingPoints, sides, levels, conflictRoads, intersections) == -1) {
                        return;
                    }
                } else {
                    System.err.println("Junctions are not printed because they are not detected yet!");
                    System.out.print("> ");
                }
            }
        }
    }

    private String appendBeforeFormat(String fileName, String appending) {
        return String.format("%s_%s%s", fileName.substring(0,fileName.lastIndexOf('.')), appending, fileName.substring(fileName.lastIndexOf('.')));
    }

    public static void main(String[] args) {
        Main main = new Main();
        System.exit(main.runCmd());
    }

    private static void printHelp() {
        System.out.println(" ____________");
        System.out.println(" _|    HELP    |__________________________________________________________________________________________");
        System.out.println();
        System.out.println(" NOTICE: Detection and Output commands will not work if Source Command is not entered.");
        System.out.println("         Output Connections and Junctions commands will not work if Detection Command is not entered.");
        System.out.println("         Two or all commands can be entered together if they hold two previous rules.");
        System.out.println("         Example: -I C:/maps/source.csv -T r -R 6 -L 900 -O C:/analysis/output.csv -P cjrx -x >3");
        System.out.println();
        System.out.println(" 1. ---- Setting Source Command --------------------------------------------------------------------------");
        System.out.println(" -I [path_to_file] -T [r/l]");
        System.out.println(" Example: -I C:/maps/source.csv -T r");
        System.out.println("        '-I' indicates input - path to the source file which is in CSV format.");
        System.out.println("        '-T' indicates traffic side. 'r' - if right hand traffic. 'l' - if left hand traffic.");
        System.out.println();
        System.out.println(" 2. ---- Detection Command -------------------------------------------------------------------------------");
        System.out.println(" -R [number_of_roads] -L [max_length]");
        System.out.println(" Example: -R 6 -L 900");
        System.out.println("        '-R' indicates unsigned integer which sets number of roads in connection, within length is not checked.");
        System.out.println("        '-L' indicates unsigned floating point number which sets max length of connection.");
        System.out.println("        NOTICE: Set length can be exceeded if connection have number of roads which is less or equal to 'number_of_roads'.");
        System.out.println();
        System.out.println(" 3. ---- Output Command ----------------------------------------------------------------------------------");
        System.out.println(" -O [path_to_file] -P [c|j|r|x]");
        System.out.println(" Example: -O C:/analysis/output.csv -P cjrx");
        System.out.println("        '-O' indicates path to the output location. Output file name is extended depending on what is written in the file.");
        System.out.println("        '-P' indicates what output files to print. c - connections, j - junction, r - all roads, x - crossing points.");
        System.out.println();
        System.out.println("        3.1. ---- Junction Output Parameters -------------------------------------------------------------");
        System.out.println("        NOTICE: These are optional Output Command parameters which filter junctions output.");
        System.out.println("                Each parameter can be set as range or specific value. Range includes both entered values [from;to].");
        System.out.println("        -[x/s/l/i/c] ( [</>/=][unsigned_integer] OR [unsigned_integer_from]-[unsigned_integer_to] )");
        System.out.println("        Example: -O C:/analysis/output.csv -P j -x >3 -s <2 -l =2 -c 2-4");
        System.out.println("        '-x' indicates crossing points number");
        System.out.println("        '-s' indicates branches number");
        System.out.println("        '-l' indicates levels number");
        System.out.println("        '-i' indicates intersections number");
        System.out.println("        '-c' indicates conflict roads number");
        System.out.println();
        System.out.println(" ---- Program Commands -----------------------------------------------------------------------------------");
        System.out.println(" help - show all commands.");
        System.out.println(" exit - exits program.");
        System.out.println(" _________________________________________________________________________________________________________");
    }
}