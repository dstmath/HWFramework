package com.huawei.wallet.sdk.business.idcard.walletbase.logic.account;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.wallet.sdk.common.log.LogC;

public class AccountReceiveManager {
    private static final String ACTION_ACCOUNT_AVATAR_CHANGE = "com.huawei.hwid.ACTION_HEAD_PIC_CHANGE";
    private static final String ACTION_ACCOUNT_NAME_CHANGE = "com.huawei.hwid.ACTION_ACCOUNTNAME_CHANGE";
    public static final String ACTION_ACCOUNT_REMOVE = "com.huawei.hwid.ACTION_REMOVE_ACCOUNT";
    private static volatile AccountReceiveManager instance;
    private AccountLogoutReceiver accountLogoutReceiver = null;
    private AccountNameChangeReceiver accountNameChangeReceiver = null;
    private AvatarChangeReceiver avatarChangeReceiver = null;
    /* access modifiers changed from: private */
    public AccountReceiveCallback mAccountReceiveCallback = null;
    private Context mContext = null;

    private class AccountLogoutReceiver extends BroadcastReceiver {
        private AccountLogoutReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String actionStr = intent.getAction();
                LogC.i("AccountLogoutReceiver", false);
                if (AccountReceiveManager.ACTION_ACCOUNT_REMOVE.equals(actionStr)) {
                    int i = 0;
                    boolean hasLogin = AccountManager.getInstance().hasLoginHWAccount(context);
                    while (hasLogin && i < 20) {
                        i++;
                        LogC.i("AccountLogoutReceiver i = " + i, false);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            LogC.i("InterruptedException", false);
                        }
                        hasLogin = AccountManager.getInstance().hasLoginHWAccount(context);
                    }
                    if (!AccountManager.getInstance().hasLoginHWAccount(context)) {
                        String userId = null;
                        try {
                            userId = intent.getStringExtra("userId");
                        } catch (Exception e2) {
                            LogC.i("AccountLogoutReceiver get userId exception.", false);
                        }
                        String memoryUserId = AccountManager.getInstance().getAccountInfo().getUserId();
                        if (!(memoryUserId == null || !memoryUserId.equals(userId) || AccountReceiveManager.this.mAccountReceiveCallback == null)) {
                            LogC.i("mAccountReceiveCallback onAccountRemove", false);
                            AccountReceiveManager.this.mAccountReceiveCallback.onAccountRemove();
                        }
                    }
                }
            }
        }
    }

    private class AccountNameChangeReceiver extends BroadcastReceiver {
        private AccountNameChangeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                LogC.i("AccountNameChangeReceiver", false);
                if (AccountReceiveManager.ACTION_ACCOUNT_NAME_CHANGE.equals(intent.getAction()) && AccountReceiveManager.this.mAccountReceiveCallback != null) {
                    AccountReceiveManager.this.mAccountReceiveCallback.onAccountNameChanged();
                }
            }
        }
    }

    private class AvatarChangeReceiver extends BroadcastReceiver {
        private AvatarChangeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                LogC.i("AvatarChangeReceiver", false);
                if (AccountReceiveManager.ACTION_ACCOUNT_AVATAR_CHANGE.equals(intent.getAction())) {
                    boolean isChange = false;
                    try {
                        isChange = intent.getBooleanExtra("headPicChange", false);
                    } catch (Exception e) {
                        LogC.i("AvatarChangeReceiver getBooleanExtra exception", false);
                    }
                    if (isChange && AccountReceiveManager.this.mAccountReceiveCallback != null) {
                        AccountReceiveManager.this.mAccountReceiveCallback.onAccountHeadChanged();
                    }
                }
            }
        }
    }

    public static AccountReceiveManager getInstance() {
        if (instance == null) {
            instance = new AccountReceiveManager();
        }
        return instance;
    }

    public void registerReceiver(Context context, AccountReceiveCallback AccountReceiveCallback) {
        if (this.mAccountReceiveCallback != null) {
            LogC.w("registerReceiver but receiver is already registered", false);
        } else if (context != null) {
            this.mContext = context.getApplicationContext();
            this.mAccountReceiveCallback = AccountReceiveCallback;
            registerAvatarChangeReceiver();
            registerAccountNameChangeReceiver();
            registerAccountLogoutReceiver();
            LogC.i("registerReceiver", false);
        } else {
            LogC.e("registerReceiver but context is null", false);
        }
    }

    public void unRegisterReceiver() {
        if (this.mContext != null) {
            unRegisterAccountLogoutReceiver();
            unRegisterAccountNameChangeReceiver();
            unRegisterAvatarChangeReceiver();
            this.mAccountReceiveCallback = null;
            return;
        }
        LogC.w("unRegisterReceiver but context is null", false);
    }

    private void registerAccountLogoutReceiver() {
        this.accountLogoutReceiver = new AccountLogoutReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ACCOUNT_REMOVE);
        intentFilter.setPriority(Integer.MAX_VALUE);
        this.mContext.registerReceiver(this.accountLogoutReceiver, intentFilter);
    }

    private void unRegisterAccountLogoutReceiver() {
        if (this.accountLogoutReceiver == null) {
            LogC.i("accountLogoutReceiver is null,need not unregisterReceiver!", false);
            return;
        }
        try {
            this.mContext.unregisterReceiver(this.accountLogoutReceiver);
            this.accountLogoutReceiver = null;
        } catch (Exception e) {
            LogC.e("AccountReceiveManager", "Register Exception", false);
        }
    }

    private void registerAvatarChangeReceiver() {
        this.avatarChangeReceiver = new AvatarChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ACCOUNT_AVATAR_CHANGE);
        intentFilter.setPriority(Integer.MAX_VALUE);
        this.mContext.registerReceiver(this.avatarChangeReceiver, intentFilter);
    }

    private void unRegisterAvatarChangeReceiver() {
        if (this.avatarChangeReceiver == null) {
            LogC.i("avatarChangeReceiver is null,need not unregisterReceiver!", false);
            return;
        }
        try {
            this.mContext.unregisterReceiver(this.avatarChangeReceiver);
            this.avatarChangeReceiver = null;
        } catch (Exception e) {
            LogC.e("AccountReceiveManager", "Register Exception", false);
        }
    }

    private void registerAccountNameChangeReceiver() {
        this.accountNameChangeReceiver = new AccountNameChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ACCOUNT_NAME_CHANGE);
        intentFilter.setPriority(Integer.MAX_VALUE);
        this.mContext.registerReceiver(this.accountNameChangeReceiver, intentFilter);
    }

    private void unRegisterAccountNameChangeReceiver() {
        if (this.accountNameChangeReceiver == null) {
            LogC.i("accountNameChangeReceiver is null,need not unregisterReceiver!", false);
            return;
        }
        try {
            this.mContext.unregisterReceiver(this.accountNameChangeReceiver);
            this.accountNameChangeReceiver = null;
        } catch (Exception e) {
            LogC.e("AccountReceiveManager", "Register Exception", false);
        }
    }
}
