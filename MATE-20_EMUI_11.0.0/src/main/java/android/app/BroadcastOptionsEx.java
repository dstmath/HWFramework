package android.app;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class BroadcastOptionsEx {
    private BroadcastOptions mBroadcastOptions;

    public BroadcastOptionsEx() {
    }

    public BroadcastOptionsEx(BroadcastOptions options) {
        this.mBroadcastOptions = options;
    }

    public BroadcastOptions getBroadcastOptions() {
        return this.mBroadcastOptions;
    }
}
