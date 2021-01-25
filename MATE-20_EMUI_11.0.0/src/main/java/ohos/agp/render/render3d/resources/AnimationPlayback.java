package ohos.agp.render.render3d.resources;

public interface AnimationPlayback {
    public static final int REPEAT_COUNT_INFINITE = Integer.MAX_VALUE;

    public enum State {
        STOP,
        PLAY,
        PAUSE
    }

    State getPlaybackState();

    int getRepeatCount();

    float getSpeed();

    float getWeight();

    boolean isCompleted();

    void release();

    void setPlaybackState(State state);

    void setRepeatCount(int i);

    void setSpeed(float f);

    void setWeight(float f);
}
