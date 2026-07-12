import java.io.IOException;
import java.lang.Math;

public class BufferPool {
    private Frame[] buffers;
    private int lastReplacedFrame;
    private int lastReplacedBlockID;

    // Initialization method for the BufferPool class
    // Sets up the buffer array and initializes each frame
    public void initialize(int size) {
        buffers = new Frame[size];
        lastReplacedFrame = -1;
        lastReplacedBlockID = -1;

        for (int i = 0; i < size; i++) {
            Frame newFrame = new Frame();
            newFrame.initialize();
            buffers[i] = newFrame;
        }
    }

    // GET method to retrieve the content of a specific record from memory or disk
    public String get(int recordNum) throws IOException {
        int fileNum = (int) Math.ceil((double) recordNum / 100);
        int shortRecordNum = recordNum - ((fileNum - 1) * 100);
        String fileContent = "The corresponding block #" + fileNum + " cannot be accessed from disk because the memory buffers are full";

        // Case 1: Block already in memory
        for (int x = 0; x < buffers.length; x++) {
            if (buffers[x].getBlockId() == fileNum) {
                fileContent = buffers[x].getRecord(recordNum, shortRecordNum);
                return fileContent + "; File " + fileNum + " already in memory; Located in Frame " + (x + 1);
            }
        }

        // Case 2: Empty frame available
        for (int x = 0; x < buffers.length; x++) {
            if (buffers[x].getBlockId() == -1) {
                buffers[x].readBlock(fileNum);
                fileContent = buffers[x].getRecord(recordNum, shortRecordNum);
                return fileContent + "; Brought file " + fileNum + " from disk; Placed in Frame " + (x + 1);
            }
        }

        // Case 3: All frames full — perform replacement
        int pinnedCounter = 0;
        for (int x = lastReplacedFrame; x < buffers.length + lastReplacedFrame; x++) {
            if (x == lastReplacedFrame) {
                pinnedCounter = 0;
                for (int i = 0; i < buffers.length; i++) {
                    if (buffers[i].isPinned()) {
                        pinnedCounter++;
                    }
                }
                if (pinnedCounter == buffers.length - 1 && !buffers[x].isPinned()) {
                    if (!buffers[(x % buffers.length)].isDirty()) {
                        lastReplacedFrame = x % buffers.length;
                        lastReplacedBlockID = buffers[(x % buffers.length)].getBlockId();
                        buffers[(x % buffers.length)].readBlock(fileNum);
                        fileContent = buffers[(x % buffers.length)].getRecord(recordNum, shortRecordNum);
                        return fileContent + "; Brought file " + fileNum + " from disk; Placed in Frame " + (lastReplacedFrame + 1) + "; Evicted file " + lastReplacedBlockID + " from Frame " + (lastReplacedFrame + 1);
                    } else {
                        lastReplacedFrame = x % buffers.length;
                        lastReplacedBlockID = buffers[(x % buffers.length)].getBlockId();
                        buffers[(x % buffers.length)].writeBlock();
                        buffers[(x % buffers.length)].readBlock(fileNum);
                        fileContent = buffers[(x % buffers.length)].getRecord(recordNum, shortRecordNum);
                        return fileContent + "; Brought file " + fileNum + " from disk; Placed in Frame " + (lastReplacedFrame + 1) + "; Evicted file " + lastReplacedBlockID + " from Frame " + (lastReplacedFrame + 1);
                    }
                } else {
                    continue;
                }
            }

            // Normal circular replacement
            if (!buffers[(x % buffers.length)].isPinned()) {
                if (!buffers[(x % buffers.length)].isDirty()) {
                    lastReplacedFrame = x % buffers.length;
                    lastReplacedBlockID = buffers[(x % buffers.length)].getBlockId();
                    buffers[(x % buffers.length)].readBlock(fileNum);
                    fileContent = buffers[(x % buffers.length)].getRecord(recordNum, shortRecordNum);
                    return fileContent + "; Brought file " + fileNum + " from disk; Placed in Frame " + (lastReplacedFrame + 1) +
                            "; Evicted file " + lastReplacedBlockID + " from Frame " + (lastReplacedFrame + 1);
                } else {
                    lastReplacedFrame = x % buffers.length;
                    lastReplacedBlockID = buffers[(x % buffers.length)].getBlockId();
                    buffers[(x % buffers.length)].writeBlock();
                    buffers[(x % buffers.length)].readBlock(fileNum);
                    fileContent = buffers[(x % buffers.length)].getRecord(recordNum, shortRecordNum);
                    return fileContent + "; Brought file " + fileNum + " from disk; Placed in Frame " + (lastReplacedFrame + 1) +
                            "; Evicted file " + lastReplacedBlockID + " from Frame " + (lastReplacedFrame + 1) + " and wrote back its new content";
                }
            }
        }

        // Case 4: No frames can be replaced
        return fileContent;
    }

