package android_maps_conflict_avoidance.com.google.googlenav.map;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.Log;
import android_maps_conflict_avoidance.com.google.common.OutOfMemoryHandler;
import android_maps_conflict_avoidance.com.google.common.StaticUtil;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;
import android_maps_conflict_avoidance.com.google.common.ui.RepaintListener;
import android_maps_conflict_avoidance.com.google.common.util.ArrayUtil;
import android_maps_conflict_avoidance.com.google.googlenav.StartupHelper;
import android_maps_conflict_avoidance.com.google.googlenav.datarequest.DataRequestDispatcher;
import android_maps_conflict_avoidance.com.google.googlenav.map.LayerService.TileUpdateObserver;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class MapService implements OutOfMemoryHandler, Runnable {
    private final boolean autoConfigCache;
    private MapTileRequest currentRequest;
    volatile boolean exitWorkThread;
    final MapTileStorage flashService;
    private final Object indefiniteThreadLockObject;
    private long lastMapMoveTime;
    private final Vector layerImageTiles;
    private final Vector layerServices;
    final Hashtable mapCache;
    private volatile boolean mapCacheLocked;
    private int maxCacheDataSize;
    private TileUpdateObserver observer;
    private long outOfMemoryTime;
    private final Vector repaintListeners;
    private int requestType;
    private int requestsOutstanding;
    private int targetCacheDataSize;
    private final Hashtable tempScaledImages;
    private final Object timedThreadLockObject;

    private class MapTileRequest extends BaseTileRequest {
        private boolean closed;
        private boolean isForeground;
        private Vector tilePriorityList;
        private Vector tileSchedule;

        MapTileRequest(byte flags) {
            super(MapService.this.requestType, flags);
            this.tileSchedule = new Vector();
            this.tilePriorityList = new Vector();
            this.isForeground = true;
            this.closed = false;
        }

        synchronized void requestTile(MapTile mapTile, int priority) {
            if (this.closed) {
                throw new RuntimeException("Adding tiles to closed request!");
            } else if (this.tileSchedule.indexOf(mapTile) == -1) {
                int i = this.tileSchedule.size();
                while (i > 0) {
                    if (priority >= ((Integer) this.tilePriorityList.elementAt(i - 1)).intValue()) {
                        this.tileSchedule.insertElementAt(mapTile, i);
                        this.tilePriorityList.insertElementAt(new Integer(priority), i);
                        break;
                    }
                    i--;
                }
                if (i == 0) {
                    this.tileSchedule.insertElementAt(mapTile, 0);
                    this.tilePriorityList.insertElementAt(new Integer(priority), 0);
                }
                return;
            } else {
                return;
            }
        }

        public boolean isForeground() {
            return this.isForeground;
        }

        public void writeRequestData(DataOutput dos) throws IOException {
            MapService.this.requestsOutstanding = MapService.this.requestsOutstanding + 1;
            synchronized (this) {
                this.closed = true;
            }
            this.tilePriorityList = null;
            Tile[] tileList = new Tile[this.tileSchedule.size()];
            for (int i = 0; i < this.tileSchedule.size(); i++) {
                tileList[i] = ((MapTile) this.tileSchedule.elementAt(i)).getLocation();
            }
            writeRequestForTiles(tileList, dos);
        }

        public boolean readResponseData(DataInput dis) throws IOException {
            MapService.this.requestsOutstanding = MapService.this.requestsOutstanding - 1;
            super.readResponseData(dis);
            if (this.tileSchedule.size() != 0) {
                return false;
            }
            return true;
        }

        protected void setTileEditionAndTextSize(int tileEdition, int textSize) {
            MapService.this.setTileEditionAndTextSize(tileEdition, textSize);
        }

        protected void handleEndOfResponse(int tileIndex) {
            Vector skippedTiles = new Vector();
            ArrayUtil.copyIntoVector(this.tileSchedule, tileIndex, skippedTiles);
            this.tileSchedule = skippedTiles;
            MapService.this.tempScaledImages.clear();
        }

        protected boolean processDownloadedTile(int tileIndex, Tile location, byte[] imageBytes) {
            MapTile mapTile = (MapTile) this.tileSchedule.elementAt(tileIndex);
            if (mapTile != null) {
                if (!mapTile.getLocation().equals(location)) {
                    return true;
                }
                mapTile.setData(imageBytes);
                mapTile.setLastAccessTime(mapTile.getLastAccessTime() - ((long) tileIndex));
                for (int i = 0; i < MapService.this.repaintListeners.size(); i++) {
                    ((RepaintListener) MapService.this.repaintListeners.elementAt(i)).repaint();
                }
            }
            return false;
        }
    }

    android_maps_conflict_avoidance.com.google.googlenav.map.MapTile getTile(android_maps_conflict_avoidance.com.google.googlenav.map.Tile r10, int r11, boolean r12, int r13, long r14) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:55:0x00af
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:248)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r9 = this;
        r3 = 0;
        r6 = 1;
        r4 = r9.mapCache;
        r0 = r4.get(r10);
        r0 = (android_maps_conflict_avoidance.com.google.googlenav.map.MapTile) r0;
        r4 = -9223372036854775808;
        r4 = (r14 > r4 ? 1 : (r14 == r4 ? 0 : -1));
        if (r4 != 0) goto L_0x001c;
    L_0x0010:
        r4 = android_maps_conflict_avoidance.com.google.common.Config.getInstance();
        r4 = r4.getClock();
        r14 = r4.currentTimeMillis();
    L_0x001c:
        if (r0 == 0) goto L_0x0028;
    L_0x001e:
        r3 = r0.isComplete();
        if (r3 == 0) goto L_0x0091;
    L_0x0024:
        r0.setLastAccessTime(r14);
        return r0;
    L_0x0028:
        r4 = r9.mapCacheLocked;
        if (r4 == 0) goto L_0x0034;
    L_0x002c:
        r0 = new android_maps_conflict_avoidance.com.google.googlenav.map.MapTile;
        r3 = (android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage) r3;
        r0.<init>(r10, r3, r6);
        goto L_0x0024;
    L_0x0034:
        r4 = r9.mapCache;
        monitor-enter(r4);
        r3 = 1;
        r9.setMapCacheLocked(r3);	 Catch:{ all -> 0x0055 }
        r3 = r9.flashService;	 Catch:{ all -> 0x008b }
        r0 = r3.getMapTile(r10);	 Catch:{ all -> 0x008b }
        if (r0 == 0) goto L_0x0058;	 Catch:{ all -> 0x008b }
    L_0x0043:
        if (r12 == 0) goto L_0x0087;	 Catch:{ all -> 0x008b }
    L_0x0045:
        r9.addMapEntry(r0);	 Catch:{ all -> 0x008b }
        r3 = android_maps_conflict_avoidance.com.google.googlenav.Stats.getInstance();	 Catch:{ all -> 0x008b }
        r3.flashCacheHit();	 Catch:{ all -> 0x008b }
    L_0x004f:
        r3 = 0;
        r9.setMapCacheLocked(r3);	 Catch:{ all -> 0x0055 }
        monitor-exit(r4);	 Catch:{ all -> 0x0055 }
        goto L_0x0024;	 Catch:{ all -> 0x0055 }
    L_0x0055:
        r3 = move-exception;	 Catch:{ all -> 0x0055 }
    L_0x0056:
        monitor-exit(r4);	 Catch:{ all -> 0x0055 }
        throw r3;
    L_0x0058:
        r2 = r9.getTempImage(r10, r13);	 Catch:{ all -> 0x008b }
        if (r12 != 0) goto L_0x0069;	 Catch:{ all -> 0x008b }
    L_0x005e:
        r1 = new android_maps_conflict_avoidance.com.google.googlenav.map.MapTile;	 Catch:{ all -> 0x008b }
        r3 = 1;	 Catch:{ all -> 0x008b }
        r1.<init>(r10, r2, r3);	 Catch:{ all -> 0x008b }
        r9.addMapEntry(r1);	 Catch:{ all -> 0x00b2 }
        r0 = r1;
        goto L_0x004f;
    L_0x0069:
        r3 = android_maps_conflict_avoidance.com.google.googlenav.datarequest.DataRequestDispatcher.getInstance();	 Catch:{ all -> 0x008b }
        r3 = r3.canDispatchNow();	 Catch:{ all -> 0x008b }
        if (r3 == 0) goto L_0x005e;	 Catch:{ all -> 0x008b }
    L_0x0073:
        r1 = new android_maps_conflict_avoidance.com.google.googlenav.map.MapTile;	 Catch:{ all -> 0x008b }
        r1.<init>(r10, r2);	 Catch:{ all -> 0x008b }
        r9.queueTileRequest(r1, r11);	 Catch:{ all -> 0x00b2 }
        r9.addMapEntry(r1);	 Catch:{ all -> 0x00b2 }
        r3 = android_maps_conflict_avoidance.com.google.googlenav.Stats.getInstance();	 Catch:{ all -> 0x00b2 }
        r3.flashCacheMiss();	 Catch:{ all -> 0x00b2 }
        r0 = r1;
        goto L_0x004f;
    L_0x0087:
        r6 = 20000; // 0x4e20 float:2.8026E-41 double:9.8813E-320;
        r14 = r14 - r6;
        goto L_0x0045;
    L_0x008b:
        r3 = move-exception;
    L_0x008c:
        r5 = 0;
        r9.setMapCacheLocked(r5);	 Catch:{ all -> 0x0055 }
        throw r3;	 Catch:{ all -> 0x0055 }
    L_0x0091:
        r3 = r0.getRequested();
        if (r3 != 0) goto L_0x0024;
    L_0x0097:
        if (r12 == 0) goto L_0x0024;
    L_0x0099:
        r3 = android_maps_conflict_avoidance.com.google.googlenav.datarequest.DataRequestDispatcher.getInstance();
        r3 = r3.canDispatchNow();
        if (r3 == 0) goto L_0x0024;
    L_0x00a3:
        r9.queueTileRequest(r0, r11);
        r3 = android_maps_conflict_avoidance.com.google.googlenav.Stats.getInstance();
        r3.flashCacheMiss();
        goto L_0x0024;
        r3 = move-exception;
        r0 = r1;
        goto L_0x0056;
    L_0x00b2:
        r3 = move-exception;
        r0 = r1;
        goto L_0x008c;
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.googlenav.map.MapService.getTile(android_maps_conflict_avoidance.com.google.googlenav.map.Tile, int, boolean, int, long):android_maps_conflict_avoidance.com.google.googlenav.map.MapTile");
    }

    void setMapCacheLocked(boolean mapCacheLocked) {
        this.mapCacheLocked = mapCacheLocked;
    }

    MapService(int maxCacheDataSize, int targetCacheDataSize, int maxFlashSize, int maxRecordStores, String tileRecordStoreName) {
        this.layerServices = new Vector();
        this.layerImageTiles = new Vector();
        this.currentRequest = null;
        this.requestsOutstanding = 0;
        this.indefiniteThreadLockObject = new Object();
        this.timedThreadLockObject = new Object();
        this.exitWorkThread = true;
        this.repaintListeners = new Vector();
        this.outOfMemoryTime = Long.MIN_VALUE;
        this.requestType = 26;
        if (maxCacheDataSize != -1) {
            this.autoConfigCache = false;
            this.maxCacheDataSize = maxCacheDataSize;
            if (targetCacheDataSize != -1) {
                this.targetCacheDataSize = targetCacheDataSize;
            } else {
                setAutoTargetCacheSize();
            }
        } else {
            this.autoConfigCache = true;
            this.maxCacheDataSize = 25000;
            setAutoTargetCacheSize();
        }
        this.tempScaledImages = new Hashtable();
        this.mapCache = new Hashtable();
        this.mapCacheLocked = false;
        if (maxFlashSize <= 0) {
            this.flashService = new NullMapTileStorage();
        } else {
            this.flashService = new MapFlashService(this, tileRecordStoreName, maxFlashSize, maxRecordStores);
        }
        this.lastMapMoveTime = getRelativeTime();
        StartupHelper.addPostStartupBgCallback(new Runnable() {
            public void run() {
                MapService.this.startWorkThread();
            }
        });
        StaticUtil.registerOutOfMemoryHandler(this);
    }

    Hashtable getMapCache() {
        return this.mapCache;
    }

    private void setAutoTargetCacheSize() {
        this.targetCacheDataSize = (this.maxCacheDataSize * 4) / 5;
    }

    void close(boolean saveState) {
        StaticUtil.removeOutOfMemoryHandler(this);
        stopWorkThread();
        this.flashService.close(saveState);
        for (int i = this.layerServices.size() - 1; i >= 0; i--) {
            LayerService layerService = (LayerService) this.layerServices.elementAt(i);
            layerService.close();
            StaticUtil.removeOutOfMemoryHandler(layerService);
        }
        this.layerServices.removeAllElements();
    }

    public MapTile getTile(Tile tile, int priority, boolean loadTile, boolean scaleOk) {
        return getTile(tile, priority, loadTile, scaleOk, Long.MIN_VALUE);
    }

    MapTile getTile(Tile tile, int priority, boolean loadTile, boolean scaleOk, long accessTime) {
        int i = 0;
        if (scaleOk) {
            i = 2;
        }
        return getTile(tile, priority, loadTile, i, accessTime);
    }

    private GoogleImage getTempImage(Tile tile, int scaleMode) {
        switch (scaleMode) {
            case LayoutParams.MODE_MAP /*0*/:
                return null;
            case OverlayItem.ITEM_STATE_PRESSED_MASK /*1*/:
                return getScaledImageFromCache(tile);
            default:
                return getOrCreateScaledImage(tile);
        }
    }

    private GoogleImage getScaledImageFromCache(Tile tile) {
        return (GoogleImage) this.tempScaledImages.get(tile);
    }

    private GoogleImage getOrCreateScaledImage(Tile tile) {
        GoogleImage image = (GoogleImage) this.tempScaledImages.get(tile);
        if (image == null) {
            image = createScaledImage(tile);
            if (image != null) {
                this.tempScaledImages.put(tile, image);
            }
        }
        return image;
    }

    private GoogleImage createScaledImage(Tile tile) {
        Object obj = null;
        long currentTime = getRelativeTime();
        if (currentTime >= this.outOfMemoryTime + 10000) {
            obj = 1;
        }
        if (obj == null) {
            return null;
        }
        GoogleImage tempImage = null;
        try {
            Tile parent = tile.getZoomParent();
            if (parent != null) {
                int ratio = parent.getZoom().getZoomRatio(tile.getZoom());
                MapTile parentMapTile = getTile(parent, 0, false, false);
                if (ratio == 2 && parentMapTile.hasImage()) {
                    tempImage = createScaledImage(tile, parent, parentMapTile.getImage());
                }
            }
        } catch (OutOfMemoryError e) {
            clearScaledImages();
            this.outOfMemoryTime = currentTime;
            Log.logQuietThrowable("Map Service image scaling", e);
        }
        return tempImage;
    }

    private void clearScaledImages() {
        synchronized (this.mapCache) {
            this.mapCacheLocked = true;
            this.tempScaledImages.clear();
            Enumeration enumeration = this.mapCache.elements();
            while (enumeration.hasMoreElements()) {
                ((MapTile) enumeration.nextElement()).removeScaledImage();
            }
            this.mapCacheLocked = false;
        }
    }

    private GoogleImage createScaledImage(Tile tile, Tile parentTile, GoogleImage parentImage) {
        int xOffset;
        int yOffset;
        if (tile.getXIndex() != parentTile.getXIndex() * 2) {
            xOffset = 128;
        } else {
            xOffset = 0;
        }
        if (tile.getYIndex() != parentTile.getYIndex() * 2) {
            yOffset = 128;
        } else {
            yOffset = 0;
        }
        return parentImage.createScaledImage(xOffset, yOffset, 128, 128, 256, 256);
    }

    private void queueTileRequest(MapTile mapTile, int priority) {
        if (this.currentRequest == null) {
            this.currentRequest = new MapTileRequest(mapTile.getLocation().getFlags());
        }
        this.currentRequest.requestTile(mapTile, priority);
        mapTile.setRequested(true);
    }

    private void doCompact(boolean emergency) {
        long maxAge;
        if (emergency) {
            maxAge = 2000;
        } else {
            maxAge = 4000;
        }
        synchronized (this.mapCache) {
            setMapCacheLocked(true);
            try {
                long currentTime = Config.getInstance().getClock().currentTimeMillis();
                Enumeration keys = this.mapCache.keys();
                while (keys.hasMoreElements()) {
                    Tile tile = (Tile) keys.nextElement();
                    MapTile mapTile = (MapTile) this.mapCache.get(tile);
                    if ((mapTile.getLastAccessTime() + maxAge >= currentTime ? 1 : null) == null) {
                        mapTile.compact();
                        for (int i = this.layerServices.size() - 1; i >= 0; i--) {
                            ((LayerService) this.layerServices.elementAt(i)).doCompact(Tile.getTile((byte) 8, tile));
                        }
                    }
                }
                setMapCacheLocked(false);
            } catch (Throwable th) {
                setMapCacheLocked(false);
            }
        }
    }

    private void addMapEntry(MapTile mapTile) {
        this.mapCache.put(mapTile.getLocation(), mapTile);
    }

    void checkTrimCache() {
        int cacheSize = getCacheSize();
        if (cacheSize > this.maxCacheDataSize) {
            if (this.autoConfigCache) {
                System.gc();
                this.maxCacheDataSize = Math.max(25000, Math.min(((int) ((Runtime.getRuntime().freeMemory() + ((long) cacheSize)) - 40000)) / 2, ((int) Runtime.getRuntime().totalMemory()) / 3));
                setAutoTargetCacheSize();
                if (cacheSize < this.maxCacheDataSize) {
                    return;
                }
            }
            trimCache(cacheSize);
        }
    }

    private void trimCache(int cacheSize) {
        synchronized (this.mapCache) {
            try {
                this.mapCacheLocked = true;
                Tile[] sortedList = getSortedCacheList();
                for (Tile minKey : sortedList) {
                    if (cacheSize <= this.targetCacheDataSize) {
                        break;
                    }
                    MapTile mapTile = (MapTile) this.mapCache.get(minKey);
                    if (mapTile.isComplete() || !mapTile.getRequested()) {
                        this.mapCache.remove(minKey);
                        cacheSize -= mapTile.getDataSize();
                    }
                }
                this.mapCacheLocked = false;
            } catch (Throwable th) {
                this.mapCacheLocked = false;
            }
        }
    }

    int getCacheSize() {
        int cacheSize = 0;
        synchronized (this.mapCache) {
            Enumeration entries = this.mapCache.elements();
            while (entries.hasMoreElements()) {
                cacheSize += ((MapTile) entries.nextElement()).getDataSize();
            }
        }
        return cacheSize;
    }

    public int restoreBaseImagesIfNeeded() {
        int renderedImageCount;
        synchronized (this.mapCache) {
            try {
                setMapCacheLocked(true);
                renderedImageCount = getRenderedImageCount();
                if (renderedImageCount > 48) {
                    Tile[] sortedList = getSortedCacheList();
                    for (int i = 0; i < sortedList.length && renderedImageCount > 24; i++) {
                        MapTile mapTile = (MapTile) this.mapCache.get(sortedList[i]);
                        if (mapTile.hasRenderedImage()) {
                            mapTile.restoreBaseImage();
                            renderedImageCount--;
                        }
                    }
                }
                setMapCacheLocked(false);
            } catch (Throwable th) {
                setMapCacheLocked(false);
            }
        }
        return renderedImageCount;
    }

    int getRenderedImageCount() {
        int renderedImageCount = 0;
        Enumeration entries = this.mapCache.elements();
        while (entries.hasMoreElements()) {
            if (((MapTile) entries.nextElement()).hasRenderedImage()) {
                renderedImageCount++;
            }
        }
        return renderedImageCount;
    }

    static long getScore(Tile tile, long currentTime, long lastAccessTime) {
        return currentTime - lastAccessTime;
    }

    long getTileDate(Tile tile) {
        return ((MapTile) this.mapCache.get(tile)).getLastAccessTime();
    }

    Tile[] getSortedCacheList() {
        long startTime = Config.getInstance().getClock().currentTimeMillis();
        Tile[] list = new Tile[this.mapCache.size()];
        long[] scoreList = new long[this.mapCache.size()];
        int index = 0;
        Enumeration enumeration = this.mapCache.keys();
        while (enumeration.hasMoreElements()) {
            list[index] = (Tile) enumeration.nextElement();
            scoreList[index] = getScore(list[index], startTime, getTileDate(list[index]));
            index++;
        }
        sort(scoreList, list);
        return list;
    }

    private void swap(long[] scoreList, Tile[] list, int indexA, int indexB) {
        Tile tempTile = list[indexB];
        list[indexB] = list[indexA];
        list[indexA] = tempTile;
        long tempScore = scoreList[indexB];
        scoreList[indexB] = scoreList[indexA];
        scoreList[indexA] = tempScore;
    }

    private int partition(long[] scoreList, Tile[] list, int left, int right, int pivotIndex) {
        long pivotValue = scoreList[pivotIndex];
        swap(scoreList, list, pivotIndex, right);
        int i = left;
        int store = left;
        while (i < right) {
            int store2;
            if ((scoreList[i] < pivotValue ? 1 : null) == null) {
                store2 = store + 1;
                swap(scoreList, list, i, store);
            } else {
                store2 = store;
            }
            i++;
            store = store2;
        }
        if ((scoreList[right] <= scoreList[store] ? 1 : null) != null) {
            return right;
        }
        swap(scoreList, list, right, store);
        return store;
    }

    private void qsort(long[] scoreList, Tile[] list, int left, int right) {
        if (right > left) {
            int newPivot = partition(scoreList, list, left, right, left);
            qsort(scoreList, list, left, newPivot - 1);
            qsort(scoreList, list, newPivot + 1, right);
        }
    }

    private void sort(long[] scoreList, Tile[] list) {
        qsort(scoreList, list, 0, list.length - 1);
    }

    boolean requestTiles() {
        if (this.currentRequest == null) {
            return false;
        }
        MapTileRequest tempRequest = this.currentRequest;
        this.currentRequest = null;
        DataRequestDispatcher.getInstance().addDataRequest(tempRequest);
        return true;
    }

    void requestLayerTiles() {
        for (int i = this.layerServices.size() - 1; i >= 0; i--) {
            LayerService layerService = (LayerService) this.layerServices.elementAt(i);
            if (layerService.needFetchLayerTiles()) {
                layerService.requestTiles();
            }
        }
    }

    public Vector getLayerTiles(Tile tile, boolean fetch) {
        this.layerImageTiles.removeAllElements();
        for (int i = this.layerServices.size() - 1; i >= 0; i--) {
            LayerService layerService = (LayerService) this.layerServices.elementAt(i);
            if (layerService.needFetchLayerTiles()) {
                LayerTile layerTile = layerService.getTile(Tile.getTile((byte) 8, tile), fetch);
                if (layerTile != null && layerTile.hasImage()) {
                    this.layerImageTiles.addElement(layerTile.getImage());
                }
            }
        }
        return this.layerImageTiles;
    }

    public void notifyLayerTilesDirty() {
        if (this.observer != null) {
            this.observer.setLayerTilesDirty();
        }
        for (int i = this.layerServices.size() - 1; i >= 0; i--) {
            ((LayerService) this.layerServices.elementAt(i)).notifyLayerTilesDirty();
        }
    }

    void setTileEditionAndTextSize(int tileEdition, int textSize) {
        if (this.flashService.setTileEditionAndTextSize(tileEdition, textSize)) {
            synchronized (this.mapCache) {
                Enumeration mapTiles = this.mapCache.keys();
                Vector toRemove = new Vector();
                while (mapTiles.hasMoreElements()) {
                    Tile tile = (Tile) mapTiles.nextElement();
                    if (((MapTile) this.mapCache.get(tile)).isComplete()) {
                        toRemove.addElement(tile);
                    }
                }
                for (int i = 0; i < toRemove.size(); i++) {
                    this.mapCache.remove(toRemove.elementAt(i));
                }
            }
        }
    }

    void mapChanged() {
        this.lastMapMoveTime = getRelativeTime();
        this.flashService.mapChanged();
        synchronized (this.indefiniteThreadLockObject) {
            this.indefiniteThreadLockObject.notify();
        }
    }

    public void handleOutOfMemory(boolean warning) {
        FlashRecord.clearDataCache();
        clearScaledImages();
        synchronized (this.mapCache) {
            doCompact(true);
            if (this.autoConfigCache) {
                this.maxCacheDataSize = 25000;
                setAutoTargetCacheSize();
            } else {
                this.maxCacheDataSize = Math.max(this.maxCacheDataSize - 8000, 25000);
                setAutoTargetCacheSize();
            }
            checkTrimCache();
        }
    }

    public void run() {
        long nextTrimTime = getRelativeTime() + 2101;
        long nextCompactTime = getRelativeTime() + 3113;
        while (!this.exitWorkThread) {
            try {
                synchronized (this.timedThreadLockObject) {
                    try {
                        long nextWakeup = (((nextTrimTime > nextCompactTime ? 1 : (nextTrimTime == nextCompactTime ? 0 : -1)) >= 0 ? 1 : null) == null ? nextTrimTime : nextCompactTime) - getRelativeTime();
                        if ((nextWakeup <= 0 ? 1 : null) == null) {
                            this.timedThreadLockObject.wait(nextWakeup);
                        }
                    } catch (InterruptedException e) {
                    }
                }
                if (!this.exitWorkThread) {
                    long currentTime = getRelativeTime();
                    if ((nextTrimTime >= currentTime ? 1 : null) == null) {
                        checkTrimCache();
                        nextTrimTime = currentTime + 2101;
                    }
                    if ((nextCompactTime >= currentTime ? 1 : null) == null) {
                        doCompact(false);
                        nextCompactTime = currentTime + 3113;
                    }
                    if (!this.flashService.writeCache()) {
                        if ((this.lastMapMoveTime + 4000 >= currentTime ? 1 : null) == null) {
                            synchronized (this.indefiniteThreadLockObject) {
                                try {
                                    this.indefiniteThreadLockObject.wait();
                                } catch (InterruptedException e2) {
                                }
                            }
                        }
                    }
                }
            } catch (Exception e3) {
                Log.logThrowable("MapService BG", e3);
            } catch (OutOfMemoryError e4) {
                StaticUtil.handleOutOfMemory();
            }
        }
    }

    private static long getRelativeTime() {
        return Config.getInstance().getClock().relativeTimeMillis();
    }

    private void stopWorkThread() {
        if (!this.exitWorkThread) {
            this.exitWorkThread = true;
            synchronized (this.timedThreadLockObject) {
                this.timedThreadLockObject.notify();
            }
            synchronized (this.indefiniteThreadLockObject) {
                this.indefiniteThreadLockObject.notify();
            }
        }
    }

    private void startWorkThread() {
        if (this.exitWorkThread) {
            this.exitWorkThread = false;
            Thread bgThread = new Thread(this, "MapService");
            bgThread.setPriority(1);
            bgThread.start();
        }
    }

    void pause() {
        stopWorkThread();
    }

    void resume() {
        startWorkThread();
    }
}
