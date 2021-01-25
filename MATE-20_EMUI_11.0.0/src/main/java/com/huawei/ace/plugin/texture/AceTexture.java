package com.huawei.ace.plugin.texture;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import com.huawei.ace.runtime.IAceOnResourceEvent;

public class AceTexture {
    private final IAceOnResourceEvent callback;
    private final long id;
    private SurfaceTexture.OnFrameAvailableListener onFrameListener = new SurfaceTexture.OnFrameAvailableListener() {
        /* class com.huawei.ace.plugin.texture.AceTexture.AnonymousClass1 */

        @Override // android.graphics.SurfaceTexture.OnFrameAvailableListener
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            AceTexture.this.textureImpl.markTextureFrameAvailable(AceTexture.this.id);
        }
    };
    private final SurfaceTexture surfaceTexture = new SurfaceTexture(0);
    private final IAceTexture textureImpl;

    public AceTexture(long j, IAceTexture iAceTexture, IAceOnResourceEvent iAceOnResourceEvent) {
        this.surfaceTexture.detachFromGLContext();
        this.surfaceTexture.setOnFrameAvailableListener(this.onFrameListener);
        this.id = j;
        this.textureImpl = iAceTexture;
        this.textureImpl.registerTexture(this.id, this.surfaceTexture);
        this.callback = iAceOnResourceEvent;
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.surfaceTexture;
    }

    public Surface getSurface() {
        return new Surface(this.surfaceTexture);
    }

    public long getId() {
        return this.id;
    }

    public void release() {
        this.surfaceTexture.setOnFrameAvailableListener(null);
        this.textureImpl.unregisterTexture(this.id);
        this.surfaceTexture.release();
    }
}
