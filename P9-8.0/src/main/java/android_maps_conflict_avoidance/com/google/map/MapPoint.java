package android_maps_conflict_avoidance.com.google.map;

import android_maps_conflict_avoidance.com.google.common.io.IoUtil;
import android_maps_conflict_avoidance.com.google.common.util.text.TextUtil;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MapPoint implements Geometry {
    private static final Zoom PIXEL_COORDS_ZOOM = Zoom.getZoom(22);
    static int[] mercatorValues = null;
    static int[] reverseMercatorValues = null;
    private final int latitudeE6;
    private final int longitudeE6;
    private final int pixelCoordX;
    private final int pixelCoordY;

    private static int adjustLongitude(int longitude) {
        while (longitude < -180000000) {
            longitude += 360000000;
        }
        while (longitude > 180000000) {
            longitude -= 360000000;
        }
        return longitude;
    }

    private static int adjustPixelX(int pixelX, int equatorPixels) {
        while (pixelX >= equatorPixels) {
            pixelX -= equatorPixels;
        }
        while (pixelX < 0) {
            pixelX += equatorPixels;
        }
        return pixelX;
    }

    private static int adjustPixelY(int pixelY, int equatorPixels) {
        if (pixelY < 0) {
            return 0;
        }
        if (pixelY < equatorPixels) {
            return pixelY;
        }
        return equatorPixels - 1;
    }

    public MapPoint(int latitude, int longitude) {
        longitude = adjustLongitude(longitude);
        if (latitude > 80000000) {
            latitude = 80000000;
        }
        if (latitude < -80000000) {
            latitude = -80000000;
        }
        this.latitudeE6 = latitude;
        this.longitudeE6 = longitude;
        this.pixelCoordX = getXPixelFromLon(longitude, PIXEL_COORDS_ZOOM);
        this.pixelCoordY = getYPixelFromLat(latitude, PIXEL_COORDS_ZOOM);
    }

    public MapPoint(int pixelX, int pixelY, int zoomLevel) {
        Zoom zoom = Zoom.getZoom(zoomLevel);
        pixelX = adjustPixelX(pixelX, zoom.getEquatorPixels());
        pixelY = adjustPixelY(pixelY, zoom.getEquatorPixels());
        this.pixelCoordX = zoom.changePixelsToTargetZoomlevel(pixelX, 22);
        this.pixelCoordY = zoom.changePixelsToTargetZoomlevel(pixelY, 22);
        this.latitudeE6 = getLatitudeFromY(this.pixelCoordY, PIXEL_COORDS_ZOOM);
        this.longitudeE6 = getLongitudeFromX(this.pixelCoordX, PIXEL_COORDS_ZOOM);
    }

    public MapPoint(int pixelX, int pixelY, int zoomLevel, int latitude, int longitude) {
        longitude = adjustLongitude(longitude);
        this.latitudeE6 = latitude;
        this.longitudeE6 = longitude;
        Zoom zoom = Zoom.getZoom(zoomLevel);
        pixelX = adjustPixelX(pixelX, zoom.getEquatorPixels());
        pixelY = adjustPixelY(pixelY, zoom.getEquatorPixels());
        this.pixelCoordX = zoom.changePixelsToTargetZoomlevel(pixelX, 22);
        this.pixelCoordY = zoom.changePixelsToTargetZoomlevel(pixelY, 22);
    }

    public int getXPixel(Zoom zoom) {
        return PIXEL_COORDS_ZOOM.changePixelsToTargetZoomlevel(this.pixelCoordX, zoom.getZoomLevel());
    }

    public static int getXPixelFromLon(int longitude, Zoom zoom) {
        return (int) ((((long) zoom.getEquatorPixels()) / 2) + ((((long) longitude) * ((long) zoom.getEquatorPixels())) / 360000000));
    }

    private static int[] getMercatorValues() {
        if (mercatorValues == null) {
            mercatorValues = new int[84];
            try {
                readMercatorValues(IoUtil.createDataInputFromBytes(new byte[]{(byte) -1, (byte) -16, (byte) -67, (byte) -115, (byte) 15, (byte) 66, (byte) 115, (byte) 15, (byte) 66, (byte) 115, (byte) 15, (byte) 67, (byte) -93, (byte) 15, (byte) 70, (byte) 6, (byte) 15, (byte) 73, (byte) -103, (byte) 15, (byte) 78, (byte) 97, (byte) 15, (byte) 84, (byte) 94, (byte) 15, (byte) 91, (byte) -109, (byte) 15, (byte) 100, (byte) 2, (byte) 15, (byte) 109, (byte) -80, (byte) 15, (byte) 120, (byte) -97, (byte) 15, (byte) -124, (byte) -44, (byte) 15, (byte) -110, (byte) 84, (byte) 15, (byte) -95, (byte) 38, (byte) 15, (byte) -79, (byte) 78, (byte) 15, (byte) -62, (byte) -45, (byte) 15, (byte) -43, (byte) -67, (byte) 15, (byte) -22, (byte) 21, (byte) 15, (byte) -1, (byte) -31, (byte) 16, (byte) 23, (byte) 45, (byte) 16, (byte) 48, (byte) 1, (byte) 16, (byte) 74, (byte) 107, (byte) 16, (byte) 102, (byte) 116, (byte) 16, (byte) -124, (byte) 43, (byte) 16, (byte) -93, (byte) -100, (byte) 16, (byte) -60, (byte) -41, (byte) 16, (byte) -25, (byte) -19, (byte) 17, (byte) 12, (byte) -18, (byte) 17, (byte) 51, (byte) -20, (byte) 17, (byte) 92, (byte) -4, (byte) 17, (byte) -120, (byte) 52, (byte) 17, (byte) -75, (byte) -87, (byte) 17, (byte) -27, (byte) 118, (byte) 18, (byte) 23, (byte) -76, (byte) 18, (byte) 76, (byte) -127, (byte) 18, (byte) -125, (byte) -3, (byte) 18, (byte) -66, (byte) 70, (byte) 18, (byte) -5, (byte) -124, (byte) 19, (byte) 59, (byte) -37, (byte) 19, Byte.MAX_VALUE, (byte) 119, (byte) 19, (byte) -58, (byte) -122, (byte) 20, (byte) 17, (byte) 56, (byte) 20, (byte) 95, (byte) -60, (byte) 20, (byte) -78, (byte) 100, (byte) 21, (byte) 9, (byte) 87, (byte) 21, (byte) 100, (byte) -27, (byte) 21, (byte) -59, (byte) 86, (byte) 22, (byte) 42, (byte) -1, (byte) 22, (byte) -106, (byte) 58, (byte) 23, (byte) 7, (byte) 109, (byte) 23, Byte.MAX_VALUE, (byte) 2, (byte) 23, (byte) -3, (byte) 117, (byte) 24, (byte) -125, (byte) 72, (byte) 25, (byte) 17, (byte) 20, (byte) 25, (byte) -89, (byte) 120, (byte) 26, (byte) 71, (byte) 46, (byte) 26, (byte) -15, (byte) 3, (byte) 27, (byte) -91, (byte) -39, (byte) 28, (byte) 102, (byte) -77, (byte) 29, (byte) 52, (byte) -77, (byte) 30, (byte) 17, (byte) 31, (byte) 30, (byte) -3, (byte) 111, (byte) 31, (byte) -5, (byte) 74, (byte) 33, (byte) 12, (byte) -105, (byte) 34, (byte) 51, (byte) -120, (byte) 35, (byte) 114, (byte) -91, (byte) 36, (byte) -52, (byte) -30, (byte) 38, (byte) 69, (byte) -76, (byte) 39, (byte) -31, (byte) 42, (byte) 41, (byte) -92, (byte) 25, (byte) 43, (byte) -108, (byte) 70, (byte) 45, (byte) -72, (byte) -91, (byte) 48, (byte) 25, (byte) -84, (byte) 50, (byte) -63, (byte) -63, (byte) 53, (byte) -67, (byte) -47, (byte) 57, (byte) 30, (byte) 28, (byte) 60, (byte) -9, (byte) 105, (byte) 65, (byte) 100, (byte) -96, (byte) 70, (byte) -119, (byte) 82, (byte) 76, (byte) -107, (byte) 115, (byte) 83, (byte) -53, (byte) 79, (byte) 92, (byte) -119, (byte) 52, (byte) 103, (byte) 90, (byte) 12}), mercatorValues);
            } catch (IOException e) {
                throw new RuntimeException("Can't read mercator.dat");
            }
        }
        return mercatorValues;
    }

    public static int getYPixelFromLat(int latitude, Zoom zoom) {
        int indexFloor = getMercatorIndex(latitude);
        int latDiff = Math.abs(latitude) % 1000000;
        int[] values = getMercatorValues();
        int mercatorY = (int) (((((((((((long) (((((values[indexFloor - 1] * -1) + (values[indexFloor] * 3)) - (values[indexFloor + 1] * 3)) + values[indexFloor + 2]) / 6)) * ((long) latDiff)) * ((long) latDiff)) / 1000000) * ((long) latDiff)) / 1000000) / 1000000) + ((((((long) ((((values[indexFloor - 1] * 3) - (values[indexFloor] * 6)) + (values[indexFloor + 1] * 3)) / 6)) * ((long) latDiff)) * ((long) latDiff)) / 1000000) / 1000000)) + ((((long) (((((values[indexFloor - 1] * -2) - (values[indexFloor] * 3)) + (values[indexFloor + 1] * 6)) - values[indexFloor + 2]) / 6)) * ((long) latDiff)) / 1000000)) + ((long) values[indexFloor]));
        if (latitude < 0) {
            mercatorY = -mercatorY;
        }
        return (int) ((((long) zoom.getEquatorPixels()) / 2) - ((long) (mercatorToPixelsTimesTen(mercatorY, zoom) / 10)));
    }

    public int getYPixel(Zoom zoom) {
        return PIXEL_COORDS_ZOOM.changePixelsToTargetZoomlevel(this.pixelCoordY, zoom.getZoomLevel());
    }

    private static int[] getReverseMercatorValues() {
        if (reverseMercatorValues == null) {
            reverseMercatorValues = new int[141];
            try {
                readMercatorValues(IoUtil.createDataInputFromBytes(new byte[]{(byte) -1, (byte) -28, (byte) -119, (byte) -24, (byte) 27, (byte) 118, (byte) 24, (byte) 27, (byte) 118, (byte) 24, (byte) 27, (byte) 111, (byte) 42, (byte) 27, (byte) 97, (byte) 85, (byte) 27, (byte) 76, (byte) -82, (byte) 27, (byte) 49, (byte) 75, (byte) 27, (byte) 15, (byte) 79, (byte) 26, (byte) -26, (byte) -26, (byte) 26, (byte) -72, (byte) 61, (byte) 26, (byte) -125, (byte) -114, (byte) 26, (byte) 73, (byte) 22, (byte) 26, (byte) 9, (byte) 25, (byte) 25, (byte) -61, (byte) -35, (byte) 25, (byte) 121, (byte) -81, (byte) 25, (byte) 42, (byte) -34, (byte) 24, (byte) -41, (byte) -70, (byte) 24, Byte.MIN_VALUE, (byte) -103, (byte) 24, (byte) 37, (byte) -51, (byte) 23, (byte) -57, (byte) -82, (byte) 23, (byte) 102, (byte) -115, (byte) 23, (byte) 2, (byte) -64, (byte) 22, (byte) -100, (byte) -101, (byte) 22, (byte) 52, (byte) 109, (byte) 21, (byte) -54, (byte) -122, (byte) 21, (byte) 95, (byte) 48, (byte) 20, (byte) -14, (byte) -72, (byte) 20, (byte) -123, (byte) 97, (byte) 20, (byte) 23, (byte) 111, (byte) 19, (byte) -87, (byte) 33, (byte) 19, (byte) 58, (byte) -76, (byte) 18, (byte) -52, (byte) 94, (byte) 18, (byte) 94, (byte) 85, (byte) 17, (byte) -16, (byte) -56, (byte) 17, (byte) -125, (byte) -26, (byte) 17, (byte) 23, (byte) -42, (byte) 16, (byte) -84, (byte) -64, (byte) 16, (byte) 66, (byte) -61, (byte) 15, (byte) -38, (byte) 2, (byte) 15, (byte) 114, (byte) -106, (byte) 15, (byte) 12, (byte) -103, (byte) 14, (byte) -88, (byte) 32, (byte) 14, (byte) 69, (byte) 64, (byte) 13, (byte) -28, (byte) 7, (byte) 13, (byte) -124, (byte) -122, (byte) 13, (byte) 38, (byte) -58, (byte) 12, (byte) -54, (byte) -45, (byte) 12, (byte) 112, (byte) -75, (byte) 12, (byte) 24, (byte) 113, (byte) 11, (byte) -62, (byte) 10, (byte) 11, (byte) 109, (byte) -121, (byte) 11, (byte) 26, (byte) -27, (byte) 10, (byte) -54, (byte) 40, (byte) 10, (byte) 123, (byte) 77, (byte) 10, (byte) 46, (byte) 82, (byte) 9, (byte) -29, (byte) 52, (byte) 9, (byte) -103, (byte) -16, (byte) 9, (byte) 82, (byte) -126, (byte) 9, (byte) 12, (byte) -29, (byte) 8, (byte) -55, (byte) 14, (byte) 8, (byte) -122, (byte) -2, (byte) 8, (byte) 70, (byte) -86, (byte) 8, (byte) 8, (byte) 13, (byte) 7, (byte) -53, (byte) 30, (byte) 7, (byte) -113, (byte) -41, (byte) 7, (byte) 86, (byte) 45, (byte) 7, (byte) 30, (byte) 27, (byte) 6, (byte) -25, (byte) -104, (byte) 6, (byte) -78, (byte) -102, (byte) 6, Byte.MAX_VALUE, (byte) 26, (byte) 6, (byte) 77, (byte) 15, (byte) 6, (byte) 28, (byte) 114, (byte) 5, (byte) -19, (byte) 56, (byte) 5, (byte) -65, (byte) 89, (byte) 5, (byte) -110, (byte) -50, (byte) 5, (byte) 103, (byte) -114, (byte) 5, (byte) 61, (byte) -111, (byte) 5, (byte) 20, (byte) -51, (byte) 4, (byte) -19, (byte) 59, (byte) 4, (byte) -58, (byte) -43, (byte) 4, (byte) -95, (byte) -113, (byte) 4, (byte) 125, (byte) 102, (byte) 4, (byte) 90, (byte) 78, (byte) 4, (byte) 56, (byte) 65, (byte) 4, (byte) 23, (byte) 58, (byte) 3, (byte) -9, (byte) 47, (byte) 3, (byte) -40, (byte) 26, (byte) 3, (byte) -71, (byte) -13, (byte) 3, (byte) -100, (byte) -75, (byte) 3, Byte.MIN_VALUE, (byte) 88, (byte) 3, (byte) 100, (byte) -41, (byte) 3, (byte) 74, (byte) 43, (byte) 3, (byte) 48, (byte) 76, (byte) 3, (byte) 23, (byte) 55, (byte) 2, (byte) -2, (byte) -28, (byte) 2, (byte) -25, (byte) 79, (byte) 2, (byte) -48, (byte) 114, (byte) 2, (byte) -70, (byte) 70, (byte) 2, (byte) -92, (byte) -56, (byte) 2, (byte) -113, (byte) -15, (byte) 2, (byte) 123, (byte) -67, (byte) 2, (byte) 104, (byte) 40, (byte) 2, (byte) 85, (byte) 43, (byte) 2, (byte) 66, (byte) -61, (byte) 2, (byte) 48, (byte) -20, (byte) 2, (byte) 31, (byte) -96, (byte) 2, (byte) 14, (byte) -35, (byte) 1, (byte) -2, (byte) -100, (byte) 1, (byte) -18, (byte) -36, (byte) 1, (byte) -33, (byte) -106, (byte) 1, (byte) -48, (byte) -53, (byte) 1, (byte) -62, (byte) 114, (byte) 1, (byte) -76, (byte) -118, (byte) 1, (byte) -89, (byte) 17, (byte) 1, (byte) -102, (byte) 1, (byte) 1, (byte) -115, (byte) 88, (byte) 1, (byte) -127, (byte) 18, (byte) 1, (byte) 117, (byte) 47, (byte) 1, (byte) 105, (byte) -89, (byte) 1, (byte) 94, (byte) 124, (byte) 1, (byte) 83, (byte) -88, (byte) 1, (byte) 73, (byte) 42, (byte) 1, (byte) 62, (byte) -1, (byte) 1, (byte) 53, (byte) 35, (byte) 1, (byte) 43, (byte) -105, (byte) 1, (byte) 34, (byte) 84, (byte) 1, (byte) 25, (byte) 91, (byte) 1, (byte) 16, (byte) -86, (byte) 1, (byte) 8, (byte) 60, (byte) 1, (byte) 0, (byte) 17, (byte) 0, (byte) -8, (byte) 40, (byte) 0, (byte) -16, (byte) 124, (byte) 0, (byte) -23, (byte) 13, (byte) 0, (byte) -31, (byte) -40, (byte) 0, (byte) -38, (byte) -34, (byte) 0, (byte) -44, (byte) 25, (byte) 0, (byte) -51, (byte) -117, (byte) 0, (byte) -57, (byte) 48, (byte) 0, (byte) -63, (byte) 8, (byte) 0, (byte) -69, (byte) 16, (byte) 0, (byte) -75, (byte) 71}), reverseMercatorValues);
            } catch (IOException e) {
                throw new RuntimeException("rmercator.dat is incorrect");
            }
        }
        return reverseMercatorValues;
    }

    public int getLatitude() {
        return this.latitudeE6;
    }

    private static int getLatitudeFromY(int y, Zoom zoom) {
        int[] data = getReverseMercatorValues();
        int mercator = pixelsToMercator((zoom.getEquatorPixels() / 2) - y, zoom);
        int indexFloor = (Math.abs(mercator) / 5000000) + 1;
        if (indexFloor < data.length - 2) {
            int yDiff = Math.abs(mercator) % 5000000;
            int latitude = (int) (((((((((((long) (((((data[indexFloor - 1] * -1) + (data[indexFloor] * 3)) - (data[indexFloor + 1] * 3)) + data[indexFloor + 2]) / 6)) * ((long) yDiff)) * ((long) yDiff)) / 5000000) * ((long) yDiff)) / 5000000) / 5000000) + ((((((long) ((((data[indexFloor - 1] * 3) - (data[indexFloor] * 6)) + (data[indexFloor + 1] * 3)) / 6)) * ((long) yDiff)) * ((long) yDiff)) / 5000000) / 5000000)) + ((((long) (((((data[indexFloor - 1] * -2) - (data[indexFloor] * 3)) + (data[indexFloor + 1] * 6)) - data[indexFloor + 2]) / 6)) * ((long) yDiff)) / 5000000)) + ((long) data[indexFloor]));
            if (latitude > 80000000) {
                latitude = 80000000;
            }
            if (mercator < 0) {
                latitude = -latitude;
            }
            return latitude;
        }
        return mercator <= 0 ? -80000000 : 80000000;
    }

    private static void readMercatorValues(DataInput dis, int[] values) throws IOException {
        values[0] = dis.readInt();
        for (int i = 1; i < values.length; i++) {
            values[i] = values[i - 1] + (((dis.readUnsignedByte() << 16) | (dis.readUnsignedByte() << 8)) | dis.readUnsignedByte());
        }
    }

    public int getLongitude() {
        return this.longitudeE6;
    }

    private static int getLongitudeFromX(int x, Zoom zoom) {
        return (int) ((((long) (x - (zoom.getEquatorPixels() / 2))) * 360000000) / ((long) zoom.getEquatorPixels()));
    }

    public String toString() {
        return latLonToString(this.latitudeE6, this.longitudeE6);
    }

    public static String latLonToString(int latitude, int longitude) {
        return TextUtil.e6ToString(latitude) + ',' + TextUtil.e6ToString(longitude);
    }

    public MapPoint pixelOffset(int xPixel, int yPixel, Zoom zoom) {
        int newX = zoom.changePixelsToTargetZoomlevel(xPixel, 22) + this.pixelCoordX;
        int newY = zoom.changePixelsToTargetZoomlevel(yPixel, 22) + this.pixelCoordY;
        int newLatitude = this.latitudeE6;
        if (yPixel != 0) {
            newLatitude = getLatitudeFromY(newY, PIXEL_COORDS_ZOOM);
        }
        int newLongitude = this.longitudeE6;
        if (xPixel != 0) {
            newLongitude += (int) ((((long) xPixel) * 360000000) / ((long) zoom.getEquatorPixels()));
        }
        return new MapPoint(newX, newY, 22, newLatitude, newLongitude);
    }

    private static int mercatorToPixelsTimesTen(int mercatorValue, Zoom zoom) {
        return (int) (((((long) mercatorValue) * ((long) zoom.getEquatorPixels())) * 10) / 360000000);
    }

    private static int getMercatorIndex(int latitude) {
        return (Math.abs(latitude) / 1000000) + 1;
    }

    private static int pixelsToMercator(int pixelValue, Zoom zoom) {
        return (int) ((((long) pixelValue) * 1000000000) / ((long) zoom.getEquatorPixels()));
    }

    public long distanceSquared(MapPoint point) {
        return ((((long) (this.latitudeE6 - point.latitudeE6)) * ((long) (this.latitudeE6 - point.latitudeE6))) / 100) + ((((long) (this.longitudeE6 - point.longitudeE6)) * ((long) (this.longitudeE6 - point.longitudeE6))) / 100);
    }

    public long pixelDistanceSquared(MapPoint point, Zoom zoom) {
        int xDiff = point.getXPixel(zoom) - getXPixel(zoom);
        int yDiff = point.getYPixel(zoom) - getYPixel(zoom);
        return (((long) xDiff) * ((long) xDiff)) + (((long) yDiff) * ((long) yDiff));
    }

    public static void writePoint(MapPoint point, DataOutput out) throws IOException {
        if (point == null) {
            out.writeInt(0);
            out.writeInt(0);
            return;
        }
        point.writePoint(out);
    }

    public MapPoint getDefiningPoint() {
        return this;
    }

    public static MapPoint readPoint(DataInput is) throws IOException {
        return new MapPoint(is.readInt(), is.readInt());
    }

    public static MapPoint getMapPointFromXY(int pixelX, int pixelY, int zoomLevel) {
        return new MapPoint(pixelX, pixelY, zoomLevel);
    }

    private void writePoint(DataOutput out) throws IOException {
        out.writeInt(this.latitudeE6);
        out.writeInt(this.longitudeE6);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (!(o instanceof MapPoint)) {
            return false;
        }
        MapPoint mapPoint = (MapPoint) o;
        if ((this.latitudeE6 == mapPoint.latitudeE6 && this.longitudeE6 == mapPoint.longitudeE6) || (this.pixelCoordX == mapPoint.pixelCoordX && this.pixelCoordY == mapPoint.pixelCoordY)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (this.latitudeE6 * 29) + this.longitudeE6;
    }
}
