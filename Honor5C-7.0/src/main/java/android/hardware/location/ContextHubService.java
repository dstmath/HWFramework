package android.hardware.location;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.location.IContextHubService.Stub;
import android.net.ProxyInfo;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ContextHubService extends Stub {
    public static final int ANY_HUB = -1;
    private static final long APP_ID_ACTIVITY_RECOGNITION = 5147455389092024320L;
    public static final String CONTEXTHUB_SERVICE = "contexthub_service";
    private static final String ENFORCE_HW_PERMISSION_MESSAGE = "Permission 'android.permission.LOCATION_HARDWARE' not granted to access ContextHub Hardware";
    private static final String HARDWARE_PERMISSION = "android.permission.LOCATION_HARDWARE";
    private static final int MSG_FIELD_APP_INSTANCE = 3;
    private static final int MSG_FIELD_HUB_HANDLE = 2;
    private static final int MSG_FIELD_TYPE = 0;
    private static final int MSG_FIELD_VERSION = 1;
    private static final int MSG_HEADER_SIZE = 4;
    public static final int MSG_LOAD_NANO_APP = 3;
    public static final int MSG_UNLOAD_NANO_APP = 4;
    private static final int OS_APP_INSTANCE = -1;
    private static final int PRE_LOADED_APP_MEM_REQ = 0;
    private static final String PRE_LOADED_APP_NAME = "Preloaded app, unknown";
    private static final String PRE_LOADED_APP_PUBLISHER = "Preloaded app, unknown";
    private static final String PRE_LOADED_GENERIC_UNKNOWN = "Preloaded app, unknown";
    private static final String TAG = "ContextHubService";
    private final RemoteCallbackList<IContextHubCallback> mCallbacksList;
    private final Context mContext;
    private final ContextHubInfo[] mContextHubInfo;
    private final ConcurrentHashMap<Integer, NanoAppInstanceInfo> mNanoAppHash;
    private final IVrStateCallbacks mVrStateCallbacks;

    private native ContextHubInfo[] nativeInitialize();

    private native int nativeSendMessage(int[] iArr, byte[] bArr);

    public ContextHubService(Context context) {
        this.mNanoAppHash = new ConcurrentHashMap();
        this.mCallbacksList = new RemoteCallbackList();
        this.mVrStateCallbacks = new IVrStateCallbacks.Stub() {
            public void onVrStateChanged(boolean enabled) {
                for (NanoAppInstanceInfo app : ContextHubService.this.mNanoAppHash.values()) {
                    if (app.getAppId() == ContextHubService.APP_ID_ACTIVITY_RECOGNITION) {
                        ContextHubService.this.sendVrStateChangeMessageToApp(app, enabled);
                        return;
                    }
                }
            }
        };
        this.mContext = context;
        this.mContextHubInfo = nativeInitialize();
        for (int i = PRE_LOADED_APP_MEM_REQ; i < this.mContextHubInfo.length; i += MSG_FIELD_VERSION) {
            Log.d(TAG, "ContextHub[" + i + "] id: " + this.mContextHubInfo[i].getId() + ", name:  " + this.mContextHubInfo[i].getName());
        }
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_VR_MODE)) {
            IVrManager vrManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
            if (vrManager != null) {
                try {
                    vrManager.registerListener(this.mVrStateCallbacks);
                } catch (RemoteException e) {
                    Log.e(TAG, "VR state listener registration failed", e);
                }
            }
        }
    }

    public int registerCallback(IContextHubCallback callback) throws RemoteException {
        checkPermissions();
        this.mCallbacksList.register(callback);
        return PRE_LOADED_APP_MEM_REQ;
    }

    public int[] getContextHubHandles() throws RemoteException {
        checkPermissions();
        int[] returnArray = new int[this.mContextHubInfo.length];
        for (int i = PRE_LOADED_APP_MEM_REQ; i < returnArray.length; i += MSG_FIELD_VERSION) {
            returnArray[i] = i;
            String str = TAG;
            Object[] objArr = new Object[MSG_FIELD_HUB_HANDLE];
            objArr[PRE_LOADED_APP_MEM_REQ] = this.mContextHubInfo[i].getName();
            objArr[MSG_FIELD_VERSION] = Integer.valueOf(returnArray[i]);
            Log.d(str, String.format("Hub %s is mapped to %d", objArr));
        }
        return returnArray;
    }

    public ContextHubInfo getContextHubInfo(int contextHubHandle) throws RemoteException {
        checkPermissions();
        if (contextHubHandle < 0 || contextHubHandle >= this.mContextHubInfo.length) {
            return null;
        }
        return this.mContextHubInfo[contextHubHandle];
    }

    public int loadNanoApp(int contextHubHandle, NanoApp app) throws RemoteException {
        checkPermissions();
        if (contextHubHandle < 0 || contextHubHandle >= this.mContextHubInfo.length) {
            Log.e(TAG, "Invalid contextHubhandle " + contextHubHandle);
            return OS_APP_INSTANCE;
        }
        int[] msgHeader = new int[MSG_UNLOAD_NANO_APP];
        msgHeader[MSG_FIELD_HUB_HANDLE] = contextHubHandle;
        msgHeader[MSG_LOAD_NANO_APP] = OS_APP_INSTANCE;
        msgHeader[MSG_FIELD_VERSION] = PRE_LOADED_APP_MEM_REQ;
        msgHeader[PRE_LOADED_APP_MEM_REQ] = MSG_LOAD_NANO_APP;
        return nativeSendMessage(msgHeader, app.getAppBinary()) != 0 ? OS_APP_INSTANCE : PRE_LOADED_APP_MEM_REQ;
    }

    public int unloadNanoApp(int nanoAppInstanceHandle) throws RemoteException {
        checkPermissions();
        if (((NanoAppInstanceInfo) this.mNanoAppHash.get(Integer.valueOf(nanoAppInstanceHandle))) == null) {
            return OS_APP_INSTANCE;
        }
        int[] msgHeader = new int[MSG_UNLOAD_NANO_APP];
        msgHeader[MSG_FIELD_HUB_HANDLE] = OS_APP_INSTANCE;
        msgHeader[MSG_LOAD_NANO_APP] = OS_APP_INSTANCE;
        msgHeader[MSG_FIELD_VERSION] = PRE_LOADED_APP_MEM_REQ;
        msgHeader[PRE_LOADED_APP_MEM_REQ] = MSG_UNLOAD_NANO_APP;
        return nativeSendMessage(msgHeader, null) != 0 ? OS_APP_INSTANCE : PRE_LOADED_APP_MEM_REQ;
    }

    public NanoAppInstanceInfo getNanoAppInstanceInfo(int nanoAppInstanceHandle) throws RemoteException {
        checkPermissions();
        if (this.mNanoAppHash.containsKey(Integer.valueOf(nanoAppInstanceHandle))) {
            return (NanoAppInstanceInfo) this.mNanoAppHash.get(Integer.valueOf(nanoAppInstanceHandle));
        }
        return null;
    }

    public int[] findNanoAppOnHub(int hubHandle, NanoAppFilter filter) throws RemoteException {
        checkPermissions();
        ArrayList<Integer> foundInstances = new ArrayList();
        for (Integer nanoAppInstance : this.mNanoAppHash.keySet()) {
            if (filter.testMatch((NanoAppInstanceInfo) this.mNanoAppHash.get(nanoAppInstance))) {
                foundInstances.add(nanoAppInstance);
            }
        }
        int[] retArray = new int[foundInstances.size()];
        for (int i = PRE_LOADED_APP_MEM_REQ; i < foundInstances.size(); i += MSG_FIELD_VERSION) {
            retArray[i] = ((Integer) foundInstances.get(i)).intValue();
        }
        return retArray;
    }

    public int sendMessage(int hubHandle, int nanoAppHandle, ContextHubMessage msg) throws RemoteException {
        checkPermissions();
        int[] msgHeader = new int[MSG_UNLOAD_NANO_APP];
        msgHeader[MSG_FIELD_HUB_HANDLE] = hubHandle;
        msgHeader[MSG_LOAD_NANO_APP] = nanoAppHandle;
        msgHeader[MSG_FIELD_VERSION] = msg.getVersion();
        msgHeader[PRE_LOADED_APP_MEM_REQ] = msg.getMsgType();
        return nativeSendMessage(msgHeader, msg.getData());
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission(permission.DUMP) != 0) {
            pw.println("Permission Denial: can't dump contexthub_service");
            return;
        }
        pw.println("Dumping ContextHub Service");
        pw.println(ProxyInfo.LOCAL_EXCL_LIST);
        pw.println("=================== CONTEXT HUBS ====================");
        for (int i = PRE_LOADED_APP_MEM_REQ; i < this.mContextHubInfo.length; i += MSG_FIELD_VERSION) {
            pw.println("Handle " + i + " : " + this.mContextHubInfo[i].toString());
        }
        pw.println(ProxyInfo.LOCAL_EXCL_LIST);
        pw.println("=================== NANOAPPS ====================");
        for (Integer nanoAppInstance : this.mNanoAppHash.keySet()) {
            pw.println(nanoAppInstance + " : " + ((NanoAppInstanceInfo) this.mNanoAppHash.get(nanoAppInstance)).toString());
        }
    }

    private void checkPermissions() {
        this.mContext.enforceCallingPermission(HARDWARE_PERMISSION, ENFORCE_HW_PERMISSION_MESSAGE);
    }

    private int onMessageReceipt(int[] header, byte[] data) {
        if (header == null || data == null || header.length < MSG_UNLOAD_NANO_APP) {
            return OS_APP_INSTANCE;
        }
        int callbacksCount = this.mCallbacksList.beginBroadcast();
        if (callbacksCount < MSG_FIELD_VERSION) {
            Log.v(TAG, "No message callbacks registered.");
            return PRE_LOADED_APP_MEM_REQ;
        }
        ContextHubMessage message = new ContextHubMessage(header[PRE_LOADED_APP_MEM_REQ], header[MSG_FIELD_VERSION], data);
        for (int i = PRE_LOADED_APP_MEM_REQ; i < callbacksCount; i += MSG_FIELD_VERSION) {
            IContextHubCallback callback = (IContextHubCallback) this.mCallbacksList.getBroadcastItem(i);
            try {
                callback.onMessageReceipt(header[MSG_FIELD_HUB_HANDLE], header[MSG_LOAD_NANO_APP], message);
            } catch (RemoteException e) {
                Log.i(TAG, "Exception (" + e + ") calling remote callback (" + callback + ").");
            }
        }
        this.mCallbacksList.finishBroadcast();
        return PRE_LOADED_APP_MEM_REQ;
    }

    private int addAppInstance(int hubHandle, int appInstanceHandle, long appId, int appVersion) {
        NanoAppInstanceInfo appInfo = new NanoAppInstanceInfo();
        appInfo.setAppId(appId);
        appInfo.setAppVersion(appVersion);
        appInfo.setName(PRE_LOADED_GENERIC_UNKNOWN);
        appInfo.setContexthubId(hubHandle);
        appInfo.setHandle(appInstanceHandle);
        appInfo.setPublisher(PRE_LOADED_GENERIC_UNKNOWN);
        appInfo.setNeededExecMemBytes(PRE_LOADED_APP_MEM_REQ);
        appInfo.setNeededReadMemBytes(PRE_LOADED_APP_MEM_REQ);
        appInfo.setNeededWriteMemBytes(PRE_LOADED_APP_MEM_REQ);
        this.mNanoAppHash.put(Integer.valueOf(appInstanceHandle), appInfo);
        Log.d(TAG, "Added app instance " + appInstanceHandle + " with id " + appId + " version " + appVersion);
        return PRE_LOADED_APP_MEM_REQ;
    }

    private void sendVrStateChangeMessageToApp(NanoAppInstanceInfo app, boolean vrModeEnabled) {
        int i = MSG_FIELD_VERSION;
        int[] msgHeader = new int[MSG_UNLOAD_NANO_APP];
        msgHeader[PRE_LOADED_APP_MEM_REQ] = PRE_LOADED_APP_MEM_REQ;
        msgHeader[MSG_FIELD_VERSION] = PRE_LOADED_APP_MEM_REQ;
        msgHeader[MSG_FIELD_HUB_HANDLE] = OS_APP_INSTANCE;
        msgHeader[MSG_LOAD_NANO_APP] = app.getHandle();
        byte[] data = new byte[MSG_FIELD_VERSION];
        if (!vrModeEnabled) {
            i = PRE_LOADED_APP_MEM_REQ;
        }
        data[PRE_LOADED_APP_MEM_REQ] = (byte) i;
        int ret = nativeSendMessage(msgHeader, data);
        if (ret != 0) {
            Log.e(TAG, "Couldn't send VR state change notification (" + ret + ")!");
        }
    }
}
