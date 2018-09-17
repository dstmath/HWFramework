package com.google.android.filterpacks.facedetect;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateProgramPort;
import android.filterfw.core.GenerateProgramPorts;
import android.filterfw.core.NativeProgram;
import android.filterfw.core.Program;
import android.filterfw.format.ImageFormat;
import android.filterfw.format.ObjectFormat;

public class MultiFaceDetectorFilter extends Filter {
    private int mInputChannels;
    private int mInputHeight;
    private int mInputWidth;
    private boolean mIsInitialized;
    @GenerateProgramPorts({@GenerateProgramPort(hasDefault = true, name = "modulePath", type = String.class), @GenerateProgramPort(hasDefault = true, name = "ffModule", type = String.class), @GenerateProgramPort(hasDefault = true, name = "lmModule", type = String.class), @GenerateProgramPort(hasDefault = true, name = "minEyeDist", type = float.class), @GenerateProgramPort(hasDefault = true, name = "rollRange", type = float.class), @GenerateProgramPort(hasDefault = true, name = "numChannelsDetector", type = int.class)})
    private Program mProgram;

    public MultiFaceDetectorFilter(String name) {
        super(name);
        this.mIsInitialized = false;
        this.mInputWidth = 0;
        this.mInputHeight = 0;
        this.mInputChannels = 0;
    }

    public void setupPorts() {
        FrameFormat imageFormat = ImageFormat.create(1, 2);
        FrameFormat outputFormat = ObjectFormat.fromClass(FaceMeta.class, 2);
        addMaskedInputPort("image", imageFormat);
        addOutputPort("faces", outputFormat);
    }

    public void prepare(FilterContext environment) {
        this.mProgram = new NativeProgram("filterpack_facedetect", "multiface_detector");
        initProgramInputs(this.mProgram, environment);
    }

    public void process(FilterContext context) {
        Frame input = pullInput("image");
        FrameFormat inputFormat = input.getFormat();
        if (this.mIsInitialized) {
            if (this.mInputWidth == inputFormat.getWidth() && this.mInputHeight == inputFormat.getHeight()) {
                if (this.mInputChannels != inputFormat.getBytesPerSample()) {
                }
            }
            throw new RuntimeException("MultiFaceDetectorFilter does not support varying frame input sizes!");
        }
        this.mInputWidth = inputFormat.getWidth();
        this.mInputHeight = inputFormat.getHeight();
        this.mInputChannels = inputFormat.getBytesPerSample();
        this.mProgram.setHostValue("imgWidth", Integer.valueOf(this.mInputWidth));
        this.mProgram.setHostValue("imgHeight", Integer.valueOf(this.mInputHeight));
        this.mProgram.setHostValue("imgChannels", Integer.valueOf(this.mInputChannels));
        this.mIsInitialized = true;
        this.mProgram.process(input, null);
        Frame output = context.getFrameManager().newFrame(ObjectFormat.fromClass(FaceMeta.class, Integer.parseInt((String) this.mProgram.getHostValue("num_faces")), 2));
        this.mProgram.process(null, output);
        pushOutput("faces", output);
        output.release();
    }
}
