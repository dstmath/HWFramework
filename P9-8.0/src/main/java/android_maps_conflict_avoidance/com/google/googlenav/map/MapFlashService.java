package android_maps_conflict_avoidance.com.google.googlenav.map;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.Log;
import android_maps_conflict_avoidance.com.google.common.io.IoUtil;
import android_maps_conflict_avoidance.com.google.common.io.PersistentStore;
import android_maps_conflict_avoidance.com.google.common.io.PersistentStore.PersistentStoreException;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

class MapFlashService implements MapTileStorage {
    private int catalogRecordBytes = 0;
    private boolean catalogUpdatedSinceLastWrite;
    private final Vector flashRecords = new Vector();
    private int highestRecordId = 0;
    private long lastChangedTime;
    private final MapService mapService;
    private int maxFlashSize;
    private int maxRecordBlocks;
    private boolean needsScavenge;
    private long nextPersistTime;
    private final String recordStoreBaseName;
    private final PersistentStore store = Config.getInstance().getPersistentStore();
    private int textSize = -1;
    private int tileEdition = -1;
    private final Hashtable tileToRecordMap = new Hashtable();

    MapFlashService(MapService mapService, String recordStoreBaseName, int maxFlashSize, int maxRecordStores) {
        this.mapService = mapService;
        this.recordStoreBaseName = recordStoreBaseName;
        this.maxFlashSize = maxFlashSize - 2000;
        long now = Config.getInstance().getClock().relativeTimeMillis();
        this.lastChangedTime = now;
        this.nextPersistTime = 2113 + now;
        this.maxRecordBlocks = maxRecordStores - 1;
        this.catalogUpdatedSinceLastWrite = true;
        readCatalog();
        this.needsScavenge = true;
    }

    int getNumBlocks() {
        return this.flashRecords.size();
    }

    String recordBlockName(int recordId) {
        return this.recordStoreBaseName + '_' + recordId;
    }

    String recordBlockName(FlashRecord flashRecord) {
        return recordBlockName(flashRecord.getRecordId());
    }

    public synchronized void close(boolean saveState) {
        if (saveState) {
            try {
                writeCache();
                writeCatalog();
            } catch (IOException e) {
                Log.logThrowable("FLASH", e);
            }
        }
        return;
    }

    private synchronized void readCatalog() {
        int formatVersion = 10;
        try {
            this.catalogUpdatedSinceLastWrite = true;
            byte[] directory = this.store.readBlock(this.recordStoreBaseName);
            if (directory != null) {
                DataInput is = IoUtil.createDataInputFromBytes(directory);
                formatVersion = is.readInt();
                if (formatVersion == 10) {
                    is.readBoolean();
                    this.tileEdition = is.readShort();
                    this.textSize = is.readShort();
                    int numEntries = is.readInt();
                    for (int entry = 0; entry < numEntries; entry++) {
                        addToFlashCatalog(FlashRecord.readFromCatalog(is));
                    }
                    this.catalogRecordBytes = directory.length;
                    this.catalogUpdatedSinceLastWrite = false;
                }
            }
        } catch (IOException e) {
            Log.logThrowable("FLASH", e);
        }
        if (this.catalogUpdatedSinceLastWrite) {
            eraseAll();
        }
        if (formatVersion != 10) {
            this.catalogUpdatedSinceLastWrite = true;
        }
        return;
    }

    synchronized boolean writeCatalog() throws IOException {
        boolean isOk = true;
        if (!this.catalogUpdatedSinceLastWrite) {
            return true;
        }
        int numEntries = this.flashRecords.size();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(10);
        dos.writeBoolean(false);
        dos.writeShort(this.tileEdition);
        dos.writeShort(this.textSize);
        dos.writeInt(numEntries);
        for (int entry = 0; entry < numEntries; entry++) {
            getFlashRecord(entry).writeToCatalog(dos);
        }
        baos.close();
        byte[] directory = baos.toByteArray();
        try {
            this.store.writeBlockX(directory, this.recordStoreBaseName);
        } catch (PersistentStoreException e) {
            handlePersistentStoreWriteException(e, true);
            isOk = false;
        }
        this.catalogRecordBytes = directory.length;
        this.catalogUpdatedSinceLastWrite = false;
        return isOk;
    }

