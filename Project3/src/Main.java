import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Waiting for First command");
        String line = scanner.nextLine();
        while(Character.compare(line.charAt(0), 'E') != 0){
            String[] command =  line.split("\\s");

            if(command[1].equals("A.Col1,")){
                Index index =  new Index();
                index.hashBasedJoin("A", "B");
                System.out.println("Waiting for next command");
                line = scanner.nextLine();
            }
            else if(command[1].equals("count(*)")){
                Index index = new Index();
                index.nestedLoopJoin();
                System.out.println("Waiting for next command");
                line = scanner.nextLine();
            }
            else if(command[1].equals("Col2,")){
                String function = command[2].substring(0, 3);
                String dataset = command[4];
                Index index = new Index();
                index.aggregationFunction(function, dataset);
                System.out.println("Waiting for next command");
                line = scanner.nextLine();

            }
            else{
                System.out.println("Command not found, please follow command structure from README.txt");
                System.out.println("Waiting for next command");
                line = scanner.nextLine();
            }
        }
    }
}
