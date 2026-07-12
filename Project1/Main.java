import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        // Ask for buffer pool size from user
        System.out.print("Enter Buffer Pool Size: ");
        int size = Integer.parseInt(scanner.nextLine());

        BufferPool bufferPool = new BufferPool();
        bufferPool.initialize(size);

        System.out.println("Buffer Initialized with " + size + " frames");
        System.out.println("The program is ready for the next command");

        while (true) {
            String line = scanner.nextLine().trim();

            // Exit condition
            if (line.equalsIgnoreCase("E")) {
                break;
            }

            // Strip inline comments
            line = line.split("//")[0].trim();

            // Skip empty or comment-only lines
            if (line.isEmpty()) {
                continue;
            }

            try {
                if (line.startsWith("G")) { // GET
                    int recordNum = Integer.parseInt(line.substring(4).trim());
                    System.out.println(bufferPool.get(recordNum));

                } else if (line.startsWith("S")) { // SET
                    int firstQuote = line.indexOf("\"");
                    int lastQuote = line.lastIndexOf("\"");

                    if (firstQuote == -1 || lastQuote == -1 || lastQuote <= firstQuote) {
                        throw new IllegalArgumentException("Record string must be wrapped in double quotes.");
                    }

                    int recordNum = Integer.parseInt(line.substring(4, firstQuote).trim());
                    String record = line.substring(firstQuote + 1, lastQuote);

                    // Pad or trim to exactly 40 characters
                    if (record.length() < 40) {
                        record = String.format("%-40s", record);
                    } else if (record.length() > 40) {
                        record = record.substring(0, 40);
                    }

                    System.out.println(bufferPool.set(recordNum, record));

                } else if (line.startsWith("P")) { // PIN
                    int blockNum = Integer.parseInt(line.substring(4).trim());
                    System.out.println(bufferPool.pin(blockNum));

                } else if (line.startsWith("U")) { // UNPIN
                    int blockNum = Integer.parseInt(line.substring(6).trim());
                    System.out.println(bufferPool.unpin(blockNum));

                } else if (line.startsWith("V")) { // VISUALIZE
                    bufferPool.visualize();

                } else {
                    System.out.println("Unknown command. Use: GET, SET, PIN, UNPIN, VISUALIZE, or E to exit.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            System.out.println("The program is ready for the next command");
        }

        System.out.println("Exiting program.");
    }
}
