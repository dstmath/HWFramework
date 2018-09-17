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

public class FaceTanningEffect extends FilterGraphEffect {
    private static final String mFaceTanningGraph = "@import android.filterpacks.base;\n@import android.filterpacks.imageproc;\n@import com.android.effects;\n@import com.google.android.filterpacks.facedetect;\n\n@setting autoBranch = \"synced\";\n\n@filter GLTextureSource input {\n  texId = 0;\n  width = 0;\n  height = 0;\n  repeatFrame = false;\n}\n\n@filter FaceTanFilter faceTanner {\n}\n\n@filter MultiFaceDetectorFilter faceDetector {\n  minEyeDist = 30.0f;\n  rollRange = 45.0f;\n  numChannelsDetector = 3;\n}\n\n@filter ToPackedGrayFilter toGrayScale {\n}\n\n@filter CallbackFilter frameListener {\n}\n\n@connect input[frame]  => toGrayScale[image];\n@connect toGrayScale[image] => faceDetector[image];\n@connect input[frame]  => faceTanner[image];\n@connect faceDetector[faces] => faceTanner[faces];\n@connect faceTanner[image] => frameListener[frame];\n";
    private FilterFunction mBlendFunction;
    private OnFrameReceivedListener mFrameListener;
    private Frame mSmoothFrame;

    public FaceTanningEffect(EffectContext context, String name) {
        super(context, name, mFaceTanningGraph, "input", "output", SimpleScheduler.class);
        this.mSmoothFrame = null;
        this.mFrameListener = new OnFrameReceivedListener() {
            public void onFrameReceived(Filter filter, Frame result, Object userData) {
                FaceTanningEffect.this.mSmoothFrame = result.retain();
            }
        };
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
        Frame outputFrame;
        beginGLEffect();
        Frame inputFrame = frameFromTexture(inputTexId, width, height);
        FrameFormat inputFormat = inputFrame.getFormat();
        if (this.mSmoothFrame != null && this.mSmoothFrame.getFormat().getWidth() == inputFormat.getWidth()) {
            if (this.mSmoothFrame.getFormat().getHeight() != inputFormat.getHeight()) {
            }
            outputFrame = frameFromTexture(outputTexId, width, height);
            if (this.mSmoothFrame == null) {
                Frame resultFrame = this.mBlendFunction.executeWithArgList(new Object[]{"left", inputFrame, "right", this.mSmoothFrame});
                outputFrame.setDataFromFrame(resultFrame);
                resultFrame.release();
            } else {
                outputFrame.setDataFromFrame(inputFrame);
            }
            inputFrame.release();
            outputFrame.release();
            endGLEffect();
            return;
        }
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
                outputFrame = frameFromTexture(outputTexId, width, height);
                if (this.mSmoothFrame == null) {
                    outputFrame.setDataFromFrame(inputFrame);
                } else {
                    Frame resultFrame2 = this.mBlendFunction.executeWithArgList(new Object[]{"left", inputFrame, "right", this.mSmoothFrame});
                    outputFrame.setDataFromFrame(resultFrame2);
                    resultFrame2.release();
                }
                inputFrame.release();
                outputFrame.release();
                endGLEffect();
                return;
            } catch (RuntimeException e) {
                throw new RuntimeException("Internal error applying effect: ", e);
            }
        }
        throw new RuntimeException("Failed to extract texture source in graph.");
    }

    public void release() {
        super.release();
        this.mSmoothFrame.release();
    }
}
