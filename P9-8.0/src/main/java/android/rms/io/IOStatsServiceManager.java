package android.rms.io;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.io.IIOStatsServiceManager.Stub;
import android.util.Log;

public class IOStatsServiceManager {
    private static final int QUERY_RESULT_FAIL = -1;
    private static final String TAG = "RMS.IOStatsServiceManager";
    private static IOStatsServiceManager mIOStatsManager;
    private final IIOStatsServiceManager mService = Stub.asInterface(ServiceManager.getService("iostatsservice"));

    private IOStatsServiceManager() {
    }

    public static synchronized IOStatsServiceManager getInstance() {
        IOStatsServiceManager iOStatsServiceManager;
        synchronized (IOStatsServiceManager.class) {
            if (mIOStatsManager == null) {
                mIOStatsManager = new IOStatsServiceManager();
            }
            iOStatsServiceManager = mIOStatsManager;
        }
        return iOStatsServiceManager;
    }

    public int query(int uid) {
        if (this.mService == null) {
            if (Log.HWINFO) {
                Log.i(TAG, "query service is null ");
            }
            return -1;
        }
        int queryResult = -1;
        try {
            queryResult = this.mService.query(uid);
        } catch (RemoteException e) {
            if (Log.HWINFO) {
                Log.i(TAG, "query RemoteException");
            }
        }
        return queryResult;
    }
}
