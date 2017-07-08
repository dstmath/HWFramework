package com.google.android.filterpacks.facedetect;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.MutableFrameFormat;
import android.filterfw.core.NativeProgram;
import android.filterfw.core.Program;
import android.filterfw.format.ImageFormat;
import android.filterfw.format.ObjectFormat;

public class FaceRegionMaskFilter extends Filter {
    private Program mProgram;

    public FaceRegionMaskFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        addMaskedInputPort("image", ImageFormat.create(2, 2));
        addMaskedInputPort("faces", ObjectFormat.fromClass(FaceMeta.class, 2));
        addOutputBasedOnInput("mask", "image");
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        if (portName == "mask") {
            return maskFormatFor(inputFormat);
        }
        throw new RuntimeException("Unknown output port name:" + portName);
    }

    private FrameFormat maskFormatFor(FrameFormat format) {
        MutableFrameFormat maskFormat = format.mutableCopy();
        maskFormat.setBytesPerSample(1);
        maskFormat.setMetaValue("colorspace", Integer.valueOf(1));
        return maskFormat;
    }

    public void prepare(FilterContext context) {
        this.mProgram = new NativeProgram("filterpack_facedetect", "face_createmask");
    }

    public void process(FilterContext context) {
        Frame input = pullInput("image");
        FaceMeta faces = (FaceMeta) pullInput("faces").getObjectValue();
        FrameFormat inFormat = input.getFormat();
        this.mProgram.setHostValue("width", Integer.valueOf(inFormat.getWidth()));
        this.mProgram.setHostValue("height", Integer.valueOf(inFormat.getHeight()));
        this.mProgram.setHostValue("maskOnly", Boolean.valueOf(true));
        Frame maskFrame = context.getFrameManager().newFrame(maskFormatFor(inFormat));
        this.mProgram.process(new Frame[]{input, facemetaFrame}, maskFrame);
        pushOutput("mask", maskFrame);
        maskFrame.release();
    }
}
