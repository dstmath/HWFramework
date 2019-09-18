package com.android.server.security.securitydiagnose;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import vendor.huawei.hardware.hwstp.V1_0.IHwStp;
import vendor.huawei.hardware.hwstp.V1_0.StpItem;

public class AppLayerStpProxy {
    /* access modifiers changed from: private */
    public static final boolean HW_DEBUG = (Log.HWINFO || RS_DEBUG || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int MAX_STP_HIDL_DEAMON_REGISTER_TIMES = 10;
    private static final int MSG_STP_HIDL_DEAMON_SERVIE_REGISTER = 1;
    private static final int RET_DEFAULT_ERROR_VALUE = -1001;
    private static final int RET_EXCEPTION_WHEN_STP_CALL = -1002;
    private static final int RET_STP_HIDL_DEAMON_IS_NOT_READY = -1000;
    private static final boolean RS_DEBUG = SystemProperties.get("ro.secure", "1").equals("0");
    private static final String STP_HIDL_MATCH_CREDIBLE = "credible: Y";
    private static final String STP_HIDL_MATCH_ROOTPROC = "root-procs";
    private static final String STP_HIDL_MATCH_VB = "verifyboot";
    private static final String STP_HIDL_MATCH_VB_RISK = "RISK";
    private static final String STP_HIDL_SERVICE_NAME = "hwstp";
    private static final int STP_ID_KCODE = 897;
    private static final int STP_ID_KCODE_SYSCALL = 898;
    private static final int STP_ID_PROP = 768;
    private static final int STP_ID_ROOT_PROCS = 901;
    private static final int STP_ID_RW = 773;
    private static final int STP_ID_SETIDS = 896;
    private static final int STP_ID_SE_ENFROCING = 899;
    private static final int STP_ID_SE_HOOK = 900;
    private static final int STP_ID_SU = 772;
    private static final int STP_ID_VERIFYBOOT = 512;
    private static final int STP_ITEM_ROOT = 1;
    /* access modifiers changed from: private */
    public static final String TAG = AppLayerStpProxy.class.getSimpleName();
    private static final int TRY_GET_HIDL_DEAMON_DEALY_MILLIS = 1000;
    private static final int[] itemId = {STP_ID_KCODE, STP_ID_KCODE_SYSCALL, 900, STP_ID_SE_ENFROCING, STP_ID_SU, STP_ID_RW, 512, 768, STP_ID_SETIDS, 901};
    private static final int[] itemMark = {1, 2, 4, 8, 16, 32, 128, 256, 512, 1024};
    private static AppLayerStpProxy mInstance;
    private Context mContext;
    /* access modifiers changed from: private */
    public IHwStp mHwStp;
    /* access modifiers changed from: private */
    public final HwStpHandler mHwStpHandler;
    private final HandlerThread mHwStpHandlerThread;
    /* access modifiers changed from: private */
    public IHwBinder.DeathRecipient mStpHidlDeamonDeathRecipient = new IHwBinder.DeathRecipient() {
        public void serviceDied(long cookie) {
            if (AppLayerStpProxy.this.mHwStpHandler != null) {
                Log.e(AppLayerStpProxy.TAG, "stp hidl deamon service has died, try to reconnect it later.");
                IHwStp unused = AppLayerStpProxy.this.mHwStp = null;
                AppLayerStpProxy.this.mHwStpHandler.sendEmptyMessageDelayed(1, 1000);
            }
        }
    };
    /* access modifiers changed from: private */
    public int mStpHidlDeamonRegisterTryTimes = 0;
    private int stpGetItemStatusRetValue = 0;
    /* access modifiers changed from: private */
    public int stpGetStatusByCategoryRetValue = -1001;
    /* access modifiers changed from: private */
    public int stpGetStatusByIDRetValue = -1001;
    /* access modifiers changed from: private */
    public int stpGetStatusRetValue = -1001;

    private final class HwStpHandler extends Handler {
        public HwStpHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what != 1) {
                String access$100 = AppLayerStpProxy.TAG;
                Log.e(access$100, "handler thread received unknown message : " + msg.what);
                return;
            }
            try {
                IHwStp unused = AppLayerStpProxy.this.mHwStp = IHwStp.getService(AppLayerStpProxy.STP_HIDL_SERVICE_NAME);
            } catch (RemoteException e) {
                Log.e(AppLayerStpProxy.TAG, "get stp hidl remote exception in handler message.");
            } catch (Exception e2) {
                Log.e(AppLayerStpProxy.TAG, "get stp hidl exception in handler message.");
            }
            if (AppLayerStpProxy.this.mHwStp != null) {
                int unused2 = AppLayerStpProxy.this.mStpHidlDeamonRegisterTryTimes = 0;
                try {
                    AppLayerStpProxy.this.mHwStp.linkToDeath(AppLayerStpProxy.this.mStpHidlDeamonDeathRecipient, 0);
                } catch (RemoteException e3) {
                    Log.e(AppLayerStpProxy.TAG, "remote exception occured when linkToDeath in handle message");
                } catch (Exception e4) {
                    Log.e(AppLayerStpProxy.TAG, "exception occured when linkToDeath in handle message");
                }
            } else {
                int unused3 = AppLayerStpProxy.this.mStpHidlDeamonRegisterTryTimes = AppLayerStpProxy.this.mStpHidlDeamonRegisterTryTimes + 1;
                if (AppLayerStpProxy.this.mStpHidlDeamonRegisterTryTimes < 10) {
                    String access$1002 = AppLayerStpProxy.TAG;
                    Log.e(access$1002, "stp hidl daemon service is not ready, try times : " + AppLayerStpProxy.this.mStpHidlDeamonRegisterTryTimes);
                    AppLayerStpProxy.this.mHwStpHandler.sendEmptyMessageDelayed(1, 1000);
                } else {
                    Log.e(AppLayerStpProxy.TAG, "stp hidl daemon service connection failed.");
                }
            }
            if (AppLayerStpProxy.HW_DEBUG) {
                Log.d(AppLayerStpProxy.TAG, "handler thread received request stp hidl deamon message.");
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
        synchronized (AppLayerStpProxy.class) {
            if (mInstance == null) {
                mInstance = new AppLayerStpProxy(context);
            }
        }
    }

    public static AppLayerStpProxy getInstance() {
        AppLayerStpProxy appLayerStpProxy;
        synchronized (AppLayerStpProxy.class) {
            appLayerStpProxy = mInstance;
        }
        return appLayerStpProxy;
    }

    public int getRootStatusSync() {
        int ret;
        if (this.mHwStp == null) {
            try {
                this.mHwStp = IHwStp.getService(STP_HIDL_SERVICE_NAME);
            } catch (RemoteException e) {
                Log.e(TAG, "get stp hidl remote exception when get root status sync.");
            } catch (Exception e2) {
                Log.e(TAG, "get stp hidl exception when get root status sync.");
            }
        }
        if (this.mHwStp != null) {
            try {
                this.mHwStp.stpGetStatusByCategory(8, false, false, new IHwStp.stpGetStatusByCategoryCallback() {
                    public void onValues(int stpGetStatusByCategoryRet, String out_buff) {
                        if (AppLayerStpProxy.HW_DEBUG) {
                            String access$100 = AppLayerStpProxy.TAG;
                            Log.d(access$100, "sync get root status from hidl ret : " + stpGetStatusByCategoryRet + " out_buf : " + out_buff);
                        }
                        int unused = AppLayerStpProxy.this.stpGetStatusByCategoryRetValue = stpGetStatusByCategoryRet;
                    }
                });
                ret = this.stpGetStatusByCategoryRetValue;
            } catch (RemoteException e3) {
                ret = -1002;
                Log.e(TAG, "sync get root status from stp hidl failed.");
            }
        } else {
            ret = -1000;
            this.mHwStpHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "stp hidl deamon is not ready when sync get root status from stp hidl");
        }
        if (HW_DEBUG) {
            String str = TAG;
            Log.d(str, "sync get root status , stp hidl ret : " + ret);
        }
        return ret;
    }

