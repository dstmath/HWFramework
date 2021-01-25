package com.huawei.agpengine.impl;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import com.huawei.agpengine.TargetBuffer;
import com.huawei.agpengine.ViewHolder;

final class SurfaceViewHolderImpl implements ViewHolder {
    private static final String TAG = "core: SurfaceViewHolderImpl";
    private ViewHolder.SurfaceListener mListener;
    private final SurfaceTargetBufferImpl mSurfaceTargetBuffer = new SurfaceTargetBufferImpl();
    private SurfaceView mView;

    SurfaceViewHolderImpl(SurfaceView surfaceView, final AgpContextImpl agpContext) {
        this.mView = surfaceView;
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback2() {
            /* class com.huawei.agpengine.impl.SurfaceViewHolderImpl.AnonymousClass1 */

            @Override // android.view.SurfaceHolder.Callback
            public void surfaceCreated(SurfaceHolder holder) {
                SurfaceViewHolderImpl.this.mSurfaceTargetBuffer.init(agpContext, holder.getSurface());
                if (SurfaceViewHolderImpl.this.mListener != null) {
                    SurfaceViewHolderImpl.this.mListener.onSurfaceAvailable();
                }
            }

            @Override // android.view.SurfaceHolder.Callback
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                SurfaceViewHolderImpl.this.mSurfaceTargetBuffer.updateSize(width, height);
                if (SurfaceViewHolderImpl.this.mListener != null) {
                    SurfaceViewHolderImpl.this.mListener.onSurfaceSizeUpdated(width, height);
                }
            }

            @Override // android.view.SurfaceHolder.Callback
            public void surfaceDestroyed(SurfaceHolder holder) {
                SurfaceViewHolderImpl.this.mSurfaceTargetBuffer.release();
                if (SurfaceViewHolderImpl.this.mListener != null) {
                    SurfaceViewHolderImpl.this.mListener.onSurfaceDestroyed();
                }
            }

            @Override // android.view.SurfaceHolder.Callback2
            public void surfaceRedrawNeeded(SurfaceHolder holder) {
            }
        });
    }

    @Override // com.huawei.agpengine.ViewHolder
    public View getView() {
        return this.mView;
    }

    @Override // com.huawei.agpengine.ViewHolder
    public void release() {
        this.mSurfaceTargetBuffer.release();
        ViewHolder.SurfaceListener surfaceListener = this.mListener;
        if (surfaceListener != null) {
            surfaceListener.onSurfaceDestroyed();
        }
        this.mListener = null;
        this.mView = null;
    }

    @Override // com.huawei.agpengine.ViewHolder
    public TargetBuffer getTargetBuffer() {
        return this.mSurfaceTargetBuffer;
    }

    @Override // com.huawei.agpengine.ViewHolder
    public void requestViewAsBitmap(Bitmap bitmap, ViewHolder.BitmapListener listener) {
        Bitmap result;
        if (listener != null) {
            if (!this.mView.getHolder().getSurface().isValid()) {
                listener.onBitmapLoadError();
                return;
            }
            int width = this.mView.getWidth();
            int height = this.mView.getHeight();
            if (width == 0 || height == 0) {
                listener.onBitmapLoadError();
                return;
            }
            if (bitmap != null) {
                result = bitmap;
            } else {
                result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            }
            HandlerThread handlerThread = new HandlerThread("requestViewAsBitmap");
            handlerThread.start();
            Looper handlerLooper = handlerThread.getLooper();
            if (handlerLooper != null) {
                PixelCopy.request(this.mView, result, new PixelCopy.OnPixelCopyFinishedListener(result, handlerThread) {
                    /* class com.huawei.agpengine.impl.$$Lambda$SurfaceViewHolderImpl$X8mYkDrUBqj65gpGWCc8MwwN8o */
                    private final /* synthetic */ Bitmap f$1;
                    private final /* synthetic */ HandlerThread f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // android.view.PixelCopy.OnPixelCopyFinishedListener
                    public final void onPixelCopyFinished(int i) {
                        SurfaceViewHolderImpl.lambda$requestViewAsBitmap$0(ViewHolder.BitmapListener.this, this.f$1, this.f$2, i);
                    }
                }, new Handler(handlerLooper));
            } else {
                handlerThread.quitSafely();
            }
        }
    }

    static /* synthetic */ void lambda$requestViewAsBitmap$0(ViewHolder.BitmapListener listener, Bitmap result, HandlerThread handlerThread, int copyResult) {
        if (copyResult == 0) {
            listener.onBitmapLoadDone(result);
        } else {
            listener.onBitmapLoadError();
        }
        handlerThread.quitSafely();
    }

    @Override // com.huawei.agpengine.ViewHolder
    public void setOpaque(boolean isOpaque) {
        if (isOpaque) {
            this.mView.getHolder().setFormat(-1);
        } else {
            this.mView.getHolder().setFormat(-3);
        }
    }

    @Override // com.huawei.agpengine.ViewHolder
    public void setSurfaceListener(ViewHolder.SurfaceListener listener) {
        this.mListener = listener;
    }
}
