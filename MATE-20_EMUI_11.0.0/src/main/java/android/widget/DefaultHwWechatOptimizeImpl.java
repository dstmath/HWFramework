package android.widget;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultHwWechatOptimizeImpl implements IHwWechatOptimize {
    @Override // android.widget.IHwWechatOptimize
    public boolean isWechatOptimizeEffect() {
        return false;
    }

    @Override // android.widget.IHwWechatOptimize
    public int getWechatFlingVelocity() {
        return 150;
    }

    @Override // android.widget.IHwWechatOptimize
    public int getWechatIdleVelocity() {
        return 0;
    }

    @Override // android.widget.IHwWechatOptimize
    public boolean isWechatFling() {
        return false;
    }

    @Override // android.widget.IHwWechatOptimize
    public void setWechatFling(boolean isFling) {
    }
}
