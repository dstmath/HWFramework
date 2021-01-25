package com.huawei.server.security.securitydiagnose;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.HiLog;
import android.util.HiLogLabel;
import com.huawei.hwstp.HwStpHidlAdapter;

public class AppLayerStpProxy {
    private static final int DOMAIN = 218115848;
    private static final HiLogLabel HILOG_LABEL = new HiLogLabel(3, (int) DOMAIN, TAG);
    private static final int[] ITEM_ID = {STP_ID_KCODE, STP_ID_KCODE_SYSCALL, STP_ID_SE_HOOK, STP_ID_SE_ENFROCING, STP_ID_SU, STP_ID_RW, 512, STP_ID_PROP, STP_ID_SETIDS, STP_ID_ROOT_PROCS};
    private static final int[] ITEM_MARK = {1, 2, 4, 8, 16, 32, 128, HwSecDiagnoseConstant.BIT_SYS_PROPS, 512, HwSecDiagnoseConstant.BIT_RPROC};
    private static final int MAX_STP_HIDL_DEAMON_REGISTER_TIMES = 10;
    private static final int MSG_STP_HIDL_DEAMON_SERVIE_REGISTER = 1;
    private static final int RET_DEFAULT_ERROR_VALUE = -1001;
    private static final int RET_EXCEPTION_WHEN_STP_CALL = -1002;
    private static final int RET_STP_HIDL_DEAMON_IS_NOT_READY = -1000;
    private static final String STP_HIDL_MATCH_CREDIBLE = "credible: Y";
    private static final String STP_HIDL_MATCH_ROOTPROC = "root-procs";
    private static final String STP_HIDL_MATCH_VB = "verifyboot";
    private static final String STP_HIDL_MATCH_VB_RISK = "RISK";
    private static final String STP_HIDL_SERVICE_NAME = "hwstp";
    private static final int STP_ID_KCODE = 642;
    private static final int STP_ID_KCODE_SYSCALL = 643;
    private static final int STP_ID_PROP = 768;
    private static final int STP_ID_ROOT_PROCS = 901;
    private static final int STP_ID_RW = 773;
    private static final int STP_ID_SETIDS = 896;
    private static final int STP_ID_SE_ENFROCING = 899;
    private static final int STP_ID_SE_HOOK = 644;
    private static final int STP_ID_SU = 520;
    private static final int STP_ID_VERIFYBOOT = 512;
    private static final int STP_ITEM_ROOT = 1;
    private static final Object STP_LOCK = new Object();
    private static final String TAG = AppLayerStpProxy.class.getSimpleName();
    private static final int TRY_GET_HIDL_DEAMON_DEALY_MILLIS = 1000;
    private static AppLayerStpProxy sInstance;
    private Context mContext;
    private final HwStpHandler mHwStpHandler;
    private final HandlerThread mHwStpHandlerThread;
    private HwStpHidlAdapter mHwStpHidlAdapter;
    private final HwStpHidlServiceDiedCallback mServiceDiedCallback = new HwStpHidlServiceDiedCallback();
    private int mStpGetItemStatusRetValue = 0;
    private final StpGetStatusByCategoryCallback mStpGetStatusByCategoryCallback = new StpGetStatusByCategoryCallback();
    private String mStpGetStatusByCategoryOutBuff;
    private int mStpGetStatusByCategoryRetValue = RET_DEFAULT_ERROR_VALUE;
    private int mStpGetStatusByIDRetValue = RET_DEFAULT_ERROR_VALUE;
    private final StpGetStatusByIdCallback mStpGetStatusByIdCallback = new StpGetStatusByIdCallback();
    private final StpGetStatusCallback mStpGetStatusCallback = new StpGetStatusCallback();
    private int mStpGetStatusRetValue = RET_DEFAULT_ERROR_VALUE;
    private int mStpHidlDeamonRegisterTryTimes = 0;

    static /* synthetic */ int access$1508(AppLayerStpProxy x0) {
        int i = x0.mStpHidlDeamonRegisterTryTimes;
        x0.mStpHidlDeamonRegisterTryTimes = i + 1;
        return i;
    }

    /* access modifiers changed from: private */
    public class StpGetStatusCallback implements HwStpHidlAdapter.StpGetStatusCallbackWrapper {
        private StpGetStatusCallback() {
        }

        public void onValues(int stpGetStatusRet, String outBuffer) {
            HiLog.debug(AppLayerStpProxy.HILOG_LABEL, "sync get system status from hidl ret : %{public}d, outBuffer : %{public}s", new Object[]{Integer.valueOf(stpGetStatusRet), outBuffer});
            AppLayerStpProxy.this.mStpGetStatusRetValue = stpGetStatusRet;
        }
    }

