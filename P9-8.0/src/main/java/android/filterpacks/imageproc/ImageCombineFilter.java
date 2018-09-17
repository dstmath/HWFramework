package android.filterpacks.imageproc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.Program;
import android.filterfw.format.ImageFormat;

public abstract class ImageCombineFilter extends Filter {
    protected int mCurrentTarget = 0;
    protected String[] mInputNames;
    protected String mOutputName;
    protected String mParameterName;
    protected Program mProgram;

    protected abstract Program getNativeProgram(FilterContext filterContext);

    protected abstract Program getShaderProgram(FilterContext filterContext);

    public ImageCombineFilter(String name, String[] inputNames, String outputName, String parameterName) {
        super(name);
        this.mInputNames = inputNames;
        this.mOutputName = outputName;
        this.mParameterName = parameterName;
    }

    public void setupPorts() {
        if (this.mParameterName != null) {
            try {
                addProgramPort(this.mParameterName, this.mParameterName, ImageCombineFilter.class.getDeclaredField("mProgram"), Float.TYPE, false);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Internal Error: mProgram field not found!");
            }
        }
        for (String inputName : this.mInputNames) {
            addMaskedInputPort(inputName, ImageFormat.create(3));
        }
        addOutputBasedOnInput(this.mOutputName, this.mInputNames[0]);
    }

    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    private void assertAllInputTargetsMatch() {
        int i = 0;
        int target = getInputFormat(this.mInputNames[0]).getTarget();
        String[] strArr = this.mInputNames;
        int length = strArr.length;
        while (i < length) {
            if (target != getInputFormat(strArr[i]).getTarget()) {
                throw new RuntimeException("Type mismatch of input formats in filter " + this + ". All input frames must have the same target!");
            }
            i++;
        }
    }

    public void process(FilterContext context) {
        Frame[] inputs = new Frame[this.mInputNames.length];
        String[] strArr = this.mInputNames;
        int length = strArr.length;
        int i = 0;
        int i2 = 0;
        while (i < length) {
            int i3 = i2 + 1;
            inputs[i2] = pullInput(strArr[i]);
            i++;
            i2 = i3;
        }
        Frame output = context.getFrameManager().newFrame(inputs[0].getFormat());
        updateProgramWithTarget(inputs[0].getFormat().getTarget(), context);
        this.mProgram.process(inputs, output);
        pushOutput(this.mOutputName, output);
        output.release();
    }

    protected void updateProgramWithTarget(int target, FilterContext context) {
        if (target != this.mCurrentTarget) {
            switch (target) {
                case 2:
                    this.mProgram = getNativeProgram(context);
                    break;
                case 3:
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
