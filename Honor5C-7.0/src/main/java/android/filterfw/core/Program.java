package android.filterfw.core;

public abstract class Program {
    public abstract Object getHostValue(String str);

    public abstract void process(Frame[] frameArr, Frame frame);

    public abstract void setHostValue(String str, Object obj);

    public void process(Frame input, Frame output) {
        process(new Frame[]{input}, output);
    }

    public void reset() {
    }
}
