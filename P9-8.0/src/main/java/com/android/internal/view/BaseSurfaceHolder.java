package com.android.internal.view;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.BadSurfaceTypeException;
import android.view.SurfaceHolder.Callback;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseSurfaceHolder implements SurfaceHolder {
    static final boolean DEBUG = false;
    private static final String TAG = "BaseSurfaceHolder";
    public final ArrayList<Callback> mCallbacks = new ArrayList();
    Callback[] mGottenCallbacks;
    boolean mHaveGottenCallbacks;
    long mLastLockTime = 0;
    protected int mRequestedFormat = -1;
    int mRequestedHeight = -1;
    int mRequestedType = -1;
    int mRequestedWidth = -1;
    public Surface mSurface = new Surface();
    final Rect mSurfaceFrame = new Rect();
    public final ReentrantLock mSurfaceLock = new ReentrantLock();
    Rect mTmpDirty;
    int mType = -1;

    public abstract boolean onAllowLockCanvas();

    public abstract void onRelayoutContainer();

    public abstract void onUpdateSurface();

    public int getRequestedWidth() {
        return this.mRequestedWidth;
    }

    public int getRequestedHeight() {
        return this.mRequestedHeight;
    }

    public int getRequestedFormat() {
        return this.mRequestedFormat;
    }

    public int getRequestedType() {
        return this.mRequestedType;
    }

    public void addCallback(Callback callback) {
        synchronized (this.mCallbacks) {
            if (!this.mCallbacks.contains(callback)) {
                this.mCallbacks.add(callback);
            }
        }
    }

    public void removeCallback(Callback callback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.remove(callback);
        }
    }

    public Callback[] getCallbacks() {
        if (this.mHaveGottenCallbacks) {
            return this.mGottenCallbacks;
        }
        synchronized (this.mCallbacks) {
            int N = this.mCallbacks.size();
            if (N > 0) {
                if (this.mGottenCallbacks == null || this.mGottenCallbacks.length != N) {
                    this.mGottenCallbacks = new Callback[N];
                }
                this.mCallbacks.toArray(this.mGottenCallbacks);
            } else {
                this.mGottenCallbacks = null;
            }
            this.mHaveGottenCallbacks = true;
        }
        return this.mGottenCallbacks;
    }

    public void ungetCallbacks() {
        this.mHaveGottenCallbacks = false;
    }

    public void setFixedSize(int width, int height) {
        if (this.mRequestedWidth != width || this.mRequestedHeight != height) {
            this.mRequestedWidth = width;
            this.mRequestedHeight = height;
            onRelayoutContainer();
        }
    }

    public void setSizeFromLayout() {
        if (this.mRequestedWidth != -1 || this.mRequestedHeight != -1) {
            this.mRequestedHeight = -1;
            this.mRequestedWidth = -1;
            onRelayoutContainer();
        }
    }

    public void setFormat(int format) {
        if (this.mRequestedFormat != format) {
            this.mRequestedFormat = format;
            onUpdateSurface();
        }
    }

    public void setType(int type) {
        switch (type) {
            case 1:
            case 2:
                type = 0;
                break;
        }
        switch (type) {
            case 0:
            case 3:
                if (this.mRequestedType != type) {
                    this.mRequestedType = type;
                    onUpdateSurface();
                    return;
                }
                return;
            default:
                return;
        }
    }

    public Canvas lockCanvas() {
        return internalLockCanvas(null, false);
    }

    public Canvas lockCanvas(Rect dirty) {
        return internalLockCanvas(dirty, false);
    }

    public Canvas lockHardwareCanvas() {
        return internalLockCanvas(null, true);
    }

    private final Canvas internalLockCanvas(Rect dirty, boolean hardware) {
        if (this.mType == 3) {
            throw new BadSurfaceTypeException("Surface type is SURFACE_TYPE_PUSH_BUFFERS");
        }
        this.mSurfaceLock.lock();
        Canvas c = null;
        if (onAllowLockCanvas()) {
            if (dirty == null) {
                if (this.mTmpDirty == null) {
                    this.mTmpDirty = new Rect();
                }
                this.mTmpDirty.set(this.mSurfaceFrame);
                dirty = this.mTmpDirty;
            }
            if (hardware) {
                try {
                    c = this.mSurface.lockHardwareCanvas();
                } catch (Exception e) {
                    Log.e(TAG, "Exception locking surface", e);
                }
            } else {
                c = this.mSurface.lockCanvas(dirty);
            }
        }
        if (c != null) {
            this.mLastLockTime = SystemClock.uptimeMillis();
            return c;
        }
        long now = SystemClock.uptimeMillis();
        long nextTime = this.mLastLockTime + 100;
        if (nextTime > now) {
            try {
                Thread.sleep(nextTime - now);
            } catch (InterruptedException e2) {
            }
            now = SystemClock.uptimeMillis();
        }
        this.mLastLockTime = now;
        this.mSurfaceLock.unlock();
        return null;
    }

    public void unlockCanvasAndPost(Canvas canvas) {
        this.mSurface.unlockCanvasAndPost(canvas);
        this.mSurfaceLock.unlock();
    }

    public Surface getSurface() {
        return this.mSurface;
    }

    public Rect getSurfaceFrame() {
        return this.mSurfaceFrame;
    }

    public void setSurfaceFrameSize(int width, int height) {
        this.mSurfaceFrame.top = 0;
        this.mSurfaceFrame.left = 0;
        this.mSurfaceFrame.right = width;
        this.mSurfaceFrame.bottom = height;
    }
}
