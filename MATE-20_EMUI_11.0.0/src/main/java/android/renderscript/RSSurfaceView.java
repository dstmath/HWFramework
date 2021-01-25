package android.renderscript;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.renderscript.RenderScriptGL;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class RSSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private RenderScriptGL mRS;
    private SurfaceHolder mSurfaceHolder;

    @UnsupportedAppUsage
    public RSSurfaceView(Context context) {
        super(context);
        init();
    }

    @UnsupportedAppUsage
    public RSSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder holder) {
        this.mSurfaceHolder = holder;
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized (this) {
            if (this.mRS != null) {
                this.mRS.setSurface(null, 0, 0);
            }
        }
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        synchronized (this) {
            if (this.mRS != null) {
                this.mRS.setSurface(holder, w, h);
            }
        }
    }

    public void pause() {
        RenderScriptGL renderScriptGL = this.mRS;
        if (renderScriptGL != null) {
            renderScriptGL.pause();
        }
    }

    public void resume() {
        RenderScriptGL renderScriptGL = this.mRS;
        if (renderScriptGL != null) {
            renderScriptGL.resume();
        }
    }

    public RenderScriptGL createRenderScriptGL(RenderScriptGL.SurfaceConfig sc) {
        RenderScriptGL rs = new RenderScriptGL(getContext(), sc);
        setRenderScriptGL(rs);
        return rs;
    }

    public void destroyRenderScriptGL() {
        synchronized (this) {
            this.mRS.destroy();
            this.mRS = null;
        }
    }

    public void setRenderScriptGL(RenderScriptGL rs) {
        this.mRS = rs;
    }

    public RenderScriptGL getRenderScriptGL() {
        return this.mRS;
    }
}
