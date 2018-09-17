package com.google.android.filterpacks.facedetect;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.FrameManager;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.GenerateFinalPort;
import android.filterfw.core.MutableFrameFormat;
import android.filterfw.core.NativeProgram;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.filterfw.format.ObjectFormat;
import android.filterfw.geometry.Quad;
import android.filterfw.geometry.Rectangle;

public class FaceZoomer extends Filter {
    private MutableFrameFormat mDebugFormat;
    @GenerateFinalPort(hasDefault = true, name = "enableDebugStream")
    private boolean mEnableDebugStream = false;
    private final String mFragShader = "precision mediump float;\nuniform sampler2D tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  const vec2 lo = vec2(0.0, 0.0);\n  const vec2 hi = vec2(1.0, 1.0);\n  const vec4 black = vec4(0.0, 0.0, 0.0, 1.0);\n  bool out_of_bounds =\n    any(lessThan(v_texcoord, lo)) ||\n    any(greaterThan(v_texcoord, hi));\n  if (out_of_bounds) {\n    gl_FragColor = black;\n  } else {\n    gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n  }\n}\n";
    @GenerateFieldPort(hasDefault = true, name = "maxOutputSize")
    private int mMaxOutputSize = 320;
    @GenerateFinalPort(hasDefault = true, name = "minFramesPerSpeaker")
    private int mMinFramesPerSpeaker = 10;
    @GenerateFinalPort(hasDefault = true, name = "minFramesPerTransition")
    private int mMinFramesPerTransition = 1;
    @GenerateFinalPort(hasDefault = true, name = "minFramesPerZoomOut")
    private int mMinFramesPerZoomOut = 25;
    @GenerateFinalPort(hasDefault = true, name = "minTransitionMagnitude")
    private float mMinTransitionMagnitude = 0.5f;
    @GenerateFinalPort(hasDefault = true, name = "minTransitionMagnitudeFast")
    private float mMinTransitionMagnitudeFast = 0.35f;
    private MutableFrameFormat mOutputFormat = null;
    @GenerateFinalPort(hasDefault = true, name = "outputTransitionsOnly")
    private boolean mOutputTransitionsOnly = true;
    private MutableFrameFormat mRegionFrameFormat;
    private ShaderProgram mRenderRegionProgram;
    private NativeProgram mSelectRegionProgram;
    private Frame[] mSelectRegionProgramInputs;
    @GenerateFinalPort(hasDefault = true, name = "transitionSeconds")
    private float mTransitionSeconds = 1.0f;
    @GenerateFinalPort(hasDefault = true, name = "transitionSigma")
    private float mTransitionSigma = 3.0f;

    public FaceZoomer(String name) {
        super(name);
    }

