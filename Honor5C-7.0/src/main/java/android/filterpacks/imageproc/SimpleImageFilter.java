package android.filterpacks.imageproc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.Program;
import android.filterfw.format.ImageFormat;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;

public abstract class SimpleImageFilter extends Filter {
    protected int mCurrentTarget;
    protected String mParameterName;
    protected Program mProgram;

    protected abstract Program getNativeProgram(FilterContext filterContext);

    protected abstract Program getShaderProgram(FilterContext filterContext);

    public SimpleImageFilter(String name, String parameterName) {
        super(name);
        this.mCurrentTarget = 0;
        this.mParameterName = parameterName;
    }

    public void setupPorts() {
        if (this.mParameterName != null) {
            try {
                addProgramPort(this.mParameterName, this.mParameterName, SimpleImageFilter.class.getDeclaredField("mProgram"), Float.TYPE, false);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Internal Error: mProgram field not found!");
            }
        }
        addMaskedInputPort("image", ImageFormat.create(3));
        addOutputBasedOnInput("image", "image");
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    public void process(FilterContext context) {
        Frame input = pullInput("image");
        FrameFormat inputFormat = input.getFormat();
        Frame output = context.getFrameManager().newFrame(inputFormat);
        updateProgramWithTarget(inputFormat.getTarget(), context);
        this.mProgram.process(input, output);
        pushOutput("image", output);
        output.release();
    }

    protected void updateProgramWithTarget(int target, FilterContext context) {
        if (target != this.mCurrentTarget) {
            switch (target) {
                case AudioState.ROUTE_BLUETOOTH /*2*/:
                    this.mProgram = getNativeProgram(context);
                    break;
                case Engine.DEFAULT_STREAM /*3*/:
                    this.mProgram = getShaderProgram(context);
                    break;
                default:
                    this.mProgram = null;
                    break;
            }
            if (this.mProgram == null) {
                throw new RuntimeException("Could not create a program for image filter " + this + "!");
            }
            initProgramInputs(this.mProgram, context);
            this.mCurrentTarget = target;
        }
    }
}
