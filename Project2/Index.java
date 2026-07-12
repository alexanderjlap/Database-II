import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Hashtable;
import java.util.LinkedList;

public class Index {
    private Hashtable<Integer, LinkedList<RecordLocation>> hashIndex = new Hashtable<>();
    private LinkedList<RecordLocation>[] arrayIndex = new LinkedList[5000];


    // Initializes the hash-based and array-based indexes that track every record’s location
    // The RandomV field in the project dataset
    public void create() throws IOException{
    // Iterate through each file, building hash-based and array-based indexes for every unique RandomV key
    // Create a linked list for each value and append the locations of any duplicates
        String fileContent;
        String fileNum;
        for (int i = 1; i < 100; i++){
            fileNum = Integer.toString(i);
            fileContent = Files.readAllLines(Paths.get("out/production/CS4432/Project2Dataset/F"+ fileNum +".txt"), StandardCharsets.UTF_8).get(0);
            // fileContent is loaded; now iterate over each record to build the hash table index for this file
            buildHashIndex(fileNum, fileContent);
            buildArrayIndex(fileNum, fileContent);
        }
        System.out.println("The hash-based and array-based indexes have been built successfully!");

    }

    // Helper function for create(): build the hash index for each record in a file using its provided content
    private void buildHashIndex(String fileNum, String fileContent){
        int recordOffset = 0;
        int randomV = 0;
        for (int j = 0; j < 100; j++){
            recordOffset = fileContent.indexOf(getRecordName(j+1))-4;
            randomV = Integer.parseInt(fileContent.substring(recordOffset+33, recordOffset+37));
            // Current RandomV value isn't yet present in the hash table
            if(!hashIndex.containsKey(randomV)){
                LinkedList<RecordLocation> recordLocations =  new LinkedList<>();
                RecordLocation newLocation = new RecordLocation(fileNum, recordOffset);
                recordLocations.add(newLocation);
                hashIndex.put(randomV, recordLocations);
            }else{
                RecordLocation newLocation = new RecordLocation(fileNum, recordOffset);
                hashIndex.get(randomV).add(newLocation);
            }
        }
    }

    // Helper function for create(): build the array index for each record in a file using its provided content
    private void buildArrayIndex(String fileNum, String fileContent){
        int recordOffset = 0;
        int randomV = 0;
        for(int j = 0; j < 100; j++){
            recordOffset = fileContent.indexOf(getRecordName(j+1))-4;
            randomV = Integer.parseInt(fileContent.substring(recordOffset+33, recordOffset+37));
            if(arrayIndex[randomV-1] == null){
                LinkedList<RecordLocation> recordLocations =  new LinkedList<>();
                RecordLocation newLocation = new RecordLocation(fileNum, recordOffset);
                recordLocations.add(newLocation);
                arrayIndex[randomV-1] = recordLocations;
            }
            else{
                RecordLocation newLocation = new RecordLocation(fileNum, recordOffset);
                arrayIndex[randomV-1].add(newLocation);
            }
        }
    }

    // Helper method: generate a record-formatted string from an integer record number
    // Helps the caller locate a record’s position by indexing a substring of the file
    private String getRecordName(int recordNum){
        String recordName;
        if(recordNum < 10){
            recordName = "Rec00" + recordNum;
        }else if(recordNum < 100){
            recordName = "Rec0" + recordNum;
        }else{
            recordName = "Rec"+ recordNum;
        }
        return recordName;
    }

    // Fetch all records where RandomV equals the specified value v
    public String equalitySearch(int randomV) throws IOException {
        // Start Timer
        Instant start = Instant.now();
        if(hashIndex.isEmpty()){
            create();
            System.out.println("Full table Scan needed and has been performed!");
        }
        if (hashIndex.containsKey(randomV)){
            System.out.println("Using hash-based index to search for randomV = "+ Integer.toString(randomV));
            combineSearches(randomV, 0, 1);
        }
        else{
            System.out.println("There are no records for the provided RandomV value");
        }
        // End Timer
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        return "Time elapsed: "+Long.toString(timeElapsed)+" ms";
    }

