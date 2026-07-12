import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.LinkedList;
import java.time.Duration;
import java.time.Instant;

public class Index {
    private Hashtable<Integer, LinkedList<RecordLocation>> hashIndex = new Hashtable<>();
    private LinkedList<RecordLocation>[] arrayIndex = new LinkedList[5000];

    // Helper method to construct a hash index on the first dataset provided in the command
    // Utilized for completing tasks in sections 2 and 4
    private int[] buildHashIndex(String dataset) throws IOException {
        // Loop through each file to iteratively build hash and array indexes for each unique key 'randomV'
        // Initialize a LinkedList for every unique key and append all corresponding locations for duplicates
        String fileContent;
        String fileNum;
        int i =0;
        int recordOffset = 0;
        int randomV = 0;
        // Build a hash index on the provided dataset
        for(i = 1; i < 100; i++){
            fileNum = Integer.toString(i);
            fileContent = Files.readAllLines(Paths.get("out/production/Project3/Project3Dataset/" +
                    "Project3Dataset-"+dataset+"/"+dataset+ fileNum +".txt"), StandardCharsets.UTF_8).get(0);
            // fileContent is set — now iterate through each record to build the hashTable index for the current file
            for (int j = 0; j < 100; j++){
                recordOffset = fileContent.indexOf(getRecordName(j+1))-4;
                randomV = Integer.parseInt(fileContent.substring(recordOffset+33, recordOffset+37));
                // current RandomV value is not yet present in the hash table
                if(!hashIndex.containsKey(randomV)){
                    LinkedList<RecordLocation> recordLocations =  new LinkedList<>();
                    RecordLocation newLocation = new RecordLocation(dataset, fileNum, recordOffset);
                    recordLocations.add(newLocation);
                    hashIndex.put(randomV, recordLocations);
                }else{
                    RecordLocation newLocation = new RecordLocation(dataset, fileNum, recordOffset);
                    hashIndex.get(randomV).add(newLocation);
                }
            }
        }
        int[] lengths = new int[500];
        for(i = 0; i < 500; i++){
            lengths[i] = hashIndex.get(i+1).size();
        }
        return lengths;
    }

    public void hashBasedJoin(String dataset1, String dataset2) throws IOException{
        // Start the timer to measure execution time
        Instant start = Instant.now();
        int[] originalSizes = buildHashIndex(dataset1);
        String fileContent;
        String fileNum;
        int i =0;
        int recordOffset = 0;
        int randomV = 0;
        // Each index will be set to 1 or 0, indicating whether a join operation has been performed in that bucket
        int[] trackJoins = new int[500];
        for(i = 1; i < 100; i++){
            fileNum = Integer.toString(i);
            fileContent = Files.readAllLines(Paths.get("out/production/Project3/Project3Dataset/" +
                    "Project3Dataset-"+dataset2+"/"+dataset2+ fileNum +".txt"), StandardCharsets.UTF_8).get(0);
            for (int j = 0; j < 100; j++){
                recordOffset = fileContent.indexOf(getRecordName(j+1))-4;
                randomV = Integer.parseInt(fileContent.substring(recordOffset+33, recordOffset+37));
                if(hashIndex.containsKey(randomV)){
                    RecordLocation newLocation = new RecordLocation(dataset2, fileNum, recordOffset);
                    hashIndex.get(randomV).add(newLocation);
                    trackJoins[randomV-1] = 1;
                }
            }
        }
        int newSize;
        System.out.println("A.Col1,        A.Col2,   B.Col1,        B.Col2");
        for(i = 0; i < 500; i++){
            if(trackJoins[i] == 1){
                LinkedList<RecordLocation> currentList = hashIndex.get(i+1);
                String fileContent1, fileContent2;
                String col1, col2, col3, col4;
                newSize = currentList.size();
                for(int j = 0; j < originalSizes[i]; j++){
                    fileContent1 = readRecord(currentList.get(j));
                    col1 = fileContent1.substring(0, 10);
                    col2 = fileContent1.substring(11, 18);
                    for(int k = newSize - (newSize-originalSizes[i]-1); k < newSize; k++){
                        fileContent2 = readRecord(currentList.get(k));
                        col3 = fileContent2.substring(0, 10);
                        col4 = fileContent2.substring(11, 18);
                        System.out.println(""+col1+"    "+col2+"    "+col3+"    "+col4);
                    }
                }
            }
        }
        // Stop the timer to mark the end of execution time measurement
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Time elapsed: "+Long.toString(timeElapsed)+" ms");
    }

