package android_maps_conflict_avoidance.com.google.googlenav.map;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.Log;
import android_maps_conflict_avoidance.com.google.common.io.IoUtil;
import android_maps_conflict_avoidance.com.google.common.io.PersistentStore.PersistentStoreException;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

class FlashRecord {
    private static final Object lastCacheDataLock = new Object();
    private static byte[] lastCachedData = null;
    private static FlashRecord lastFlashRecord = null;
    private final Vector flashEntries = new Vector();
    private int recordId = -1;
    private boolean unverified = false;

    public boolean addEntry(FlashEntry entry) {
        if (numEntries() >= 255 || this.recordId != -1) {
            return false;
        }
        this.flashEntries.addElement(entry);
        entry.setFlashRecord(this);
        return true;
    }

    public FlashEntry getEntry(Tile location) {
        for (int i = 0; i < this.flashEntries.size(); i++) {
            FlashEntry entry = getEntry(i);
            if (entry.getTile().equals(location)) {
                return entry;
            }
        }
        return null;
    }

    public FlashEntry getEntry(int index) {
        return (FlashEntry) this.flashEntries.elementAt(index);
    }

    public int numEntries() {
        return this.flashEntries.size();
    }

    public int getDataSize() {
        int size = 1;
        for (int i = 0; i < this.flashEntries.size(); i++) {
            size += getEntry(i).getByteSize();
        }
        return size;
    }

    public int getRecordId() {
        return this.recordId;
    }

    boolean isSaved() {
        return this.recordId != -1;
    }

    void setUnsaved() {
        this.recordId = -1;
    }

    public long getScore(long currentTime) {
        int numEntries = this.flashEntries.size();
        long scores = 0;
        if (numEntries <= 0) {
            return Long.MAX_VALUE;
        }
        for (int i = 0; i < numEntries; i++) {
            FlashEntry entry = getEntry(i);
            scores += MapService.getScore(entry.getTile(), currentTime, entry.getLastAccessTime());
        }
        return scores / ((long) numEntries);
    }

    public String toString() {
        return super.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlashRecord)) {
            return false;
        }
        FlashRecord flashRecord = (FlashRecord) o;
        if (this.recordId != flashRecord.recordId) {
            return false;
        }
        int numEntries = numEntries();
        if (numEntries != flashRecord.numEntries()) {
            return false;
        }
        Vector otherEntries = flashRecord.flashEntries;
        for (int i = 0; i < numEntries; i++) {
            if (!((FlashEntry) this.flashEntries.elementAt(i)).equals(otherEntries.elementAt(i))) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return this.recordId;
    }

    public static FlashRecord readFromCatalog(DataInput is) throws IOException {
        int entries = is.readUnsignedByte();
        int recordIndex = is.readInt();
        FlashRecord flashRecord = new FlashRecord();
        int entry = 0;
        while (entry < entries) {
            if (flashRecord.addEntry(FlashEntry.readFromCatalog(is))) {
                entry++;
            } else {
                throw new IOException("FlashRecord full");
            }
        }
        flashRecord.unverified = true;
        flashRecord.recordId = recordIndex;
        return flashRecord;
    }

    public void writeToCatalog(DataOutput dos) throws IOException {
        if (this.recordId != -1) {
            int numEntries = numEntries();
            dos.writeByte(numEntries);
            dos.writeInt(this.recordId);
            for (int index = 0; index < numEntries; index++) {
                getEntry(index).writeToCatalog(dos);
            }
            return;
        }
        throw new IllegalStateException("Can't write unsaved FlashRecord");
    }

    private MapTile loadTileFromDataEntry(byte[] recordBlock, Tile desiredTile) {
        MapTile mapTile = null;
        if (recordBlock == null || recordBlock.length == 0) {
            return null;
        }
        try {
            DataInput dis = IoUtil.createDataInputFromBytes(recordBlock);
            int entries = dis.readUnsignedByte();
            if (entries != numEntries()) {
                return null;
            }
            int i = 0;
            while (i < entries) {
                MapTile currentTile = MapTile.read(dis);
                if (this.unverified && !new FlashEntry(currentTile).equals(this.flashEntries.elementAt(i))) {
                    return null;
                }
                if (currentTile.getLocation().equals(desiredTile)) {
                    mapTile = currentTile;
                    if (!this.unverified) {
                        break;
                    }
                }
                i++;
            }
            this.unverified = false;
            return mapTile;
        } catch (IOException e) {
            Log.logQuietThrowable("FLASH", e);
        }
    }

    byte[] createDataEntry(Hashtable tileMap) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(72000);
        DataOutputStream dos = new DataOutputStream(baos);
        int numEntries = numEntries();
        dos.writeByte(numEntries);
        for (int i = 0; i < numEntries; i++) {
            MapTile mapTile = (MapTile) tileMap.get(getEntry(i).getTile());
            if (mapTile == null) {
                return null;
            }
            mapTile.write(dos);
        }
        return baos.toByteArray();
    }

    public MapTile loadTile(String blockName, Tile desiredTile) {
        if (this.recordId == -1) {
            return null;
        }
        byte[] recordBlock = null;
        synchronized (lastCacheDataLock) {
            if (equals(lastFlashRecord)) {
                recordBlock = lastCachedData;
            }
        }
        if (recordBlock == null) {
            recordBlock = Config.getInstance().getPersistentStore().readBlock(blockName);
            if (!(recordBlock == null || recordBlock.length == 0)) {
                synchronized (lastCacheDataLock) {
                    lastCachedData = recordBlock;
                    lastFlashRecord = this;
                }
            }
        }
        if (recordBlock != null) {
            return loadTileFromDataEntry(recordBlock, desiredTile);
        }
        return null;
    }

    void writeRecord(String blockName, int recordId, byte[] recordData) throws PersistentStoreException, IllegalStateException {
        if (this.recordId == -1) {
            Config.getInstance().getPersistentStore().writeBlockX(recordData, blockName);
            this.recordId = recordId;
            return;
        }
        throw new IllegalStateException("already saved");
    }

    static void clearDataCache() {
        synchronized (lastCacheDataLock) {
            lastCachedData = null;
            lastFlashRecord = null;
        }
    }
}
