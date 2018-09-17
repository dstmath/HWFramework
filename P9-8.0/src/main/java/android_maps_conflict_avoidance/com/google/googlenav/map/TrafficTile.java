package android_maps_conflict_avoidance.com.google.googlenav.map;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.io.IoUtil;
import android_maps_conflict_avoidance.com.google.common.util.RuntimeCheck;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

public class TrafficTile {
    private long dataTime = Long.MIN_VALUE;
    private long lastAccess;
    private final Tile location;
    private TrafficRoad[] roads;

    public TrafficTile(Tile location) {
        this.location = location;
    }

    public Tile getLocation() {
        return this.location;
    }

    public boolean isComplete() {
        return this.roads != null;
    }

    public TrafficRoad[] getTrafficRoads() {
        return this.roads;
    }

    public long getDataTime() {
        return !isEmpty() ? this.dataTime : Config.getInstance().getClock().relativeTimeMillis();
    }

    protected void setData(long time, TrafficRoad[] roads) {
        this.dataTime = time;
        this.roads = roads;
    }

    public void readData(byte[] data) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(data, 0, 6));
        if (1414676787 == dataInputStream.readInt()) {
            DataInput dis;
            int inflatedDataSize = dataInputStream.readUnsignedShort();
            int lengthOfDeflatedBytes = data.length - 6;
            if (RuntimeCheck.isTest()) {
                dis = new DataInputStream(new ByteArrayInputStream(data, 6, lengthOfDeflatedBytes));
            } else {
                dis = IoUtil.createDataInputFromBytes(IoUtil.inflate(data, 6, lengthOfDeflatedBytes, inflatedDataSize));
            }
            int roadwayPropertyLength = dis.readUnsignedShort();
            int roadPointLength = dis.readUnsignedShort();
            int numRoadways = dis.readUnsignedShort();
            TrafficRoad[] roads = new TrafficRoad[numRoadways];
            for (int i = 0; i < numRoadways; i++) {
                roads[i] = TrafficRoad.readTrafficRoad(dis, roadwayPropertyLength, roadPointLength);
            }
            setData(Config.getInstance().getClock().relativeTimeMillis(), roads);
            return;
        }
        throw new IOException("Bad traffic header");
    }

    public void setLastAccess(long time) {
        this.lastAccess = time;
    }

    long getLastAccess() {
        return this.lastAccess;
    }

    public boolean isEmpty() {
        return this.roads != null && this.roads.length == 0;
    }
}
