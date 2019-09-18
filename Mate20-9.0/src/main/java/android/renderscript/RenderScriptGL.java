package android.renderscript;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.renderscript.RenderScript;
import android.view.Surface;
import android.view.SurfaceHolder;

public class RenderScriptGL extends RenderScript {
    int mHeight = 0;
    SurfaceConfig mSurfaceConfig;
    int mWidth = 0;

    public static class SurfaceConfig {
        int mAlphaMin = 0;
        int mAlphaPref = 0;
        int mColorMin = 8;
        int mColorPref = 8;
        int mDepthMin = 0;
        int mDepthPref = 0;
        int mSamplesMin = 1;
        int mSamplesPref = 1;
        float mSamplesQ = 1.0f;
        int mStencilMin = 0;
        int mStencilPref = 0;

        public SurfaceConfig() {
        }

        public SurfaceConfig(SurfaceConfig sc) {
            this.mDepthMin = sc.mDepthMin;
            this.mDepthPref = sc.mDepthPref;
            this.mStencilMin = sc.mStencilMin;
            this.mStencilPref = sc.mStencilPref;
            this.mColorMin = sc.mColorMin;
            this.mColorPref = sc.mColorPref;
            this.mAlphaMin = sc.mAlphaMin;
            this.mAlphaPref = sc.mAlphaPref;
            this.mSamplesMin = sc.mSamplesMin;
            this.mSamplesPref = sc.mSamplesPref;
            this.mSamplesQ = sc.mSamplesQ;
        }

        private void validateRange(int umin, int upref, int rmin, int rmax) {
            if (umin < rmin || umin > rmax) {
                throw new RSIllegalArgumentException("Minimum value provided out of range.");
            } else if (upref < umin) {
                throw new RSIllegalArgumentException("preferred must be >= Minimum.");
            }
        }

        public void setColor(int minimum, int preferred) {
            validateRange(minimum, preferred, 5, 8);
            this.mColorMin = minimum;
            this.mColorPref = preferred;
        }

        public void setAlpha(int minimum, int preferred) {
            validateRange(minimum, preferred, 0, 8);
            this.mAlphaMin = minimum;
            this.mAlphaPref = preferred;
        }

        public void setDepth(int minimum, int preferred) {
            validateRange(minimum, preferred, 0, 24);
            this.mDepthMin = minimum;
            this.mDepthPref = preferred;
        }

        public void setSamples(int minimum, int preferred, float Q) {
            validateRange(minimum, preferred, 1, 32);
            if (Q < 0.0f || Q > 1.0f) {
                throw new RSIllegalArgumentException("Quality out of 0-1 range.");
            }
            this.mSamplesMin = minimum;
            this.mSamplesPref = preferred;
            this.mSamplesQ = Q;
        }
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RenderScriptGL(Context ctx, SurfaceConfig sc) {
        super(ctx);
        this.mSurfaceConfig = new SurfaceConfig(sc);
        int sdkVersion = ctx.getApplicationInfo().targetSdkVersion;
        long device = nDeviceCreate();
        int dpi = ctx.getResources().getDisplayMetrics().densityDpi;
        int i = this.mSurfaceConfig.mColorMin;
        int i2 = this.mSurfaceConfig.mColorPref;
        int i3 = this.mSurfaceConfig.mAlphaMin;
        int i4 = this.mSurfaceConfig.mAlphaPref;
        int i5 = this.mSurfaceConfig.mDepthMin;
        int i6 = this.mSurfaceConfig.mDepthPref;
        int i7 = this.mSurfaceConfig.mStencilMin;
        int i8 = this.mSurfaceConfig.mStencilPref;
        int sdkVersion2 = this.mSurfaceConfig.mSamplesMin;
        int i9 = this.mSurfaceConfig.mSamplesPref;
        int i10 = i9;
        int dpi2 = i8;
        int i11 = sdkVersion;
        this.mContext = nContextCreateGL(device, 0, sdkVersion, i, i2, i3, i4, i5, i6, i7, dpi2, sdkVersion2, i10, this.mSurfaceConfig.mSamplesQ, dpi);
        if (this.mContext != 0) {
            this.mMessageThread = new RenderScript.MessageThread(this);
            this.mMessageThread.start();
            return;
        }
        throw new RSDriverException("Failed to create RS context.");
    }

    public void setSurface(SurfaceHolder sur, int w, int h) {
        validate();
        Surface s = null;
        if (sur != null) {
            s = sur.getSurface();
        }
        this.mWidth = w;
        this.mHeight = h;
        nContextSetSurface(w, h, s);
    }

    public void setSurfaceTexture(SurfaceTexture sur, int w, int h) {
        validate();
        Surface s = null;
        if (sur != null) {
            s = new Surface(sur);
        }
        this.mWidth = w;
        this.mHeight = h;
        nContextSetSurface(w, h, s);
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public void pause() {
        validate();
        nContextPause();
    }

    public void resume() {
        validate();
        nContextResume();
    }

    public void bindRootScript(Script s) {
        validate();
        nContextBindRootScript((long) ((int) safeID(s)));
    }

    public void bindProgramStore(ProgramStore p) {
        validate();
        nContextBindProgramStore((long) ((int) safeID(p)));
    }

    public void bindProgramFragment(ProgramFragment p) {
        validate();
        nContextBindProgramFragment((long) ((int) safeID(p)));
    }

    public void bindProgramRaster(ProgramRaster p) {
        validate();
        nContextBindProgramRaster((long) ((int) safeID(p)));
    }

    public void bindProgramVertex(ProgramVertex p) {
        validate();
        nContextBindProgramVertex((long) ((int) safeID(p)));
    }
}
