package android.widget.sr;

import android.graphics.Bitmap;

public class NativeBitmap {
    private Bitmap mBitmap;
    public int mFd;
    public long mPtr;

    public NativeBitmap(Bitmap bitmap, long ptr, int fd) {
        this.mBitmap = bitmap;
        this.mPtr = ptr;
        this.mFd = fd;
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public long getPtr() {
        return this.mPtr;
    }

    public int getFd() {
        return this.mFd;
    }

    public int getWidth() {
        Bitmap bitmap = this.mBitmap;
        if (bitmap == null) {
            return -1;
        }
        return bitmap.getWidth();
    }

    public int getHeight() {
        Bitmap bitmap = this.mBitmap;
        if (bitmap == null) {
            return -1;
        }
        return bitmap.getHeight();
    }
}