    /* access modifiers changed from: private */
    public class StpGetStatusByCategoryCallback implements HwStpHidlAdapter.StpGetStatusByCategoryCallbackWrapper {
        private StpGetStatusByCategoryCallback() {
        }

        public void onValues(int stpGetStatusByCategoryRet, String stpOutBuff) {
            HiLog.debug(AppLayerStpProxy.HILOG_LABEL, "sync get root status by category from hidl ret : %{public}d stpOutBuff : %{public}s", new Object[]{Integer.valueOf(stpGetStatusByCategoryRet), stpOutBuff});
            AppLayerStpProxy.this.mStpGetStatusByCategoryRetValue = stpGetStatusByCategoryRet;
            AppLayerStpProxy.this.mStpGetStatusByCategoryOutBuff = stpOutBuff;
        }
    }

    private class StpGetStatusByIdCallback implements HwStpHidlAdapter.StpGetStatusByIdCallbackWrapper {
        private StpGetStatusByIdCallback() {
        }

        public void onValues(int stpGetStatusByIdRet, String outBuffer) {
            if (outBuffer == null) {
                HiLog.error(AppLayerStpProxy.HILOG_LABEL, "parameter outBuffer is null", new Object[0]);
            } else {
                HiLog.debug(AppLayerStpProxy.HILOG_LABEL, "sync get item root status from hidl ret : %{public}d outBuffer : %{public}s", new Object[]{Integer.valueOf(stpGetStatusByIdRet), outBuffer});
                AppLayerStpProxy.this.checkAdbd(outBuffer);
                AppLayerStpProxy.this.checkVerifyboot(outBuffer);
            }
            HiLog.debug(AppLayerStpProxy.HILOG_LABEL, "mStpGetStatusByIDRetValue is %{public}d", new Object[]{Integer.valueOf(AppLayerStpProxy.this.mStpGetStatusByIDRetValue)});
            AppLayerStpProxy.this.mStpGetStatusByIDRetValue = stpGetStatusByIdRet;
        }
    }

    /* access modifiers changed from: private */
    public class HwStpHidlServiceDiedCallback implements HwStpHidlAdapter.HwStpHidlServiceDiedCallbackWrapper {
        private HwStpHidlServiceDiedCallback() {
        }

        public void onServiceDied() {
            if (AppLayerStpProxy.this.mHwStpHandler != null) {
                HiLog.error(AppLayerStpProxy.HILOG_LABEL, "stp hidl deamon service has died, try to reconnect it later.", new Object[0]);
                AppLayerStpProxy.this.mHwStpHandler.sendEmptyMessageDelayed(1, 1000);
            }
            synchronized (AppLayerStpProxy.STP_LOCK) {
                AppLayerStpProxy.this.mHwStpHidlAdapter = null;
            }
        }
    }

    private AppLayerStpProxy(Context context) {
        this.mContext = context;
        this.mHwStpHandlerThread = new HandlerThread(TAG);
        this.mHwStpHandlerThread.start();
        this.mHwStpHandler = new HwStpHandler(this.mHwStpHandlerThread.getLooper());
    }

    public static void init(Context context) {
        synchronized (STP_LOCK) {
            if (sInstance == null) {
                sInstance = new AppLayerStpProxy(context);
            }
        }
    }