    public void setupPorts() {
        FrameFormat imageFormat = ImageFormat.create(3, 3);
        FrameFormat facesFormat = ObjectFormat.fromClass(FaceMeta.class, 2);
        FrameFormat lipsFormat = ObjectFormat.fromClass(LipDiff.class, 2);
        addMaskedInputPort("image", imageFormat);
        addMaskedInputPort("faces", facesFormat);
        addMaskedInputPort("lips", lipsFormat);
        addOutputBasedOnInput("image", "image");
        if (this.mEnableDebugStream) {
            this.mDebugFormat = ObjectFormat.fromClass(String.class, 1);
            addOutputPort("debug", this.mDebugFormat);
        }
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    protected void prepare(FilterContext context) {
        this.mRegionFrameFormat = ObjectFormat.fromClass(RectFrame.class, 1, 2);
        this.mSelectRegionProgramInputs = new Frame[2];
        this.mSelectRegionProgram = new NativeProgram("filterpack_facedetect", "face_zoomer");
        this.mRenderRegionProgram = new ShaderProgram(context, "precision mediump float;\nuniform sampler2D tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  const vec2 lo = vec2(0.0, 0.0);\n  const vec2 hi = vec2(1.0, 1.0);\n  const vec4 black = vec4(0.0, 0.0, 0.0, 1.0);\n  bool out_of_bounds =\n    any(lessThan(v_texcoord, lo)) ||\n    any(greaterThan(v_texcoord, hi));\n  if (out_of_bounds) {\n    gl_FragColor = black;\n  } else {\n    gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n  }\n}\n");
        this.mSelectRegionProgram.setHostValue("minTransitionMagnitudeFast", Float.valueOf(this.mMinTransitionMagnitudeFast));
        this.mSelectRegionProgram.setHostValue("minTransitionMagnitude", Float.valueOf(this.mMinTransitionMagnitude));
        this.mSelectRegionProgram.setHostValue("minFramesPerSpeaker", Integer.valueOf(this.mMinFramesPerSpeaker));
        this.mSelectRegionProgram.setHostValue("minFramesPerTransition", Integer.valueOf(this.mMinFramesPerTransition));
        this.mSelectRegionProgram.setHostValue("minFramesPerZoomOut", Integer.valueOf(this.mMinFramesPerZoomOut));
        this.mSelectRegionProgram.setHostValue("transitionSeconds", Float.valueOf(this.mTransitionSeconds));
        this.mSelectRegionProgram.setHostValue("transitionSigma", Float.valueOf(this.mTransitionSigma));
        this.mSelectRegionProgram.setHostValue("enableDebugStream", Boolean.valueOf(this.mEnableDebugStream));
        this.mSelectRegionProgram.setHostValue("outputTransitionsOnly", Boolean.valueOf(this.mOutputTransitionsOnly));
    }

    public void process(FilterContext context) {
        FrameManager frameManager = context.getFrameManager();
        Frame imageFrame = pullInput("image");
        long timestamp = imageFrame.getTimestamp();
        Frame facesFrame = pullInput("faces");
        Frame lipsFrame = pullInput("lips");
        setOutputFormat(imageFrame.getFormat());
        this.mSelectRegionProgramInputs[0] = facesFrame;
        this.mSelectRegionProgramInputs[1] = lipsFrame;
        Frame regionFrame = frameManager.newFrame(this.mRegionFrameFormat);
        this.mSelectRegionProgram.process(this.mSelectRegionProgramInputs, regionFrame);
        Quad selectedRegion = getSelectedRegion(regionFrame);
        if (selectedRegion.getBoundingWidth() <= 0.0f || selectedRegion.getBoundingHeight() <= 0.0f) {
            throw new RuntimeException("Illegal selected region size: " + selectedRegion.getBoundingWidth() + " x " + selectedRegion.getBoundingHeight() + "!");
        }
        this.mRenderRegionProgram.setSourceRegion(selectedRegion);
        if (timestamp != -1) {
            this.mSelectRegionProgram.setHostValue("timestamp", Long.valueOf(timestamp));
        }
        Frame zoomedImageFrame = frameManager.newFrame(this.mOutputFormat);
        this.mRenderRegionProgram.process(imageFrame, zoomedImageFrame);
        regionFrame.release();
        pushOutput("image", zoomedImageFrame);
        zoomedImageFrame.release();
        if (this.mEnableDebugStream) {
            String debugString = (String) this.mSelectRegionProgram.getHostValue("debug");
            Frame debugFrame = frameManager.newFrame(this.mDebugFormat);
            if (debugString.length() > 0 && this.mOutputTransitionsOnly) {
                debugString = timestamp + "," + debugString;
            }
            debugFrame.setObjectValue(debugString);
            pushOutput("debug", debugFrame);
            debugFrame.release();
        }
    }

    private void setOutputFormat(FrameFormat inputFormat) {
        int ow;
        int oh;
        int w = inputFormat.getWidth();
        int h = inputFormat.getHeight();
        if (w > h) {
            ow = Math.min(this.mMaxOutputSize, w);
            oh = (ow * h) / w;
        } else {
            oh = Math.min(this.mMaxOutputSize, h);
            ow = (oh * w) / h;
        }
        if (this.mOutputFormat == null) {
            this.mOutputFormat = ImageFormat.create(ow, oh, 3, 3);
        }
        this.mOutputFormat.setDimensions(ow, oh);
    }

    private Quad getSelectedRegion(Frame regionFrame) {
        RectFrame rectFrame = (RectFrame) regionFrame.getObjectValue();
        int numRects = rectFrame.count();
        if (numRects == 1) {
            return new Rectangle(rectFrame.getX(0), rectFrame.getY(0), rectFrame.getWidth(0), rectFrame.getHeight(0));
        }
        throw new RuntimeException("Illegal RectFrame size: " + numRects);
    }
}
