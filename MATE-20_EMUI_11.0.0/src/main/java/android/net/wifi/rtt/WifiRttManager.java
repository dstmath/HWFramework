package android.net.wifi.rtt;

import android.annotation.SystemApi;
import android.content.Context;
import android.net.wifi.rtt.IRttCallback;
import android.os.Binder;
import android.os.RemoteException;
import android.os.WorkSource;
import java.util.List;
import java.util.concurrent.Executor;

public class WifiRttManager {
    public static final String ACTION_WIFI_RTT_STATE_CHANGED = "android.net.wifi.rtt.action.WIFI_RTT_STATE_CHANGED";
    private static final String TAG = "WifiRttManager";
    private static final boolean VDBG = false;
    private final Context mContext;
    private final IWifiRttManager mService;

    public WifiRttManager(Context context, IWifiRttManager service) {
        this.mContext = context;
        this.mService = service;
    }

    public boolean isAvailable() {
        try {
            return this.mService.isAvailable();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startRanging(RangingRequest request, Executor executor, RangingResultCallback callback) {
        startRanging(null, request, executor, callback);
    }

    @SystemApi
    public void startRanging(WorkSource workSource, RangingRequest request, final Executor executor, final RangingResultCallback callback) {
        if (executor == null) {
            throw new IllegalArgumentException("Null executor provided");
        } else if (callback != null) {
            try {
                this.mService.startRanging(new Binder(), this.mContext.getOpPackageName(), workSource, request, new IRttCallback.Stub() {
                    /* class android.net.wifi.rtt.WifiRttManager.AnonymousClass1 */

                    @Override // android.net.wifi.rtt.IRttCallback
                    public void onRangingFailure(int status) throws RemoteException {
                        clearCallingIdentity();
                        executor.execute(new Runnable(status) {
                            /* class android.net.wifi.rtt.$$Lambda$WifiRttManager$1$j3tVizFtxt_z0tTXfTNSFM4Loi8 */
                            private final /* synthetic */ int f$1;

                            {
                                this.f$1 = r2;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                RangingResultCallback.this.onRangingFailure(this.f$1);
                            }
                        });
                    }

                    @Override // android.net.wifi.rtt.IRttCallback
                    public void onRangingResults(List<RangingResult> results) throws RemoteException {
                        clearCallingIdentity();
                        executor.execute(new Runnable(results) {
                            /* class android.net.wifi.rtt.$$Lambda$WifiRttManager$1$3uT7vOEOvW11feiFUB6LWvcBJEk */
                            private final /* synthetic */ List f$1;

                            {
                                this.f$1 = r2;
                            }

                            @Override // java.lang.Runnable
                            public final void run() {
                                RangingResultCallback.this.onRangingResults(this.f$1);
                            }
                        });
                    }
                });
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("Null callback provided");
        }
    }

    @SystemApi
    public void cancelRanging(WorkSource workSource) {
        try {
            this.mService.cancelRanging(workSource);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
