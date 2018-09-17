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
        if (this.mBitmap == null) {
            return -1;
        }
        return this.mBitmap.getWidth();
    }

    public int getHeight() {
        if (this.mBitmap == null) {
            return -1;
        }
        return this.mBitmap.getHeight();
    }
}
