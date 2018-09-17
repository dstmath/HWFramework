package android_maps_conflict_avoidance.com.google.googlenav.map;

import android_maps_conflict_avoidance.com.google.common.Clock;
import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.Log;
import android_maps_conflict_avoidance.com.google.common.OutOfMemoryHandler;
import android_maps_conflict_avoidance.com.google.common.StaticUtil;
import android_maps_conflict_avoidance.com.google.common.task.TaskRunner;
import android_maps_conflict_avoidance.com.google.common.task.TimerTask;
import android_maps_conflict_avoidance.com.google.googlenav.android.TaskRunnerManager;
import android_maps_conflict_avoidance.com.google.googlenav.datarequest.DataRequestDispatcher;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class TrafficService implements Runnable, OutOfMemoryHandler {
    private static final TrafficRoad[] EMPTY_TRAFFICROAD_ARRAY = new TrafficRoad[0];
    private final Hashtable cache;
    private long nextRefreshTime;
    private final long refreshMillis;
    private volatile TrafficTileRequest request;
    private final Hashtable requestedTiles;
    private volatile boolean stopCleanCache = true;
    private final TimerTask timerTask;

    private class TrafficTileRequest extends BaseTileRequest {
        private final Vector tiles = new Vector();

        public TrafficTileRequest() {
            super(26, (byte) 4);
        }

        public void writeRequestData(DataOutput dos) throws IOException {
            Tile[] tileList = new Tile[this.tiles.size()];
            for (int i = 0; i < this.tiles.size(); i++) {
                tileList[i] = ((TrafficTile) this.tiles.elementAt(i)).getLocation();
            }
            writeRequestForTiles(tileList, dos);
        }

        protected boolean processDownloadedTile(int tileIndex, Tile tile, byte[] data) throws IOException {
            TrafficTile tt = (TrafficTile) TrafficService.this.requestedTiles.get(tile);
            if (tt == null) {
                tt = new TrafficTile(tile);
            } else if (tt.isComplete()) {
                tt = new TrafficTile(tile);
            }
            if (data.length != 0) {
                tt.readData(data);
            } else {
                tt.setData(Config.getInstance().getClock().relativeTimeMillis(), TrafficService.EMPTY_TRAFFICROAD_ARRAY);
            }
            synchronized (this) {
                TrafficService.this.cache.put(tile, tt);
                TrafficService.this.requestedTiles.remove(tile);
            }
            TrafficService.this.notifyDownloadedTile();
            return false;
        }

        protected void setTileEditionAndTextSize(int tileEdition, int textSize) {
        }

        protected void handleEndOfResponse(int tileIndex) {
        }

        public void addTile(TrafficTile trafficTile) {
            this.tiles.addElement(trafficTile);
        }
    }

    public TrafficService(long refreshMillis, TaskRunner taskRunner) {
        this.refreshMillis = refreshMillis;
        this.nextRefreshTime = Long.MIN_VALUE;
        this.cache = new Hashtable();
        this.requestedTiles = new Hashtable();
        StaticUtil.registerOutOfMemoryHandler(this);
        this.timerTask = new TimerTask(taskRunner, this);
        this.timerTask.setDelay(20113);
        start();
    }

    public void close() {
        StaticUtil.removeOutOfMemoryHandler(this);
        stop();
    }

    /* JADX WARNING: Missing block: B:10:0x0029, code:
            return r6;
     */
    /* JADX WARNING: Missing block: B:19:0x0042, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized TrafficTile getTile(Tile tile, boolean fetch) {
        if (tile.getZoom().getZoomLevel() > 20) {
            return null;
        }
        TrafficTile trafficTile = (TrafficTile) this.cache.get(tile);
        TrafficTile queuedTile = (TrafficTile) this.requestedTiles.get(tile);
        if (trafficTile != null) {
            Clock clock = Config.getInstance().getClock();
            long tileDataTime = trafficTile.getDataTime();
            long dataAge = clock.relativeTimeMillis() - tileDataTime;
            if (fetch && queuedTile == null && tileDataTime != Long.MIN_VALUE) {
                if ((dataAge <= this.refreshMillis / 2 ? 1 : null) == null) {
                    if (!((clock.relativeTimeMillis() > this.nextRefreshTime ? 1 : null) == null && this.request == null)) {
                        requestTile(tile, trafficTile);
                    }
                }
            }
        } else if (queuedTile != null) {
            return queuedTile;
        } else {
            trafficTile = new TrafficTile(tile);
            if (fetch) {
                requestTile(tile, trafficTile);
            }
        }
    }

    private synchronized void requestTile(Tile tile, TrafficTile trafficTile) {
        if (this.request == null) {
            this.request = new TrafficTileRequest();
        }
        this.request.addTile(trafficTile);
        this.requestedTiles.put(tile, trafficTile);
    }

    public synchronized void requestTiles() {
        if (this.request != null) {
            DataRequestDispatcher.getInstance().addDataRequest(this.request);
            this.request = null;
            this.nextRefreshTime = Config.getInstance().getClock().relativeTimeMillis() + this.refreshMillis;
        }
    }

    private void notifyDownloadedTile() {
        if (!this.stopCleanCache && this.timerTask.isUnscheduled()) {
            this.timerTask.schedule();
        }
    }

    synchronized void cleanCache(long maxAge) {
        Enumeration tiles = this.cache.keys();
        while (tiles.hasMoreElements()) {
            Object obj;
            Tile tile = (Tile) tiles.nextElement();
            if (Config.getInstance().getClock().relativeTimeMillis() - ((TrafficTile) this.cache.get(tile)).getLastAccess() <= maxAge) {
                obj = 1;
            } else {
                obj = null;
            }
            if (obj == null) {
                this.cache.remove(tile);
            }
        }
    }

    public void run() {
        try {
            cleanCache(60000);
        } catch (Exception e) {
            Log.logQuietThrowable("TrafficService BG", e);
        } catch (OutOfMemoryError e2) {
            StaticUtil.handleOutOfMemory();
        }
    }

    public void handleOutOfMemory(boolean warning) {
        this.cache.clear();
    }

    public synchronized void start() {
        this.stopCleanCache = false;
    }

    public synchronized void stop() {
        this.stopCleanCache = true;
        TaskRunnerManager.getTaskRunner().cancelTask(this.timerTask);
    }
}
