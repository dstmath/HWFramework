package huawei.android.security.facerecognition;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemClock;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hardware.face.FaceAuthenticationManager;
import com.huawei.hwpartsecurity.BuildConfig;

public class FaceReportEventToIaware {
    private static final String CHIPSET = SystemPropertiesEx.get("ro.hardware", BuildConfig.FLAVOR);
    private static final long MIN_RUNNING_TIME = 1000;
    private static final String TAG = "FaceReportEventToIaware";
    private static volatile boolean mIsRunning = false;
    private static volatile int mLastEventId = -1;
    private static volatile long mLastRuntime = 0;
    private static final Object sReportEventLock = new Object();

    /* access modifiers changed from: private */
    public static void setEventToIawareRunning(int eventId) {
        mIsRunning = true;
        SlogEx.i(TAG, "Start time.");
        IBinder awareService = ServiceManagerEx.getService("hwsysresmanager");
        if (awareService != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken("android.rms.IHwSysResManager");
                awareService.transact(eventId, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                SlogEx.e(TAG, "AwareService ontransact failed.");
            } catch (Throwable th) {
                data.recycle();
                reply.recycle();
                SlogEx.i(TAG, "End time.");
                throw th;
            }
            data.recycle();
            reply.recycle();
            SlogEx.i(TAG, "End time.");
            return;
        }
        SlogEx.i(TAG, "AwareService not start.");
    }

    public static void reportEventToIaware(Context context, int eventId) {
        if (CHIPSET.contains("mt")) {
            if (mIsRunning) {
                SlogEx.e(TAG, "Report is runnning, return.");
            } else if (context == null) {
                SlogEx.e(TAG, "Context is null, return.");
            } else {
                FaceAuthenticationManager faceAuthenticationManager = new FaceAuthenticationManager(context);
                if (faceAuthenticationManager.isHardwareDetected() && faceAuthenticationManager.hasEnrolledFace()) {
                    synchronized (sReportEventLock) {
                        if (mIsRunning) {
                            SlogEx.e(TAG, "Report is runnning, return.");
                            return;
                        }
                        long currentTime = SystemClock.elapsedRealtime();
                        if (mLastRuntime == 0 || currentTime - mLastRuntime >= MIN_RUNNING_TIME || mLastEventId != eventId) {
                            startReportEventThread(eventId, currentTime);
                        } else {
                            SlogEx.i(TAG, "Last one second run.");
                        }
                    }
                }
            }
        }
    }

    private static void startReportEventThread(final int finalEventId, final long finalCurrentTime) {
        new Thread(new Runnable() {
            /* class huawei.android.security.facerecognition.FaceReportEventToIaware.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                if (FaceReportEventToIaware.mIsRunning) {
                    SlogEx.e(FaceReportEventToIaware.TAG, "Report is runnning, thread return.");
                    return;
                }
                synchronized (FaceReportEventToIaware.sReportEventLock) {
                    if (FaceReportEventToIaware.mIsRunning) {
                        SlogEx.e(FaceReportEventToIaware.TAG, "Report is runnning, thread return.");
                        return;
                    }
                    FaceReportEventToIaware.setEventToIawareRunning(finalEventId);
                    boolean unused = FaceReportEventToIaware.mIsRunning = false;
                    long unused2 = FaceReportEventToIaware.mLastRuntime = finalCurrentTime;
                    int unused3 = FaceReportEventToIaware.mLastEventId = finalEventId;
                }
            }
        }).start();
    }
}
