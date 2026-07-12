import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Frame {
    private String content;
    private boolean dirty;
    private boolean pinned;
    private int blockId;

    // Constructor for the Frame class
    public Frame() {
        initialize();
    }

    // Helper method to initialize the Frame class fields
    public void initialize() {
        content = "";
        dirty = false;
        pinned = false;
        blockId = -1;
    }

    // Uses the full record number and its local position (1–100) to locate and return the corresponding record substring from the block
    public String getRecord(int recordNum, int shortRecordNum) {
        int startIndex = (shortRecordNum - 1) * 40;

        if (startIndex < 0 || startIndex + 40 > content.length()) {
            return "Error: Record position out of bounds.";
        }

        return content.substring(startIndex, startIndex + 40);
    }



    // Reads the content of a block file using the Path API. Each block file contains a single line.
    // Constructs the file path using the block number and reads the full content from the file.
    public void readBlock(int fileNum) throws IOException {
        String fileContent = Files.readAllLines(Paths.get("out/production/CS4432/Project1/F"+Integer.toString(fileNum)+".txt"), StandardCharsets.UTF_8).get(0);
        setBlockId(fileNum);
        setDirty(false);
        setContent(fileContent);
    }

    // Writes the current in-memory content to the corresponding block file on disk using the Path API.
    // Uses the content field of the Frame instance. Returns void.
    public void writeBlock() throws IOException {
        Files.write(Paths.get("out/production/CS4432/Project1/F"+Integer.toString(blockId)+".txt"), content.getBytes());
    }

    // All getter and setter methods for instance variables of the class
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }
}
