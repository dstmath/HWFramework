package ohos.bluetooth;

import java.util.ArrayList;
import java.util.List;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public final class HandsFreeAudioGateway implements ProfileBase {
    public static final String EVENT_AUDIO_STATE_UPDATE = "usual.event.bluetooth.handsfree.ag.AUDIO_STATE_UPDATE";
    public static final int SCO_STATE_CONNECTED = 2;
    public static final int SCO_STATE_CONNECTING = 1;
    public static final int SCO_STATE_DISCONNECTED = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogHelper.BT_DOMAIN_ID, "HandsFreeAudioGateway");
    private static HandsFreeAudioGateway sHandsFreeAudioGateway;
    private BluetoothHost mBluetoothHost;
    private HandsFreeAgProxy mHandsFreeAgProxy;

    HandsFreeAudioGateway(Context context) {
        this.mHandsFreeAgProxy = null;
        this.mHandsFreeAgProxy = new HandsFreeAgProxy();
        this.mBluetoothHost = BluetoothHost.getDefaultHost(context);
    }

    public static synchronized HandsFreeAudioGateway getProfile(Context context) {
        HandsFreeAudioGateway handsFreeAudioGateway;
        synchronized (HandsFreeAudioGateway.class) {
            if (sHandsFreeAudioGateway == null) {
                sHandsFreeAudioGateway = new HandsFreeAudioGateway(context);
            }
            handsFreeAudioGateway = sHandsFreeAudioGateway;
        }
        return handsFreeAudioGateway;
    }

    @Override // ohos.bluetooth.ProfileBase
    public List<BluetoothRemoteDevice> getDevicesByStates(int[] iArr) {
        HiLog.info(TAG, "getDevicesByStates", new Object[0]);
        if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeAgProxy.getDevicesByStates(iArr);
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
            return this.mHandsFreeAgProxy.getDeviceState(bluetoothRemoteDevice);
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
            return this.mHandsFreeAgProxy.getScoState(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "getScoState when state not on", new Object[0]);
            return 0;
        }
    }

    public boolean connect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "connect", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "connect got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeAgProxy.connect(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "connect when state not on", new Object[0]);
            return false;
        }
    }

    public boolean disconnect(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "disconnect", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "disconnect got null device", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeAgProxy.disconnect(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "disconnect when state not on", new Object[0]);
            return false;
        }
    }

    public boolean openVoiceRecognition(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "openVoiceRecognition", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "got null device in openVoiceRecognition", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeAgProxy.openVoiceRecognition(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "call openVoiceRecognition when state not on", new Object[0]);
            return false;
        }
    }

    public boolean closeVoiceRecognition(BluetoothRemoteDevice bluetoothRemoteDevice) {
        HiLog.info(TAG, "closeVoiceRecognition", new Object[0]);
        if (bluetoothRemoteDevice == null) {
            HiLog.info(TAG, "got null device in closeVoiceRecognition", new Object[0]);
            return false;
        } else if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeAgProxy.closeVoiceRecognition(bluetoothRemoteDevice);
        } else {
            HiLog.info(TAG, "call closeVoiceRecognition when state not on", new Object[0]);
            return false;
        }
    }

    public boolean connectSco() {
        HiLog.info(TAG, "connectSco", new Object[0]);
        if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeAgProxy.connectSco();
        }
        HiLog.info(TAG, "connectSco when state not on", new Object[0]);
        return false;
    }

    public boolean disconnectSco() {
        HiLog.info(TAG, "disconnectSco", new Object[0]);
        if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeAgProxy.disconnectSco();
        }
        HiLog.info(TAG, "disconnectSco when state not on", new Object[0]);
        return false;
    }

    public List<BluetoothRemoteDevice> getConnectedDevices() {
        HiLog.info(TAG, "getConnectedDevices", new Object[0]);
        if (this.mBluetoothHost.getBtState() == 2) {
            return this.mHandsFreeAgProxy.getConnectedDevices();
        }
        HiLog.info(TAG, "getConnectedDevices when state not on", new Object[0]);
        return new ArrayList();
    }
}
