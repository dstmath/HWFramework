package android.filterpacks.base;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.FrameFormat;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.GenerateFinalPort;
import android.filterfw.format.ObjectFormat;

public class ObjectSource extends Filter {
    private Frame mFrame;
    @GenerateFieldPort(name = "object")
    private Object mObject;
    @GenerateFinalPort(hasDefault = true, name = "format")
    private FrameFormat mOutputFormat = FrameFormat.unspecified();
    @GenerateFieldPort(hasDefault = true, name = "repeatFrame")
    boolean mRepeatFrame = false;

    public ObjectSource(String name) {
        super(name);
    }

    @Override // android.filterfw.core.Filter
    public void setupPorts() {
        addOutputPort("frame", this.mOutputFormat);
    }

    @Override // android.filterfw.core.Filter
    public void process(FilterContext context) {
        if (this.mFrame == null) {
            Object obj = this.mObject;
            if (obj != null) {
                this.mFrame = context.getFrameManager().newFrame(ObjectFormat.fromObject(obj, 1));
                this.mFrame.setObjectValue(this.mObject);
                this.mFrame.setTimestamp(-1);
            } else {
                throw new NullPointerException("ObjectSource producing frame with no object set!");
            }
        }
        pushOutput("frame", this.mFrame);
        if (!this.mRepeatFrame) {
            closeOutputPort("frame");
        }
    }

    @Override // android.filterfw.core.Filter
    public void tearDown(FilterContext context) {
        this.mFrame.release();
    }

    @Override // android.filterfw.core.Filter
    public void fieldPortValueUpdated(String name, FilterContext context) {
        Frame frame;
        if (name.equals("object") && (frame = this.mFrame) != null) {
            frame.release();
            this.mFrame = null;
        }
    }
}
