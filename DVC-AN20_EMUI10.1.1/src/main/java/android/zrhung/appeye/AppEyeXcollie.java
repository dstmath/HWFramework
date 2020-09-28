package android.zrhung.appeye;

import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;

public final class AppEyeXcollie extends ZrHungImpl {
    private static final String TAG = "ZrHung.AppEyeXcollie";
    private static AppEyeXcollie singleton;

    public AppEyeXcollie(String wpName) {
        super(wpName);
    }

    public static synchronized AppEyeXcollie getInstance(String wpName) {
        AppEyeXcollie appEyeXcollie;
        synchronized (AppEyeXcollie.class) {
            if (singleton == null) {
                singleton = new AppEyeXcollie(wpName);
            }
            appEyeXcollie = singleton;
        }
        return appEyeXcollie;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean sendEvent(ZrHungData args) {
        if (args == null) {
            return false;
        }
        return ZRHung.sendHungEvent(ZRHung.XCOLLIE_FWK_SERVICE, "p=" + args.getInt("pid") + ",B", args.getString("stackTrace"));
    }
}
