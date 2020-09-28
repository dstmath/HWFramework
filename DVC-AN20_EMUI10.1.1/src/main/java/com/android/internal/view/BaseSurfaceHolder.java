package com.android.internal.view;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseSurfaceHolder implements SurfaceHolder {
    static final boolean DEBUG = false;
    private static final String TAG = "BaseSurfaceHolder";
    public final ArrayList<SurfaceHolder.Callback> mCallbacks = new ArrayList<>();
    SurfaceHolder.Callback[] mGottenCallbacks;
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

    @Override // android.view.SurfaceHolder
    public void addCallback(SurfaceHolder.Callback callback) {
        synchronized (this.mCallbacks) {
            if (!this.mCallbacks.contains(callback)) {
                this.mCallbacks.add(callback);
            }
        }
    }

    @Override // android.view.SurfaceHolder
    public void removeCallback(SurfaceHolder.Callback callback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.remove(callback);
        }
    }

    public SurfaceHolder.Callback[] getCallbacks() {
        if (this.mHaveGottenCallbacks) {
            return this.mGottenCallbacks;
        }
        synchronized (this.mCallbacks) {
            int N = this.mCallbacks.size();
            if (N > 0) {
                if (this.mGottenCallbacks == null || this.mGottenCallbacks.length != N) {
                    this.mGottenCallbacks = new SurfaceHolder.Callback[N];
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

    @Override // android.view.SurfaceHolder
    public void setFixedSize(int width, int height) {
        if (this.mRequestedWidth != width || this.mRequestedHeight != height) {
            this.mRequestedWidth = width;
            this.mRequestedHeight = height;
            onRelayoutContainer();
        }
    }

    @Override // android.view.SurfaceHolder
    public void setSizeFromLayout() {
        if (this.mRequestedWidth != -1 || this.mRequestedHeight != -1) {
            this.mRequestedHeight = -1;
            this.mRequestedWidth = -1;
            onRelayoutContainer();
        }
    }

    @Override // android.view.SurfaceHolder
    public void setFormat(int format) {
        if (this.mRequestedFormat != format) {
            this.mRequestedFormat = format;
            onUpdateSurface();
        }
    }

    @Override // android.view.SurfaceHolder
    public void setType(int type) {
        if (type == 1 || type == 2) {
            type = 0;
        }
        if ((type == 0 || type == 3) && this.mRequestedType != type) {
            this.mRequestedType = type;
            onUpdateSurface();
        }
    }

    @Override // android.view.SurfaceHolder
    public Canvas lockCanvas() {
        return internalLockCanvas(null, false);
    }

    @Override // android.view.SurfaceHolder
    public Canvas lockCanvas(Rect dirty) {
        return internalLockCanvas(dirty, false);
    }

    @Override // android.view.SurfaceHolder
    public Canvas lockHardwareCanvas() {
        return internalLockCanvas(null, true);
    }

    private final Canvas internalLockCanvas(Rect dirty, boolean hardware) {
        if (this.mType != 3) {
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
        throw new SurfaceHolder.BadSurfaceTypeException("Surface type is SURFACE_TYPE_PUSH_BUFFERS");
    }

    @Override // android.view.SurfaceHolder
    public void unlockCanvasAndPost(Canvas canvas) {
        this.mSurface.unlockCanvasAndPost(canvas);
        this.mSurfaceLock.unlock();
    }

    @Override // android.view.SurfaceHolder
    public Surface getSurface() {
        return this.mSurface;
    }

    @Override // android.view.SurfaceHolder
    public Rect getSurfaceFrame() {
        return this.mSurfaceFrame;
    }

    public void setSurfaceFrameSize(int width, int height) {
        Rect rect = this.mSurfaceFrame;
        rect.top = 0;
        rect.left = 0;
        rect.right = width;
        rect.bottom = height;
    }
}
