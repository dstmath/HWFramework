package android.graphics;

import android.content.res.AssetManager.AssetInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Movie {
    private long mNativeMovie;

    public static native Movie decodeByteArray(byte[] bArr, int i, int i2);

    private native void nDraw(long j, float f, float f2, long j2);

    private static native Movie nativeDecodeAsset(long j);

    private static native Movie nativeDecodeStream(InputStream inputStream);

    private static native void nativeDestructor(long j);

    public native int duration();

    public native int height();

    public native boolean isOpaque();

    public native boolean setTime(int i);

    public native int width();

    private Movie(long nativeMovie) {
        if (nativeMovie == 0) {
            throw new RuntimeException("native movie creation failed");
        }
        this.mNativeMovie = nativeMovie;
    }

    public void draw(Canvas canvas, float x, float y, Paint paint) {
        nDraw(canvas.getNativeCanvasWrapper(), x, y, paint != null ? paint.getNativeInstance() : 0);
    }

    public void draw(Canvas canvas, float x, float y) {
        nDraw(canvas.getNativeCanvasWrapper(), x, y, 0);
    }

    public static Movie decodeStream(InputStream is) {
        if (is == null) {
            return null;
        }
        if (is instanceof AssetInputStream) {
            return nativeDecodeAsset(((AssetInputStream) is).getNativeAsset());
        }
        return nativeDecodeStream(is);
    }

    public static Movie decodeFile(String pathName) {
        try {
            return decodeTempStream(new FileInputStream(pathName));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    protected void finalize() throws Throwable {
        try {
            nativeDestructor(this.mNativeMovie);
            this.mNativeMovie = 0;
        } finally {
            super.finalize();
        }
    }

    private static Movie decodeTempStream(InputStream is) {
        Movie movie = null;
        try {
            movie = decodeStream(is);
            is.close();
            return movie;
        } catch (IOException e) {
            return movie;
        }
    }
}
