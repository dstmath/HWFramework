package huawei.android.security.facerecognition;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import com.huawei.hardware.face.FaceAuthenticationManager;

public class FaceReportEventToIaware {
    private static final String TAG = "FaceReportEventToIaware";
    private static final String chipset = SystemProperties.get("ro.hardware", "");
    /* access modifiers changed from: private */
    public static volatile boolean mIsRunning = false;
    /* access modifiers changed from: private */
    public static volatile int mLastEventId = -1;
    /* access modifiers changed from: private */
    public static volatile long mLastRuntime = 0;
    /* access modifiers changed from: private */
    public static Object mReportEventLock = new Object();

    /* access modifiers changed from: private */
    public static void setEventToIawareRunning(int eventID) {
        mIsRunning = true;
        Slog.i(TAG, "Start time.");
        IBinder awareService = ServiceManager.getService("hwsysresmanager");
        if (awareService != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken("android.rms.IHwSysResManager");
                awareService.transact(eventID, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                Slog.e(TAG, "AwareService ontransact failed.");
            } catch (Throwable th) {
                data.recycle();
                reply.recycle();
                Slog.i(TAG, "End time.");
                throw th;
            }
            data.recycle();
            reply.recycle();
            Slog.i(TAG, "End time.");
            return;
        }
        Slog.i(TAG, "AwareService not start.");
    }

    public static void reportEventToIaware(Context context, final int eventID) {
        if (chipset.contains("mt")) {
            if (mIsRunning) {
                Slog.e(TAG, "Report is runnning, return.");
            } else if (context == null) {
                Slog.e(TAG, "Context is null, return.");
            } else {
                FaceAuthenticationManager faceAuthenticationManager = new FaceAuthenticationManager(context);
                if (faceAuthenticationManager.isHardwareDetected() && faceAuthenticationManager.hasEnrolledFace()) {
                    synchronized (mReportEventLock) {
                        if (mIsRunning) {
                            Slog.e(TAG, "Report is runnning, return.");
                            return;
                        }
                        final long currentTime = SystemClock.elapsedRealtime();
                        if (mLastRuntime == 0 || currentTime - mLastRuntime >= 1000 || mLastEventId != eventID) {
                            new Thread(new Runnable() {
                                public void run() {
                                    if (FaceReportEventToIaware.mIsRunning) {
                                        Slog.e(FaceReportEventToIaware.TAG, "Report is runnning, thread return.");
                                        return;
                                    }
                                    synchronized (FaceReportEventToIaware.mReportEventLock) {
                                        if (FaceReportEventToIaware.mIsRunning) {
                                            Slog.e(FaceReportEventToIaware.TAG, "Report is runnning, thread return.");
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
                            Slog.i(TAG, "Last one second run.");
                        }
                    }
                }
            }
        }
    }
}
