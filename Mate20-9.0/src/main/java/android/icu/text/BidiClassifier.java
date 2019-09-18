package android.icu.text;

public class BidiClassifier {
    protected Object context;

    public BidiClassifier(Object context2) {
        this.context = context2;
    }

    public void setContext(Object context2) {
        this.context = context2;
    }

    public Object getContext() {
        return this.context;
    }

    public int classify(int c) {
        return 23;
    }
}
