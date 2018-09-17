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

    public void setupPorts() {
        addOutputPort("frame", this.mOutputFormat);
    }

    public void process(FilterContext context) {
        if (this.mFrame == null) {
            if (this.mObject == null) {
                throw new NullPointerException("ObjectSource producing frame with no object set!");
            }
            this.mFrame = context.getFrameManager().newFrame(ObjectFormat.fromObject(this.mObject, 1));
            this.mFrame.setObjectValue(this.mObject);
            this.mFrame.setTimestamp(-1);
        }
        pushOutput("frame", this.mFrame);
        if (!this.mRepeatFrame) {
            closeOutputPort("frame");
        }
    }

    public void tearDown(FilterContext context) {
        this.mFrame.release();
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (name.equals("object") && this.mFrame != null) {
            this.mFrame.release();
            this.mFrame = null;
        }
    }
}
