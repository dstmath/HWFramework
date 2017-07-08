package com.android.server.security.trustcircle;

import android.os.RemoteException;
import com.android.server.security.trustcircle.AuthPara.RespPkInfo;
import com.android.server.security.trustcircle.task.HwSecurityEvent;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.EventListener;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.RetCallback;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.TimerOutProc;
import com.android.server.security.trustcircle.task.HwSecurityTimerTask;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.security.IAuthCallback;

public class ServiceReqPkTask extends HwSecurityTaskBase {
    private static final String TAG = null;
    private static final long TIME_OUT = 5000;
    private long mAuthID;
    EventListener mCancelListener;
    EventListener mRecPkListener;
    private RespPkInfo mRespPkInfo;
    TimerOutProc mTimeoutProc;
    private HwSecurityTimerTask mTimer;
    private int mType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.ServiceReqPkTask.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.ServiceReqPkTask.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.ServiceReqPkTask.<clinit>():void");
    }

    public ServiceReqPkTask(HwSecurityTaskBase parent, RetCallback callback, long authID, int type) {
        super(parent, callback);
        this.mCancelListener = new EventListener() {
            public boolean onEvent(HwSecurityEvent ev) {
                long authID = ((CancelAuthEv) ev).getAuthID();
                if (authID != ServiceReqPkTask.this.mAuthID && authID != -2) {
                    return false;
                }
                LogHelper.i(ServiceReqPkTask.TAG, "onCancelAuthEvent, authID: " + authID);
                ServiceReqPkTask.this.mTimer.cancel();
                ServiceReqPkTask.this.endWithResult(2);
                return true;
            }
        };
        this.mRecPkListener = new EventListener() {
            public boolean onEvent(HwSecurityEvent ev) {
                ServiceReqPkTask.this.mRespPkInfo = ((ReceivePkEv) ev).getPkInfo();
                LogHelper.i(ServiceReqPkTask.TAG, "onReceivePkEvent, authID: " + ServiceReqPkTask.this.mRespPkInfo.mAuthID + ", mAuthID: " + ServiceReqPkTask.this.mAuthID);
                if (ServiceReqPkTask.this.mAuthID != ServiceReqPkTask.this.mRespPkInfo.mAuthID) {
                    return false;
                }
                ServiceReqPkTask.this.mTimer.cancel();
                ServiceReqPkTask.this.endWithResult(0);
                return true;
            }
        };
        this.mTimeoutProc = new TimerOutProc() {
            public void onTimerOut() {
                ServiceReqPkTask.this.endWithResult(1);
            }
        };
        this.mTimer = new HwSecurityTimerTask();
        this.mAuthID = authID;
        this.mType = type;
    }

    public int doAction() {
        IAuthCallback callback = IOTController.getInstance().getIOTCallback(this.mAuthID, this.mType);
        if (callback != null) {
            try {
                callback.requestPK();
                LogHelper.i(TAG, "waiting for receivePk...");
                this.mTimer.setTimeout(TIME_OUT, this.mTimeoutProc);
                return -1;
            } catch (RemoteException e) {
                LogHelper.e(TAG, e.toString());
            } catch (Exception e2) {
                LogHelper.e(TAG, e2.toString());
            }
        }
        return 3;
    }

    public RespPkInfo getRespPkInfo() {
        return this.mRespPkInfo;
    }

    public void onStart() {
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
        HwSecurityMsgCenter.staticRegisterEvent(WifiProCommonUtils.HISTORY_TYPE_PORTAL, this, this.mRecPkListener);
    }

    public void onStop() {
        HwSecurityMsgCenter.staticUnregisterEvent(100, this);
        HwSecurityMsgCenter.staticUnregisterEvent(WifiProCommonUtils.HISTORY_TYPE_PORTAL, this);
    }
}
