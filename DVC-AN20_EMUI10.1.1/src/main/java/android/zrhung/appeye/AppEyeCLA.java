package android.zrhung.appeye;

import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;

public final class AppEyeCLA extends ZrHungImpl {
    private static final String EVENT_TAG = "CLA";
    private static final String TAG = "ZrHung.AppEyeCLA";
    private static AppEyeCLA singleton;

    public AppEyeCLA(String wpName) {
        super(wpName);
    }

    public static synchronized AppEyeCLA getInstance(String wpName) {
        AppEyeCLA appEyeCLA;
        synchronized (AppEyeCLA.class) {
            if (singleton == null) {
                singleton = new AppEyeCLA(wpName);
            }
            appEyeCLA = singleton;
        }
        return appEyeCLA;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean sendEvent(ZrHungData zrHungData) {
        return ZRHung.sendHungEvent(ZRHung.APPEYE_CLA, null, EVENT_TAG);
    }
}
