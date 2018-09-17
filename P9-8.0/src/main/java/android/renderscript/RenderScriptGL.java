package android.renderscript;

import android.content.Context;
import android.graphics.SurfaceTexture;
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

    public RenderScriptGL(Context ctx, SurfaceConfig sc) {
        super(ctx);
        this.mSurfaceConfig = new SurfaceConfig(sc);
        int sdkVersion = ctx.getApplicationInfo().targetSdkVersion;
        this.mContext = nContextCreateGL(nDeviceCreate(), 0, sdkVersion, this.mSurfaceConfig.mColorMin, this.mSurfaceConfig.mColorPref, this.mSurfaceConfig.mAlphaMin, this.mSurfaceConfig.mAlphaPref, this.mSurfaceConfig.mDepthMin, this.mSurfaceConfig.mDepthPref, this.mSurfaceConfig.mStencilMin, this.mSurfaceConfig.mStencilPref, this.mSurfaceConfig.mSamplesMin, this.mSurfaceConfig.mSamplesPref, this.mSurfaceConfig.mSamplesQ, ctx.getResources().getDisplayMetrics().densityDpi);
        if (this.mContext == 0) {
            throw new RSDriverException("Failed to create RS context.");
        }
        this.mMessageThread = new MessageThread(this);
        this.mMessageThread.start();
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
        Surface surface = null;
        if (sur != null) {
            surface = new Surface(sur);
        }
        this.mWidth = w;
        this.mHeight = h;
        nContextSetSurface(w, h, surface);
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
