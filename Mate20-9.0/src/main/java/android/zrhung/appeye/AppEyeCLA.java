package android.zrhung.appeye;

import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;

public final class AppEyeCLA extends ZrHungImpl {
    private static final String KEYWORD = "CLA";
    private static final String TAG = "ZrHung.AppEyeCLA";
    private static AppEyeCLA mSingleton;

    public AppEyeCLA(String wpName) {
        super(wpName);
    }

    public static synchronized AppEyeCLA getInstance(String wpName) {
        AppEyeCLA appEyeCLA;
        synchronized (AppEyeCLA.class) {
            if (mSingleton == null) {
                mSingleton = new AppEyeCLA(wpName);
            }
            appEyeCLA = mSingleton;
        }
        return appEyeCLA;
    }

    public boolean sendEvent(ZrHungData args) {
        return ZRHung.sendHungEvent(266, null, KEYWORD);
    }
}
