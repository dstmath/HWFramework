package android.filterpacks.numeric;

import android.app.slice.Slice;
import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.format.ObjectFormat;

public class SinWaveFilter extends Filter {
    private FrameFormat mOutputFormat;
    @GenerateFieldPort(hasDefault = true, name = "stepSize")
    private float mStepSize = 0.05f;
    private float mValue = 0.0f;

    public SinWaveFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        this.mOutputFormat = ObjectFormat.fromClass(Float.class, 1);
        addOutputPort(Slice.SUBTYPE_VALUE, this.mOutputFormat);
    }

    public void open(FilterContext env) {
        this.mValue = 0.0f;
    }

    public void process(FilterContext env) {
        Frame output = env.getFrameManager().newFrame(this.mOutputFormat);
        output.setObjectValue(Float.valueOf((((float) Math.sin((double) this.mValue)) + 1.0f) / 2.0f));
        pushOutput(Slice.SUBTYPE_VALUE, output);
        this.mValue += this.mStepSize;
        output.release();
    }
}
