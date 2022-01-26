import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class battleships {
    private static String shipLengths = "54432";                // Amount and length of ships. (Example: 54432 means 1 x 5 blocks, 2x 4 blocks etc.)
    private static int winPoints = maxPoints();                 // depends on shipLengths (Example: 54432 = 19)
    private static boolean player2Human = false;                // is player 2 human player or computer

    private static char[][] player1ownMap = makeMapMatrix();    // 4 global matrixes are declared
    private static char[][] player1shootMap = makeMapMatrix();
    private static char[][] player2ownMap = makeMapMatrix();
    private static char[][] player2shootMap = makeMapMatrix();

    private static int player1Points = 0;                       // Both players' points are declared
    private static int player2Points = 0;

    public static Scanner input = new Scanner(System.in);       // Scanner for user input
    public static Random rn = new Random();                     // Random
    
    /** 
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        mainMenu();
    }


/** 
 * Main menu is where the game starts. Player can choose if they want to exit, play against computer, play multi player or read the instructions.
 * @throws InterruptedException
 * @throws IOException
 */
// MAIN MENU
    public static void mainMenu() throws InterruptedException, IOException {
        flushAll();         // resets all maps and points when entering the menu
        int choice = -1;
        clearScreen();
            print("\n" + "Battleships Mega Ultra 2021   (c) Jesse Kottonen 2021     - kaikki oikeudenrikkojat suljetaan tyrmään.");
            print("\n" +
               "0 - End \n" +
               "1 - Man vs machine \n" +
               "2 - Captain vs captain \n" +
               "3 - How to play \n" +
               "4 - Custom game (for advanced debugging purposes only) \n"
               );
        do {
            choice = Integer.parseInt(filterInput("Choose: ", 2));
        
            switch(choice) {
                case 0:
                    print("END");
                    System.exit(0);
                    break;
                case 1:
                    player2Human = false;
                    play();
                    break;
                case 2:
                    player2Human = true;
                    play();
                    break;
                case 3:
                    readFile();
                    break;
                case 4:
                    customMenu();
                    break;
            }
        }
        while (choice < 0 || choice > 4);

    }
    
    /** Custom menu let's player decide the amount and length of ships.
     * @throws InterruptedException
     * @throws IOException
     */
    public static void customMenu() throws InterruptedException, IOException {
        clearScreen();
        print("Current seed: " + battleships.shipLengths + "\nInsert custom seed or empty to use the current seed \n");
        String line = userInput("Custom seed: ");
        if (!line.equals("")) {
            battleships.shipLengths = line;
        }
        mainMenu();
        
    }

/** 
 * Play method starts the game.
 * Human players can choose wether to place their ships or generate them randomly.
 * The program loops between players' turns until other one of the players reach max points.
 * When max points are reached, program prints the winner and reveals both player's remaining ships.
 * @throws InterruptedException
 * @throws IOException
 */
// PLAY METHOD
    public static void play() throws InterruptedException, IOException {
    int winner = 0;
    // Ships are arranged
        buildShipsMenu(1);
        if(player2Human) {
            buildShipsMenu(2);
        }
        else {
            fillMap(2,true);
        }
        clearScreen();

    // The game loop:
        while(player1Points < winPoints && player2Points < winPoints) {
            playerTurn(1, true);
            if(player1Points >= winPoints || player2Points >= winPoints) {
                break;
            }
            playerTurn(2, player2Human);
        }
    // Declaring the winner:
        if(player1Points > player2Points) {
            winner = 1;
        }
        else {
            winner = 2;
        }
        clearScreen();
        print("Player " + winner + " WINS!!! \n \n");
        print("Player 1's map:");
        printMatrix(player1ownMap);
        print("Player 2's map:");
        printMatrix(player2ownMap);
        userInput("\n" + "Press ENTER to quit");
        System.exit(0);

    }
    
    /** 
     * Turn -method is given parameters player and isHuman.
     * If player is human, printUI is called and the player will see their own ships,
     * their target map and points.
     * Human players are asked to insert coordinates for shooting and computer player has them randomised.
     * Human player's coordinates go through filterInput, so they will be in the right format
     * @param player
     * @param isHuman
     * @throws InterruptedException
     * @throws IOException
     */
    public static void playerTurn(int player, boolean isHuman) throws InterruptedException, IOException {
        String[] coordinates;
        int x = 0;
        int y = 0;
        if (player == 2 && !isHuman) {
            do {
                x = rn.nextInt(10) +1;
                y = rn.nextInt(10) +1;
            }
            while(player2shootMap[y][x] != '-');
            shot(2, player1ownMap, player2shootMap, x, y);
        }
        if (player == 1) {
            clearScreen();
            userInput("Player " + player + " Press ENTER");
            printUI(player, player1Points, player2Points, player1shootMap, player1ownMap);
            do {
                coordinates = filterInput("Insert coordinates to shoot: ", 1).split(" ");
                x = letterToInt(coordinates[0]);
                y = Integer.parseInt(coordinates[1]);
            }
            while (player1shootMap[y][x] != '-');
            shot(player, player2ownMap, player1shootMap, x, y);

            printUI(player, player1Points, player2Points, player1shootMap, player1ownMap);
            userInput("Press ENTER");
        }
        if (player == 2 && isHuman) {
            clearScreen();
            userInput("Player " + player + " Press ENTER");
            printUI(player, player2Points, player1Points, player2shootMap, player2ownMap);
            do {
                coordinates = filterInput("Insert coordinates to shoot: ", 1).split(" ");
                x = letterToInt(coordinates[0]);
                y = Integer.parseInt(coordinates[1]);
            }
            while (player2shootMap[y][x] != '-');
            shot(player, player1ownMap, player2shootMap,x,y);

            printUI(player, player2Points, player1Points, player2shootMap, player2ownMap);
            userInput("Press ENTER");
        }
    }

    
    /** 
     * Shot method is given the player id, opponent's map, player's target map and coordinates.
     * opponent's map will be used to check if the shot hits or misses.
     * if it hits, players points are added by 1.
     * Both players' maps are updated
     * 
     * @param player        player id
     * @param enemyMap      opponents map of ships
     * @param shootMap      players target map
     * @param x             x -coordinate
     * @param y             y - coordinate
     * @throws InterruptedException
     * @throws IOException
     */
    // SHOT
    public static void shot(int player, char[][] enemyMap, char[][] shootMap, int x, int y) throws InterruptedException, IOException {
        if (enemyMap[y][x] == 'O') {
            shootMap[y][x] = 'X';
            enemyMap[y][x] = '¤';
            clearScreen();
            if(player == 1) {
                player1Points ++;
            }
            else {
                player2Points ++;
            }
            //userInput("Player "+player+" HIT! \n");

        }
        else {
            shootMap[y][x] = '?';
            enemyMap[y][x] = '?';
            clearScreen();
            //print("Player "+player+"\nMISS! \n");
        }
    }

/** printUI prints the ui for human players.
 * enemmyPoints and winPoints is used to calculate health bar
 * @param player
 * @param ownPoints
 * @param enemyPoints
 * @param shootMap
 * @param ownMap
 * @throws InterruptedException
 * @throws IOException
 */
// PRINT UI
    public static void printUI (int player, int ownPoints, int enemyPoints, char[][] shootMap, char [][] ownMap) throws InterruptedException, IOException {
        //userInput("PLAYER "+ player + " PRESS ENTER:");
        clearScreen();
        // If player is human, UI is printed.
        print("\n----------- PLAYER "+ player + " -----------");
        
        // BOARD:
            print("\nOWN SHIPS: ");
            printMatrix(ownMap);

            print("\nENEMY SHIPS: ");
            printMatrix(shootMap);

        // HEALTH BAR:
            String health = "=".repeat(winPoints-enemyPoints) + " ".repeat(enemyPoints);
            print("\n"+"Your health: " + "|" + health + "|");
            
        // POINT COUNT:
            print("\n"+"Your points: " + ownPoints + "/" + winPoints +"\n");
    }


/** In ship building menu, the human players can choose if they want to place their ships or automatically generate them.
 * @param player
 * @throws InterruptedException
 * @throws IOException
 */
// SHIP BUILDING MENU
    public static void buildShipsMenu(int player) throws InterruptedException, IOException {
        clearScreen();
        int choice = -1;
        print("PLAYER " + player + ":\n");
        print("1 - place ships\n");
        print("2 - random ship placement\n");
        print("3 - main menu\n");
        do {
            choice = Integer.parseInt(filterInput("Choose: ", 2));
            switch(choice) {
                case 1:
                    fillMap(player,false);
                    break;
                case 2:
                    fillMap(player, true);
                    break;
                case 3:
                    mainMenu();
                    break;
                
            }
        }
        while(choice < 1 || choice > 3);
    }

/** fillMap is used for creating ships. One revolution of for loop means one ship.
 * @param player
 * @param random
 * @throws InterruptedException
 * @throws IOException
 */
// Fill map with ships
    public static void fillMap (int player, boolean random) throws InterruptedException, IOException {
        for (int i = 0; i < shipLengths.length(); i++) {
            clearScreen();
            if(player == 1 && !random) {
                printMatrix(player1ownMap);
                player1ownMap = createShip(1, player, player1ownMap, Integer.parseInt(Character.toString(shipLengths.charAt(i))));
            }
            else if(player == 1 && random) {
                player1ownMap = createShip(2, player, player1ownMap, Integer.parseInt(Character.toString(shipLengths.charAt(i))));
            }
            if(player == 2 && !random) {
                printMatrix(player2ownMap);
                player2ownMap = createShip(1, player, player2ownMap, Integer.parseInt(Character.toString(shipLengths.charAt(i))));
            }
            else if(player == 2 && random) {
                player2ownMap = createShip(2, player, player2ownMap, Integer.parseInt(Character.toString(shipLengths.charAt(i))));
            }
        }
    }

/** Create ship literally creates the ship.
 * If player is not human, or has chosen to randomly generate their ships, it won't ask for coordinates.
 * The method asks canbuild-method if the ship can be generated with the instructions given. If not, it asks
 * for new coordinates and direction
 * 
 * @param choice    if 1, player gives starting coordinates and direction of the ship
 * @param player    player id
 * @param map       player's own map
 * @param longness  length of the ship that is currently generated
 * @return char[][]
 * @throws IOException
 * @throws InterruptedException
 */
// CREATE A SHIP OF X BLOCKS
    public static char[][] createShip(int choice, int player, char[][] map, int longness) throws InterruptedException, IOException {
        int x = 0;
        int y = 0;
        String direction = "";
        boolean build = false;
        while(!build) {
            if (choice == 1){
                print("PLAYER "+ player + ": \n");
                String[] coordinates = filterInput("Starting point of " + longness + " blocks long ship " + "(A 1 - J 0): ", 1).split(" ");
                x = letterToInt(coordinates[0].toUpperCase());
                y = Integer.parseInt(coordinates[1]);
                //print(x + " " + y);
                direction = filterInput("Which way to create the ship? (N,E,S,W): ",3);
            }
            if(choice == 2) {
                x = rn.nextInt(10) +1;
                y = rn.nextInt(10) +1;
                direction = "" + rn.nextInt(2);
                if(direction.equals("1")) {
                    direction = "N";
                }
                else {
                    direction = "W";
                }
            }
            build = canbuild(choice, map, x, y, longness, direction);
        }

            map = shipBuild(map, x, y, longness, direction);
        

        return map;
    }

/** ShipBuild generates a ship, if canbuild is true
 * @param map
 * @param x
 * @param y
 * @param longness
 * @param direction
 * @return char[][]
 */
// COMMIT SHIP
    public static char[][] shipBuild(char[][] map, int x, int y, int longness, String direction) {
        for (int i = 0; i < longness; i++) {
            map[y][x] = 'O';
            if (direction.equals("N") || direction.equals("1")) {
                y--;
            }
            else if (direction.equals("E") || direction.equals("2")) {
                x++;
            }
            else if (direction.equals("S") || direction.equals("3")) {
                y++;
            }
            else {
                x--;
            }
        }
        
        return map;
    }

/** checks if a ship can be generated by the instructions given by player or generated randomly.
 * false is returned if the ship is to collide with another ship or would be generated off the map.
 * @param choice
 * @param map
 * @param x
 * @param y
 * @param longness
 * @param direction
 * @return boolean
 */
// CHECK IF SHIP CAN BE BUILT
    public static boolean canbuild(int choice, char[][] map, int x ,int y, int longness, String direction) {
        for (int i = 0; i < longness; i++) {
            if (x < 1 || x > 10 || y < 1 || y > 10) {
                if(choice == 1) {
                    print("\nINVALID PLACEMENT!\n");
                }
                return false;
            }
            if (map[y][x] != '-') {
                return false;
            }
            if (direction.equals("N")) {
                y--;
            }
            else if (direction.equals("E")) {
                x++;
            }
            else if (direction.equals("S")) {
                y++;
            }
            else {
                x--;
            }
            
        }
        return true;
        
    }
    
    
    /** Turns letters A-J to corresponding numbers for coordinates
     * @param letter
     * @return int
     */
    // LETTER TO INT
    public static int letterToInt(String letter) {
        switch(letter.toUpperCase()) {
            case "A":
                return 1;
            case "B":
                return 2;
            case "C":
                return 3;
            case "D":
                return 4;
            case "E":
                return 5;
            case "F":
                return 6;
            case "G":
                return 7;
            case "H":
                return 8;
            case "I":
                return 9;
            case "J":
                return 10;
            default:
                return -2;
        }
    }

    
    /** User input prints out the question and returns the user input.
     * if exit is inserted, the program will end
     * @param question
     * @return String
     * @throws IOException
     * @throws InterruptedException
     */
    // USER INPUT
    public static String userInput(String question) throws InterruptedException, IOException {
        System.out.print(question);
        String line = input.nextLine();
        if(line.equals("exit") || line.equals("EXIT")) {
            print("END");
            System.exit(0);
        }
        if(line.equals("menu") || line.equals("MENU")) {
            mainMenu();
        }
        return line;
    }

    
    /** Filter input is used in between methods and user input, if needed.
     * it chooses one of three regex filters that are defined when the method is called.
     * 1 - Let's player insert only coordinates (Example "A 1" or "J 10")
     * 2 - Let's player insert only numbers 0-9
     * 3 - Let's player insert only N, E, W, or S
     * 
     * @param question
     * @param choice123
     * @return String
     * @throws IOException
     * @throws InterruptedException
     */
    // COORDINATE/NUMBER FILTER FOR USER INPUT
    public static String filterInput(String question, int choice123) throws InterruptedException, IOException {
        String re = "";
        String line = "";
        if(choice123 == 1) {
            re = "^[A-J] ([1-9]|10)$";
        }
        else if(choice123 == 2) {
            re = "[0-9]";
        }
        else if(choice123 == 3) {
            re = "N|E|W|S";
        }
        else {
            System.out.println("Kyssärissä "+question+"Väärä valinta filterInputtiin!");
        }
        do {
            line = userInput(question).toUpperCase();
        }
        while(!line.matches(re));
        
        return line;
    }

    
    /** Generates an empty matrix with letters A-J above and numbers 1-10 on the left side
     * Empty spaces are marked with '-'
     * @return char[][]
     */
    // MAKE AN EMPTY MAP MATRIX
    public static char[][] makeMapMatrix() {
        char[][] matrix = new char[11][11];
        String alphabets = " ABCDEFGHIJ";
        String numbers = "1234567890";
        for (int x = 0; x < 11; x ++) {
            matrix[0][x] = alphabets.charAt(x);
        }
        for (int y = 1; y < 11; y ++) {
            matrix[y][0] = numbers.charAt(y -1);
        }
        for (int y = 1; y < 11; y++) {
            for (int x = 1; x < 11; x++) {
                matrix[y][x] = '-';
            }
        }
        return matrix;
    }

    
    /** Matrix printer. Iterates a matrix and prints it
     * @param matrix
     */
    // PRINT MATRIX
    public static void printMatrix(char[][] matrix) {
        for (int y = 0; y < matrix.length; y++) {
            System.out.print("\n");
            for (int x = 0; x < matrix[y].length; x++) {
                System.out.print(" " + matrix[y][x] + " ");
            }
        }
        System.out.print("\n");
    }

    
    /** Prints any string given
     * @param string
     */
    // PRINT
    public static void print(String string) {
        System.out.print(string);
    }

    
    /** File reader is used to read and print the instructions on the console screen for the player
     * @throws InterruptedException
     * @throws IOException
     */
    // FILE READER
    public static void readFile() throws InterruptedException, IOException {
        clearScreen();
        final Scanner lukija = new Scanner(new File("how_to_play.txt"));
        String rivi = "";
        while (lukija.hasNext() ) {
            rivi = lukija.nextLine();
            System.out.println(rivi);
        }
    userInput("Press Enter");
    mainMenu();
        
    }
    
    /** maxPoints method iterates the shipLengths -string and calculates
     * how many hits are needed for winning the game
     * @return int
     */
    public static int maxPoints() {
        int max = 0;
        for (int i = 0; i < shipLengths.length(); i++) {
            max += Integer.parseInt(""+shipLengths.charAt(i));
        }
        return max;
    }
    
    /** Clear screen clears the console when called.
     * (Works on VsCode and windows command prompt for sure)
     * @throws InterruptedException
     * @throws IOException
     */
    // CLEAR SCREEN
    public static void clearScreen() throws InterruptedException, IOException {
        // System.out.print("\033[H\033[2J");
        // System.out.flush();
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        }

    /**
     * flush All resets all points and maps when entering main menu
     */
    public static void flushAll() {
        battleships.player1ownMap = makeMapMatrix();
        battleships.player2ownMap = makeMapMatrix();
        battleships.player1shootMap = makeMapMatrix();
        battleships.player2shootMap = makeMapMatrix();
        battleships.player1Points = 0;
        battleships.player2Points = 0;
        battleships.winPoints = maxPoints();
    }
}