package ohos.global.icu.text;

public class BidiClassifier {
    protected Object context;

    public int classify(int i) {
        return 23;
    }

    public BidiClassifier(Object obj) {
        this.context = obj;
    }

    public void setContext(Object obj) {
        this.context = obj;
    }

    public Object getContext() {
        return this.context;
    }
}
