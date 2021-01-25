package ohos.agp.graphics;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.graphics.adapter.TextureHolderAdapter;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class TextureHolder {
    private static final HiLogLabel LOG_TAG = new HiLogLabel(3, (int) LogDomain.END, "TextureHolder");
    private AdapterFrameAvailableListener mAdapterFrameAvailableListener = new AdapterFrameAvailableListener();
    private OnNewFrameCallback mOnFrameAvailableCallback;
    private TextureHolderAdapter mTextureHolderAdapter;

    public interface OnNewFrameCallback {
        void onNewFrame(TextureHolder textureHolder);
    }

    private class AdapterFrameAvailableListener implements TextureHolderAdapter.FrameAvailableListener {
        private AdapterFrameAvailableListener() {
        }

        @Override // ohos.agp.graphics.adapter.TextureHolderAdapter.FrameAvailableListener
        public void onFrameAvailable() {
            if (TextureHolder.this.mOnFrameAvailableCallback != null) {
                TextureHolder.this.mOnFrameAvailableCallback.onNewFrame(TextureHolder.this);
            }
        }
    }

    public TextureHolder(int i) {
        this.mTextureHolderAdapter = new TextureHolderAdapter(i);
    }

    public void bindToGPUContext(int i) {
        this.mTextureHolderAdapter.attachToGPUContext(i);
    }

    public void unbindFromGPUContext() {
        this.mTextureHolderAdapter.detachFromGPUContext();
    }

    public void freeTextureImage() {
        this.mTextureHolderAdapter.releaseTextureImage();
    }

    public void refreshTextureImage() {
        this.mTextureHolderAdapter.updateTextureImage();
    }

    public void setBufferDimension(int i, int i2) {
        this.mTextureHolderAdapter.setDefaultBufferDimension(i, i2);
    }

    public boolean getMatrixForTransform(float[] fArr) {
        if (fArr.length != 16) {
            HiLog.error(LOG_TAG, "getMatrixForTransform failed, invalid matrix length", new Object[0]);
            return false;
        }
        this.mTextureHolderAdapter.getTransform(fArr);
        return true;
    }

    public void setOnNewFrameCallback(OnNewFrameCallback onNewFrameCallback) {
        this.mOnFrameAvailableCallback = onNewFrameCallback;
        if (onNewFrameCallback == null) {
            this.mTextureHolderAdapter.setOnFrameAvailableListener(null);
        } else {
            this.mTextureHolderAdapter.setOnFrameAvailableListener(this.mAdapterFrameAvailableListener);
        }
    }

    public void abandon() {
        this.mTextureHolderAdapter.abandon();
    }
}
