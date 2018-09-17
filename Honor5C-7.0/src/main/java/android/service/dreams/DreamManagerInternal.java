package android.service.dreams;

public abstract class DreamManagerInternal {
    public abstract boolean isDreaming();

    public abstract void startDream(boolean z);

    public abstract void stopDream(boolean z);
}
