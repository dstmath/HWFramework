package com.android.server.security.trustcircle;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.core.IHwSecurityPlugin.Creator;
import com.android.server.security.trustcircle.jni.TcisJNI;
import com.android.server.security.trustcircle.lifecycle.TcisLifeCycleDispatcher;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskThread;
import com.android.server.security.trustcircle.task.HwSecurityTimer;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.IAuthCallback;
import huawei.android.security.ILifeCycleCallback;
import huawei.android.security.ITrustCircleManager.Stub;

public class TrustCirclePlugin extends Stub implements IHwSecurityPlugin {
    public static final Object BINDLOCK = null;
    public static final Creator CREATOR = null;
    private static final String MANAGE_TRUSTCIRCLE = "com.huawei.permission.USE_TRUSTCIRCLE_MANAGER";
    private static final int MSG_PROCESS_CMD = 10;
    private static final String TAG = null;
    private Context mContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.TrustCirclePlugin.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.TrustCirclePlugin.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.TrustCirclePlugin.<clinit>():void");
    }

    public TrustCirclePlugin(Context context) {
        this.mContext = context;
    }

    public void onStart() {
        HwSecurityMsgCenter.createInstance();
        HwSecurityTaskThread.createInstance();
        HwSecurityTaskThread.getInstance().startThread();
        TcisJNI.start();
    }

    public void onStop() {
        HwSecurityTaskThread.getInstance().stopThread();
        HwSecurityTaskThread.destroyInstance();
        HwSecurityMsgCenter.destroyInstance();
        HwSecurityTimer.destroyInstance();
        TcisJNI.stop();
    }

    public IBinder asBinder() {
        return this;
    }

    public Bundle getTcisInfo() {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        return TcisLifeCycleDispatcher.getInstance().getTcisInfo();
    }

    public int getCurrentState() {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        return TcisLifeCycleDispatcher.getInstance().getCurrentState();
    }

    public void loginServerRequest(ILifeCycleCallback callback, long userID, int serverRegisterStatus, String sessionID) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (callback == null) {
            LogHelper.e(TAG, "error:ILifeCycleCallback is null in loginServerRequest");
        } else {
            TcisLifeCycleDispatcher.getInstance().loginServerRequest(callback, userID, serverRegisterStatus, sessionID);
        }
    }

    public void finalRegister(ILifeCycleCallback callback, String authPKData, String authPKDataSign, String updateIndexInfo, String updateIndexSignature) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (callback == null) {
            LogHelper.e(TAG, "error:ILifeCycleCallback is null in finalRegister");
        } else {
            TcisLifeCycleDispatcher.getInstance().finalRegister(callback, authPKData, authPKDataSign, updateIndexInfo, updateIndexSignature);
        }
    }

    public void finalLogin(ILifeCycleCallback callback, int updateResult, String updateIndexInfo, String updateIndexSignature) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (callback == null) {
            LogHelper.e(TAG, "error:ILifeCycleCallback is null in finalLogin");
        } else {
            TcisLifeCycleDispatcher.getInstance().finalLogin(callback, updateResult, updateIndexInfo, updateIndexSignature);
        }
    }

    public void logout(ILifeCycleCallback callback, long userID) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (callback == null) {
            LogHelper.e(TAG, "error:ILifeCycleCallback is null in logout");
        } else {
            TcisLifeCycleDispatcher.getInstance().logout(callback, userID);
        }
    }

    public void cancelRegOrLogin(ILifeCycleCallback callback, long userID) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (callback == null) {
            LogHelper.e(TAG, "error:ILifeCycleCallback is null in cancelRegOrLogin");
        } else {
            TcisLifeCycleDispatcher.getInstance().cancelRegOrLogin(callback, userID);
        }
    }

    public void unregister(ILifeCycleCallback callback, long userID) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (callback == null) {
            LogHelper.e(TAG, "error:ILifeCycleCallback is null in unregister");
        } else {
            TcisLifeCycleDispatcher.getInstance().unregister(callback, userID);
        }
    }

    public long initAuthenticate(IAuthCallback callback, int authType, int authVersion, int policy, long userID, byte[] AESTmpKey) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        return IOTController.getInstance().initAuth(callback, authType, authVersion, policy, userID, AESTmpKey);
    }

    public long receiveAuthSync(IAuthCallback callback, int authType, int authVersion, int taVersion, int policy, long userID, byte[] AESTmpKey, byte[] tcisId, int pkVersion, long nonce, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        return IOTController.getInstance().receiveAuthSync(callback, authType, authVersion, taVersion, policy, userID, AESTmpKey, tcisId, pkVersion, nonce, authKeyAlgoType, authKeyInfo, authKeyInfoSign);
    }

    public boolean receiveAuthSyncAck(long authID, byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, int authKeyAlgoTypeSlave, byte[] authKeyInfoSlave, byte[] authKeyInfoSignSlave) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        return IOTController.getInstance().receiveAuthSyncAck(authID, tcisIdSlave, pkVersionSlave, nonceSlave, mac, authKeyAlgoTypeSlave, authKeyInfoSlave, authKeyInfoSignSlave);
    }

    public boolean receiveAck(long authID, byte[] mac) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        return IOTController.getInstance().receiveAck(authID, mac);
    }

    public boolean requestPK(long authID, long userID) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        return IOTController.getInstance().requestPK(authID, userID);
    }

    public boolean receivePK(long authID, int authKeyAlgoType, byte[] authKeyData, byte[] authKeyDataSign) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        return IOTController.getInstance().receivePK(authID, authKeyAlgoType, authKeyData, authKeyDataSign);
    }

    public void cancelAuthentication(long authID) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        IOTController.getInstance().cancelAuth(authID);
    }
}
