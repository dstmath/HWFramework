package android.widget;

public interface IHwWechatOptimize {
    int getWechatFlingVelocity();

    int getWechatIdleVelocity();

    boolean isWechatFling();

    boolean isWechatOptimizeEffect();

    void setWechatFling(boolean z);
}
