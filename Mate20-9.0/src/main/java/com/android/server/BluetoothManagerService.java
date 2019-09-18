package com.android.server;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothCallback;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothHeadset;
import android.bluetooth.IBluetoothManager;
import android.bluetooth.IBluetoothManagerCallback;
import android.bluetooth.IBluetoothProfileServiceConnection;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Slog;
import android.util.StatsLog;
import android.widget.Toast;
import com.android.internal.util.DumpUtils;
import com.android.server.HwServiceFactory;
import com.android.server.am.ActivityManagerService;
import com.android.server.display.DisplayTransformManager;
import com.android.server.pm.DumpState;
import com.android.server.pm.UserRestrictionsUtils;
import com.android.server.usage.AppStandbyController;
import com.android.server.utils.PriorityDump;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class BluetoothManagerService extends IBluetoothManager.Stub {
    private static final int ACTIVE_LOG_MAX_SIZE = 20;
    private static final int ADD_PROXY_DELAY_MS = 100;
    private static final int AIRPLANE_MODE_CHANGE_DELAY_MS = 1500;
    private static final String BLUETOOTH_ADMIN_PERM = "android.permission.BLUETOOTH_ADMIN";
    private static final int BLUETOOTH_OFF = 0;
    private static final int BLUETOOTH_ON_AIRPLANE = 2;
    private static final int BLUETOOTH_ON_BLUETOOTH = 1;
    private static final String BLUETOOTH_PERM = "android.permission.BLUETOOTH";
    public static final int BT_DEVICE_THIRD_PART_CONN = 11;
    public static final short BT_SERVICE_ERROR = 1;
    public static final int BT_SWITCH_OFF = 2;
    public static final int BT_SWITCH_ON = 1;
    public static final short BT_TIMEOUT_BIND_ERROR = 20;
    private static final int CRASH_LOG_MAX_SIZE = 100;
    private static final boolean DBG = true;
    public static final String DEFAULT_PACKAGE_NAME = "NULL";
    private static final int ENABLE_MESSAGE_REPEAT_MS = 1500;
    private static final int ERROR_RESTART_TIME_MS = 3000;
    private static final int MAX_ERROR_RESTART_RETRIES = 6;
    /* access modifiers changed from: private */
    public static int MAX_RETRY_USER_SWITCHED_COUNT = 20;
    private static final int MESSAGE_ADD_PROXY_DELAYED = 400;
    private static final int MESSAGE_AIRPLANE_MODE_CHANGE = 600;
    private static final int MESSAGE_BIND_PROFILE_SERVICE = 401;
    private static final int MESSAGE_BLUETOOTH_SERVICE_CONNECTED = 40;
    private static final int MESSAGE_BLUETOOTH_SERVICE_DISCONNECTED = 41;
    private static final int MESSAGE_BLUETOOTH_STATE_CHANGE = 60;
    private static final int MESSAGE_DISABLE = 2;
    private static final int MESSAGE_DISABLE_RADIO = 4;
    private static final int MESSAGE_ENABLE = 1;
    private static final int MESSAGE_ENABLE_RADIO = 3;
    private static final int MESSAGE_GET_NAME_AND_ADDRESS = 200;
    private static final int MESSAGE_REGISTER_ADAPTER = 20;
    private static final int MESSAGE_REGISTER_STATE_CHANGE_CALLBACK = 30;
    private static final int MESSAGE_RESTART_BLUETOOTH_SERVICE = 42;
    private static final int MESSAGE_RESTORE_USER_SETTING = 500;
    private static final int MESSAGE_TIMEOUT_BIND = 100;
    private static final int MESSAGE_TIMEOUT_UNBIND = 101;
    private static final int MESSAGE_UNREGISTER_ADAPTER = 21;
    private static final int MESSAGE_UNREGISTER_STATE_CHANGE_CALLBACK = 31;
    private static final int MESSAGE_USER_SWITCHED = 300;
    private static final int MESSAGE_USER_UNLOCKED = 301;
    private static final int RESTORE_SETTING_TO_OFF = 0;
    private static final int RESTORE_SETTING_TO_ON = 1;
    private static final String SECURE_SETTINGS_BLUETOOTH_ADDRESS = "bluetooth_address";
    private static final String SECURE_SETTINGS_BLUETOOTH_ADDR_VALID = "bluetooth_addr_valid";
    private static final String SECURE_SETTINGS_BLUETOOTH_NAME = "bluetooth_name";
    private static final int SERVICE_IBLUETOOTH = 1;
    private static final int SERVICE_IBLUETOOTHGATT = 2;
    private static final int SERVICE_RESTART_TIME_MS = 200;
    private static final String TAG = "BluetoothManagerService";
    private static final int TIMEOUT_BIND_MS = 3000;
    private static final int USER_SWITCHED_TIME_MS = 200;
    /* access modifiers changed from: private */
    public volatile boolean isAirplaneModeChanging = false;
    private final LinkedList<ActiveLog> mActiveLogs = new LinkedList<>();
    private String mAddress;
    private final ContentObserver mAirplaneModeObserver = new ContentObserver(null) {
        public void onChange(boolean unused) {
            synchronized (this) {
                if (HwDeviceManager.disallowOp(51)) {
                    Slog.w(BluetoothManagerService.TAG, "mdm force open bluetooth, not allow airplane close bluetooth");
                    return;
                }
                Slog.d(BluetoothManagerService.TAG, "receiver Airplane Mode change isAirplaneModeChanging: " + BluetoothManagerService.this.isAirplaneModeChanging);
                BluetoothManagerService.this.mHandler.removeMessages(600);
                if (BluetoothManagerService.this.isAirplaneModeChanging) {
                    BluetoothManagerService.this.mHandler.sendEmptyMessageDelayed(600, 1500);
                } else {
                    BluetoothManagerService.this.changeBluetoothStateFromAirplaneMode();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mBinding;
    /* access modifiers changed from: private */
    public Map<IBinder, ClientDeathRecipient> mBleApps = new ConcurrentHashMap();
    /* access modifiers changed from: private */
    public IBluetooth mBluetooth;
    /* access modifiers changed from: private */
    public IBinder mBluetoothBinder;
    /* access modifiers changed from: private */
    public final IBluetoothCallback mBluetoothCallback = new IBluetoothCallback.Stub() {
        public void onBluetoothStateChange(int prevState, int newState) throws RemoteException {
            HwLog.i(BluetoothManagerService.TAG, "mBluetoothCallback, onBluetoothStateChange prevState=" + prevState + ", newState=" + newState);
            BluetoothManagerService.this.mHandler.sendMessage(BluetoothManagerService.this.mHandler.obtainMessage(60, prevState, newState));
        }
    };
    /* access modifiers changed from: private */
    public IBluetoothGatt mBluetoothGatt;
    /* access modifiers changed from: private */
    public final ReentrantReadWriteLock mBluetoothLock = new ReentrantReadWriteLock();
    private final BluetoothServiceStateCallback mBluetoothServiceStateCallback;
    /* access modifiers changed from: private */
    public final RemoteCallbackList<IBluetoothManagerCallback> mCallbacks;
    /* access modifiers changed from: private */
    public BluetoothServiceConnection mConnection = new BluetoothServiceConnection();
    private final ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final LinkedList<Long> mCrashTimestamps = new LinkedList<>();
    private int mCrashes;
    /* access modifiers changed from: private */
    public boolean mEnable;
    /* access modifiers changed from: private */
    public boolean mEnableExternal;
    /* access modifiers changed from: private */
    public boolean mEnableForNameAndAddress;
    /* access modifiers changed from: private */
    public int mErrorRecoveryRetryCounter;
    /* access modifiers changed from: private */
    public final BluetoothHandler mHandler = new BluetoothHandler(IoThread.get().getLooper());
    private long mLastEnableMessageTime;
    private long mLastEnabledTime;
    private int mLastMessage;
    private boolean mLastQuietMode;
    private String mName;
    private final boolean mPermissionReviewRequired;
    /* access modifiers changed from: private */
    public final Map<Integer, ProfileServiceConnections> mProfileServices = new HashMap();
    /* access modifiers changed from: private */
    public boolean mQuietEnable = false;
    /* access modifiers changed from: private */
    public boolean mQuietEnableExternal;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED".equals(action)) {
                String newName = intent.getStringExtra("android.bluetooth.adapter.extra.LOCAL_NAME");
                Slog.d(BluetoothManagerService.TAG, "Bluetooth Adapter name changed to " + newName);
                if (newName != null) {
                    BluetoothManagerService.this.storeNameAndAddress(newName, null);
                }
            } else if ("android.bluetooth.adapter.action.BLUETOOTH_ADDRESS_CHANGED".equals(action)) {
                String newAddress = intent.getStringExtra("android.bluetooth.adapter.extra.BLUETOOTH_ADDRESS");
                if (newAddress != null) {
                    Slog.d(BluetoothManagerService.TAG, "Bluetooth Adapter address changed to " + BluetoothManagerService.this.getPartMacAddress(newAddress));
                    BluetoothManagerService.this.storeNameAndAddress(null, newAddress);
                } else {
                    Slog.e(BluetoothManagerService.TAG, "No Bluetooth Adapter address parameter found");
                }
                HwLog.d(BluetoothManagerService.TAG, "mEnableForNameAndAddress = " + BluetoothManagerService.this.mEnableForNameAndAddress);
                if (BluetoothManagerService.this.mEnableForNameAndAddress && BluetoothManagerService.this.getState() == 15) {
                    HwLog.d(BluetoothManagerService.TAG, "get name and address, and disable bluetooth, state = " + BluetoothManagerService.this.getState());
                    boolean unused = BluetoothManagerService.this.mEnableForNameAndAddress = false;
                    boolean unused2 = BluetoothManagerService.this.mEnable = false;
                    BluetoothManagerService.this.sendBrEdrDownCallback();
                }
            } else if ("android.os.action.SETTING_RESTORED".equals(action) && "bluetooth_on".equals(intent.getStringExtra("setting_name"))) {
                String prevValue = intent.getStringExtra("previous_value");
                String newValue = intent.getStringExtra("new_value");
                Slog.d(BluetoothManagerService.TAG, "ACTION_SETTING_RESTORED with BLUETOOTH_ON, prevValue=" + prevValue + ", newValue=" + newValue);
                if (newValue != null && prevValue != null && !prevValue.equals(newValue)) {
                    BluetoothManagerService.this.mHandler.sendMessage(BluetoothManagerService.this.mHandler.obtainMessage(500, newValue.equals("0") ? 0 : 1, 0));
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mState;
    /* access modifiers changed from: private */
    public final RemoteCallbackList<IBluetoothStateChangeCallback> mStateChangeCallbacks;
    private final int mSystemUiUid;
    /* access modifiers changed from: private */
    public boolean mUnbinding;
    private final UserManagerInternal.UserRestrictionsListener mUserRestrictionsListener = new UserManagerInternal.UserRestrictionsListener() {
        public void onUserRestrictionsChanged(int userId, Bundle newRestrictions, Bundle prevRestrictions) {
            if (UserRestrictionsUtils.restrictionsChanged(prevRestrictions, newRestrictions, "no_bluetooth_sharing")) {
                BluetoothManagerService.this.updateOppLauncherComponentState(userId, newRestrictions.getBoolean("no_bluetooth_sharing"));
            }
            if (userId == 0 && UserRestrictionsUtils.restrictionsChanged(prevRestrictions, newRestrictions, "no_bluetooth")) {
                if (userId != 0 || !newRestrictions.getBoolean("no_bluetooth")) {
                    BluetoothManagerService.this.updateOppLauncherComponentState(userId, newRestrictions.getBoolean("no_bluetooth_sharing"));
                    return;
                }
                BluetoothManagerService.this.updateOppLauncherComponentState(userId, true);
                BluetoothManagerService.this.sendDisableMsg(3, BluetoothManagerService.this.mContext.getPackageName());
            }
        }
    };

    private class ActiveLog {
        private boolean mEnable;
        private String mPackageName;
        private int mReason;
        private long mTimestamp;

        ActiveLog(int reason, String packageName, boolean enable, long timestamp) {
            this.mReason = reason;
            this.mPackageName = packageName;
            this.mEnable = enable;
            this.mTimestamp = timestamp;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(BluetoothManagerService.timeToLog(this.mTimestamp));
            sb.append(this.mEnable ? "  Enabled " : " Disabled ");
            sb.append(" due to ");
            sb.append(BluetoothManagerService.getEnableDisableReasonString(this.mReason));
            sb.append(" by ");
            sb.append(this.mPackageName);
            return sb.toString();
        }
    }

    private class BluetoothHandler extends Handler {
        boolean mGetNameAddressOnly = false;

        BluetoothHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_ENABLE(" + msg.arg1 + "): mBluetooth = " + BluetoothManagerService.this.mBluetooth);
                    BluetoothManagerService.this.mHandler.removeMessages(42);
                    boolean unused = BluetoothManagerService.this.mEnable = true;
                    try {
                        BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                        if (BluetoothManagerService.this.mBluetooth != null && BluetoothManagerService.this.mBluetooth.getState() == 15) {
                            Slog.w(BluetoothManagerService.TAG, "BT Enable in BLE_ON State, going to ON");
                            BluetoothManagerService.this.mBluetooth.onLeServiceUp();
                            BluetoothManagerService.this.persistBluetoothSetting(1);
                            BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                            break;
                        }
                    } catch (RemoteException e) {
                        Slog.e(BluetoothManagerService.TAG, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, e);
                    } catch (Throwable th) {
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                        throw th;
                    }
                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                    boolean unused2 = BluetoothManagerService.this.mQuietEnable = msg.arg1 == 1;
                    if (BluetoothManagerService.this.mBluetooth != null) {
                        boolean unused3 = BluetoothManagerService.this.waitForOnOff(false, true);
                        BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(42), 400);
                        break;
                    } else {
                        BluetoothManagerService.this.handleEnable(BluetoothManagerService.this.mQuietEnable);
                        break;
                    }
                case 2:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_DISABLE: mBluetooth = " + BluetoothManagerService.this.mBluetooth);
                    BluetoothManagerService.this.mHandler.removeMessages(42);
                    if (BluetoothManagerService.this.mEnable && BluetoothManagerService.this.mBluetooth != null) {
                        boolean unused4 = BluetoothManagerService.this.waitForOnOff(true, false);
                        boolean unused5 = BluetoothManagerService.this.mEnable = false;
                        BluetoothManagerService.this.handleDisable();
                        boolean unused6 = BluetoothManagerService.this.waitForOnOff(false, false);
                        break;
                    } else {
                        boolean unused7 = BluetoothManagerService.this.mEnable = false;
                        BluetoothManagerService.this.handleDisable();
                        break;
                    }
                    break;
                case 3:
                    HwLog.d(BluetoothManagerService.TAG, "MESSAGE_ENABLE_RADIO: mBluetooth = " + BluetoothManagerService.this.mBluetooth);
                    BluetoothManagerService.this.handleEnableRadio();
                    break;
                case 4:
                    BluetoothManagerService.this.handleDisableRadio();
                    break;
                case 20:
                    IBluetoothManagerCallback callback = (IBluetoothManagerCallback) msg.obj;
                    boolean added = BluetoothManagerService.this.mCallbacks.register(callback, new Integer(msg.arg1));
                    StringBuilder sb = new StringBuilder();
                    sb.append("Added callback: ");
                    sb.append(callback == null ? "null" : callback);
                    sb.append(":");
                    sb.append(added);
                    sb.append(" pid = ");
                    sb.append(msg.arg1);
                    HwLog.d(BluetoothManagerService.TAG, sb.toString());
                    break;
                case 21:
                    BluetoothManagerService.this.mCallbacks.unregister((IBluetoothManagerCallback) msg.obj);
                    break;
                case 30:
                    boolean added2 = BluetoothManagerService.this.mStateChangeCallbacks.register((IBluetoothStateChangeCallback) msg.obj, new Integer(msg.arg1));
                    HwLog.d(BluetoothManagerService.TAG, "Added state change callback: " + callback + ":" + added2 + " pid = " + msg.arg1);
                    break;
                case 31:
                    BluetoothManagerService.this.mStateChangeCallbacks.unregister((IBluetoothStateChangeCallback) msg.obj);
                    break;
                case 40:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_BLUETOOTH_SERVICE_CONNECTED: " + msg.arg1);
                    IBinder service = (IBinder) msg.obj;
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        if (msg.arg1 != 2) {
                            BluetoothManagerService.this.mHandler.removeMessages(100);
                            boolean unused8 = BluetoothManagerService.this.mBinding = false;
                            IBinder unused9 = BluetoothManagerService.this.mBluetoothBinder = service;
                            IBluetooth unused10 = BluetoothManagerService.this.mBluetooth = IBluetooth.Stub.asInterface(Binder.allowBlocking(service));
                            if (!BluetoothManagerService.this.isNameAndAddressSet()) {
                                BluetoothManagerService.this.mHandler.sendMessage(BluetoothManagerService.this.mHandler.obtainMessage(DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE));
                                if (this.mGetNameAddressOnly) {
                                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                                    return;
                                }
                            }
                            BluetoothManagerService.this.mBluetooth.registerCallback(BluetoothManagerService.this.mBluetoothCallback);
                            BluetoothManagerService.this.sendBluetoothServiceUpCallback();
                            if (!BluetoothManagerService.this.mConnection.isTurnOnRadio()) {
                                try {
                                    if (!BluetoothManagerService.this.mQuietEnable) {
                                        if (!BluetoothManagerService.this.mBluetooth.enable()) {
                                            Slog.e(BluetoothManagerService.TAG, "IBluetooth.enable() returned false");
                                        }
                                    } else if (!BluetoothManagerService.this.mBluetooth.enableNoAutoConnect()) {
                                        Slog.e(BluetoothManagerService.TAG, "IBluetooth.enableNoAutoConnect() returned false");
                                    }
                                } catch (RemoteException e2) {
                                    HwLog.e(BluetoothManagerService.TAG, "Unable to call enable()", e2);
                                }
                                BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                                if (BluetoothManagerService.this.mEnable) {
                                    HwLog.d(BluetoothManagerService.TAG, "re-getNameAndAddress when bt enabled!");
                                    BluetoothManagerService.this.getNameAndAddress();
                                    break;
                                } else {
                                    boolean unused11 = BluetoothManagerService.this.waitForOnOff(true, false);
                                    BluetoothManagerService.this.handleDisable();
                                    boolean unused12 = BluetoothManagerService.this.waitForOnOff(false, false);
                                    break;
                                }
                            } else {
                                try {
                                    if (!BluetoothManagerService.this.mBluetooth.enableRadio()) {
                                        HwLog.e(BluetoothManagerService.TAG, "IBluetooth.enableRadio() returned false");
                                    }
                                    BluetoothManagerService.this.mConnection.setTurnOnRadio(false);
                                } catch (RemoteException e3) {
                                    HwLog.e(BluetoothManagerService.TAG, "Unable to call enableRadio()", e3);
                                }
                            }
                        } else {
                            IBluetoothGatt unused13 = BluetoothManagerService.this.mBluetoothGatt = IBluetoothGatt.Stub.asInterface(Binder.allowBlocking(service));
                            BluetoothManagerService.this.continueFromBleOnState();
                        }
                    } catch (RemoteException re) {
                        Slog.e(BluetoothManagerService.TAG, "Unable to register BluetoothCallback", re);
                    } catch (Throwable th2) {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        throw th2;
                    }
                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    break;
                case 41:
                    Slog.e(BluetoothManagerService.TAG, "MESSAGE_BLUETOOTH_SERVICE_DISCONNECTED(" + msg.arg1 + ")");
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        if (msg.arg1 == 1) {
                            if (BluetoothManagerService.this.mBluetooth != null) {
                                IBluetooth unused14 = BluetoothManagerService.this.mBluetooth = null;
                                BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                                BluetoothManagerService.this.addCrashLog();
                                BluetoothManagerService.this.addActiveLog(7, BluetoothManagerService.this.mContext.getPackageName(), false);
                                if (BluetoothManagerService.this.mEnable) {
                                    boolean unused15 = BluetoothManagerService.this.mEnable = false;
                                    BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(42), 200);
                                }
                                BluetoothManagerService.this.sendBluetoothServiceDownCallback();
                                if (BluetoothManagerService.this.mState == 11 || BluetoothManagerService.this.mState == 12) {
                                    BluetoothManagerService.this.bluetoothStateChangeHandler(12, 13);
                                    int unused16 = BluetoothManagerService.this.mState = 13;
                                }
                                if (BluetoothManagerService.this.mState == 13) {
                                    BluetoothManagerService.this.bluetoothStateChangeHandler(13, 10);
                                }
                                BluetoothManagerService.this.mHandler.removeMessages(60);
                                int unused17 = BluetoothManagerService.this.mState = 10;
                                break;
                            }
                        } else if (msg.arg1 == 2) {
                            IBluetoothGatt unused18 = BluetoothManagerService.this.mBluetoothGatt = null;
                        } else {
                            Slog.e(BluetoothManagerService.TAG, "Unknown argument for service disconnect!");
                        }
                        break;
                    } finally {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    }
                case 42:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_RESTART_BLUETOOTH_SERVICE");
                    boolean unused19 = BluetoothManagerService.this.mEnable = true;
                    BluetoothManagerService.this.addActiveLog(4, BluetoothManagerService.this.mContext.getPackageName(), true);
                    BluetoothManagerService.this.handleEnable(BluetoothManagerService.this.mQuietEnable);
                    break;
                case 60:
                    int prevState = msg.arg1;
                    int newState = msg.arg2;
                    if (prevState != 14 || newState != 15 || !BluetoothManagerService.this.mEnableForNameAndAddress) {
                        Slog.d(BluetoothManagerService.TAG, "MESSAGE_BLUETOOTH_STATE_CHANGE: " + BluetoothAdapter.nameForState(prevState) + " > " + BluetoothAdapter.nameForState(newState));
                        int unused20 = BluetoothManagerService.this.mState = newState;
                        BluetoothManagerService.this.bluetoothStateChangeHandler(prevState, newState);
                        if (prevState == 14 && newState == 10 && BluetoothManagerService.this.mBluetooth != null && BluetoothManagerService.this.mEnable) {
                            BluetoothManagerService.this.recoverBluetoothServiceFromError(false);
                        }
                        if (prevState == 11 && newState == 15 && BluetoothManagerService.this.mBluetooth != null && BluetoothManagerService.this.mEnable) {
                            BluetoothManagerService.this.recoverBluetoothServiceFromError(true);
                        }
                        if (prevState == 16 && newState == 10 && BluetoothManagerService.this.mEnable) {
                            Slog.d(BluetoothManagerService.TAG, "Entering STATE_OFF but mEnabled is true; restarting.");
                            boolean unused21 = BluetoothManagerService.this.waitForOnOff(false, true);
                            BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(42), 400);
                        }
                        if ((newState == 12 || newState == 15) && BluetoothManagerService.this.mErrorRecoveryRetryCounter != 0) {
                            Slog.w(BluetoothManagerService.TAG, "bluetooth is recovered from error");
                            int unused22 = BluetoothManagerService.this.mErrorRecoveryRetryCounter = 0;
                        }
                        if (BluetoothManagerService.this.isAirplaneModeChanging && (newState == 12 || newState == 10)) {
                            Slog.d(BluetoothManagerService.TAG, "Entering STATE_OFF but mEnabled is true; restarting.");
                            boolean unused23 = BluetoothManagerService.this.isAirplaneModeChanging = false;
                            break;
                        }
                    } else {
                        HwLog.d(BluetoothManagerService.TAG, "mEnableForNameAndAddress = " + BluetoothManagerService.this.mEnableForNameAndAddress);
                        if (BluetoothManagerService.this.isNameAndAddressSet()) {
                            HwLog.d(BluetoothManagerService.TAG, "get name and address, and disable bluetooth, state is ble on.");
                            boolean unused24 = BluetoothManagerService.this.mEnableForNameAndAddress = false;
                            boolean unused25 = BluetoothManagerService.this.mEnable = false;
                            BluetoothManagerService.this.sendBrEdrDownCallback();
                        }
                        return;
                    }
                case 100:
                    Slog.e(BluetoothManagerService.TAG, "MESSAGE_TIMEOUT_BIND");
                    BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                    boolean unused26 = BluetoothManagerService.this.mBinding = false;
                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    BluetoothManagerService.this.reportBtServiceChrToDft(1, 20, BluetoothManagerService.DEFAULT_PACKAGE_NAME);
                    break;
                case 101:
                    HwLog.e(BluetoothManagerService.TAG, "MESSAGE_TIMEOUT_UNBIND");
                    BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                    boolean unused27 = BluetoothManagerService.this.mUnbinding = false;
                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    break;
                case DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE /*200*/:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_GET_NAME_AND_ADDRESS");
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        if (BluetoothManagerService.this.mBluetooth == null && !BluetoothManagerService.this.mBinding) {
                            Slog.d(BluetoothManagerService.TAG, "Binding to service to get name and address");
                            this.mGetNameAddressOnly = true;
                            BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(100), 3000);
                            if (!BluetoothManagerService.this.doBind(new Intent(IBluetooth.class.getName()), BluetoothManagerService.this.mConnection, 65, UserHandle.CURRENT)) {
                                BluetoothManagerService.this.mHandler.removeMessages(100);
                            } else {
                                boolean unused28 = BluetoothManagerService.this.mBinding = true;
                            }
                        } else if (BluetoothManagerService.this.mBluetooth != null) {
                            BluetoothManagerService.this.storeNameAndAddress(BluetoothManagerService.this.mBluetooth.getName(), BluetoothManagerService.this.mBluetooth.getAddress());
                            if (this.mGetNameAddressOnly && !BluetoothManagerService.this.mEnable) {
                                BluetoothManagerService.this.unbindAndFinish();
                            }
                            this.mGetNameAddressOnly = false;
                        }
                    } catch (RemoteException re2) {
                        Slog.e(BluetoothManagerService.TAG, "Unable to grab names", re2);
                    } catch (Throwable th3) {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        throw th3;
                    }
                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    break;
                case 300:
                    HwLog.d(BluetoothManagerService.TAG, "MESSAGE_USER_SWITCHED");
                    BluetoothManagerService.this.mHandler.removeMessages(300);
                    if (BluetoothManagerService.this.mBluetooth == null || !BluetoothManagerService.this.isEnabled()) {
                        if (BluetoothManagerService.this.mBinding || BluetoothManagerService.this.mBluetooth != null) {
                            Message userMsg = BluetoothManagerService.this.mHandler.obtainMessage(300);
                            userMsg.arg2 = 1 + msg.arg2;
                            if (userMsg.arg2 > BluetoothManagerService.MAX_RETRY_USER_SWITCHED_COUNT) {
                                try {
                                    if (BluetoothManagerService.this.mBluetooth.getState() == 15) {
                                        Slog.d(BluetoothManagerService.TAG, "STOP Retry");
                                        return;
                                    }
                                } catch (RemoteException e4) {
                                    Slog.e(BluetoothManagerService.TAG, "Unable to call getState", e4);
                                    return;
                                }
                            }
                            BluetoothManagerService.this.mHandler.sendMessageDelayed(userMsg, 200);
                            Slog.d(BluetoothManagerService.TAG, "Retry MESSAGE_USER_SWITCHED " + userMsg.arg2);
                            break;
                        }
                    } else {
                        BluetoothManagerService.this.clearBleApps();
                        try {
                            BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                            if (BluetoothManagerService.this.mBluetooth != null) {
                                BluetoothManagerService.this.mBluetooth.unregisterCallback(BluetoothManagerService.this.mBluetoothCallback);
                            }
                        } catch (RemoteException re3) {
                            Slog.e(BluetoothManagerService.TAG, "Unable to unregister", re3);
                        } catch (Throwable th4) {
                            BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                            throw th4;
                        }
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                        if (BluetoothManagerService.this.mState == 13) {
                            BluetoothManagerService.this.bluetoothStateChangeHandler(BluetoothManagerService.this.mState, 10);
                            int unused29 = BluetoothManagerService.this.mState = 10;
                        }
                        if (BluetoothManagerService.this.mState == 10) {
                            BluetoothManagerService.this.bluetoothStateChangeHandler(BluetoothManagerService.this.mState, 11);
                            int unused30 = BluetoothManagerService.this.mState = 11;
                        }
                        boolean unused31 = BluetoothManagerService.this.waitForMonitoredOnOff(true, false);
                        if (BluetoothManagerService.this.mState == 11) {
                            BluetoothManagerService.this.bluetoothStateChangeHandler(BluetoothManagerService.this.mState, 12);
                        }
                        BluetoothManagerService.this.unbindAllBluetoothProfileServices();
                        BluetoothManagerService.this.addActiveLog(8, BluetoothManagerService.this.mContext.getPackageName(), false);
                        BluetoothManagerService.this.handleDisable();
                        BluetoothManagerService.this.bluetoothStateChangeHandler(12, 13);
                        boolean didDisableTimeout = !BluetoothManagerService.this.waitForMonitoredOnOff(false, true);
                        BluetoothManagerService.this.bluetoothStateChangeHandler(13, 10);
                        BluetoothManagerService.this.sendBluetoothServiceDownCallback();
                        try {
                            BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                            if (BluetoothManagerService.this.mBluetooth != null) {
                                IBluetooth unused32 = BluetoothManagerService.this.mBluetooth = null;
                                BluetoothManagerService.this.mContext.unbindService(BluetoothManagerService.this.mConnection);
                            }
                            IBluetoothGatt unused33 = BluetoothManagerService.this.mBluetoothGatt = null;
                            if (didDisableTimeout) {
                                SystemClock.sleep(3000);
                            } else {
                                SystemClock.sleep(100);
                            }
                            BluetoothManagerService.this.mHandler.removeMessages(60);
                            int unused34 = BluetoothManagerService.this.mState = 10;
                            BluetoothManagerService.this.addActiveLog(8, BluetoothManagerService.this.mContext.getPackageName(), true);
                            boolean unused35 = BluetoothManagerService.this.mEnable = true;
                            BluetoothManagerService.this.handleEnable(BluetoothManagerService.this.mQuietEnable);
                            break;
                        } finally {
                            BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        }
                    }
                    break;
                case BluetoothManagerService.MESSAGE_USER_UNLOCKED /*301*/:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_USER_UNLOCKED");
                    BluetoothManagerService.this.mHandler.removeMessages(300);
                    if (BluetoothManagerService.this.mEnable && !BluetoothManagerService.this.mBinding && BluetoothManagerService.this.mBluetooth == null) {
                        Slog.d(BluetoothManagerService.TAG, "Enabled but not bound; retrying after unlock");
                        BluetoothManagerService.this.handleEnable(BluetoothManagerService.this.mQuietEnable);
                        break;
                    }
                case BluetoothManagerService.MESSAGE_ADD_PROXY_DELAYED /*400*/:
                    ProfileServiceConnections psc = (ProfileServiceConnections) BluetoothManagerService.this.mProfileServices.get(Integer.valueOf(msg.arg1));
                    if (psc != null) {
                        psc.addProxy((IBluetoothProfileServiceConnection) msg.obj);
                        break;
                    }
                    break;
                case BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE /*401*/:
                    ProfileServiceConnections psc2 = (ProfileServiceConnections) msg.obj;
                    removeMessages(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE, msg.obj);
                    if (psc2 != null) {
                        boolean unused36 = psc2.bindService();
                        break;
                    }
                    break;
                case 500:
                    if (msg.arg1 != 0 || !BluetoothManagerService.this.mEnable) {
                        if (msg.arg1 == 1 && !BluetoothManagerService.this.mEnable) {
                            Slog.d(BluetoothManagerService.TAG, "Restore Bluetooth state to enabled");
                            boolean unused37 = BluetoothManagerService.this.mQuietEnableExternal = false;
                            boolean unused38 = BluetoothManagerService.this.mEnableExternal = true;
                            BluetoothManagerService.this.sendEnableMsg(false, 9, BluetoothManagerService.this.mContext.getPackageName());
                            break;
                        }
                    } else {
                        Slog.d(BluetoothManagerService.TAG, "Restore Bluetooth state to disabled");
                        BluetoothManagerService.this.persistBluetoothSetting(0);
                        boolean unused39 = BluetoothManagerService.this.mEnableExternal = false;
                        BluetoothManagerService.this.sendDisableMsg(9, BluetoothManagerService.this.mContext.getPackageName());
                        break;
                    }
                    break;
                case 600:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_AIRPLANE_MODE_CHANGE");
                    BluetoothManagerService.this.mHandler.removeMessages(600);
                    if (!BluetoothManagerService.this.isAirplaneModeChanging) {
                        BluetoothManagerService.this.changeBluetoothStateFromAirplaneMode();
                        break;
                    } else {
                        BluetoothManagerService.this.mHandler.sendEmptyMessageDelayed(600, 1500);
                        return;
                    }
            }
        }
    }

    private class BluetoothServiceConnection implements ServiceConnection {
        private boolean mGetNameAddressOnly;
        private boolean mIsTurnOnRadio;

        private BluetoothServiceConnection() {
        }

        public void setTurnOnRadio(boolean isTurnOnRadio) {
            this.mIsTurnOnRadio = isTurnOnRadio;
        }

        public boolean isTurnOnRadio() {
            return this.mIsTurnOnRadio;
        }

        public void setGetNameAddressOnly(boolean getOnly) {
            this.mGetNameAddressOnly = getOnly;
        }

        public boolean isGetNameAddressOnly() {
            return this.mGetNameAddressOnly;
        }

        public void onServiceConnected(ComponentName componentName, IBinder service) {
            String name = componentName.getClassName();
            Slog.d(BluetoothManagerService.TAG, "BluetoothServiceConnection: " + name);
            Message msg = BluetoothManagerService.this.mHandler.obtainMessage(40);
            if (name.equals("com.android.bluetooth.btservice.AdapterService")) {
                msg.arg1 = 1;
            } else if (name.equals("com.android.bluetooth.gatt.GattService")) {
                msg.arg1 = 2;
            } else {
                Slog.e(BluetoothManagerService.TAG, "Unknown service connected: " + name);
                return;
            }
            msg.obj = service;
            BluetoothManagerService.this.mHandler.sendMessage(msg);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            String name = componentName.getClassName();
            Slog.d(BluetoothManagerService.TAG, "BluetoothServiceConnection, disconnected: " + name);
            Message msg = BluetoothManagerService.this.mHandler.obtainMessage(41);
            if (name.equals("com.android.bluetooth.btservice.AdapterService")) {
                msg.arg1 = 1;
            } else if (name.equals("com.android.bluetooth.gatt.GattService")) {
                msg.arg1 = 2;
            } else {
                Slog.e(BluetoothManagerService.TAG, "Unknown service disconnected: " + name);
                return;
            }
            BluetoothManagerService.this.mHandler.sendMessage(msg);
        }
    }

    private final class BluetoothServiceStateCallback {
        private static final int AUTO_LOG_BUG_TYPE_FUNCTION_FAULT = 2;
        private static final String AUTO_UPLOAD_CATEGORY_NAME = "bluetooth";
        private static final long AUTO_UPLOAD_MIN_INTERVAL_TIME = 60000;
        private static final int BINDER_CALLBACK_TIMEOUT_MS = 20000;
        private static final int BUG_TYPE_CALLBACK_TIMEOUT = 0;
        private static final int MESSAGE_BINDER_CALLBACK_TIMEOUT = 1;
        private static final String PREFIX_AUTO_UPLOAD = "prefixautoupload";
        private static final int SERVICE_DOWN = 1;
        private static final int SERVICE_UP = 0;
        public Handler mKillPidHandler;
        private long sLastAutoUploadTime;

        private BluetoothServiceStateCallback() {
            this.sLastAutoUploadTime = 0;
            this.mKillPidHandler = new Handler() {
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        int[] pids = {msg.arg1};
                        ActivityManagerService ams = (ActivityManagerService) ServiceManager.getService("activity");
                        String logMsg = "mKillPidHandler---pids = " + pids[0] + " getAppName = " + BluetoothServiceStateCallback.this.getAppName(pids[0]);
                        HwLog.e(BluetoothManagerService.TAG, logMsg);
                        if (Process.myPid() != pids[0]) {
                            ams.killPids(pids, "BluetoothManagerService callback timeout", true);
                            BluetoothServiceStateCallback.this.autoUpload(2, 0, logMsg);
                            if (!HwServiceFactory.getHwIMonitorManager().uploadBtRadarEvent(HwServiceFactory.IHwIMonitorManager.IMONITOR_BINDER_FAILED, logMsg)) {
                                HwLog.d(BluetoothManagerService.TAG, "upload MESSAGE_BINDER_CALLBACK_TIMEOUT failed!");
                            }
                        }
                    }
                }
            };
        }

        /* access modifiers changed from: private */
        public String getAppName(int pID) {
            List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) BluetoothManagerService.this.mContext.getSystemService("activity")).getRunningAppProcesses();
            if (appProcessList != null) {
                for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
                    if (appProcess.pid == pID) {
                        return appProcess.processName;
                    }
                }
            }
            return null;
        }

        private boolean isActiveProcess(int pID) {
            if (getAppName(pID) != null) {
                return true;
            }
            List<ActivityManager.ProcessErrorStateInfo> appProcessErrorList = ((ActivityManager) BluetoothManagerService.this.mContext.getSystemService("activity")).getProcessesInErrorState();
            if (appProcessErrorList != null) {
                for (ActivityManager.ProcessErrorStateInfo appProcessError : appProcessErrorList) {
                    if (appProcessError.pid == pID && appProcessError.condition == 2) {
                        return true;
                    }
                }
            }
            HwLog.d(BluetoothManagerService.TAG, "[isActiveProcess] pID: " + pID + " return false");
            return false;
        }

        public void sendBluetoothStateCallback(boolean isUp) {
            int i;
            try {
                int n = BluetoothManagerService.this.mStateChangeCallbacks.beginBroadcast();
                HwLog.d(BluetoothManagerService.TAG, "Broadcasting onBluetoothStateChange(" + isUp + ") to " + n + " receivers.");
                for (i = 0; i < n; i++) {
                    IBluetoothStateChangeCallback broadcastItem = BluetoothManagerService.this.mStateChangeCallbacks.getBroadcastItem(i);
                    Message timeoutMsg = this.mKillPidHandler.obtainMessage(1);
                    timeoutMsg.arg1 = ((Integer) BluetoothManagerService.this.mStateChangeCallbacks.getBroadcastCookie(i)).intValue();
                    this.mKillPidHandler.sendMessageDelayed(timeoutMsg, 20000);
                    BluetoothManagerService.this.mStateChangeCallbacks.getBroadcastItem(i).onBluetoothStateChange(isUp);
                    this.mKillPidHandler.removeMessages(1);
                }
                BluetoothManagerService.this.mStateChangeCallbacks.finishBroadcast();
            } catch (RemoteException e) {
                HwLog.e(BluetoothManagerService.TAG, "Unable to call onBluetoothStateChange() on callback #" + i, e);
            } catch (Throwable th) {
                BluetoothManagerService.this.mStateChangeCallbacks.finishBroadcast();
                throw th;
            }
        }

        public void sendBluetoothServiceUpCallback() {
            sendBluetoothServiceStateCallback(0);
        }

        public void sendBluetoothServiceDownCallback() {
            sendBluetoothServiceStateCallback(1);
        }

        /* access modifiers changed from: private */
        public void autoUpload(int bugType, int sceneDef, String msg) {
            StringBuilder sb = new StringBuilder(256);
            sb.append("Package:");
            sb.append("com.android.bluetooth");
            sb.append("\n");
            sb.append("APK version:");
            sb.append("1");
            sb.append("\n");
            sb.append("Bug type:");
            sb.append(bugType);
            sb.append("\n");
            sb.append("Scene def:");
            sb.append(sceneDef);
            sb.append("\n");
            HwLog.i(BluetoothManagerService.TAG, "autoUpload->bugType:" + bugType + "; sceneDef:" + sceneDef + "; msg:" + msg + ";" + PREFIX_AUTO_UPLOAD);
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.sLastAutoUploadTime < 60000) {
                HwLog.w(BluetoothManagerService.TAG, "autoUpload->trigger auto upload frequently, return directly.");
                return;
            }
            this.sLastAutoUploadTime = currentTime;
            try {
                autoUpload(AUTO_UPLOAD_CATEGORY_NAME, 65, sb.toString(), msg);
            } catch (Exception ex) {
                HwLog.e(BluetoothManagerService.TAG, "autoUpload->LogException.msg() ex:prefixautoupload", ex);
            }
        }

        private void autoUpload(String appId, int level, String header, String msg) {
            try {
                HwLog.i(BluetoothManagerService.TAG, "autoupload");
                Class<?> clazz = Class.forName("android.util.HwLogException");
                clazz.getMethod("msg", new Class[]{String.class, Integer.TYPE, String.class, String.class}).invoke(clazz.newInstance(), new Object[]{appId, Integer.valueOf(level), header, msg});
            } catch (ClassNotFoundException ex) {
                HwLog.e(BluetoothManagerService.TAG, "autoUpload->HwLogException.msg() ClassNotFoundException, ex:prefixautoupload", ex);
            } catch (NoSuchMethodException ex2) {
                HwLog.e(BluetoothManagerService.TAG, "autoUpload->HwLogException.msg() NoSuchMethodException, ex:prefixautoupload", ex2);
            } catch (Exception ex3) {
                HwLog.e(BluetoothManagerService.TAG, "autoUpload->HwLogException.msg() Exception, ex:prefixautoupload", ex3);
            }
        }

        private void sendBluetoothServiceStateCallback(int state) {
            int n = BluetoothManagerService.this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IBluetoothManagerCallback currentCallback = BluetoothManagerService.this.mCallbacks.getBroadcastItem(i);
                Integer currentPid = (Integer) BluetoothManagerService.this.mCallbacks.getBroadcastCookie(i);
                Message timeoutMsg = this.mKillPidHandler.obtainMessage(1);
                timeoutMsg.arg1 = currentPid.intValue();
                this.mKillPidHandler.sendMessageDelayed(timeoutMsg, 20000);
                if (state == 0) {
                    try {
                        if (isActiveProcess(currentPid.intValue())) {
                            currentCallback.onBluetoothServiceUp(BluetoothManagerService.this.mBluetooth);
                            this.mKillPidHandler.removeMessages(1);
                        }
                    } catch (RemoteException e) {
                        HwLog.e(BluetoothManagerService.TAG, "Unable to call onBluetoothServiceUp() on callback #" + i, e);
                    }
                }
                if (state == 1 && isActiveProcess(currentPid.intValue())) {
                    currentCallback.onBluetoothServiceDown();
                }
                this.mKillPidHandler.removeMessages(1);
            }
            BluetoothManagerService.this.mCallbacks.finishBroadcast();
        }
    }

    class ClientDeathRecipient implements IBinder.DeathRecipient {
        private String mPackageName;

        ClientDeathRecipient(String packageName) {
            this.mPackageName = packageName;
        }

        public void binderDied() {
            Slog.d(BluetoothManagerService.TAG, "Binder is dead - unregister " + this.mPackageName);
            for (Map.Entry<IBinder, ClientDeathRecipient> entry : BluetoothManagerService.this.mBleApps.entrySet()) {
                IBinder token = entry.getKey();
                if (entry.getValue().equals(this)) {
                    BluetoothManagerService.this.updateBleAppCount(token, false, this.mPackageName);
                    return;
                }
            }
        }

        public String getPackageName() {
            return this.mPackageName;
        }
    }

    private final class ProfileServiceConnections implements ServiceConnection, IBinder.DeathRecipient {
        ComponentName mClassName = null;
        Intent mIntent;
        boolean mInvokingProxyCallbacks = false;
        final RemoteCallbackList<IBluetoothProfileServiceConnection> mProxies = new RemoteCallbackList<>();
        IBinder mService = null;

        ProfileServiceConnections(Intent intent) {
            this.mIntent = intent;
        }

        /* access modifiers changed from: private */
        public boolean bindService() {
            try {
                Slog.d(BluetoothManagerService.TAG, "before bindService, unbind service with intent: " + this.mIntent);
                BluetoothManagerService.this.mContext.unbindService(this);
            } catch (IllegalArgumentException e) {
                Slog.w(BluetoothManagerService.TAG, "Unable to unbind service");
            }
            if (this.mIntent == null || this.mService != null || !BluetoothManagerService.this.doBind(this.mIntent, this, 0, UserHandle.CURRENT_OR_SELF)) {
                Slog.w(BluetoothManagerService.TAG, "Unable to bind with intent: " + this.mIntent);
                return false;
            }
            Message msg = BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE);
            msg.obj = this;
            BluetoothManagerService.this.mHandler.sendMessageDelayed(msg, 3000);
            return true;
        }

        /* access modifiers changed from: private */
        public void addProxy(IBluetoothProfileServiceConnection proxy) {
            this.mProxies.register(proxy);
            if (this.mService != null) {
                try {
                    proxy.onServiceConnected(this.mClassName, this.mService);
                } catch (RemoteException e) {
                    Slog.e(BluetoothManagerService.TAG, "Unable to connect to proxy", e);
                }
            } else if (!BluetoothManagerService.this.mHandler.hasMessages(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE, this)) {
                Message msg = BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE);
                msg.obj = this;
                BluetoothManagerService.this.mHandler.sendMessage(msg);
            }
        }

        /* access modifiers changed from: private */
        public void removeProxy(IBluetoothProfileServiceConnection proxy) {
            if (proxy == null) {
                Slog.w(BluetoothManagerService.TAG, "Trying to remove a null proxy");
            } else if (this.mProxies.unregister(proxy)) {
                try {
                    proxy.onServiceDisconnected(this.mClassName);
                } catch (RemoteException e) {
                    Slog.e(BluetoothManagerService.TAG, "Unable to disconnect proxy", e);
                }
            }
        }

        /* access modifiers changed from: private */
        public void removeAllProxies() {
            onServiceDisconnected(this.mClassName);
            this.mProxies.kill();
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothManagerService.this.mHandler.removeMessages(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE, this);
            this.mService = service;
            this.mClassName = className;
            try {
                this.mService.linkToDeath(this, 0);
            } catch (RemoteException e) {
                Slog.e(BluetoothManagerService.TAG, "Unable to linkToDeath", e);
            }
            if (this.mInvokingProxyCallbacks) {
                Slog.e(BluetoothManagerService.TAG, "Proxy callbacks already in progress.");
                return;
            }
            this.mInvokingProxyCallbacks = true;
            int n = this.mProxies.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    this.mProxies.getBroadcastItem(i).onServiceConnected(className, service);
                } catch (RemoteException e2) {
                    Slog.e(BluetoothManagerService.TAG, "Unable to connect to proxy", e2);
                } catch (Throwable th) {
                    this.mProxies.finishBroadcast();
                    this.mInvokingProxyCallbacks = false;
                    throw th;
                }
            }
            this.mProxies.finishBroadcast();
            this.mInvokingProxyCallbacks = false;
        }

        public void onServiceDisconnected(ComponentName className) {
            if (this.mService != null) {
                try {
                    this.mService.unlinkToDeath(this, 0);
                } catch (NoSuchElementException e) {
                    HwLog.e(BluetoothManagerService.TAG, "onServiceDisconnected Unable to unlinkToDeath", e);
                } catch (NullPointerException e2) {
                    HwLog.e(BluetoothManagerService.TAG, "onServiceDisconnected mService is null");
                    return;
                }
                this.mService = null;
                this.mClassName = null;
                if (this.mInvokingProxyCallbacks) {
                    Slog.e(BluetoothManagerService.TAG, "Proxy callbacks already in progress.");
                    return;
                }
                this.mInvokingProxyCallbacks = true;
                int n = this.mProxies.beginBroadcast();
                for (int i = 0; i < n; i++) {
                    try {
                        this.mProxies.getBroadcastItem(i).onServiceDisconnected(className);
                    } catch (RemoteException e3) {
                        Slog.e(BluetoothManagerService.TAG, "Unable to disconnect from proxy", e3);
                    } catch (Throwable th) {
                        this.mProxies.finishBroadcast();
                        this.mInvokingProxyCallbacks = false;
                        throw th;
                    }
                }
                this.mProxies.finishBroadcast();
                this.mInvokingProxyCallbacks = false;
                try {
                    Slog.d(BluetoothManagerService.TAG, "onServiceDisconnected, unbind service with intent: " + this.mIntent);
                    BluetoothManagerService.this.mContext.unbindService(this);
                } catch (IllegalArgumentException e4) {
                    Slog.e(BluetoothManagerService.TAG, "Unable to unbind service with intent: " + this.mIntent, e4);
                }
            }
        }

        public void binderDied() {
            HwLog.w(BluetoothManagerService.TAG, "Profile service for profile: " + this.mClassName + " died.");
            onServiceDisconnected(this.mClassName);
            Message msg = BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE);
            msg.obj = this;
            BluetoothManagerService.this.mHandler.sendMessageDelayed(msg, 3000);
        }
    }

    /* access modifiers changed from: private */
    public static CharSequence timeToLog(long timestamp) {
        return DateFormat.format("MM-dd HH:mm:ss", timestamp);
    }

    /* access modifiers changed from: private */
    public void changeBluetoothStateFromAirplaneMode() {
        if (isBluetoothPersistedStateOn()) {
            if (isAirplaneModeOn()) {
                persistBluetoothSetting(2);
            } else {
                persistBluetoothSetting(1);
            }
        }
        int st = 10;
        try {
            this.mBluetoothLock.readLock().lock();
            if (this.mBluetooth != null) {
                st = this.mBluetooth.getState();
            }
            this.mBluetoothLock.readLock().unlock();
            if ((isAirplaneModeOn() && st != 10) || (!isAirplaneModeOn() && isBluetoothPersistedStateOn() && st != 12)) {
                this.isAirplaneModeChanging = true;
            }
            Slog.d(TAG, "Airplane Mode change - current state:  " + BluetoothAdapter.nameForState(st));
            if (isAirplaneModeOn()) {
                clearBleApps();
                if (st == 15) {
                    try {
                        this.mBluetoothLock.readLock().lock();
                        if (this.mBluetooth != null) {
                            addActiveLog(2, this.mContext.getPackageName(), false);
                            this.mBluetooth.onBrEdrDown();
                            this.mEnable = false;
                            this.mEnableExternal = false;
                        }
                    } catch (RemoteException e) {
                        Slog.e(TAG, "Unable to call onBrEdrDown", e);
                    } catch (Throwable th) {
                        this.mBluetoothLock.readLock().unlock();
                        throw th;
                    }
                } else if (st == 12) {
                    sendDisableMsg(2, this.mContext.getPackageName());
                }
            } else if (this.mEnableExternal) {
                sendEnableMsg(this.mQuietEnableExternal, 2, this.mContext.getPackageName());
            }
        } catch (RemoteException e2) {
            Slog.e(TAG, "Unable to call getState", e2);
        } finally {
            this.mBluetoothLock.readLock().unlock();
        }
    }

    BluetoothManagerService(Context context) {
        this.mContext = context;
        this.mPermissionReviewRequired = context.getResources().getBoolean(17957000);
        this.mCrashes = 0;
        this.mBluetooth = null;
        this.mBluetoothBinder = null;
        this.mBluetoothGatt = null;
        this.mBinding = false;
        this.mUnbinding = false;
        this.mEnable = false;
        this.mState = 10;
        this.mQuietEnableExternal = false;
        this.mEnableExternal = false;
        this.mEnableForNameAndAddress = false;
        this.mAddress = null;
        this.mName = null;
        this.mErrorRecoveryRetryCounter = 0;
        this.mContentResolver = context.getContentResolver();
        this.mLastMessage = 2;
        this.mLastEnableMessageTime = SystemClock.elapsedRealtime();
        registerForBleScanModeChange();
        this.mCallbacks = new RemoteCallbackList<>();
        this.mStateChangeCallbacks = new RemoteCallbackList<>();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED");
        filter.addAction("android.bluetooth.adapter.action.BLUETOOTH_ADDRESS_CHANGED");
        filter.addAction("android.os.action.SETTING_RESTORED");
        filter.setPriority(1000);
        this.mContext.registerReceiver(this.mReceiver, filter);
        loadStoredNameAndAddress();
        if (isBluetoothPersistedStateOn()) {
            Slog.d(TAG, "Startup: Bluetooth persisted state is ON.");
            this.mEnableExternal = true;
        }
        String airplaneModeRadios = Settings.Global.getString(this.mContentResolver, "airplane_mode_radios");
        if (airplaneModeRadios == null || airplaneModeRadios.contains("bluetooth")) {
            this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver);
        }
        int systemUiUid = -1;
        try {
            systemUiUid = !this.mContext.getResources().getBoolean(17956996) ? this.mContext.getPackageManager().getPackageUidAsUser("com.android.systemui", DumpState.DUMP_DEXOPT, 0) : systemUiUid;
            Slog.d(TAG, "Detected SystemUiUid: " + Integer.toString(systemUiUid));
        } catch (PackageManager.NameNotFoundException e) {
            Slog.w(TAG, "Unable to resolve SystemUI's UID.", e);
        }
        this.mSystemUiUid = systemUiUid;
        this.mBluetoothServiceStateCallback = new BluetoothServiceStateCallback();
    }

    private boolean isAirplaneModeOn() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
    }

    private boolean supportBluetoothPersistedState() {
        return this.mContext.getResources().getBoolean(17957035);
    }

    private boolean isBluetoothPersistedStateOn() {
        boolean z = false;
        if (!supportBluetoothPersistedState()) {
            return false;
        }
        int state = Settings.Global.getInt(this.mContentResolver, "bluetooth_on", -1);
        Slog.d(TAG, "Bluetooth persisted state: " + state);
        if (state != 0) {
            z = true;
        }
        return z;
    }

    private boolean isBluetoothPersistedStateOnBluetooth() {
        boolean z = false;
        if (!supportBluetoothPersistedState()) {
            return false;
        }
        if (Settings.Global.getInt(this.mContentResolver, "bluetooth_on", 1) == 1) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void persistBluetoothSetting(int value) {
        Slog.d(TAG, "Persisting Bluetooth Setting: " + value);
        long callingIdentity = Binder.clearCallingIdentity();
        Settings.Global.putInt(this.mContext.getContentResolver(), "bluetooth_on", value);
        Binder.restoreCallingIdentity(callingIdentity);
    }

    /* access modifiers changed from: private */
    public boolean isNameAndAddressSet() {
        return this.mName != null && this.mAddress != null && this.mName.length() > 0 && this.mAddress.length() > 0;
    }

    private void loadStoredNameAndAddress() {
        Slog.d(TAG, "Loading stored name and address");
        if (!this.mContext.getResources().getBoolean(17956898) || Settings.Secure.getInt(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDR_VALID, 0) != 0) {
            this.mName = Settings.Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_NAME);
            this.mAddress = Settings.Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS);
            HwLog.d(TAG, "Stored bluetooth Name=" + this.mName + ",Address=" + getPartMacAddress(this.mAddress));
            return;
        }
        Slog.d(TAG, "invalid bluetooth name and address stored");
    }

    /* access modifiers changed from: private */
    public String getPartMacAddress(String address) {
        if (address == null || address.isEmpty()) {
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        return address.substring(0, address.length() / 2) + ":**:**:**";
    }

    /* access modifiers changed from: private */
    public void storeNameAndAddress(String name, String address) {
        if (name != null) {
            Settings.Secure.putString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_NAME, name);
            this.mName = name;
            Slog.d(TAG, "Stored Bluetooth name: " + Settings.Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_NAME));
        }
        if (address != null) {
            Settings.Secure.putString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS, address);
            this.mAddress = address;
            String addr = Settings.Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS);
            HwLog.d(TAG, "Stored Bluetoothaddress: " + getPartMacAddress(addr));
        }
        if (this.mName != null && this.mAddress != null) {
            Settings.Secure.putInt(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDR_VALID, 1);
            HwLog.d(TAG, "Stored bluetooth_addr_valid to 1 for " + this.mName);
        }
    }

    public IBluetooth registerAdapter(IBluetoothManagerCallback callback) {
        if (callback == null) {
            Slog.w(TAG, "Callback is null in registerAdapter");
            return null;
        }
        Message msg = this.mHandler.obtainMessage(20);
        msg.obj = callback;
        msg.arg1 = Binder.getCallingPid();
        this.mHandler.sendMessage(msg);
        return this.mBluetooth;
    }

    public void unregisterAdapter(IBluetoothManagerCallback callback) {
        if (callback == null) {
            Slog.w(TAG, "Callback is null in unregisterAdapter");
            return;
        }
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        Message msg = this.mHandler.obtainMessage(21);
        msg.obj = callback;
        this.mHandler.sendMessage(msg);
    }

    public void registerStateChangeCallback(IBluetoothStateChangeCallback callback) {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (callback == null) {
            Slog.w(TAG, "registerStateChangeCallback: Callback is null!");
            return;
        }
        Message msg = this.mHandler.obtainMessage(30);
        msg.obj = callback;
        msg.arg1 = Binder.getCallingPid();
        this.mHandler.sendMessage(msg);
    }

    public void unregisterStateChangeCallback(IBluetoothStateChangeCallback callback) {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (callback == null) {
            Slog.w(TAG, "unregisterStateChangeCallback: Callback is null!");
            return;
        }
        Message msg = this.mHandler.obtainMessage(31);
        msg.obj = callback;
        this.mHandler.sendMessage(msg);
    }

    public boolean isEnabled() {
        if (Binder.getCallingUid() == 1000 || checkIfCallerIsForegroundUser()) {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    boolean isEnabled = this.mBluetooth.isEnabled();
                    this.mBluetoothLock.readLock().unlock();
                    return isEnabled;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "isEnabled()", e);
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
                throw th;
            }
            this.mBluetoothLock.readLock().unlock();
            return false;
        }
        Slog.w(TAG, "isEnabled(): not allowed for non-active and non system user");
        return false;
    }

    public int getState() {
        if (Binder.getCallingUid() == 1000 || checkIfCallerIsForegroundUser()) {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    int state = this.mBluetooth.getState();
                    this.mBluetoothLock.readLock().unlock();
                    return state;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "getState()", e);
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
                throw th;
            }
            this.mBluetoothLock.readLock().unlock();
            return 10;
        }
        Slog.w(TAG, "getState(): report OFF for non-active and non system user");
        return 10;
    }

    public boolean isBleScanAlwaysAvailable() {
        boolean z = false;
        if (isAirplaneModeOn() && !this.mEnable) {
            return false;
        }
        try {
            if (Settings.Global.getInt(this.mContentResolver, "ble_scan_always_enabled") != 0) {
                z = true;
            }
            return z;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    private void registerForBleScanModeChange() {
        this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("ble_scan_always_enabled"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                if (!BluetoothManagerService.this.isBleScanAlwaysAvailable()) {
                    BluetoothManagerService.this.disableBleScanMode();
                    BluetoothManagerService.this.clearBleApps();
                    try {
                        BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                        if (BluetoothManagerService.this.mBluetooth != null) {
                            BluetoothManagerService.this.addActiveLog(1, BluetoothManagerService.this.mContext.getPackageName(), false);
                            BluetoothManagerService.this.mBluetooth.onBrEdrDown();
                        }
                    } catch (RemoteException e) {
                        Slog.e(BluetoothManagerService.TAG, "error when disabling bluetooth", e);
                    } catch (Throwable th) {
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                        throw th;
                    }
                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void disableBleScanMode() {
        try {
            this.mBluetoothLock.writeLock().lock();
            if (!(this.mBluetooth == null || this.mBluetooth.getState() == 12)) {
                Slog.d(TAG, "Reseting the mEnable flag for clean disable");
                this.mEnable = false;
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "getState()", e);
        } catch (Throwable th) {
            this.mBluetoothLock.writeLock().unlock();
            throw th;
        }
        this.mBluetoothLock.writeLock().unlock();
    }

    public int updateBleAppCount(IBinder token, boolean enable, String packageName) {
        ClientDeathRecipient r = this.mBleApps.get(token);
        if (r == null && enable) {
            ClientDeathRecipient deathRec = new ClientDeathRecipient(packageName);
            try {
                token.linkToDeath(deathRec, 0);
                this.mBleApps.put(token, deathRec);
                Slog.d(TAG, "Registered for death of " + packageName);
            } catch (RemoteException e) {
                throw new IllegalArgumentException("BLE app (" + packageName + ") already dead!");
            }
        } else if (!enable && r != null) {
            try {
                token.unlinkToDeath(r, 0);
            } catch (NoSuchElementException ex) {
                HwLog.e(TAG, "updateBleAppCount Unable to unlinkToDeath", ex);
            }
            this.mBleApps.remove(token);
            Slog.d(TAG, "Unregistered for death of " + packageName);
        }
        int appCount = this.mBleApps.size();
        Slog.d(TAG, appCount + " registered Ble Apps");
        if (appCount == 0 && this.mEnable) {
            disableBleScanMode();
        }
        if (appCount == 0 && !this.mEnableExternal) {
            sendBrEdrDownCallback();
        }
        return appCount;
    }

    /* access modifiers changed from: private */
    public void clearBleApps() {
        this.mBleApps.clear();
    }

    public boolean isBleAppPresent() {
        Slog.d(TAG, "isBleAppPresent() count: " + this.mBleApps.size());
        return this.mBleApps.size() > 0;
    }

    /* access modifiers changed from: private */
    public void continueFromBleOnState() {
        Slog.d(TAG, "continueFromBleOnState()");
        try {
            this.mBluetoothLock.readLock().lock();
            if (this.mBluetooth == null) {
                Slog.e(TAG, "onBluetoothServiceUp: mBluetooth is null!");
                this.mBluetoothLock.readLock().unlock();
                return;
            }
            if (isBluetoothPersistedStateOnBluetooth() || !isBleAppPresent()) {
                this.mBluetooth.onLeServiceUp();
                persistBluetoothSetting(1);
            }
            this.mBluetoothLock.readLock().unlock();
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to call onServiceUp", e);
        } catch (Throwable th) {
            this.mBluetoothLock.readLock().unlock();
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public void sendBrEdrDownCallback() {
        Slog.d(TAG, "Calling sendBrEdrDownCallback callbacks");
        if (this.mBluetooth == null) {
            Slog.w(TAG, "Bluetooth handle is null");
            return;
        }
        if (isBleAppPresent()) {
            try {
                this.mBluetoothGatt.unregAll();
            } catch (RemoteException e) {
                Slog.e(TAG, "Unable to disconnect all apps.", e);
            }
        } else {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    this.mBluetooth.onBrEdrDown();
                }
            } catch (RemoteException e2) {
                Slog.e(TAG, "Call to onBrEdrDown() failed.", e2);
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
                throw th;
            }
            this.mBluetoothLock.readLock().unlock();
        }
    }

    public boolean isRadioEnabled() {
        boolean z;
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        synchronized (this.mConnection) {
            z = false;
            try {
                if (this.mBluetooth != null && this.mBluetooth.isRadioEnabled()) {
                    z = true;
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "isRadioEnabled()", e);
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return z;
    }

    public void getNameAndAddress() {
        HwLog.d(TAG, "getNameAndAddress(): mBluetooth = " + this.mBluetooth + " mBinding = " + this.mBinding);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE));
    }

    public boolean enableNoAutoConnect(String packageName) {
        if (isBluetoothDisallowed()) {
            Slog.d(TAG, "enableNoAutoConnect(): not enabling - bluetooth disallowed");
            return false;
        }
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM, "Need BLUETOOTH ADMIN permission");
        Slog.d(TAG, "enableNoAutoConnect():  mBluetooth =" + this.mBluetooth + " mBinding = " + this.mBinding);
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1027) {
            synchronized (this.mReceiver) {
                this.mQuietEnableExternal = true;
                this.mEnableExternal = true;
                sendEnableMsg(true, 1, packageName);
            }
            return true;
        }
        throw new SecurityException("no permission to enable Bluetooth quietly");
    }

    public boolean enable(String packageName) throws RemoteException {
        int callingUid = Binder.getCallingUid();
        boolean callerSystem = UserHandle.getAppId(callingUid) == 1000;
        if (isBluetoothDisallowed()) {
            Slog.d(TAG, "enable(): not enabling - bluetooth disallowed");
            return false;
        } else if (HwDeviceManager.disallowOp(8)) {
            HwLog.i(TAG, "bluetooth has been restricted.");
            return false;
        } else {
            boolean needCheck = false;
            if (!callerSystem) {
                if (!checkIfCallerIsForegroundUser()) {
                    Slog.w(TAG, "enable(): not allowed for non-active and non system user");
                    return false;
                }
                this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM, "Need BLUETOOTH ADMIN permission");
                needCheck = checkPrecondition(callingUid);
                if (!isEnabled() && needCheck && startConsentUiIfNeeded(packageName, callingUid, "android.bluetooth.adapter.action.REQUEST_ENABLE")) {
                    return true;
                }
            }
            Slog.d(TAG, "enable(" + packageName + "):  mBluetooth =" + this.mBluetooth + " mBinding = " + this.mBinding + " mState = " + BluetoothAdapter.nameForState(this.mState) + ", needCheck = " + needCheck);
            reportBtServiceChrToDft(11, 1, packageName);
            HwServiceFactory.getHwBluetoothBigDataService().sendBigDataEvent(this.mContext, HwServiceFactory.IHwBluetoothBigDataService.GET_OPEN_BT_APP_NAME);
            synchronized (this.mReceiver) {
                this.mQuietEnableExternal = false;
                this.mEnableExternal = true;
                sendEnableMsg(false, 1, packageName);
            }
            HwLog.d(TAG, "enable returning");
            return true;
        }
    }

    public boolean enableRadio() {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        StringBuilder sb = new StringBuilder();
        sb.append("enable():  mBluetooth =");
        sb.append(this.mBluetooth == null ? "null" : this.mBluetooth);
        sb.append(" mBinding = ");
        sb.append(this.mBinding);
        HwLog.d(TAG, sb.toString());
        synchronized (this.mConnection) {
            if (this.mBinding) {
                HwLog.w(TAG, "enable(): binding in progress. Returning..");
                return true;
            }
            Message msg = this.mHandler.obtainMessage(3);
            msg.arg1 = 1;
            this.mHandler.sendMessage(msg);
            return true;
        }
    }

    public boolean disable(String packageName, boolean persist) throws RemoteException {
        int callingUid = Binder.getCallingUid();
        boolean needCheck = false;
        if (!(UserHandle.getAppId(callingUid) == 1000)) {
            if (!checkIfCallerIsForegroundUser()) {
                Slog.w(TAG, "disable(): not allowed for non-active and non system user");
                return false;
            }
            this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM, "Need BLUETOOTH ADMIN permission");
            needCheck = checkPrecondition(callingUid);
            if (isEnabled() && needCheck && startConsentUiIfNeeded(packageName, callingUid, "android.bluetooth.adapter.action.REQUEST_DISABLE")) {
                return true;
            }
        }
        if (!HwDeviceManager.disallowOp(51) || !persist) {
            Slog.d(TAG, "disable(" + packageName + "): mBluetooth = " + this.mBluetooth + " mBinding = " + this.mBinding + ",needCheck = " + needCheck);
            reportBtServiceChrToDft(11, 2, packageName);
            synchronized (this.mReceiver) {
                if (persist) {
                    try {
                        persistBluetoothSetting(0);
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                this.mEnableExternal = false;
                sendDisableMsg(1, packageName);
            }
            return true;
        }
        Slog.w(TAG, "mdm force open bluetooth, not allow close bluetooth");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                Toast.makeText(BluetoothManagerService.this.mContext, BluetoothManagerService.this.mContext.getResources().getString(33686053), 0).show();
            }
        });
        return false;
    }

    public boolean disableRadio() {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        StringBuilder sb = new StringBuilder();
        sb.append("disable(): mBluetooth = ");
        sb.append(this.mBluetooth == null ? "null" : this.mBluetooth);
        sb.append(" mBinding = ");
        sb.append(this.mBinding);
        HwLog.d(TAG, sb.toString());
        synchronized (this.mConnection) {
            if (this.mBluetooth == null) {
                return false;
            }
            this.mHandler.sendMessage(this.mHandler.obtainMessage(4));
            return true;
        }
    }

    private boolean startConsentUiIfNeeded(String packageName, int callingUid, String intentAction) throws RemoteException {
        if (checkBluetoothPermissionWhenPermissionReviewRequired()) {
            return false;
        }
        try {
            if (this.mContext.getPackageManager().getApplicationInfoAsUser(packageName, 268435456, UserHandle.getUserId(callingUid)).uid == callingUid) {
                Intent intent = new Intent(intentAction);
                intent.putExtra("android.intent.extra.PACKAGE_NAME", packageName);
                intent.setFlags(276824064);
                try {
                    this.mContext.startActivity(intent);
                    return true;
                } catch (ActivityNotFoundException e) {
                    Slog.e(TAG, "Intent to handle action " + intentAction + " missing");
                    return false;
                }
            } else {
                throw new SecurityException("Package " + packageName + " not in uid " + callingUid);
            }
        } catch (PackageManager.NameNotFoundException e2) {
            throw new RemoteException(e2.getMessage());
        }
    }

    private boolean checkBluetoothPermissionWhenPermissionReviewRequired() {
        boolean z = false;
        if (!this.mPermissionReviewRequired) {
            return false;
        }
        if (this.mContext.checkCallingPermission("android.permission.MANAGE_BLUETOOTH_WHEN_PERMISSION_REVIEW_REQUIRED") == 0) {
            z = true;
        }
        return z;
    }

    public void unbindAndFinish() {
        Slog.d(TAG, "unbindAndFinish(): " + this.mBluetooth + " mBinding = " + this.mBinding + " mUnbinding = " + this.mUnbinding);
        try {
            this.mBluetoothLock.writeLock().lock();
            if (this.mUnbinding) {
                this.mBluetoothLock.writeLock().unlock();
                return;
            }
            this.mUnbinding = true;
            this.mHandler.removeMessages(60);
            this.mHandler.removeMessages(MESSAGE_BIND_PROFILE_SERVICE);
            if (this.mBluetooth != null) {
                this.mBluetooth.unregisterCallback(this.mBluetoothCallback);
                this.mBluetoothBinder = null;
                this.mBluetooth = null;
                this.mContext.unbindService(this.mConnection);
                this.mUnbinding = false;
                this.mBinding = false;
            } else {
                this.mUnbinding = false;
            }
            this.mBluetoothGatt = null;
            this.mBluetoothLock.writeLock().unlock();
        } catch (RemoteException re) {
            Slog.e(TAG, "Unable to unregister BluetoothCallback", re);
        } catch (Throwable th) {
            this.mBluetoothLock.writeLock().unlock();
            throw th;
        }
    }

    public IBluetoothGatt getBluetoothGatt() {
        return this.mBluetoothGatt;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0086, code lost:
        r0 = r7.mHandler.obtainMessage(MESSAGE_ADD_PROXY_DELAYED);
        r0.arg1 = r8;
        r0.obj = r9;
        r7.mHandler.sendMessageDelayed(r0, 100);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0099, code lost:
        return true;
     */
    public boolean bindBluetoothProfileService(int bluetoothProfile, IBluetoothProfileServiceConnection proxy) {
        if (!this.mEnable) {
            HwLog.d(TAG, "Trying to bind to profile: " + bluetoothProfile + ", while Bluetooth was disabled");
            return false;
        }
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (proxy == null) {
            HwLog.w(TAG, "proxy is null.");
            return false;
        }
        synchronized (this.mProfileServices) {
            if (this.mProfileServices.get(new Integer(bluetoothProfile)) == null) {
                HwLog.d(TAG, "Creating new ProfileServiceConnections object for profile: " + bluetoothProfile);
                if (bluetoothProfile != 1) {
                    return false;
                }
                ProfileServiceConnections psc = new ProfileServiceConnections(new Intent(IBluetoothHeadset.class.getName()));
                if (!psc.bindService()) {
                    return false;
                }
                this.mProfileServices.put(new Integer(bluetoothProfile), psc);
            }
        }
    }

    public void unbindBluetoothProfileService(int bluetoothProfile, IBluetoothProfileServiceConnection proxy) {
        synchronized (this.mProfileServices) {
            ProfileServiceConnections psc = this.mProfileServices.get(new Integer(bluetoothProfile));
            if (psc != null) {
                psc.removeProxy(proxy);
            }
        }
    }

    /* access modifiers changed from: private */
    public void unbindAllBluetoothProfileServices() {
        synchronized (this.mProfileServices) {
            for (Integer i : this.mProfileServices.keySet()) {
                ProfileServiceConnections psc = this.mProfileServices.get(i);
                try {
                    this.mContext.unbindService(psc);
                } catch (IllegalArgumentException e) {
                    Slog.e(TAG, "Unable to unbind service with intent: " + psc.mIntent, e);
                }
                psc.removeAllProxies();
            }
            this.mProfileServices.clear();
        }
    }

    public void handleOnBootPhase() {
        Slog.d(TAG, "Bluetooth boot completed");
        ((UserManagerInternal) LocalServices.getService(UserManagerInternal.class)).addUserRestrictionsListener(this.mUserRestrictionsListener);
        if (!isBluetoothDisallowed()) {
            if (this.mEnableExternal && isBluetoothPersistedStateOnBluetooth()) {
                Slog.d(TAG, "Auto-enabling Bluetooth.");
                sendEnableMsg(this.mQuietEnableExternal, 6, this.mContext.getPackageName());
            } else if (!isNameAndAddressSet()) {
                if (!"factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
                    Slog.d(TAG, "Getting adapter name and address");
                    this.mEnableForNameAndAddress = true;
                    sendEnableMsg(true, 0, "get name and address");
                } else {
                    Slog.e(TAG, "in factory mode, don't allow to enable BT");
                }
            }
        }
    }

    public void handleOnSwitchUser(int userHandle) {
        Slog.d(TAG, "User " + userHandle + " switched");
        this.mHandler.obtainMessage(300, userHandle, 0).sendToTarget();
    }

    public void handleOnUnlockUser(int userHandle) {
        Slog.d(TAG, "User " + userHandle + " unlocked");
        this.mHandler.obtainMessage(MESSAGE_USER_UNLOCKED, userHandle, 0).sendToTarget();
    }

    private void sendBluetoothStateCallback(boolean isUp) {
        this.mBluetoothServiceStateCallback.sendBluetoothStateCallback(isUp);
    }

    /* access modifiers changed from: private */
    public void sendBluetoothServiceUpCallback() {
        HwLog.d(TAG, "Calling onBluetoothServiceUp callbacks");
        this.mBluetoothServiceStateCallback.sendBluetoothServiceUpCallback();
    }

    /* access modifiers changed from: private */
    public void sendBluetoothServiceDownCallback() {
        HwLog.d(TAG, "Calling onBluetoothServiceDown callbacks");
        this.mBluetoothServiceStateCallback.sendBluetoothServiceDownCallback();
    }

    /* JADX INFO: finally extract failed */
    public String getAddress() {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (Binder.getCallingUid() != 1000 && !checkIfCallerIsForegroundUser()) {
            Slog.w(TAG, "getAddress(): not allowed for non-active and non system user");
            return null;
        } else if (this.mContext.checkCallingOrSelfPermission("android.permission.LOCAL_MAC_ADDRESS") != 0) {
            return "02:00:00:00:00:00";
        } else {
            try {
                this.mBluetoothLock.readLock().lock();
                String addr = null;
                if (this.mBluetooth != null) {
                    addr = this.mBluetooth.getAddress();
                }
                if (addr == null) {
                    addr = this.mAddress;
                } else {
                    this.mAddress = addr;
                }
                this.mBluetoothLock.readLock().unlock();
                return addr;
            } catch (RemoteException e) {
                Slog.e(TAG, "getAddress(): Unable to retrieve address remotely. Returning cached address", e);
                this.mBluetoothLock.readLock().unlock();
                return this.mAddress;
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
                throw th;
            }
        }
    }

    public String getName() {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (Binder.getCallingUid() == 1000 || checkIfCallerIsForegroundUser()) {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    String name = this.mBluetooth.getName();
                    this.mBluetoothLock.readLock().unlock();
                    return name;
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "getName(): Unable to retrieve name remotely. Returning cached name", e);
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
                throw th;
            }
            this.mBluetoothLock.readLock().unlock();
            return this.mName;
        }
        Slog.w(TAG, "getName(): not allowed for non-active and non system user");
        return null;
    }

    /* access modifiers changed from: private */
    public void handleEnable(boolean quietMode) {
        this.mQuietEnable = quietMode;
        try {
            this.mBluetoothLock.writeLock().lock();
            if (this.mBluetooth == null && !this.mBinding) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100), 3000);
                if (!doBind(new Intent(IBluetooth.class.getName()), this.mConnection, 65, UserHandle.CURRENT)) {
                    this.mHandler.removeMessages(100);
                } else {
                    this.mBinding = true;
                }
            } else if (this.mBluetooth != null) {
                if (!this.mQuietEnable) {
                    HwLog.i(TAG, "BT-Enable-FW handleEnable");
                    if (!this.mBluetooth.enable()) {
                        Slog.e(TAG, "IBluetooth.enable() returned false");
                    }
                } else if (!this.mBluetooth.enableNoAutoConnect()) {
                    Slog.e(TAG, "IBluetooth.enableNoAutoConnect() returned false");
                }
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to call enable()", e);
        } catch (Throwable th) {
            this.mBluetoothLock.writeLock().unlock();
            throw th;
        }
        this.mBluetoothLock.writeLock().unlock();
    }

    /* access modifiers changed from: package-private */
    public boolean doBind(Intent intent, ServiceConnection conn, int flags, UserHandle user) {
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), 0);
        intent.setComponent(comp);
        if (comp != null && this.mContext.bindServiceAsUser(intent, conn, flags, user)) {
            return true;
        }
        Slog.e(TAG, "Fail to bind to: " + intent);
        return false;
    }

    /* access modifiers changed from: private */
    public void handleDisable() {
        try {
            this.mBluetoothLock.readLock().lock();
            if (this.mBluetooth != null) {
                Slog.d(TAG, "Sending off request.");
                if (!this.mBluetooth.disable()) {
                    Slog.e(TAG, "IBluetooth.disable() returned false");
                }
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to call disable()", e);
        } catch (Throwable th) {
            this.mBluetoothLock.readLock().unlock();
            throw th;
        }
        this.mBluetoothLock.readLock().unlock();
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x003e A[Catch:{ all -> 0x0072 }] */
    private boolean checkIfCallerIsForegroundUser() {
        boolean valid;
        int callingUser = UserHandle.getCallingUserId();
        int callingUid = Binder.getCallingUid();
        long callingIdentity = Binder.clearCallingIdentity();
        UserInfo ui = ((UserManager) this.mContext.getSystemService("user")).getProfileParent(callingUser);
        int parentUser = ui != null ? ui.id : -10000;
        int callingAppId = UserHandle.getAppId(callingUid);
        boolean z = false;
        try {
            int foregroundUser = ActivityManager.getCurrentUser();
            if (!(callingUser == foregroundUser || parentUser == foregroundUser || callingAppId == 1027)) {
                if (callingAppId != this.mSystemUiUid) {
                    valid = z;
                    if (!valid) {
                        Slog.d(TAG, "checkIfCallerIsForegroundUser: valid=" + valid + " callingUser=" + callingUser + " parentUser=" + parentUser + " foregroundUser=" + foregroundUser);
                    }
                    Binder.restoreCallingIdentity(callingIdentity);
                    int i = foregroundUser;
                    return valid;
                }
            }
            z = true;
            valid = z;
            if (!valid) {
            }
            Binder.restoreCallingIdentity(callingIdentity);
            int i2 = foregroundUser;
            return valid;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingIdentity);
            throw th;
        }
    }

    private void sendBleStateChanged(int prevState, int newState) {
        Slog.d(TAG, "Sending BLE State Change: " + BluetoothAdapter.nameForState(prevState) + " > " + BluetoothAdapter.nameForState(newState));
        Intent intent = new Intent("android.bluetooth.adapter.action.BLE_STATE_CHANGED");
        intent.putExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", prevState);
        intent.putExtra("android.bluetooth.adapter.extra.STATE", newState);
        intent.addFlags(67108864);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BLUETOOTH_PERM);
    }

    /* access modifiers changed from: private */
    public void bluetoothStateChangeHandler(int prevState, int newState) {
        boolean isStandardBroadcast = true;
        if (prevState != newState) {
            if (prevState == 10 && newState == 18) {
                Intent intentRadio1 = new Intent("android.bluetooth.adapter.action.RADIO_STATE_CHANGED");
                intentRadio1.putExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", prevState);
                intentRadio1.putExtra("android.bluetooth.adapter.extra.STATE", newState);
                HwLog.d(TAG, "ACTION_RADIO_STATE_CHANGED, Radio State Change Intent: " + prevState + " -> " + newState);
                this.mContext.sendBroadcast(intentRadio1);
                sendBluetoothServiceDownCallback();
                unbindAndFinish();
                return;
            }
            boolean isUp = false;
            if (newState == 15 || newState == 10) {
                boolean intermediate_off = prevState == 13 && newState == 15;
                if (newState == 10) {
                    Slog.d(TAG, "Bluetooth is complete send Service Down");
                    sendBluetoothStateCallback(false);
                    if (!isRadioEnabled()) {
                        sendBluetoothServiceDownCallback();
                        unbindAndFinish();
                    }
                    sendBleStateChanged(prevState, newState);
                    isStandardBroadcast = false;
                } else if (!intermediate_off) {
                    Slog.d(TAG, "Bluetooth is in LE only mode");
                    if (this.mBluetoothGatt != null || !this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
                        continueFromBleOnState();
                    } else {
                        Slog.d(TAG, "Binding Bluetooth GATT service");
                        doBind(new Intent(IBluetoothGatt.class.getName()), this.mConnection, 65, UserHandle.CURRENT);
                    }
                    sendBleStateChanged(prevState, newState);
                    isStandardBroadcast = false;
                } else if (intermediate_off) {
                    Slog.d(TAG, "Intermediate off, back to LE only mode");
                    sendBleStateChanged(prevState, newState);
                    newState = 10;
                    sendBrEdrDownCallback();
                }
            } else if (newState == 12) {
                if (newState == 12) {
                    isUp = true;
                }
                sendBluetoothStateCallback(isUp);
                sendBleStateChanged(prevState, newState);
            } else if (newState == 14 || newState == 16) {
                sendBleStateChanged(prevState, newState);
                isStandardBroadcast = false;
            } else if (newState == 11 || newState == 13) {
                sendBleStateChanged(prevState, newState);
            }
            HwLog.i(TAG, "isStandardBroadcast=" + isStandardBroadcast + ", prevState=" + prevState + ", newState=" + newState);
            if (newState == 11 && prevState == 15) {
                this.mEnable = true;
            }
            if (isStandardBroadcast) {
                if (prevState == 15) {
                    prevState = 10;
                }
                if (newState == 17 || newState == 18) {
                    Intent intentRadio = new Intent("android.bluetooth.adapter.action.RADIO_STATE_CHANGED");
                    intentRadio.putExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", prevState);
                    intentRadio.putExtra("android.bluetooth.adapter.extra.STATE", newState);
                    HwLog.d(TAG, "send ACTION_RADIO_STATE_CHANGED, Radio State Change Intent: " + prevState + " -> " + newState);
                    this.mContext.sendBroadcast(intentRadio);
                } else if (newState == 15 && prevState == 13) {
                    HwLog.e(TAG, "newState is ble on,so don't send broadcast");
                } else {
                    if (newState == 10 && prevState == 16) {
                        prevState = 13;
                    }
                    HwLog.i(TAG, "send ACTION_STATE_CHANGED, newState=" + newState + ", prevState=" + prevState);
                    Intent intent = new Intent("android.bluetooth.adapter.action.STATE_CHANGED");
                    intent.putExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", prevState);
                    intent.putExtra("android.bluetooth.adapter.extra.STATE", newState);
                    intent.addFlags(83886080);
                    intent.addFlags(268435456);
                    this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BLUETOOTH_PERM);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean waitForOnOff(boolean on, boolean off) {
        int i = 0;
        while (true) {
            if (i >= 10) {
                break;
            }
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth == null) {
                    this.mBluetoothLock.readLock().unlock();
                    break;
                }
                if (on) {
                    if (this.mBluetooth.getState() == 12) {
                        this.mBluetoothLock.readLock().unlock();
                        return true;
                    }
                } else if (off) {
                    if (this.mBluetooth.getState() == 10) {
                        this.mBluetoothLock.readLock().unlock();
                        return true;
                    }
                } else if (this.mBluetooth.getState() != 12) {
                    this.mBluetoothLock.readLock().unlock();
                    return true;
                }
                if (on || off) {
                    SystemClock.sleep(300);
                } else {
                    SystemClock.sleep(50);
                }
                i++;
            } catch (RemoteException e) {
                Slog.e(TAG, "getState()", e);
            } finally {
                this.mBluetoothLock.readLock().unlock();
            }
        }
        Slog.e(TAG, "waitForOnOff time out");
        return false;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0055, code lost:
        if (r8 != false) goto L_0x0060;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0057, code lost:
        if (r9 == false) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x005a, code lost:
        android.os.SystemClock.sleep(50);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0060, code lost:
        android.os.SystemClock.sleep(800);
     */
    public boolean waitForMonitoredOnOff(boolean on, boolean off) {
        int i = 0;
        while (true) {
            if (i >= 10) {
                break;
            }
            synchronized (this.mConnection) {
                try {
                    if (this.mBluetooth == null) {
                        break;
                    } else if (on) {
                        if (this.mBluetooth.getState() == 12) {
                            return true;
                        }
                        if (this.mBluetooth.getState() == 15) {
                            bluetoothStateChangeHandler(14, 15);
                        }
                    } else if (off) {
                        if (this.mBluetooth.getState() == 10) {
                            return true;
                        }
                        if (this.mBluetooth.getState() == 15) {
                            bluetoothStateChangeHandler(13, 15);
                        }
                    } else if (this.mBluetooth.getState() != 12) {
                        return true;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "getState()", e);
                }
            }
            i++;
        }
        Log.e(TAG, "waitForOnOff time out");
        return false;
    }

    /* access modifiers changed from: private */
    public void sendDisableMsg(int reason, String packageName) {
        this.mLastMessage = 2;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
        addActiveLog(reason, packageName, false);
    }

    /* access modifiers changed from: private */
    public void sendEnableMsg(boolean quietMode, int reason, String packageName) {
        long now = SystemClock.elapsedRealtime();
        if (now - this.mLastEnableMessageTime < 1500 && this.mLastMessage == 1 && this.mLastQuietMode == quietMode) {
            HwLog.d(TAG, "MESSAGE_ENABLE message repeat in short time, return");
            this.mLastEnableMessageTime = now;
            return;
        }
        this.mLastEnableMessageTime = now;
        this.mLastMessage = 1;
        this.mLastQuietMode = this.mQuietEnable;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, quietMode, 0));
        addActiveLog(reason, packageName, true);
        this.mLastEnabledTime = SystemClock.elapsedRealtime();
    }

    /* access modifiers changed from: private */
    public void addActiveLog(int reason, String packageName, boolean enable) {
        int state;
        synchronized (this.mActiveLogs) {
            if (this.mActiveLogs.size() > 20) {
                this.mActiveLogs.remove();
            }
            LinkedList<ActiveLog> linkedList = this.mActiveLogs;
            ActiveLog activeLog = new ActiveLog(reason, packageName, enable, System.currentTimeMillis());
            linkedList.add(activeLog);
        }
        if (enable) {
            state = 1;
        } else {
            state = 2;
        }
        StatsLog.write_non_chained(67, Binder.getCallingUid(), null, state, reason, packageName);
    }

    /* access modifiers changed from: private */
    public void addCrashLog() {
        synchronized (this.mCrashTimestamps) {
            if (this.mCrashTimestamps.size() == 100) {
                this.mCrashTimestamps.removeFirst();
            }
            this.mCrashTimestamps.add(Long.valueOf(System.currentTimeMillis()));
            this.mCrashes++;
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    public void recoverBluetoothServiceFromError(boolean clearBle) {
        Slog.e(TAG, "recoverBluetoothServiceFromError");
        try {
            this.mBluetoothLock.readLock().lock();
            if (this.mBluetooth != null) {
                this.mBluetooth.unregisterCallback(this.mBluetoothCallback);
            }
        } catch (RemoteException re) {
            Slog.e(TAG, "Unable to unregister", re);
        } catch (Throwable th) {
            this.mBluetoothLock.readLock().unlock();
            throw th;
        }
        this.mBluetoothLock.readLock().unlock();
        SystemClock.sleep(500);
        addActiveLog(5, this.mContext.getPackageName(), false);
        handleDisable();
        waitForOnOff(false, true);
        sendBluetoothServiceDownCallback();
        try {
            this.mBluetoothLock.writeLock().lock();
            if (this.mBluetooth != null) {
                this.mBluetooth = null;
                this.mContext.unbindService(this.mConnection);
            }
            this.mBluetoothGatt = null;
            this.mBluetoothLock.writeLock().unlock();
            this.mHandler.removeMessages(60);
            this.mState = 10;
            if (clearBle) {
                clearBleApps();
            }
            this.mEnable = false;
            int i = this.mErrorRecoveryRetryCounter;
            this.mErrorRecoveryRetryCounter = i + 1;
            if (i < 6) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(42), 3000);
            }
        } catch (Throwable th2) {
            this.mBluetoothLock.writeLock().unlock();
            throw th2;
        }
    }

    private boolean isBluetoothDisallowed() {
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            return ((UserManager) this.mContext.getSystemService(UserManager.class)).hasUserRestriction("no_bluetooth", UserHandle.SYSTEM);
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    /* access modifiers changed from: private */
    public void updateOppLauncherComponentState(int userId, boolean bluetoothSharingDisallowed) {
        int newState;
        ComponentName oppLauncherComponent = new ComponentName("com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppLauncherActivity");
        if (bluetoothSharingDisallowed) {
            newState = 2;
        } else {
            newState = 0;
        }
        try {
            AppGlobals.getPackageManager().setComponentEnabledSetting(oppLauncherComponent, newState, 1, userId);
        } catch (Exception e) {
        }
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        PrintWriter printWriter = writer;
        String[] args2 = args;
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, printWriter)) {
            String errorMsg = null;
            boolean protoOut = args2.length > 0 && args2[0].startsWith(PriorityDump.PROTO_ARG);
            if (!protoOut) {
                printWriter.println("Bluetooth Status");
                printWriter.println("  enabled: " + isEnabled());
                printWriter.println("  state: " + BluetoothAdapter.nameForState(this.mState));
                printWriter.println("  address: " + getFormatMacAddress(this.mAddress));
                printWriter.println("  name: " + this.mName);
                if (this.mEnable) {
                    long onDuration = SystemClock.elapsedRealtime() - this.mLastEnabledTime;
                    String onDurationString = String.format(Locale.US, "%02d:%02d:%02d.%03d", new Object[]{Integer.valueOf((int) (onDuration / AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT)), Integer.valueOf((int) ((onDuration / 60000) % 60)), Integer.valueOf((int) ((onDuration / 1000) % 60)), Integer.valueOf((int) (onDuration % 1000))});
                    printWriter.println("  time since enabled: " + onDurationString);
                }
                if (this.mActiveLogs.size() == 0) {
                    printWriter.println("\nBluetooth never enabled!");
                } else {
                    printWriter.println("\nEnable log:");
                    Iterator it = this.mActiveLogs.iterator();
                    while (it.hasNext()) {
                        printWriter.println("  " + ((ActiveLog) it.next()));
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append("\nBluetooth crashed ");
                sb.append(this.mCrashes);
                sb.append(" time");
                sb.append(this.mCrashes == 1 ? BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS : "s");
                printWriter.println(sb.toString());
                if (this.mCrashes == 100) {
                    printWriter.println("(last 100)");
                }
                Iterator it2 = this.mCrashTimestamps.iterator();
                while (it2.hasNext()) {
                    printWriter.println("  " + timeToLog(((Long) it2.next()).longValue()));
                }
                StringBuilder sb2 = new StringBuilder();
                sb2.append("\n");
                sb2.append(this.mBleApps.size());
                sb2.append(" BLE app");
                sb2.append(this.mBleApps.size() == 1 ? BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS : "s");
                sb2.append("registered");
                printWriter.println(sb2.toString());
                Iterator<ClientDeathRecipient> it3 = this.mBleApps.values().iterator();
                while (it3.hasNext()) {
                    printWriter.println("  " + it3.next().getPackageName());
                }
                printWriter.println(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                writer.flush();
                if (args2.length == 0) {
                    args2 = new String[]{"--print"};
                }
            }
            String[] args3 = args2;
            if (this.mBluetoothBinder == null) {
                errorMsg = "Bluetooth Service not connected";
                FileDescriptor fileDescriptor = fd;
            } else {
                try {
                    try {
                        this.mBluetoothBinder.dump(fd, args3);
                    } catch (RemoteException e) {
                    }
                } catch (RemoteException e2) {
                    FileDescriptor fileDescriptor2 = fd;
                    errorMsg = "RemoteException while dumping Bluetooth Service";
                    if (errorMsg == null) {
                    }
                }
            }
            if (errorMsg == null && !protoOut) {
                printWriter.println(errorMsg);
            }
        }
    }

    /* access modifiers changed from: private */
    public static String getEnableDisableReasonString(int reason) {
        switch (reason) {
            case 1:
                return "APPLICATION_REQUEST";
            case 2:
                return "AIRPLANE_MODE";
            case 3:
                return "DISALLOWED";
            case 4:
                return "RESTARTED";
            case 5:
                return "START_ERROR";
            case 6:
                return "SYSTEM_BOOT";
            case 7:
                return "CRASH";
            case 8:
                return "USER_SWITCH";
            case 9:
                return "RESTORE_USER_SETTING";
            default:
                return "UNKNOWN[" + reason + "]";
        }
    }

    private String getFormatMacAddress(String address) {
        if (address == null) {
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + null;
        }
        int len = address.length();
        String substring = address.substring(len / 2, len);
        return "******" + substring;
    }

    /* access modifiers changed from: private */
    public void handleEnableRadio() {
        synchronized (this.mConnection) {
            HwLog.i(TAG, "handleEnableRadio mBluetooth = " + this.mBluetooth);
            if (this.mBluetooth == null) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(100), 3000);
                this.mConnection.setGetNameAddressOnly(false);
                this.mConnection.setTurnOnRadio(true);
                Intent i = new Intent(IBluetooth.class.getName());
                i.setComponent(i.resolveSystemService(this.mContext.getPackageManager(), 0));
                if (i.getComponent() == null && i.getPackage() == null) {
                    HwLog.e(TAG, "Illegal Argument ! Fail to open radio !");
                    return;
                } else if (!this.mContext.bindService(i, this.mConnection, 1)) {
                    this.mHandler.removeMessages(100);
                    HwLog.e(TAG, "Fail to bind to: " + IBluetooth.class.getName());
                }
            } else {
                try {
                    HwLog.d(TAG, "Getting and storing Bluetooth name and address prior to enable.");
                    storeNameAndAddress(this.mBluetooth.getName(), this.mBluetooth.getAddress());
                } catch (RemoteException e) {
                    Log.e(TAG, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, e);
                }
                try {
                    if (!this.mBluetooth.enableRadio()) {
                        HwLog.e(TAG, "IBluetooth.enableRadio() returned false");
                    }
                } catch (RemoteException e2) {
                    HwLog.e(TAG, "Unable to call enableRadio()", e2);
                }
            }
        }
        return;
    }

    /* access modifiers changed from: private */
    public void handleDisableRadio() {
        synchronized (this.mConnection) {
            if (isRadioEnabled()) {
                try {
                    if (!this.mBluetooth.disableRadio()) {
                        HwLog.e(TAG, "IBluetooth.disableRadio() returned false");
                    }
                } catch (RemoteException e) {
                    HwLog.e(TAG, "Unable to call disableRadio()", e);
                }
            }
        }
    }

    public boolean checkPrecondition(int uid) {
        return false;
    }

    public void reportBtServiceChrToDft(int code, int subcode, String apkName) {
    }
}
