package android.bluetooth;

import android.bluetooth.BluetoothProfile.ServiceListener;
import android.bluetooth.IBluetoothStateChangeCallback.Stub;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ProxyInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public final class BluetoothHeadsetClient implements BluetoothProfile {
    public static final String ACTION_AG_EVENT = "android.bluetooth.headsetclient.profile.action.AG_EVENT";
    public static final String ACTION_AUDIO_STATE_CHANGED = "android.bluetooth.headsetclient.profile.action.AUDIO_STATE_CHANGED";
    public static final String ACTION_CALL_CHANGED = "android.bluetooth.headsetclient.profile.action.AG_CALL_CHANGED";
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED";
    public static final String ACTION_LAST_VTAG = "android.bluetooth.headsetclient.profile.action.LAST_VTAG";
    public static final String ACTION_RESULT = "android.bluetooth.headsetclient.profile.action.RESULT";
    public static final int ACTION_RESULT_ERROR = 1;
    public static final int ACTION_RESULT_ERROR_BLACKLISTED = 6;
    public static final int ACTION_RESULT_ERROR_BUSY = 3;
    public static final int ACTION_RESULT_ERROR_CME = 7;
    public static final int ACTION_RESULT_ERROR_DELAYED = 5;
    public static final int ACTION_RESULT_ERROR_NO_ANSWER = 4;
    public static final int ACTION_RESULT_ERROR_NO_CARRIER = 2;
    public static final int ACTION_RESULT_OK = 0;
    public static final int CALL_ACCEPT_HOLD = 1;
    public static final int CALL_ACCEPT_NONE = 0;
    public static final int CALL_ACCEPT_TERMINATE = 2;
    public static final int CME_CORPORATE_PERSONALIZATION_PIN_REQUIRED = 46;
    public static final int CME_CORPORATE_PERSONALIZATION_PUK_REQUIRED = 47;
    public static final int CME_DIAL_STRING_TOO_LONG = 26;
    public static final int CME_EAP_NOT_SUPPORTED = 49;
    public static final int CME_EMERGENCY_SERVICE_ONLY = 32;
    public static final int CME_HIDDEN_KEY_REQUIRED = 48;
    public static final int CME_INCORRECT_PARAMETERS = 50;
    public static final int CME_INCORRECT_PASSWORD = 16;
    public static final int CME_INVALID_CHARACTER_IN_DIAL_STRING = 27;
    public static final int CME_INVALID_CHARACTER_IN_TEXT_STRING = 25;
    public static final int CME_INVALID_INDEX = 21;
    public static final int CME_MEMORY_FAILURE = 23;
    public static final int CME_MEMORY_FULL = 20;
    public static final int CME_NETWORK_PERSONALIZATION_PIN_REQUIRED = 40;
    public static final int CME_NETWORK_PERSONALIZATION_PUK_REQUIRED = 41;
    public static final int CME_NETWORK_SUBSET_PERSONALIZATION_PIN_REQUIRED = 42;
    public static final int CME_NETWORK_SUBSET_PERSONALIZATION_PUK_REQUIRED = 43;
    public static final int CME_NETWORK_TIMEOUT = 31;
    public static final int CME_NOT_FOUND = 22;
    public static final int CME_NOT_SUPPORTED_FOR_VOIP = 34;
    public static final int CME_NO_CONNECTION_TO_PHONE = 1;
    public static final int CME_NO_NETWORK_SERVICE = 30;
    public static final int CME_NO_SIMULTANOUS_VOIP_CS_CALLS = 33;
    public static final int CME_OPERATION_NOT_ALLOWED = 3;
    public static final int CME_OPERATION_NOT_SUPPORTED = 4;
    public static final int CME_PHFSIM_PIN_REQUIRED = 6;
    public static final int CME_PHFSIM_PUK_REQUIRED = 7;
    public static final int CME_PHONE_FAILURE = 0;
    public static final int CME_PHSIM_PIN_REQUIRED = 5;
    public static final int CME_SERVICE_PROVIDER_PERSONALIZATION_PIN_REQUIRED = 44;
    public static final int CME_SERVICE_PROVIDER_PERSONALIZATION_PUK_REQUIRED = 45;
    public static final int CME_SIM_BUSY = 14;
    public static final int CME_SIM_FAILURE = 13;
    public static final int CME_SIM_NOT_INSERTED = 10;
    public static final int CME_SIM_PIN2_REQUIRED = 17;
    public static final int CME_SIM_PIN_REQUIRED = 11;
    public static final int CME_SIM_PUK2_REQUIRED = 18;
    public static final int CME_SIM_PUK_REQUIRED = 12;
    public static final int CME_SIM_WRONG = 15;
    public static final int CME_SIP_RESPONSE_CODE = 35;
    public static final int CME_TEXT_STRING_TOO_LONG = 24;
    private static final boolean DBG = true;
    public static final String EXTRA_AG_FEATURE_3WAY_CALLING = "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_3WAY_CALLING";
    public static final String EXTRA_AG_FEATURE_ACCEPT_HELD_OR_WAITING_CALL = "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_ACCEPT_HELD_OR_WAITING_CALL";
    public static final String EXTRA_AG_FEATURE_ATTACH_NUMBER_TO_VT = "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_ATTACH_NUMBER_TO_VT";
    public static final String EXTRA_AG_FEATURE_ECC = "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_ECC";
    public static final String EXTRA_AG_FEATURE_MERGE = "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_MERGE";
    public static final String EXTRA_AG_FEATURE_MERGE_AND_DETACH = "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_MERGE_AND_DETACH";
    public static final String EXTRA_AG_FEATURE_REJECT_CALL = "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_REJECT_CALL";
    public static final String EXTRA_AG_FEATURE_RELEASE_AND_ACCEPT = "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_RELEASE_AND_ACCEPT";
    public static final String EXTRA_AG_FEATURE_RELEASE_HELD_OR_WAITING_CALL = "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_RELEASE_HELD_OR_WAITING_CALL";
    public static final String EXTRA_AG_FEATURE_RESPONSE_AND_HOLD = "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_RESPONSE_AND_HOLD";
    public static final String EXTRA_AG_FEATURE_VOICE_RECOGNITION = "android.bluetooth.headsetclient.extra.EXTRA_AG_FEATURE_VOICE_RECOGNITION";
    public static final String EXTRA_AUDIO_WBS = "android.bluetooth.headsetclient.extra.AUDIO_WBS";
    public static final String EXTRA_BATTERY_LEVEL = "android.bluetooth.headsetclient.extra.BATTERY_LEVEL";
    public static final String EXTRA_CALL = "android.bluetooth.headsetclient.extra.CALL";
    public static final String EXTRA_CME_CODE = "android.bluetooth.headsetclient.extra.CME_CODE";
    public static final String EXTRA_IN_BAND_RING = "android.bluetooth.headsetclient.extra.IN_BAND_RING";
    public static final String EXTRA_NETWORK_ROAMING = "android.bluetooth.headsetclient.extra.NETWORK_ROAMING";
    public static final String EXTRA_NETWORK_SIGNAL_STRENGTH = "android.bluetooth.headsetclient.extra.NETWORK_SIGNAL_STRENGTH";
    public static final String EXTRA_NETWORK_STATUS = "android.bluetooth.headsetclient.extra.NETWORK_STATUS";
    public static final String EXTRA_NUMBER = "android.bluetooth.headsetclient.extra.NUMBER";
    public static final String EXTRA_OPERATOR_NAME = "android.bluetooth.headsetclient.extra.OPERATOR_NAME";
    public static final String EXTRA_RESULT_CODE = "android.bluetooth.headsetclient.extra.RESULT_CODE";
    public static final String EXTRA_SUBSCRIBER_INFO = "android.bluetooth.headsetclient.extra.SUBSCRIBER_INFO";
    public static final String EXTRA_VOICE_RECOGNITION = "android.bluetooth.headsetclient.extra.VOICE_RECOGNITION";
    public static final int STATE_AUDIO_CONNECTED = 2;
    public static final int STATE_AUDIO_CONNECTING = 1;
    public static final int STATE_AUDIO_DISCONNECTED = 0;
    private static final String TAG = "BluetoothHeadsetClient";
    private static final boolean VDBG = false;
    private BluetoothAdapter mAdapter;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback;
    private ServiceConnection mConnection;
    private Context mContext;
    private IBluetoothHeadsetClient mService;
    private ServiceListener mServiceListener;

    BluetoothHeadsetClient(Context context, ServiceListener l) {
        this.mBluetoothStateChangeCallback = new Stub() {
            public void onBluetoothStateChange(boolean up) {
                Log.d(BluetoothHeadsetClient.TAG, "onBluetoothStateChange: up=" + up);
                ServiceConnection -get0;
                if (up) {
                    -get0 = BluetoothHeadsetClient.this.mConnection;
                    synchronized (-get0) {
                        try {
                        } catch (Exception re) {
                            Log.e(BluetoothHeadsetClient.TAG, ProxyInfo.LOCAL_EXCL_LIST, re);
                        }
                    }
                    if (BluetoothHeadsetClient.this.mService == null) {
                        Intent intent = new Intent(IBluetoothHeadsetClient.class.getName());
                        BluetoothHeadsetClient.this.doBind();
                    }
                } else {
                    -get0 = BluetoothHeadsetClient.this.mConnection;
                    synchronized (-get0) {
                        try {
                        } catch (Exception re2) {
                            Log.e(BluetoothHeadsetClient.TAG, ProxyInfo.LOCAL_EXCL_LIST, re2);
                        }
                    }
                    BluetoothHeadsetClient.this.mService = null;
                    BluetoothHeadsetClient.this.mContext.unbindService(BluetoothHeadsetClient.this.mConnection);
                }
            }
        };
        this.mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.d(BluetoothHeadsetClient.TAG, "Proxy object connected");
                BluetoothHeadsetClient.this.mService = IBluetoothHeadsetClient.Stub.asInterface(service);
                if (BluetoothHeadsetClient.this.mServiceListener != null) {
                    BluetoothHeadsetClient.this.mServiceListener.onServiceConnected(BluetoothHeadsetClient.CME_INCORRECT_PASSWORD, BluetoothHeadsetClient.this);
                }
            }

            public void onServiceDisconnected(ComponentName className) {
                Log.d(BluetoothHeadsetClient.TAG, "Proxy object disconnected");
                BluetoothHeadsetClient.this.mService = null;
                if (BluetoothHeadsetClient.this.mServiceListener != null) {
                    BluetoothHeadsetClient.this.mServiceListener.onServiceDisconnected(BluetoothHeadsetClient.CME_INCORRECT_PASSWORD);
                }
            }
        };
        this.mContext = context;
        this.mServiceListener = l;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.registerStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (RemoteException e) {
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            }
        }
        doBind();
    }

    boolean doBind() {
        Intent intent = new Intent(IBluetoothHeadsetClient.class.getName());
        ComponentName comp = intent.resolveSystemService(this.mContext.getPackageManager(), STATE_AUDIO_DISCONNECTED);
        intent.setComponent(comp);
        if (comp != null && this.mContext.bindServiceAsUser(intent, this.mConnection, STATE_AUDIO_DISCONNECTED, Process.myUserHandle())) {
            return DBG;
        }
        Log.e(TAG, "Could not bind to Bluetooth Headset Client Service with " + intent);
        return false;
    }

    void close() {
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.unregisterStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (Exception e) {
                Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, e);
            }
        }
        synchronized (this.mConnection) {
            if (this.mService != null) {
                try {
                    this.mService = null;
                    this.mContext.unbindService(this.mConnection);
                } catch (Exception re) {
                    Log.e(TAG, ProxyInfo.LOCAL_EXCL_LIST, re);
                }
            }
        }
        this.mServiceListener = null;
    }

    public boolean connect(BluetoothDevice device) {
        log("connect(" + device + ")");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.connect(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return false;
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean disconnect(BluetoothDevice device) {
        log("disconnect(" + device + ")");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.disconnect(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return false;
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        if (this.mService == null || !isEnabled()) {
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return this.mService.getConnectedDevices();
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        if (this.mService == null || !isEnabled()) {
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return this.mService.getDevicesMatchingConnectionStates(states);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    public int getConnectionState(BluetoothDevice device) {
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.getConnectionState(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return STATE_AUDIO_DISCONNECTED;
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return STATE_AUDIO_DISCONNECTED;
    }

    public boolean setPriority(BluetoothDevice device, int priority) {
        log("setPriority(" + device + ", " + priority + ")");
        if (this.mService == null || !isEnabled() || !isValidDevice(device)) {
            if (this.mService == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        } else if (priority != 0 && priority != 100) {
            return false;
        } else {
            try {
                return this.mService.setPriority(device, priority);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return false;
            }
        }
    }

    public int getPriority(BluetoothDevice device) {
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.getPriority(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return STATE_AUDIO_DISCONNECTED;
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return STATE_AUDIO_DISCONNECTED;
    }

    public boolean startVoiceRecognition(BluetoothDevice device) {
        log("startVoiceRecognition()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.startVoiceRecognition(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean stopVoiceRecognition(BluetoothDevice device) {
        log("stopVoiceRecognition()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.stopVoiceRecognition(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public List<BluetoothHeadsetClientCall> getCurrentCalls(BluetoothDevice device) {
        log("getCurrentCalls()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.getCurrentCalls(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return null;
    }

    public Bundle getCurrentAgEvents(BluetoothDevice device) {
        log("getCurrentCalls()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.getCurrentAgEvents(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return null;
    }

    public boolean acceptCall(BluetoothDevice device, int flag) {
        log("acceptCall()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.acceptCall(device, flag);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean holdCall(BluetoothDevice device) {
        log("holdCall()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.holdCall(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean rejectCall(BluetoothDevice device) {
        log("rejectCall()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.rejectCall(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean terminateCall(BluetoothDevice device, int index) {
        log("terminateCall()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.terminateCall(device, index);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean enterPrivateMode(BluetoothDevice device, int index) {
        log("enterPrivateMode()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.enterPrivateMode(device, index);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean explicitCallTransfer(BluetoothDevice device) {
        log("explicitCallTransfer()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.explicitCallTransfer(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean redial(BluetoothDevice device) {
        log("redial()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.redial(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean dial(BluetoothDevice device, String number) {
        log("dial()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.dial(device, number);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean dialMemory(BluetoothDevice device, int location) {
        log("dialMemory()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.dialMemory(device, location);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean sendDTMF(BluetoothDevice device, byte code) {
        log("sendDTMF()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.sendDTMF(device, code);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean getLastVoiceTagNumber(BluetoothDevice device) {
        log("getLastVoiceTagNumber()");
        if (this.mService != null && isEnabled() && isValidDevice(device)) {
            try {
                return this.mService.getLastVoiceTagNumber(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (this.mService == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean acceptIncomingConnect(BluetoothDevice device) {
        log("acceptIncomingConnect");
        if (this.mService == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                return this.mService.acceptIncomingConnect(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return false;
    }

    public boolean rejectIncomingConnect(BluetoothDevice device) {
        log("rejectIncomingConnect");
        if (this.mService != null) {
            try {
                return this.mService.rejectIncomingConnect(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public int getAudioState(BluetoothDevice device) {
        if (this.mService == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                return this.mService.getAudioState(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return STATE_AUDIO_DISCONNECTED;
    }

    public void setAudioRouteAllowed(boolean allowed) {
        if (this.mService == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
            return;
        }
        try {
            this.mService.setAudioRouteAllowed(allowed);
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }

    public boolean getAudioRouteAllowed() {
        if (this.mService == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                return this.mService.getAudioRouteAllowed();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return false;
    }

    public boolean connectAudio() {
        if (this.mService == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                return this.mService.connectAudio();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return false;
    }

    public boolean disconnectAudio() {
        if (this.mService == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                return this.mService.disconnectAudio();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return false;
    }

    public Bundle getCurrentAgFeatures(BluetoothDevice device) {
        if (this.mService == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                return this.mService.getCurrentAgFeatures(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return null;
    }

    private boolean isEnabled() {
        if (this.mAdapter.getState() == CME_SIM_PUK_REQUIRED) {
            return DBG;
        }
        return false;
    }

    private boolean isValidDevice(BluetoothDevice device) {
        if (device != null && BluetoothAdapter.checkBluetoothAddress(device.getAddress())) {
            return DBG;
        }
        return false;
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
