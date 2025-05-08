import java.sql.*;
import java.util.Scanner;

public class DatabaseAIApp {
    public static void main(String[] args) {
        // Credentials
        String url = "";
        String user = "";
        String password = "";

        // prompt bittles
        String promptSchema = "Given the following Schema and Message, convert the message into a SQL query. Schema:\n" +
                "Table: payment_types\n" +
                "- payment_type_id (INT, primary key)\n" +
                "- payment_type (VARCHAR)\n" +
                "\n" +
                "Table: status_types\n" +
                "- status_id (INT, primary key)\n" +
                "- status (VARCHAR)\n" +
                "\n" +
                "Table: product_types\n" +
                "- product_type_id (INT, primary key)\n" +
                "- product_type (VARCHAR)\n" +
                "\n" +
                "Table: vending_machine\n" +
                "- machine_id (INT, primary key)\n" +
                "- location (VARCHAR)\n" +
                "- status_id (INT, foreign key -> status_types.status_id)\n" +
                "- is_refrigerated (TINYINT)\n" +
                "- cash_on_hand (DECIMAL)\n" +
                "\n" +
                "Table: transactions\n" +
                "- transaction_id (INT, primary key)\n" +
                "- machine_id (INT, foreign key -> vending_machine.machine_id)\n" +
                "- total_amount (DECIMAL)\n" +
                "- payment_type_id (INT, foreign key -> payment_types.payment_type_id)\n" +
                "\n" +
                "Table: slot\n" +
                "- slot_id (INT, primary key)\n" +
                "- machine_id (INT, foreign key -> vending_machine.machine_id)\n" +
                "- product_id (INT, foreign key -> product.product_id)\n" +
                "- quantity (TINYINT)\n" +
                "- max_quantity (TINYINT)\n" +
                "\n" +
                "Table: product\n" +
                "- product_id (INT, primary key)\n" +
                "- name (VARCHAR)\n" +
                "- price (DECIMAL)\n" +
                "- product_type_id (INT, foreign key -> product_types.product_type_id)\n" +
                "\n" +
                "Table: transaction_product\n" +
                "- transaction_product_id (INT, primary key)\n" +
                "- transaction_id (INT, foreign key -> transactions.transaction_id)\n" +
                "- product_id (INT, foreign key -> product.product_id)\n" +
                "- amount (TINYINT)\n\n" + "Message:\n";

        String promptConvertQuery = "Convert the following SQL query result into plain English that could be understood by someone with little database experience.\n" +
                "The query is also provided to give you additional context.\n" +
                "Query:\n";

        String promptConvertResult = "Result to convert:\n";

        // do intro stuff
        System.out.println("*put an intro explanation here later*");
        boolean keepLooping = true;
        Scanner scan = new Scanner(System.in);
        ChatGPTClient chatGPT = new ChatGPTClient();


        // do while loop
        while (keepLooping) {
            boolean runInDebug = false;
            System.out.println("-------------------------------");
            System.out.println("[1] - Generate SQL w/ AI");
            System.out.println("[2] - Run in Debug Mode");
            System.out.println("[3] - Exit");
            System.out.println("-------------------------------");
            System.out.print("Your choice: ");
            char inputOption = scan.next().charAt(0);

            if (inputOption == '2') {
                // is this a lil bit of a hack? Probably
                // but it's marginally better than just commenting out print statements lol
                runInDebug = true;
                inputOption = '1';
            }

            // handle query route
            if (inputOption == '1') {
                // ask user for GPT 'plain english'
                System.out.println("Type your DB request, and ChatGPT will try to convert it to SQL:");
                Scanner reqScan = new Scanner(System.in);
                String request = reqScan.nextLine();
                System.out.println();

                // assemble the query with additional bits
                String assembledRequest = promptSchema + request;

                // put result into a query
                String query = chatGPT.sendAndConvertMessage(assembledRequest);

                if (runInDebug) System.out.println("Your AI Generated SQL query is: " + query);

                // do query cleanup to remove the ```sql lines
                String cleanedQuery = query.trim();
                if (cleanedQuery.startsWith("```sql")) {
                    cleanedQuery = cleanedQuery.substring(6);  // remove "```sql"
                }
                if (cleanedQuery.endsWith("```")) {
                    cleanedQuery = cleanedQuery.substring(0, cleanedQuery.length() - 3).trim();  // remove "```"
                }

                if (runInDebug) System.out.println("Your cleaned up query is: " + cleanedQuery);
                StringBuilder sb = new StringBuilder();

                try {
                    // Connect to the database
                    Connection conn = DriverManager.getConnection(url, user, password);
                    // Create a statement
                    Statement stmt = conn.createStatement();
                    // execute the query
                    boolean hasResultSet = stmt.execute(cleanedQuery);

                    if (hasResultSet) {
                        // It's a SELECT or similar query
                        ResultSet rs = stmt.getResultSet();
                        ResultSetMetaData metaData = rs.getMetaData();
                        int totalColumns = metaData.getColumnCount();

                        while (rs.next()) {
                            for (int i = 1; i <= totalColumns; i++) {
                                String columnName = metaData.getColumnLabel(i);
                                Object value = rs.getObject(i);
                                sb.append(columnName).append(": ").append(value).append("  ");
                                //System.out.print(columnName + ": " + value + "  ");
                            }
                            sb.append("\n");
                            //System.out.println();
                        }

                        rs.close();
                    } else {
                        // It's an update query (INSERT, UPDATE, DELETE, etc.)
                        int updateCount = stmt.getUpdateCount();
                        sb.append("Number of updates: ").append(updateCount).append("\n");
                        //System.out.println("Number of updates: " + updateCount);
                    }

                    // Close up shop
                    stmt.close();
                    conn.close();

                    // to see query results if you want
                    if (runInDebug) System.out.println(sb);

                    // send string to ChatGPT with query addons
                    String convertQuery = promptConvertQuery + cleanedQuery +
                            "\n" + promptConvertResult + sb;

                    String plainEnglishResponse = chatGPT.sendAndConvertMessage(convertQuery);
                    System.out.println("Your AI response is: ");
                    System.out.println(plainEnglishResponse + "\n");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // handle exit route
            else if (inputOption == '3') {
                System.out.println("Exiting...");
                keepLooping = false;
            }

            else {
                System.out.println("Invalid input - " + inputOption);
            }
        }
    }
}
