package android_maps_conflict_avoidance.com.google.googlenav.map;

import java.io.DataInput;
import java.io.IOException;

public class TrafficRoad {
    private final byte[] roadContent;

    TrafficRoad(byte[] roadContent) {
        this.roadContent = roadContent;
    }

    public int getNumPoints() {
        return (this.roadContent.length - 2) / 5;
    }

    public int getXOffset(int pointIndex) {
        return readShortFrom(this.roadContent, (pointIndex * 5) + 2);
    }

    public int getYOffset(int pointIndex) {
        return readShortFrom(this.roadContent, (pointIndex * 5) + 4);
    }

    public int getSpeedCategory(int pointIndex) {
        return this.roadContent[(pointIndex * 5) + 6] & 255;
    }

    public int getTrafficLineWidth() {
        return this.roadContent[0] & 255;
    }

    public int getTrafficLineBackgroundWidth() {
        return this.roadContent[1] & 255;
    }

    public static TrafficRoad readTrafficRoad(DataInput dis, int roadwayPropertyLength, int roadPointLength) throws IOException {
        int trafficLineWidth = dis.readUnsignedByte();
        int trafficLineBackgroundWidth = dis.readUnsignedByte();
        dis.skipBytes(roadwayPropertyLength - 2);
        int numPoints = dis.readUnsignedShort();
        byte[] roadPointsContent = new byte[((numPoints * 5) + 2)];
        roadPointsContent[0] = (byte) ((byte) trafficLineWidth);
        int nextByte = 1 + 1;
        roadPointsContent[1] = (byte) ((byte) trafficLineBackgroundWidth);
        int i = 0;
        int nextByte2 = nextByte;
        while (i < numPoints) {
            nextByte2 = writeShortTo(dis.readUnsignedShort(), roadPointsContent, writeShortTo(dis.readUnsignedShort(), roadPointsContent, nextByte2));
            nextByte = nextByte2 + 1;
            roadPointsContent[nextByte2] = (byte) ((byte) dis.readUnsignedByte());
            dis.skipBytes(roadPointLength - 5);
            i++;
            nextByte2 = nextByte;
        }
        return new TrafficRoad(roadPointsContent);
    }

    private static int writeShortTo(int value, byte[] content, int nextByte) {
        int i = nextByte + 1;
        content[nextByte] = (byte) ((byte) ((value >> 8) & 255));
        nextByte = i + 1;
        content[i] = (byte) ((byte) (value & 255));
        return nextByte;
    }

    private static int readShortFrom(byte[] content, int startByte) {
        return (content[startByte] << 8) | (content[startByte + 1] & 255);
    }
}
