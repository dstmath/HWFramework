package com.huawei.server.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceNotification;
import android.os.Bundle;
import android.os.Handler;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UEventObserver;
import android.os.UserHandle;
import com.android.server.HwLog;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import vendor.huawei.hardware.tp.V1_0.ITouchscreen;

public final class HwBluetoothPencilManager {
    private static final String ACTION_HWKEYBOARD_BATTERY_LEVEL_CHANGED = "com.huawei.bluetooth.action.HWKEYBOARD_BATTRY_LEVEL_CHANGED";
    private static final String ACTION_HWKEYBOARD_CONNECTION_STATE_CHANGED = "com.huawei.bluetooth.action.HWKEYBOARD_CONNECTION_STATE_CHANGED";
    private static final String ACTION_HWPENCIL_BATTERY_LEVEL_CHANGED = "com.huawei.bluetooth.action.HWPENCIL_BATTRY_LEVEL_CHANGED";
    private static final String ACTION_HWPENCIL_CONNECTION_STATE_CHANGED = "com.huawei.bluetooth.action.HWPENCIL_CONNECTION_STATE_CHANGED";
    private static final String ACTION_PENCIL_STATE_CHANGED = "com.huawei.nearby.peripheral.action.STATE_CHANGED";
    private static final int BATTERY_LEVEL_MAX = 99;
    private static final int BATTERY_LEVEL_MIN = 40;
    private static final String BLUETOOTH_PERIPHERAL_PERM = "com.huawei.bluetooth.permission.HUAWEI_BLUETOOTH_PERIPHERAL_PRIVILEGED";
    private static final String BLUETOOTH_PERM = "android.permission.BLUETOOTH";
    private static final int BYTE_LENGTH_OF_MODELID = 4;
    private static final String CHARGE_PATH = "sys/class/hw_power/charger/wireless_tx/tx_open";
    private static final int COIL_ATTACH_STATE = 1;
    private static final int COIL_PING_SUCC_STATE = 2;
    private static final int COIL_REMOVE_STATE = 0;
    private static final byte EXTRA_ADDRESS_INDEX = 4;
    private static final byte EXTRA_ADDRESS_LENGTH = 6;
    private static final String EXTRA_BATTERY_LEVEL = "com.huawei.bluetooth.extra.BATTERY_LEVEL";
    private static final String EXTRA_CONNECTION_STATE = "com.huawei.bluetooth.extra.CONNECTION_STATE";
    private static final String EXTRA_DESCRIPTION_TAG = "hwnearby";
    private static final byte EXTRA_MODEL_ID_INDEX = 1;
    private static final byte EXTRA_MODEL_ID_LENGTH = 3;
    private static final short EXTRA_NEARBY_VERSION = 256;
    private static final byte EXTRA_SUB_MODEL_ID_INDEX = 2;
    private static final byte EXTRA_SUB_MODEL_ID_LENGTH = 1;
    private static final short EXTRA_SUB_TYPE_KEYBOARD = 3;
    private static final short EXTRA_SUB_TYPE_PENCIL = 2;
    private static final short EXTRA_TOTAL_LENGTH = 32;
    private static final short EXTRA_TYPE_KEYBOARD = 3;
    private static final short EXTRA_TYPE_PENCIL = 2;
    private static final int FLAG_PENCIL_CONNECTION_STATE = 5;
    private static final String HW_DEVICE_MAC = "DEVICEMAC";
    private static final String HW_DEVICE_MODEL_ID = "DEVICEMODELID";
    private static final String HW_DEVICE_NEARBY_VERSION = "DEVICEVERSION";
    private static final String HW_DEVICE_NO = "DEVICENO";
    private static final String HW_DEVICE_NO_KEYBOARD = "2";
    static final String HW_DEVICE_NO_PENCIL = "1";
    private static final String HW_DEVICE_STATE = "DEVICESTATE";
    static final String HW_DEVICE_STATE_CONNECTED = "CONNECTED";
    static final String HW_DEVICE_STATE_DISCONNECTED = "DISCONNECTED";
    static final String HW_DEVICE_STATE_PING_SUCC = "PING_SUCC";
    private static final String HW_DEVICE_SUB_MODEL_ID = "DEVICESUBMODELID";
    private static final String HW_PENCIL_STATE_INFO = "DEVPATH=/devices/virtual/hw_accessory/monitor";
    private static final int KEYBOARD_START_CHARGING = 3;
    private static final int KEYBOARD_STOP_CHARGING = 2;
    private static final int MESSAGE_HWKEYBOARD_BATTRY_LEVEL_CHANGED = 1;
    private static final int MESSAGE_HWPENCIL_BATTERY_LEVEL_CHANGED = 2;
    private static final int MESSAGE_HWPENCIL_CONNECTION_STATE_CHANGED = 0;
    private static final String MSG_BUNDLE_TAG = "message.bundle";
    private static final String MSG_CONTENT_TAG = "message.content";
    private static final String MSG_PENCIL_REQUEST = "magnetic.request";
    private static final String MSG_SOURCE_TAG = "message.source";
    private static final int PENCIL_BATTERY_LEVEL_INVALID = -1;
    private static final int PENCIL_BATTERY_LEVEL_MIN = 0;
    private static final int PENCIL_BATTERY_LEVEL_THRESHOLD = 90;
    private static final String PENCIL_CHARGE_PATH = "sys/class/hw_power/charger/wireless_aux_tx/tx_open";
    private static final String PENCIL_CONNECTTED_FOR_TP_HAL = "on";
    private static final String PENCIL_DISCONNECTTED_FOR_TP_HAL = "off";
    private static final int PENCIL_STATE_CONNECTED = 2;
    private static final int PENCIL_STATE_DISCONNECTED = 0;
    private static final String PERIPHERAL_STATE = "peripheral.state";
    private static final int START_CHARGING = 1;
    private static final int STOP_CHARGING = 0;
    private static final String TAG = "HwBluetoothPencilManager";
    private static final int TP_HAL_DEATH_COOKIE = 1010;
    private final Context mContext;
    private Handler mHandler;
    private HwUsbManager mHwUsbManager;
    private boolean mIsMarx;
    private final UEventObserver mPencilObserver = new UEventObserver() {
        /* class com.huawei.server.bluetooth.HwBluetoothPencilManager.AnonymousClass1 */

        public void onUEvent(UEventObserver.UEvent event) {
            if (event == null) {
                HwLog.e(HwBluetoothPencilManager.TAG, "onUEvent: event is null");
                return;
            }
            HwLog.d(HwBluetoothPencilManager.TAG, "onUEvent: get event: " + event.get(HwBluetoothPencilManager.HW_DEVICE_NO));
            if ("1".equals(event.get(HwBluetoothPencilManager.HW_DEVICE_NO)) || "2".equals(event.get(HwBluetoothPencilManager.HW_DEVICE_NO))) {
                HwBluetoothPencilManager.this.sendHwPencilBroadcast(event.get(HwBluetoothPencilManager.HW_DEVICE_STATE), event.get(HwBluetoothPencilManager.HW_DEVICE_MAC), event.get(HwBluetoothPencilManager.HW_DEVICE_NEARBY_VERSION), event.get(HwBluetoothPencilManager.HW_DEVICE_MODEL_ID), event.get(HwBluetoothPencilManager.HW_DEVICE_SUB_MODEL_ID), event.get(HwBluetoothPencilManager.HW_DEVICE_NO));
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.huawei.server.bluetooth.HwBluetoothPencilManager.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwLog.e(HwBluetoothPencilManager.TAG, "BroadcastReceiver: intent is null");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                HwLog.e(HwBluetoothPencilManager.TAG, "BroadcastReceiver: action is null");
                return;
            }
            char c = 65535;
            switch (action.hashCode()) {
                case -2114103349:
                    if (action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
                        c = 3;
                        break;
                    }
                    break;
                case -1689371670:
                    if (action.equals(HwBluetoothPencilManager.ACTION_HWPENCIL_CONNECTION_STATE_CHANGED)) {
                        c = 0;
                        break;
                    }
                    break;
                case -1608292967:
                    if (action.equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1142402821:
                    if (action.equals(HwBluetoothPencilManager.ACTION_HWPENCIL_BATTERY_LEVEL_CHANGED)) {
                        c = 2;
                        break;
                    }
                    break;
                case 1523249067:
                    if (action.equals(HwBluetoothPencilManager.ACTION_HWKEYBOARD_BATTERY_LEVEL_CHANGED)) {
                        c = 1;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                HwLog.i(HwBluetoothPencilManager.TAG, "BroadcastReceiver: pencil connection state changed");
                HwBluetoothPencilManager.this.mHandler.obtainMessage(0, intent).sendToTarget();
            } else if (c == 1) {
                HwLog.i(HwBluetoothPencilManager.TAG, "BroadcastReceiver: keyboard battery level changed");
                HwBluetoothPencilManager.this.mHandler.obtainMessage(1, intent).sendToTarget();
            } else if (c == 2) {
                HwLog.i(HwBluetoothPencilManager.TAG, "BroadcastReceiver: pencil battery level changed");
                HwBluetoothPencilManager.this.mHandler.obtainMessage(2, intent).sendToTarget();
            } else if (c == 3 || c == 4) {
                HwLog.i(HwBluetoothPencilManager.TAG, "usb state changed!");
                HwBluetoothPencilManager.this.mHwUsbManager.getUsbStateFromBroadcast(intent);
            } else {
                HwLog.e(HwBluetoothPencilManager.TAG, "BroadcastReceiver: receive unknow broadcast:" + action);
            }
        }
    };
    private final ServiceNotification mServiceNotification = new ServiceNotification();
    private ITouchscreen mTpProxy = null;
    private final Object mTpProxyLock = new Object();

    public void writeDevice(String path, String data) {
        StringBuilder sb;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            fos.write(data.getBytes());
            HwLog.i(TAG, "enter writedevice: " + data);
            try {
                fos.close();
                return;
            } catch (IOException e) {
                sb = new StringBuilder();
            }
            sb.append("close writedevice");
            sb.append(path);
            HwLog.e(TAG, sb.toString());
        } catch (IOException e2) {
            HwLog.e(TAG, "can't enter writedevice" + path);
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e3) {
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e4) {
                    HwLog.e(TAG, "close writedevice" + path);
                }
            }
            throw th;
        }
    }

    public void keyboardCharge(Intent intent) {
        int batteryLevel = intent.getIntExtra(EXTRA_BATTERY_LEVEL, 0);
        HwLog.d(TAG, "BatteryLevel: " + batteryLevel);
        if (batteryLevel <= 40) {
            writeDevice(CHARGE_PATH, String.valueOf(3));
        } else if (batteryLevel > 40 && batteryLevel <= 99) {
            writeDevice(CHARGE_PATH, String.valueOf(2));
        } else if (batteryLevel != 100 || this.mIsMarx) {
            HwLog.i(TAG, "KeyboardCharge: ignore this level");
        } else {
            writeDevice(CHARGE_PATH, String.valueOf(2));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPencilBatteryChanged(Intent intent) {
        int batteryLevel = intent.getIntExtra(EXTRA_BATTERY_LEVEL, -1);
        HwLog.i(TAG, "onPencilBatteryChanged: BatteryLevel " + batteryLevel);
        if (batteryLevel <= PENCIL_BATTERY_LEVEL_THRESHOLD && batteryLevel >= 0) {
            writeDevice(PENCIL_CHARGE_PATH, String.valueOf(1));
        }
    }

    /* access modifiers changed from: private */
    public final class DeathRecipient implements IHwBinder.DeathRecipient {
        private DeathRecipient() {
        }

        public void serviceDied(long cookie) {
            if (cookie == 1010) {
                HwLog.d(HwBluetoothPencilManager.TAG, "tp hal service died cookie");
                synchronized (HwBluetoothPencilManager.this.mTpProxyLock) {
                    HwBluetoothPencilManager.this.mTpProxy = null;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ServiceNotification extends IServiceNotification.Stub {
        private ServiceNotification() {
        }

        public void onRegistration(String fqName, String name, boolean preexisting) {
            HwLog.d(HwBluetoothPencilManager.TAG, "tp hal service started " + fqName + " " + name);
            HwBluetoothPencilManager.this.connectToProxy();
        }
    }

    private class BluetoothPencilHandler extends Handler {
        BluetoothPencilHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                HwBluetoothPencilManager.this.onPencilConnectionStateChanged(((Intent) msg.obj).getIntExtra(HwBluetoothPencilManager.EXTRA_CONNECTION_STATE, 0));
            } else if (i == 1) {
                HwBluetoothPencilManager.this.keyboardCharge((Intent) msg.obj);
            } else if (i != 2) {
                HwLog.e(HwBluetoothPencilManager.TAG, "BluetoothPencilHandler: unknow message:" + msg.what);
            } else {
                HwBluetoothPencilManager.this.onPencilBatteryChanged((Intent) msg.obj);
            }
        }
    }

    public HwBluetoothPencilManager(Context context, Looper looper) {
        this.mContext = context;
        this.mPencilObserver.startObserving(HW_PENCIL_STATE_INFO);
        getTouchService();
        this.mHandler = new BluetoothPencilHandler(looper);
        this.mHwUsbManager = new HwUsbManager(context, this);
        IntentFilter filter = new IntentFilter();
        IntentFilter filterPencil = new IntentFilter();
        filterPencil.addAction(ACTION_HWPENCIL_CONNECTION_STATE_CHANGED);
        filterPencil.addAction(ACTION_HWPENCIL_BATTERY_LEVEL_CHANGED);
        filter.addAction(ACTION_HWKEYBOARD_BATTERY_LEVEL_CHANGED);
        filter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        this.mContext.registerReceiver(this.mReceiver, filter, BLUETOOTH_PERM, null);
        this.mContext.registerReceiver(this.mReceiver, filterPencil, BLUETOOTH_PERIPHERAL_PERM, null);
        this.mIsMarx = isMarx();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPencilConnectionStateChanged(int state) {
        HwLog.d(TAG, "pencil connection state changed:" + state);
        setPencilConnectionStatusToHal(state);
    }

    /* access modifiers changed from: package-private */
    public void sendHwPencilBroadcast(String state, String macAddr, String nearbyVersion, String modelId, String subModelId, String deviceNum) {
        int attachState;
        if (nearbyVersion == null || modelId == null || subModelId == null || !HwUtils.isValidMac(macAddr)) {
            HwLog.e(TAG, "sendHwPencilBroadcast: invalid args");
            return;
        }
        HwLog.d(TAG, "sendHwPencilBroadcast: sendBroadcast nearbyVersion:" + nearbyVersion + ", state:" + state);
        byte[] extraArray = getNearbyExtraData(nearbyVersion, macAddr, modelId, subModelId, deviceNum);
        if (extraArray == null) {
            HwLog.e(TAG, "sendHwPencilBroadcast: get extra data failed");
            return;
        }
        if (Objects.equals(state, HW_DEVICE_STATE_CONNECTED)) {
            attachState = 1;
        } else if (Objects.equals(state, HW_DEVICE_STATE_PING_SUCC)) {
            attachState = 2;
        } else {
            attachState = 0;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(PERIPHERAL_STATE, attachState);
        bundle.putString(MSG_SOURCE_TAG, MSG_PENCIL_REQUEST);
        bundle.putByteArray(MSG_CONTENT_TAG, extraArray);
        Intent intent = new Intent(ACTION_PENCIL_STATE_CHANGED);
        intent.putExtra(MSG_BUNDLE_TAG, bundle);
        try {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, BLUETOOTH_PERM);
        } catch (IllegalStateException e) {
            HwLog.e(TAG, "send broad in illegal state");
        }
    }

    private short GetType(String deviceNum) {
        if ("1".equals(deviceNum)) {
            HwLog.i(TAG, "GetType: EXTRA_TYPE_PENCIL");
            return 2;
        } else if ("2".equals(deviceNum)) {
            HwLog.i(TAG, "GetType: EXTRA_TYPE_KEYBOARD");
            return 3;
        } else {
            HwLog.i(TAG, "GetType: ERROR");
            return 0;
        }
    }

    private short GetSubType(String deviceNum) {
        if ("1".equals(deviceNum)) {
            HwLog.i(TAG, "GetSubType: EXTRA_SUB_TYPE_PENCIL");
            return 2;
        } else if ("2".equals(deviceNum)) {
            HwLog.i(TAG, "GetSubType: EXTRA_SUB_TYPE_KEYBOARD");
            return 3;
        } else {
            HwLog.i(TAG, "GetSubType: ERROR");
            return 0;
        }
    }

    private byte[] getNearbyExtraData(String nearbyVersion, String macAddr, String modelId, String subModelId, String deviceNum) {
        HwLog.i(TAG, "getNearbyExtraData: nearby version:" + nearbyVersion + ", model id:" + modelId + ", sub model id:" + subModelId);
        try {
            short bufferModelId = Short.parseShort(modelId.substring(0, 4), 16);
            byte extBufferModelId = Byte.parseByte(modelId.substring(4), 16);
            byte bufferSubModelId = Byte.parseByte(subModelId, 16);
            byte bufferVersion = Byte.parseByte(nearbyVersion, 16);
            short DeviceType = GetType(deviceNum);
            short DeviceSubType = GetSubType(deviceNum);
            ByteBuffer byteBuffer = ByteBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.put(EXTRA_DESCRIPTION_TAG.getBytes(StandardCharsets.UTF_8));
            byteBuffer.putShort(32);
            byteBuffer.putShort(DeviceType);
            byteBuffer.put(bufferVersion);
            byteBuffer.put(extBufferModelId);
            byteBuffer.putShort(DeviceSubType);
            byteBuffer.put((byte) 1);
            byteBuffer.put((byte) 3);
            byteBuffer.put((byte) 0);
            byteBuffer.putShort(bufferModelId);
            byteBuffer.put((byte) 2);
            byteBuffer.put((byte) 1);
            byteBuffer.put(bufferSubModelId);
            byteBuffer.put((byte) 4);
            byteBuffer.put((byte) 6);
            byte[] byteAddress = HwUtils.getBytesFromAddress(macAddr);
            if (byteAddress == null) {
                return null;
            }
            byteBuffer.put(byteAddress);
            HwLog.e(TAG, "getNearbyExtraData: " + Arrays.toString(byteBuffer.array()));
            return byteBuffer.array();
        } catch (NumberFormatException e) {
            HwLog.e(TAG, "getNearbyExtraData: string parse failed");
            return null;
        }
    }

    private void getTouchService() {
        try {
            if (!IServiceManager.getService().registerForNotifications(ITouchscreen.kInterfaceName, "", this.mServiceNotification)) {
                HwLog.e(TAG, "Failed to register service start notification");
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "Failed to register service start notification with exception");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connectToProxy() {
        synchronized (this.mTpProxyLock) {
            if (this.mTpProxy != null) {
                HwLog.i(TAG, "mTpProxy has registered, do not register again");
                return;
            }
            try {
                this.mTpProxy = ITouchscreen.getService();
                if (this.mTpProxy != null) {
                    HwLog.d(TAG, "connectToProxy: mTpProxy get success.");
                    this.mTpProxy.linkToDeath(new DeathRecipient(), 1010);
                } else {
                    HwLog.d(TAG, "connectToProxy: mTpProxy get failed.");
                }
            } catch (NoSuchElementException e) {
                HwLog.e(TAG, "connectToProxy: tp hal service not found.");
            } catch (RemoteException e2) {
                HwLog.e(TAG, "connectToProxy: tp hal service not responding");
            }
        }
    }

    private void setPencilConnectionStatusToHal(int state) {
        String connState;
        synchronized (this.mTpProxyLock) {
            if (this.mTpProxy == null) {
                HwLog.e(TAG, "mTpProxy is null, return");
                return;
            }
            if (state == 2) {
                connState = PENCIL_CONNECTTED_FOR_TP_HAL;
            } else if (state == 0) {
                connState = PENCIL_DISCONNECTTED_FOR_TP_HAL;
            } else {
                HwLog.d(TAG, "ignore connection state:" + state);
                return;
            }
            HwLog.i(TAG, "setPencilConnectionStatusToHal: " + connState);
            try {
                if (this.mTpProxy.hwSetFeatureConfig(5, connState) == 0) {
                    HwLog.d(TAG, "setPencilConnectionStatusToHal success");
                } else {
                    HwLog.d(TAG, "setPencilConnectionStatusToHal error");
                }
            } catch (RemoteException e) {
                HwLog.e(TAG, "Failed to set connection state to hal");
            }
        }
    }

    private boolean isMarx() {
        String localId = SystemPropertiesEx.get("ro.product.model");
        HwLog.i(TAG, "isMarx: " + localId);
        String[] buff = localId.split(AwarenessInnerConstants.DASH_KEY);
        return buff.length > 1 && Objects.equals("MRX", buff[0]);
    }
}
