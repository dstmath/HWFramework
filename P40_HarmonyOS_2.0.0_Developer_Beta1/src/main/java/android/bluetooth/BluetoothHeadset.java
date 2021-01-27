package android.bluetooth;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
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
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.List;

public final class BluetoothHeadset implements BluetoothProfile {
    @UnsupportedAppUsage
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
    private static final boolean DBG = false;
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
        /* class android.bluetooth.BluetoothHeadset.AnonymousClass1 */

        @Override // android.bluetooth.IBluetoothStateChangeCallback
        public void onBluetoothStateChange(boolean up) {
            if (!up) {
                BluetoothHeadset.this.doUnbind();
            } else {
                BluetoothHeadset.this.doBind();
            }
        }
    };
    private final IBluetoothProfileServiceConnection mConnection = new IBluetoothProfileServiceConnection.Stub() {
        /* class android.bluetooth.BluetoothHeadset.AnonymousClass2 */

        @Override // android.bluetooth.IBluetoothProfileServiceConnection
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothHeadset.this.mService = IBluetoothHeadset.Stub.asInterface(Binder.allowBlocking(service));
            BluetoothHeadset.this.mHandler.sendMessage(BluetoothHeadset.this.mHandler.obtainMessage(100));
        }

        @Override // android.bluetooth.IBluetoothProfileServiceConnection
        public void onServiceDisconnected(ComponentName className) {
            BluetoothHeadset.this.doUnbind();
            BluetoothHeadset.this.mHandler.sendMessage(BluetoothHeadset.this.mHandler.obtainMessage(101));
        }
    };
    private Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class android.bluetooth.BluetoothHeadset.AnonymousClass3 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 100) {
                if (i == 101 && BluetoothHeadset.this.mServiceListener != null) {
                    BluetoothHeadset.this.mServiceListener.onServiceDisconnected(1);
                }
            } else if (BluetoothHeadset.this.mServiceListener != null) {
                BluetoothHeadset.this.mServiceListener.onServiceConnected(1, BluetoothHeadset.this);
            }
        }
    };
    private volatile IBluetoothHeadset mService;
    private BluetoothProfile.ServiceListener mServiceListener;

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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean doBind() {
        synchronized (this.mConnection) {
            if (this.mService == null) {
                try {
                    return this.mAdapter.getBluetoothManager().bindBluetoothProfileService(1, this.mConnection);
                } catch (RemoteException e) {
                    Log.e(TAG, "Unable to bind HeadsetService", e);
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doUnbind() {
        synchronized (this.mConnection) {
            if (this.mService != null) {
                try {
                    this.mAdapter.getBluetoothManager().unbindBluetoothProfileService(1, this.mConnection);
                } catch (RemoteException e) {
                    Log.e(TAG, "Unable to unbind HeadsetService", e);
                } finally {
                    this.mService = null;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void close() {
        IBluetoothManager mgr = this.mAdapter.getBluetoothManager();
        if (mgr != null) {
            try {
                mgr.unregisterStateChangeCallback(this.mBluetoothStateChangeCallback);
            } catch (RemoteException re) {
                Log.e(TAG, "", re);
            }
        }
        this.mServiceListener = null;
        doUnbind();
    }

    @SystemApi
    public boolean connect(BluetoothDevice device) {
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

    @Override // android.bluetooth.BluetoothProfile
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

    @Override // android.bluetooth.BluetoothProfile
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

    @Override // android.bluetooth.BluetoothProfile
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

    @UnsupportedAppUsage
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
        IBluetoothHeadset service = this.mService;
        if (service != null && isEnabled() && isValidDevice(device)) {
            try {
                return service.startVoiceRecognition(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (service != null) {
            return false;
        }
        Log.w(TAG, "Proxy not attached to service");
        return false;
    }

    public boolean stopVoiceRecognition(BluetoothDevice device) {
        IBluetoothHeadset service = this.mService;
        if (service != null && isEnabled() && isValidDevice(device)) {
            try {
                return service.stopVoiceRecognition(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (service != null) {
            return false;
        }
        Log.w(TAG, "Proxy not attached to service");
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
        if (service != null) {
            return false;
        }
        Log.w(TAG, "Proxy not attached to service");
        return false;
    }

    public static boolean isBluetoothVoiceDialingEnabled(Context context) {
        return context.getResources().getBoolean(R.bool.config_bluetooth_sco_off_call);
    }

    @UnsupportedAppUsage
    public int getAudioState(BluetoothDevice device) {
        IBluetoothHeadset service = this.mService;
        if (service == null || isDisabled()) {
            Log.w(TAG, "Proxy not attached to service");
            return 10;
        }
        try {
            return service.getAudioState(device);
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
            return 10;
        }
    }

    public void setAudioRouteAllowed(boolean allowed) {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
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
            return false;
        }
        try {
            return service.getAudioRouteAllowed();
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public void setForceScoAudio(boolean forced) {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
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
        if (service != null) {
            return false;
        }
        Log.w(TAG, "Proxy not attached to service");
        return false;
    }

    @UnsupportedAppUsage
    public boolean connectAudio() {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            return false;
        }
        try {
            return service.connectAudio();
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    @UnsupportedAppUsage
    public boolean disconnectAudio() {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            return false;
        }
        try {
            return service.disconnectAudio();
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    @UnsupportedAppUsage
    public boolean startScoUsingVirtualVoiceCall() {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            return false;
        }
        try {
            return service.startScoUsingVirtualVoiceCall();
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    @UnsupportedAppUsage
    public boolean stopScoUsingVirtualVoiceCall() {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            return false;
        }
        try {
            return service.stopScoUsingVirtualVoiceCall();
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    @UnsupportedAppUsage
    public void phoneStateChanged(int numActive, int numHeld, int callState, String number, int type, String name) {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            return;
        }
        try {
            service.phoneStateChanged(numActive, numHeld, callState, number, type, name);
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }

    public void clccResponse(int index, int direction, int status, int mode, boolean mpty, String number, int type) {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "Proxy not attached to service");
            return;
        }
        try {
            service.clccResponse(index, direction, status, mode, mpty, number, type);
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }

    public boolean sendVendorSpecificResultCode(BluetoothDevice device, String command, String arg) {
        if (command != null) {
            IBluetoothHeadset service = this.mService;
            if (service != null && isEnabled() && isValidDevice(device)) {
                try {
                    return service.sendVendorSpecificResultCode(device, command, arg);
                } catch (RemoteException e) {
                    Log.e(TAG, Log.getStackTraceString(new Throwable()));
                }
            }
            if (service != null) {
                return false;
            }
            Log.w(TAG, "Proxy not attached to service");
            return false;
        }
        throw new IllegalArgumentException("command is null");
    }

    @UnsupportedAppUsage
    public boolean setActiveDevice(BluetoothDevice device) {
        IBluetoothHeadset service = this.mService;
        if (service != null && isEnabled() && (device == null || isValidDevice(device))) {
            try {
                return service.setActiveDevice(device);
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (service != null) {
            return false;
        }
        Log.w(TAG, "Proxy not attached to service");
        return false;
    }

    @UnsupportedAppUsage
    public BluetoothDevice getActiveDevice() {
        IBluetoothHeadset service = this.mService;
        if (service != null && isEnabled()) {
            try {
                return service.getActiveDevice();
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (service != null) {
            return null;
        }
        Log.w(TAG, "Proxy not attached to service");
        return null;
    }

    public boolean isInbandRingingEnabled() {
        IBluetoothHeadset service = this.mService;
        if (service != null && isEnabled()) {
            try {
                return service.isInbandRingingEnabled();
            } catch (RemoteException e) {
                Log.e(TAG, Log.getStackTraceString(new Throwable()));
            }
        }
        if (service != null) {
            return false;
        }
        Log.w(TAG, "Proxy not attached to service");
        return false;
    }

    public static boolean isInbandRingingSupported(Context context) {
        return context.getResources().getBoolean(R.bool.config_bluetooth_hfp_inband_ringing_support);
    }

    @UnsupportedAppUsage
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

    public boolean isScoAvailableOffCall() {
        IBluetoothHeadset service = this.mService;
        if (service == null || !isEnabled()) {
            Log.w(TAG, "isScoAvailableOffCall() Proxy not attached to service");
            return true;
        }
        try {
            return service.isScoAvailableOffCall();
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
            return true;
        }
    }

    private static String getPartAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return "";
        }
        return address.substring(0, address.length() / 2) + ":**:**:**";
    }

    private static String getPartAddress(BluetoothDevice device) {
        if (device == null) {
            return "";
        }
        return getPartAddress(device.getAddress());
    }
}
