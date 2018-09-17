package com.google.android.filterpacks.facedetect;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateProgramPort;
import android.filterfw.core.GenerateProgramPorts;
import android.filterfw.core.NativeProgram;
import android.filterfw.format.ImageFormat;
import android.filterfw.format.ObjectFormat;

public class MultiFaceTrackerFilter extends Filter {
    private int mInputChannels = 0;
    private int mInputHeight = 0;
    private int mInputWidth = 0;
    private boolean mIsInitialized = false;
    @GenerateProgramPorts({@GenerateProgramPort(hasDefault = true, name = "modulePath", type = String.class), @GenerateProgramPort(hasDefault = true, name = "ffModule", type = String.class), @GenerateProgramPort(hasDefault = true, name = "lmModule", type = String.class), @GenerateProgramPort(hasDefault = true, name = "numSkipFrames", type = int.class), @GenerateProgramPort(hasDefault = true, name = "trackingError", type = float.class), @GenerateProgramPort(hasDefault = true, name = "minEyeDist", type = float.class), @GenerateProgramPort(hasDefault = true, name = "rollRange", type = float.class), @GenerateProgramPort(hasDefault = true, name = "quality", type = float.class), @GenerateProgramPort(hasDefault = true, name = "smoothness", type = float.class), @GenerateProgramPort(hasDefault = true, name = "mouthOnlySmoothing", type = int.class), @GenerateProgramPort(hasDefault = true, name = "useAffineCorrection", type = int.class), @GenerateProgramPort(hasDefault = true, name = "numChannelsDetector", type = int.class), @GenerateProgramPort(hasDefault = true, name = "patchSize", type = int.class)})
    private NativeProgram mProgram = null;

    public MultiFaceTrackerFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        FrameFormat imageFormat = ImageFormat.create(1, 2);
        FrameFormat outputFormat = ObjectFormat.fromClass(FaceMeta.class, 2);
        addMaskedInputPort("image", imageFormat);
        addOutputPort("faces", outputFormat);
    }

    public void prepare(FilterContext environment) {
        this.mProgram = new NativeProgram("filterpack_facedetect", "multiface_tracker");
        initProgramInputs(this.mProgram, environment);
    }

    public void tearDown(FilterContext context) {
        if (this.mProgram != null) {
            this.mProgram.tearDown();
        }
    }

    public void close(FilterContext context) {
        if (this.mProgram != null) {
            this.mProgram.reset();
        }
    }

    public void process(FilterContext context) {
        Frame input = pullInput("image");
        FrameFormat inputFormat = input.getFormat();
        if (!this.mIsInitialized) {
            this.mInputWidth = inputFormat.getWidth();
            this.mInputHeight = inputFormat.getHeight();
            this.mInputChannels = inputFormat.getBytesPerSample();
            this.mProgram.setHostValue("imgWidth", Integer.valueOf(this.mInputWidth));
            this.mProgram.setHostValue("imgHeight", Integer.valueOf(this.mInputHeight));
            this.mProgram.setHostValue("imgChannels", Integer.valueOf(this.mInputChannels));
            this.mIsInitialized = true;
        } else if (!(this.mInputWidth == inputFormat.getWidth() && this.mInputHeight == inputFormat.getHeight() && this.mInputChannels == inputFormat.getBytesPerSample())) {
            this.mProgram.reset();
            this.mInputWidth = inputFormat.getWidth();
            this.mInputHeight = inputFormat.getHeight();
            this.mInputChannels = inputFormat.getBytesPerSample();
            this.mProgram.setHostValue("imgWidth", Integer.valueOf(this.mInputWidth));
            this.mProgram.setHostValue("imgHeight", Integer.valueOf(this.mInputHeight));
            this.mProgram.setHostValue("imgChannels", Integer.valueOf(this.mInputChannels));
        }
        this.mProgram.process(input, null);
        Frame output = context.getFrameManager().newFrame(ObjectFormat.fromClass(FaceMeta.class, Integer.parseInt((String) this.mProgram.getHostValue("num_faces")), 2));
        this.mProgram.process(null, output);
        pushOutput("faces", output);
        output.release();
    }
}
