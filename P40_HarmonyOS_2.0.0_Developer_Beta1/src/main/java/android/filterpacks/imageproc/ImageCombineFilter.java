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

    /* access modifiers changed from: protected */
    public abstract Program getNativeProgram(FilterContext filterContext);

    /* access modifiers changed from: protected */
    public abstract Program getShaderProgram(FilterContext filterContext);

    public ImageCombineFilter(String name, String[] inputNames, String outputName, String parameterName) {
        super(name);
        this.mInputNames = inputNames;
        this.mOutputName = outputName;
        this.mParameterName = parameterName;
    }

    @Override // android.filterfw.core.Filter
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

    @Override // android.filterfw.core.Filter
    public FrameFormat getOutputFormat(String portName, FrameFormat inputFormat) {
        return inputFormat;
    }

    private void assertAllInputTargetsMatch() {
        int target = getInputFormat(this.mInputNames[0]).getTarget();
        for (String inputName : this.mInputNames) {
            if (target != getInputFormat(inputName).getTarget()) {
                throw new RuntimeException("Type mismatch of input formats in filter " + this + ". All input frames must have the same target!");
            }
        }
    }

    @Override // android.filterfw.core.Filter
    public void process(FilterContext context) {
        String[] strArr = this.mInputNames;
        Frame[] inputs = new Frame[strArr.length];
        int length = strArr.length;
        int i = 0;
        int i2 = 0;
        while (i2 < length) {
            inputs[i] = pullInput(strArr[i2]);
            i2++;
            i++;
        }
        Frame output = context.getFrameManager().newFrame(inputs[0].getFormat());
        updateProgramWithTarget(inputs[0].getFormat().getTarget(), context);
        this.mProgram.process(inputs, output);
        pushOutput(this.mOutputName, output);
        output.release();
    }

    /* access modifiers changed from: protected */
    public void updateProgramWithTarget(int target, FilterContext context) {
        if (target != this.mCurrentTarget) {
            if (target == 2) {
                this.mProgram = getNativeProgram(context);
            } else if (target != 3) {
                this.mProgram = null;
            } else {
                this.mProgram = getShaderProgram(context);
            }
            Program program = this.mProgram;
            if (program != null) {
                initProgramInputs(program, context);
                this.mCurrentTarget = target;
                return;
            }
            throw new RuntimeException("Could not create a program for image filter " + this + "!");
        }
    }
}
