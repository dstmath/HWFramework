package com.huawei.agpengine.impl;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import android.view.View;
import com.huawei.agpengine.TargetBuffer;
import com.huawei.agpengine.ViewHolder;

final class TextureViewHolderImpl implements ViewHolder {
    private static final String TAG = "core: TextureViewHolderImpl";
    private ViewHolder.SurfaceListener mListener;
    private final SurfaceTargetBufferImpl mSurfaceTargetBuffer = new SurfaceTargetBufferImpl();
    private TextureView mView;

    TextureViewHolderImpl(TextureView textureView, final AgpContextImpl agpContext) {
        this.mView = textureView;
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            /* class com.huawei.agpengine.impl.TextureViewHolderImpl.AnonymousClass1 */

            @Override // android.view.TextureView.SurfaceTextureListener
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                TextureViewHolderImpl.this.mSurfaceTargetBuffer.init(agpContext, surface);
                TextureViewHolderImpl.this.mSurfaceTargetBuffer.updateSize(width, height);
                if (TextureViewHolderImpl.this.mListener != null) {
                    TextureViewHolderImpl.this.mListener.onSurfaceAvailable();
                    TextureViewHolderImpl.this.mListener.onSurfaceSizeUpdated(width, height);
                }
            }

            @Override // android.view.TextureView.SurfaceTextureListener
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                TextureViewHolderImpl.this.mSurfaceTargetBuffer.updateSize(width, height);
                if (TextureViewHolderImpl.this.mListener != null) {
                    TextureViewHolderImpl.this.mListener.onSurfaceSizeUpdated(width, height);
                }
            }

            @Override // android.view.TextureView.SurfaceTextureListener
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                TextureViewHolderImpl.this.mSurfaceTargetBuffer.release();
                if (TextureViewHolderImpl.this.mListener == null) {
                    return true;
                }
                TextureViewHolderImpl.this.mListener.onSurfaceDestroyed();
                return true;
            }

            @Override // android.view.TextureView.SurfaceTextureListener
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
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
    public void setSurfaceListener(ViewHolder.SurfaceListener listener) {
        this.mListener = listener;
    }

    @Override // com.huawei.agpengine.ViewHolder
    public TargetBuffer getTargetBuffer() {
        return this.mSurfaceTargetBuffer;
    }

    @Override // com.huawei.agpengine.ViewHolder
    public void requestViewAsBitmap(Bitmap bitmap, ViewHolder.BitmapListener listener) {
        if (listener != null) {
            if (!this.mView.isAvailable()) {
                listener.onBitmapLoadError();
            }
            if (bitmap == null) {
                listener.onBitmapLoadDone(this.mView.getBitmap());
            } else {
                listener.onBitmapLoadDone(this.mView.getBitmap(bitmap));
            }
        }
    }

    @Override // com.huawei.agpengine.ViewHolder
    public void setOpaque(boolean isOpaque) {
        this.mView.setOpaque(isOpaque);
    }
}
