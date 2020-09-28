package android.zrhung.appeye;

import android.rms.iaware.DataContract;
import android.util.ZRHung;
import android.zrhung.ZrHungData;
import android.zrhung.ZrHungImpl;

public final class AppEyeCL extends ZrHungImpl {
    private static final String EVENT_TAG = "CL";
    private static final String TAG = "ZrHung.AppEyeCL";
    private static AppEyeCL singleton;

    public AppEyeCL(String wpName) {
        super(wpName);
    }

    public static synchronized AppEyeCL getInstance(String wpName) {
        AppEyeCL appEyeCL;
        synchronized (AppEyeCL.class) {
            if (singleton == null) {
                singleton = new AppEyeCL(wpName);
            }
            appEyeCL = singleton;
        }
        return appEyeCL;
    }

    @Override // android.zrhung.ZrHungImpl
    public boolean sendEvent(ZrHungData args) {
        return sendAppEyeEvent(ZRHung.APPEYE_CL, args, null, "CL:" + args.getString(DataContract.BaseProperty.PACKAGE_NAME));
    }
}
