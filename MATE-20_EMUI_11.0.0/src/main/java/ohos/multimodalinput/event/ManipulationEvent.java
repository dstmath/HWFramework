package ohos.multimodalinput.event;

public abstract class ManipulationEvent extends MultimodalEvent {
    public static final int PHASE_CANCEL = 4;
    public static final int PHASE_COMPLETED = 3;
    public static final int PHASE_MOVE = 2;
    public static final int PHASE_NONE = 0;
    public static final int PHASE_START = 1;

    public abstract float getForce(int i);

    public abstract int getPhase();

    public abstract int getPointerCount();

    public abstract int getPointerId(int i);

    public abstract MmiPoint getPointerPosition(int i);

    public abstract MmiPoint getPointerScreenPosition(int i);

    public abstract float getRadius(int i);

    public abstract long getStartTime();

    public abstract void setScreenOffset(float f, float f2);
}
