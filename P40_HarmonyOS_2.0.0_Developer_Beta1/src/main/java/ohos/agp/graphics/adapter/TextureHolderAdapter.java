package ohos.agp.graphics.adapter;

import android.graphics.SurfaceTexture;

public class TextureHolderAdapter {
    private FrameAvailableListener mFrameAvailableListener;
    private final OnFrameAvailableListenerInternal mFrameCallback = new OnFrameAvailableListenerInternal();
    private SurfaceTexture mSurfaceTexture;

    public interface FrameAvailableListener {
        void onFrameAvailable();
    }

    public TextureHolderAdapter(int i) {
        this.mSurfaceTexture = new SurfaceTexture(i);
    }

    /* access modifiers changed from: private */
    public class OnFrameAvailableListenerInternal implements SurfaceTexture.OnFrameAvailableListener {
        private OnFrameAvailableListenerInternal() {
        }

        @Override // android.graphics.SurfaceTexture.OnFrameAvailableListener
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            if (TextureHolderAdapter.this.mFrameAvailableListener != null) {
                TextureHolderAdapter.this.mFrameAvailableListener.onFrameAvailable();
            }
        }
    }

    public void setOnFrameAvailableListener(FrameAvailableListener frameAvailableListener) {
        this.mFrameAvailableListener = frameAvailableListener;
        if (frameAvailableListener == null) {
            this.mSurfaceTexture.setOnFrameAvailableListener(null);
        } else {
            this.mSurfaceTexture.setOnFrameAvailableListener(this.mFrameCallback);
        }
    }

    public void attachToGPUContext(int i) {
        this.mSurfaceTexture.attachToGLContext(i);
    }

    public void detachFromGPUContext() {
        this.mSurfaceTexture.detachFromGLContext();
    }

    public void releaseTextureImage() {
        this.mSurfaceTexture.releaseTexImage();
    }

    public void updateTextureImage() {
        this.mSurfaceTexture.updateTexImage();
    }

    public void setDefaultBufferDimension(int i, int i2) {
        this.mSurfaceTexture.setDefaultBufferSize(i, i2);
    }

    public void getTransform(float[] fArr) {
        this.mSurfaceTexture.getTransformMatrix(fArr);
    }

    public void abandon() {
        this.mSurfaceTexture.release();
    }
}
