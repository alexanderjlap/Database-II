public class RecordLocation {
    private String dataset;
    private String fileNum;
    private Integer recordOffset;

    // Class constructor to initialize the object
    public RecordLocation(String dataset, String fileNum, Integer recordOffset) {
        this.dataset = dataset;
        this.fileNum = fileNum;
        this.recordOffset = recordOffset;
    }

    // Class getters and setters for accessing and modifying private fields
    public String getFileNum() {
        return fileNum;
    }

    public Integer getRecordOffset() {
        return recordOffset;
    }

    public String getDataset() { return dataset; }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public void setFileNum(String fileNum) {
        this.fileNum = fileNum;
    }

    public void setRecordOffset(Integer recordOffset) {
        this.recordOffset = recordOffset;
    }
}