    public void nestedLoopJoin() throws IOException {
        // Start the timer to begin measuring execution time
        Instant start = Instant.now();
        RecordLocation[] currentFileA = new RecordLocation[100];
        int recordOffset, randomVA, randomVB, count = 0;
        String fileNum, fileContent;
        System.out.println("Executing query please wait...");
        for(int i = 1; i < 100; i++) {
            System.out.println("Completed File A"+i);
            fileNum = Integer.toString(i);
            fileContent = Files.readAllLines(Paths.get("out/production/Project3/Project3Dataset/" +
                    "Project3Dataset-" + "A" + "/" + "A" + fileNum + ".txt"), StandardCharsets.UTF_8).get(0);
            // fileContent is set — now iterate over each record to build the hashTable index for the current file
            for (int j = 0; j < 100; j++) {
                recordOffset = fileContent.indexOf(getRecordName(j + 1)) - 4;
                randomVA = Integer.parseInt(fileContent.substring(recordOffset + 33, recordOffset + 37));
                RecordLocation newLocation = new RecordLocation("A", fileNum, recordOffset);
                currentFileA[j] = newLocation;
                for(int k = 1; k < 100; k++) {
                    fileNum = Integer.toString(k);
                    fileContent = Files.readAllLines(Paths.get("out/production/Project3/Project3Dataset/" +
                            "Project3Dataset-" + "B" + "/" + "B" + fileNum + ".txt"), StandardCharsets.UTF_8).get(0);
                    for (int v = 0; v < 100; v++) {
                        recordOffset = fileContent.indexOf(getRecordName(v + 1)) - 4;
                        randomVB = Integer.parseInt(fileContent.substring(recordOffset + 33, recordOffset + 37));
                        if(randomVA > randomVB){
                            count++;
                        }
                    }
                }

            }
        }
        System.out.println("count(*)");
        System.out.println(count);
        // End the timer to complete execution time measurement
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Time elapsed: "+Long.toString(timeElapsed)+" ms");
    }



    public void aggregationFunction(String function, String dataset) throws IOException {
        // Start the timer to begin timing the operation
        Instant start = Instant.now();
        buildHashIndex(dataset);
        System.out.println("Col2       "+function+"(RandomV)");
        for(int j = 1; j <= 100; j++){
            int count = 0;
            int sum = 0;
            for(int i = 1; i <= 500; i++){
                LinkedList<RecordLocation> currentList = hashIndex.get(i);
                for (RecordLocation recordLocation : currentList){
                    String recordContent = readRecord(recordLocation);
                    int col2 = Integer.parseInt(recordContent.substring(16, 19));
                    if(col2 == j){
                        sum = sum + i;
                        count++;
                    }
                }
            }
            String recordName = "Name"+getRecordName(j).substring(3);
            if(function.equals("SUM")){
                System.out.println(recordName+"    "+sum);
            }else{
                int avg = sum / count;
                System.out.println(recordName+"    "+avg);
            }
        }
        // Stop the timer to end timing the operation
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.println("Time elapsed: "+Long.toString(timeElapsed)+" ms");
    }



    // Helper method that takes an integer record number and builds a formatted string
    // Assists the calling function in indexing the correct substring of the file to locate the record position
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

    private int lastIndexOf(int randomV){
        int i = -1;
        for (int j = hashIndex.get(randomV).size()-1; j >= 0; j--){
            RecordLocation currentRecord = hashIndex.get(randomV).get(j);
            if(currentRecord.getDataset().equals("B")){
                i = j;
                break;
            }
        }
        return i;
    }

    private String readRecord(RecordLocation recordLocation) throws IOException {
        String fileContent = Files.readAllLines(Paths.get("out/production/Project3/Project3Dataset/" +
                "Project3Dataset-"+recordLocation.getDataset()+
                "/"+recordLocation.getDataset()+ recordLocation.getFileNum() +".txt"), StandardCharsets.UTF_8).get(0);
        fileContent = fileContent.substring(recordLocation.getRecordOffset(), recordLocation.getRecordOffset() + 40);
        return fileContent;
    }


}
