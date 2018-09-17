package android.filterpacks.text;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.format.ObjectFormat;
import java.util.Locale;

public class ToUpperCase extends Filter {
    private FrameFormat mOutputFormat;

    public ToUpperCase(String name) {
        super(name);
    }

    public void setupPorts() {
        this.mOutputFormat = ObjectFormat.fromClass(String.class, 1);
        addMaskedInputPort("mixedcase", this.mOutputFormat);
        addOutputPort("uppercase", this.mOutputFormat);
    }

    public void process(FilterContext env) {
        String inputString = (String) pullInput("mixedcase").getObjectValue();
        Frame output = env.getFrameManager().newFrame(this.mOutputFormat);
        output.setObjectValue(inputString.toUpperCase(Locale.getDefault()));
        pushOutput("uppercase", output);
    }
}
