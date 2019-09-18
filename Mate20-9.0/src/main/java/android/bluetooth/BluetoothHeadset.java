package android.bluetooth;

import android.annotation.SystemApi;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.IBluetoothHeadset;
import android.bluetooth.IBluetoothProfileServiceConnection;
import android.bluetooth.IBluetoothStateChangeCallback;
import android.content.ComponentName;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.List;

public final class BluetoothHeadset implements BluetoothProfile {
    public static final String ACTION_ACTIVE_DEVICE_CHANGED = "android.bluetooth.headset.profile.action.ACTIVE_DEVICE_CHANGED";
    public static final String ACTION_AUDIO_STATE_CHANGED = "android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED";
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED";
    public static final String ACTION_HF_INDICATORS_VALUE_CHANGED = "android.bluetooth.headset.action.HF_INDICATORS_VALUE_CHANGED";
    public static final String ACTION_VENDOR_SPECIFIC_HEADSET_EVENT = "android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT";
    public static final int AT_CMD_TYPE_ACTION = 4;
    public static final int AT_CMD_TYPE_BASIC = 3;
    public static final int AT_CMD_TYPE_READ = 0;
    public static final int AT_CMD_TYPE_SET = 2;
    public static final int AT_CMD_TYPE_TEST = 1;
    private static final boolean DBG = true;
    public static final String EXTRA_HF_INDICATORS_IND_ID = "android.bluetooth.headset.extra.HF_INDICATORS_IND_ID";
    public static final String EXTRA_HF_INDICATORS_IND_VALUE = "android.bluetooth.headset.extra.HF_INDICATORS_IND_VALUE";
    public static final String EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS = "android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_ARGS";
    public static final String EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD = "android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_CMD";
    public static final String EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE = "android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_CMD_TYPE";
    private static final int MESSAGE_HEADSET_SERVICE_CONNECTED = 100;
    private static final int MESSAGE_HEADSET_SERVICE_DISCONNECTED = 101;
    public static final int STATE_AUDIO_CONNECTED = 12;
    public static final int STATE_AUDIO_CONNECTING = 11;
    public static final int STATE_AUDIO_DISCONNECTED = 10;
    private static final String TAG = "BluetoothHeadset";
    private static final boolean VDBG = false;
    public static final String VENDOR_RESULT_CODE_COMMAND_ANDROID = "+ANDROID";
    public static final String VENDOR_SPECIFIC_HEADSET_EVENT_COMPANY_ID_CATEGORY = "android.bluetooth.headset.intent.category.companyid";
    public static final String VENDOR_SPECIFIC_HEADSET_EVENT_IPHONEACCEV = "+IPHONEACCEV";
    public static final int VENDOR_SPECIFIC_HEADSET_EVENT_IPHONEACCEV_BATTERY_LEVEL = 1;
    public static final String VENDOR_SPECIFIC_HEADSET_EVENT_XAPL = "+XAPL";
    public static final String VENDOR_SPECIFIC_HEADSET_EVENT_XEVENT = "+XEVENT";
    public static final String VENDOR_SPECIFIC_HEADSET_EVENT_XEVENT_BATTERY_LEVEL = "BATTERY";
    private BluetoothAdapter mAdapter;
    private final IBluetoothStateChangeCallback mBluetoothStateChangeCallback = new IBluetoothStateChangeCallback.Stub() {
        /* JADX WARNING: No exception handlers in catch block: Catch:{  } */
        public void onBluetoothStateChange(boolean up) {
            Log.d(BluetoothHeadset.TAG, "onBluetoothStateChange: up=" + up);
            if (!up) {
                BluetoothHeadset.this.doUnbind();
                return;
            }
            synchronized (BluetoothHeadset.this.mConnection) {
                try {
                    if (BluetoothHeadset.this.mService == null) {
                        BluetoothHeadset.this.doBind();
                    }
                } catch (Exception re) {
                    Log.e(BluetoothHeadset.TAG, "", re);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final IBluetoothProfileServiceConnection mConnection = new IBluetoothProfileServiceConnection.Stub() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(BluetoothHeadset.TAG, "Proxy object connected");
            IBluetoothHeadset unused = BluetoothHeadset.this.mService = IBluetoothHeadset.Stub.asInterface(Binder.allowBlocking(service));
            BluetoothHeadset.this.mHandler.sendMessage(BluetoothHeadset.this.mHandler.obtainMessage(100));
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(BluetoothHeadset.TAG, "Proxy object disconnected");
            IBluetoothHeadset unused = BluetoothHeadset.this.mService = null;
            BluetoothHeadset.this.mHandler.sendMessage(BluetoothHeadset.this.mHandler.obtainMessage(101));
        }
    };
    private Context mContext;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    if (BluetoothHeadset.this.mServiceListener != null) {
                        BluetoothHeadset.this.mServiceListener.onServiceConnected(1, BluetoothHeadset.this);
                        return;
                    }
                    return;
                case 101:
                    if (BluetoothHeadset.this.mServiceListener != null) {
                        BluetoothHeadset.this.mServiceListener.onServiceDisconnected(1);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public volatile IBluetoothHeadset mService;
    /* access modifiers changed from: private */
    public BluetoothProfile.ServiceListener mServiceListener;

    BluetoothHeadset(Context context, BluetoothProfile.ServiceListener l) {
        this.mContext = context;
        this.mServiceListener = l;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.registerStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (RemoteException e) {
                Log.e(TAG, "", e);
            }
        }
        doBind();
    }

    /* access modifiers changed from: package-private */
    public boolean doBind() {
        try {
            return this.mAdapter.getBluetoothManager().bindBluetoothProfileService(1, this.mConnection);
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to bind HeadsetService", e);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void doUnbind() {
        synchronized (this.mConnection) {
            try {
                this.mAdapter.getBluetoothManager().unbindBluetoothProfileService(1, this.mConnection);
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to unbind HeadsetService", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void close() {
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.unregisterStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }
        this.mServiceListener = null;
        doUnbind();
    }

    @SystemApi
    public boolean connect(BluetoothDevice device) {
        log("connect(" + device + ")");
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        try {
            return service.connect(device);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    @SystemApi
    public boolean disconnect(BluetoothDevice device) {
        log("disconnect(" + device + ")");
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        try {
            return service.disconnect(device);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return false;
        }
    }

    public List<BluetoothDevice> getConnectedDevices() {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return service.getConnectedDevices();
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        } catch (NullPointerException e1) {
            Log.e(TAG, "mService is null", e1);
            return new ArrayList();
        }
    }

    public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] states) {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return new ArrayList();
        }
        try {
            return service.getDevicesMatchingConnectionStates(states);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return new ArrayList();
        }
    }

    public int getConnectionState(BluetoothDevice device) {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return 0;
        }
        try {
            return service.getConnectionState(device);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return 0;
        }
    }

    @SystemApi
    public boolean setPriority(BluetoothDevice device, int priority) {
        log("setPriority(" + device + ", " + priority + ")");
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        } else if (priority != 0 && priority != 100) {
            return false;
        } else {
            try {
                return service.setPriority(device, priority);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
                return false;
            }
        }
    }

