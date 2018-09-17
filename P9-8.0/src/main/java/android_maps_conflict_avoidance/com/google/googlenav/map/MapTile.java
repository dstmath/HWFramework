package android_maps_conflict_avoidance.com.google.googlenav.map;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.Log;
import android_maps_conflict_avoidance.com.google.common.StaticUtil;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleGraphics;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;
import android_maps_conflict_avoidance.com.google.common.graphics.ImageFactory;
import android_maps_conflict_avoidance.com.google.common.io.Gunzipper;
import android_maps_conflict_avoidance.com.google.common.io.protocol.ProtoBuf;
import android_maps_conflict_avoidance.com.google.common.util.ConversionUtil;
import android_maps_conflict_avoidance.com.google.googlenav.layer.ClickableArea;
import android_maps_conflict_avoidance.com.google.googlenav.proto.GmmMessageTypes;
import android_maps_conflict_avoidance.com.google.image.compression.jpeg.JpegUtil;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Hashtable;

public class MapTile {
    private static final byte[] CJPG_HEADER = new byte[]{(byte) 67, (byte) 74, (byte) 80, (byte) 71};
    protected static final byte[] LAYER_DATA_HEADER = new byte[]{(byte) 76, (byte) 84, (byte) 73, (byte) 80, (byte) 10};
    private static GoogleImage loadingImage;
    private static GoogleImage notAvailableImage;
    private static GoogleImage notLoadingImage;
    private static int textSize = 1;
    private static final Hashtable unicolorTiles = new Hashtable();
    private GoogleImage baseMapImage;
    private int completePaintCount;
    private byte[] data;
    private long firstPaintTime;
    private boolean hasScaledImage;
    private int imageVersion;
    private boolean isBaseMapImageRecyclable;
    private boolean isMapImageRecyclable;
    private boolean isPreCached;
    private final boolean isTemp;
    private long lastAccessTime;
    private long lastPaintTime;
    private LayerTile layerTile;
    private final Tile location;
    private GoogleImage mapImage;
    private boolean requested;

    public MapTile(Tile location, GoogleImage tempImage) {
        this(location, tempImage, false);
    }

    public MapTile(Tile location, GoogleImage tempImage, boolean isTemp) {
        this.requested = false;
        this.isMapImageRecyclable = false;
        this.isBaseMapImageRecyclable = false;
        verifyTileDimensions(tempImage);
        this.location = location;
        this.isTemp = isTemp;
        setMapImage(tempImage, false);
        this.hasScaledImage = tempImage != null;
        this.lastAccessTime = 0;
        this.imageVersion = 0;
    }

    public MapTile(Tile location, byte[] imageData) {
        this.requested = false;
        this.isMapImageRecyclable = false;
        this.isBaseMapImageRecyclable = false;
        this.location = location;
        this.isTemp = false;
        this.lastAccessTime = 0;
        this.hasScaledImage = false;
        setData(imageData);
    }

    public void removeScaledImage() {
        if (this.hasScaledImage) {
            this.hasScaledImage = false;
            setMapImage(null, false);
        }
    }

    public Tile getLocation() {
        return this.location;
    }

