package ohos.miscservices.area;

import ohos.annotation.SystemApi;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

@SystemApi
public class Area {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "Area");
    private static volatile Area sInstance;
    private Context mAppContext;
    private IAreaSysAbility mAreaSysAbility = null;
    private String mPackageName = "";

    private Area(Context context) {
        this.mAppContext = context;
        if (this.mAppContext.getApplicationInfo() != null) {
            this.mPackageName = this.mAppContext.getApplicationInfo().getName();
        }
        this.mAreaSysAbility = AreaProxy.getAreaSysAbility(context);
    }

    public static Area getInstance(Context context) {
        if (context == null) {
            return null;
        }
        if (sInstance == null) {
            synchronized (Area.class) {
                if (sInstance == null) {
                    sInstance = new Area(context);
                }
            }
        }
        return sInstance;
    }

    public String getISOAlpha2Code() {
        try {
            return this.mAreaSysAbility.getISOAlpha2Code();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "area system ability is not ready!!", new Object[0]);
            return "";
        }
    }

    public void addAreaListener(IAreaListener iAreaListener) {
        if (iAreaListener == null) {
            HiLog.error(TAG, "Invaild listener object", new Object[0]);
            return;
        }
        try {
            this.mAreaSysAbility.addAreaListener(iAreaListener);
            HiLog.info(TAG, "Successfully add listener: %{public}s", this.mPackageName);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "add listener failed!", new Object[0]);
        }
    }

    public void removeAreaListener(IAreaListener iAreaListener) {
        if (iAreaListener == null) {
            HiLog.error(TAG, "Invaild listener object", new Object[0]);
            return;
        }
        try {
            this.mAreaSysAbility.removeAreaListener(iAreaListener);
            HiLog.info(TAG, "Successfully remove listener: %{public}s", this.mPackageName);
        } catch (RemoteException unused) {
            HiLog.error(TAG, "remove listener failed!", new Object[0]);
        }
    }
}
