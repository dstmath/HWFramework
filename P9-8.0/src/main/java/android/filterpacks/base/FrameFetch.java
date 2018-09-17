package android.filterpacks.base;

import android.R;
import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.GenerateFinalPort;

public class FrameFetch extends Filter {
    @GenerateFinalPort(hasDefault = true, name = "format")
    private FrameFormat mFormat;
    @GenerateFieldPort(name = "key")
    private String mKey;
    @GenerateFieldPort(hasDefault = true, name = "repeatFrame")
    private boolean mRepeatFrame = false;

    public FrameFetch(String name) {
        super(name);
    }

    public void setupPorts() {
        addOutputPort("frame", this.mFormat == null ? FrameFormat.unspecified() : this.mFormat);
    }

    public void process(FilterContext context) {
        Frame output = context.fetchFrame(this.mKey);
        if (output != null) {
            pushOutput("frame", output);
            if (!this.mRepeatFrame) {
                closeOutputPort("frame");
                return;
            }
            return;
        }
        delayNextProcess(R.styleable.Theme_timePickerDialogTheme);
    }
}
