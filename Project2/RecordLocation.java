public class RecordLocation {
    private String fileNum;
    private Integer recordOffset;

    // Class Constructor
    public RecordLocation(String fileNum, Integer recordOffset) {
        this.fileNum = fileNum;
        this.recordOffset = recordOffset;
    }

    // Use the Paths API to fetch a record’s content, leveraging the combined hash table of files and record offsets
    public String toString(){
        return "File Number: "+fileNum + " and offset in file: " + recordOffset;
    }

    // Getter methods for the required class fields
    public String getFileNum() {
        return fileNum;
    }

    public Integer getRecordOffset() {
        return recordOffset;
    }

}
