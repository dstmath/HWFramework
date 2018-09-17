package com.google.android.media.effect.effects;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext.OnFrameReceivedListener;
import android.filterfw.core.Frame;
import android.filterfw.core.OneShotScheduler;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera.Face;
import android.media.effect.EffectContext;
import android.media.effect.EffectUpdateListener;
import android.media.effect.FilterGraphEffect;
import com.google.android.filterpacks.facedetect.FaceMeta;

public class FaceTrackingEffect extends FilterGraphEffect {
    private static final int FACE_DETECTOR_MAX_SIZE = 320;
    private static final String mFaceTrackingGraph = "@import android.filterpacks.base;\n@import android.filterpacks.imageproc;\n@import com.google.android.filterpacks.facedetect;\n\n\n@filter GLTextureSource srcTex {\n  texId = 0;\n  repeatFrame = true;\n}\n\n@filter FrameBranch branch1 {\n  outputs = 2;\n}\n\n@filter ToPackedGrayFilter toPackedGray {\n}\n\n@filter MultiFaceTrackerFilter faceTracker {\n  quality = 0.0f;\n  smoothness = 0.2f;\n  minEyeDist = 30.0f;\n  rollRange = 45.0f;\n  numSkipFrames = 9;\n  trackingError = 1.0;\n  mouthOnlySmoothing = 0;\n  useAffineCorrection = 1;\n  patchSize = 15;\n}\n\n@filter CallbackFilter faceListener {\n}\n\n@filter GLTextureTarget trgTex {\n  texId = 0;\n}\n\n@connect srcTex[frame]           => branch1[in];\n@connect branch1[out0]           => toPackedGray[image];\n@connect toPackedGray[image]     => faceTracker[image];\n@connect faceTracker[faces]      => faceListener[frame];\n@connect branch1[out1]           => trgTex[frame];\n";
    private EffectUpdateListener mEffectListener = null;
    private OnFrameReceivedListener mFaceListener = new OnFrameReceivedListener() {
        public void onFrameReceived(Filter filter, Frame frame, Object userData) {
            if (FaceTrackingEffect.this.mEffectListener != null) {
                FaceMeta faces = (FaceMeta) frame.getObjectValue();
                Face[] facedata = new Face[faces.count()];
                for (int i = 0; i < faces.count(); i++) {
                    facedata[i] = new Face();
                    facedata[i].rect = new Rect((int) ((faces.getFaceX0(i) - 0.5f) * 2000.0f), (int) ((faces.getFaceY0(i) - 0.5f) * 2000.0f), (int) ((faces.getFaceX1(i) - 0.5f) * 2000.0f), (int) ((faces.getFaceY1(i) - 0.5f) * 2000.0f));
                    facedata[i].id = faces.getId(i);
                    facedata[i].score = (int) ((faces.getConfidence(i) * 99.0f) + 1.0f);
                    facedata[i].leftEye = new Point((int) ((faces.getLeftEyeX(i) - 0.5f) * 2000.0f), (int) ((faces.getLeftEyeY(i) - 0.5f) * 2000.0f));
                    facedata[i].rightEye = new Point((int) ((faces.getRightEyeX(i) - 0.5f) * 2000.0f), (int) ((faces.getRightEyeY(i) - 0.5f) * 2000.0f));
                    facedata[i].mouth = new Point((int) ((faces.getMouthX(i) - 0.5f) * 2000.0f), (int) ((faces.getMouthY(i) - 0.5f) * 2000.0f));
                }
                FaceTrackingEffect.this.mEffectListener.onEffectUpdated(FaceTrackingEffect.this, facedata);
            }
        }
    };
    private boolean mFirstTime = true;
    private int mInputHeight = 0;
    private int mInputWidth = 0;

    public FaceTrackingEffect(EffectContext context, String name) {
        super(context, name, mFaceTrackingGraph, "srcTex", "trgTex", OneShotScheduler.class);
        this.mGraph.getFilter("faceListener").setInputValue("listener", this.mFaceListener);
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
            throw new RuntimeException("FaceTrackingEffect can't change input size!");
        }
        super.apply(inputTexId, width, height, outputTexId);
    }

    public void setParameter(String parameterKey, Object value) {
    }

    public void setUpdateListener(EffectUpdateListener listener) {
        this.mEffectListener = listener;
    }
}
