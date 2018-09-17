package com.google.android.media.effect.effects;

import android.filterfw.core.Filter;
import android.filterfw.core.OneShotScheduler;
import android.media.effect.EffectContext;
import android.media.effect.FilterGraphEffect;

public class GoofyFaceEffect extends FilterGraphEffect {
    private static final int FACE_DETECTOR_MAX_SIZE = 320;
    private static final String mGoofyGraph = "@import android.filterpacks.base;\n@import android.filterpacks.imageproc;\n@import com.google.android.filterpacks.facedetect;\n\n\n@filter GLTextureSource srcTex {\n  texId = 0;\n  repeatFrame = true;\n}\n\n@filter FrameBranch branch1 {\n  outputs = 2;\n}\n\n@filter ToPackedGrayFilter toPackedGray {\n}\n\n@filter MultiFaceTrackerFilter faceTracker {\n  quality = 0.0f;\n  smoothness = 0.2f;\n  minEyeDist = 30.0f;\n  rollRange = 45.0f;\n  numSkipFrames = 9;\n  trackingError = 0.8;\n  mouthOnlySmoothing = 0;\n  useAffineCorrection = 1;\n  patchSize = 15;\n}\n\n@filter GoofyFastRenderFilter goofyRenderer {\n  currentEffect = 0;\n  distortionAmount = 1.0f;\n}\n\n@filter GLTextureTarget trgTex {\n  texId = 0;\n}\n\n@connect srcTex[frame]           => branch1[in];\n@connect branch1[out0]           => toPackedGray[image];\n@connect branch1[out1]           => goofyRenderer[image];\n@connect toPackedGray[image]     => faceTracker[image];\n@connect faceTracker[faces]      => goofyRenderer[faces];\n@connect goofyRenderer[outimage] => trgTex[frame];\n";
    private boolean mFirstTime = true;
    private int mInputHeight = 0;
    private int mInputWidth = 0;

    public GoofyFaceEffect(EffectContext context, String name) {
        super(context, name, mGoofyGraph, "srcTex", "trgTex", OneShotScheduler.class);
    }

    public void apply(int inputTexId, int width, int height, int outputTexId) {
        if (this.mFirstTime) {
            int fdWidth;
            int fdHeight;
            if (width > height) {
                fdWidth = Math.min(FACE_DETECTOR_MAX_SIZE, width);
                fdHeight = (fdWidth * height) / width;
            } else {
                fdHeight = Math.min(FACE_DETECTOR_MAX_SIZE, height);
                fdWidth = (fdHeight * width) / height;
            }
            Filter grayFilter = this.mGraph.getFilter("toPackedGray");
            grayFilter.setInputValue("owidth", Integer.valueOf(fdWidth));
            grayFilter.setInputValue("oheight", Integer.valueOf(fdHeight));
            this.mFirstTime = false;
            this.mInputWidth = width;
            this.mInputHeight = height;
        } else if (!(width == this.mInputWidth && height == this.mInputHeight)) {
            throw new RuntimeException("GoofyFaceEffect can't change input size!");
        }
        super.apply(inputTexId, width, height, outputTexId);
    }

    public void setParameter(String parameterKey, Object value) {
        if (parameterKey.equals("currentEffect")) {
            this.mGraph.getFilter("goofyRenderer").setInputValue("currentEffect", value);
        } else if (parameterKey.equals("strength")) {
            this.mGraph.getFilter("goofyRenderer").setInputValue("distortionAmount", value);
        }
    }
}
