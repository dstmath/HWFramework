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
    private static final String TAG = "FaceReportEventToIaware";
    private static volatile boolean mIsRunning = false;
    private static volatile int mLastEventId = -1;
    private static volatile long mLastRuntime = 0;
    private static final Object mReportEventLock = new Object();

    /* access modifiers changed from: private */
    public static void setEventToIawareRunning(int eventID) {
        mIsRunning = true;
        SlogEx.i(TAG, "Start time.");
        IBinder awareService = ServiceManagerEx.getService("hwsysresmanager");
        if (awareService != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken("android.rms.IHwSysResManager");
                awareService.transact(eventID, data, reply, 0);
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

    public static void reportEventToIaware(Context context, final int eventID) {
        if (CHIPSET.contains("mt")) {
            if (mIsRunning) {
                SlogEx.e(TAG, "Report is runnning, return.");
            } else if (context == null) {
                SlogEx.e(TAG, "Context is null, return.");
            } else {
                FaceAuthenticationManager faceAuthenticationManager = new FaceAuthenticationManager(context);
                if (faceAuthenticationManager.isHardwareDetected() && faceAuthenticationManager.hasEnrolledFace()) {
                    synchronized (mReportEventLock) {
                        if (mIsRunning) {
                            SlogEx.e(TAG, "Report is runnning, return.");
                            return;
                        }
                        final long currentTime = SystemClock.elapsedRealtime();
                        if (mLastRuntime == 0 || currentTime - mLastRuntime >= 1000 || mLastEventId != eventID) {
                            new Thread(new Runnable() {
                                /* class huawei.android.security.facerecognition.FaceReportEventToIaware.AnonymousClass1 */

                                public void run() {
                                    if (FaceReportEventToIaware.mIsRunning) {
                                        SlogEx.e(FaceReportEventToIaware.TAG, "Report is runnning, thread return.");
                                        return;
                                    }
                                    synchronized (FaceReportEventToIaware.mReportEventLock) {
                                        if (FaceReportEventToIaware.mIsRunning) {
                                            SlogEx.e(FaceReportEventToIaware.TAG, "Report is runnning, thread return.");
                                            return;
                                        }
                                        FaceReportEventToIaware.setEventToIawareRunning(eventID);
                                        boolean unused = FaceReportEventToIaware.mIsRunning = false;
                                        long unused2 = FaceReportEventToIaware.mLastRuntime = currentTime;
                                        int unused3 = FaceReportEventToIaware.mLastEventId = eventID;
                                    }
                                }
                            }).start();
                        } else {
                            SlogEx.i(TAG, "Last one second run.");
                        }
                    }
                }
            }
        }
    }
}
