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
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class MapService implements OutOfMemoryHandler, Runnable {
    private final boolean autoConfigCache;
    private MapTileRequest currentRequest = null;
    volatile boolean exitWorkThread = true;
    final MapTileStorage flashService;
    private final Object indefiniteThreadLockObject = new Object();
    private long lastMapMoveTime;
    private final Vector layerImageTiles = new Vector();
    private final Vector layerServices = new Vector();
    final Hashtable mapCache;
    private volatile boolean mapCacheLocked;
    private int maxCacheDataSize;
    private TileUpdateObserver observer;
    private long outOfMemoryTime = Long.MIN_VALUE;
    private final Vector repaintListeners = new Vector();
    private int requestType = 26;
    private int requestsOutstanding = 0;
    private int targetCacheDataSize;
    private final Hashtable tempScaledImages;
    private final Object timedThreadLockObject = new Object();

    private class MapTileRequest extends BaseTileRequest {
        private boolean closed = false;
        private boolean isForeground = true;
        private Vector tilePriorityList = new Vector();
        private Vector tileSchedule = new Vector();

        MapTileRequest(byte flags) {
            super(MapService.this.requestType, flags);
        }

        /* JADX WARNING: Missing block: B:10:0x0019, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
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

    /*  JADX ERROR: JadxRuntimeException in pass: BlockProcessor
        jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:54:0x00b6
        	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:360)
        	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:56)
        	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    android_maps_conflict_avoidance.com.google.googlenav.map.MapTile getTile(android_maps_conflict_avoidance.com.google.googlenav.map.Tile r14, int r15, boolean r16, int r17, long r18) {
        /*
        r13 = this;
        r9 = r13.mapCache;
        r6 = r9.get(r14);
        r6 = (android_maps_conflict_avoidance.com.google.googlenav.map.MapTile) r6;
        r10 = -9223372036854775808;
        r9 = (r18 > r10 ? 1 : (r18 == r10 ? 0 : -1));
        if (r9 != 0) goto L_0x001a;
    L_0x000e:
        r9 = android_maps_conflict_avoidance.com.google.common.Config.getInstance();
        r9 = r9.getClock();
        r18 = r9.currentTimeMillis();
    L_0x001a:
        if (r6 == 0) goto L_0x0028;
    L_0x001c:
        r9 = r6.isComplete();
        if (r9 == 0) goto L_0x0098;
    L_0x0022:
        r0 = r18;
        r6.setLastAccessTime(r0);
        return r6;
    L_0x0028:
        r9 = r13.mapCacheLocked;
        if (r9 == 0) goto L_0x0036;
    L_0x002c:
        r6 = new android_maps_conflict_avoidance.com.google.googlenav.map.MapTile;
        r9 = 0;
        r9 = (android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage) r9;
        r10 = 1;
        r6.<init>(r14, r9, r10);
        goto L_0x0022;
    L_0x0036:
        r5 = r13.mapCache;
        monitor-enter(r5);
        r9 = 1;
        r13.setMapCacheLocked(r9);	 Catch:{ all -> 0x0058 }
        r9 = r13.flashService;	 Catch:{ all -> 0x0091 }
        r6 = r9.getMapTile(r14);	 Catch:{ all -> 0x0091 }
        if (r6 == 0) goto L_0x005b;	 Catch:{ all -> 0x0091 }
    L_0x0045:
        if (r16 == 0) goto L_0x008c;	 Catch:{ all -> 0x0091 }
    L_0x0047:
        r13.addMapEntry(r6);	 Catch:{ all -> 0x0091 }
        r9 = android_maps_conflict_avoidance.com.google.googlenav.Stats.getInstance();	 Catch:{ all -> 0x0091 }
        r9.flashCacheHit();	 Catch:{ all -> 0x0091 }
    L_0x0051:
        r3 = 0;
        r9 = 0;
        r13.setMapCacheLocked(r9);	 Catch:{ all -> 0x0058 }
        monitor-exit(r5);	 Catch:{ all -> 0x0058 }
        goto L_0x0022;	 Catch:{ all -> 0x0058 }
    L_0x0058:
        r4 = move-exception;	 Catch:{ all -> 0x0058 }
    L_0x0059:
        monitor-exit(r5);	 Catch:{ all -> 0x0058 }
        throw r4;
    L_0x005b:
        r0 = r17;	 Catch:{ all -> 0x0091 }
        r8 = r13.getTempImage(r14, r0);	 Catch:{ all -> 0x0091 }
        if (r16 != 0) goto L_0x006e;	 Catch:{ all -> 0x0091 }
    L_0x0063:
        r7 = new android_maps_conflict_avoidance.com.google.googlenav.map.MapTile;	 Catch:{ all -> 0x0091 }
        r9 = 1;	 Catch:{ all -> 0x0091 }
        r7.<init>(r14, r8, r9);	 Catch:{ all -> 0x0091 }
        r13.addMapEntry(r7);	 Catch:{ all -> 0x00b9 }
        r6 = r7;
        goto L_0x0051;
    L_0x006e:
        r9 = android_maps_conflict_avoidance.com.google.googlenav.datarequest.DataRequestDispatcher.getInstance();	 Catch:{ all -> 0x0091 }
        r9 = r9.canDispatchNow();	 Catch:{ all -> 0x0091 }
        if (r9 == 0) goto L_0x0063;	 Catch:{ all -> 0x0091 }
    L_0x0078:
        r7 = new android_maps_conflict_avoidance.com.google.googlenav.map.MapTile;	 Catch:{ all -> 0x0091 }
        r7.<init>(r14, r8);	 Catch:{ all -> 0x0091 }
        r13.queueTileRequest(r7, r15);	 Catch:{ all -> 0x00b9 }
        r13.addMapEntry(r7);	 Catch:{ all -> 0x00b9 }
        r9 = android_maps_conflict_avoidance.com.google.googlenav.Stats.getInstance();	 Catch:{ all -> 0x00b9 }
        r9.flashCacheMiss();	 Catch:{ all -> 0x00b9 }
        r6 = r7;
        goto L_0x0051;
    L_0x008c:
        r10 = 20000; // 0x4e20 float:2.8026E-41 double:9.8813E-320;
        r18 = r18 - r10;
        goto L_0x0047;
    L_0x0091:
        r2 = move-exception;
    L_0x0092:
        r3 = 0;
        r9 = 0;
        r13.setMapCacheLocked(r9);	 Catch:{ all -> 0x0058 }
        throw r2;	 Catch:{ all -> 0x0058 }
    L_0x0098:
        r9 = r6.getRequested();
        if (r9 != 0) goto L_0x0022;
    L_0x009e:
        if (r16 == 0) goto L_0x0022;
    L_0x00a0:
        r9 = android_maps_conflict_avoidance.com.google.googlenav.datarequest.DataRequestDispatcher.getInstance();
        r9 = r9.canDispatchNow();
        if (r9 == 0) goto L_0x0022;
    L_0x00aa:
        r13.queueTileRequest(r6, r15);
        r9 = android_maps_conflict_avoidance.com.google.googlenav.Stats.getInstance();
        r9.flashCacheMiss();
        goto L_0x0022;
        r4 = move-exception;
        r6 = r7;
        goto L_0x0059;
    L_0x00b9:
        r2 = move-exception;
        r6 = r7;
        goto L_0x0092;
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.googlenav.map.MapService.getTile(android_maps_conflict_avoidance.com.google.googlenav.map.Tile, int, boolean, int, long):android_maps_conflict_avoidance.com.google.googlenav.map.MapTile");
    }

    void setMapCacheLocked(boolean mapCacheLocked) {
        this.mapCacheLocked = mapCacheLocked;
    }

    MapService(int maxCacheDataSize, int targetCacheDataSize, int maxFlashSize, int maxRecordStores, String tileRecordStoreName) {
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
            case 1:
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
        long maxAge = !emergency ? 4000 : 2000;
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
