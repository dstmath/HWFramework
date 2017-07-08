package android.filterpacks.numeric;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.format.ObjectFormat;
import android.provider.Settings.NameValueTable;
import android.speech.tts.TextToSpeech.Engine;

public class SinWaveFilter extends Filter {
    private FrameFormat mOutputFormat;
    @GenerateFieldPort(hasDefault = true, name = "stepSize")
    private float mStepSize;
    private float mValue;

    public SinWaveFilter(String name) {
        super(name);
        this.mStepSize = 0.05f;
        this.mValue = 0.0f;
    }

    public void setupPorts() {
        this.mOutputFormat = ObjectFormat.fromClass(Float.class, 1);
        addOutputPort(NameValueTable.VALUE, this.mOutputFormat);
    }

    public void open(FilterContext env) {
        this.mValue = 0.0f;
    }

    public void process(FilterContext env) {
        Frame output = env.getFrameManager().newFrame(this.mOutputFormat);
        output.setObjectValue(Float.valueOf((((float) Math.sin((double) this.mValue)) + Engine.DEFAULT_VOLUME) / 2.0f));
        pushOutput(NameValueTable.VALUE, output);
        this.mValue += this.mStepSize;
        output.release();
    }
}
