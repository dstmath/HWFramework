package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManager.ProcessErrorStateInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.bluetooth.IBluetooth;
import android.bluetooth.IBluetoothCallback;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.IBluetoothHeadset;
import android.bluetooth.IBluetoothManager.Stub;
import android.bluetooth.IBluetoothManagerCallback;
import android.bluetooth.IBluetoothProfileServiceConnection;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.hsm.HwSystemManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.util.Slog;
import com.android.server.HwServiceFactory.IHwBluetoothBigDataService;
import com.android.server.HwServiceFactory.IHwIMonitorManager;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ProcessList;
import com.android.server.audio.AudioService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class BluetoothManagerService extends Stub {
    private static final String ACTION_SERVICE_STATE_CHANGED = "com.android.bluetooth.btservice.action.STATE_CHANGED";
    private static final int ADD_PROXY_DELAY_MS = 100;
    private static final String BLUETOOTH_ADMIN_PERM = "android.permission.BLUETOOTH_ADMIN";
    private static final int BLUETOOTH_OFF = 0;
    private static final int BLUETOOTH_ON_AIRPLANE = 2;
    private static final int BLUETOOTH_ON_BLUETOOTH = 1;
    private static final String BLUETOOTH_PERM = "android.permission.BLUETOOTH";
    private static final boolean DBG = true;
    private static final int ENABLE_MESSAGE_REPEAT_MS = 1500;
    private static final int ERROR_RESTART_TIME_MS = 3000;
    private static final String EXTRA_ACTION = "action";
    private static final int MAX_ERROR_RESTART_RETRIES = 6;
    private static final int MAX_SAVE_RETRIES = 3;
    private static final int MESSAGE_ADD_PROXY_DELAYED = 400;
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
    private static final int MESSAGE_TIMEOUT_BIND = 100;
    private static final int MESSAGE_TIMEOUT_UNBIND = 101;
    private static final int MESSAGE_UNREGISTER_ADAPTER = 21;
    private static final int MESSAGE_UNREGISTER_STATE_CHANGE_CALLBACK = 31;
    private static final int MESSAGE_USER_SWITCHED = 300;
    private static final int MESSAGE_USER_UNLOCKED = 301;
    private static final String SECURE_SETTINGS_BLUETOOTH_ADDRESS = "bluetooth_address";
    private static final String SECURE_SETTINGS_BLUETOOTH_ADDR_VALID = "bluetooth_addr_valid";
    private static final String SECURE_SETTINGS_BLUETOOTH_NAME = "bluetooth_name";
    private static final int SERVICE_IBLUETOOTH = 1;
    private static final int SERVICE_IBLUETOOTHGATT = 2;
    private static final int SERVICE_RESTART_TIME_MS = 200;
    private static final String TAG = "BluetoothManagerService";
    private static final int TIMEOUT_BIND_MS = 3000;
    private static final int TIMEOUT_SAVE_MS = 500;
    private static final int USER_SWITCHED_TIME_MS = 200;
    private static int mBleAppCount;
    private String mAddress;
    private boolean mBinding;
    Map<IBinder, ClientDeathRecipient> mBleApps;
    private IBluetooth mBluetooth;
    private IBinder mBluetoothBinder;
    private final IBluetoothCallback mBluetoothCallback;
    private IBluetoothGatt mBluetoothGatt;
    private final ReentrantReadWriteLock mBluetoothLock;
    private final BluetoothServiceStateCallback mBluetoothServiceStateCallback;
    private final RemoteCallbackList<IBluetoothManagerCallback> mCallbacks;
    private BluetoothServiceConnection mConnection;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private boolean mEnable;
    private boolean mEnableExternal;
    private int mErrorRecoveryRetryCounter;
    private final BluetoothHandler mHandler;
    private long mLastEnableMessageTime;
    private int mLastMessage;
    private boolean mLastQuietMode;
    private String mName;
    private final Map<Integer, ProfileServiceConnections> mProfileServices;
    private boolean mQuietEnable;
    private boolean mQuietEnableExternal;
    private final BroadcastReceiver mReceiver;
    private int mState;
    private final RemoteCallbackList<IBluetoothStateChangeCallback> mStateChangeCallbacks;
    private final int mSystemUiUid;
    private boolean mUnbinding;

    /* renamed from: com.android.server.BluetoothManagerService.3 */
    class AnonymousClass3 extends ContentObserver {
        AnonymousClass3(Handler $anonymous0) {
            super($anonymous0);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onChange(boolean selfChange) {
            if (!BluetoothManagerService.this.isBleScanAlwaysAvailable()) {
                BluetoothManagerService.this.disableBleScanMode();
                BluetoothManagerService.this.clearBleApps();
                try {
                    BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                    if (BluetoothManagerService.this.mBluetooth != null) {
                        BluetoothManagerService.this.mBluetooth.onBrEdrDown();
                    }
                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                } catch (RemoteException e) {
                    HwLog.e(BluetoothManagerService.TAG, "error when disabling bluetooth", e);
                } catch (Throwable th) {
                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                }
            }
        }
    }

    private class BluetoothHandler extends Handler {
        boolean mGetNameAddressOnly;

        public BluetoothHandler(Looper looper) {
            super(looper);
            this.mGetNameAddressOnly = false;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            HwLog.d(BluetoothManagerService.TAG, "Message: " + msg.what);
            int i;
            Object callback;
            String str;
            StringBuilder append;
            IBluetoothStateChangeCallback callback2;
            ProfileServiceConnections psc;
            switch (msg.what) {
                case BluetoothManagerService.SERVICE_IBLUETOOTH /*1*/:
                    HwLog.i(BluetoothManagerService.TAG, "BT-Enable-FW MESSAGE_ENABLE: mBluetooth = " + BluetoothManagerService.this.mBluetooth);
                    BluetoothManagerService.this.mHandler.removeMessages(BluetoothManagerService.MESSAGE_RESTART_BLUETOOTH_SERVICE);
                    BluetoothManagerService.this.mEnable = BluetoothManagerService.DBG;
                    try {
                        BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                        if (BluetoothManagerService.this.mBluetooth != null) {
                            if (BluetoothManagerService.this.mBluetooth.getState() == 15) {
                                Slog.w(BluetoothManagerService.TAG, "BT is in BLE_ON State");
                                BluetoothManagerService.this.mBluetooth.onLeServiceUp();
                                break;
                            }
                        }
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                    } catch (RemoteException e) {
                        Slog.e(BluetoothManagerService.TAG, "", e);
                    } finally {
                        BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                    }
                    BluetoothManagerService bluetoothManagerService = BluetoothManagerService.this;
                    i = msg.arg1;
                    bluetoothManagerService.mQuietEnable = r0 == BluetoothManagerService.SERVICE_IBLUETOOTH ? BluetoothManagerService.DBG : false;
                    if (BluetoothManagerService.this.mBluetooth != null) {
                        BluetoothManagerService.this.waitForOnOff(false, BluetoothManagerService.DBG);
                        BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_RESTART_BLUETOOTH_SERVICE), 400);
                        break;
                    }
                    BluetoothManagerService.this.handleEnable(BluetoothManagerService.this.mQuietEnable);
                    break;
                case BluetoothManagerService.SERVICE_IBLUETOOTHGATT /*2*/:
                    HwLog.i(BluetoothManagerService.TAG, "BT-Disable-FW MESSAGE_ENABLE: mBluetooth = " + BluetoothManagerService.this.mBluetooth);
                    BluetoothManagerService.this.mHandler.removeMessages(BluetoothManagerService.MESSAGE_RESTART_BLUETOOTH_SERVICE);
                    if (!BluetoothManagerService.this.skipAndDelayDisable()) {
                        if (BluetoothManagerService.this.mEnable) {
                            if (BluetoothManagerService.this.mBluetooth != null) {
                                BluetoothManagerService.this.waitForOnOff(BluetoothManagerService.DBG, false);
                                BluetoothManagerService.this.mEnable = false;
                                BluetoothManagerService.this.handleDisable();
                                BluetoothManagerService.this.waitForOnOff(false, false);
                                break;
                            }
                        }
                        BluetoothManagerService.this.mEnable = false;
                        BluetoothManagerService.this.handleDisable();
                        break;
                    }
                    HwLog.w(BluetoothManagerService.TAG, "skipAndDelayDisable() return true");
                case BluetoothManagerService.MESSAGE_ENABLE_RADIO /*3*/:
                    HwLog.d(BluetoothManagerService.TAG, "MESSAGE_ENABLE_RADIO: mBluetooth = " + BluetoothManagerService.this.mBluetooth);
                    BluetoothManagerService.this.handleEnableRadio();
                    break;
                case BluetoothManagerService.MESSAGE_DISABLE_RADIO /*4*/:
                    BluetoothManagerService.this.handleDisableRadio();
                    break;
                case BluetoothManagerService.MESSAGE_REGISTER_ADAPTER /*20*/:
                    callback = msg.obj;
                    boolean added = BluetoothManagerService.this.mCallbacks.register(callback, new Integer(msg.arg1));
                    str = BluetoothManagerService.TAG;
                    append = new StringBuilder().append("Added callback: ");
                    if (callback == null) {
                        callback = "null";
                    }
                    HwLog.d(str, append.append(callback).append(":").append(added).append(" pid = ").append(msg.arg1).toString());
                    break;
                case BluetoothManagerService.MESSAGE_UNREGISTER_ADAPTER /*21*/:
                    callback = (IBluetoothManagerCallback) msg.obj;
                    boolean removed = BluetoothManagerService.this.mCallbacks.unregister(callback);
                    str = BluetoothManagerService.TAG;
                    append = new StringBuilder().append("Removed callback: ");
                    if (callback == null) {
                        callback = "null";
                    }
                    HwLog.d(str, append.append(callback).append(":").append(removed).toString());
                    break;
                case BluetoothManagerService.MESSAGE_REGISTER_STATE_CHANGE_CALLBACK /*30*/:
                    callback2 = msg.obj;
                    if (callback2 != null) {
                        HwLog.d(BluetoothManagerService.TAG, "Added state change callback: " + callback2 + ":" + BluetoothManagerService.this.mStateChangeCallbacks.register(callback2, new Integer(msg.arg1)) + " pid = " + msg.arg1);
                        break;
                    }
                    break;
                case BluetoothManagerService.MESSAGE_UNREGISTER_STATE_CHANGE_CALLBACK /*31*/:
                    callback2 = (IBluetoothStateChangeCallback) msg.obj;
                    if (callback2 != null) {
                        BluetoothManagerService.this.mStateChangeCallbacks.unregister(callback2);
                        break;
                    }
                    break;
                case BluetoothManagerService.MESSAGE_BLUETOOTH_SERVICE_CONNECTED /*40*/:
                    HwLog.d(BluetoothManagerService.TAG, "MESSAGE_BLUETOOTH_SERVICE_CONNECTED: " + msg.arg1);
                    IBinder service = msg.obj;
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        i = msg.arg1;
                        if (r0 != BluetoothManagerService.SERVICE_IBLUETOOTHGATT) {
                            BluetoothManagerService.this.mHandler.removeMessages(BluetoothManagerService.MESSAGE_TIMEOUT_BIND);
                            BluetoothManagerService.this.mBinding = false;
                            BluetoothManagerService.this.mBluetoothBinder = service;
                            BluetoothManagerService.this.mBluetooth = IBluetooth.Stub.asInterface(service);
                            if (!BluetoothManagerService.this.isNameAndAddressSet()) {
                                BluetoothManagerService.this.mHandler.sendMessage(BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.USER_SWITCHED_TIME_MS));
                                if (this.mGetNameAddressOnly) {
                                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                                    return;
                                }
                            }
                            if (!BluetoothManagerService.this.mBluetooth.configHciSnoopLog(Secure.getInt(BluetoothManagerService.this.mContentResolver, "bluetooth_hci_log", BluetoothManagerService.BLUETOOTH_OFF) == BluetoothManagerService.SERVICE_IBLUETOOTH ? BluetoothManagerService.DBG : false)) {
                                HwLog.e(BluetoothManagerService.TAG, "IBluetooth.configHciSnoopLog return false");
                            }
                            try {
                                BluetoothManagerService.this.mBluetooth.registerCallback(BluetoothManagerService.this.mBluetoothCallback);
                            } catch (Throwable re) {
                                HwLog.e(BluetoothManagerService.TAG, "Unable to register BluetoothCallback", re);
                            }
                            BluetoothManagerService.this.sendBluetoothServiceUpCallback();
                            if (!BluetoothManagerService.this.mConnection.isTurnOnRadio()) {
                                try {
                                    if (BluetoothManagerService.this.mQuietEnable) {
                                        if (!BluetoothManagerService.this.mBluetooth.enableNoAutoConnect()) {
                                            HwLog.e(BluetoothManagerService.TAG, "IBluetooth.enableNoAutoConnect() returned false");
                                        }
                                    } else {
                                        if (!BluetoothManagerService.this.mBluetooth.enable()) {
                                            HwLog.e(BluetoothManagerService.TAG, "IBluetooth.enable() returned false");
                                        }
                                    }
                                } catch (RemoteException e2) {
                                    HwLog.e(BluetoothManagerService.TAG, "Unable to call enable()", e2);
                                }
                                BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                                if (!BluetoothManagerService.this.mEnable) {
                                    if (!BluetoothManagerService.this.skipAndDelayDisable()) {
                                        BluetoothManagerService.this.waitForOnOff(BluetoothManagerService.DBG, false);
                                        BluetoothManagerService.this.handleDisable();
                                        BluetoothManagerService.this.waitForOnOff(false, false);
                                        break;
                                    }
                                    HwLog.w(BluetoothManagerService.TAG, "skipAndDelayDisable() return true");
                                    return;
                                }
                                HwLog.d(BluetoothManagerService.TAG, "re-getNameAndAddress when bt enabled!");
                                BluetoothManagerService.this.getNameAndAddress();
                                break;
                            }
                            try {
                                if (!BluetoothManagerService.this.mBluetooth.enableRadio()) {
                                    HwLog.e(BluetoothManagerService.TAG, "IBluetooth.enableRadio() returned false");
                                }
                                BluetoothManagerService.this.mConnection.setTurnOnRadio(false);
                            } catch (RemoteException e22) {
                                HwLog.e(BluetoothManagerService.TAG, "Unable to call enableRadio()", e22);
                            }
                            BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                            break;
                        }
                        BluetoothManagerService.this.mBluetoothGatt = IBluetoothGatt.Stub.asInterface(service);
                        BluetoothManagerService.this.onBluetoothGattServiceUp();
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                        break;
                    } catch (RemoteException e222) {
                        HwLog.e(BluetoothManagerService.TAG, "Unable to call configHciSnoopLog", e222);
                    } catch (Throwable th) {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    }
                case BluetoothManagerService.MESSAGE_BLUETOOTH_SERVICE_DISCONNECTED /*41*/:
                    HwLog.e(BluetoothManagerService.TAG, "MESSAGE_BLUETOOTH_SERVICE_DISCONNECTED: " + msg.arg1);
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        i = msg.arg1;
                        if (r0 != BluetoothManagerService.SERVICE_IBLUETOOTH) {
                            i = msg.arg1;
                            if (r0 != BluetoothManagerService.SERVICE_IBLUETOOTHGATT) {
                                HwLog.e(BluetoothManagerService.TAG, "Bad msg.arg1: " + msg.arg1);
                                BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                                break;
                            }
                            BluetoothManagerService.this.mBluetoothGatt = null;
                            BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                            break;
                        }
                        if (BluetoothManagerService.this.mBluetooth != null) {
                            BluetoothManagerService.this.mBluetooth = null;
                            BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                            if (BluetoothManagerService.this.mEnable) {
                                BluetoothManagerService.this.mEnable = false;
                                BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_RESTART_BLUETOOTH_SERVICE), 200);
                            }
                            BluetoothManagerService.this.sendBluetoothServiceDownCallback();
                            if (BluetoothManagerService.this.mState != 11) {
                                break;
                            }
                            BluetoothManagerService.this.bluetoothStateChangeHandler(12, 13);
                            BluetoothManagerService.this.mState = 13;
                            if (BluetoothManagerService.this.mState == 13) {
                                BluetoothManagerService.this.bluetoothStateChangeHandler(13, 10);
                            }
                            BluetoothManagerService.this.mHandler.removeMessages(BluetoothManagerService.MESSAGE_BLUETOOTH_STATE_CHANGE);
                            BluetoothManagerService.this.mState = 10;
                            break;
                        }
                        break;
                    } finally {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    }
                case BluetoothManagerService.MESSAGE_RESTART_BLUETOOTH_SERVICE /*42*/:
                    HwLog.d(BluetoothManagerService.TAG, "MESSAGE_RESTART_BLUETOOTH_SERVICE: Restart IBluetooth service");
                    BluetoothManagerService.this.mEnable = BluetoothManagerService.DBG;
                    BluetoothManagerService.this.handleEnable(BluetoothManagerService.this.mQuietEnable);
                    break;
                case BluetoothManagerService.MESSAGE_BLUETOOTH_STATE_CHANGE /*60*/:
                    int prevState = msg.arg1;
                    int newState = msg.arg2;
                    HwLog.d(BluetoothManagerService.TAG, "MESSAGE_BLUETOOTH_STATE_CHANGE: prevState = " + prevState + ", newState=" + newState);
                    BluetoothManagerService.this.mState = newState;
                    BluetoothManagerService.this.bluetoothStateChangeHandler(prevState, newState);
                    if (prevState == 14 && newState == 10) {
                        if (BluetoothManagerService.this.mBluetooth != null) {
                            if (BluetoothManagerService.this.mEnable) {
                                BluetoothManagerService.this.recoverBluetoothServiceFromError();
                            }
                        }
                    }
                    if (prevState == 11 && newState == 15) {
                        if (BluetoothManagerService.this.mBluetooth != null) {
                            if (BluetoothManagerService.this.mEnable) {
                                BluetoothManagerService.this.recoverBluetoothServiceFromError();
                            }
                        }
                    }
                    if (prevState == 16 && newState == 10) {
                        if (BluetoothManagerService.this.mEnable) {
                            Slog.d(BluetoothManagerService.TAG, "Entering STATE_OFF but mEnabled is true; restarting.");
                            BluetoothManagerService.this.waitForOnOff(false, BluetoothManagerService.DBG);
                            BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_RESTART_BLUETOOTH_SERVICE), 400);
                        }
                    }
                    if (newState == 12 || newState == 15) {
                        if (BluetoothManagerService.this.mErrorRecoveryRetryCounter != 0) {
                            HwLog.w(BluetoothManagerService.TAG, "bluetooth is recovered from error");
                            BluetoothManagerService.this.mErrorRecoveryRetryCounter = BluetoothManagerService.BLUETOOTH_OFF;
                            break;
                        }
                    }
                    break;
                case BluetoothManagerService.MESSAGE_TIMEOUT_BIND /*100*/:
                    HwLog.e(BluetoothManagerService.TAG, "MESSAGE_TIMEOUT_BIND");
                    BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                    BluetoothManagerService.this.mBinding = false;
                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    break;
                case BluetoothManagerService.MESSAGE_TIMEOUT_UNBIND /*101*/:
                    HwLog.e(BluetoothManagerService.TAG, "MESSAGE_TIMEOUT_UNBIND");
                    BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                    BluetoothManagerService.this.mUnbinding = false;
                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    break;
                case BluetoothManagerService.USER_SWITCHED_TIME_MS /*200*/:
                    HwLog.d(BluetoothManagerService.TAG, "MESSAGE_GET_NAME_AND_ADDRESS");
                    try {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                        if (BluetoothManagerService.this.mBluetooth == null) {
                            if (!BluetoothManagerService.this.mBinding) {
                                HwLog.d(BluetoothManagerService.TAG, "Binding to service to get name and address");
                                this.mGetNameAddressOnly = BluetoothManagerService.DBG;
                                BluetoothManagerService.this.mHandler.sendMessageDelayed(BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_TIMEOUT_BIND), 3000);
                                if (BluetoothManagerService.this.doBind(new Intent(IBluetooth.class.getName()), BluetoothManagerService.this.mConnection, 65, UserHandle.CURRENT)) {
                                    BluetoothManagerService.this.mBinding = BluetoothManagerService.DBG;
                                } else {
                                    BluetoothManagerService.this.mHandler.removeMessages(BluetoothManagerService.MESSAGE_TIMEOUT_BIND);
                                }
                                BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                                break;
                            }
                        }
                        if (BluetoothManagerService.this.mBluetooth != null) {
                            BluetoothManagerService.this.storeNameAndAddress(BluetoothManagerService.this.mBluetooth.getName(), BluetoothManagerService.this.mBluetooth.getAddress());
                            if (this.mGetNameAddressOnly) {
                                if (!BluetoothManagerService.this.mEnable) {
                                    BluetoothManagerService.this.unbindAndFinish();
                                }
                            }
                            this.mGetNameAddressOnly = false;
                        }
                    } catch (Throwable re2) {
                        Slog.e(BluetoothManagerService.TAG, "Unable to grab names", re2);
                    } catch (Throwable th2) {
                        BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                    }
                    BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                case BluetoothManagerService.MESSAGE_USER_SWITCHED /*300*/:
                    HwLog.d(BluetoothManagerService.TAG, "MESSAGE_USER_SWITCHED");
                    BluetoothManagerService.this.mHandler.removeMessages(BluetoothManagerService.MESSAGE_USER_SWITCHED);
                    if (BluetoothManagerService.this.mEnable) {
                        if (BluetoothManagerService.this.mBluetooth != null) {
                            try {
                                BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                                if (BluetoothManagerService.this.mBluetooth != null) {
                                    BluetoothManagerService.this.mBluetooth.unregisterCallback(BluetoothManagerService.this.mBluetoothCallback);
                                }
                                BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                            } catch (Throwable re22) {
                                HwLog.e(BluetoothManagerService.TAG, "Unable to unregister", re22);
                            } catch (Throwable th3) {
                                break;
                            }
                            if (BluetoothManagerService.this.mState == 13) {
                                BluetoothManagerService.this.bluetoothStateChangeHandler(BluetoothManagerService.this.mState, 10);
                                BluetoothManagerService.this.mState = 10;
                            }
                            if (BluetoothManagerService.this.mState == 10) {
                                BluetoothManagerService.this.bluetoothStateChangeHandler(BluetoothManagerService.this.mState, 11);
                                BluetoothManagerService.this.mState = 11;
                            }
                            BluetoothManagerService.this.waitForMonitoredOnOff(BluetoothManagerService.DBG, false);
                            if (BluetoothManagerService.this.mState == 11) {
                                BluetoothManagerService.this.bluetoothStateChangeHandler(BluetoothManagerService.this.mState, 12);
                            }
                            BluetoothManagerService.this.unbindAllBluetoothProfileServices();
                            BluetoothManagerService.this.handleDisable();
                            BluetoothManagerService.this.bluetoothStateChangeHandler(12, 13);
                            boolean didDisableTimeout = BluetoothManagerService.this.waitForMonitoredOnOff(false, BluetoothManagerService.DBG) ? false : BluetoothManagerService.DBG;
                            BluetoothManagerService.this.bluetoothStateChangeHandler(13, 10);
                            BluetoothManagerService.this.sendBluetoothServiceDownCallback();
                            try {
                                BluetoothManagerService.this.mBluetoothLock.writeLock().lock();
                                if (BluetoothManagerService.this.mBluetooth != null) {
                                    BluetoothManagerService.this.mBluetooth = null;
                                    BluetoothManagerService.this.mContext.unbindService(BluetoothManagerService.this.mConnection);
                                }
                                BluetoothManagerService.this.mBluetoothGatt = null;
                                if (didDisableTimeout) {
                                    SystemClock.sleep(3000);
                                } else {
                                    SystemClock.sleep(100);
                                }
                                BluetoothManagerService.this.mHandler.removeMessages(BluetoothManagerService.MESSAGE_BLUETOOTH_STATE_CHANGE);
                                BluetoothManagerService.this.mState = 10;
                                BluetoothManagerService.this.handleEnable(BluetoothManagerService.this.mQuietEnable);
                                break;
                            } finally {
                                BluetoothManagerService.this.mBluetoothLock.writeLock().unlock();
                            }
                        }
                    }
                    if (!BluetoothManagerService.this.mBinding) {
                        break;
                    }
                    Message userMsg = BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_USER_SWITCHED);
                    userMsg.arg2 = msg.arg2 + BluetoothManagerService.SERVICE_IBLUETOOTH;
                    BluetoothManagerService.this.mHandler.sendMessageDelayed(userMsg, 200);
                    HwLog.d(BluetoothManagerService.TAG, "delay MESSAGE_USER_SWITCHED " + userMsg.arg2);
                    break;
                case BluetoothManagerService.MESSAGE_USER_UNLOCKED /*301*/:
                    Slog.d(BluetoothManagerService.TAG, "MESSAGE_USER_UNLOCKED");
                    BluetoothManagerService.this.mHandler.removeMessages(BluetoothManagerService.MESSAGE_USER_SWITCHED);
                    if (BluetoothManagerService.this.mEnable) {
                        if (!BluetoothManagerService.this.mBinding) {
                            if (BluetoothManagerService.this.mBluetooth == null) {
                                Slog.d(BluetoothManagerService.TAG, "Enabled but not bound; retrying after unlock");
                                BluetoothManagerService.this.handleEnable(BluetoothManagerService.this.mQuietEnable);
                                break;
                            }
                        }
                    }
                    break;
                case BluetoothManagerService.MESSAGE_ADD_PROXY_DELAYED /*400*/:
                    psc = (ProfileServiceConnections) BluetoothManagerService.this.mProfileServices.get(new Integer(msg.arg1));
                    if (psc != null) {
                        psc.addProxy(msg.obj);
                        break;
                    }
                    break;
                case BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE /*401*/:
                    psc = (ProfileServiceConnections) msg.obj;
                    removeMessages(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE, msg.obj);
                    if (psc != null) {
                        psc.bindService();
                        break;
                    }
                    break;
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

        public void onServiceConnected(ComponentName className, IBinder service) {
            HwLog.d(BluetoothManagerService.TAG, "BluetoothServiceConnection: " + className.getClassName());
            Message msg = BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_BLUETOOTH_SERVICE_CONNECTED);
            if (className.getClassName().equals("com.android.bluetooth.btservice.AdapterService")) {
                msg.arg1 = BluetoothManagerService.SERVICE_IBLUETOOTH;
            } else if (className.getClassName().equals("com.android.bluetooth.gatt.GattService")) {
                msg.arg1 = BluetoothManagerService.SERVICE_IBLUETOOTHGATT;
            } else {
                HwLog.e(BluetoothManagerService.TAG, "Unknown service connected: " + className.getClassName());
                return;
            }
            msg.obj = service;
            BluetoothManagerService.this.mHandler.sendMessage(msg);
        }

        public void onServiceDisconnected(ComponentName className) {
            HwLog.d(BluetoothManagerService.TAG, "BluetoothServiceConnection, disconnected: " + className.getClassName());
            Message msg = BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_BLUETOOTH_SERVICE_DISCONNECTED);
            if (className.getClassName().equals("com.android.bluetooth.btservice.AdapterService")) {
                msg.arg1 = BluetoothManagerService.SERVICE_IBLUETOOTH;
            } else if (className.getClassName().equals("com.android.bluetooth.gatt.GattService")) {
                msg.arg1 = BluetoothManagerService.SERVICE_IBLUETOOTHGATT;
            } else {
                HwLog.e(BluetoothManagerService.TAG, "Unknown service disconnected: " + className.getClassName());
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
                    switch (msg.what) {
                        case BluetoothServiceStateCallback.SERVICE_DOWN /*1*/:
                            int[] pids = new int[BluetoothServiceStateCallback.SERVICE_DOWN];
                            pids[BluetoothServiceStateCallback.BUG_TYPE_CALLBACK_TIMEOUT] = msg.arg1;
                            ActivityManagerService ams = (ActivityManagerService) ServiceManager.getService("activity");
                            String logMsg = "mKillPidHandler---pids = " + pids[BluetoothServiceStateCallback.BUG_TYPE_CALLBACK_TIMEOUT] + " getAppName = " + BluetoothServiceStateCallback.this.getAppName(pids[BluetoothServiceStateCallback.BUG_TYPE_CALLBACK_TIMEOUT]);
                            HwLog.e(BluetoothManagerService.TAG, logMsg);
                            if (Process.myPid() != pids[BluetoothServiceStateCallback.BUG_TYPE_CALLBACK_TIMEOUT]) {
                                ams.killPids(pids, "BluetoothManagerService callback timeout", BluetoothManagerService.DBG);
                                BluetoothServiceStateCallback.this.autoUpload(BluetoothServiceStateCallback.AUTO_LOG_BUG_TYPE_FUNCTION_FAULT, BluetoothServiceStateCallback.BUG_TYPE_CALLBACK_TIMEOUT, logMsg);
                                if (!HwServiceFactory.getHwIMonitorManager().uploadBtRadarEvent(IHwIMonitorManager.IMONITOR_BINDER_FAILED, logMsg)) {
                                    HwLog.d(BluetoothManagerService.TAG, "upload MESSAGE_BINDER_CALLBACK_TIMEOUT failed!");
                                }
                            }
                        default:
                    }
                }
            };
        }

        private String getAppName(int pID) {
            List<RunningAppProcessInfo> appProcessList = ((ActivityManager) BluetoothManagerService.this.mContext.getSystemService("activity")).getRunningAppProcesses();
            if (appProcessList != null) {
                for (RunningAppProcessInfo appProcess : appProcessList) {
                    if (appProcess.pid == pID) {
                        return appProcess.processName;
                    }
                }
            }
            return null;
        }

        private boolean isActiveProcess(int pID) {
            if (getAppName(pID) != null) {
                return BluetoothManagerService.DBG;
            }
            List<ProcessErrorStateInfo> appProcessErrorList = ((ActivityManager) BluetoothManagerService.this.mContext.getSystemService("activity")).getProcessesInErrorState();
            if (appProcessErrorList != null) {
                for (ProcessErrorStateInfo appProcessError : appProcessErrorList) {
                    if (appProcessError.pid == pID && appProcessError.condition == AUTO_LOG_BUG_TYPE_FUNCTION_FAULT) {
                        return BluetoothManagerService.DBG;
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
                i = BUG_TYPE_CALLBACK_TIMEOUT;
                while (i < n) {
                    IBluetoothStateChangeCallback currentCallback = (IBluetoothStateChangeCallback) BluetoothManagerService.this.mStateChangeCallbacks.getBroadcastItem(i);
                    Integer currentPid = (Integer) BluetoothManagerService.this.mStateChangeCallbacks.getBroadcastCookie(i);
                    Message timeoutMsg = this.mKillPidHandler.obtainMessage(SERVICE_DOWN);
                    timeoutMsg.arg1 = currentPid.intValue();
                    this.mKillPidHandler.sendMessageDelayed(timeoutMsg, 20000);
                    ((IBluetoothStateChangeCallback) BluetoothManagerService.this.mStateChangeCallbacks.getBroadcastItem(i)).onBluetoothStateChange(isUp);
                    this.mKillPidHandler.removeMessages(SERVICE_DOWN);
                    i += SERVICE_DOWN;
                }
                BluetoothManagerService.this.mStateChangeCallbacks.finishBroadcast();
            } catch (RemoteException e) {
                HwLog.e(BluetoothManagerService.TAG, "Unable to call onBluetoothStateChange() on callback #" + i, e);
            } catch (Throwable th) {
                BluetoothManagerService.this.mStateChangeCallbacks.finishBroadcast();
            }
        }

        public void sendBluetoothServiceUpCallback() {
            sendBluetoothServiceStateCallback(BUG_TYPE_CALLBACK_TIMEOUT);
        }

        public void sendBluetoothServiceDownCallback() {
            sendBluetoothServiceStateCallback(SERVICE_DOWN);
        }

        private void autoUpload(int bugType, int sceneDef, String msg) {
            StringBuilder sb = new StringBuilder(DumpState.DUMP_SHARED_USERS);
            sb.append("Package:").append("com.android.bluetooth").append("\n");
            sb.append("APK version:").append("1").append("\n");
            sb.append("Bug type:").append(bugType).append("\n");
            sb.append("Scene def:").append(sceneDef).append("\n");
            HwLog.i(BluetoothManagerService.TAG, "autoUpload->bugType:" + bugType + "; sceneDef:" + sceneDef + "; msg:" + msg + ";" + PREFIX_AUTO_UPLOAD);
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.sLastAutoUploadTime < AUTO_UPLOAD_MIN_INTERVAL_TIME) {
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
                Class[] clsArr = new Class[BluetoothManagerService.MESSAGE_DISABLE_RADIO];
                clsArr[BUG_TYPE_CALLBACK_TIMEOUT] = String.class;
                clsArr[SERVICE_DOWN] = Integer.TYPE;
                clsArr[AUTO_LOG_BUG_TYPE_FUNCTION_FAULT] = String.class;
                clsArr[BluetoothManagerService.MESSAGE_ENABLE_RADIO] = String.class;
                Method method = clazz.getMethod("msg", clsArr);
                Object newInstance = clazz.newInstance();
                Object[] objArr = new Object[BluetoothManagerService.MESSAGE_DISABLE_RADIO];
                objArr[BUG_TYPE_CALLBACK_TIMEOUT] = appId;
                objArr[SERVICE_DOWN] = Integer.valueOf(level);
                objArr[AUTO_LOG_BUG_TYPE_FUNCTION_FAULT] = header;
                objArr[BluetoothManagerService.MESSAGE_ENABLE_RADIO] = msg;
                method.invoke(newInstance, objArr);
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
            for (int i = BUG_TYPE_CALLBACK_TIMEOUT; i < n; i += SERVICE_DOWN) {
                IBluetoothManagerCallback currentCallback = (IBluetoothManagerCallback) BluetoothManagerService.this.mCallbacks.getBroadcastItem(i);
                Integer currentPid = (Integer) BluetoothManagerService.this.mCallbacks.getBroadcastCookie(i);
                Message timeoutMsg = this.mKillPidHandler.obtainMessage(SERVICE_DOWN);
                timeoutMsg.arg1 = currentPid.intValue();
                this.mKillPidHandler.sendMessageDelayed(timeoutMsg, 20000);
                if (state == 0) {
                    try {
                        if (isActiveProcess(currentPid.intValue())) {
                            currentCallback.onBluetoothServiceUp(BluetoothManagerService.this.mBluetooth);
                            this.mKillPidHandler.removeMessages(SERVICE_DOWN);
                        }
                    } catch (RemoteException e) {
                        HwLog.e(BluetoothManagerService.TAG, "Unable to call onBluetoothServiceUp() on callback #" + i, e);
                    }
                }
                if (state == SERVICE_DOWN) {
                    currentCallback.onBluetoothServiceDown();
                }
                this.mKillPidHandler.removeMessages(SERVICE_DOWN);
            }
            BluetoothManagerService.this.mCallbacks.finishBroadcast();
        }
    }

    class ClientDeathRecipient implements DeathRecipient {
        ClientDeathRecipient() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void binderDied() {
            HwLog.d(BluetoothManagerService.TAG, "Binder is dead -  unregister Ble App");
            if (BluetoothManagerService.mBleAppCount > 0) {
                BluetoothManagerService.mBleAppCount = BluetoothManagerService.mBleAppCount - 1;
            }
            if (BluetoothManagerService.mBleAppCount == 0) {
                HwLog.d(BluetoothManagerService.TAG, "Disabling LE only mode after application crash");
                try {
                    BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                    if (BluetoothManagerService.this.mBluetooth != null && BluetoothManagerService.this.mBluetooth.getState() == 15) {
                        BluetoothManagerService.this.mEnable = false;
                        BluetoothManagerService.this.mBluetooth.onBrEdrDown();
                    }
                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                } catch (RemoteException e) {
                    HwLog.e(BluetoothManagerService.TAG, "Unable to call onBrEdrDown", e);
                } catch (Throwable th) {
                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                }
            }
        }
    }

    private final class ProfileServiceConnections implements ServiceConnection, DeathRecipient {
        ComponentName mClassName;
        Intent mIntent;
        boolean mInvokingProxyCallbacks;
        final RemoteCallbackList<IBluetoothProfileServiceConnection> mProxies;
        IBinder mService;

        ProfileServiceConnections(Intent intent) {
            this.mProxies = new RemoteCallbackList();
            this.mInvokingProxyCallbacks = false;
            this.mService = null;
            this.mClassName = null;
            this.mIntent = intent;
        }

        private boolean bindService() {
            if (this.mIntent != null && this.mService == null && BluetoothManagerService.this.doBind(this.mIntent, this, BluetoothManagerService.BLUETOOTH_OFF, UserHandle.CURRENT_OR_SELF)) {
                Message msg = BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE);
                msg.obj = this;
                BluetoothManagerService.this.mHandler.sendMessageDelayed(msg, 3000);
                return BluetoothManagerService.DBG;
            }
            HwLog.w(BluetoothManagerService.TAG, "Unable to bind with intent: " + this.mIntent);
            return false;
        }

        private void addProxy(IBluetoothProfileServiceConnection proxy) {
            this.mProxies.register(proxy);
            if (this.mService != null) {
                try {
                    proxy.onServiceConnected(this.mClassName, this.mService);
                } catch (RemoteException e) {
                    HwLog.e(BluetoothManagerService.TAG, "Unable to connect to proxy", e);
                }
            } else if (!BluetoothManagerService.this.mHandler.hasMessages(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE, this)) {
                Message msg = BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE);
                msg.obj = this;
                BluetoothManagerService.this.mHandler.sendMessage(msg);
            }
        }

        private void removeProxy(IBluetoothProfileServiceConnection proxy) {
            if (proxy == null) {
                HwLog.w(BluetoothManagerService.TAG, "Trying to remove a null proxy");
            } else if (this.mProxies.unregister(proxy)) {
                try {
                    proxy.onServiceDisconnected(this.mClassName);
                } catch (RemoteException e) {
                    HwLog.e(BluetoothManagerService.TAG, "Unable to disconnect proxy", e);
                }
            }
        }

        private void removeAllProxies() {
            onServiceDisconnected(this.mClassName);
            this.mProxies.kill();
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothManagerService.this.mHandler.removeMessages(BluetoothManagerService.MESSAGE_BIND_PROFILE_SERVICE, this);
            this.mService = service;
            this.mClassName = className;
            try {
                this.mService.linkToDeath(this, BluetoothManagerService.BLUETOOTH_OFF);
            } catch (RemoteException e) {
                HwLog.e(BluetoothManagerService.TAG, "Unable to linkToDeath", e);
            }
            if (this.mInvokingProxyCallbacks) {
                HwLog.e(BluetoothManagerService.TAG, "Proxy callbacks already in progress.");
                return;
            }
            this.mInvokingProxyCallbacks = BluetoothManagerService.DBG;
            int n = this.mProxies.beginBroadcast();
            for (int i = BluetoothManagerService.BLUETOOTH_OFF; i < n; i += BluetoothManagerService.SERVICE_IBLUETOOTH) {
                try {
                    ((IBluetoothProfileServiceConnection) this.mProxies.getBroadcastItem(i)).onServiceConnected(className, service);
                } catch (RemoteException e2) {
                    HwLog.e(BluetoothManagerService.TAG, "Unable to connect to proxy", e2);
                } catch (Throwable th) {
                    this.mProxies.finishBroadcast();
                    this.mInvokingProxyCallbacks = false;
                }
            }
            this.mProxies.finishBroadcast();
            this.mInvokingProxyCallbacks = false;
        }

        public void onServiceDisconnected(ComponentName className) {
            if (this.mService != null) {
                try {
                    this.mService.unlinkToDeath(this, BluetoothManagerService.BLUETOOTH_OFF);
                } catch (NoSuchElementException e) {
                    HwLog.e(BluetoothManagerService.TAG, "onServiceDisconnected Unable to unlinkToDeath", e);
                }
                this.mService = null;
                this.mClassName = null;
                if (this.mInvokingProxyCallbacks) {
                    HwLog.e(BluetoothManagerService.TAG, "Proxy callbacks already in progress.");
                    return;
                }
                this.mInvokingProxyCallbacks = BluetoothManagerService.DBG;
                int n = this.mProxies.beginBroadcast();
                for (int i = BluetoothManagerService.BLUETOOTH_OFF; i < n; i += BluetoothManagerService.SERVICE_IBLUETOOTH) {
                    try {
                        ((IBluetoothProfileServiceConnection) this.mProxies.getBroadcastItem(i)).onServiceDisconnected(className);
                    } catch (RemoteException e2) {
                        HwLog.e(BluetoothManagerService.TAG, "Unable to disconnect from proxy", e2);
                    } catch (Throwable th) {
                        this.mProxies.finishBroadcast();
                        this.mInvokingProxyCallbacks = false;
                    }
                }
                this.mProxies.finishBroadcast();
                this.mInvokingProxyCallbacks = false;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.BluetoothManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.BluetoothManagerService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.BluetoothManagerService.<clinit>():void");
    }

    private void registerForAirplaneMode(IntentFilter filter) {
        boolean mIsAirplaneSensitive;
        ContentResolver resolver = this.mContext.getContentResolver();
        String airplaneModeRadios = Global.getString(resolver, "airplane_mode_radios");
        String toggleableRadios = Global.getString(resolver, "airplane_mode_toggleable_radios");
        if (airplaneModeRadios == null) {
            mIsAirplaneSensitive = DBG;
        } else {
            mIsAirplaneSensitive = airplaneModeRadios.contains("bluetooth");
        }
        if (mIsAirplaneSensitive) {
            filter.addAction("android.intent.action.AIRPLANE_MODE");
        }
    }

    BluetoothManagerService(Context context) {
        this.mBluetoothLock = new ReentrantReadWriteLock();
        this.mQuietEnable = false;
        this.mProfileServices = new HashMap();
        this.mBluetoothCallback = new IBluetoothCallback.Stub() {
            public void onBluetoothStateChange(int prevState, int newState) throws RemoteException {
                HwLog.i(BluetoothManagerService.TAG, "mBluetoothCallback, onBluetoothStateChange prevState=" + prevState + ", newState=" + newState);
                BluetoothManagerService.this.mHandler.sendMessage(BluetoothManagerService.this.mHandler.obtainMessage(BluetoothManagerService.MESSAGE_BLUETOOTH_STATE_CHANGE, prevState, newState));
            }
        };
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED".equals(action)) {
                    String newName = intent.getStringExtra("android.bluetooth.adapter.extra.LOCAL_NAME");
                    Slog.d(BluetoothManagerService.TAG, "Bluetooth Adapter name changed to " + newName);
                    if (newName != null) {
                        BluetoothManagerService.this.storeNameAndAddress(newName, null);
                    }
                } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                    synchronized (BluetoothManagerService.this.mReceiver) {
                        if (BluetoothManagerService.this.isBluetoothPersistedStateOn()) {
                            if (BluetoothManagerService.this.isAirplaneModeOn()) {
                                BluetoothManagerService.this.persistBluetoothSetting(BluetoothManagerService.SERVICE_IBLUETOOTHGATT);
                            } else {
                                BluetoothManagerService.this.persistBluetoothSetting(BluetoothManagerService.SERVICE_IBLUETOOTH);
                            }
                        }
                        int st = 10;
                        try {
                            BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                            if (BluetoothManagerService.this.mBluetooth != null) {
                                st = BluetoothManagerService.this.mBluetooth.getState();
                                if (st == 10) {
                                    BluetoothManagerService.this.mEnableExternal = false;
                                }
                            }
                            BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                        } catch (RemoteException e) {
                            Slog.e(BluetoothManagerService.TAG, "Unable to call getState", e);
                            BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                        } catch (Throwable th) {
                            BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                        }
                        Slog.d(BluetoothManagerService.TAG, AudioService.CONNECT_INTENT_KEY_STATE + st);
                        if (BluetoothManagerService.this.isAirplaneModeOn()) {
                            int BleAppNum = BluetoothManagerService.mBleAppCount;
                            synchronized (this) {
                                BluetoothManagerService.mBleAppCount = BluetoothManagerService.BLUETOOTH_OFF;
                                BluetoothManagerService.this.mBleApps.clear();
                            }
                            if (st == 15) {
                                try {
                                    BluetoothManagerService.this.mBluetoothLock.readLock().lock();
                                    if (BluetoothManagerService.this.mBluetooth != null) {
                                        BluetoothManagerService.this.mBluetooth.onBrEdrDown();
                                        BluetoothManagerService.this.mEnable = false;
                                        HwLog.d(BluetoothManagerService.TAG, "BleAppNum" + BleAppNum);
                                        if (BleAppNum > 0) {
                                            BluetoothManagerService.this.mEnableExternal = false;
                                        }
                                    }
                                } catch (RemoteException e2) {
                                    Slog.e(BluetoothManagerService.TAG, "Unable to call onBrEdrDown", e2);
                                } finally {
                                    BluetoothManagerService.this.mBluetoothLock.readLock().unlock();
                                }
                            } else if (st == 12) {
                                Slog.d(BluetoothManagerService.TAG, "Calling disable");
                                BluetoothManagerService.this.sendDisableMsg();
                            }
                        } else if (BluetoothManagerService.this.mEnableExternal) {
                            Slog.d(BluetoothManagerService.TAG, "Calling enable");
                            BluetoothManagerService.this.sendEnableMsg(BluetoothManagerService.this.mQuietEnableExternal);
                        }
                    }
                }
            }
        };
        this.mBleApps = new HashMap();
        this.mConnection = new BluetoothServiceConnection();
        this.mHandler = new BluetoothHandler(IoThread.get().getLooper());
        this.mContext = context;
        this.mBluetooth = null;
        this.mBluetoothBinder = null;
        this.mBluetoothGatt = null;
        this.mBinding = false;
        this.mUnbinding = false;
        this.mEnable = false;
        this.mState = 10;
        this.mQuietEnableExternal = false;
        this.mEnableExternal = false;
        this.mAddress = null;
        this.mName = null;
        this.mErrorRecoveryRetryCounter = BLUETOOTH_OFF;
        this.mContentResolver = context.getContentResolver();
        this.mLastMessage = SERVICE_IBLUETOOTHGATT;
        this.mLastEnableMessageTime = SystemClock.elapsedRealtime();
        registerForBleScanModeChange();
        this.mCallbacks = new RemoteCallbackList();
        this.mStateChangeCallbacks = new RemoteCallbackList();
        IntentFilter filter = new IntentFilter("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED");
        registerForAirplaneMode(filter);
        filter.setPriority(ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
        this.mContext.registerReceiver(this.mReceiver, filter);
        loadStoredNameAndAddress();
        if (isBluetoothPersistedStateOn()) {
            this.mEnableExternal = DBG;
        }
        int sysUiUid = -1;
        try {
            sysUiUid = this.mContext.getPackageManager().getPackageUidAsUser("com.android.systemui", DumpState.DUMP_DEXOPT, BLUETOOTH_OFF);
        } catch (NameNotFoundException e) {
            Slog.w(TAG, "Unable to resolve SystemUI's UID.", e);
        }
        this.mSystemUiUid = sysUiUid;
        this.mBluetoothServiceStateCallback = new BluetoothServiceStateCallback();
    }

    private final boolean isAirplaneModeOn() {
        return Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", BLUETOOTH_OFF) == SERVICE_IBLUETOOTH ? DBG : false;
    }

    private final boolean isBluetoothPersistedStateOn() {
        return Global.getInt(this.mContentResolver, "bluetooth_on", BLUETOOTH_OFF) != 0 ? DBG : false;
    }

    private final boolean isBluetoothPersistedStateOnBluetooth() {
        return Global.getInt(this.mContentResolver, "bluetooth_on", BLUETOOTH_OFF) == SERVICE_IBLUETOOTH ? DBG : false;
    }

    private void persistBluetoothSetting(int value) {
        Global.putInt(this.mContext.getContentResolver(), "bluetooth_on", value);
    }

    private boolean isNameAndAddressSet() {
        return (this.mName == null || this.mAddress == null || this.mName.length() <= 0 || this.mAddress.length() <= 0) ? false : DBG;
    }

    private void loadStoredNameAndAddress() {
        Slog.d(TAG, "Loading stored name and address");
        if (this.mContext.getResources().getBoolean(17956953) && Secure.getInt(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDR_VALID, BLUETOOTH_OFF) == 0) {
            Slog.d(TAG, "invalid bluetooth name and address stored");
            return;
        }
        this.mName = Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_NAME);
        this.mAddress = Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS);
        String partAddress = "";
        if (!(this.mAddress == null || this.mAddress.isEmpty())) {
            partAddress = "**:**:**" + this.mAddress.substring(this.mAddress.length() / SERVICE_IBLUETOOTHGATT, this.mAddress.length());
        }
        HwLog.d(TAG, "Stored bluetooth Name=" + this.mName + ",Address=" + partAddress);
    }

    private void storeNameAndAddress(String name, String address) {
        if (name != null) {
            Secure.putString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_NAME, name);
            this.mName = name;
            Slog.d(TAG, "Stored Bluetooth name: " + Secure.getString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_NAME));
        }
        if (address != null) {
            Secure.putString(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS, address);
            this.mAddress = address;
        }
        if (name != null && address != null) {
            Secure.putInt(this.mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDR_VALID, SERVICE_IBLUETOOTH);
        }
    }

    public IBluetooth registerAdapter(IBluetoothManagerCallback callback) {
        if (callback == null) {
            HwLog.w(TAG, "Callback is null in registerAdapter");
            return null;
        }
        Message msg = this.mHandler.obtainMessage(MESSAGE_REGISTER_ADAPTER);
        msg.obj = callback;
        msg.arg1 = Binder.getCallingPid();
        this.mHandler.sendMessage(msg);
        return this.mBluetooth;
    }

    public void unregisterAdapter(IBluetoothManagerCallback callback) {
        if (callback == null) {
            HwLog.w(TAG, "Callback is null in unregisterAdapter");
            return;
        }
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        Message msg = this.mHandler.obtainMessage(MESSAGE_UNREGISTER_ADAPTER);
        msg.obj = callback;
        this.mHandler.sendMessage(msg);
    }

    public void registerStateChangeCallback(IBluetoothStateChangeCallback callback) {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        Message msg = this.mHandler.obtainMessage(MESSAGE_REGISTER_STATE_CHANGE_CALLBACK);
        msg.obj = callback;
        msg.arg1 = Binder.getCallingPid();
        this.mHandler.sendMessage(msg);
    }

    public void unregisterStateChangeCallback(IBluetoothStateChangeCallback callback) {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        Message msg = this.mHandler.obtainMessage(MESSAGE_UNREGISTER_STATE_CHANGE_CALLBACK);
        msg.obj = callback;
        this.mHandler.sendMessage(msg);
    }

    public boolean isEnabled() {
        if (Binder.getCallingUid() == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || checkIfCallerIsForegroundUser()) {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    boolean isEnabled = this.mBluetooth.isEnabled();
                    return isEnabled;
                }
                this.mBluetoothLock.readLock().unlock();
                return false;
            } catch (RemoteException e) {
                Slog.e(TAG, "isEnabled()", e);
            } finally {
                this.mBluetoothLock.readLock().unlock();
            }
        } else {
            HwLog.w(TAG, "isEnabled(): not allowed for non-active and non system user");
            return false;
        }
    }

    public boolean isBleScanAlwaysAvailable() {
        boolean z = false;
        if (isAirplaneModeOn() && !this.mEnable) {
            return false;
        }
        try {
            if (Global.getInt(this.mContentResolver, "ble_scan_always_enabled") != 0) {
                z = DBG;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    private void registerForBleScanModeChange() {
        this.mContentResolver.registerContentObserver(Global.getUriFor("ble_scan_always_enabled"), false, new AnonymousClass3(null));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void disableBleScanMode() {
        try {
            this.mBluetoothLock.writeLock().lock();
            if (!(this.mBluetooth == null || this.mBluetooth.getState() == 12)) {
                HwLog.d(TAG, "Reseting the mEnable flag for clean disable");
                this.mEnable = false;
            }
            this.mBluetoothLock.writeLock().unlock();
        } catch (RemoteException e) {
            HwLog.e(TAG, "getState()", e);
        } catch (Throwable th) {
            this.mBluetoothLock.writeLock().unlock();
        }
    }

    public int updateBleAppCount(IBinder token, boolean enable) {
        if (!enable) {
            ClientDeathRecipient r = (ClientDeathRecipient) this.mBleApps.get(token);
            if (r != null) {
                try {
                    token.unlinkToDeath(r, BLUETOOTH_OFF);
                } catch (NoSuchElementException ex) {
                    HwLog.e(TAG, "updateBleAppCount Unable to unlinkToDeath", ex);
                }
                this.mBleApps.remove(token);
                synchronized (this) {
                    if (mBleAppCount > 0) {
                        mBleAppCount--;
                    }
                }
                HwLog.d(TAG, "Unregistered for death Notification");
            }
        } else if (((ClientDeathRecipient) this.mBleApps.get(token)) == null) {
            ClientDeathRecipient deathRec = new ClientDeathRecipient();
            try {
                token.linkToDeath(deathRec, BLUETOOTH_OFF);
                this.mBleApps.put(token, deathRec);
                synchronized (this) {
                    mBleAppCount += SERVICE_IBLUETOOTH;
                }
                HwLog.d(TAG, "Registered for death Notification");
            } catch (RemoteException e) {
                throw new IllegalArgumentException("Wake lock is already dead.");
            }
        }
        HwLog.d(TAG, "Updated BleAppCount" + mBleAppCount);
        if (mBleAppCount == 0 && this.mEnable) {
            disableBleScanMode();
        }
        return mBleAppCount;
    }

    private void clearBleApps() {
        synchronized (this) {
            this.mBleApps.clear();
            mBleAppCount = BLUETOOTH_OFF;
        }
    }

    public boolean isBleAppPresent() {
        HwLog.d(TAG, "isBleAppPresent() count: " + mBleAppCount);
        if (mBleAppCount > 0) {
            return DBG;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onBluetoothGattServiceUp() {
        HwLog.d(TAG, "BluetoothGatt Service is Up");
        try {
            this.mBluetoothLock.readLock().lock();
            if (!(isBleAppPresent() || this.mBluetooth == null || this.mBluetooth.getState() != 15)) {
                this.mBluetooth.onLeServiceUp();
                long callingIdentity = Binder.clearCallingIdentity();
                persistBluetoothSetting(SERVICE_IBLUETOOTH);
                Binder.restoreCallingIdentity(callingIdentity);
            }
            this.mBluetoothLock.readLock().unlock();
        } catch (RemoteException e) {
            HwLog.e(TAG, "Unable to call onServiceUp", e);
        } catch (Throwable th) {
            this.mBluetoothLock.readLock().unlock();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendBrEdrDownCallback() {
        HwLog.d(TAG, "Calling sendBrEdrDownCallback callbacks");
        if (this.mBluetooth == null) {
            HwLog.w(TAG, "Bluetooth handle is null");
            return;
        }
        if (isBleAppPresent()) {
            try {
                this.mBluetoothGatt.unregAll();
            } catch (RemoteException e) {
                HwLog.e(TAG, "Unable to disconnect all apps.", e);
            }
        } else {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    this.mBluetooth.onBrEdrDown();
                }
                this.mBluetoothLock.readLock().unlock();
            } catch (RemoteException e2) {
                HwLog.e(TAG, "Call to onBrEdrDown() failed.", e2);
            } catch (Throwable th) {
                this.mBluetoothLock.readLock().unlock();
            }
        }
    }

    public boolean isRadioEnabled() {
        boolean z = false;
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        synchronized (this.mConnection) {
            try {
                if (this.mBluetooth != null) {
                    z = this.mBluetooth.isRadioEnabled();
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "isRadioEnabled()", e);
                return false;
            }
        }
        return z;
    }

    public void getNameAndAddress() {
        HwLog.d(TAG, "getNameAndAddress(): mBluetooth = " + this.mBluetooth + " mBinding = " + this.mBinding);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(USER_SWITCHED_TIME_MS));
    }

    public boolean enableNoAutoConnect() {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM, "Need BLUETOOTH ADMIN permission");
        HwLog.d(TAG, "enableNoAutoConnect():  mBluetooth =" + this.mBluetooth + " mBinding = " + this.mBinding);
        if (UserHandle.getAppId(Binder.getCallingUid()) != 1027) {
            throw new SecurityException("no permission to enable Bluetooth quietly");
        }
        synchronized (this.mReceiver) {
            this.mQuietEnableExternal = DBG;
            this.mEnableExternal = DBG;
            sendEnableMsg(DBG);
        }
        return DBG;
    }

    public boolean enable() {
        if (Binder.getCallingUid() == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || checkIfCallerIsForegroundUser()) {
            this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM, "Need BLUETOOTH ADMIN permission");
            if (HwDeviceManager.disallowOp(8)) {
                HwLog.i(TAG, "bluetooth has been restricted.");
                return false;
            }
            HwLog.i(TAG, "BT-Enable-FW enable():  mBluetooth =" + this.mBluetooth + " mBinding = " + this.mBinding + " mState = " + this.mState);
            if (!HwSystemManager.allowOp(this.mContext, 8388608)) {
                return false;
            }
            HwServiceFactory.getHwBluetoothBigDataService().sendBigDataEvent(this.mContext, IHwBluetoothBigDataService.GET_OPEN_BT_APP_NAME);
            synchronized (this.mReceiver) {
                this.mQuietEnableExternal = false;
                this.mEnableExternal = DBG;
                sendEnableMsg(false);
            }
            HwLog.d(TAG, "enable returning");
            return DBG;
        }
        HwLog.w(TAG, "enable(): not allowed for non-active and non system user");
        return false;
    }

    public boolean enableRadio() {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        HwLog.d(TAG, "enable():  mBluetooth =" + (this.mBluetooth == null ? "null" : this.mBluetooth) + " mBinding = " + this.mBinding);
        synchronized (this.mConnection) {
            if (this.mBinding) {
                HwLog.w(TAG, "enable(): binding in progress. Returning..");
                return DBG;
            }
            Message msg = this.mHandler.obtainMessage(MESSAGE_ENABLE_RADIO);
            msg.arg1 = SERVICE_IBLUETOOTH;
            this.mHandler.sendMessage(msg);
            return DBG;
        }
    }

    public boolean disable(boolean persist) {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM, "Need BLUETOOTH ADMIN permissicacheNameAndAddresson");
        if (Binder.getCallingUid() == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || checkIfCallerIsForegroundUser()) {
            HwLog.i(TAG, "BT-Disable-FW disable(): mBluetooth = " + this.mBluetooth + " mBinding = " + this.mBinding);
            synchronized (this.mReceiver) {
                if (persist) {
                    long callingIdentity = Binder.clearCallingIdentity();
                    persistBluetoothSetting(BLUETOOTH_OFF);
                    Binder.restoreCallingIdentity(callingIdentity);
                }
                this.mEnableExternal = false;
                sendDisableMsg();
            }
            return DBG;
        }
        HwLog.w(TAG, "disable(): not allowed for non-active and non system user");
        return false;
    }

    public boolean disableRadio() {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        HwLog.d(TAG, "disable(): mBluetooth = " + (this.mBluetooth == null ? "null" : this.mBluetooth) + " mBinding = " + this.mBinding);
        synchronized (this.mConnection) {
            if (this.mBluetooth == null) {
                return false;
            }
            this.mHandler.sendMessage(this.mHandler.obtainMessage(MESSAGE_DISABLE_RADIO));
            return DBG;
        }
    }

    public void unbindAndFinish() {
        Slog.d(TAG, "unbindAndFinish(): " + this.mBluetooth + " mBinding = " + this.mBinding);
        try {
            this.mBluetoothLock.writeLock().lock();
            if (this.mUnbinding) {
                this.mBluetoothLock.writeLock().unlock();
                return;
            }
            this.mUnbinding = DBG;
            this.mHandler.removeMessages(MESSAGE_BLUETOOTH_STATE_CHANGE);
            if (this.mBluetooth != null) {
                this.mBluetooth.unregisterCallback(this.mBluetoothCallback);
                Slog.d(TAG, "Sending unbind request.");
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
        }
    }

    public IBluetoothGatt getBluetoothGatt() {
        return this.mBluetoothGatt;
    }

    public boolean bindBluetoothProfileService(int bluetoothProfile, IBluetoothProfileServiceConnection proxy) {
        if (this.mEnable) {
            this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
            if (proxy == null) {
                HwLog.w(TAG, "proxy is null.");
                return false;
            }
            synchronized (this.mProfileServices) {
                if (((ProfileServiceConnections) this.mProfileServices.get(new Integer(bluetoothProfile))) == null) {
                    HwLog.d(TAG, "Creating new ProfileServiceConnections object for profile: " + bluetoothProfile);
                    if (bluetoothProfile != SERVICE_IBLUETOOTH) {
                        return false;
                    }
                    ProfileServiceConnections psc = new ProfileServiceConnections(new Intent(IBluetoothHeadset.class.getName()));
                    if (psc.bindService()) {
                        this.mProfileServices.put(new Integer(bluetoothProfile), psc);
                    } else {
                        return false;
                    }
                }
                Message addProxyMsg = this.mHandler.obtainMessage(MESSAGE_ADD_PROXY_DELAYED);
                addProxyMsg.arg1 = bluetoothProfile;
                addProxyMsg.obj = proxy;
                this.mHandler.sendMessageDelayed(addProxyMsg, 100);
                return DBG;
            }
        }
        HwLog.d(TAG, "Trying to bind to profile: " + bluetoothProfile + ", while Bluetooth was disabled");
        return false;
    }

    public void unbindBluetoothProfileService(int bluetoothProfile, IBluetoothProfileServiceConnection proxy) {
        synchronized (this.mProfileServices) {
            ProfileServiceConnections psc = (ProfileServiceConnections) this.mProfileServices.get(new Integer(bluetoothProfile));
            if (psc == null) {
                return;
            }
            psc.removeProxy(proxy);
        }
    }

    private void unbindAllBluetoothProfileServices() {
        synchronized (this.mProfileServices) {
            for (Integer i : this.mProfileServices.keySet()) {
                ProfileServiceConnections psc = (ProfileServiceConnections) this.mProfileServices.get(i);
                try {
                    this.mContext.unbindService(psc);
                } catch (IllegalArgumentException e) {
                    HwLog.e(TAG, "Unable to unbind service with intent: " + psc.mIntent, e);
                }
                psc.removeAllProxies();
            }
            this.mProfileServices.clear();
        }
    }

    public void handleOnBootPhase() {
        HwLog.d(TAG, "Bluetooth boot completed");
        if (this.mEnableExternal && isBluetoothPersistedStateOnBluetooth()) {
            HwLog.d(TAG, "Auto-enabling Bluetooth.");
            sendEnableMsg(this.mQuietEnableExternal);
        } else if (!isNameAndAddressSet()) {
            Slog.d(TAG, "Getting adapter name and address");
            this.mHandler.sendMessage(this.mHandler.obtainMessage(USER_SWITCHED_TIME_MS));
        }
    }

    public void handleOnSwitchUser(int userHandle) {
        Slog.d(TAG, "User " + userHandle + " switched");
        this.mHandler.obtainMessage(MESSAGE_USER_SWITCHED, userHandle, BLUETOOTH_OFF).sendToTarget();
    }

    public void handleOnUnlockUser(int userHandle) {
        Slog.d(TAG, "User " + userHandle + " unlocked");
        this.mHandler.obtainMessage(MESSAGE_USER_UNLOCKED, userHandle, BLUETOOTH_OFF).sendToTarget();
    }

    private void sendBluetoothStateCallback(boolean isUp) {
        this.mBluetoothServiceStateCallback.sendBluetoothStateCallback(isUp);
    }

    private void sendBluetoothServiceUpCallback() {
        HwLog.d(TAG, "Calling onBluetoothServiceUp callbacks");
        this.mBluetoothServiceStateCallback.sendBluetoothServiceUpCallback();
    }

    private void sendBluetoothServiceDownCallback() {
        HwLog.d(TAG, "Calling onBluetoothServiceDown callbacks");
        this.mBluetoothServiceStateCallback.sendBluetoothServiceDownCallback();
    }

    public String getAddress() {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (Binder.getCallingUid() != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE && !checkIfCallerIsForegroundUser()) {
            HwLog.w(TAG, "getAddress(): not allowed for non-active and non system user");
            return null;
        } else if (this.mContext.checkCallingOrSelfPermission("android.permission.LOCAL_MAC_ADDRESS") != 0) {
            return "02:00:00:00:00:00";
        } else {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    String address = this.mBluetooth.getAddress();
                    return address;
                }
                this.mBluetoothLock.readLock().unlock();
                return this.mAddress;
            } catch (RemoteException e) {
                HwLog.e(TAG, "getAddress(): Unable to retrieve address remotely. Returning cached address", e);
            } finally {
                this.mBluetoothLock.readLock().unlock();
            }
        }
    }

    public String getName() {
        this.mContext.enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
        if (Binder.getCallingUid() == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || checkIfCallerIsForegroundUser()) {
            try {
                this.mBluetoothLock.readLock().lock();
                if (this.mBluetooth != null) {
                    String name = this.mBluetooth.getName();
                    return name;
                }
                this.mBluetoothLock.readLock().unlock();
                return this.mName;
            } catch (RemoteException e) {
                HwLog.e(TAG, "getName(): Unable to retrieve name remotely. Returning cached name", e);
            } finally {
                this.mBluetoothLock.readLock().unlock();
            }
        } else {
            HwLog.w(TAG, "getName(): not allowed for non-active and non system user");
            return null;
        }
    }

    boolean skipAndDelayDisable() {
        for (int i = BLUETOOTH_OFF; i < 10; i += SERVICE_IBLUETOOTH) {
            synchronized (this.mReceiver) {
                try {
                    this.mBluetoothLock.readLock().lock();
                    if (this.mBluetooth != null) {
                        int state = this.mBluetooth.getState();
                        if (state == 15) {
                            if (this.mLastMessage != SERVICE_IBLUETOOTHGATT || this.mEnable) {
                                this.mBluetoothLock.readLock().unlock();
                                return false;
                            }
                            sendDisableMsg();
                            this.mBluetoothLock.readLock().unlock();
                            return DBG;
                        } else if (state == 12) {
                            this.mBluetoothLock.readLock().unlock();
                            return false;
                        }
                    }
                    this.mBluetoothLock.readLock().unlock();
                } catch (RemoteException e) {
                    Slog.e(TAG, "getState()", e);
                    this.mBluetoothLock.readLock().unlock();
                } catch (Throwable th) {
                    this.mBluetoothLock.readLock().unlock();
                }
                SystemClock.sleep(300);
            }
        }
        return false;
    }

    private void handleEnable(boolean quietMode) {
        this.mQuietEnable = quietMode;
        try {
            this.mBluetoothLock.writeLock().lock();
            if (this.mBluetooth == null && !this.mBinding) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MESSAGE_TIMEOUT_BIND), 3000);
                if (doBind(new Intent(IBluetooth.class.getName()), this.mConnection, 65, UserHandle.CURRENT)) {
                    this.mBinding = DBG;
                } else {
                    this.mHandler.removeMessages(MESSAGE_TIMEOUT_BIND);
                }
            } else if (this.mBluetooth != null) {
                if (!this.mQuietEnable) {
                    HwLog.i(TAG, "BT-Enable-FW handleEnable");
                    if (!this.mBluetooth.enable()) {
                        HwLog.e(TAG, "IBluetooth.enable() returned false");
                    }
                } else if (!this.mBluetooth.enableNoAutoConnect()) {
                    HwLog.e(TAG, "IBluetooth.enableNoAutoConnect() returned false");
                }
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "Unable to call enable()", e);
        } catch (Throwable th) {
            this.mBluetoothLock.writeLock().unlock();
        }
        this.mBluetoothLock.writeLock().unlock();
    }

    boolean doBind(Intent intent, ServiceConnection conn, int flags, UserHandle user) {
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), BLUETOOTH_OFF);
        intent.setComponent(comp);
        if (comp != null && this.mContext.bindServiceAsUser(intent, conn, flags, user)) {
            return DBG;
        }
        HwLog.e(TAG, "Fail to bind to: " + intent);
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleDisable() {
        try {
            this.mBluetoothLock.readLock().lock();
            if (this.mBluetooth != null) {
                Slog.d(TAG, "Sending off request.");
                if (!this.mBluetooth.disable()) {
                    Slog.e(TAG, "IBluetooth.disable() returned false");
                }
            }
            this.mBluetoothLock.readLock().unlock();
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to call disable()", e);
        } catch (Throwable th) {
            this.mBluetoothLock.readLock().unlock();
        }
    }

    private boolean checkIfCallerIsForegroundUser() {
        int callingUser = UserHandle.getCallingUserId();
        int callingUid = Binder.getCallingUid();
        long callingIdentity = Binder.clearCallingIdentity();
        UserInfo ui = ((UserManager) this.mContext.getSystemService("user")).getProfileParent(callingUser);
        int parentUser = ui != null ? ui.id : -10000;
        int callingAppId = UserHandle.getAppId(callingUid);
        boolean z = false;
        try {
            int foregroundUser = ActivityManager.getCurrentUser();
            z = (callingUser == foregroundUser || parentUser == foregroundUser || callingAppId == 1027) ? DBG : callingAppId == this.mSystemUiUid ? DBG : false;
            HwLog.d(TAG, "checkIfCallerIsForegroundUser: valid=" + z + " callingUser=" + callingUser + " parentUser=" + parentUser + " foregroundUser=" + foregroundUser);
            return z;
        } finally {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    private void sendBleStateChanged(int prevState, int newState) {
        HwLog.d(TAG, "BLE State Change Intent: " + prevState + " -> " + newState);
        Intent intent = new Intent("android.bluetooth.adapter.action.BLE_STATE_CHANGED");
        intent.putExtra("android.bluetooth.adapter.extra.PREVIOUS_STATE", prevState);
        intent.putExtra("android.bluetooth.adapter.extra.STATE", newState);
        intent.addFlags(67108864);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BLUETOOTH_PERM);
    }

    private void bluetoothStateChangeHandler(int prevState, int newState) {
        boolean isStandardBroadcast = DBG;
        HwLog.i(TAG, "bluetoothStateChangeHandler isStandardBroadcast=" + DBG + ", prevState=" + prevState + ", newState=" + newState);
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
            if (newState == 15 || newState == 10) {
                boolean intermediate_off = prevState == 13 ? newState == 15 ? DBG : false : false;
                if (newState == 10) {
                    HwLog.d(TAG, "Bluetooth is complete turn off");
                    sendBluetoothStateCallback(false);
                    if (!isRadioEnabled()) {
                        sendBluetoothServiceDownCallback();
                        unbindAndFinish();
                    }
                    sendBleStateChanged(prevState, newState);
                    isStandardBroadcast = false;
                } else if (!intermediate_off) {
                    HwLog.d(TAG, "Bluetooth is in LE only mode");
                    if (this.mBluetoothGatt != null) {
                        HwLog.d(TAG, "Calling BluetoothGattServiceUp");
                        onBluetoothGattServiceUp();
                    } else {
                        HwLog.d(TAG, "Binding Bluetooth GATT service");
                        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
                            doBind(new Intent(IBluetoothGatt.class.getName()), this.mConnection, 65, UserHandle.CURRENT);
                        }
                    }
                    sendBleStateChanged(prevState, newState);
                    isStandardBroadcast = false;
                } else if (intermediate_off) {
                    HwLog.d(TAG, "Intermediate off, back to LE only mode");
                    sendBleStateChanged(prevState, newState);
                    newState = 10;
                    sendBrEdrDownCallback();
                }
            } else if (newState == 12) {
                sendBluetoothStateCallback(newState == 12 ? DBG : false);
                sendBleStateChanged(prevState, newState);
            } else if (newState == 14 || newState == 16) {
                sendBleStateChanged(prevState, newState);
                isStandardBroadcast = false;
            } else if (newState == 11 || newState == 13) {
                sendBleStateChanged(prevState, newState);
            }
            HwLog.i(TAG, "isStandardBroadcast=" + isStandardBroadcast + ", prevState=" + prevState + ", newState=" + newState);
            if (newState == 11 && prevState == 15) {
                this.mEnable = DBG;
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
                    intent.addFlags(67108864);
                    intent.addFlags(268435456);
                    this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BLUETOOTH_PERM);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean waitForOnOff(boolean on, boolean off) {
        for (int i = BLUETOOTH_OFF; i < 10; i += SERVICE_IBLUETOOTH) {
            this.mBluetoothLock.readLock().lock();
            if (this.mBluetooth == null) {
                this.mBluetoothLock.readLock().unlock();
                break;
            }
            if (on) {
                if (this.mBluetooth.getState() == 12) {
                    this.mBluetoothLock.readLock().unlock();
                    return DBG;
                }
            } else if (!off) {
                try {
                    if (this.mBluetooth.getState() != 12) {
                        this.mBluetoothLock.readLock().unlock();
                        return DBG;
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "getState()", e);
                } catch (Throwable th) {
                    this.mBluetoothLock.readLock().unlock();
                }
            } else if (this.mBluetooth.getState() == 10) {
                this.mBluetoothLock.readLock().unlock();
                return DBG;
            }
            this.mBluetoothLock.readLock().unlock();
            if (on || off) {
                SystemClock.sleep(300);
            } else {
                SystemClock.sleep(50);
            }
        }
        HwLog.e(TAG, "waitForOnOff time out");
        return false;
    }

    private boolean waitForMonitoredOnOff(boolean on, boolean off) {
        for (int i = BLUETOOTH_OFF; i < 10; i += SERVICE_IBLUETOOTH) {
            synchronized (this.mConnection) {
                if (this.mBluetooth == null) {
                    break;
                }
                if (on) {
                    if (this.mBluetooth.getState() == 12) {
                        return DBG;
                    } else if (this.mBluetooth.getState() == 15) {
                        bluetoothStateChangeHandler(14, 15);
                    }
                } else if (off) {
                    if (this.mBluetooth.getState() == 10) {
                        return DBG;
                    }
                    try {
                        if (this.mBluetooth.getState() == 15) {
                            bluetoothStateChangeHandler(13, 15);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "getState()", e);
                    }
                } else if (this.mBluetooth.getState() != 12) {
                    return DBG;
                }
                if (on || off) {
                    SystemClock.sleep(800);
                } else {
                    SystemClock.sleep(50);
                }
            }
        }
        Log.e(TAG, "waitForOnOff time out");
        return false;
    }

    private void sendDisableMsg() {
        this.mLastMessage = SERVICE_IBLUETOOTHGATT;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(SERVICE_IBLUETOOTHGATT));
    }

    private void sendEnableMsg(boolean quietMode) {
        long now = SystemClock.elapsedRealtime();
        if (now - this.mLastEnableMessageTime < 1500 && this.mLastMessage == SERVICE_IBLUETOOTH && this.mLastQuietMode == quietMode) {
            HwLog.d(TAG, "MESSAGE_ENABLE message repeat in short time, return");
            this.mLastEnableMessageTime = now;
            return;
        }
        this.mLastEnableMessageTime = now;
        this.mLastMessage = SERVICE_IBLUETOOTH;
        this.mLastQuietMode = this.mQuietEnable;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(SERVICE_IBLUETOOTH, quietMode ? SERVICE_IBLUETOOTH : BLUETOOTH_OFF, BLUETOOTH_OFF));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void recoverBluetoothServiceFromError() {
        HwLog.e(TAG, "recoverBluetoothServiceFromError");
        try {
            this.mBluetoothLock.readLock().lock();
            if (this.mBluetooth != null) {
                this.mBluetooth.unregisterCallback(this.mBluetoothCallback);
            }
            this.mBluetoothLock.readLock().unlock();
        } catch (RemoteException re) {
            Slog.e(TAG, "Unable to unregister", re);
        } catch (Throwable th) {
        }
        SystemClock.sleep(500);
        handleDisable();
        waitForOnOff(false, DBG);
        sendBluetoothServiceDownCallback();
        try {
            this.mBluetoothLock.writeLock().lock();
            if (this.mBluetooth != null) {
                this.mBluetooth = null;
                this.mContext.unbindService(this.mConnection);
            }
            this.mBluetoothGatt = null;
            this.mHandler.removeMessages(MESSAGE_BLUETOOTH_STATE_CHANGE);
            this.mState = 10;
            this.mEnable = false;
            int i = this.mErrorRecoveryRetryCounter;
            this.mErrorRecoveryRetryCounter = i + SERVICE_IBLUETOOTH;
            if (i < MAX_ERROR_RESTART_RETRIES) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MESSAGE_RESTART_BLUETOOTH_SERVICE), 3000);
            }
        } finally {
            this.mBluetoothLock.writeLock().unlock();
        }
    }

    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.DUMP", TAG);
        String errorMsg = null;
        if (this.mBluetoothBinder == null) {
            errorMsg = "Bluetooth Service not connected";
        } else {
            try {
                this.mBluetoothBinder.dump(fd, args);
            } catch (RemoteException e) {
                errorMsg = "RemoteException while calling Bluetooth Service";
            }
        }
        if (errorMsg != null && (args.length <= 0 || !args[BLUETOOTH_OFF].startsWith("--proto"))) {
            writer.println(errorMsg);
        }
    }

    private void handleEnableRadio() {
        synchronized (this.mConnection) {
            HwLog.i(TAG, "handleEnableRadio mBluetooth = " + this.mBluetooth);
            if (this.mBluetooth == null) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MESSAGE_TIMEOUT_BIND), 3000);
                this.mConnection.setGetNameAddressOnly(false);
                this.mConnection.setTurnOnRadio(DBG);
                Intent i = new Intent(IBluetooth.class.getName());
                i.setComponent(i.resolveSystemService(this.mContext.getPackageManager(), BLUETOOTH_OFF));
                if (i.getComponent() == null && i.getPackage() == null) {
                    HwLog.e(TAG, "Illegal Argument ! Fail to open radio !");
                    return;
                } else if (!this.mContext.bindService(i, this.mConnection, SERVICE_IBLUETOOTH)) {
                    this.mHandler.removeMessages(MESSAGE_TIMEOUT_BIND);
                    HwLog.e(TAG, "Fail to bind to: " + IBluetooth.class.getName());
                }
            } else {
                try {
                    HwLog.d(TAG, "Getting and storing Bluetooth name and address prior to enable.");
                    storeNameAndAddress(this.mBluetooth.getName(), this.mBluetooth.getAddress());
                } catch (RemoteException e) {
                    Log.e(TAG, "", e);
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
    }

    private void handleDisableRadio() {
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
}
