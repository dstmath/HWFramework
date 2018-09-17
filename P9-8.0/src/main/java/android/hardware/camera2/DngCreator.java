package android.hardware.camera2;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.camera2.impl.CameraMetadataNative;
import android.hardware.camera2.params.TonemapCurve;
import android.location.Location;
import android.media.Image;
import android.media.Image.Plane;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public final class DngCreator implements AutoCloseable {
    private static final int BYTES_PER_RGB_PIX = 3;
    private static final int DEFAULT_PIXEL_STRIDE = 2;
    private static final String GPS_DATE_FORMAT_STR = "yyyy:MM:dd";
    private static final String GPS_LAT_REF_NORTH = "N";
    private static final String GPS_LAT_REF_SOUTH = "S";
    private static final String GPS_LONG_REF_EAST = "E";
    private static final String GPS_LONG_REF_WEST = "W";
    public static final int MAX_THUMBNAIL_DIMENSION = 256;
    private static final String TAG = "DngCreator";
    private static final int TAG_ORIENTATION_UNKNOWN = 9;
    private static final String TIFF_DATETIME_FORMAT = "yyyy:MM:dd HH:mm:ss";
    private static final DateFormat sExifGPSDateStamp = new SimpleDateFormat(GPS_DATE_FORMAT_STR);
    private final Calendar mGPSTimeStampCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private long mNativeContext;

    private static native void nativeClassInit();

    private native synchronized void nativeDestroy();

    private native synchronized void nativeInit(CameraMetadataNative cameraMetadataNative, CameraMetadataNative cameraMetadataNative2, String str);

    private native synchronized void nativeSetDescription(String str);

    private native synchronized void nativeSetGpsTags(int[] iArr, String str, int[] iArr2, String str2, String str3, int[] iArr3);

    private native synchronized void nativeSetOrientation(int i);

    private native synchronized void nativeSetThumbnail(ByteBuffer byteBuffer, int i, int i2);

    private native synchronized void nativeWriteImage(OutputStream outputStream, int i, int i2, ByteBuffer byteBuffer, int i3, int i4, long j, boolean z) throws IOException;

    private native synchronized void nativeWriteInputStream(OutputStream outputStream, InputStream inputStream, int i, int i2, long j) throws IOException;

    public DngCreator(CameraCharacteristics characteristics, CaptureResult metadata) {
        if (characteristics == null || metadata == null) {
            throw new IllegalArgumentException("Null argument to DngCreator constructor");
        }
        long timeOffset;
        long currentTime = System.currentTimeMillis();
        int timestampSource = ((Integer) characteristics.get(CameraCharacteristics.SENSOR_INFO_TIMESTAMP_SOURCE)).intValue();
        if (timestampSource == 1) {
            timeOffset = currentTime - SystemClock.elapsedRealtime();
        } else if (timestampSource == 0) {
            timeOffset = currentTime - SystemClock.uptimeMillis();
        } else {
            Log.w(TAG, "Sensor timestamp source is unexpected: " + timestampSource);
            timeOffset = currentTime - SystemClock.uptimeMillis();
        }
        Long timestamp = (Long) metadata.get(CaptureResult.SENSOR_TIMESTAMP);
        long captureTime = currentTime;
        if (timestamp != null) {
            captureTime = (timestamp.longValue() / 1000000) + timeOffset;
        }
        DateFormat dateTimeStampFormat = new SimpleDateFormat(TIFF_DATETIME_FORMAT, Locale.US);
        dateTimeStampFormat.setTimeZone(TimeZone.getDefault());
        nativeInit(characteristics.getNativeCopy(), metadata.getNativeCopy(), dateTimeStampFormat.format(Long.valueOf(captureTime)));
    }

    public DngCreator setOrientation(int orientation) {
        if (orientation < 0 || orientation > 8) {
            throw new IllegalArgumentException("Orientation " + orientation + " is not a valid EXIF orientation value");
        }
        if (orientation == 0) {
            orientation = 9;
        }
        nativeSetOrientation(orientation);
        return this;
    }

    public DngCreator setThumbnail(Bitmap pixels) {
        if (pixels == null) {
            throw new IllegalArgumentException("Null argument to setThumbnail");
        }
        int width = pixels.getWidth();
        int height = pixels.getHeight();
        if (width > 256 || height > 256) {
            throw new IllegalArgumentException("Thumbnail dimensions width,height (" + width + "," + height + ") too large, dimensions must be smaller than " + 256);
        }
        nativeSetThumbnail(convertToRGB(pixels), width, height);
        return this;
    }

    public DngCreator setThumbnail(Image pixels) {
        if (pixels == null) {
            throw new IllegalArgumentException("Null argument to setThumbnail");
        }
        int format = pixels.getFormat();
        if (format != 35) {
            throw new IllegalArgumentException("Unsupported Image format " + format);
        }
        int width = pixels.getWidth();
        int height = pixels.getHeight();
        if (width > 256 || height > 256) {
            throw new IllegalArgumentException("Thumbnail dimensions width,height (" + width + "," + height + ") too large, dimensions must be smaller than " + 256);
        }
        nativeSetThumbnail(convertToRGB(pixels), width, height);
        return this;
    }

    public DngCreator setLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Null location passed to setLocation");
        }
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        long time = location.getTime();
        int[] latTag = toExifLatLong(latitude);
        int[] longTag = toExifLatLong(longitude);
        String latRef = latitude >= 0.0d ? GPS_LAT_REF_NORTH : GPS_LAT_REF_SOUTH;
        String longRef = longitude >= 0.0d ? GPS_LONG_REF_EAST : GPS_LONG_REF_WEST;
        String dateTag = sExifGPSDateStamp.format(Long.valueOf(time));
        this.mGPSTimeStampCalendar.setTimeInMillis(time);
        nativeSetGpsTags(latTag, latRef, longTag, longRef, dateTag, new int[]{this.mGPSTimeStampCalendar.get(11), 1, this.mGPSTimeStampCalendar.get(12), 1, this.mGPSTimeStampCalendar.get(13), 1});
        return this;
    }

    public DngCreator setDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("Null description passed to setDescription.");
        }
        nativeSetDescription(description);
        return this;
    }

    public void writeInputStream(OutputStream dngOutput, Size size, InputStream pixels, long offset) throws IOException {
        if (dngOutput == null) {
            throw new IllegalArgumentException("Null dngOutput passed to writeInputStream");
        } else if (size == null) {
            throw new IllegalArgumentException("Null size passed to writeInputStream");
        } else if (pixels == null) {
            throw new IllegalArgumentException("Null pixels passed to writeInputStream");
        } else if (offset < 0) {
            throw new IllegalArgumentException("Negative offset passed to writeInputStream");
        } else {
            int width = size.getWidth();
            int height = size.getHeight();
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("Size with invalid width, height: (" + width + "," + height + ") passed to writeInputStream");
            }
            nativeWriteInputStream(dngOutput, pixels, width, height, offset);
        }
    }

    public void writeByteBuffer(OutputStream dngOutput, Size size, ByteBuffer pixels, long offset) throws IOException {
        if (dngOutput == null) {
            throw new IllegalArgumentException("Null dngOutput passed to writeByteBuffer");
        } else if (size == null) {
            throw new IllegalArgumentException("Null size passed to writeByteBuffer");
        } else if (pixels == null) {
            throw new IllegalArgumentException("Null pixels passed to writeByteBuffer");
        } else if (offset < 0) {
            throw new IllegalArgumentException("Negative offset passed to writeByteBuffer");
        } else {
            int width = size.getWidth();
            writeByteBuffer(width, size.getHeight(), pixels, dngOutput, 2, width * 2, offset);
        }
    }

    public void writeImage(OutputStream dngOutput, Image pixels) throws IOException {
        if (dngOutput == null) {
            throw new IllegalArgumentException("Null dngOutput to writeImage");
        } else if (pixels == null) {
            throw new IllegalArgumentException("Null pixels to writeImage");
        } else {
            int format = pixels.getFormat();
            if (format != 32) {
                throw new IllegalArgumentException("Unsupported image format " + format);
            }
            Plane[] planes = pixels.getPlanes();
            if (planes == null || planes.length <= 0) {
                throw new IllegalArgumentException("Image with no planes passed to writeImage");
            }
            ByteBuffer buf = planes[0].getBuffer();
            writeByteBuffer(pixels.getWidth(), pixels.getHeight(), buf, dngOutput, planes[0].getPixelStride(), planes[0].getRowStride(), 0);
        }
    }

    public void close() {
        nativeDestroy();
    }

    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    static {
        sExifGPSDateStamp.setTimeZone(TimeZone.getTimeZone("UTC"));
        nativeClassInit();
    }

    private void writeByteBuffer(int width, int height, ByteBuffer pixels, OutputStream dngOutput, int pixelStride, int rowStride, long offset) throws IOException {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Image with invalid width, height: (" + width + "," + height + ") passed to write");
        }
        long capacity = (long) pixels.capacity();
        long totalSize = (((long) rowStride) * ((long) height)) + offset;
        if (capacity < totalSize) {
            throw new IllegalArgumentException("Image size " + capacity + " is too small (must be larger than " + totalSize + ")");
        }
        int minRowStride = pixelStride * width;
        if (minRowStride > rowStride) {
            throw new IllegalArgumentException("Invalid image pixel stride, row byte width " + minRowStride + " is too large, expecting " + rowStride);
        }
        pixels.clear();
        nativeWriteImage(dngOutput, width, height, pixels, rowStride, pixelStride, offset, pixels.isDirect());
        pixels.clear();
    }

    private static void yuvToRgb(byte[] yuvData, int outOffset, byte[] rgbOut) {
        float y = (float) (yuvData[0] & 255);
        float cb = (float) (yuvData[1] & 255);
        float cr = (float) (yuvData[2] & 255);
        float g = (y - ((cb - 128.0f) * 0.34414f)) - ((cr - 128.0f) * 0.71414f);
        float b = y + ((cb - 128.0f) * 1.772f);
        rgbOut[outOffset] = (byte) ((int) Math.max(TonemapCurve.LEVEL_BLACK, Math.min(255.0f, y + ((cr - 128.0f) * 1.402f))));
        rgbOut[outOffset + 1] = (byte) ((int) Math.max(TonemapCurve.LEVEL_BLACK, Math.min(255.0f, g)));
        rgbOut[outOffset + 2] = (byte) ((int) Math.max(TonemapCurve.LEVEL_BLACK, Math.min(255.0f, b)));
    }

    private static void colorToRgb(int color, int outOffset, byte[] rgbOut) {
        rgbOut[outOffset] = (byte) Color.red(color);
        rgbOut[outOffset + 1] = (byte) Color.green(color);
        rgbOut[outOffset + 2] = (byte) Color.blue(color);
    }

    private static ByteBuffer convertToRGB(Image yuvImage) {
        int width = yuvImage.getWidth();
        int height = yuvImage.getHeight();
        ByteBuffer buf = ByteBuffer.allocateDirect((width * 3) * height);
        Plane yPlane = yuvImage.getPlanes()[0];
        Plane uPlane = yuvImage.getPlanes()[1];
        Plane vPlane = yuvImage.getPlanes()[2];
        ByteBuffer yBuf = yPlane.getBuffer();
        ByteBuffer uBuf = uPlane.getBuffer();
        ByteBuffer vBuf = vPlane.getBuffer();
        yBuf.rewind();
        uBuf.rewind();
        vBuf.rewind();
        int yRowStride = yPlane.getRowStride();
        int vRowStride = vPlane.getRowStride();
        int uRowStride = uPlane.getRowStride();
        int yPixStride = yPlane.getPixelStride();
        int vPixStride = vPlane.getPixelStride();
        int uPixStride = uPlane.getPixelStride();
        byte[] bArr = new byte[3];
        bArr = new byte[]{(byte) 0, (byte) 0, (byte) 0};
        byte[] yFullRow = new byte[(((width - 1) * yPixStride) + 1)];
        byte[] uFullRow = new byte[((((width / 2) - 1) * uPixStride) + 1)];
        byte[] vFullRow = new byte[((((width / 2) - 1) * vPixStride) + 1)];
        byte[] finalRow = new byte[(width * 3)];
        for (int i = 0; i < height; i++) {
            int halfH = i / 2;
            yBuf.position(yRowStride * i);
            yBuf.get(yFullRow);
            uBuf.position(uRowStride * halfH);
            uBuf.get(uFullRow);
            vBuf.position(vRowStride * halfH);
            vBuf.get(vFullRow);
            for (int j = 0; j < width; j++) {
                int halfW = j / 2;
                bArr[0] = yFullRow[yPixStride * j];
                bArr[1] = uFullRow[uPixStride * halfW];
                bArr[2] = vFullRow[vPixStride * halfW];
                yuvToRgb(bArr, j * 3, finalRow);
            }
            buf.put(finalRow);
        }
        yBuf.rewind();
        uBuf.rewind();
        vBuf.rewind();
        buf.rewind();
        return buf;
    }

    private static ByteBuffer convertToRGB(Bitmap argbBitmap) {
        int width = argbBitmap.getWidth();
        int height = argbBitmap.getHeight();
        ByteBuffer buf = ByteBuffer.allocateDirect((width * 3) * height);
        int[] pixelRow = new int[width];
        byte[] finalRow = new byte[(width * 3)];
        for (int i = 0; i < height; i++) {
            argbBitmap.getPixels(pixelRow, 0, width, 0, i, width, 1);
            for (int j = 0; j < width; j++) {
                colorToRgb(pixelRow[j], j * 3, finalRow);
            }
            buf.put(finalRow);
        }
        buf.rewind();
        return buf;
    }

    private static int[] toExifLatLong(double value) {
        value = Math.abs(value);
        value = (value - ((double) ((int) value))) * 60.0d;
        int seconds = (int) ((value - ((double) ((int) value))) * 6000.0d);
        return new int[]{degrees, 1, (int) value, 1, seconds, 100};
    }
}
