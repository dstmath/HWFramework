package android.magicwin;

import android.os.Bundle;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultHwMagicWindowManager implements HwMagicWindow {
    private static HwMagicWindow sInstance = new DefaultHwMagicWindowManager();

    @HwSystemApi
    public static HwMagicWindow getInstance() {
        return sInstance;
    }

    @Override // android.magicwin.HwMagicWindow
    @HwSystemApi
    public IHwMagicWindow getService() {
        return null;
    }

    @Override // android.magicwin.HwMagicWindow
    @HwSystemApi
    public Bundle performHwMagicWindowPolicy(int policy, Object... params) {
        return null;
    }
}
