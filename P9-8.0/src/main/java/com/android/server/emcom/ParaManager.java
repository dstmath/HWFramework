package com.android.server.emcom;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.telephony.HwTelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.emcom.daemon.DaemonCommand;
import com.android.server.emcom.util.EMCOMConstants;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;

public class ParaManager implements EMCOMConstants {
    private static final boolean PARA_DEBUG = ParaManagerConstants.PARA_DEBUG;
    private static final String TAG = "ParaManager";
    private static ParaManager sInstance = null;
    private final Context mContext;
    private int mCotaParaReadyRec = 0;
    private DaemonCommand mDaemonCommand;
    private Handler mEmcomManagerServiceHandler;
    private ParaManagerHandle mHandler;
    private HwTelephonyManager mHwTelephonyManager;
    private String mParaFile = null;
    private int mParaUpgradePhoneState = -1;
    private int mParaUpgradeScreenStatus = -1;
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            ParaManager.this.mParaUpgradePhoneState = state;
            if ((ParaManager.this.mCotaParaReadyRec & 1) != 0) {
                if (ParaManager.this.myUpgradeProc[0] == null) {
                    Log.e(ParaManager.TAG, "myUpgradeProc[" + 0 + "] is null");
                    return;
                } else if (1 == ParaManager.this.myUpgradeProc[0].pathType && 1 == ParaManager.this.myUpgradeProc[0].cotaUpgradeStatus) {
                    if (ParaManager.PARA_DEBUG) {
                        Log.d(ParaManager.TAG, "onCallStateChanged  state = " + state);
                    }
                    ParaManager.this.checkIfReadytoUpgradePara(ParaManager.this.myUpgradeProc[0].paraType, 1);
                }
            }
            if ((ParaManager.this.mCotaParaReadyRec & 512) != 0) {
                if (ParaManager.this.myUpgradeProc[9] == null) {
                    Log.e(ParaManager.TAG, "myUpgradeProc[" + 9 + "] is null");
                } else if (1 == ParaManager.this.myUpgradeProc[9].pathType && 1 == ParaManager.this.myUpgradeProc[9].cotaUpgradeStatus) {
                    if (ParaManager.PARA_DEBUG) {
                        Log.d(ParaManager.TAG, "onCallStateChanged  state = " + state);
                    }
                    ParaManager.this.checkIfReadytoUpgradePara(ParaManager.this.myUpgradeProc[9].paraType, 1);
                }
            }
        }
    };
    private String[] mRelativePath = null;
    private TelephonyManager mTelephonyManager;
    public UpgradeProc[] myUpgradeProc = null;

    private class ParaManagerHandle extends Handler {
        public ParaManagerHandle(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int k;
            Log.d(ParaManager.TAG, "handleMessage: msg.what = " + msg.what);
            int msgIndex = msg.what;
            if (msgIndex < ParaManagerConstants.MESSAGE_BASE_MONITOR_RESPONSE) {
                k = msgIndex + 0;
                if (ParaManager.this.myUpgradeProc[k] == null) {
                    Log.e(ParaManager.TAG, "myUpgradeProc[" + k + "] is null");
                    return;
                } else if (ParaManager.this.myUpgradeProc[k].monitorUpgradeSitu && 1 == ParaManager.this.myUpgradeProc[k].pathType) {
                    ParaManager.this.myUpgradeProc[k].monitorUpgradeSitu = false;
                    ParaManager.this.myUpgradeProc[k].cotaUpgradeStatus = 2;
                    ParaManager.this.myUpgradeProc[k].upgradeResp = ParaManager.this.myUpgradeProc[k].respBase + ParaManager.this.myUpgradeProc[k].cotaUpgradeStatus;
                    Log.d(ParaManager.TAG, "Situation monitor overtime for paraType[" + k + "]" + " Upgrade");
                    if (ParaManager.PARA_DEBUG) {
                        Log.d(ParaManager.TAG, "cotaUpgradeStatus[" + k + "] = " + ParaManager.this.myUpgradeProc[k].cotaUpgradeStatus + ", upgradeResp[" + k + "] = " + ParaManager.this.myUpgradeProc[k].upgradeResp);
                    }
                    ParaManager.this.myUpgradeProc[k].cotaUpgradeStatus = -1;
                    ParaManager.this.myUpgradeProc[k].upgradeResp = -1;
                    ParaManager.this.mCotaParaReadyRec = ParaManager.this.mCotaParaReadyRec & (~ParaManager.this.myUpgradeProc[k].paraType);
                    Log.d(ParaManager.TAG, "mCotaParaReadyRec = " + ParaManager.this.mCotaParaReadyRec);
                }
            }
            if (msgIndex >= ParaManagerConstants.MESSAGE_BASE_MONITOR_RESPONSE) {
                k = msgIndex - 4000;
                if (ParaManager.this.myUpgradeProc[k].monitorUpgradeResp && 1 == ParaManager.this.myUpgradeProc[k].pathType) {
                    ParaManager.this.myUpgradeProc[k].monitorUpgradeResp = false;
                    ParaManager.this.myUpgradeProc[k].cotaUpgradeStatus = 3;
                    ParaManager.this.myUpgradeProc[k].upgradeResp = ParaManager.this.myUpgradeProc[k].respBase + ParaManager.this.myUpgradeProc[k].cotaUpgradeStatus;
                    Log.d(ParaManager.TAG, "Response monitor overtime for paraType[" + k + "]" + " Upgrade");
                    if (ParaManager.PARA_DEBUG) {
                        Log.d(ParaManager.TAG, "cotaUpgradeStatus[" + k + "] = " + ParaManager.this.myUpgradeProc[k].cotaUpgradeStatus + ", upgradeResp[" + k + "] = " + ParaManager.this.myUpgradeProc[k].upgradeResp);
                    }
                    ParaManager.this.myUpgradeProc[k].cotaUpgradeStatus = -1;
                    ParaManager.this.myUpgradeProc[k].upgradeResp = -1;
                    ParaManager.this.mCotaParaReadyRec = ParaManager.this.mCotaParaReadyRec & (~ParaManager.this.myUpgradeProc[k].paraType);
                    Log.d(ParaManager.TAG, "mCotaParaReadyRec = " + ParaManager.this.mCotaParaReadyRec);
                }
            }
        }
    }

    public static class UpgradeProc {
        public int cotaUpgradeStatus;
        public boolean monitorUpgradeResp;
        public boolean monitorUpgradeSitu;
        public int msgMonUpgrResp;
        public int msgMonUpgrSitu;
        public int paraType;
        public int pathType;
        public int respBase;
        public int typeMask;
        public int upgradeResp;
        public int waitRespTime;
        public int waitSituTime;

        public void upgradeInit(int i) {
            this.typeMask = i;
            this.paraType = 1 << i;
            this.pathType = -1;
            this.cotaUpgradeStatus = -1;
            this.monitorUpgradeSitu = false;
            this.monitorUpgradeResp = false;
            this.respBase = i * 100;
            this.upgradeResp = -1;
            this.msgMonUpgrSitu = i + 0;
            this.msgMonUpgrResp = i + ParaManagerConstants.MESSAGE_BASE_MONITOR_RESPONSE;
            if (i == 0 || i == 9) {
                this.waitSituTime = 520000;
                this.waitRespTime = 70000;
                return;
            }
            this.waitSituTime = 0;
            this.waitRespTime = 590000;
        }
    }

    public static ParaManager make(Context context, Handler handler) {
        if (sInstance != null) {
            if (PARA_DEBUG) {
                Log.d(TAG, "make: sInstance is already made,just return");
            }
            return sInstance;
        } else if (context == null) {
            Log.e(TAG, "make: Context is null, return!");
            return null;
        } else if (handler == null) {
            Log.e(TAG, "make: handler is null, return!");
            return null;
        } else {
            sInstance = new ParaManager(context, handler);
            return sInstance;
        }
    }

    public ParaManager(Context context, Handler handler) {
        if (PARA_DEBUG) {
            Log.d(TAG, "ParaManager()0");
        }
        this.mContext = context;
        this.mEmcomManagerServiceHandler = handler;
        this.mDaemonCommand = DaemonCommand.getInstance();
        HandlerThread ParaManagerthread = new HandlerThread("ParaManagerProcessorthread", 10);
        ParaManagerthread.start();
        this.mHandler = new ParaManagerHandle(ParaManagerthread.getLooper());
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mHwTelephonyManager = HwTelephonyManager.getDefault();
        this.myUpgradeProc = new UpgradeProc[11];
        this.mRelativePath = new String[]{"/ncfg", "/cellular_cloud", null, null, "/emcom/noncell", null, null, null, "/emcom/emcomctr", "/emcom/carrierconfig"};
        this.mParaFile = "/version.txt";
        if (PARA_DEBUG) {
            Log.d(TAG, "ParaManager()1");
        }
        for (int i = 0; i < 10; i++) {
            this.myUpgradeProc[i] = new UpgradeProc();
            this.myUpgradeProc[i].upgradeInit(i);
        }
    }

    public static ParaManager getInstance() {
        if (sInstance == null) {
            Log.e(TAG, "getInstance: sInstance is null !");
        }
        return sInstance;
    }

    public boolean needtoCheckBasicImsNVUpgrade() {
        if (this.myUpgradeProc[0] != null) {
            return 1 == this.myUpgradeProc[0].pathType && 1 == this.myUpgradeProc[0].cotaUpgradeStatus;
        } else {
            Log.e(TAG, "myUpgradeProc[" + 0 + "] is null");
            return false;
        }
    }

    public boolean needtoCheckCarrierConfigUpgrade() {
        if (this.myUpgradeProc[9] != null) {
            return 1 == this.myUpgradeProc[9].pathType && 1 == this.myUpgradeProc[9].cotaUpgradeStatus;
        } else {
            Log.e(TAG, "myUpgradeProc[" + 9 + "] is null");
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:21:0x007a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handlerCotaParaUpgrade(String cotaParaCfgDir, String cotaParaUpdateMode) {
        synchronized (this) {
            Log.d(TAG, "handlerCotaParaUpgrade: cotaParaCfgDir=" + cotaParaCfgDir + ", cotaParaUpdateMode = " + cotaParaUpdateMode);
            if (this.myUpgradeProc == null) {
                Log.e(TAG, "myUpgradeProc[] is null");
            } else if (this.mRelativePath == null) {
                Log.e(TAG, "mRelativePath[] is null");
            } else {
                for (int i = 0; i < 10; i++) {
                    if (cotaParaCfgDir.equals(this.mRelativePath[i])) {
                        Log.d(TAG, "paraTypeBitmap = " + this.myUpgradeProc[i].paraType);
                        int k = i;
                        checkCotaParaUpgrade(cotaParaCfgDir, cotaParaUpdateMode, i);
                        break;
                    }
                }
            }
        }
    }

    public void checkCotaParaUpgrade(String cotaParaCfgDir, String cotaParaUpdateMode, int typeMask) {
        if (cotaParaCfgDir == null || cotaParaUpdateMode == null) {
            Log.e(TAG, "cotaParaCfgDir or cotaParaUpdateMode is null");
            return;
        }
        int i = typeMask;
        if (this.myUpgradeProc[typeMask] == null || this.mParaFile == null) {
            Log.e(TAG, "myUpgradeProc[" + typeMask + "] or mParaFile[" + typeMask + "] is null");
            return;
        }
        String[] cfgFileInfo = HwCfgFilePolicy.getDownloadCfgFile(cotaParaCfgDir, cotaParaCfgDir + this.mParaFile);
        if (cfgFileInfo == null) {
            Log.e(TAG, "Both default and cota config files not exist");
            return;
        }
        if (PARA_DEBUG) {
            Log.d(TAG, "config file path is: " + cfgFileInfo[0]);
            Log.d(TAG, "config file version is: " + cfgFileInfo[1]);
        }
        if (cfgFileInfo[0].contains("/cota/")) {
            if (PARA_DEBUG) {
                Log.d(TAG, "cota config file path is: " + cfgFileInfo[0]);
            }
            if (cotafileIsExists(cfgFileInfo[0])) {
                Log.d(TAG, "cotafiles of mParaFile[" + typeMask + "] exist");
                this.myUpgradeProc[typeMask].pathType = 1;
                if (this.mHandler.hasMessages(this.myUpgradeProc[typeMask].msgMonUpgrSitu)) {
                    this.myUpgradeProc[typeMask].monitorUpgradeSitu = false;
                    if (PARA_DEBUG) {
                        Log.d(TAG, "Monitor for msg[" + this.myUpgradeProc[typeMask].msgMonUpgrSitu + "]" + " already started");
                    }
                    this.mHandler.removeMessages(this.myUpgradeProc[typeMask].msgMonUpgrSitu);
                }
                this.myUpgradeProc[typeMask].cotaUpgradeStatus = 1;
                this.mCotaParaReadyRec |= this.myUpgradeProc[typeMask].paraType;
                if (typeMask == 0 || 9 == typeMask) {
                    if (cotaParaUpdateMode.equals("night")) {
                        this.myUpgradeProc[typeMask].monitorUpgradeSitu = true;
                        Message msg = this.mHandler.obtainMessage();
                        msg.what = this.myUpgradeProc[typeMask].msgMonUpgrSitu;
                        this.mHandler.sendMessageDelayed(msg, (long) this.myUpgradeProc[typeMask].waitSituTime);
                        Log.d(TAG, "Start monitor opportunity for Upgrade");
                        registerPhoneStateListener(this.mContext);
                    } else if (cotaParaUpdateMode.equals("manual")) {
                        this.myUpgradeProc[typeMask].monitorUpgradeSitu = false;
                        this.myUpgradeProc[typeMask].cotaUpgradeStatus = 3;
                        monitorUpgradeResponse(typeMask);
                        if (typeMask == 0) {
                            notifyCellularCommParaReady(this.myUpgradeProc[typeMask].paraType, this.myUpgradeProc[typeMask].pathType);
                        }
                        if (9 == typeMask) {
                            notifyParaReady(this.myUpgradeProc[typeMask].paraType, this.myUpgradeProc[typeMask].pathType);
                        }
                    } else {
                        Log.e(TAG, "cotaParaUpdateMode error, do nothing");
                        return;
                    }
                } else if (1 == typeMask) {
                    this.myUpgradeProc[typeMask].monitorUpgradeSitu = false;
                    this.myUpgradeProc[typeMask].cotaUpgradeStatus = 3;
                    monitorUpgradeResponse(typeMask);
                    notifyCellularCommParaReady(this.myUpgradeProc[typeMask].paraType, this.myUpgradeProc[typeMask].pathType);
                } else {
                    this.myUpgradeProc[typeMask].monitorUpgradeSitu = false;
                    this.myUpgradeProc[typeMask].cotaUpgradeStatus = 3;
                    monitorUpgradeResponse(typeMask);
                    notifyParaReady(this.myUpgradeProc[typeMask].paraType, this.myUpgradeProc[typeMask].pathType);
                }
            } else {
                Log.e(TAG, "cotafiles of mParaFile[" + typeMask + "] not exist");
                this.myUpgradeProc[typeMask].cotaUpgradeStatus = 0;
                this.myUpgradeProc[typeMask].upgradeResp = this.myUpgradeProc[typeMask].respBase + this.myUpgradeProc[typeMask].cotaUpgradeStatus;
                this.mCotaParaReadyRec &= ~this.myUpgradeProc[typeMask].paraType;
            }
            if (PARA_DEBUG) {
                Log.d(TAG, "cotaUpgradeStatus[" + typeMask + "] = " + this.myUpgradeProc[typeMask].cotaUpgradeStatus + ", upgradeResp[" + typeMask + "] = " + this.myUpgradeProc[typeMask].upgradeResp + ", mCotaParaReadyRec = " + this.mCotaParaReadyRec);
            }
            return;
        }
        Log.e(TAG, "version of version.txt in cota path is lower than default path, no need to upgrade");
    }

    public boolean cotafileIsExists(String path) {
        if (path == null) {
            return false;
        }
        if (PARA_DEBUG) {
            Log.d(TAG, "cotafileIsExists: path =" + path);
        }
        if (new File(path).exists()) {
            return true;
        }
        Log.e(TAG, "cotafile not exists");
        return false;
    }

    protected void registerPhoneStateListener(Context context) {
        this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
    }

    public boolean isRadioAvailable() {
        return this.mHwTelephonyManager.is4GCardRadioAvailable();
    }

    public void updateScreenStatus(int screenStatus) {
        this.mParaUpgradeScreenStatus = screenStatus;
        Log.d(TAG, "updateScreenStatus  screenStatus = " + this.mParaUpgradeScreenStatus);
    }

    public void checkIfReadytoUpgradePara(int paratype, int pathtype) {
        int k = -1;
        if ((paratype & 1) != 0) {
            k = 0;
        } else if ((paratype & 512) != 0) {
            k = 9;
        }
        if (this.myUpgradeProc[k] == null) {
            Log.e(TAG, "myUpgradeProc[" + k + "] is null");
            return;
        }
        Log.d(TAG, "checkIfReadytoUpgradePara: paratype = " + paratype + ", pathtype = " + pathtype + ", typeMask = " + k);
        boolean radioAvailable = isRadioAvailable();
        if (1 == this.myUpgradeProc[k].pathType && 1 == this.myUpgradeProc[k].cotaUpgradeStatus) {
            if (2 == this.mParaUpgradeScreenStatus && radioAvailable && this.mParaUpgradePhoneState == 0) {
                Log.d(TAG, "suitable to upgrade Para");
                if (PARA_DEBUG) {
                    Log.d(TAG, "screenStatus = " + this.mParaUpgradeScreenStatus + ", radioAvailable = " + radioAvailable + ", phoneState = " + this.mParaUpgradePhoneState);
                }
                if (this.mHandler.hasMessages(this.myUpgradeProc[k].msgMonUpgrSitu)) {
                    this.myUpgradeProc[k].monitorUpgradeSitu = false;
                    this.mHandler.removeMessages(this.myUpgradeProc[k].msgMonUpgrSitu);
                    if (PARA_DEBUG) {
                        Log.d(TAG, "removeMessages(" + this.myUpgradeProc[k].msgMonUpgrSitu + ")");
                    }
                }
                this.myUpgradeProc[k].cotaUpgradeStatus = 3;
                monitorUpgradeResponse(k);
                if (1 == paratype) {
                    notifyCellularCommParaReady(paratype, pathtype);
                } else if (512 == paratype) {
                    notifyParaReady(paratype, pathtype);
                }
            } else {
                int screenOn = 2 != this.mParaUpgradeScreenStatus ? 4 : 0;
                int UpgradeSituation = ((screenOn | 0) | (!radioAvailable ? 2 : 0)) | (this.mParaUpgradePhoneState != 0 ? 1 : 0);
                if (PARA_DEBUG) {
                    Log.d(TAG, "cotaUpgradeStatus[" + k + "] = " + this.myUpgradeProc[k].cotaUpgradeStatus + ", screenStatus = " + this.mParaUpgradeScreenStatus + ", radioAvailable = " + radioAvailable + ", phoneState = " + this.mParaUpgradePhoneState);
                }
                Log.e(TAG, "situation is not ready. UpgradeSituation = " + UpgradeSituation);
            }
        }
    }

    public void notifyCellularCommParaReady(int paratype, int pathtype) {
        Log.d(TAG, "notifyCellularCommParaReady: paratype = " + paratype + ", pathtype = " + pathtype);
        this.mHwTelephonyManager.notifyCellularCommParaReady(paratype, pathtype, null);
    }

    public void notifyParaReady(int paraBitRec, int pathtype) {
        Log.d(TAG, "notifyParaReady: paraBitRec = " + paraBitRec + ", pathtype = " + pathtype);
        if (1 == pathtype) {
            Intent intent = new Intent(ParaManagerConstants.EMCOM_PARA_READY_ACTION);
            intent.putExtra(ParaManagerConstants.EXTRA_EMCOM_PARA_READY_REC, paraBitRec);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, ParaManagerConstants.RECEIVE_EMCOM_PARA_UPGRADE_PERMISSION);
        }
        if ((paraBitRec & 256) != 0) {
            Log.d(TAG, "EMCOM_CTR para update, sendCfgUpdateToDaemond.");
            sendCfgUpdateToDaemond();
        }
    }

    private void monitorUpgradeResponse(int typeMask) {
        int k = typeMask;
        if (this.myUpgradeProc[typeMask] == null) {
            Log.e(TAG, "myUpgradeProc[" + typeMask + "] is null");
            return;
        }
        this.myUpgradeProc[typeMask].monitorUpgradeResp = true;
        Message msg = this.mHandler.obtainMessage();
        msg.what = typeMask + ParaManagerConstants.MESSAGE_BASE_MONITOR_RESPONSE;
        this.mHandler.sendMessageDelayed(msg, (long) this.myUpgradeProc[typeMask].waitRespTime);
        if (PARA_DEBUG) {
            Log.d(TAG, "monitorUpgradeResp[" + typeMask + "] = " + this.myUpgradeProc[typeMask].monitorUpgradeResp);
        }
        Log.d(TAG, "Start monitor upgrade response back, msg[" + typeMask + "] = " + msg.what + ", waitRespTime[" + typeMask + "] = " + (this.myUpgradeProc[typeMask].waitRespTime / 1000) + " sec");
    }

    public void responseForParaUpgrade(int paratype, int pathtype, int result) {
        synchronized (this) {
            Log.d(TAG, "responseForParaUpgrade: paratype = " + paratype + ", pathtype = " + pathtype + ", result = " + result);
            int k = -1;
            for (int i = 0; i < 10; i++) {
                if (paratype == (1 << i)) {
                    k = i;
                    break;
                }
            }
            if (k == -1) {
                Log.e(TAG, "Unknown paratype: " + paratype);
                return;
            }
            handleResponseForParaUpgrade(paratype, pathtype, result, k);
        }
    }

    public void handleResponseForParaUpgrade(int paratype, int pathtype, int result, int typeMask) {
        int k = typeMask;
        if (this.myUpgradeProc[typeMask] == null) {
            Log.e(TAG, "myUpgradeProc[" + typeMask + "] is null");
            return;
        }
        Log.d(TAG, "handleResponseForParaUpgrade: msg[" + typeMask + "] = " + this.myUpgradeProc[typeMask].msgMonUpgrResp);
        if (1 == pathtype) {
            this.myUpgradeProc[typeMask].cotaUpgradeStatus = result;
            this.myUpgradeProc[typeMask].upgradeResp = this.myUpgradeProc[typeMask].respBase + result;
            Log.d(TAG, "cotaUpgradeStatus[" + typeMask + "] = " + result + ", upgradeResp[" + typeMask + "] = " + this.myUpgradeProc[typeMask].upgradeResp);
            if (this.mHandler.hasMessages(this.myUpgradeProc[typeMask].msgMonUpgrResp)) {
                this.myUpgradeProc[typeMask].monitorUpgradeResp = false;
                this.mHandler.removeMessages(this.myUpgradeProc[typeMask].msgMonUpgrResp);
                if (PARA_DEBUG) {
                    Log.d(TAG, "upgradeResp[" + typeMask + "] response back, removeMessages(" + this.myUpgradeProc[typeMask].msgMonUpgrResp + ")");
                }
            }
            this.mCotaParaReadyRec &= ~paratype;
            this.myUpgradeProc[typeMask].cotaUpgradeStatus = -1;
            this.myUpgradeProc[typeMask].upgradeResp = -1;
            this.myUpgradeProc[typeMask].pathType = -1;
            Log.d(TAG, "cotaParaReadyRec = " + this.mCotaParaReadyRec);
        }
    }

    private void sendCfgUpdateToDaemond() {
        this.mDaemonCommand.exeConfigUpdate(this.mEmcomManagerServiceHandler.obtainMessage(2));
    }
}
