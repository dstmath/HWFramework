package com.google.android.media.effect.effects;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext.OnFrameReceivedListener;
import android.filterfw.core.FilterFactory;
import android.filterfw.core.FilterFunction;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.SimpleScheduler;
import android.filterpacks.imageproc.BlendFilter;
import android.media.effect.EffectContext;
import android.media.effect.FilterGraphEffect;

public class FaceliftEffect extends FilterGraphEffect {
    private static final String mFaceliftGraph = "@import android.filterpacks.base;\n@import android.filterpacks.imageproc;\n@import com.android.effects;\n@import com.google.android.filterpacks.facedetect;\n\n@set padSize        = 10;\n@set intensitySteps = 10;\n@set sigma          = 0.01f;\n@set coordOffset    = 0.5f;\n\n@setting autoBranch = \"synced\";\n\n@filter GLTextureSource input {\n  texId = 0;\n  width = 0;\n  height = 0;\n  repeatFrame = false;\n}\n\n@filter FaceliftFilter faceLifter {\n  rangeSteps = $intensitySteps;\n  rangeSigma = $sigma;\n  padSize = $padSize;\n  glCoordOffset = $coordOffset;\n}\n\n@filter MultiFaceDetectorFilter faceDetector {\n  minEyeDist = 30.0f;\n  rollRange = 45.0f;\n  numChannelsDetector = 3;\n}\n\n@filter ToPackedGrayFilter toGrayScale {\n}\n\n@filter CallbackFilter frameListener {\n}\n\n@connect input[frame]  => toGrayScale[image];\n@connect toGrayScale[image] => faceDetector[image];\n@connect input[frame]  => faceLifter[image];\n@connect faceDetector[faces] => faceLifter[faces];\n@connect faceLifter[image] => frameListener[frame];\n";
    private FilterFunction mBlendFunction;
    private OnFrameReceivedListener mFrameListener = new OnFrameReceivedListener() {
        public void onFrameReceived(Filter filter, Frame result, Object userData) {
            FaceliftEffect.this.mSmoothFrame = result.retain();
        }
    };
    private Frame mSmoothFrame = null;

    public FaceliftEffect(EffectContext context, String name) {
        super(context, name, mFaceliftGraph, "input", "output", SimpleScheduler.class);
        this.mGraph.getFilter("frameListener").setInputValue("listener", this.mFrameListener);
        createBlendFunction();
    }

    private void createBlendFunction() {
        Class filterClass = BlendFilter.class;
        Filter filter = FilterFactory.sharedFactory().createFilterByClass(filterClass, filterClass.getSimpleName());
        filter.init();
        this.mBlendFunction = new FilterFunction(getFilterContext(), filter);
    }

    public void setParameter(String parameterKey, Object value) {
        if (parameterKey.equals("blend")) {
            this.mBlendFunction.setInputValue(parameterKey, value);
        }
    }

    public void apply(int inputTexId, int width, int height, int outputTexId) {
        beginGLEffect();
        Frame inputFrame = frameFromTexture(inputTexId, width, height);
        FrameFormat inputFormat = inputFrame.getFormat();
        if (!(this.mSmoothFrame != null && this.mSmoothFrame.getFormat().getWidth() == inputFormat.getWidth() && this.mSmoothFrame.getFormat().getHeight() == inputFormat.getHeight())) {
            if (this.mSmoothFrame != null) {
                this.mSmoothFrame.release();
            }
            Filter src = this.mGraph.getFilter(this.mInputName);
            if (src != null) {
                src.setInputValue("texId", Integer.valueOf(inputTexId));
                src.setInputValue("width", Integer.valueOf(width));
                src.setInputValue("height", Integer.valueOf(height));
                try {
                    this.mRunner.run();
                } catch (RuntimeException e) {
                    throw new RuntimeException("Internal error applying effect: ", e);
                }
            }
            throw new RuntimeException("Failed to extract texture source in graph.");
        }
        Frame outputFrame = frameFromTexture(outputTexId, width, height);
        if (this.mSmoothFrame != null) {
            Frame resultFrame = this.mBlendFunction.executeWithArgList(new Object[]{"left", inputFrame, "right", this.mSmoothFrame});
            outputFrame.setDataFromFrame(resultFrame);
            resultFrame.release();
        } else {
            outputFrame.setDataFromFrame(inputFrame);
        }
        inputFrame.release();
        outputFrame.release();
        endGLEffect();
    }

    public void release() {
        if (this.mSmoothFrame != null) {
            this.mSmoothFrame.release();
            this.mSmoothFrame = null;
        }
        super.release();
    }
}
