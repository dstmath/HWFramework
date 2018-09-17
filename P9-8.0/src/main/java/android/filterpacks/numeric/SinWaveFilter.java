package android.filterpacks.numeric;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.format.ObjectFormat;
import android.hardware.camera2.params.TonemapCurve;

public class SinWaveFilter extends Filter {
    private FrameFormat mOutputFormat;
    @GenerateFieldPort(hasDefault = true, name = "stepSize")
    private float mStepSize = 0.05f;
    private float mValue = TonemapCurve.LEVEL_BLACK;

    public SinWaveFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        this.mOutputFormat = ObjectFormat.fromClass(Float.class, 1);
        addOutputPort("value", this.mOutputFormat);
    }

    public void open(FilterContext env) {
        this.mValue = TonemapCurve.LEVEL_BLACK;
    }

    public void process(FilterContext env) {
        Frame output = env.getFrameManager().newFrame(this.mOutputFormat);
        output.setObjectValue(Float.valueOf((((float) Math.sin((double) this.mValue)) + 1.0f) / 2.0f));
        pushOutput("value", output);
        this.mValue += this.mStepSize;
        output.release();
    }
}