    /* JADX WARNING: Missing block: B:13:0x001b, code:
            if (getRequested() != false) goto L_0x0015;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void compact() {
        if (!isComplete()) {
        }
        if (this.mapImage != null) {
            setMapImage(null, false);
        }
        if (this.baseMapImage != null) {
            setBaseMapImage(null, false);
        }
        this.imageVersion = 0;
        this.layerTile = null;
    }

    public boolean isComplete() {
        return this.data != null;
    }

    public boolean hasImage() {
        return this.mapImage != null;
    }

    public long getLastAccessTime() {
        return this.lastAccessTime;
    }

    public final void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public void setPaint(long mapPaintTime, long lastMapPaintTime) {
        Object obj = null;
        if (this.completePaintCount == 0 && this.lastPaintTime != lastMapPaintTime) {
            if (mapPaintTime - this.lastPaintTime <= 2000) {
                obj = 1;
            }
            if (obj == null) {
                this.firstPaintTime = 0;
            }
        }
        if (this.firstPaintTime == 0) {
            this.firstPaintTime = mapPaintTime;
        }
        this.lastPaintTime = mapPaintTime;
        if (hasImage() && !this.hasScaledImage && this.completePaintCount < Integer.MAX_VALUE) {
            this.completePaintCount++;
        }
    }

    public long getFirstPaintTime() {
        return this.firstPaintTime;
    }

    public int getCompletePaintCount() {
        return this.completePaintCount;
    }

    public int getDataSize() {
        return this.data == null ? 0 : this.data.length;
    }

    /* JADX WARNING: Missing block: B:17:0x0034, code:
            if (r6.hasScaledImage == false) goto L_0x0030;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected synchronized void createImage() {
        boolean isRecyclable = false;
        synchronized (this) {
            GoogleImage createdImage;
            if (this.mapImage != null) {
            }
            byte[] imageData = extractLayerTileAndImageData();
            if (imageData.length == 0) {
                createdImage = getNotAvailableImage();
                isRecyclable = false;
            } else if (imageData.length == 3) {
                createdImage = getImageFromUnicolor(imageData);
                isRecyclable = false;
            } else if (equalBytes(imageData, 0, CJPG_HEADER)) {
                createdImage = getImageFromCjpg(imageData);
                if (createdImage != notAvailableImage) {
                    isRecyclable = true;
                }
            } else {
                createdImage = Config.getInstance().getImageFactory().createImage(imageData, 0, imageData.length);
                isRecyclable = true;
            }
            setImage(createdImage, 0, isRecyclable);
            this.hasScaledImage = false;
        }
    }

    private byte[] extractLayerTileAndImageData() {
        byte[] imageData = this.data;
        if (equalBytes(this.data, 0, LAYER_DATA_HEADER)) {
            try {
                int offset = LAYER_DATA_HEADER.length;
                byte[] sizeBytes = new byte[4];
                System.arraycopy(this.data, offset, sizeBytes, 0, sizeBytes.length);
                int size = ConversionUtil.byteArrayToInt(sizeBytes);
                int layerDataSize = Math.abs(size);
                offset += 4;
                InputStream is = new ByteArrayInputStream(this.data, offset, layerDataSize);
                offset += layerDataSize;
                imageData = new byte[(this.data.length - offset)];
                System.arraycopy(this.data, offset, imageData, 0, imageData.length);
                if (size < 0) {
                    is = Gunzipper.gunzip(is);
                }
                ProtoBuf tileInfo = new ProtoBuf(GmmMessageTypes.LAYER_TILE_INFO_PROTO);
                tileInfo.parse(is);
                int areasNum = tileInfo.getCount(3);
                ClickableArea[] areas = new ClickableArea[areasNum];
                for (int j = 0; j < areasNum; j++) {
                    areas[j] = new ClickableArea(tileInfo.getProtoBuf(3, j));
                }
                this.layerTile = new LayerTile(this.location);
                this.layerTile.setLayerTileData(areas);
            } catch (IOException e) {
                this.layerTile = null;
                Log.logQuietThrowable("IOException reading layer data", e);
            }
        }
        return imageData;
    }

    public void setImage(GoogleImage image, int imageVersion, boolean isRecyclable) {
        verifyTileDimensions(image);
        setMapImage(image, isRecyclable);
        setImageVersion(imageVersion);
    }

    public void setImageVersion(int imageVersion) {
        if (imageVersion == 0) {
            setBaseMapImage(this.mapImage, this.isMapImageRecyclable);
            this.isMapImageRecyclable = false;
        }
        this.imageVersion = imageVersion;
    }

    private void setMapImage(GoogleImage image, boolean isRecyclable) {
        if (this.mapImage != null && this.isMapImageRecyclable) {
            this.mapImage.recycle();
        }
        this.mapImage = image;
        this.isMapImageRecyclable = isRecyclable;
    }

    private void setBaseMapImage(GoogleImage image, boolean isRecyclable) {
        if (this.baseMapImage != null && this.isBaseMapImageRecyclable) {
            this.baseMapImage.recycle();
        }
        this.baseMapImage = image;
        this.isBaseMapImageRecyclable = isRecyclable;
    }

    private static void verifyTileDimensions(GoogleImage image) {
        if (image != null) {
            if (image.getHeight() != 256 || image.getWidth() != 256) {
                throw new IllegalArgumentException("wrong image size: " + image.getWidth() + " " + image.getHeight());
            }
        }
    }

    public void restoreBaseImage() {
        setMapImage(this.baseMapImage, false);
        this.imageVersion = 0;
    }

    public int getImageVersion() {
        return this.imageVersion;
    }

    public boolean hasScaledImage() {
        return this.hasScaledImage;
    }

    public GoogleImage getImage() {
        return getImage(Long.MIN_VALUE);
    }

    /* JADX WARNING: Missing block: B:22:0x0033, code:
            if (r9.hasScaledImage == false) goto L_0x0021;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public GoogleImage getImage(long accessTime) {
        GoogleImage returnValue;
        boolean handleOutOfMemory = false;
        synchronized (this) {
            if (accessTime == Long.MIN_VALUE) {
                this.lastAccessTime = Config.getInstance().getClock().currentTimeMillis();
            } else {
                this.lastAccessTime = accessTime;
            }
            if (this.mapImage != null) {
            }
            if (isComplete()) {
                try {
                    createImage();
                } catch (OutOfMemoryError e) {
                    handleOutOfMemory = true;
                }
            }
            if (this.mapImage != null) {
                returnValue = this.mapImage;
            } else {
                returnValue = getTempImage();
            }
        }
        if (handleOutOfMemory) {
            StaticUtil.handleOutOfMemory();
        }
        return returnValue;
    }

    private GoogleImage getTempImage() {
        if (notLoadingImage == null || loadingImage == null) {
            createTempImages();
        }
        return !this.isTemp ? loadingImage : notLoadingImage;
    }

    public void write(DataOutput dos) throws IOException {
        this.location.write(dos);
        dos.writeShort(this.data.length);
        dos.write(this.data);
    }

    public static MapTile read(DataInput dis) throws IOException {
        Tile location = Tile.read(dis);
        byte[] data = new byte[dis.readUnsignedShort()];
        dis.readFully(data);
        return new MapTile(location, data);
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (!(o instanceof MapTile)) {
            return false;
        }
        MapTile imageTile = (MapTile) o;
        if (this.location != null) {
            z = this.location.equals(imageTile.location);
        } else if (imageTile.location == null) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.location == null ? 0 : this.location.hashCode();
    }

    public String toString() {
        return this.location.toString() + (this.data == null ? "B?" : "B" + this.data.length);
    }

    public synchronized void setData(byte[] data) {
        if (isComplete()) {
            throw new RuntimeException("Tile already complete");
        }
        this.imageVersion = 0;
        this.data = data;
        setBaseMapImage(null, false);
        if (!this.hasScaledImage) {
            setMapImage(null, false);
        }
    }

    public boolean hasRenderedImage() {
        return (this.imageVersion == 0 || this.baseMapImage == this.mapImage) ? false : true;
    }

    private static void createTempImages() {
        ImageFactory factory = Config.getInstance().getImageFactory();
        try {
            loadingImage = factory.createUnscaledImage("/loading_tile_android.png");
            notLoadingImage = loadingImage;
        } catch (IOException e) {
            GoogleImage createImage = factory.createImage(256, 256, false);
            notLoadingImage = createImage;
            loadingImage = createImage;
            Log.logThrowable("MAP", e);
        }
    }

    private static boolean equalBytes(byte[] src, int srcOffset, byte[] reference) {
        if (src.length < reference.length + srcOffset) {
            return false;
        }
        for (int i = 0; i < reference.length; i++) {
            if (src[srcOffset + i] != reference[i]) {
                return false;
            }
        }
        return true;
    }

    private static GoogleImage getNotAvailableImage() {
        ImageFactory factory = Config.getInstance().getImageFactory();
        if (notAvailableImage == null) {
            try {
                notAvailableImage = factory.createUnscaledImage("/no_tile_256.png");
            } catch (IOException e) {
                notAvailableImage = factory.createImage(256, 256);
                Log.logThrowable("MAP", e);
            }
        }
        return notAvailableImage;
    }

    private static GoogleImage getImageFromUnicolor(byte[] unicolorData) {
        if (unicolorData.length < 3) {
            return getNotAvailableImage();
        }
        int blue = unicolorData[2] & 255;
        int color = (((unicolorData[0] & 255) << 16) | ((unicolorData[1] & 255) << 8)) | blue;
        Integer colorKey = new Integer(color);
        WeakReference ref = (WeakReference) unicolorTiles.get(colorKey);
        if (ref != null) {
            GoogleImage image = (GoogleImage) ref.get();
            if (image != null) {
                return image;
            }
        }
        GoogleImage img = Config.getInstance().getImageFactory().createImage(256, 256, false);
        GoogleGraphics gcs = img.getGraphics();
        gcs.setColor(color);
        gcs.fillRect(0, 0, 256, 256);
        unicolorTiles.put(colorKey, new WeakReference(img));
        return img;
    }

    private static GoogleImage getImageFromCjpg(byte[] data) {
        try {
            data = JpegUtil.uncompactJpeg(data);
            return Config.getInstance().getImageFactory().createImage(data, 0, data.length);
        } catch (IllegalArgumentException e) {
            Log.logThrowable("MAP", e);
            return getNotAvailableImage();
        }
    }

    public boolean getIsPreCached() {
        return this.isPreCached;
    }

    public void setRequested(boolean requested) {
        this.requested = requested;
    }

    public boolean getRequested() {
        return this.requested;
    }

    public static void setTextSize(int desiredTextSize) {
        textSize = desiredTextSize;
    }

    public static int getTextSize() {
        return textSize;
    }
}