    public int getPriority(BluetoothDevice device) {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled() || !isValidDevice(device)) {
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return 0;
        }
        try {
            return service.getPriority(device);
        } catch (RemoteException e) {
            Log.e(TAG, Log.getStackTraceString(new Throwable()));
            return 0;
        }
    }

    public boolean startVoiceRecognition(BluetoothDevice device) {
        log("startVoiceRecognition()");
        IBluetoothHeadset service = this.mService;
        if (service != null && isEnabled() && isValidDevice(device)) {
            try {
                return service.startVoiceRecognition(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (service == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean stopVoiceRecognition(BluetoothDevice device) {
        log("stopVoiceRecognition()");
        IBluetoothHeadset service = this.mService;
        if (service != null && isEnabled() && isValidDevice(device)) {
            try {
                return service.stopVoiceRecognition(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (service == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean isAudioConnected(BluetoothDevice device) {
        IBluetoothHeadset service = this.mService;
        if (service != null && isEnabled() && isValidDevice(device)) {
            try {
                return service.isAudioConnected(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (service == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public static boolean isBluetoothVoiceDialingEnabled(Context context) {
        return context.getResources().getBoolean(R.bool.config_bluetooth_sco_off_call);
    }

    public int getAudioState(BluetoothDevice device) {
        IBluetoothHeadset service = this.mService;
        if (service == null || isDisabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                return service.getAudioState(device);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return 10;
    }

    public void setAudioRouteAllowed(boolean allowed) {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
            return;
        }
        try {
            service.setAudioRouteAllowed(allowed);
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }

    public boolean getAudioRouteAllowed() {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                return service.getAudioRouteAllowed();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return false;
    }

    public void setForceScoAudio(boolean forced) {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
            return;
        }
        try {
            service.setForceScoAudio(forced);
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }

    public boolean isAudioOn() {
        IBluetoothHeadset service = this.mService;
        if (service != null && isEnabled()) {
            try {
                return service.isAudioOn();
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (service == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public boolean connectAudio() {
        IBluetoothHeadset service = this.mService;
        infolog("connectAudio");
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                return service.connectAudio();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return false;
    }

    public boolean connectAudio(String address) {
        infolog("connectAudio with address");
        if (this.mService == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                return this.mService.connectAudioWithAddress(address);
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return false;
    }

    public boolean disconnectAudio() {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                infolog("disconnectAudio");
                return service.disconnectAudio();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return false;
    }

    public boolean startScoUsingVirtualVoiceCall() {
        log("startScoUsingVirtualVoiceCall()");
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                return service.startScoUsingVirtualVoiceCall();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return false;
    }

    public boolean stopScoUsingVirtualVoiceCall() {
        log("stopScoUsingVirtualVoiceCall()");
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                return service.stopScoUsingVirtualVoiceCall();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return false;
    }

    public void phoneStateChanged(int numActive, int numHeld, int callState, String number, int type) {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
            return;
        }
        try {
            infolog("phoneStateChanged");
            service.phoneStateChanged(numActive, numHeld, callState, number, type);
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }

    public void clccResponse(int index, int direction, int status, int mode, boolean mpty, String number, int type) {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
            return;
        }
        try {
            infolog("clccResponse");
            service.clccResponse(index, direction, status, mode, mpty, number, type);
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }

    public boolean sendVendorSpecificResultCode(BluetoothDevice device, String command, String arg) {
        log("sendVendorSpecificResultCode()");
        if (command != null) {
            IBluetoothHeadset service = this.mService;
            if (service != null && isEnabled() && isValidDevice(device)) {
                try {
                    return service.sendVendorSpecificResultCode(device, command, arg);
                } catch (RemoteException e) {
                    Log.e(TAG, Log.getStackTraceString(new Throwable()));
                }
            }
            if (service == null) {
                Log.w(TAG, "Proxy not attached to service");
            }
            return false;
        }
        throw new IllegalArgumentException("command is null");
    }

    public boolean setActiveDevice(BluetoothDevice device) {
        Log.d(TAG, "setActiveDevice: " + device);
        IBluetoothHeadset service = this.mService;
        if (service != null && isEnabled() && (device == null || isValidDevice(device))) {
            try {
                return service.setActiveDevice(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (service == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public BluetoothDevice getActiveDevice() {
        IBluetoothHeadset service = this.mService;
        if (service != null && isEnabled()) {
            try {
                return service.getActiveDevice();
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (service == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return null;
    }

    public boolean isInbandRingingEnabled() {
        log("isInbandRingingEnabled()");
        IBluetoothHeadset service = this.mService;
        if (service != null && isEnabled()) {
            try {
                return service.isInbandRingingEnabled();
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (service == null) {
            Log.w(TAG, "Proxy not attached to service");
        }
        return false;
    }

    public static boolean isInbandRingingSupported(Context context) {
        return context.getResources().getBoolean(R.bool.config_bluetooth_hfp_inband_ringing_support);
    }

    private boolean isEnabled() {
        return this.mAdapter.getState() == 12;
    }

    private boolean isDisabled() {
        return this.mAdapter.getState() == 10;
    }

    private static boolean isValidDevice(BluetoothDevice device) {
        return device != null && BluetoothAdapter.checkBluetoothAddress(device.getAddress());
    }

    private static void log(String msg) {
        Log.d(TAG, msg);
    }

    private static void infolog(String msg) {
        Log.i(TAG, msg);
    }

    public boolean isScoAvailableOffCall() {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "isScoAvailableOffCall() Proxy not attached to service");
            Log.d(TAG, Log.getStackTraceString(new Throwable()));
        } else {
            try {
                return service.isScoAvailableOffCall();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        }
        return true;
    }
}
