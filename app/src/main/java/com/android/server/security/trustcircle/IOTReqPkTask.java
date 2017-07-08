package com.android.server.security.trustcircle;

import android.os.RemoteException;
import com.android.server.security.trustcircle.AuthPara.ReqPkInfo;
import com.android.server.security.trustcircle.AuthPara.RespPkInfo;
import com.android.server.security.trustcircle.task.HwSecurityEvent;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.EventListener;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.RetCallback;
import com.android.server.security.trustcircle.utils.AuthUtils;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.IAuthCallback;

public class IOTReqPkTask extends HwSecurityTaskBase {
    private static final String TAG = null;
    EventListener mCancelListener;
    private ReqPkInfo mReqPkInfo;
    private int mType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.IOTReqPkTask.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.IOTReqPkTask.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.IOTReqPkTask.<clinit>():void");
    }

    public IOTReqPkTask(HwSecurityTaskBase parent, RetCallback callback, ReqPkInfo reqPkInfo, int type) {
        super(parent, callback);
        this.mCancelListener = new EventListener() {
            public boolean onEvent(HwSecurityEvent ev) {
                long authID = ((CancelAuthEv) ev).getAuthID();
                if (authID != IOTReqPkTask.this.mReqPkInfo.mAuthID && authID != -2) {
                    return false;
                }
                LogHelper.i(IOTReqPkTask.TAG, "onCancelAuthEvent, authID: " + authID);
                IOTReqPkTask.this.endWithResult(2);
                return true;
            }
        };
        this.mReqPkInfo = reqPkInfo;
        this.mType = type;
    }

    public int doAction() {
        RespPkInfo pkInfo = AuthUtils.processGetPk(this.mReqPkInfo);
        IAuthCallback callback = IOTController.getInstance().getIOTCallback(this.mReqPkInfo.mAuthID, this.mType);
        if (callback != null) {
            try {
                callback.responsePK(pkInfo.mAuthID, pkInfo.mAuthKeyAlgoType, pkInfo.mAuthKeyData, pkInfo.mAuthKeyDataSign);
                return 0;
            } catch (RemoteException e) {
                LogHelper.e(TAG, e.toString());
            } catch (Exception e2) {
                LogHelper.e(TAG, e2.toString());
            }
        }
        return 3;
    }

    public void onStart() {
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
    }

    public void onStop() {
        HwSecurityMsgCenter.staticUnregisterEvent(100, this);
    }
}
