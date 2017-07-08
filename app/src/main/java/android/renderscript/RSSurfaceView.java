package android.renderscript;

import android.content.Context;
import android.renderscript.RenderScriptGL.SurfaceConfig;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class RSSurfaceView extends SurfaceView implements Callback {
    private RenderScriptGL mRS;
    private SurfaceHolder mSurfaceHolder;

    public RSSurfaceView(Context context) {
        super(context);
        init();
    }

    public RSSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        this.mSurfaceHolder = holder;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized (this) {
            if (this.mRS != null) {
                this.mRS.setSurface(null, 0, 0);
            }
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        synchronized (this) {
            if (this.mRS != null) {
                this.mRS.setSurface(holder, w, h);
            }
        }
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
