package ohos.agp.render.render3d;

public abstract class Task {
    private State mState = State.QUEUED;

    public enum State {
        QUEUED,
        RUNNING,
        FINISHED
    }

    public abstract void onCancel();

    public abstract boolean onExecute();

    public abstract void onFinish();

    public abstract void onInitialize();

    public final State getState() {
        return this.mState;
    }

    public final void initialize() {
        onInitialize();
        this.mState = State.RUNNING;
    }
}
