package com.android.server.security.deviceusage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.NtpTrustedTime;
import android.util.Slog;
import com.android.server.HwConnectivityService;
import java.util.Date;

public class HwDeviceFirstUseTime {
    private static final long GET_TIME_DELAY = 3600000;
    private static final long GET_TIME_DELAY_MOBILE_CONNCTION = 21600000;
    private static final boolean HW_DEBUG = false;
    private static final long NTP_INTERVAL = 86400000;
    private static final String TAG = "HwDeviceFirstUseTime";
    private static int TYPE_HAS_GET_TIME;
    private Runnable getTimeRunnable;
    private boolean isGetTimeFlag;
    private final BroadcastReceiver mBroadcastReceiver;
    private Handler mCollectionHandler;
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private long mCurrentTime;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private NtpTrustedTime mNtpTime;
    private long mTime;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.deviceusage.HwDeviceFirstUseTime.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.deviceusage.HwDeviceFirstUseTime.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.deviceusage.HwDeviceFirstUseTime.<clinit>():void");
    }

    public HwDeviceFirstUseTime(Context context, Handler handler) {
        this.isGetTimeFlag = HW_DEBUG;
        this.mCurrentTime = -1;
        this.getTimeRunnable = new Runnable() {
            public void run() {
                if (HwDeviceFirstUseTime.HW_DEBUG) {
                    Slog.d(HwDeviceFirstUseTime.TAG, "threadRun");
                }
                if (!HwDeviceFirstUseTime.this.isGetTimeFlag) {
                    if (HwDeviceFirstUseTime.this.mCurrentTime == -1 || !HwDeviceFirstUseTime.this.isMobileNetworkConnected() || HwDeviceFirstUseTime.this.mCurrentTime - SystemClock.elapsedRealtime() > HwDeviceFirstUseTime.GET_TIME_DELAY_MOBILE_CONNCTION) {
                        if (HwDeviceFirstUseTime.this.mNtpTime.getCacheAge() >= HwDeviceFirstUseTime.NTP_INTERVAL) {
                            boolean forceRefresh = HwDeviceFirstUseTime.this.mNtpTime.forceRefresh();
                        }
                        if (HwDeviceFirstUseTime.this.mNtpTime.getCacheAge() < HwDeviceFirstUseTime.NTP_INTERVAL) {
                            HwDeviceFirstUseTime.this.mTime = HwDeviceFirstUseTime.this.mNtpTime.getCachedNtpTime();
                            if (HwDeviceFirstUseTime.this.mTime != 0) {
                                HwDeviceFirstUseTime.this.obtianHasGetTime(HwDeviceFirstUseTime.this.mTime);
                            }
                            if (HwDeviceFirstUseTime.HW_DEBUG) {
                                Slog.d(HwDeviceFirstUseTime.TAG, "NTP server returned: " + HwDeviceFirstUseTime.this.mTime + " (" + new Date(HwDeviceFirstUseTime.this.mTime) + ")");
                            }
                            return;
                        }
                        if (HwDeviceFirstUseTime.this.isNetworkConnected()) {
                            HwDeviceFirstUseTime.this.mCurrentTime = SystemClock.elapsedRealtime();
                            HwDeviceFirstUseTime.this.mHandler.postDelayed(HwDeviceFirstUseTime.this.getTimeRunnable, HwDeviceFirstUseTime.GET_TIME_DELAY);
                        } else {
                            HwDeviceFirstUseTime.this.mHandler.removeCallbacks(HwDeviceFirstUseTime.this.getTimeRunnable);
                        }
                        return;
                    }
                    HwDeviceFirstUseTime.this.mHandler.postDelayed(HwDeviceFirstUseTime.this.getTimeRunnable, HwDeviceFirstUseTime.GET_TIME_DELAY_MOBILE_CONNCTION);
                }
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NetworkInfo networkInfo = HwDeviceFirstUseTime.this.mConnectivityManager.getActiveNetworkInfo();
                String action = intent.getAction();
                if (HwDeviceFirstUseTime.HW_DEBUG) {
                    Slog.d(HwDeviceFirstUseTime.TAG, "action  " + action);
                }
                if (!HwConnectivityService.CONNECTIVITY_CHANGE_ACTION.equals(action)) {
                    Slog.e(HwDeviceFirstUseTime.TAG, "Receive error broadcast");
                } else if (networkInfo != null && networkInfo.isAvailable() && !HwDeviceFirstUseTime.this.isGetTimeFlag) {
                    HwDeviceFirstUseTime.this.mHandler.post(HwDeviceFirstUseTime.this.getTimeRunnable);
                }
            }
        };
        if (HW_DEBUG) {
            Slog.d(TAG, TAG);
        }
        this.mContext = context;
        this.mCollectionHandler = handler;
        this.mNtpTime = NtpTrustedTime.getInstance(this.mContext);
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mHandlerThread = new HandlerThread("HwDeviceFirstUseTimeThread");
    }

    public void start() {
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HwConnectivityService.CONNECTIVITY_CHANGE_ACTION);
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    public void triggerGetFirstUseTime() {
        if (HW_DEBUG) {
            Slog.d(TAG, "getFirstUseTime");
        }
        if (isNetworkConnected() && !this.isGetTimeFlag) {
            this.mHandler.post(this.getTimeRunnable);
        }
    }

    private void obtianHasGetTime(long time) {
        if (this.mHandler != null) {
            this.isGetTimeFlag = true;
            this.mCurrentTime = -1;
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            this.mHandler.removeCallbacks(this.getTimeRunnable);
            Message msg = Message.obtain();
            msg.what = TYPE_HAS_GET_TIME;
            msg.obj = Long.valueOf(time);
            this.mCollectionHandler.sendMessage(msg);
        }
    }

    private boolean isNetworkConnected() {
        NetworkInfo networkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        return networkInfo != null ? networkInfo.isConnected() : HW_DEBUG;
    }

    private boolean isMobileNetworkConnected() {
        NetworkInfo networkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() && networkInfo.getType() == 0) {
            return true;
        }
        return HW_DEBUG;
    }
}
