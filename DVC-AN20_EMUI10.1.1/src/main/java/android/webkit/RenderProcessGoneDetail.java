package android.webkit;

public abstract class RenderProcessGoneDetail {
    public abstract boolean didCrash();

    public abstract int rendererPriorityAtExit();
}
