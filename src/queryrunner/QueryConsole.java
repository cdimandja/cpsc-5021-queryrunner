package queryrunner;

import java.util.Scanner;

/**
 * CLI tool we've been building in all of our other classes.
 */
public class QueryConsole {
    public enum MENU_OPTION {
        query,
        help,
        quit;

        public static int ordinalOf(String value) {
            if (!isValid(value)) {
                return -1;
            }

            return MENU_OPTION.valueOf(value).ordinal();
        }

        public static MENU_OPTION fromOrdinal(int value) {
            return MENU_OPTION.values()[value];
        }

        public static boolean isValid(String value) {
            try {
                MENU_OPTION.valueOf(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public static boolean isValid(int value) {
            return value >= 0 && value < MENU_OPTION.values().length;
        }
    }

    public static void main(String[] args, QueryRunner runner) {
        int menuOption = -1;
        int connectionAttempts = 0;
        boolean notConnected = false;

        Scanner userIn = new Scanner(System.in);

        greeting();

        // Connect to the mySQL database
        while (!notConnected) {
            if (connectionAttempts > 0) {
                System.out.println(
                    "We need to establish a connection to the database, to do "
                );
                System.out.println(
                    "so we will need a few pieces of information from you."
                );
            } else {
                System.out.println(
                    "The previous connection attempt failed, please try again"
                );
            }

            notConnected = connectPrompt(userIn, runner);
            connectionAttempts++;
        }

        // Menu
        while (isContinue(menuOption)) {
            printMenu();
            int option = menu(userIn, runner);

            if (option == MENU_OPTION.query.ordinal()) {
                queryMenu(userIn, runner);
            } else if (option == MENU_OPTION.help.ordinal()) {
                help();
            }
        }

        /* CLI tool to add all the gui elements,
         * then show menu
         * add new parameters
         * prompt for data, these data will be then used to the 'routine'
         * these routines are partially filled s    ql queries or stored procedures.
         *
         * Data structure and code will grab the parameters needed for the sql query
         * underneath.
         *
         */

        System.out.println("Nothing has been implemented yet. Please implement the necessary code");
        // TODO
        // You should code the following functionality:

        //    You need to determine if it is a parameter query. If it is, then
        //    you will need to ask the user to put in the values for the Parameters in your query
        //    you will then call ExecuteQuery or ExecuteUpdate (depending on whether it is an action query or regular query)
        //    if it is a regular query, you should then get the data by calling GetQueryData. You should then display this
        //    output.
        //    If it is an action query, you will tell how many row's were affected by it.
        //
        //    This is Psuedo Code for the task:
        //    Connect()
        //    n = GetTotalQueries()
        //    for (i=0;i < n; i++)
        //    {
        //       Is it a query that Has Parameters
        //       Then
        //           amt = find out how many parameters it has
        //           Create a paramter array of strings for that amount
        //           for (j=0; j< amt; j++)
        //              Get The Paramater Label for Query and print it to console. Ask the user to enter a value
        //              Take the value you got and put it into your parameter array
        //           If it is an Action Query then
        //              call ExecuteUpdate to run the Query
        //              call GetUpdateAmount to find out how many rows were affected, and print that value
        //           else
        //               call ExecuteQuery
        //               call GetQueryData to get the results back
        //               print out all the results
        //           end if
        //      }

        // NOTE - IF THERE ARE ANY ERRORS, please print the Error output
        // NOTE - The QueryRunner functions call the various JDBC Functions that are in QueryJDBC. If you would rather code JDBC
        // functions directly, you can choose to do that. It will be harder, but that is your option.
        // NOTE - You can look at the QueryRunner API calls that are in QueryFrame.java for assistance. You should not have to
        //    alter any code in QueryJDBC, QueryData, or QueryFrame to make this work.
//                System.out.println("Please write the non-gui functionality");


        farewell();
    }

    public static boolean connectPrompt(Scanner userIn, QueryRunner runner) {
        String host, user, pass, database;

        System.out.print("Enter the hostname (localhost?): ");
        host = userIn.nextLine().trim();

        System.out.print("Enter the username (root, etc.): ");
        user = userIn.nextLine().trim();

        System.out.print("Enter the password: ");
        pass = userIn.nextLine().trim();

        System.out.print(
                "Enter the database you wish to use (mm_cpsc502101team05): "
        );
        database = userIn.nextLine().trim();

        runner.Connect(host, user, pass, database);

        if (runner.GetError() != null) {
            System.out.println("Error: " + runner.GetError());

            runner.ClearError();
            return false;
        }

        return true;
    }

    public static int menu(Scanner userIn, QueryRunner runner) {
        boolean isContinue = true;
        String chosenOption;
        int selectedOption = -1;

        while (isContinue) {
            System.out.print("Please select an option (1,2,...): ");
            chosenOption = userIn.nextLine().trim();
            System.out.println();

            try {
                int chosenInt = Integer.parseInt(chosenOption);

                if (chosenInt == MENU_OPTION.query.ordinal()) {
                    queryMenu(userIn, runner);

                } else if (chosenInt == MENU_OPTION.help.ordinal()) {
                    help();
                } else if (chosenInt == MENU_OPTION.quit.ordinal()) {
                    isContinue = false;

                }
            } catch (Exception e) {
                continue;
            }
        }

        return selectedOption;
    }

    public static boolean isContinue(int option) {
        return MENU_OPTION.quit.ordinal() != option;
    }

    public static void queryMenu(Scanner userIn, QueryRunner runner) {
        int selectedQuery = -1;
        printQueryList(runner);

        while (
            !(selectedQuery >= 0 && selectedQuery < runner.GetTotalQueries())
        ) {
            String input = userIn.nextLine().trim();

            try {
                selectedQuery = Integer.parseInt(input);
                handleQuery(userIn, runner, selectedQuery);
            } catch (Exception e) {
                selectedQuery = -1;
            }
        }
    }

    public static void handleQuery(
            Scanner userIn,
            QueryRunner runner,
            int selectedQuery) {
        int paramCount = runner.GetParameterAmtForQuery(selectedQuery);
        String[] params = new String[paramCount];

        if (runner.isParameterQuery(selectedQuery)) {
            for (int i = 0; i < paramCount; i++) {
                String paramText = runner.GetParamText(selectedQuery, i);

                System.out.print(paramText + ": ");
                params[i] = userIn.nextLine().trim();
            }
        }

        if (runner.isActionQuery(selectedQuery)) {
            executeUpdate(runner, selectedQuery, params);
        } else {
            executeQuery(runner, selectedQuery, params);
        }
    }

    public static void executeQuery(
            QueryRunner runner, int queryChoice, String[] params) {

        runner.ExecuteQuery(queryChoice, params);
    }

    public static void executeUpdate(
            QueryRunner runner, int queryChoice, String[] params) {

        runner.ExecuteUpdate(queryChoice, params);
    }

    public static void printMenu() {
        System.out.println("-".repeat(15));
        System.out.println("Menu");
        System.out.println("-".repeat(15));

        for (int i = 0; i < MENU_OPTION.values().length; i++) {
            System.out.println(i + ": " + MENU_OPTION.fromOrdinal(i));
        }

        System.out.println("-".repeat(15));
        System.out.println();
    }

    public static void printQueryList(QueryRunner runner) {
        System.out.println("-".repeat(15));
        System.out.println("Queries");
        System.out.println("-".repeat(15));

        for (int i = 0; i < runner.GetTotalQueries(); i++) {
            String queryText = runner.GetQueryText(i);

            System.out.println(i + ": " + queryText.subSequence(0, 10) + "...");
        }

        System.out.println("-".repeat(15));
        System.out.println();
    }

    public static void greeting() {
        System.out.println("Welcome to the Query Console tool");
        System.out.println("TODO greeting");
    }

    public static void farewell() {
        System.out.println("TODO farewell");

    }

    public static void help() {
        System.out.println("TODO help");
    }
}
