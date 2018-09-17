package android_maps_conflict_avoidance.com.google.common.graphics.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleGraphics;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;
import java.util.Map;

public class AndroidImage implements GoogleImage {
    private static volatile int bitmapCount = 0;
    private volatile Bitmap bitmap;
    private final boolean isOriginal;
    private boolean pinned;

    public enum AutoScale {
        AUTO_SCALE_ENABLED,
        AUTO_SCALE_DISABLED
    }

    public AndroidImage(byte[] imageData, int imageOffset, int imageLength) {
        this.pinned = false;
        this.bitmap = BitmapFactory.decodeByteArray(imageData, imageOffset, imageLength);
        if (this.bitmap != null) {
            Class cls = AndroidImage.class;
            synchronized (AndroidImage.class) {
                bitmapCount++;
                this.isOriginal = true;
                return;
            }
        }
        throw new IllegalStateException("Null Bitmap!");
    }

    public AndroidImage(int width, int height) {
        this(width, height, true);
    }

    public AndroidImage(int width, int height, boolean processAlpha) {
        this.pinned = false;
        this.bitmap = Bitmap.createBitmap(width, height, !processAlpha ? Config.RGB_565 : Config.ARGB_8888);
        if (this.bitmap != null) {
            Class cls = AndroidImage.class;
            synchronized (AndroidImage.class) {
                bitmapCount++;
                this.isOriginal = true;
                return;
            }
        }
        throw new IllegalStateException("Null Bitmap!");
    }

    public AndroidImage(Bitmap bitmap) {
        this.pinned = false;
        this.bitmap = bitmap;
        this.isOriginal = false;
    }

    public AndroidImage(Context context, Map<String, Integer> stringIdMap, String name, AutoScale autoScale) {
        this.pinned = false;
        String cleanName = cleanName(name);
        if (stringIdMap != null) {
            Integer resourceId = (Integer) stringIdMap.get(cleanName);
            if (resourceId != null) {
                Options options = null;
                if (autoScale == AutoScale.AUTO_SCALE_DISABLED) {
                    options = new Options();
                    options.inScaled = false;
                }
                this.bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId.intValue(), options);
                if (autoScale == AutoScale.AUTO_SCALE_DISABLED && this.bitmap != null) {
                    this.bitmap.setDensity(android_maps_conflict_avoidance.com.google.common.Config.getInstance().getPixelsPerInch());
                }
            }
        }
        if (this.bitmap == null) {
            this.bitmap = BitmapFactory.decodeFile(name);
        }
        if (this.bitmap != null) {
            Class cls = AndroidImage.class;
            synchronized (AndroidImage.class) {
                bitmapCount++;
                this.isOriginal = true;
                return;
            }
        }
        throw new IllegalStateException("Null Bitmap! \"" + name + "\"; if seen during a test, " + "this usually means that the image file needs to be added to the test.config file");
    }

    private static String cleanName(String name) {
        if (name.indexOf("/") == 0) {
            name = name.substring(1);
        }
        int dotIndex = name.indexOf(".");
        if (dotIndex <= 0) {
            return name;
        }
        return name.substring(0, dotIndex);
    }

    public void pin() {
        this.pinned = true;
    }

    public void recycle() {
        if (!this.pinned && this.bitmap != null) {
            this.bitmap.recycle();
            this.bitmap = null;
        }
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public int getWidth() {
        return this.bitmap.getWidth();
    }

    public int getHeight() {
        return this.bitmap.getHeight();
    }

    public GoogleGraphics getGraphics() {
        return new AndroidGraphics(new Canvas(this.bitmap));
    }

    public GoogleImage createScaledImage(int srcX, int srcY, int srcWidth, int srcHeight, int newWidth, int newHeight) {
        ScaledAndroidImage image = new ScaledAndroidImage(this, newWidth, newHeight, srcX, srcY, srcWidth, srcHeight);
        if (newWidth * newHeight < 4096) {
            image.getGraphics();
        }
        return image;
    }

    public void drawImage(GoogleGraphics g, int x, int y) {
        ((AndroidGraphics) g).getCanvas().drawBitmap(this.bitmap, (float) x, (float) y, null);
    }

    protected void finalize() throws Throwable {
        compact();
        super.finalize();
    }

    private void compact() {
        if (this.isOriginal) {
            Bitmap b;
            synchronized (this) {
                b = this.bitmap;
                this.bitmap = null;
            }
            if (b != null) {
                Class cls = AndroidImage.class;
                synchronized (AndroidImage.class) {
                    bitmapCount--;
                    if (bitmapCount >= 0) {
                    } else {
                        throw new IllegalStateException();
                    }
                }
            }
            return;
        }
        this.bitmap = null;
    }
}