    // Fetch all records whose RandomV value lies between V1 and V2 (inclusive)
    public String rangeSearch(int randomV1, int randomV2) throws IOException{
        Instant start = Instant.now();
        if(hashIndex.isEmpty()){
            create();
            System.out.println("Full table Scan needed and has been performed!");
        }
        // Iterate over each value from V1 to V2, honoring the specified range constraints
        if(randomV2 > randomV1){
            System.out.println("Using array-based index to search for v1 = "+ Integer.toString(randomV1) + " and v2 = "+
                    Integer.toString(randomV2));
            combineSearches(randomV1, randomV2, 2);
            // End Timer
            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();
            return "Time elapsed: "+Long.toString(timeElapsed)+" ms";
        }
        return "ERROR: Invalid range please ensure v2 is greater then v1";
    }

    // Retrieve all records whose RandomV value is not v, scanning without any indexes
    public String inequalitySearch(int randomV) throws IOException{
        Instant start = Instant.now();
        int fileCounter = 0;
        String fileContent;
        String fileNum;
        int recordOffset;
        int currentRandomV;
        for (int i = 1; i < 100; i++){
            fileNum = Integer.toString(i);
            fileContent = Files.readAllLines(Paths.get("out/production/CS4432/Project2Dataset/F"+ fileNum +".txt"), StandardCharsets.UTF_8).get(0);
            fileCounter++;
            for (int j = 0; j < 100; j++){
                recordOffset = fileContent.indexOf(getRecordName(j+1))-4;
                currentRandomV = Integer.parseInt(fileContent.substring(recordOffset+33, recordOffset+37));
                if (currentRandomV != randomV){
                    System.out.println(fileContent.substring(recordOffset, recordOffset+40));
                }
            }
        }
        // End Timer
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Read "+Integer.toString(fileCounter)+" file(s) (disk blocks)");
        return "Time elapsed: "+Long.toString(timeElapsed)+" ms";
    }

    // Build a temporary hash table keyed by RandomV to reduce I/O during equality and range searches
    // Associate each file number with a linked list of record offsets, ensuring only one I/O per file lookup
    // For each file, perform a single I/O and retrieve all required records for the query using fetchRecord
    private void combineSearches(int randomV1, int randomV2, int search) throws IOException {
        // For Hash-Based Index
        Hashtable<Integer, LinkedList<Integer>> filesAndOffsets = new Hashtable<>();
        if(search == 1){
            for (RecordLocation recordLocation : hashIndex.get(randomV1)) {
                if (!filesAndOffsets.containsKey(Integer.parseInt(recordLocation.getFileNum()))) {
                    LinkedList<Integer> tempList = new LinkedList<>();
                    tempList.add(recordLocation.getRecordOffset());
                    filesAndOffsets.put(Integer.parseInt(recordLocation.getFileNum()), tempList);
                } else {
                    filesAndOffsets.get(Integer.parseInt(recordLocation.getFileNum())).add(recordLocation.getRecordOffset());
                }
            }
        }
        else{
            for(int i = randomV1; i < randomV2-1; i++){
                if(arrayIndex[i] != null){
                    for (RecordLocation recordLocation : arrayIndex[i]) {
                        if (!filesAndOffsets.containsKey(Integer.parseInt(recordLocation.getFileNum()))) {
                            LinkedList<Integer> tempList = new LinkedList<>();
                            tempList.add(recordLocation.getRecordOffset());
                            filesAndOffsets.put(Integer.parseInt(recordLocation.getFileNum()), tempList);
                        } else {
                            filesAndOffsets.get(Integer.parseInt(recordLocation.getFileNum())).add(recordLocation.getRecordOffset());
                        }
                    }
                }
            }
        }
        fetchRecord(filesAndOffsets);
    }

    // For operations using the hash-based index
    // Use record offsets to fetch and print the required records with minimal I/O
    private void fetchRecord(Hashtable<Integer, LinkedList<Integer>> filesAndOffsets) throws IOException {
        filesAndOffsets.forEach((k, v) -> {
            try {
                String fileContent = Files.readAllLines(
                        Paths.get("out/production/CS4432/Project2Dataset/F"
                                + Integer.toString(k)+".txt"), StandardCharsets.UTF_8).get(0);
                for(Integer offset : v){
                    System.out.println(fileContent.substring(offset, offset + 40));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        System.out.println("Read "+Integer.toString(filesAndOffsets.size())+" file(s) (disk blocks)");
    }
}