    private void handlePersistentStoreWriteException(PersistentStoreException e, boolean catalog) {
        int curFlashSize = getSize();
        int curNumRecordBlocks = getNumBlocks();
        Log.logQuietThrowable("FLASH " + curFlashSize + "B " + curNumRecordBlocks + "R" + (!catalog ? "" : " catalog"), e);
        if (e.getType() == -2) {
            if (canCreateAnEmptyRecordStore()) {
                this.maxFlashSize = curFlashSize - 1000;
            } else {
                this.maxRecordBlocks = curNumRecordBlocks;
            }
        }
    }

    public synchronized void eraseAll() {
        this.tileToRecordMap.clear();
        this.flashRecords.removeAllElements();
        this.catalogRecordBytes = 0;
        this.highestRecordId = 0;
        this.catalogUpdatedSinceLastWrite = false;
        this.store.deleteAllBlocks(this.recordStoreBaseName);
    }

    synchronized boolean scavengeCatalog() {
        boolean wasOk;
        int i;
        wasOk = true;
        String[] rsNames = this.store.listBlocks(this.recordStoreBaseName);
        for (i = getNumBlocks() - 1; i >= 0; i--) {
            FlashRecord flashRecord = (FlashRecord) this.flashRecords.elementAt(i);
            if (!removeNameFromArray(recordBlockName(flashRecord), rsNames)) {
                wasOk = false;
                removeFromFlashCatalog(flashRecord, i);
            }
        }
        boolean catalogInFlash = false;
        if (rsNames != null) {
            catalogInFlash = removeNameFromArray(this.recordStoreBaseName, rsNames);
            for (String rsName : rsNames) {
                if (rsName != null) {
                    wasOk = false;
                    this.store.deleteBlock(rsName);
                }
            }
        }
        if (getNumBlocks() > 0 && !catalogInFlash) {
            wasOk = false;
        }
        return wasOk;
    }

    private static boolean removeNameFromArray(String name, String[] array) {
        if (array == null) {
            return false;
        }
        for (int i = 0; i < array.length; i++) {
            if (name.equals(array[i])) {
                array[i] = null;
                return true;
            }
        }
        return false;
    }

    private int findRecordIndexByID(int recordID) {
        int numEntries = this.flashRecords.size();
        for (int i = 0; i < numEntries; i++) {
            if (((FlashRecord) this.flashRecords.elementAt(i)).getRecordId() == recordID) {
                return i;
            }
        }
        return -1;
    }

    private FlashRecord getFlashRecord(int index) {
        return (FlashRecord) this.flashRecords.elementAt(index);
    }

