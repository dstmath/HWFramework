package com.android.server.location;

import android.content.Context;
import android.hardware.location.ContextHubInfo;
import android.hardware.location.ContextHubMessage;
import android.hardware.location.IContextHubCallback;
import android.hardware.location.IContextHubService.Stub;
import android.hardware.location.NanoApp;
import android.hardware.location.NanoAppFilter;
import android.hardware.location.NanoAppInstanceInfo;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.DumpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ContextHubService extends Stub {
    public static final int ANY_HUB = -1;
    private static final String ENFORCE_HW_PERMISSION_MESSAGE = "Permission 'android.permission.LOCATION_HARDWARE' not granted to access ContextHub Hardware";
    private static final String HARDWARE_PERMISSION = "android.permission.LOCATION_HARDWARE";
    private static final int HEADER_FIELD_APP_INSTANCE = 3;
    private static final int HEADER_FIELD_HUB_HANDLE = 2;
    private static final int HEADER_FIELD_LOAD_APP_ID_HI = 5;
    private static final int HEADER_FIELD_LOAD_APP_ID_LO = 4;
    private static final int HEADER_FIELD_MSG_TYPE = 0;
    private static final int HEADER_FIELD_MSG_VERSION = 1;
    private static final int MSG_HEADER_SIZE = 4;
    private static final int MSG_LOAD_APP_HEADER_SIZE = 6;
    public static final int MSG_LOAD_NANO_APP = 3;
    public static final int MSG_UNLOAD_NANO_APP = 4;
    private static final int OS_APP_INSTANCE = -1;
    private static final int PRE_LOADED_APP_MEM_REQ = 0;
    private static final String PRE_LOADED_APP_NAME = "Preloaded app, unknown";
    private static final String PRE_LOADED_APP_PUBLISHER = "Preloaded app, unknown";
    private static final String PRE_LOADED_GENERIC_UNKNOWN = "Preloaded app, unknown";
    private static final String TAG = "ContextHubService";
    private final RemoteCallbackList<IContextHubCallback> mCallbacksList = new RemoteCallbackList();
    private final Context mContext;
    private final ContextHubInfo[] mContextHubInfo;
    private final ConcurrentHashMap<Integer, NanoAppInstanceInfo> mNanoAppHash = new ConcurrentHashMap();

    private native ContextHubInfo[] nativeInitialize();

    private native int nativeSendMessage(int[] iArr, byte[] bArr);

    public ContextHubService(Context context) {
        this.mContext = context;
        this.mContextHubInfo = nativeInitialize();
        for (int i = 0; i < this.mContextHubInfo.length; i++) {
            Log.d(TAG, "ContextHub[" + i + "] id: " + this.mContextHubInfo[i].getId() + ", name:  " + this.mContextHubInfo[i].getName());
        }
    }

    public int registerCallback(IContextHubCallback callback) throws RemoteException {
        checkPermissions();
        this.mCallbacksList.register(callback);
        Log.d(TAG, "Added callback, total callbacks " + this.mCallbacksList.getRegisteredCallbackCount());
        return 0;
    }

    public int[] getContextHubHandles() throws RemoteException {
        checkPermissions();
        int[] returnArray = new int[this.mContextHubInfo.length];
        Log.d(TAG, "System supports " + returnArray.length + " hubs");
        for (int i = 0; i < returnArray.length; i++) {
            returnArray[i] = i;
            Log.d(TAG, String.format("Hub %s is mapped to %d", new Object[]{this.mContextHubInfo[i].getName(), Integer.valueOf(returnArray[i])}));
        }
        return returnArray;
    }

    public ContextHubInfo getContextHubInfo(int contextHubHandle) throws RemoteException {
        checkPermissions();
        if (contextHubHandle >= 0 && contextHubHandle < this.mContextHubInfo.length) {
            return this.mContextHubInfo[contextHubHandle];
        }
        Log.e(TAG, "Invalid context hub handle " + contextHubHandle);
        return null;
    }

    public int loadNanoApp(int contextHubHandle, NanoApp app) throws RemoteException {
        checkPermissions();
        if (contextHubHandle < 0 || contextHubHandle >= this.mContextHubInfo.length) {
            Log.e(TAG, "Invalid contextHubhandle " + contextHubHandle);
            return -1;
        } else if (app == null) {
            Log.e(TAG, "Invalid null app");
            return -1;
        } else {
            msgHeader = new int[6];
            long appId = app.getAppId();
            msgHeader[4] = (int) (appId & -1);
            msgHeader[5] = (int) ((appId >> 32) & -1);
            if (nativeSendMessage(msgHeader, app.getAppBinary()) == 0) {
                return 0;
            }
            Log.e(TAG, "Send Message returns error" + contextHubHandle);
            return -1;
        }
    }

    public int unloadNanoApp(int nanoAppInstanceHandle) throws RemoteException {
        checkPermissions();
        if (((NanoAppInstanceInfo) this.mNanoAppHash.get(Integer.valueOf(nanoAppInstanceHandle))) == null) {
            Log.e(TAG, "Cannot find app with handle " + nanoAppInstanceHandle);
            return -1;
        }
        if (nativeSendMessage(new int[]{-1, nanoAppInstanceHandle, 0, 4}, new byte[0]) == 0) {
            return 0;
        }
        Log.e(TAG, "native send message fails");
        return -1;
    }

    public NanoAppInstanceInfo getNanoAppInstanceInfo(int nanoAppInstanceHandle) throws RemoteException {
        checkPermissions();
        if (this.mNanoAppHash.containsKey(Integer.valueOf(nanoAppInstanceHandle))) {
            return (NanoAppInstanceInfo) this.mNanoAppHash.get(Integer.valueOf(nanoAppInstanceHandle));
        }
        Log.e(TAG, "Could not find nanoApp with handle " + nanoAppInstanceHandle);
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
        for (int i = 0; i < foundInstances.size(); i++) {
            retArray[i] = ((Integer) foundInstances.get(i)).intValue();
        }
        Log.w(TAG, "Found " + retArray.length + " apps on hub handle " + hubHandle);
        return retArray;
    }

    public int sendMessage(int hubHandle, int nanoAppHandle, ContextHubMessage msg) throws RemoteException {
        checkPermissions();
        if (msg == null || msg.getData() == null) {
            Log.w(TAG, "null ptr");
            return -1;
        }
        return nativeSendMessage(new int[]{hubHandle, nanoAppHandle, msg.getVersion(), msg.getMsgType()}, msg.getData());
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.println("Dumping ContextHub Service");
            pw.println("");
            pw.println("=================== CONTEXT HUBS ====================");
            for (int i = 0; i < this.mContextHubInfo.length; i++) {
                pw.println("Handle " + i + " : " + this.mContextHubInfo[i].toString());
            }
            pw.println("");
            pw.println("=================== NANOAPPS ====================");
            for (Integer nanoAppInstance : this.mNanoAppHash.keySet()) {
                pw.println(nanoAppInstance + " : " + ((NanoAppInstanceInfo) this.mNanoAppHash.get(nanoAppInstance)).toString());
            }
        }
    }

    private void checkPermissions() {
        this.mContext.enforceCallingPermission(HARDWARE_PERMISSION, ENFORCE_HW_PERMISSION_MESSAGE);
    }

    private int onMessageReceipt(int[] header, byte[] data) {
        if (header == null || data == null || header.length < 4) {
            return -1;
        }
        int callbacksCount = this.mCallbacksList.beginBroadcast();
        int msgType = header[0];
        int msgVersion = header[1];
        int hubHandle = header[2];
        int appInstance = header[3];
        Log.d(TAG, "Sending message " + msgType + " version " + msgVersion + " from hubHandle " + hubHandle + ", appInstance " + appInstance + ", callBackCount " + callbacksCount);
        if (callbacksCount < 1) {
            Log.v(TAG, "No message callbacks registered.");
            return 0;
        }
        ContextHubMessage msg = new ContextHubMessage(msgType, msgVersion, data);
        for (int i = 0; i < callbacksCount; i++) {
            IContextHubCallback callback = (IContextHubCallback) this.mCallbacksList.getBroadcastItem(i);
            try {
                callback.onMessageReceipt(hubHandle, appInstance, msg);
            } catch (RemoteException e) {
                Log.i(TAG, "Exception (" + e + ") calling remote callback (" + callback + ").");
            }
        }
        this.mCallbacksList.finishBroadcast();
        return 0;
    }

    private int addAppInstance(int hubHandle, int appInstanceHandle, long appId, int appVersion) {
        String action;
        NanoAppInstanceInfo appInfo = new NanoAppInstanceInfo();
        appInfo.setAppId(appId);
        appInfo.setAppVersion(appVersion);
        appInfo.setName("Preloaded app, unknown");
        appInfo.setContexthubId(hubHandle);
        appInfo.setHandle(appInstanceHandle);
        appInfo.setPublisher("Preloaded app, unknown");
        appInfo.setNeededExecMemBytes(0);
        appInfo.setNeededReadMemBytes(0);
        appInfo.setNeededWriteMemBytes(0);
        if (this.mNanoAppHash.containsKey(Integer.valueOf(appInstanceHandle))) {
            action = "Updated";
        } else {
            action = "Added";
        }
        this.mNanoAppHash.put(Integer.valueOf(appInstanceHandle), appInfo);
        Log.d(TAG, action + " app instance " + appInstanceHandle + " with id " + appId + " version " + appVersion);
        return 0;
    }

    private int deleteAppInstance(int appInstanceHandle) {
        if (this.mNanoAppHash.remove(Integer.valueOf(appInstanceHandle)) == null) {
            return -1;
        }
        return 0;
    }
}
