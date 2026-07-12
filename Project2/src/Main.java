import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Program is ready and waiting for user command.");
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        Index tableIndexes = new Index();
        while(Character.compare(line.charAt(0), 'E') != 0){
            // Commands List:
            // CREATE INDEX
            if(Character.compare(line.charAt(0), 'C') == 0){
                tableIndexes.create();
                System.out.println("Program is ready and waiting for user command.");
                line = scanner.nextLine();
            }
            // SELECT
            else if (Character.compare(line.charAt(0), 'S') == 0){
                // Equality
                if (Character.compare(line.charAt(44), '=') == 0){
                    System.out.println(tableIndexes.equalitySearch(Integer.parseInt(line.substring(46))));
                }
                // Range
                else if (Character.compare(line.charAt(44), '>') == 0){
                    int andIndex = line.indexOf("AND");
                    int randomV1 = Integer.parseInt(line.substring(46, andIndex-1));
                    System.out.println(tableIndexes.rangeSearch(randomV1,
                            Integer.parseInt(line.substring(andIndex+14))));
                }
                // Inequality
                else if (Character.compare(line.charAt(44), '!') == 0){
                    System.out.println(tableIndexes.inequalitySearch(Integer.parseInt(line.substring(47))));
                }
                System.out.println("Program is ready and waiting for user command.");
                line = scanner.nextLine();
            }
        }
    }
}
