package android.widget;

import com.huawei.pgmng.log.LogPower;

public class HwWechatOptimizeImplDummy implements IHwWechatOptimize {
    public boolean isWechatOptimizeEffect() {
        return false;
    }

    public int getWechatFlingVelocity() {
        return LogPower.PC_WEBVIEW_END;
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
