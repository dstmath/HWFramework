package android_maps_conflict_avoidance.com.google.googlenav.map;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.util.StopwatchStats;
import android_maps_conflict_avoidance.com.google.common.util.text.TextUtil;
import android_maps_conflict_avoidance.com.google.googlenav.GmmLogger;
import android_maps_conflict_avoidance.com.google.googlenav.datarequest.BaseDataRequest;
import android_maps_conflict_avoidance.com.google.googlenav.labs.LocalLanguageTileLab;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Vector;

public abstract class BaseTileRequest extends BaseDataRequest {
    protected final long createTime = Config.getInstance().getClock().relativeTimeMillis();
    private final int requestType;
    private StopwatchStats stopwatchStatsTile;
    private int textSize;
    private int writeLatency;

    protected abstract void handleEndOfResponse(int i);

    protected abstract boolean processDownloadedTile(int i, Tile tile, byte[] bArr) throws IOException;

    protected abstract void setTileEditionAndTextSize(int i, int i2);

    protected BaseTileRequest(int requestType, byte flags) {
        this.requestType = requestType;
        this.stopwatchStatsTile = new StopwatchStats("tile-" + formatTileTypesForLog(1 << flags), "t", (short) 22);
        this.stopwatchStatsTile.start();
    }

    public int getRequestType() {
        return this.requestType;
    }

    protected void writeRequestForTiles(Tile[] tileList, DataOutput dos) throws IOException {
        if (this.requestType == 26) {
            dos.writeShort(tileList.length);
            this.textSize = MapTile.getTextSize();
            dos.writeShort(this.textSize);
            dos.writeShort(256);
            long format = 2607;
            if (LocalLanguageTileLab.INSTANCE.isActive()) {
                format = 10799;
            }
            dos.writeLong(format);
        }
        for (Tile tile : tileList) {
            tile.write(dos);
        }
        this.writeLatency = (int) (Config.getInstance().getClock().relativeTimeMillis() - this.createTime);
    }

    public boolean readResponseData(DataInput dis) throws IOException {
        int tileIndex = 0;
        try {
            int firstByteLatency = (int) (Config.getInstance().getClock().relativeTimeMillis() - this.createTime);
            setTileEditionAndTextSize(dis.readUnsignedShort(), this.textSize);
            int tileCount = 0;
            int totalSize = 0;
            int tileTypes = 0;
            if (this.requestType == 26) {
                int responseCode = dis.readUnsignedByte();
                if (responseCode == 0) {
                    tileCount = dis.readUnsignedShort();
                } else {
                    throw new IOException("Server returned: " + responseCode);
                }
            }
            tileIndex = 0;
            while (tileIndex < tileCount) {
                Tile location = Tile.read(dis);
                byte[] imageBytes = readImageData(dis);
                if (processDownloadedTile(tileIndex, location, imageBytes)) {
                    break;
                }
                totalSize += imageBytes.length;
                tileTypes |= 1 << location.getFlags();
                tileIndex++;
            }
            int lastByteLatency = (int) (Config.getInstance().getClock().relativeTimeMillis() - this.createTime);
            this.stopwatchStatsTile.stop();
            GmmLogger.logTimingTileLatency(formatTileTypesForLog(tileTypes), this.writeLatency, firstByteLatency, lastByteLatency, tileCount, totalSize);
            return true;
        } finally {
            handleEndOfResponse(tileIndex);
        }
    }

    private byte[] readImageData(DataInput dis) throws IOException {
        byte[] imageBytes = new byte[dis.readUnsignedShort()];
        dis.readFully(imageBytes);
        return imageBytes;
    }

    private static String formatTileTypesForLog(int tileTypes) {
        Vector result = new Vector();
        if ((tileTypes & 4) != 0) {
            result.addElement("m");
        }
        if ((tileTypes & 8) != 0) {
            result.addElement("s");
        }
        if ((tileTypes & 64) != 0) {
            result.addElement("h");
        }
        if ((tileTypes & 128) != 0) {
            result.addElement("n");
        }
        if ((tileTypes & 16) != 0) {
            result.addElement("t");
        }
        return TextUtil.join(result, ",");
    }
}
