package ohos.dcall;

import ohos.annotation.SystemApi;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class DistributedCallManager {
    private static final int PERMISSION_GRANTED = 0;
    private static final String PERMISSION_READ_CALL_LOG = "ohos.permission.READ_CALL_LOG";
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) DistributedCallUtils.LOG_ID_DCALL, "DistributedCallManager");
    private static volatile DistributedCallManager sInstance;
    private final Context mContext;
    private final DistributedCallProxy mDistributedCallProxy = DistributedCallProxy.getInstance();

    private DistributedCallManager(Context context) {
        this.mContext = context;
    }

    public static DistributedCallManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DistributedCallManager.class) {
                if (sInstance == null) {
                    sInstance = new DistributedCallManager(context);
                }
            }
        }
        return sInstance;
    }

    public boolean hasCall() {
        return this.mDistributedCallProxy.hasCall();
    }

    public boolean dial(String str, boolean z) {
        return this.mDistributedCallProxy.dial(str, z);
    }

    public void displayCallScreen(boolean z) {
        this.mDistributedCallProxy.displayCallScreen(z);
    }

    @SystemApi
    public void muteRinger() {
        this.mDistributedCallProxy.muteRinger();
    }

    public int getCallState() {
        return this.mDistributedCallProxy.getCallState();
    }

    public boolean isVoiceCap() {
        return this.mDistributedCallProxy.isVoiceCap();
    }

    public void addObserver(CallStateObserver callStateObserver, int i) {
        boolean z = false;
        if (callStateObserver == null) {
            HiLog.error(TAG, "addObserver null param", new Object[0]);
            return;
        }
        if (i == 4) {
            Context context = this.mContext;
            if (context != null && context.verifySelfPermission(PERMISSION_READ_CALL_LOG) == 0) {
                z = true;
            }
            callStateObserver.setReadCallLogPermission(z);
        }
        this.mDistributedCallProxy.addCallObserver(callStateObserver.slotId, callStateObserver.callback, i);
    }

    public void removeObserver(CallStateObserver callStateObserver) {
        if (callStateObserver != null) {
            this.mDistributedCallProxy.removeCallObserver(callStateObserver.slotId, callStateObserver.callback);
        }
    }

    public boolean isVideoCallingEnabled() {
        return this.mDistributedCallProxy.isVideoCallingEnabled();
    }

    @SystemApi
    public void inputDialerSpecialCode(String str) {
        this.mDistributedCallProxy.inputDialerSpecialCode(str);
    }
}
