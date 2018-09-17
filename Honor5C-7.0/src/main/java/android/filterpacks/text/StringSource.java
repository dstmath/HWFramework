package android.filterpacks.text;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.format.ObjectFormat;

public class StringSource extends Filter {
    private FrameFormat mOutputFormat;
    @GenerateFieldPort(name = "stringValue")
    private String mString;

    public StringSource(String name) {
        super(name);
    }

    public void setupPorts() {
        this.mOutputFormat = ObjectFormat.fromClass(String.class, 1);
        addOutputPort("string", this.mOutputFormat);
    }

    public void process(FilterContext env) {
        Frame output = env.getFrameManager().newFrame(this.mOutputFormat);
        output.setObjectValue(this.mString);
        output.setTimestamp(-1);
        pushOutput("string", output);
        closeOutputPort("string");
    }
}