    public static AppLayerStpProxy getInstance() {
        AppLayerStpProxy appLayerStpProxy;
        synchronized (STP_LOCK) {
            appLayerStpProxy = sInstance;
        }
        return appLayerStpProxy;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private HwStpHidlAdapter getHwStpHidlAdapter() {
        synchronized (STP_LOCK) {
            if (this.mHwStpHidlAdapter != null) {
                return this.mHwStpHidlAdapter;
            }
            this.mHwStpHidlAdapter = new HwStpHidlAdapter();
            if (!this.mHwStpHidlAdapter.isServiceConnected()) {
                this.mHwStpHidlAdapter = null;
                return this.mHwStpHidlAdapter;
            }
            if (!this.mHwStpHidlAdapter.isLinkToDeath(this.mServiceDiedCallback)) {
                HiLog.error(HILOG_LABEL, "hwstp link to death failed", new Object[0]);
            }
            return this.mHwStpHidlAdapter;
        }
    }

    /* access modifiers changed from: private */
    public final class HwStpHandler extends Handler {
        public HwStpHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                HiLog.error(AppLayerStpProxy.HILOG_LABEL, "handler thread received unknown message : %{public}d", new Object[]{Integer.valueOf(msg.what)});
                return;
            }
            if (AppLayerStpProxy.this.getHwStpHidlAdapter() != null) {
                AppLayerStpProxy.this.mStpHidlDeamonRegisterTryTimes = 0;
            } else {
                AppLayerStpProxy.access$1508(AppLayerStpProxy.this);
                if (AppLayerStpProxy.this.mStpHidlDeamonRegisterTryTimes < 10) {
                    HiLog.error(AppLayerStpProxy.HILOG_LABEL, "stp hidl daemon service is not ready, try times : %{public}d", new Object[]{Integer.valueOf(AppLayerStpProxy.this.mStpHidlDeamonRegisterTryTimes)});
                    AppLayerStpProxy.this.mHwStpHandler.sendEmptyMessageDelayed(1, 1000);
                } else {
                    HiLog.error(AppLayerStpProxy.HILOG_LABEL, "stp hidl daemon service connection failed.", new Object[0]);
                }
            }
            HiLog.debug(AppLayerStpProxy.HILOG_LABEL, "handler thread received request stp hidl deamon message.", new Object[0]);
        }
    }

    public int getRootStatusSync() {
        int ret;
        int category = HwStpHidlAdapter.STP_ROOT_THREAT;
        HwStpHidlAdapter hwStpHidlAdapter = getHwStpHidlAdapter();
        if (hwStpHidlAdapter == null) {
            ret = RET_STP_HIDL_DEAMON_IS_NOT_READY;
            this.mHwStpHandler.sendEmptyMessageDelayed(1, 1000);
            HiLog.error(HILOG_LABEL, "stp hidl deamon is not ready when sync get root status from stp hidl", new Object[0]);
        } else if (hwStpHidlAdapter.stpGetStatusByCategory(category, false, false, this.mStpGetStatusByCategoryCallback) != RET_EXCEPTION_WHEN_STP_CALL) {
            ret = this.mStpGetStatusByCategoryRetValue;
        } else {
            ret = RET_EXCEPTION_WHEN_STP_CALL;
            HiLog.error(HILOG_LABEL, "sync get root status from stp hidl failed.", new Object[0]);
        }
        HiLog.debug(HILOG_LABEL, "sync get root status , stp hidl ret : %{public}d", new Object[]{Integer.valueOf(ret)});
        return ret;
    }

    public int getSystemStatusSync() {
        int ret;
        HwStpHidlAdapter hwStpHidlAdapter = getHwStpHidlAdapter();
        if (hwStpHidlAdapter == null) {
            ret = RET_STP_HIDL_DEAMON_IS_NOT_READY;
            this.mHwStpHandler.sendEmptyMessageDelayed(1, 1000);
            HiLog.error(HILOG_LABEL, "stp hidl deamon is not ready when sync get system status from stp hidl", new Object[0]);
        } else if (hwStpHidlAdapter.stpGetStatus(false, false, this.mStpGetStatusCallback) != RET_EXCEPTION_WHEN_STP_CALL) {
            ret = this.mStpGetStatusRetValue;
        } else {
            ret = RET_EXCEPTION_WHEN_STP_CALL;
            HiLog.error(HILOG_LABEL, "sync get system status from stp hidl failed.", new Object[0]);
        }
        HiLog.debug(HILOG_LABEL, "sync get system status , stp hidl ret : %{public}d", new Object[]{Integer.valueOf(ret)});
        return ret;
    }

    public int sendThreatenInfo(int id, byte status, byte credible, byte version, String name, String addition) {
        int ret;
        HiLog.debug(HILOG_LABEL, "receive the app's threaten info to stp hidl deamon , status: %{public}d credible: %{public}d name:%{public}s addition:%{public}s", new Object[]{Byte.valueOf(status), Byte.valueOf(credible), name, addition});
        HwStpHidlAdapter hwStpHidlAdapter = getHwStpHidlAdapter();
        if (hwStpHidlAdapter != null) {
            ret = hwStpHidlAdapter.stpAddThreat(id, status, credible, version, name, addition);
            if (ret == RET_EXCEPTION_WHEN_STP_CALL) {
                ret = RET_EXCEPTION_WHEN_STP_CALL;
                HiLog.error(HILOG_LABEL, "sync send threaten info to stp hidl failed.", new Object[0]);
            }
        } else {
            ret = RET_STP_HIDL_DEAMON_IS_NOT_READY;
            this.mHwStpHandler.sendEmptyMessageDelayed(1, 1000);
            HiLog.error(HILOG_LABEL, "stp hidl deamon is not ready when app send threaten info to stp hidl deamon", new Object[0]);
        }
        HiLog.debug(HILOG_LABEL, "send the app's threaten info to stp hidl deamon, ret : %{public}d", new Object[]{Integer.valueOf(ret)});
        return ret;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkAdbd(String outBuffer) {
        if (outBuffer.contains(STP_HIDL_MATCH_ROOTPROC) && outBuffer.contains(STP_HIDL_MATCH_CREDIBLE)) {
            this.mStpGetItemStatusRetValue |= 64;
            HiLog.debug(HILOG_LABEL, "adbd abnormal, stpGetItemStatusRetValue %{public}d", new Object[]{Integer.valueOf(this.mStpGetItemStatusRetValue)});
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkVerifyboot(String outBuffer) {
        if (outBuffer.contains(STP_HIDL_MATCH_VB) && outBuffer.contains(STP_HIDL_MATCH_VB_RISK)) {
            this.mStpGetItemStatusRetValue |= 128;
            HiLog.debug(HILOG_LABEL, "verifyboot abnormal, stpGetItemStatusRetValue %{public}d", new Object[]{Integer.valueOf(this.mStpGetItemStatusRetValue)});
        }
    }

    public int getEachItemRootStatus() {
        int ret;
        this.mStpGetItemStatusRetValue = 0;
        HwStpHidlAdapter hwStpHidlAdapter = getHwStpHidlAdapter();
        if (hwStpHidlAdapter == null) {
            this.mStpGetItemStatusRetValue = RET_STP_HIDL_DEAMON_IS_NOT_READY;
            this.mHwStpHandler.sendEmptyMessageDelayed(1, 1000);
            HiLog.error(HILOG_LABEL, "stp hidl deamon is not ready when sync get item root status from stp", new Object[0]);
            return this.mStpGetItemStatusRetValue;
        }
        int i = 0;
        while (true) {
            int[] iArr = ITEM_ID;
            if (i >= iArr.length) {
                return this.mStpGetItemStatusRetValue;
            }
            if (hwStpHidlAdapter.stpGetStatusById(iArr[i], true, false, this.mStpGetStatusByIdCallback) != RET_EXCEPTION_WHEN_STP_CALL) {
                ret = this.mStpGetStatusByIDRetValue;
            } else {
                ret = RET_EXCEPTION_WHEN_STP_CALL;
                HiLog.error(HILOG_LABEL, "get each item root status from stp hidl failed. ITEM_ID : %{public}d", new Object[]{Integer.valueOf(ITEM_ID[i])});
            }
            HiLog.debug(HILOG_LABEL, "get item root status , stp hidl ret : %{public}d ITEM_ID : %{public}d", new Object[]{Integer.valueOf(ret), Integer.valueOf(ITEM_ID[i])});
            if (ret < 0) {
                HiLog.debug(HILOG_LABEL, "stp get status by id failed", new Object[0]);
                return ret;
            }
            if (ret == 1) {
                this.mStpGetItemStatusRetValue |= ITEM_MARK[i];
            } else {
                HiLog.info(HILOG_LABEL, "get item root status from stp hidl is normal", new Object[0]);
            }
            HiLog.debug(HILOG_LABEL, "mStpGetItemStatusRetValue ret : %{public}d ITEM_ID : %{public}d", new Object[]{Integer.valueOf(this.mStpGetItemStatusRetValue), Integer.valueOf(ITEM_ID[i])});
            i++;
        }
    }

    public int getStpStatusByCategory(int category, boolean inDetail, boolean withHistory, char[] outBuff, int[] outBuffLen) {
        HwStpHidlAdapter hwStpHidlAdapter = getHwStpHidlAdapter();
        if (hwStpHidlAdapter == null) {
            this.mHwStpHandler.sendEmptyMessageDelayed(1, 1000);
            HiLog.error(HILOG_LABEL, "stp hidl deamon is not ready when get status by category", new Object[0]);
            return RET_STP_HIDL_DEAMON_IS_NOT_READY;
        } else if (hwStpHidlAdapter.stpGetStatusByCategory(category, inDetail, withHistory, this.mStpGetStatusByCategoryCallback) != RET_EXCEPTION_WHEN_STP_CALL) {
            int ret = this.mStpGetStatusByCategoryRetValue;
            char[] temp = this.mStpGetStatusByCategoryOutBuff.toCharArray();
            if ((inDetail || withHistory) && outBuff != null) {
                outBuffLen[0] = outBuff.length <= temp.length ? outBuff.length : temp.length;
                System.arraycopy(temp, 0, outBuff, 0, outBuffLen[0]);
                return ret;
            }
            outBuffLen[0] = 0;
            return ret;
        } else {
            HiLog.error(HILOG_LABEL, "get status by category from stp hidl failed.", new Object[0]);
            return RET_EXCEPTION_WHEN_STP_CALL;
        }
    }
}