    public int getSystemStatusSync() {
        int ret;
        if (this.mHwStp == null) {
            try {
                this.mHwStp = IHwStp.getService(STP_HIDL_SERVICE_NAME);
            } catch (RemoteException e) {
                Log.e(TAG, "get stp hidl remote exception when get system status sync.");
            } catch (Exception e2) {
                Log.e(TAG, "get stp hidl exception when get system status sync.");
            }
        }
        if (this.mHwStp != null) {
            try {
                this.mHwStp.stpGetStatus(false, false, new IHwStp.stpGetStatusCallback() {
                    public void onValues(int stpGetStatusRet, String out_buff) {
                        if (AppLayerStpProxy.HW_DEBUG) {
                            String access$100 = AppLayerStpProxy.TAG;
                            Log.d(access$100, "sync get system status from hidl ret : " + stpGetStatusRet + " out_buf : " + out_buff);
                        }
                        int unused = AppLayerStpProxy.this.stpGetStatusRetValue = stpGetStatusRet;
                    }
                });
                ret = this.stpGetStatusRetValue;
            } catch (RemoteException e3) {
                ret = -1002;
                Log.e(TAG, "sync get system status from stp hidl failed.");
            }
        } else {
            ret = -1000;
            this.mHwStpHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "stp hidl deamon is not ready when sync get system status from stp hidl");
        }
        if (HW_DEBUG) {
            String str = TAG;
            Log.d(str, "sync get system status , stp hidl ret : " + ret);
        }
        return ret;
    }

    public int sendThreatenInfo(int id, byte status, byte credible, byte version, String name, String additian_info) {
        int ret;
        if (HW_DEBUG) {
            String str = TAG;
            Log.d(str, "receive the app's threaten info to stp hidl deamon , status: " + status + ", credible: " + credible + ", name:" + name + ",additian_info:" + additian_info);
        }
        StpItem item = new StpItem();
        item.id = id;
        item.status = status;
        item.credible = credible;
        item.version = version;
        item.name = name;
        if (this.mHwStp == null) {
            try {
                this.mHwStp = IHwStp.getService(STP_HIDL_SERVICE_NAME);
            } catch (RemoteException e) {
                Log.e(TAG, "get stp hidl remote exception when send threaten info.");
            } catch (Exception e2) {
                Log.e(TAG, "get stp hidl exception when send threaten info.");
            }
        }
        if (this.mHwStp != null) {
            try {
                ret = this.mHwStp.stpAddThreat(item, additian_info);
            } catch (RemoteException e3) {
                Log.e(TAG, "sync send threaten info to stp hidl failed.");
                ret = -1002;
            }
        } else {
            ret = -1000;
            this.mHwStpHandler.sendEmptyMessageDelayed(1, 1000);
            Log.e(TAG, "stp hidl deamon is not ready when app send threaten info to stp hidl deamon");
        }
        if (HW_DEBUG) {
            String str2 = TAG;
            Log.d(str2, "send the app's threaten info to stp hidl deamon, ret : " + ret);
        }
        return ret;
    }

    /* access modifiers changed from: private */
    public void checkAdbd(String out_buff) {
        if (out_buff.contains(STP_HIDL_MATCH_ROOTPROC) && out_buff.contains(STP_HIDL_MATCH_CREDIBLE)) {
            this.stpGetItemStatusRetValue |= 64;
            if (HW_DEBUG) {
                Log.d(TAG, "adbd abnormal, stpGetItemStatusRetValue " + this.stpGetItemStatusRetValue);
            }
        }
    }

    /* access modifiers changed from: private */
    public void checkVerifyboot(String out_buff) {
        if (out_buff.contains(STP_HIDL_MATCH_VB) && out_buff.contains(STP_HIDL_MATCH_VB_RISK)) {
            this.stpGetItemStatusRetValue |= 128;
            if (HW_DEBUG) {
                Log.d(TAG, "verifyboot abnormal, stpGetItemStatusRetValue " + this.stpGetItemStatusRetValue);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003a, code lost:
        if (r9.stpGetItemStatusRetValue == 0) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004b, code lost:
        if (r9.stpGetItemStatusRetValue == 0) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001a, code lost:
        if (r9.stpGetItemStatusRetValue == 0) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
        r9.stpGetItemStatusRetValue = -1000;
        android.util.Log.e(TAG, "stp hidl deamon is not ready when sync get item root status from stp");
     */
    public int getEachItemRootStatus() {
        int ret;
        this.stpGetItemStatusRetValue = 0;
        if (this.mHwStp == null) {
            try {
                this.mHwStp = IHwStp.getService(STP_HIDL_SERVICE_NAME);
                if (this.mHwStp == null) {
                }
            } catch (RemoteException e) {
                Log.e(TAG, "get stp hidl remote exception when get item root status sync.");
                if (this.mHwStp == null) {
                }
            } catch (Exception e2) {
                this.stpGetItemStatusRetValue = -1001;
                Log.e(TAG, "try get stp hidl exception when get item root status sync.");
                if (this.mHwStp == null) {
                }
            } catch (Throwable th) {
                if (this.mHwStp == null && this.stpGetItemStatusRetValue == 0) {
                    this.stpGetItemStatusRetValue = -1000;
                    Log.e(TAG, "stp hidl deamon is not ready when sync get item root status from stp");
                }
                throw th;
            }
        }
        if (this.mHwStp != null) {
            for (int i = 0; i < itemId.length; i++) {
                try {
                    this.mHwStp.stpGetStatusById(itemId[i], true, false, new IHwStp.stpGetStatusByIdCallback() {
                        public void onValues(int stpGetStatusByIdRet, String out_buff) {
                            if (out_buff == null) {
                                Log.e(AppLayerStpProxy.TAG, "parameter out_buff is null");
                            } else {
                                if (AppLayerStpProxy.HW_DEBUG) {
                                    String access$100 = AppLayerStpProxy.TAG;
                                    Log.d(access$100, "sync get item root status from hidl ret : " + stpGetStatusByIdRet + " out_buf : " + out_buff);
                                }
                                AppLayerStpProxy.this.checkAdbd(out_buff);
                                AppLayerStpProxy.this.checkVerifyboot(out_buff);
                            }
                            if (AppLayerStpProxy.HW_DEBUG) {
                                String access$1002 = AppLayerStpProxy.TAG;
                                Log.d(access$1002, "stpGetStatusByIDRetValue is " + AppLayerStpProxy.this.stpGetStatusByIDRetValue);
                            }
                            int unused = AppLayerStpProxy.this.stpGetStatusByIDRetValue = stpGetStatusByIdRet;
                        }
                    });
                    ret = this.stpGetStatusByIDRetValue;
                } catch (RemoteException e3) {
                    ret = -1002;
                    Log.e(TAG, "get each item root status from stp hidl failed. itemId:" + itemId[i]);
                }
                if (HW_DEBUG) {
                    Log.d(TAG, "get item root status , stp hidl ret : " + ret + " itemId : " + itemId[i]);
                }
                if (ret < 0) {
                    if (HW_DEBUG) {
                        Log.d(TAG, "stp get status by id failed");
                    }
                    return ret;
                }
                if (ret == 1) {
                    this.stpGetItemStatusRetValue |= itemMark[i];
                } else {
                    Log.d(TAG, "get item root status from stp hidl is normal");
                }
                if (HW_DEBUG) {
                    Log.d(TAG, "stpGetItemStatusRetValue ret : " + this.stpGetItemStatusRetValue + " itemId : " + itemId[i]);
                }
            }
        }
        return this.stpGetItemStatusRetValue;
    }
}
