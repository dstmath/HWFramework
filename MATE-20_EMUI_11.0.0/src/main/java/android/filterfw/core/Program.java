package android.filterfw.core;

import android.annotation.UnsupportedAppUsage;

public abstract class Program {
    public abstract Object getHostValue(String str);

    @UnsupportedAppUsage
    public abstract void process(Frame[] frameArr, Frame frame);

    @UnsupportedAppUsage
    public abstract void setHostValue(String str, Object obj);

    @UnsupportedAppUsage
    public void process(Frame input, Frame output) {
        process(new Frame[]{input}, output);
    }

    public void reset() {
    }
}