    // SET method: updates the record content in the appropriate block
    public String set(int recordNum, String record) throws IOException {
        int fileNum = (int) Math.ceil((double) recordNum / 100);
        int shortRecordNum = recordNum - ((fileNum - 1) * 100);
        String oldRecord;

        // Case 1: Block is already in memory
        for (int x = 0; x < buffers.length; x++) {
            if (buffers[x].getBlockId() == fileNum) {
                oldRecord = buffers[x].getRecord(recordNum, shortRecordNum);
                buffers[x].setContent(buffers[x].getContent().replace(oldRecord, record));
                buffers[x].setDirty(true);
                return "Write was successful; File " + fileNum + " already in memory; Located in Frame " + (x + 1);
            }
        }

        // Case 2: Load block if not in memory and then write
        int oldReplacedBlockID = lastReplacedBlockID;
        get(recordNum);
        for (int x = 0; x < buffers.length; x++) {
            if (buffers[x].getBlockId() == fileNum) {
                oldRecord = buffers[x].getRecord(recordNum, shortRecordNum);
                buffers[x].setContent(buffers[x].getContent().replace(oldRecord, record));
                buffers[x].setDirty(true);
                if (oldReplacedBlockID != lastReplacedBlockID) {
                    return "Write was successful; Brought File " + fileNum + " from disk; Placed in Frame " + (x + 1) + "; Evicted file " + lastReplacedBlockID + " from Frame " + (x + 1);
                } else {
                    return "Write was successful; Brought File " + fileNum + " from disk; Placed in Frame " + (x + 1);
                }
            }
        }

        // Case 3: get() failed
        return "The corresponding block #" + fileNum + " cannot be accessed from disk because the memory buffers are full; Write was unsuccessful";
    }

    // PIN method: pins a block into memory; performs replacement if necessary
    public String pin(int blockNum) throws IOException {
        // Case 1: Block already in buffer pool
        for (int x = 0; x < buffers.length; x++) {
            if (buffers[x].getBlockId() == blockNum) {
                if (!buffers[x].isPinned()) {
                    buffers[x].setPinned(true);
                    return "File " + blockNum + " pinned in Frame " + (x + 1) + "; Not already pinned";
                } else {
                    return "File " + blockNum + " pinned in Frame " + (x + 1) + "; Already pinned";
                }
            }
        }

        // Case 2/3: Not in memory — perform circular replacement
        int pinnedCounter = 0;
        for (int x = lastReplacedFrame; x < buffers.length + lastReplacedFrame; x++) {
            if (x == lastReplacedFrame) {
                pinnedCounter = 0;
                for (int i = 0; i < buffers.length; i++) {
                    if (buffers[i].isPinned()) pinnedCounter++;
                }
                if (pinnedCounter == buffers.length - 1 && !buffers[x].isPinned()) {
                    Frame f = buffers[(x % buffers.length)];
                    if (f.isDirty()) f.writeBlock();
                    int evicted = f.getBlockId();
                    f.readBlock(blockNum);
                    f.setPinned(true);
                    lastReplacedFrame = x % buffers.length;
                    lastReplacedBlockID = evicted;
                    return "File " + blockNum + " is pinned in Frame " + (x % buffers.length + 1) + "; Frame " + (x % buffers.length + 1) + " was not already pinned; Evicted file " + evicted + " from frame " + (x % buffers.length + 1);
                } else {
                    continue;
                }
            }
            if (!buffers[x % buffers.length].isPinned()) {
                Frame f = buffers[x % buffers.length];
                if (f.isDirty()) f.writeBlock();
                int evicted = f.getBlockId();
                f.readBlock(blockNum);
                f.setPinned(true);
                lastReplacedFrame = x % buffers.length;
                lastReplacedBlockID = evicted;
                return "File " + blockNum + " pinned in Frame " + (x % buffers.length + 1) + "; Not already pinned; Evicted file " + evicted + " from frame " + (x % buffers.length + 1);
            }
        }

        // Case 4: All frames pinned
        return "The corresponding block " + blockNum + " cannot be pinned because the memory buffers are full";
    }

    // unpin() method: unpins a block that is currently in the buffer pool.
    // Returns the status of the operation (success or failure).
    public String unpin(int blockNum){
        // Case 1: Block is already present in the buffer pool
        for(int x = 0; x < buffers.length; x++) {
            if (buffers[x].getBlockId() == blockNum) {
                if(buffers[x].isPinned()){
                    buffers[x].setPinned(false);
                    return "File " + blockNum + " is unpinned in frame " + (x + 1) + "; Frame " + (x + 1) + " was not already unpinned";
                }else{
                    return "File " + blockNum + " in frame " + (x + 1) + " is unpinned; Frame was already unpinned";
                }
            }
        }
        // Case 2: Block is not present in the buffer pool
        return "The corresponding block " + blockNum + " cannot be unpinned because it is not in memory.";
    }

    // visualize() is a helper method that prints all metadata for each frame in the buffer pool.
    // Useful for inspecting internal state and debugging.
    public void visualize(){
        System.out.println("---Visualizing Buffer Pool---");
        for(int i = 0; i < buffers.length; i++){
            System.out.println("Frame: "+ Integer.toString(i+1));
            System.out.println("Content: "+ buffers[i].getContent());
            System.out.println("Dirty: "+ buffers[i].isDirty());
            System.out.println("Pinned: "+ buffers[i].isPinned());
            System.out.println("BlockID: "+ Integer.toString(buffers[i].getBlockId()));
        }
        System.out.println("-----------------------------");
    }

    // Getter and setter methods for BufferPool class variables
    public Frame[] getBuffers() {
        return buffers;
    }

    public void setBuffers(Frame[] buffers) {
        this.buffers = buffers;
    }
    public int getLastReplacedFrame() {
        return lastReplacedFrame;
    }

    public void setLastReplacedFrame(int lastReplacedFrame) {
        this.lastReplacedFrame = lastReplacedFrame;
    }

    public int getLastReplacedBlockID() {
        return lastReplacedBlockID;
    }

    public void setLastReplacedBlockID(int lastReplacedBlockID) {
        this.lastReplacedBlockID = lastReplacedBlockID;
    }
}
