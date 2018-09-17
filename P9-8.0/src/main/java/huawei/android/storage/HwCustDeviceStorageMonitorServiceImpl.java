package huawei.android.storage;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.storage.AbsDeviceStorageMonitorService;
import java.util.List;

public class HwCustDeviceStorageMonitorServiceImpl extends HwCustDeviceStorageMonitorService {
    private static final String ACTION_HWSYSTEMMANAGER_STORAGE_CLEAN = "huawei.intent.action.HSM_STORAGE_CLEANER";
    private static final int CRITICAL_LOW_REMIND_INTERVAL = 600000;
    private static final int CRITICAL_LOW_THRESHOLD_BYTES = 8388608;
    private static final int DEVICE_MEMORY_CRITICAL_LOW = 1001;
    private static final String ENCRYPTED_STATE = "encrypted";
    private static final String ENCRYPTING_STATE = "trigger_restart_min_framework";
    protected static final boolean HWDBG;
    protected static final boolean HWFLOW;
    protected static final boolean HWLOGW_E = true;
    private static final String TAG = "HwCustDeviceStorage";
    private static final String TAG_FLOW = "HwCustDeviceStorage_FLOW";
    private static final String TAG_INIT = "HwCustDeviceStorage_INIT";
    private boolean mConfigChanged;
    private Context mContext;
    private Intent mCriticalLowIntent;
    private AlertDialog mDialog;
    private Handler mHandler;
    private boolean mHasReminded;
    private BroadcastReceiver mIntentReceiver;
    private long mLastShowDialogTime;

    static {
        boolean z;
        boolean z2 = HWLOGW_E;
        if (Log.HWLog) {
            z = HWLOGW_E;
        } else {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false;
        }
        HWDBG = z;
        if (!Log.HWINFO) {
            z2 = Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false;
        }
        HWFLOW = z2;
    }

    public HwCustDeviceStorageMonitorServiceImpl(AbsDeviceStorageMonitorService obj, Handler handler) {
        super(obj, handler);
        this.mCriticalLowIntent = null;
        this.mHandler = null;
        this.mContext = null;
        this.mDialog = null;
        this.mLastShowDialogTime = 0;
        this.mConfigChanged = false;
        this.mHasReminded = false;
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.LOCALE_CHANGED")) {
                    HwCustDeviceStorageMonitorServiceImpl.this.mConfigChanged = HwCustDeviceStorageMonitorServiceImpl.HWLOGW_E;
                    if (HwCustDeviceStorageMonitorServiceImpl.this.mDialog != null) {
                        HwCustDeviceStorageMonitorServiceImpl.this.mDialog.cancel();
                    }
                }
            }
        };
        this.mContext = this.mService.getContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        this.mContext.registerReceiver(this.mIntentReceiver, filter);
        this.mCriticalLowIntent = new Intent(ACTION_HWSYSTEMMANAGER_STORAGE_CLEAN);
        List<ResolveInfo> activitysList = this.mContext.getPackageManager().queryIntentActivities(this.mCriticalLowIntent, 65536);
        if (activitysList == null || activitysList.size() <= 0) {
            this.mCriticalLowIntent = new Intent("android.intent.action.MANAGE_PACKAGE_STORAGE");
        }
        this.mCriticalLowIntent.setFlags(335544320);
        this.mHandler = new Handler(handler.getLooper()) {
            public void handleMessage(Message msg) {
                if (msg.what == HwCustDeviceStorageMonitorServiceImpl.DEVICE_MEMORY_CRITICAL_LOW) {
                    if (HwCustDeviceStorageMonitorServiceImpl.this.mDialog == null || HwCustDeviceStorageMonitorServiceImpl.this.mConfigChanged) {
                        if (HwCustDeviceStorageMonitorServiceImpl.this.mConfigChanged) {
                            HwCustDeviceStorageMonitorServiceImpl.this.mConfigChanged = false;
                        }
                        Builder builder = new Builder(HwCustDeviceStorageMonitorServiceImpl.this.mContext);
                        builder.setIcon(17301543);
                        builder.setTitle(33686006);
                        builder.setMessage(33686007);
                        builder.setNegativeButton(17039360, null);
                        builder.setPositiveButton(17039370, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                HwCustDeviceStorageMonitorServiceImpl.this.mContext.startActivity(HwCustDeviceStorageMonitorServiceImpl.this.mCriticalLowIntent);
                            }
                        });
                        builder.setOnDismissListener(new OnDismissListener() {
                            public void onDismiss(DialogInterface dialog) {
                                if (HwCustDeviceStorageMonitorServiceImpl.HWFLOW) {
                                    Slog.i(HwCustDeviceStorageMonitorServiceImpl.TAG_FLOW, "dialog is dissmiss!");
                                }
                                HwCustDeviceStorageMonitorServiceImpl.this.mLastShowDialogTime = SystemClock.elapsedRealtime();
                            }
                        });
                        HwCustDeviceStorageMonitorServiceImpl.this.mDialog = builder.create();
                        HwCustDeviceStorageMonitorServiceImpl.this.mDialog.getWindow().setType(2003);
                    }
                    long currTime = SystemClock.elapsedRealtime();
                    if (!HwCustDeviceStorageMonitorServiceImpl.this.mDialog.isShowing() && (!HwCustDeviceStorageMonitorServiceImpl.this.mHasReminded || currTime - HwCustDeviceStorageMonitorServiceImpl.this.mLastShowDialogTime > 600000)) {
                        if (SystemProperties.getInt("ctsrunning", 0) == 0) {
                            HwCustDeviceStorageMonitorServiceImpl.this.mHasReminded = HwCustDeviceStorageMonitorServiceImpl.HWLOGW_E;
                            HwCustDeviceStorageMonitorServiceImpl.this.mDialog.show();
                        } else if (HwCustDeviceStorageMonitorServiceImpl.HWFLOW) {
                            Slog.i(HwCustDeviceStorageMonitorServiceImpl.TAG_FLOW, "In CTS Running,do not show the no space dailog");
                        }
                    }
                }
            }
        };
    }

    public void clearMemoryForCritiLow() {
        String decryptState = SystemProperties.get("vold.decrypt");
        if (!ENCRYPTED_STATE.equals(SystemProperties.get("ro.crypto.state")) && (ENCRYPTING_STATE.equals(decryptState) ^ 1) != 0) {
            this.mHandler.removeMessages(DEVICE_MEMORY_CRITICAL_LOW);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(DEVICE_MEMORY_CRITICAL_LOW));
        }
    }

    public long getCritiLowMemThreshold() {
        return 8388608;
    }
}
