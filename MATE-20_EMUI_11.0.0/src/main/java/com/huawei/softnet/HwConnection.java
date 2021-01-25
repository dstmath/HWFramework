package com.huawei.softnet;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.softnet.IConnService;
import com.huawei.softnet.connect.ConnectOption;
import com.huawei.softnet.connect.ConnectionCallback;
import com.huawei.softnet.connect.ConnectionDesc;
import com.huawei.softnet.connect.ConnectionResult;
import com.huawei.softnet.connect.DataCallback;
import com.huawei.softnet.connect.DataPayload;
import com.huawei.softnet.connect.DevConfig;
import com.huawei.softnet.connect.DeviceDesc;
import com.huawei.softnet.connect.DiscoveryCallback;
import com.huawei.softnet.connect.DiscoveryOption;
import com.huawei.softnet.connect.IConnectOption;
import com.huawei.softnet.connect.IConnectionCallback;
import com.huawei.softnet.connect.IConnectionDesc;
import com.huawei.softnet.connect.IConnectionResult;
import com.huawei.softnet.connect.IDataCallback;
import com.huawei.softnet.connect.IDevConfig;
import com.huawei.softnet.connect.IDeviceDesc;
import com.huawei.softnet.connect.IDiscoveryCallback;
import com.huawei.softnet.connect.IDiscoveryOption;
import com.huawei.softnet.connect.INetRole;
import com.huawei.softnet.connect.IPowerPolicy;
import com.huawei.softnet.connect.IPublishOption;
import com.huawei.softnet.connect.IServiceDesc;
import com.huawei.softnet.connect.IServiceFilter;
import com.huawei.softnet.connect.IStrategy;
import com.huawei.softnet.connect.ModuleIdentifier;
import com.huawei.softnet.connect.NetRole;
import com.huawei.softnet.connect.PowerPolicy;
import com.huawei.softnet.connect.PublishOption;
import com.huawei.softnet.connect.ServiceDesc;
import com.huawei.softnet.connect.ServiceFilter;
import com.huawei.softnet.connect.Strategy;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public final class HwConnection {
    private static final int ACTIVE_APP_COUNT_ZERO = 0;
    private static final long DELAY_TIME = 4000;
    private static final String F_RESULT_CODE = "F_RESULT_CODE:";
    private static final int INDEX_BEGIN = 0;
    private static final int MAX_SEND_BYTE_LENGTH = 2000;
    private static final int MSG_ACCEPT_CONNECT = 23;
    private static final int MSG_CONNECT = 21;
    private static final int MSG_DESTROY = 2;
    private static final int MSG_DISCONNECT = 22;
    private static final int MSG_DISCONNECT_ALL = 25;
    private static final int MSG_PUBLSIH_SERVICE = 103;
    private static final int MSG_REJECT_CONNECT = 24;
    private static final int MSG_SEND_BLOCK = 27;
    private static final int MSG_SEND_BYTE = 26;
    private static final int MSG_SEND_FILE = 28;
    private static final int MSG_SEND_STREAM = 29;
    private static final int MSG_SET_CONFIG = 3;
    private static final int MSG_START_DISCOVERY = 101;
    private static final int MSG_STOP_DISCOVERY = 102;
    private static final int MSG_UN_PUBLISH = 104;
    private static final int RESULT_CODE_CALLBACK_CONNECT_INIT = 13;
    private static final int RESULT_CODE_CALLBACK_CONNECT_STATE_UPDATE = 14;
    private static final int RESULT_CODE_CALLBACK_DATA_BLOCK_RECEIVE = 17;
    private static final int RESULT_CODE_CALLBACK_DATA_BYTE_RECEIVE = 16;
    private static final int RESULT_CODE_CALLBACK_DATA_COMMON_UPDATE = 21;
    private static final int RESULT_CODE_CALLBACK_DATA_FILE_RECEIVE = 18;
    private static final int RESULT_CODE_CALLBACK_DATA_RECEIVE = 11;
    private static final int RESULT_CODE_CALLBACK_DATA_SENDFILE_UPDATE = 20;
    private static final int RESULT_CODE_CALLBACK_DATA_STREAM_RECEIVE = 19;
    private static final int RESULT_CODE_CALLBACK_DATA_UPDATE = 12;
    private static final int RESULT_CODE_CALLBACK_DEVICE_FOUND = 9;
    private static final int RESULT_CODE_CALLBACK_DEVICE_LOST = 10;
    private static final int RESULT_CODE_CALLBACK_DISCONNECT = 15;
    private static final int RESULT_CODE_ERROR_CONTEXT_NULL = 4;
    private static final int RESULT_CODE_ERROR_HANDLER_UNINIT = 7;
    private static final int RESULT_CODE_ERROR_PARM_NULL = 1;
    private static final int RESULT_CODE_ERROR_QUEUE_EXCEPTION = 6;
    private static final int RESULT_CODE_ERROR_REMOTE_EXCEPTION = 8;
    private static final int RESULT_CODE_ERROR_SERVICE_NULL = 5;
    private static final int RESULT_CODE_SUCCESS = 0;
    public static final String SERVICE_TYPE_AACAPABILITY = "aaCapability";
    public static final String SERVICE_TYPE_CASTPLUS = "castPlus";
    public static final String SERVICE_TYPE_DDMPCAPABILITY = "ddmpCapability";
    public static final String SERVICE_TYPE_DVKIT = "dvKit";
    public static final String SERVICE_TYPE_HICALL = "hicall";
    public static final String SERVICE_TYPE_HOMEVISIONPIC = "homevisionPic";
    public static final String SERVICE_TYPE_PROFILE = "profile";
    private static final String TAG = "HwConnection";
    private static HwConnection sInstance;
    private static final Object sLock = new Object();
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private String mModuleId;
    private final Queue<Message> mPendingTasks = new LinkedList();
    private IConnService mService;

    private HwConnection(Context context) {
        Log.i(TAG, TAG);
        initHandler();
        handleServiceBind();
        this.mModuleId = new ModuleIdentifier(context).getModuleId();
    }

    public static HwConnection getInstance(Context context) {
        HwConnection hwConnection;
        if (context == null) {
            Log.e(TAG, "F_RESULT_CODE:4reason: getInstance fail for context null");
            return null;
        }
        Log.i(TAG, "getInstance");
        synchronized (sLock) {
            if (sInstance == null) {
                Log.i(TAG, "new HwConnection");
                sInstance = new HwConnection(context);
            }
            Log.i(TAG, "getInstance APP is:" + context.getPackageName());
            hwConnection = sInstance;
        }
        return hwConnection;
    }

    private HwConnection(Context context, String moduleName) {
        Log.i(TAG, TAG);
        initHandler();
        handleServiceBind();
        ModuleIdentifier mModuleIdentifier = new ModuleIdentifier(context);
        this.mModuleId = mModuleIdentifier.getModuleId() + "$" + moduleName;
    }

    public static HwConnection getInstance(Context context, String moduleName) {
        HwConnection hwConnection;
        if (context == null) {
            Log.e(TAG, "F_RESULT_CODE:4reason: getInstance fail for context null");
            return null;
        }
        Log.i(TAG, "getInstance with moduleName");
        synchronized (sLock) {
            if (sInstance == null) {
                Log.i(TAG, "new HwConnection");
                sInstance = new HwConnection(context, moduleName);
            }
            Log.i(TAG, "getInstance APP is:" + context.getPackageName() + " moduleName:" + moduleName);
            hwConnection = sInstance;
        }
        return hwConnection;
    }

    public int startDiscovery(String moduleName, DiscoveryOption option, DiscoveryCallback callback) {
        if (!checkModuleName(moduleName) || callback == null || option == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: startDiscovery fail for parm is null");
            return 1;
        }
        Log.i(TAG, "startDiscovery");
        putMessage(Message.obtain(this.mHandler, 101, new DiscoveryArgs(moduleName, option, callback)));
        return 0;
    }

    public int stopDiscovery(String moduleName, int discoveryMode) {
        if (!checkModuleName(moduleName)) {
            Log.e(TAG, "F_RESULT_CODE:1reason: stopDiscovery fail for parm is null");
            return 1;
        }
        Log.i(TAG, "stopDiscovery");
        putMessage(Message.obtain(this.mHandler, 102, new StopDiscArgs(moduleName, discoveryMode)));
        return 0;
    }

    public int publishService(String moduleName, PublishOption option, ConnectionCallback callback) {
        if (!checkModuleName(moduleName) || callback == null || option == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: publishService fail for parm is null");
            return 1;
        }
        Log.i(TAG, "publishService");
        putMessage(Message.obtain(this.mHandler, 103, new PublishArgs(moduleName, option, callback)));
        return 0;
    }

    public int unPublishService(String moduleName, int publishMode) {
        if (!checkModuleName(moduleName)) {
            Log.e(TAG, "F_RESULT_CODE:1reason: unPublishService fail for parm is null");
            return 1;
        }
        Log.i(TAG, "unPublishService");
        putMessage(Message.obtain(this.mHandler, 104, new UnPublishArgs(moduleName, publishMode)));
        return 0;
    }

    public int setConfig(DevConfig devConfig) {
        if (devConfig == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: setConfig fail for parm is null");
            return 1;
        }
        Log.i(TAG, "setConfig");
        putMessage(Message.obtain(this.mHandler, 3, devConfig));
        return 0;
    }

    public int connectDevice(String localModule, String remoteDeviceId, String remoteModule, ConnectOption option, ConnectionCallback callback) {
        if (!checkModuleName(localModule) || TextUtils.isEmpty(remoteDeviceId) || !checkModuleName(remoteModule) || option == null || callback == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: connectDevice fail for parm is null");
            return 1;
        }
        Log.i(TAG, "connectDevice");
        putMessage(Message.obtain(this.mHandler, 21, new ConnectArgs(localModule, remoteDeviceId, remoteModule, option, callback)));
        return 0;
    }

    public int disconnectDevice(String localModule, String remoteDeviceId, String remoteModule) {
        if (!checkModuleName(localModule) || TextUtils.isEmpty(remoteDeviceId) || !checkModuleName(remoteModule)) {
            Log.e(TAG, "F_RESULT_CODE:1reason: disconnectDevice fail for parm is null");
            return 1;
        }
        Log.i(TAG, "disconnectDevice");
        putMessage(Message.obtain(this.mHandler, 22, new DisConnectArgs(localModule, remoteDeviceId, remoteModule)));
        return 0;
    }

    public int acceptConnect(String localModule, String remoteDeviceId, String remoteModule, DataCallback callback) {
        if (!checkModuleName(localModule) || TextUtils.isEmpty(remoteDeviceId) || !checkModuleName(remoteModule) || callback == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: acceptConnect fail for parm is null");
            return 1;
        }
        Log.i(TAG, "acceptConnect");
        putMessage(Message.obtain(this.mHandler, 23, new AcceptConnectArgs(localModule, remoteDeviceId, remoteModule, callback)));
        return 0;
    }

    public int rejectConnect(String localModule, String remoteDeviceId, String remoteModule) {
        if (!checkModuleName(localModule) || TextUtils.isEmpty(remoteDeviceId) || !checkModuleName(remoteModule)) {
            Log.e(TAG, "F_RESULT_CODE:1reason: rejectConnect fail for parm is null");
            return 1;
        }
        Log.i(TAG, "rejectConnect");
        putMessage(Message.obtain(this.mHandler, 24, new DisConnectArgs(localModule, remoteDeviceId, remoteModule)));
        return 0;
    }

    public int sendByte(String localModule, String remoteDeviceId, String remoteModule, byte[] data, int len, String extInfo) {
        if (checkModuleName(localModule) && !TextUtils.isEmpty(remoteDeviceId)) {
            if (checkModuleName(remoteModule) && data != null) {
                if (TextUtils.isEmpty(extInfo)) {
                    Log.e(TAG, "F_RESULT_CODE:1reason: sendByte fail for parm is null");
                    return 1;
                } else if (len >= 2000) {
                    Log.e(TAG, "F_RESULT_CODE:1reason: sendByte data length cannot be >= 2000");
                    return 1;
                } else {
                    Log.i(TAG, "sendByte");
                    putMessage(Message.obtain(this.mHandler, 26, new SendByteArgs(localModule, remoteDeviceId, remoteModule, data, len, extInfo)));
                    return 0;
                }
            }
        }
        Log.e(TAG, "F_RESULT_CODE:1reason: sendByte fail for parm is null");
        return 1;
    }

    public int sendBlock(String localModule, String remoteDeviceId, String remoteModule, byte[] data, int len, String extInfo) {
        if (checkModuleName(localModule) && !TextUtils.isEmpty(remoteDeviceId)) {
            if (checkModuleName(remoteModule) && data != null) {
                if (TextUtils.isEmpty(extInfo)) {
                    Log.e(TAG, "F_RESULT_CODE:1reason: sendBlock fail for parm is null");
                    return 1;
                } else if (len < 2000) {
                    Log.e(TAG, "F_RESULT_CODE:1reason: sendBlock data length cannot be < 2000");
                    return 1;
                } else {
                    Log.i(TAG, "sendBlock");
                    putMessage(Message.obtain(this.mHandler, 27, new SendByteArgs(localModule, remoteDeviceId, remoteModule, data, len, extInfo)));
                    return 0;
                }
            }
        }
        Log.e(TAG, "F_RESULT_CODE:1reason: sendBlock fail for parm is null");
        return 1;
    }

    public int sendFile(String localModule, String remoteDeviceId, String remoteModule, String sourceFile, String destFilePath, String extInfo) {
        if (checkModuleName(localModule) && !TextUtils.isEmpty(remoteDeviceId)) {
            if (checkModuleName(remoteModule) && !TextUtils.isEmpty(sourceFile) && !TextUtils.isEmpty(destFilePath) && !TextUtils.isEmpty(extInfo)) {
                Log.i(TAG, "sendFile");
                putMessage(Message.obtain(this.mHandler, 28, new SendFileArgs(localModule, remoteDeviceId, remoteModule, sourceFile, destFilePath, extInfo)));
                return 0;
            }
        }
        Log.e(TAG, "F_RESULT_CODE:1reason: sendFile fail for parm is null");
        return 1;
    }

    public int sendStream(String localModule, String remoteDeviceId, String remoteModule, DataPayload stream, String extInfo) {
        if (!checkModuleName(localModule) || TextUtils.isEmpty(remoteDeviceId) || !checkModuleName(remoteModule) || stream == null || TextUtils.isEmpty(extInfo)) {
            Log.e(TAG, "F_RESULT_CODE:1reason: sendStream fail for parm is null");
            return 1;
        }
        Log.i(TAG, "sendStream");
        putMessage(Message.obtain(this.mHandler, 29, new SendStreamArgs(localModule, remoteDeviceId, remoteModule, stream, extInfo)));
        return 0;
    }

    public int disconnectAll() {
        Log.i(TAG, "disconnectAll");
        putMessage(Message.obtain(this.mHandler, 25));
        return 0;
    }

    public int destroy() {
        Log.i(TAG, "destroy App is:" + this.mModuleId);
        putMessage(Message.obtain(this.mHandler, 2));
        return 0;
    }

    private void putMessage(Message msg) {
        if (this.mService == null) {
            Log.i(TAG, "F_RESULT_CODE:5reason: putmMessage mService == null, rebind service");
            rebindService();
        }
        if (this.mService == null) {
            Log.e(TAG, "F_RESULT_CODE:5reason: putMessage mService == null after rebind");
            if (!this.mPendingTasks.offer(msg)) {
                Log.e(TAG, "F_RESULT_CODE:6reason: putMessage queue fail");
                return;
            }
            return;
        }
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendMessage(msg);
        } else {
            Log.e(TAG, "F_RESULT_CODE:7reason: putMessage mHandler == null");
        }
    }

    private void initHandler() {
        Log.i(TAG, "initHandler");
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper()) {
            /* class com.huawei.softnet.HwConnection.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 2) {
                    HwConnection.this.handleDestroy();
                } else if (i != 3) {
                    switch (i) {
                        case 21:
                            HwConnection.this.handleConnect((ConnectArgs) msg.obj);
                            return;
                        case 22:
                            HwConnection.this.handleDisConnect((DisConnectArgs) msg.obj);
                            return;
                        case 23:
                            HwConnection.this.handleAcceptConnect((AcceptConnectArgs) msg.obj);
                            return;
                        case 24:
                            HwConnection.this.handleRejectConnect((DisConnectArgs) msg.obj);
                            return;
                        case 25:
                            HwConnection.this.handleDisconnectAll();
                            return;
                        case 26:
                            HwConnection.this.handleSendByte((SendByteArgs) msg.obj);
                            return;
                        case 27:
                            HwConnection.this.handleSendBlock((SendByteArgs) msg.obj);
                            return;
                        case 28:
                            HwConnection.this.handleSendFile((SendFileArgs) msg.obj);
                            return;
                        case 29:
                            HwConnection.this.handleSendStream((SendStreamArgs) msg.obj);
                            return;
                        default:
                            switch (i) {
                                case 101:
                                    HwConnection.this.handleStartDiscovery((DiscoveryArgs) msg.obj);
                                    return;
                                case 102:
                                    HwConnection.this.handleStopDiscovery((StopDiscArgs) msg.obj);
                                    return;
                                case 103:
                                    HwConnection.this.handleStartPublish((PublishArgs) msg.obj);
                                    return;
                                case 104:
                                    HwConnection.this.handleUnPublish((UnPublishArgs) msg.obj);
                                    return;
                                default:
                                    Log.i(HwConnection.TAG, "handleMessage default");
                                    return;
                            }
                    }
                } else if (msg.obj instanceof DevConfig) {
                    HwConnection.this.handleSetConfig((DevConfig) msg.obj);
                } else {
                    Log.e(HwConnection.TAG, "msg obj cannot transact to DevConfig");
                }
            }
        };
    }

    private void handleServiceBind() {
        Log.i(TAG, "handleServiceBind");
        IBinder binder = ServiceManager.getService("CommunicationManager");
        if (binder == null) {
            Log.e(TAG, "F_RESULT_CODE:5reason: not bind to ConnService");
            handleDestroy();
            return;
        }
        this.mService = IConnService.Stub.asInterface(binder);
        while (!this.mPendingTasks.isEmpty()) {
            Message message = this.mPendingTasks.poll();
            Handler handler = this.mHandler;
            if (!(handler == null || message == null)) {
                handler.sendMessage(message);
            }
        }
    }

    private void rebindService() {
        Log.i(TAG, "rebindService");
        IBinder binder = ServiceManager.getService("CommunicationManager");
        if (binder == null) {
            Log.e(TAG, "F_RESULT_CODE:5reason: not bind to ConnService");
            handleDestroy();
            return;
        }
        this.mService = IConnService.Stub.asInterface(binder);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDestroy() {
        Log.i(TAG, "handleDestroy");
        if (this.mService != null) {
            Log.i(TAG, "handleDestroy destroy start");
            try {
                int resultCode = this.mService.destroy(this.mModuleId);
                Log.i(TAG, "handleDestroy destroy result:" + resultCode);
            } catch (RemoteException e) {
                Log.e(TAG, "F_RESULT_CODE:8reason: handleDestroyRemoteException");
            }
        }
        this.mService = null;
        this.mPendingTasks.clear();
        HandlerThread handlerThread = this.mHandlerThread;
        if (handlerThread != null) {
            handlerThread.quitSafely();
        }
        this.mHandler = null;
        synchronized (sLock) {
            sInstance = null;
        }
    }

    /* access modifiers changed from: private */
    public static class DiscoveryArgs {
        final DiscoveryCallback callback;
        final String moduleName;
        final DiscoveryOption option;

        DiscoveryArgs(String moduleNameArg, DiscoveryOption optionArg, DiscoveryCallback callbackArg) {
            this.moduleName = moduleNameArg;
            this.option = optionArg;
            this.callback = callbackArg;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleStartDiscovery(DiscoveryArgs args) {
        int resultCode = 0;
        rebindService();
        if (args == null || this.mService == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: handleStartDiscovery args is null");
            return 1;
        }
        Log.i(TAG, "handleStartDiscovery");
        String moduleName = args.moduleName;
        try {
            resultCode = this.mService.startDiscovery(moduleName, getIDiscoveryOption(args.option), new DiscoveryListener(args.callback));
        } catch (RemoteException e) {
            Log.e(TAG, "F_RESULT_CODE:8reason: handleStartDiscoveryRemoteException");
        }
        Log.i(TAG, "handleStartDiscovery moduleName: " + moduleName + " resultCode: " + resultCode);
        return resultCode;
    }

    /* access modifiers changed from: private */
    public static class StopDiscArgs {
        final int discoveryMode;
        final String moduleName;

        StopDiscArgs(String moduleNameArgs, int discoveryModeArgs) {
            this.moduleName = moduleNameArgs;
            this.discoveryMode = discoveryModeArgs;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleStopDiscovery(StopDiscArgs args) {
        int resultCode = 0;
        rebindService();
        if (args == null || this.mService == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: handleStopDiscovery fail for parmis null");
            return 1;
        }
        Log.i(TAG, "handleStopDiscovery");
        String moduleName = args.moduleName;
        try {
            resultCode = this.mService.stopDiscovery(moduleName, args.discoveryMode);
        } catch (RemoteException e) {
            Log.e(TAG, "F_RESULT_CODE:8reason: handleStopDiscoveryRemoteException");
        }
        Log.i(TAG, "handleStopDiscovery moduleName: " + moduleName + " resultCode: " + resultCode);
        return resultCode;
    }

    /* access modifiers changed from: private */
    public static class PublishArgs {
        final ConnectionCallback callback;
        final String moduleName;
        final PublishOption option;

        PublishArgs(String moduleNameArg, PublishOption optionArg, ConnectionCallback callbackArg) {
            this.moduleName = moduleNameArg;
            this.option = optionArg;
            this.callback = callbackArg;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleStartPublish(PublishArgs args) {
        int resultCode = 0;
        rebindService();
        if (args == null || this.mService == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: handleStartPublish args is null");
            return 1;
        }
        Log.i(TAG, "handleStartPublish");
        String moduleName = args.moduleName;
        try {
            resultCode = this.mService.publishService(moduleName, getIPublishOption(args.option), new ConnectionListener(args.callback));
        } catch (RemoteException e) {
            Log.e(TAG, "F_RESULT_CODE:8reason: handleStartPublishRemoteException");
        }
        Log.i(TAG, "handleStartPublish moduleName: " + moduleName + " resultCode: " + resultCode);
        return resultCode;
    }

    /* access modifiers changed from: private */
    public static class UnPublishArgs {
        final String moduleName;
        final int publishMode;

        UnPublishArgs(String moduleNameArgs, int publishModeArgs) {
            this.moduleName = moduleNameArgs;
            this.publishMode = publishModeArgs;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleUnPublish(UnPublishArgs args) {
        int resultCode = 0;
        rebindService();
        if (args == null || this.mService == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: handleUnPublish args is null");
            return 1;
        }
        Log.i(TAG, "handleUnPublish");
        String moduleName = args.moduleName;
        try {
            resultCode = this.mService.unPublishService(moduleName, args.publishMode);
        } catch (RemoteException e) {
            Log.e(TAG, "F_RESULT_CODE:8reason: handleUnPublishRemoteException");
        }
        Log.i(TAG, "handleUnPublish moduleName: " + moduleName + " resultCode: " + resultCode);
        return resultCode;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleSetConfig(DevConfig args) {
        int resultCode = 0;
        rebindService();
        if (args == null || this.mService == null || TextUtils.isEmpty(this.mModuleId)) {
            Log.e(TAG, "F_RESULT_CODE:1reason: handleSetConfig args is null");
            return 1;
        }
        Log.i(TAG, "handleSetConfig");
        try {
            resultCode = this.mService.setConfig(this.mModuleId, getIDevConfig(args));
        } catch (RemoteException e) {
            Log.e(TAG, "F_RESULT_CODE:8reason: handleSetConfig RemoteException");
        }
        Log.i(TAG, "handleSetConfig moduleName: " + this.mModuleId + " resultCode: " + resultCode);
        return resultCode;
    }

    /* access modifiers changed from: private */
    public static class ConnectArgs {
        final ConnectionCallback callback;
        final String deviceId;
        final String localModule;
        final ConnectOption option;
        final String remoteModule;

        ConnectArgs(String localModuleArg, String deviceIdArg, String remoteModuleArg, ConnectOption optionArg, ConnectionCallback callbackArg) {
            this.localModule = localModuleArg;
            this.deviceId = deviceIdArg;
            this.remoteModule = remoteModuleArg;
            this.option = optionArg;
            this.callback = callbackArg;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleConnect(ConnectArgs args) {
        int resultCode = 0;
        rebindService();
        if (args == null || this.mService == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: handleConnect args is null");
            return 1;
        }
        Log.i(TAG, "handleConnect");
        String localModule = args.localModule;
        try {
            resultCode = this.mService.connect(localModule, args.deviceId, args.remoteModule, getIConnectOption(args.option), new ConnectionListener(args.callback));
        } catch (RemoteException e) {
            Log.e(TAG, "F_RESULT_CODE:8reason: handleConnect RemoteException");
        }
        Log.i(TAG, "handleConnect moduleName: " + localModule + " resultCode: " + resultCode);
        return resultCode;
    }

    /* access modifiers changed from: private */
    public static class DisConnectArgs {
        final String deviceId;
        final String localModule;
        final String remoteModule;

        DisConnectArgs(String localModuleArg, String deviceIdArg, String remoteModuleArg) {
            this.localModule = localModuleArg;
            this.deviceId = deviceIdArg;
            this.remoteModule = remoteModuleArg;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleDisConnect(DisConnectArgs args) {
        int resultCode = 0;
        rebindService();
        if (args == null || this.mService == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: handleDisConnect args is null");
            return 1;
        }
        Log.e(TAG, "handleDisConnect");
        String localModule = args.localModule;
        try {
            resultCode = this.mService.disconnect(localModule, args.deviceId, args.remoteModule);
        } catch (RemoteException e) {
            Log.e(TAG, "F_RESULT_CODE:8reason: handleDisConnect RemoteException");
        }
        Log.i(TAG, "handleDisConnect moduleName: " + localModule + " resultCode: " + resultCode);
        return resultCode;
    }

    /* access modifiers changed from: private */
    public static class AcceptConnectArgs {
        final DataCallback callback;
        final String deviceId;
        final String localModule;
        final String remoteModule;

        AcceptConnectArgs(String localModuleArg, String deviceIdArg, String remoteModuleArg, DataCallback callbackArg) {
            this.localModule = localModuleArg;
            this.deviceId = deviceIdArg;
            this.remoteModule = remoteModuleArg;
            this.callback = callbackArg;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleAcceptConnect(AcceptConnectArgs args) {
        int resultCode = 0;
        rebindService();
        if (args == null || this.mService == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: handleAcceptConnect args is null");
            return 1;
        }
        Log.i(TAG, "handleAcceptConnect");
        String localModule = args.localModule;
        try {
            resultCode = this.mService.acceptConnect(localModule, args.deviceId, args.remoteModule, new DataListener(args.callback));
        } catch (RemoteException e) {
            Log.e(TAG, "F_RESULT_CODE:8reason: handleAcceptConnectRemoteException");
        }
        Log.i(TAG, "handleAcceptConnect moduleName: " + localModule + " resultCode: " + resultCode);
        return resultCode;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleRejectConnect(DisConnectArgs args) {
        int resultCode = 0;
        rebindService();
        if (args == null || this.mService == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: handleRejectConnect args is null");
            return 1;
        }
        Log.i(TAG, "handleRejectConnect");
        String localModule = args.localModule;
        try {
            resultCode = this.mService.rejectConnect(localModule, args.deviceId, args.remoteModule);
        } catch (RemoteException e) {
            Log.e(TAG, "F_RESULT_CODE:8reason: handleRejectConnectRemoteException");
        }
        Log.i(TAG, "handleRejectConnect moduleName: " + localModule + " resultCode: " + resultCode);
        return resultCode;
    }

    /* access modifiers changed from: private */
    public static class SendByteArgs {
        final byte[] data;
        final String deviceId;
        final String extInfo;
        final int length;
        final String localModule;
        final String remoteModule;

        SendByteArgs(String localModuleArg, String deviceIdArg, String remoteModuleArg, byte[] dataArg, int lengthArg, String extInfoArg) {
            this.localModule = localModuleArg;
            this.deviceId = deviceIdArg;
            this.remoteModule = remoteModuleArg;
            this.data = dataArg;
            this.length = lengthArg;
            this.extInfo = extInfoArg;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleSendByte(SendByteArgs args) {
        int resultCode = 0;
        rebindService();
        if (args == null || this.mService == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: handleSendByte args is null");
            return 1;
        }
        Log.i(TAG, "handleSendByte");
        String localModule = args.localModule;
        try {
            try {
                resultCode = this.mService.sendByte(localModule, args.deviceId, args.remoteModule, args.data, args.length, args.extInfo);
            } catch (RemoteException e) {
                Log.e(TAG, "F_RESULT_CODE:8reason: handleSendByte RemoteException");
                Log.i(TAG, "handleSendByte moduleName: " + localModule + " resultCode: " + resultCode);
                return resultCode;
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "F_RESULT_CODE:8reason: handleSendByte RemoteException");
            Log.i(TAG, "handleSendByte moduleName: " + localModule + " resultCode: " + resultCode);
            return resultCode;
        }
        Log.i(TAG, "handleSendByte moduleName: " + localModule + " resultCode: " + resultCode);
        return resultCode;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleSendBlock(SendByteArgs args) {
        int resultCode = 0;
        rebindService();
        if (args == null || this.mService == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: handleSendBlock args is null");
            return 1;
        }
        Log.i(TAG, "handleSendBlock");
        String localModule = args.localModule;
        try {
            try {
                resultCode = this.mService.sendBlock(localModule, args.deviceId, args.remoteModule, args.data, args.length, args.extInfo);
            } catch (RemoteException e) {
                Log.e(TAG, "F_RESULT_CODE:8reason: handleSendBlock RemoteException");
                Log.i(TAG, "handleSendBlock moduleName: " + localModule + " resultCode: " + resultCode);
                return resultCode;
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "F_RESULT_CODE:8reason: handleSendBlock RemoteException");
            Log.i(TAG, "handleSendBlock moduleName: " + localModule + " resultCode: " + resultCode);
            return resultCode;
        }
        Log.i(TAG, "handleSendBlock moduleName: " + localModule + " resultCode: " + resultCode);
        return resultCode;
    }

    /* access modifiers changed from: private */
    public static class SendFileArgs {
        final String destFilePath;
        final String deviceId;
        final String extInfo;
        final String localModule;
        final String remoteModule;
        final String sourceFile;

        SendFileArgs(String localModuleArg, String deviceIdArg, String remoteModuleArg, String sourceFileArg, String destFilePathArg, String extInfoArg) {
            this.localModule = localModuleArg;
            this.deviceId = deviceIdArg;
            this.remoteModule = remoteModuleArg;
            this.sourceFile = sourceFileArg;
            this.destFilePath = destFilePathArg;
            this.extInfo = extInfoArg;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleSendFile(SendFileArgs args) {
        int resultCode = 0;
        rebindService();
        if (args == null || this.mService == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: handleSendFile args is null");
            return 1;
        }
        Log.i(TAG, "handleSendFile");
        String localModule = args.localModule;
        try {
            try {
                resultCode = this.mService.sendFile(localModule, args.deviceId, args.remoteModule, args.sourceFile, args.destFilePath, args.extInfo);
            } catch (RemoteException e) {
                Log.e(TAG, "F_RESULT_CODE:8reason: handleSendFile RemoteException");
                Log.i(TAG, "handleSendFile moduleName: " + localModule + " resultCode: " + resultCode);
                return resultCode;
            }
        } catch (RemoteException e2) {
            Log.e(TAG, "F_RESULT_CODE:8reason: handleSendFile RemoteException");
            Log.i(TAG, "handleSendFile moduleName: " + localModule + " resultCode: " + resultCode);
            return resultCode;
        }
        Log.i(TAG, "handleSendFile moduleName: " + localModule + " resultCode: " + resultCode);
        return resultCode;
    }

    /* access modifiers changed from: private */
    public static class SendStreamArgs {
        final String deviceId;
        final String extInfo;
        final String localModule;
        final String remoteModule;
        final DataPayload stream;

        SendStreamArgs(String localModuleArg, String deviceIdArg, String remoteModuleArg, DataPayload streamArg, String extInfoArg) {
            this.localModule = localModuleArg;
            this.deviceId = deviceIdArg;
            this.remoteModule = remoteModuleArg;
            this.stream = streamArg;
            this.extInfo = extInfoArg;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleSendStream(SendStreamArgs args) {
        int resultCode = 0;
        rebindService();
        if (args == null || this.mService == null || args.stream == null) {
            Log.e(TAG, "F_RESULT_CODE:1reason: handleSendStream args is null");
            return 1;
        }
        Log.i(TAG, "handleSendStream");
        String localModule = args.localModule;
        try {
            resultCode = this.mService.sendStream(localModule, args.deviceId, args.remoteModule, args.stream, args.extInfo);
        } catch (RemoteException e) {
            Log.e(TAG, "F_RESULT_CODE:8reason: handleSendStream RemoteException");
        }
        Log.i(TAG, "handleSendStream moduleName: " + localModule + " resultCode: " + resultCode);
        return resultCode;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int handleDisconnectAll() {
        int resultCode = 0;
        rebindService();
        if (this.mService == null || TextUtils.isEmpty(this.mModuleId)) {
            Log.e(TAG, "F_RESULT_CODE:1reason: handleDisConnectAll args is null");
            return 1;
        }
        Log.e(TAG, "handleDisConnectAll");
        try {
            resultCode = this.mService.disconnectAll(this.mModuleId);
        } catch (RemoteException e) {
            Log.e(TAG, "F_RESULT_CODE:8reason: handleDisConnectAllRemoteException");
        }
        Log.i(TAG, "handleDisConnectAll mModuleId: " + this.mModuleId + " resultCode: " + resultCode);
        return resultCode;
    }

    /* access modifiers changed from: private */
    public class ConnectionListener extends IConnectionCallback.Stub {
        public ConnectionCallback connectionCallback;

        public ConnectionListener(ConnectionCallback listener) {
            this.connectionCallback = listener;
        }

        public void onConnectionInit(String remoteDeviceId, String remoteModuleName, String para) throws RemoteException {
            Log.i(HwConnection.TAG, "F_RESULT_CODE:13reason: onConnectionInit");
            this.connectionCallback.onConnectionInit(remoteDeviceId, remoteModuleName, para);
        }

        public void onConnectionStateUpdate(String remoteDeviceId, String remoteModuleName, int state, String para) throws RemoteException {
            Log.i(HwConnection.TAG, "F_RESULT_CODE:14reason: onConnectStateUpdate state: " + state);
            this.connectionCallback.onConnectionStateUpdate(remoteDeviceId, remoteModuleName, state, para);
        }
    }

    /* access modifiers changed from: private */
    public class DataListener extends IDataCallback.Stub {
        public DataCallback dataCallback;

        public DataListener(DataCallback listener) {
            this.dataCallback = listener;
        }

        public int onByteReceive(String remoteDeviceId, String remoteModuleName, byte[] data, int len, String para) throws RemoteException {
            Log.i(HwConnection.TAG, "F_RESULT_CODE:16reason: onByteReceive");
            return this.dataCallback.onByteReceive(remoteDeviceId, remoteModuleName, data, len, para);
        }

        public int onBlockReceive(String remoteDeviceId, String remoteModuleName, byte[] data, int len, String para) throws RemoteException {
            Log.i(HwConnection.TAG, "F_RESULT_CODE:17reason: onBlockReceive");
            return this.dataCallback.onBlockReceive(remoteDeviceId, remoteModuleName, data, len, para);
        }

        public int onFileReceive(String remoteDeviceId, String remoteModuleName, String file, String para) throws RemoteException {
            Log.i(HwConnection.TAG, "F_RESULT_CODE:18reason: onFileReceive");
            return this.dataCallback.onFileReceive(remoteDeviceId, remoteModuleName, file, para);
        }

        public int onStreamReceive(String remoteDeviceId, String remoteModuleName, DataPayload streamPayload, String para) throws RemoteException {
            Log.i(HwConnection.TAG, "F_RESULT_CODE:19reason: onStreamReceive");
            return this.dataCallback.onStreamReceive(remoteDeviceId, remoteModuleName, streamPayload, para);
        }

        public int onSendFileStateUpdate(String remoteDeviceId, String remoteModuleName, int state, String para) throws RemoteException {
            Log.i(HwConnection.TAG, "F_RESULT_CODE:20reason: onSendFileStateUpdate");
            return this.dataCallback.onSendFileStateUpdate(remoteDeviceId, remoteModuleName, state, para);
        }

        public String onCommonUpdate(String para) throws RemoteException {
            Log.i(HwConnection.TAG, "F_RESULT_CODE:12reason: onDataUpdate");
            return this.dataCallback.onCommonUpdate(para);
        }
    }

    /* access modifiers changed from: private */
    public class DiscoveryListener extends IDiscoveryCallback.Stub {
        public DiscoveryCallback discoveryCallback;

        public DiscoveryListener(DiscoveryCallback listener) {
            this.discoveryCallback = listener;
        }

        public void onDeviceFound(IDeviceDesc deviceDesc) throws RemoteException {
            Log.i(HwConnection.TAG, "F_RESULT_CODE:9");
            this.discoveryCallback.onDeviceFound(HwConnection.this.getDeviceDesc(deviceDesc));
        }

        public void onDeviceLost(IDeviceDesc deviceDesc) throws RemoteException {
            Log.i(HwConnection.TAG, "F_RESULT_CODE:10");
            this.discoveryCallback.onDeviceLost(HwConnection.this.getDeviceDesc(deviceDesc));
        }
    }

    private List<IServiceDesc> getIServiceDescs(List<ServiceDesc> serviceDescs) {
        List<IServiceDesc> nearDescs = new ArrayList<>();
        if (serviceDescs == null) {
            return nearDescs;
        }
        int size = serviceDescs.size();
        for (int i = 0; i < size; i++) {
            nearDescs.add(getIServiceDesc(serviceDescs.get(i)));
        }
        return nearDescs;
    }

    private IPublishOption getIPublishOption(PublishOption option) {
        if (option == null) {
            return null;
        }
        return new IPublishOption.Builder().strategy(getIStrategy(option.getStrategy())).powerPolicy(getIPowerPolicy(option.getPowerPolicy())).serviceFilters(getIServiceFilters(option.getServiceFilters())).infos(getIServiceDescs(option.getInfos())).publishMode(option.getPublishMode()).extInfo(option.getExtInfo()).timeout(option.getTimeout()).count(option.getCount()).build();
    }

    private IDiscoveryOption getIDiscoveryOption(DiscoveryOption option) {
        if (option == null) {
            return null;
        }
        return new IDiscoveryOption.Builder().count(option.getCount()).infos(getIServiceDescs(option.getInfos())).strategy(getIStrategy(option.getStrategy())).timeout(option.getTimeout()).serviceFilters(getIServiceFilters(option.getServiceFilters())).powerPolicy(getIPowerPolicy(option.getPowerPolicy())).discoveryMode(option.getDiscoveryMode()).extInfo(option.getExtInfo()).build();
    }

    private IConnectOption getIConnectOption(ConnectOption option) {
        if (option == null) {
            return null;
        }
        return new IConnectOption.Builder().serviceId(option.getServiceId()).opt(option.getOption()).extInfo(option.getExtInfo()).strategy(getIStrategy(option.getStrategy())).build();
    }

    private IServiceDesc getIServiceDesc(ServiceDesc option) {
        if (option == null) {
            return null;
        }
        return new IServiceDesc.Builder().serviceId(option.getServiceId()).serviceName(option.getServiceName()).serviceData(option.getServiceData()).build();
    }

    private List<IServiceFilter> getIServiceFilters(List<ServiceFilter> serviceFilters) {
        List<IServiceFilter> nearFilters = new ArrayList<>();
        if (serviceFilters == null) {
            return nearFilters;
        }
        int size = serviceFilters.size();
        for (int i = 0; i < size; i++) {
            nearFilters.add(getIServiceFilter(serviceFilters.get(i)));
        }
        return nearFilters;
    }

    private IServiceFilter getIServiceFilter(ServiceFilter option) {
        if (option == null) {
            return null;
        }
        return new IServiceFilter.Builder().serviceId(option.getServiceId()).filterData(option.getFilterData()).filterMask(option.getFilterMask()).build();
    }

    private IDevConfig getIDevConfig(DevConfig option) {
        if (option == null) {
            return null;
        }
        return new IDevConfig.Builder().netRole(getINetRole(option.getNetRole())).build();
    }

    private IStrategy getIStrategy(Strategy option) {
        if (option == null) {
            return null;
        }
        return IStrategy.values()[option.ordinal()];
    }

    private IPowerPolicy getIPowerPolicy(PowerPolicy option) {
        if (option == null) {
            return null;
        }
        return IPowerPolicy.values()[option.ordinal()];
    }

    private INetRole getINetRole(NetRole option) {
        if (option == null) {
            return null;
        }
        return INetRole.values()[option.ordinal()];
    }

    private ConnectionDesc getConnectionDesc(IConnectionDesc option) {
        if (option == null) {
            return null;
        }
        return new ConnectionDesc.Builder().isIncomming(option.getIsIncomming()).fd(option.getFd()).build();
    }

    private ConnectionResult getConnectionResult(IConnectionResult option) {
        if (option == null) {
            return null;
        }
        return new ConnectionResult.Builder().status(option.getStatus()).resultData(option.getResultData()).build();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private DeviceDesc getDeviceDesc(IDeviceDesc option) {
        if (option == null) {
            return null;
        }
        return new DeviceDesc.Builder().deviceName(option.getDeviceName()).deviceId(option.getDeviceId()).deviceType(option.getDeviceType()).wifiMac(option.getWifiMac()).btMac(option.getBtMac()).ipv4(option.getIpv4()).ipv6(option.getIpv6()).port(option.getPort()).capabilityBitmapNum(option.getCapabilityBitmapNum()).capabilityBitmap(option.getCapabilityBitmap()).reservedInfo(option.getReservedInfo()).build();
    }

    private boolean checkModuleName(String moduleName) {
        if (TextUtils.isEmpty(moduleName)) {
            Log.i(TAG, "checkModuleName, moduleName is null or empty");
            return false;
        } else if (moduleName.contains("+")) {
            return true;
        } else {
            Log.i(TAG, "checkModuleName, moduleName type is not [packageName+moduleId]");
            return false;
        }
    }
}
