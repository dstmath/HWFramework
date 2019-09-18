package com.huawei.wallet.sdk.business.idcard.walletbase.logic.account;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.util.ThreadPoolManager;
import com.huawei.wallet.sdk.business.buscard.base.model.HciConfigInfo;
import com.huawei.wallet.sdk.business.idcard.walletbase.model.account.AccountInfo;
import com.huawei.wallet.sdk.business.idcard.walletbase.storage.sp.AccountPreferences;
import com.huawei.wallet.sdk.common.log.LogC;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AccountManager extends AccountConst {
    private static final String KEY_HEAD_URL = "key_head_url";
    private static final String KEY_SECURITY_PHONE = "key_security_phone";
    private static final int MSG_GET_AT_ERROR = 16;
    private static final int MSG_GET_AT_SUC = 15;
    private static final int MSG_LOGIN_ERROR = 12;
    private static final int MSG_LOGIN_SUC = 11;
    private static final int MSG_PHONE_HEAD_SUC = 14;
    private static final int MSG_UPDATE_NICK_NAME = 13;
    private static final String QUERY_HWID_INFO = "1000";
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final byte[] SYNC_LOCK_ISLOGIN = new byte[0];
    private static final String TAG = "AccountManager";
    /* access modifiers changed from: private */
    public static AtomicBoolean atomIsAuthLogin = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public static AtomicBoolean atomIsNoAuthLogin = new AtomicBoolean(false);
    private static volatile AccountManager instance;
    /* access modifiers changed from: private */
    public static HashMap<String, AccountInfo> mOtherAccountMap = new HashMap<>();
    private List<AccountGetATCallback> accountAtCallbacks = new CopyOnWriteArrayList();
    private boolean accountFromWallet = false;
    private List<AccountInfoListener> accountInfoListeners = new CopyOnWriteArrayList();
    private List<AccountLoginCallback> accountLoginCallbacks = new CopyOnWriteArrayList();
    private List<AccountStateListener> accountStateListeners = new CopyOnWriteArrayList();
    /* access modifiers changed from: private */
    public Handler callBackHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 11:
                    AccountManager.this.notifyAccountLoginSuc();
                    AccountManager.this.notifyLoginSuccess(AccountManager.this.mAccountInfo);
                    return;
                case 12:
                    AccountManager.this.notifyAccountLoginError(msg.arg1);
                    return;
                case 13:
                    Object name = msg.obj;
                    if (name instanceof String) {
                        AccountManager.this.notifyNickNameChanged((String) name);
                        return;
                    }
                    return;
                case 14:
                    Bundle data = msg.getData();
                    if (data != null) {
                        String phone = data.getString(AccountManager.KEY_SECURITY_PHONE);
                        String headUrl = data.getString(AccountManager.KEY_HEAD_URL);
                        Object callback = msg.obj;
                        if (callback instanceof AccountPhoneHeadCallback) {
                            ((AccountPhoneHeadCallback) callback).onPhoneHeadSuccess(phone, headUrl);
                            return;
                        }
                        return;
                    }
                    return;
                case 15:
                    Object obj = msg.obj;
                    if (obj instanceof String) {
                        AccountManager.this.notifyGetATSuccess((String) obj);
                        return;
                    }
                    return;
                case 16:
                    int error = msg.arg1;
                    Object description = msg.obj;
                    if (description instanceof String) {
                        AccountManager.this.notifyGetATError(error, (String) description);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private boolean isAccountHasLogin = false;
    private volatile boolean isLogin = false;
    /* access modifiers changed from: private */
    public AccountInfo mAccountInfo = new AccountInfo();

    private class GetATRunnable implements Runnable {
        private WeakReference<Context> context;
        HwIDAccountGetATCallback hwIDAccountGetATCallback = new HwIDAccountGetATCallback();

        public GetATRunnable(Context context2) {
            this.context = new WeakReference<>(context2);
        }

        public void run() {
            if (this.context != null) {
                Context c = (Context) this.context.get();
                if (c != null) {
                    AccountManager.this.getAccessTokenFromHwID(c, this.hwIDAccountGetATCallback);
                }
            }
        }
    }

    private static class GetAccountRunnable implements Runnable {
        private WeakReference<Context> context;
        private boolean isLoginByAIDL;
        private boolean isNeedAuth;
        private AccountManager manager;

        public GetAccountRunnable(Context context2, AccountManager manager2, boolean isNeedAuth2, boolean isLoginByAIDL2) {
            this.context = new WeakReference<>(context2.getApplicationContext());
            this.manager = manager2;
            this.isLoginByAIDL = isLoginByAIDL2;
            this.isNeedAuth = isNeedAuth2;
        }

        public void run() {
            if (this.context != null && ((Context) this.context.get()) != null) {
                if (this.isNeedAuth) {
                    AccountManager.atomIsAuthLogin.set(false);
                } else {
                    AccountManager.atomIsNoAuthLogin.set(false);
                }
            }
        }
    }

    private class HwIDAccountGetATCallback implements AccountGetATCallback {
        private HwIDAccountGetATCallback() {
        }

        public void onGetATSuccess(String accessToken) {
            LogC.d(AccountManager.TAG, "getAccessToken onGetATSuccess", false);
            AccountManager.this.mAccountInfo.setAccessToken(accessToken);
            if (AccountManager.mOtherAccountMap.containsKey(AccountManager.this.mAccountInfo.getUserId())) {
                ((AccountInfo) AccountManager.mOtherAccountMap.get(AccountManager.this.mAccountInfo.getUserId())).setAccessToken(accessToken);
            }
            Message msg = Message.obtain(AccountManager.this.callBackHandler, 15);
            msg.obj = accessToken;
            msg.sendToTarget();
        }

        public void onGetATError(int errorCode, String errorDesc) {
            LogC.w(AccountManager.TAG, "getAccessToken onGetATError ,errorCode = " + errorCode, false);
            Message msg = Message.obtain(AccountManager.this.callBackHandler, 16);
            msg.arg1 = errorCode;
            msg.obj = errorDesc;
            msg.sendToTarget();
        }
    }

    private static class NormalAccountGetATCallback implements AccountGetATCallback {
        private NormalAccountGetATCallback() {
        }

        public void onGetATSuccess(String accessToken) {
            LogC.i(AccountManager.TAG, "init get AT success", false);
        }

        public void onGetATError(int errorCode, String errorDesc) {
            LogC.w(AccountManager.TAG, "init get AT errorCode =" + errorCode + ", errorDesc =" + errorDesc, false);
        }
    }

    public static AccountManager getInstance() {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new AccountManager();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        checkAccountHasLogin(context);
        registerReceiver(context);
    }

    public String getUid(Context context) {
        return AccountPreferences.getInstance(context).getString("user_id", "");
    }

    public void clearSPAndResetFlag(Context context) {
        LogC.i(TAG, "clear sp and reset flag. ", false);
        SharedPreferences.Editor editor = context.getSharedPreferences("payInitParams", 0).edit();
        editor.putString("account_name", "");
        editor.putString("nick_name", "");
        editor.putString("account_icon", "");
        editor.putBoolean("gestrue_password_enable", false);
        editor.putBoolean("getstrue_password_path_enable", true);
        editor.putBoolean("is_prompt_set_gestrue_password", false);
        editor.putInt("gestrue_wrong_count", 0);
        Long l = -1L;
        editor.putLong("ecash", l.longValue());
        Long l2 = -1L;
        editor.putLong(HciConfigInfo.HCI_DATA_TYPE_AFTER_TRANSCTION_BALANCE, l2.longValue());
        editor.putBoolean("is_accept_pay_agreement", false);
        editor.putBoolean("has_agree_phone_bill_privacy", false);
        editor.remove("is_know_alipay_withhold_agreement");
        editor.remove("is_first_remind_alipay_withhold_agreement");
        editor.remove("nfc_uploaded_pushtoken");
        editor.remove("nfc_pushtoken_reuploaded");
        editor.remove("sn_uploaded");
        editor.commit();
        getAccountInfo().setHeadBitmap(null);
        AccountPreferences.getInstance(context).putString(AccountPreferences.ACCOUNT_HEADURL, "");
        AccountPreferences.getInstance(context).putString("user_id", "");
    }

    public void checkAccountHasLogin(final Context context) {
        if (!TextUtils.isEmpty(this.mAccountInfo.getUserId()) || !hasLoginHWAccount(context)) {
            LogC.i(TAG, "mAccountInfo not init, has not login HWAccount or UserId is not null", false);
            return;
        }
        LogC.i(TAG, "mAccountInfo init", false);
        loginHWAccount(context, new AccountLoginCallback() {
            public void onLoginSuccess(AccountInfo accountInfo) {
                String spId = AccountPreferences.getInstance(context).getString("user_id", "");
                if (accountInfo != null && !spId.equals(accountInfo.getUserId())) {
                    LogC.i("sp is not equals user id.", false);
                    AccountManager.this.clearSPAndResetFlag(context);
                }
                if (accountInfo != null) {
                    AccountPreferences.getInstance(context).putString("user_id", accountInfo.getUserId());
                    AccountPreferences.getInstance(context).putString(AccountPreferences.LAST_USER_ID, accountInfo.getUserId());
                }
                AccountManager.this.getAccessToken(context, new NormalAccountGetATCallback());
            }

            public void onLoginError(int errorCode) {
                LogC.w(AccountManager.TAG, "monLoginError errorCode = " + errorCode, false);
            }
        });
    }

    public void destroy() {
        LogC.i(TAG, "account destroy", false);
        unRegisterReceiver();
    }

    public boolean hasLoginHWAccount(Context context) {
        if (context == null) {
            return false;
        }
        android.accounts.AccountManager systemAccountManager = android.accounts.AccountManager.get(context);
        if (systemAccountManager != null) {
            Account[] accs = systemAccountManager.getAccountsByType("com.huawei.hwid");
            if (accs != null && accs.length > 0) {
                return true;
            }
        }
        return false;
    }

    public AccountInfo getAccountInfo() {
        return this.mAccountInfo;
    }

    public void loginHWAccount(Context context, AccountLoginCallback loginCallback) {
        LogC.d(TAG, "loginHWAccount: method param context,loginCallback", false);
        loginHWAccount(context, false, loginCallback);
    }

    private void loginHWAccount(Context context, boolean isNeedAuth, AccountLoginCallback loginCallback) {
        if (loginCallback == null) {
            LogC.e(TAG, "loginHWAccount but loginCallback is null", false);
        } else if (context == null) {
            LogC.e(TAG, "loginHWAccount but context is null", false);
            loginCallback.onLoginError(AccountConst.ERRORCODE_INNER_ERROR);
        } else {
            if (isNeedAuth || TextUtils.isEmpty(this.mAccountInfo.getServiceToken()) || TextUtils.isEmpty(this.mAccountInfo.getServiceCountryCode())) {
                LogC.d(TAG, "loginHWAccount has login, get from server. isNeedAuth = " + isNeedAuth, false);
                if (!this.accountLoginCallbacks.contains(loginCallback)) {
                    this.accountLoginCallbacks.add(loginCallback);
                }
                getHWAccountFromHwID(context, isNeedAuth);
            } else {
                LogC.d(TAG, "loginHWAccount return login success from cache", false);
                loginCallback.onLoginSuccess(this.mAccountInfo);
            }
        }
    }

    private boolean isNoNeedReLogin(boolean isNeedAuth) {
        if (isNeedAuth) {
            if (atomIsAuthLogin.get()) {
                return true;
            }
            atomIsAuthLogin.set(true);
        } else if (atomIsNoAuthLogin.get()) {
            return true;
        } else {
            atomIsNoAuthLogin.set(true);
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004e, code lost:
        if (isNoNeedReLogin(r8) == false) goto L_0x0051;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0050, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0051, code lost:
        com.huawei.wallet.sdk.business.bankcard.util.ThreadPoolManager.getInstance().execute(new com.huawei.wallet.sdk.business.idcard.walletbase.logic.account.AccountManager.GetAccountRunnable(r7, instance, r8, r1));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005f, code lost:
        return;
     */
    private void getHWAccountFromHwID(Context context, boolean isNeedAuth) {
        this.accountFromWallet = false;
        this.isAccountHasLogin = hasLoginHWAccount(context);
        boolean isLoginByAIDL = true;
        if (!this.isAccountHasLogin || isNeedAuth) {
            this.accountFromWallet = true;
            isLoginByAIDL = false;
        }
        if (this.isAccountHasLogin && !TextUtils.isEmpty(this.mAccountInfo.getServiceToken())) {
            this.accountFromWallet = true;
        }
        LogC.d(TAG, "getHWAccountFromHwID isLogin:" + this.isLogin, false);
        synchronized (SYNC_LOCK_ISLOGIN) {
            if (!this.isLogin) {
                this.isLogin = true;
            }
        }
    }

    public void setIsLoginFalse() {
        this.isLogin = false;
    }

    public void getAccessToken(Context context, AccountGetATCallback atCallback) {
        if (atCallback == null) {
            LogC.e(TAG, "getAccessToken but atCallback is null", false);
        } else if (context == null) {
            LogC.e("getAccessToken but serviceToken is null", false);
            atCallback.onGetATError(AccountConst.ERRORCODE_INNER_ERROR, String.valueOf(AccountConst.ERRORCODE_INNER_ERROR));
        } else {
            if (!this.accountAtCallbacks.contains(atCallback)) {
                this.accountAtCallbacks.add(atCallback);
            }
            ThreadPoolManager.getInstance().execute(new GetATRunnable(context));
        }
    }

    /* access modifiers changed from: private */
    public void notifyLoginSuccess(AccountInfo accountInfo) {
        for (AccountStateListener listener : this.accountStateListeners) {
            listener.onAccountLogin(accountInfo);
        }
    }

    /* access modifiers changed from: private */
    public void notifyGetATSuccess(String acToken) {
        for (AccountGetATCallback callback : this.accountAtCallbacks) {
            callback.onGetATSuccess(acToken);
        }
        this.accountAtCallbacks.clear();
    }

    /* access modifiers changed from: private */
    public void notifyGetATError(int errorcode, String description) {
        for (AccountGetATCallback callback : this.accountAtCallbacks) {
            callback.onGetATError(errorcode, description);
        }
        this.accountAtCallbacks.clear();
    }

    /* access modifiers changed from: private */
    public void notifyNickNameChanged(String nickName) {
        for (AccountInfoListener listener : this.accountInfoListeners) {
            listener.onAccountNickNameChanged(nickName);
        }
    }

    /* access modifiers changed from: private */
    public void notifyAccountLoginError(int errorCode) {
        for (AccountLoginCallback listener : this.accountLoginCallbacks) {
            listener.onLoginError(errorCode);
        }
        removeAccountLoginCallbacks();
    }

    /* access modifiers changed from: private */
    public void notifyAccountLoginSuc() {
        for (AccountLoginCallback listener : this.accountLoginCallbacks) {
            listener.onLoginSuccess(this.mAccountInfo);
        }
        removeAccountLoginCallbacks();
    }

    public void registerReceiver(Context context) {
        LogC.i(TAG, "AccountManager: registerReceiver ", false);
        AccountReceiveManager.getInstance().registerReceiver(context, new AccountReceiveCallback() {
            public void onAccountRemove() {
            }

            public void onAccountNameChanged() {
            }

            public void onAccountHeadChanged() {
            }
        });
    }

    private void unRegisterReceiver() {
        AccountReceiveManager.getInstance().unRegisterReceiver();
    }

    public boolean registerAccountStateListener(AccountStateListener listener) {
        if (listener == null) {
            LogC.e(TAG, "registerAccountStateListener but listener is null", false);
            return false;
        } else if (this.accountStateListeners.contains(listener)) {
            return false;
        } else {
            return this.accountStateListeners.add(listener);
        }
    }

    public boolean unregisterAccountStateListener(AccountStateListener listener) {
        if (listener != null) {
            return this.accountStateListeners.remove(listener);
        }
        LogC.w(TAG, "unregisterAccountStateListener but listener is null", false);
        return false;
    }

    private void removeAccountLoginCallbacks() {
        this.accountLoginCallbacks.clear();
    }

    /* access modifiers changed from: private */
    public void getAccessTokenFromHwID(Context context, AccountGetATCallback atCallback) {
    }
}
