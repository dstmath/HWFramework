package com.huawei.wallet.sdk.business.idcard.walletbase.tcis;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.trustcircle.TrustCircleManager;
import com.huawei.wallet.sdk.business.bankcard.util.PackageUtil;
import com.huawei.wallet.sdk.business.bankcard.util.ThreadPoolManager;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.account.NFCAccountManager;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.oversea.OverSeasManager;
import com.huawei.wallet.sdk.business.idcard.walletbase.tcis.impl.CallBack;
import com.huawei.wallet.sdk.business.idcard.walletbase.tcis.impl.GetSessionKeyTcisTask;
import com.huawei.wallet.sdk.business.idcard.walletbase.tcis.impl.TcisRqestListener;
import com.huawei.wallet.sdk.business.idcard.walletbase.tcis.request.TcisRequest;
import com.huawei.wallet.sdk.business.idcard.walletbase.tcis.response.TcisResponse;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.TCISSignUtils;
import com.huawei.wallet.sdk.common.utils.crypto.AES;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TcisManager implements TrustCircleManager.KaCallback {
    private static final String KEY_TAVERSION = "TAVersion";
    private static final String KEY_TCISID = "tcisID";
    public static final int TCIS_ERROR = 7;
    public static final int TCIS_LOGIN_ERROR = 8;
    public static final int TCIS_NORMAL = 0;
    public static final int TCIS_NOT_SURRPORT = 1;
    public static final int TCIS_REQUEST_FAIL = 4;
    public static final int TCIS_REQUEST_SUC = 3;
    public static final int TCIS_SIGN_FAIL = 6;
    public static final int TCIS_SIGN_SUC = 5;
    public static final int TCIS_SURRPORT = 2;
    public static volatile TcisManager instance = null;
    protected static final Object lock = new Object();
    private String TAG = TcisManager.class.getSimpleName();
    /* access modifiers changed from: private */
    public String aesTempkey;
    protected Context context;
    /* access modifiers changed from: private */
    public AtomicBoolean isExcuteAtom = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public boolean isTicisLogin = false;
    private List<WeakReference<TcisRqestListener>> listeners = new ArrayList();
    private CallBack sessionCallBack = new CallBack<TcisResponse>() {
        public void onSucess(TcisResponse response) {
            if (response == null || TextUtils.isEmpty(response.getkAInfo())) {
                LogX.i("getTCISSessionKey respons fail", false);
                TcisManager.this.state.set(4);
                TcisManager.this.isExcuteAtom.set(false);
                return;
            }
            LogX.i("getTCISSessionKey sucess", false);
            TcisManager.this.state.set(3);
            TcisManager.this.getTCISSessionKey(response.getKaVersion(), Long.parseLong(NFCAccountManager.getAccountUserId()), TCISSignUtils.encryptUsingPubKey(AES.asBin(TcisManager.this.aesTempkey)), response.getkAInfo());
        }

        public void onFail(int status) {
            LogX.i("getTCISSessionKey fail status=" + status, false);
            TcisManager.this.isExcuteAtom.set(false);
            TcisManager.this.state.set(4);
            TcisManager.this.notifyAllTcisRqestListeners();
        }
    };
    private String sessionKey;
    /* access modifiers changed from: private */
    public AtomicInteger state = new AtomicInteger(0);
    private short taVersion = 0;
    private String tcisID;
    /* access modifiers changed from: private */
    public AtomicInteger tryLoginTcisTimes = new AtomicInteger(0);

    public TcisManager(Context context2) {
        this.context = context2;
    }

    public static TcisManager getInstance(Context context2) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new TcisManager(context2);
                }
            }
        }
        return instance;
    }

    public void notifyAllTcisRqestListeners() {
        if (this.listeners != null) {
            for (WeakReference<TcisRqestListener> listener : this.listeners) {
                if (listener != null) {
                    TcisRqestListener tcisRqestListener = (TcisRqestListener) listener.get();
                    if (tcisRqestListener != null) {
                        tcisRqestListener.onResponse(this.sessionKey, this.state.get());
                    }
                }
            }
            this.listeners.clear();
        }
    }

    public String getTcisID() {
        if (this.state.get() == 0) {
            initTcisId();
        }
        return this.tcisID;
    }

    public void setTcisID(String tcisID2) {
        this.tcisID = tcisID2;
    }

    public short getTaVersion() {
        return this.taVersion;
    }

    public void setTaVersion(short taVersion2) {
        this.taVersion = taVersion2;
    }

    public void init(boolean isSync) {
        if (this.state.get() == 0 || this.state.get() == 4) {
            initTcisId();
            if (TextUtils.isEmpty(this.aesTempkey)) {
                this.aesTempkey = AES.getaeskey();
            }
        }
        getSeeionKeyByCloudServer(isSync);
    }

    public void initTcisId() {
        try {
            if (TextUtils.isEmpty(this.tcisID)) {
                Bundle bundle = TrustCircleManager.getInstance().getTcisInfo();
                if (bundle == null) {
                    this.state.set(1);
                    return;
                }
                this.tcisID = bundle.getString(KEY_TCISID);
                setTcisID(this.tcisID);
                this.taVersion = bundle.getShort(KEY_TAVERSION);
                setTaVersion(this.taVersion);
                if (TextUtils.isEmpty(bundle.getString("hwUserId"))) {
                    this.isTicisLogin = false;
                } else {
                    this.isTicisLogin = true;
                }
                LogX.i(this.TAG + this.isTicisLogin, this.isTicisLogin);
            }
        } catch (Exception e) {
        }
    }

    public boolean checkeTcisLogin() {
        if (this.isTicisLogin) {
            return true;
        }
        Bundle bundle = TrustCircleManager.getInstance().getTcisInfo();
        if (bundle != null) {
            if (TextUtils.isEmpty(bundle.getString("hwUserId"))) {
                this.isTicisLogin = false;
            } else {
                this.isTicisLogin = true;
            }
        }
        return this.isTicisLogin;
    }

    public String getSeeionKeyByCloudServer(boolean isSync) {
        if (this.state.get() == 8 || this.state.get() == 7 || this.state.get() == 6) {
            return null;
        }
        if (this.state.get() == 5) {
            return this.sessionKey;
        }
        if (TextUtils.isEmpty(this.aesTempkey)) {
            this.aesTempkey = AES.getaeskey();
        }
        TcisRequest request = new TcisRequest();
        request.setTA_VERSION(this.taVersion);
        request.setTcisID(this.tcisID);
        request.setDeviceModel(Build.MODEL);
        request.setAdditionAuthData(PackageUtil.getApkSignHashCode(this.context));
        getSessionKey(request, this.sessionCallBack, true);
        if (!isSync) {
            return getAsyncTCISSession();
        }
        return null;
    }

    public String getAsyncTCISSession() {
        long delayTime = System.currentTimeMillis();
        do {
            try {
                Thread.sleep(10);
                if (this.state.get() == 5 || this.state.get() == 6 || this.state.get() == 4 || this.state.get() == 7 || this.state.get() == 8) {
                    break;
                }
            } catch (InterruptedException e) {
                LogX.e("getAsyncTCISSession fail" + e.getMessage(), false);
            }
        } while (System.currentTimeMillis() - delayTime <= 10000);
        return this.sessionKey;
    }

    public void getTCISSessionKey(int kaVersion, long userId, byte[] aesTmpKey, String kaInfo) {
        if (TrustCircleManager.getInstance().initKeyAgreement(this, kaVersion, userId, aesTmpKey, kaInfo) == -1) {
            this.state.set(7);
        }
    }

    public String encryptSession(long l, int i, byte[] bytes, byte[] bytes1) {
        try {
            return AES.byte2HexStr(TCISSignUtils.decrypt(bytes1, AES.asBin(this.aesTempkey), bytes));
        } catch (Exception e) {
            LogX.e("encryptSession fail" + e.getMessage(), false);
            return null;
        }
    }

    public TcisResponse getSessionKey(TcisRequest request, CallBack callBack, boolean isSync) {
        if (!isSync) {
            return doAsnycGetSessionResponse(request);
        }
        doSyncGetSessionResponse(request, callBack);
        return null;
    }

    private TcisResponse doAsnycGetSessionResponse(TcisRequest request) {
        if (checkeTcisLogin() || this.tryLoginTcisTimes.get() >= 4) {
            this.state.set(4);
            return null;
        }
        this.tryLoginTcisTimes.set(this.tryLoginTcisTimes.get() + 1);
        String walletUrl = OverSeasManager.getInstance(this.context).getGrsUrlSync("WALLET");
        return (TcisResponse) new GetSessionKeyTcisTask(this.context, walletUrl + "?clientVersion=" + PackageUtil.getVersionCode(this.context), null).processTask(request);
    }

    private void doSyncGetSessionResponse(final TcisRequest request, final CallBack callBack) {
        if (this.state.get() == 3) {
            LogX.i("doSyncGetSessionResponse -- TCIS_REQUEST_SUC", false);
        } else if (!this.isExcuteAtom.get()) {
            this.isExcuteAtom.set(true);
            ThreadPoolManager.getInstance().submit(new Runnable() {
                public void run() {
                    if (!TcisManager.this.checkeTcisLogin()) {
                        int count = 0;
                        while (count <= 4) {
                            try {
                                Thread.sleep(1000);
                                if (TcisManager.this.checkeTcisLogin()) {
                                    break;
                                }
                                count++;
                            } catch (InterruptedException e) {
                                LogX.e("tcis_interrupt" + e.getMessage(), false);
                            }
                        }
                    }
                    if (TcisManager.this.isTicisLogin) {
                        String walletUrl = OverSeasManager.getInstance(TcisManager.this.context).getGrsUrlSync("WALLET");
                        new GetSessionKeyTcisTask(TcisManager.this.context, walletUrl + "?clientVersion=" + PackageUtil.getVersionCode(TcisManager.this.context), callBack).processTask(request);
                    } else if (TcisManager.this.tryLoginTcisTimes.get() < 4) {
                        TcisManager.this.tryLoginTcisTimes.set(TcisManager.this.tryLoginTcisTimes.get() + 1);
                        TcisManager.this.state.set(4);
                    } else {
                        TcisManager.this.state.set(8);
                    }
                }
            });
        }
    }

    public void onDestroy() {
        this.tcisID = null;
        this.taVersion = 0;
        this.aesTempkey = null;
        this.sessionKey = null;
        this.state.set(0);
    }

    public void onKaResult(long l, int i, byte[] bytes, byte[] bytes1) {
        this.sessionKey = encryptSession(l, i, bytes, bytes1);
        if (this.sessionKey == null || this.sessionKey.length() == 0) {
            this.state.set(6);
        } else {
            this.state.set(5);
        }
        LogX.i("onKaResult state= " + this.state, false);
        this.isExcuteAtom.set(false);
        notifyAllTcisRqestListeners();
    }

    public void onKaError(long l, int result) {
        this.state.set(6);
        this.isExcuteAtom.set(false);
        LogX.e("onKaError state= " + this.state, false);
        notifyAllTcisRqestListeners();
    }

    public String getSessionKey() {
        return this.sessionKey;
    }

    public void setSessionKey(String sessionKey2) {
        this.sessionKey = sessionKey2;
    }
}
