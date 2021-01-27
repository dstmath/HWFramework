package huawei.android.widget;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.widget.HwWidgetAppAttrsHelper;

public class HwWidgetAppAttrsHelperImpl implements HwWidgetAppAttrsHelper {
    private static final int EVENT_GET_APP_SCROLL_TOP_ENABLE = 40002;
    private static final String HW_SYS_RES_MANAGER = "hwsysresmanager";
    private static final String IHWSYSRESMANAGER_INTERFACE_NAME = "android.rms.IHwSysResManager";
    private static final long INQUIRE_TIME_INTERVAL = 300000;
    private static final String TAG = "HwWidgetAppAttrsHelperImpl";
    private static final long TIME_INIT = -1;
    private static long sInquireTime = TIME_INIT;
    private static boolean sIsEnabled = false;
    private IBinder mAwareService;
    private Context mContext;

    public HwWidgetAppAttrsHelperImpl(Context context) {
        this.mContext = context;
    }

    public boolean isScrollTopEnabled(String appName) {
        if (!isNeedInquire()) {
            return sIsEnabled;
        }
        if (appName == null) {
            Log.e(TAG, "isScrollTopEnabled: appName is null");
            return false;
        }
        if (this.mAwareService == null) {
            this.mAwareService = getAwareService();
            if (this.mAwareService == null) {
                Log.e(TAG, "isScrollTopEnabled: mAwareService is null");
                return false;
            }
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IHWSYSRESMANAGER_INTERFACE_NAME);
            data.writeString(appName);
            this.mAwareService.transact(EVENT_GET_APP_SCROLL_TOP_ENABLE, data, reply, 0);
            reply.readException();
            sIsEnabled = reply.readBoolean();
            sInquireTime = System.currentTimeMillis();
        } catch (RemoteException e) {
            Log.e(TAG, "mAwareService ontransact has remoteException");
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return sIsEnabled;
    }

    private IBinder getAwareService() {
        return ServiceManager.getService(HW_SYS_RES_MANAGER);
    }

    private boolean isNeedInquire() {
        long currentTime = System.currentTimeMillis();
        if (currentTime < sInquireTime) {
            sInquireTime = currentTime;
        }
        long j = sInquireTime;
        if (j == TIME_INIT || currentTime - j > INQUIRE_TIME_INTERVAL) {
            return true;
        }
        return false;
    }
}
