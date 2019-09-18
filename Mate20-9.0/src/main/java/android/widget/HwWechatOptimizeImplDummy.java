package android.widget;

public class HwWechatOptimizeImplDummy implements IHwWechatOptimize {
    public boolean isWechatOptimizeEffect() {
        return false;
    }

    public int getWechatFlingVelocity() {
        return 150;
    }

    public int getWechatIdleVelocity() {
        return 0;
    }

    public boolean isWechatFling() {
        return false;
    }

    public void setWechatFling(boolean isFling) {
    }
}
