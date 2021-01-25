package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public final class HandsFreeUnit implements ProfileBase {
    public static final String EVENT_AUDIO_STATE_UPDATE = "usual.event.bluetooth.handsfreeunit.AUDIO_STATE_UPDATE";
    public static final String EVENT_CALL_STATE_UPDATE = "usual.event.bluetooth.handsfreeunit.AG_CALL_STATE_UPDATE";
    public static final String EVENT_CONNECTION_STATE_UPDATE = "usual.event.bluetooth.handsfreeunit.CONNECT_STATE_UPDATE";
    public static final int FLAG_ACCEPT_CALL_HOLD = 1;
    public static final int FLAG_ACCEPT_CALL_NONE = 0;
    public static final int FLAG_ACCEPT_CALL_TERMINATE = 2;
    public static final int SCO_STATE_CONNECTED = 2;
    public static final int SCO_STATE_CONNECTING = 1;
    public static final int SCO_STATE_DISCONNECTED = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "HandsFreeUnit");
    private static HandsFreeUnit sHandsFreeUnit;
    private BluetoothHost mBluetoothHost;
    private HandsFreeHfProxy mHandsFreeHfProxy;

    HandsFreeUnit(Context context) {
        this.mHandsFreeHfProxy = null;
        this.mHandsFreeHfProxy = new HandsFreeHfProxy();
        this.mBluetoothHost = BluetoothHost.getDefaultHost(context);
    }

    public static synchronized HandsFreeUnit getProfile(Context context) {
        HandsFreeUnit handsFreeUnit;
        synchronized (HandsFreeUnit.class) {
            if (sHandsFreeUnit == null) {
                sHandsFreeUnit = new HandsFreeUnit(context);
            }
            handsFreeUnit = sHandsFreeUnit;
        }
        return handsFreeUnit;
    }

    @Override // ohos.bluetooth.ProfileBase
    public List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr) {
        HiLog.info(TAG, "getDevicesByStates", new Object[0]);
        if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeHfProxy.getDevicesByStates(iArr);
        }
        HiLog.info(TAG, "getDevicesByStates when state not on", new Object[0]);
        return new ArrayList();
    }

    @Override // ohos.bluetooth.ProfileBase
    public int getDeviceState(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "getDeviceState", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "getDeviceState got null device", new Object[0]);
            return 0;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeHfProxy.getDeviceState(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "getDeviceState when state not on", new Object[0]);
            return 0;
        }
    }

    public int getScoState(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "getScoState", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "getScoState got null device", new Object[0]);
            return 0;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeHfProxy.getScoState(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "getScoState when state not on", new Object[0]);
            return 0;
        }
    }

    public Optional<HandsFreeUnitCall> startDial(BluetoothRemoteDevice bluetoothRemoteDevice, String str) {
        HiLog.info(TAG, "startDial", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "startDial got null device", new Object[0]);
            return Optional.empty();
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeHfProxy.startDial(bluetoothRemoteDevice, str);
        } else {
            HiLog.info(TAG, "startDial when state not on", new Object[0]);
            return Optional.empty();
        }
    }

    public boolean acceptIncomingCall(BluetoothRemoteDevice bluetoothRemoteDevice, int i) {
        HiLog.info(TAG, "acceptIncomingCall", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "acceptIncomingCall got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeHfProxy.acceptIncomingCall(bluetoothRemoteDevice, i);
        } else {
            HiLog.info(TAG, "acceptIncomingCall when state not on", new Object[0]);
            return false;
        }
    }

    public boolean holdActiveCall(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "holdActiveCall", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "holdActiveCall got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeHfProxy.holdActiveCall(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "holdActiveCall when state not on", new Object[0]);
            return false;
        }
    }

    public boolean rejectIncomingCall(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "rejectIncomingCall", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "rejectIncomingCall got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeHfProxy.rejectIncomingCall(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "rejectIncomingCall when state not on", new Object[0]);
            return false;
        }
    }

    public boolean finishActiveCall(BluetoothRemoteDevice bluetoothRemoteDevice, HandsFreeUnitCall handsFreeUnitCall) {
        HiLog.info(TAG, "finishActiveCall", new Object[0]);
        if (bluetoothRemoteDevice == null || handsFreeUnitCall == null) {
            HiLog.info(TAG, "finishActiveCall got null device or call", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeHfProxy.finishActiveCall(bluetoothRemoteDevice, handsFreeUnitCall);
        } else {
            HiLog.info(TAG, "finishActiveCall when state not on", new Object[0]);
            return false;
        }
    }

    public boolean sendDTMFTone(BluetoothRemoteDevice bluetoothRemoteDevice, byte b) {
        HiLog.info(TAG, "sendDTMFTone", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "sendDTMFTone got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeHfProxy.sendDTMFTone(bluetoothRemoteDevice, b);
        } else {
            HiLog.info(TAG, "sendDTMFTone when state not on", new Object[0]);
            return false;
        }
    }

    public boolean connectSco(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "connectSco", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "connectSco got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeHfProxy.connectSco(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "connectSco when state not on", new Object[0]);
            return false;
        }
    }

    public boolean disconnectSco(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "disconnectSco", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "disconnectSco got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeHfProxy.disconnectSco(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "disconnectSco when state not on", new Object[0]);
            return false;
        }
    }

    public boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "hf connect", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "hf connect got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeHfProxy.connect(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "hf connect when state not on", new Object[0]);
            return false;
        }
    }

    public boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "hf disconnect", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "hf disconnect got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeHfProxy.disconnect(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "hf disconnect when state not on", new Object[0]);
            return false;
        }
    }
}
