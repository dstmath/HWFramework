package android.renderscript;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.renderscript.RenderScriptGL.SurfaceConfig;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;

public class RSTextureView extends TextureView implements SurfaceTextureListener {
    private RenderScriptGL mRS;
    private SurfaceTexture mSurfaceTexture;

    public RSTextureView(Context context) {
        super(context);
        init();
    }

    public RSTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setSurfaceTextureListener(this);
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.mSurfaceTexture = surface;
        if (this.mRS != null) {
            this.mRS.setSurfaceTexture(this.mSurfaceTexture, width, height);
        }
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        this.mSurfaceTexture = surface;
        if (this.mRS != null) {
            this.mRS.setSurfaceTexture(this.mSurfaceTexture, width, height);
        }
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        this.mSurfaceTexture = surface;
        if (this.mRS != null) {
            this.mRS.setSurfaceTexture(null, 0, 0);
        }
        return true;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        this.mSurfaceTexture = surface;
    }

    public void pause() {
        if (this.mRS != null) {
            this.mRS.pause();
        }
    }

    public void resume() {
        if (this.mRS != null) {
            this.mRS.resume();
        }
    }

    public RenderScriptGL createRenderScriptGL(SurfaceConfig sc) {
        RenderScriptGL rs = new RenderScriptGL(getContext(), sc);
        setRenderScriptGL(rs);
        if (this.mSurfaceTexture != null) {
            this.mRS.setSurfaceTexture(this.mSurfaceTexture, getWidth(), getHeight());
        }
        return rs;
    }

    public void destroyRenderScriptGL() {
        this.mRS.destroy();
        this.mRS = null;
    }

    public void setRenderScriptGL(RenderScriptGL rs) {
        this.mRS = rs;
        if (this.mSurfaceTexture != null) {
            this.mRS.setSurfaceTexture(this.mSurfaceTexture, getWidth(), getHeight());
        }
    }

    public RenderScriptGL getRenderScriptGL() {
        return this.mRS;
    }
}