    /* JADX WARNING: Missing block: B:120:0x02d6, code:
            if (r24 < r44.maxRecordBlocks) goto L_0x01fe;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int doPersist(Hashtable mapCache) throws IOException {
        int bytesAvail;
        int maxDataSize;
        int[] indicesToFree = new int[]{-1, -1};
        FlashRecord[] recordsToFree = new FlashRecord[]{null, null};
        long startTime = Config.getInstance().getClock().currentTimeMillis();
        synchronized (this) {
            if (this.needsScavenge) {
                scavengeCatalog();
                this.needsScavenge = false;
            }
            bytesAvail = this.maxFlashSize - getSize();
            maxDataSize = bytesAvail;
            if (bytesAvail >= 72000) {
                if (getNumBlocks() < this.maxRecordBlocks) {
                    maxDataSize = 72000;
                }
            }
            int worstIndex = -1;
            int secondWorstIndex = -1;
            long worstScore = Long.MIN_VALUE;
            long secondWorstScore = Long.MIN_VALUE;
            int numEntries = getNumBlocks();
            for (int index = 0; index < numEntries; index++) {
                long score = getFlashRecord(index).getScore(startTime);
                if (secondWorstIndex != -1) {
                    if ((score <= secondWorstScore ? 1 : null) != null) {
                    }
                }
                if (worstIndex != -1) {
                    if ((score <= worstScore ? 1 : null) != null) {
                        secondWorstIndex = index;
                        secondWorstScore = score;
                    }
                }
                secondWorstIndex = worstIndex;
                secondWorstScore = worstScore;
                worstIndex = index;
                worstScore = score;
            }
            if (worstIndex != -1) {
                indicesToFree[0] = worstIndex;
                recordsToFree[0] = getFlashRecord(worstIndex);
                maxDataSize += recordsToFree[0].getDataSize();
            }
            if (maxDataSize < 72000 && secondWorstIndex != -1) {
                indicesToFree[1] = secondWorstIndex;
                recordsToFree[1] = getFlashRecord(secondWorstIndex);
                maxDataSize += recordsToFree[1].getDataSize();
            }
            maxDataSize = Math.min(maxDataSize, 72000);
        }
        if (maxDataSize >= 6000) {
            FlashRecord newRecord;
            int status;
            Hashtable hashtable = mapCache;
            synchronized (mapCache) {
                try {
                    this.mapService.setMapCacheLocked(true);
                    newRecord = fillNewRecord(mapCache, maxDataSize);
                    this.mapService.setMapCacheLocked(false);
                } catch (Throwable th) {
                    this.mapService.setMapCacheLocked(false);
                }
            }
            int newRecordSize = newRecord.getDataSize();
            if (newRecordSize < 6000) {
                status = 0;
            } else {
                int id = -1;
                synchronized (this) {
                    bytesAvail = this.maxFlashSize - getSize();
                    boolean[] shouldFree = new boolean[]{false, false};
                    int displaceBytes = 0;
                    int i = 0;
                    while (i < 2) {
                        if (recordsToFree[i] != null && recordsToFree[i].isSaved()) {
                            if ((recordsToFree[i].getScore(startTime) <= newRecord.getScore(startTime) ? 1 : null) == null) {
                                shouldFree[i] = true;
                                displaceBytes += recordsToFree[i].getDataSize();
                            }
                        }
                        if (newRecordSize <= bytesAvail + displaceBytes) {
                            break;
                        }
                        i++;
                    }
                    int numBlocks = getNumBlocks();
                    if (newRecordSize <= bytesAvail + displaceBytes) {
                        if (shouldFree[0]) {
                            if (newRecordSize <= bytesAvail) {
                            }
                            status = 4;
                            id = recordsToFree[0].getRecordId();
                            removeFromFlashCatalog(recordsToFree[0], indicesToFree[0]);
                            if (shouldFree[1]) {
                                if (indicesToFree[0] < indicesToFree[1]) {
                                    indicesToFree[1] = indicesToFree[1] - 1;
                                }
                                this.store.deleteBlock(recordBlockName(recordsToFree[1]));
                                removeFromFlashCatalog(recordsToFree[1], indicesToFree[1]);
                            }
                        }
                        if (numBlocks >= this.maxRecordBlocks) {
                            status = 5;
                        } else {
                            status = 3;
                            int id2 = this.highestRecordId + 1;
                            this.highestRecordId = id2;
                            id = id2;
                        }
                    } else {
                        status = 2;
                    }
                }
                if (id >= 0) {
                    byte[] newRecordData = newRecord.createDataEntry(mapCache);
                    if (newRecordData != null) {
                        persistRecord(newRecord, newRecordData, id);
                    }
                }
            }
            writeCatalog();
            return status;
        }
        if (recordsToFree[0] != null) {
            this.store.deleteBlock(recordBlockName(recordsToFree[0]));
            removeFromFlashCatalog(recordsToFree[0], indicesToFree[0]);
        }
        return 1;
    }

    private synchronized void persistRecord(FlashRecord newRecord, byte[] newRecordData, int recordId) {
        if (this.catalogRecordBytes == 0) {
            this.store.writeBlock(new byte[0], this.recordStoreBaseName);
        }
        try {
            newRecord.writeRecord(recordBlockName(recordId), recordId, newRecordData);
            addToFlashCatalog(newRecord);
        } catch (PersistentStoreException e) {
            handlePersistentStoreWriteException(e, false);
        } catch (IllegalStateException e2) {
            Log.logThrowable("FLASH", e2);
        }
    }

    private boolean canCreateAnEmptyRecordStore() {
        String rsName = this.recordStoreBaseName + "_Test";
        try {
            this.store.writeBlockX(new byte[0], rsName);
            this.store.deleteBlock(rsName);
            return true;
        } catch (PersistentStoreException e) {
            return false;
        }
    }

    private FlashRecord fillNewRecord(Hashtable mapCache, int maxDataSize) {
        FlashRecord newRecord = new FlashRecord();
        int newDataSize = 1;
        Tile[] sortedMemoryTiles = this.mapService.getSortedCacheList();
        for (int tileIndex = sortedMemoryTiles.length - 1; tileIndex >= 0; tileIndex--) {
            Tile tile = sortedMemoryTiles[tileIndex];
            if (this.tileToRecordMap.get(tile) == null) {
                MapTile mapTile = (MapTile) mapCache.get(tile);
                if (mapTile.isComplete()) {
                    FlashEntry newEntry = new FlashEntry(mapTile);
                    int entrySize = newEntry.getByteSize();
                    if (newDataSize + entrySize <= maxDataSize && newRecord.addEntry(newEntry)) {
                        newDataSize += entrySize;
                    }
                }
            }
        }
        return newRecord;
    }

    private void addToFlashCatalog(FlashRecord newRecord) {
        int numEntries = newRecord.numEntries();
        this.catalogUpdatedSinceLastWrite = true;
        this.highestRecordId = Math.max(this.highestRecordId, newRecord.getRecordId());
        this.flashRecords.addElement(newRecord);
        for (int i = 0; i < numEntries; i++) {
            this.tileToRecordMap.put(newRecord.getEntry(i).getTile(), newRecord);
        }
    }

    private void removeFromFlashCatalog(FlashRecord flashRecord, int elementIndex) {
        if (flashRecord.isSaved()) {
            int numEntries = flashRecord.numEntries();
            this.catalogUpdatedSinceLastWrite = true;
            flashRecord.setUnsaved();
            this.flashRecords.removeElementAt(elementIndex);
            for (int i = 0; i < numEntries; i++) {
                this.tileToRecordMap.remove(flashRecord.getEntry(i).getTile());
            }
        }
    }

    private FlashEntry getFlashEntry(Tile location) {
        FlashRecord record = (FlashRecord) this.tileToRecordMap.get(location);
        if (record != null) {
            return record.getEntry(location);
        }
        return null;
    }

    synchronized int getFlashRecordsSize() {
        int size;
        size = 0;
        for (int index = 0; index < this.flashRecords.size(); index++) {
            size += getFlashRecord(index).getDataSize();
        }
        return size;
    }

    public synchronized int getSize() {
        return this.catalogRecordBytes + getFlashRecordsSize();
    }

    public MapTile getMapTile(Tile tile) {
        MapTile mapTile = null;
        FlashEntry flashEntry = getFlashEntry(tile);
        if (flashEntry != null) {
            mapTile = loadFlashRecordTile(flashEntry.getFlashRecord(), tile);
            if (mapTile != null) {
                flashEntry.setLastAccessTime(Config.getInstance().getClock().currentTimeMillis());
            }
        }
        return mapTile;
    }

    public void mapChanged() {
        this.lastChangedTime = Config.getInstance().getClock().relativeTimeMillis();
    }

    public boolean writeCache() throws IOException {
        long startTime = Config.getInstance().getClock().relativeTimeMillis();
        Hashtable mapCache = this.mapService.getMapCache();
        if ((this.nextPersistTime >= startTime ? 1 : null) == null) {
            if ((this.lastChangedTime + 1500 >= startTime ? 1 : null) == null) {
                try {
                    int status = doPersist(mapCache);
                    boolean cachingStillActive = status == 3 || status == 4;
                    this.nextPersistTime = Config.getInstance().getClock().relativeTimeMillis() + 2113;
                    return cachingStillActive;
                } catch (Throwable th) {
                    this.nextPersistTime = Config.getInstance().getClock().relativeTimeMillis() + 2113;
                }
            }
        }
        return true;
    }

    private MapTile loadFlashRecordTile(FlashRecord flashRecord, Tile desiredTile) {
        MapTile mapTile = flashRecord.loadTile(recordBlockName(flashRecord), desiredTile);
        if (mapTile == null) {
            synchronized (this) {
                int recordId = flashRecord.getRecordId();
                removeFromFlashCatalog(flashRecord, findRecordIndexByID(recordId));
                this.store.deleteBlock(recordBlockName(recordId));
            }
        }
        return mapTile;
    }

    public boolean setTileEditionAndTextSize(int newTileEdition, int newTextSize) {
        boolean changed = false;
        if (!((newTileEdition == this.tileEdition || this.tileEdition == -1) && (newTextSize == this.textSize || this.textSize == -1))) {
            changed = true;
        }
        this.tileEdition = newTileEdition;
        this.textSize = newTextSize;
        if (changed) {
            eraseAll();
            this.catalogUpdatedSinceLastWrite = true;
        }
        return changed;
    }
}
