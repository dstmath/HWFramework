package ohos.agp.window.aspbshell;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.inputmethod.EditorInfo;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.window.aspbshell.TextInputConnection;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AGPContainerView extends SurfaceView implements SurfaceHolder.Callback {
    private static final int DEFAULT_INPUT_TYPE = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "AGPContainerView");
    private ISurfaceViewListener mListener;
    private Surface mSurface;
    private int mSurfaceHeight;
    private int mSurfaceWidth;
    private TextInputConnection.ITextViewListener mTextViewListener;
    private int surfaceInputType = 0;

    public interface ISurfaceViewListener {
        void onConfigurationChanged(Configuration configuration);

        void onSurfaceChanged(Surface surface, int i, int i2, int i3);

        void onSurfaceCreated(Surface surface);

        void onSurfaceDestroyed(Surface surface);
    }

    public AGPContainerView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            HiLog.error(LABEL, "surfaceCreated holder=null.", new Object[0]);
            return;
        }
        this.mSurface = surfaceHolder.getSurface();
        ISurfaceViewListener iSurfaceViewListener = this.mListener;
        if (iSurfaceViewListener != null) {
            iSurfaceViewListener.onSurfaceCreated(this.mSurface);
        } else {
            HiLog.error(LABEL, "surfaceCreated mListener=null.", new Object[0]);
        }
    }

    @Override // android.view.SurfaceHolder.Callback
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        if (surfaceHolder == null) {
            HiLog.error(LABEL, "surfaceChanged holder=null.", new Object[0]);
            return;
        }
        this.mSurface = surfaceHolder.getSurface();
        this.mSurfaceWidth = i2;
        this.mSurfaceHeight = i3;
        HiLog.debug(LABEL, "surfaceChanged w=%{public}d, h=%{public}d", new Object[]{Integer.valueOf(i2), Integer.valueOf(i3)});
        ISurfaceViewListener iSurfaceViewListener = this.mListener;
        if (iSurfaceViewListener != null) {
            iSurfaceViewListener.onSurfaceChanged(this.mSurface, i, i2, i3);
        } else {
            HiLog.error(LABEL, "surfaceChanged mListener=null.", new Object[0]);
        }
        requestFocus();
    }

    @Override // android.view.SurfaceHolder.Callback
    public synchronized void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (this.mListener != null) {
            this.mListener.onSurfaceDestroyed(this.mSurface);
        } else {
            HiLog.error(LABEL, "surfaceDestroyed mListener=null.", new Object[0]);
        }
    }

    public int getActualWidth() {
        return this.mSurfaceWidth;
    }

    public int getActualHeight() {
        return this.mSurfaceHeight;
    }

    public void setSurfaceListener(ISurfaceViewListener iSurfaceViewListener) {
        this.mListener = iSurfaceViewListener;
    }

    public void setInputChannelListener(TextInputConnection.ITextViewListener iTextViewListener) {
        this.mTextViewListener = iTextViewListener;
    }

    @Override // android.view.View
    public TextInputConnection onCreateInputConnection(EditorInfo editorInfo) {
        TextInputConnection.ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener != null) {
            iTextViewListener.updateEditorInfo(editorInfo);
        }
        HiLog.debug(LABEL, "onCreateInputConnection reloaded, now surfaceInputType=%{public}d", new Object[]{Integer.valueOf(this.surfaceInputType)});
        return new TextInputConnection(this, null, this.mTextViewListener);
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        ISurfaceViewListener iSurfaceViewListener = this.mListener;
        if (iSurfaceViewListener != null) {
            iSurfaceViewListener.onConfigurationChanged(configuration);
        } else {
            HiLog.error(LABEL, "surfaceCreated mListener=null.", new Object[0]);
        }
    }

    public void destroy() {
        getHolder().removeCallback(this);
        this.mListener = null;
        this.mTextViewListener = null;
    }

    public void reloadInputAttribute(int i, int i2) {
        this.surfaceInputType = i;
    }
}
