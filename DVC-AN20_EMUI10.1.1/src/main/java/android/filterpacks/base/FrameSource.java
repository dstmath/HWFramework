package android.filterpacks.base;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.GenerateFinalPort;

public class FrameSource extends Filter {
    @GenerateFinalPort(name = "format")
    private FrameFormat mFormat;
    @GenerateFieldPort(hasDefault = true, name = "frame")
    private Frame mFrame = null;
    @GenerateFieldPort(hasDefault = true, name = "repeatFrame")
    private boolean mRepeatFrame = false;

    public FrameSource(String name) {
        super(name);
    }

    @Override // android.filterfw.core.Filter
    public void setupPorts() {
        addOutputPort("frame", this.mFormat);
    }

    @Override // android.filterfw.core.Filter
    public void process(FilterContext context) {
        Frame frame = this.mFrame;
        if (frame != null) {
            pushOutput("frame", frame);
        }
        if (!this.mRepeatFrame) {
            closeOutputPort("frame");
        }
    }
}
