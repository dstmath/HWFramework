package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.role.IRoleManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Telephony;
import android.telephony.IFinancialSmsCallback;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import com.huawei.internal.telephony.dataconnection.ApnSettingHelper;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AppSmsManager {
    private static final String LOG_TAG = "AppSmsManager";
    private static final long TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(5);
    private final Context mContext;
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private final Map<String, AppRequestInfo> mPackageMap = new ArrayMap();
    private final SecureRandom mRandom = new SecureRandom();
    @GuardedBy({"mLock"})
    private final Map<String, AppRequestInfo> mTokenMap = new ArrayMap();

    public AppSmsManager(Context context) {
        this.mContext = context;
    }

    public String createAppSpecificSmsToken(String callingPkg, PendingIntent intent) {
        ((AppOpsManager) this.mContext.getSystemService("appops")).checkPackage(Binder.getCallingUid(), callingPkg);
        String token = generateNonce();
        synchronized (this.mLock) {
            if (this.mPackageMap.containsKey(callingPkg)) {
                removeRequestLocked(this.mPackageMap.get(callingPkg));
            }
            addRequestLocked(new AppRequestInfo(this, callingPkg, intent, token));
        }
        return token;
    }

    public String createAppSpecificSmsTokenWithPackageInfo(int subId, String callingPackageName, String prefixes, PendingIntent intent) {
        Preconditions.checkStringNotEmpty(callingPackageName, "callingPackageName cannot be null or empty.");
        Preconditions.checkNotNull(intent, "intent cannot be null");
        ((AppOpsManager) this.mContext.getSystemService("appops")).checkPackage(Binder.getCallingUid(), callingPackageName);
        String token = PackageBasedTokenUtil.generateToken(this.mContext, callingPackageName);
        if (token != null) {
            synchronized (this.mLock) {
                if (this.mPackageMap.containsKey(callingPackageName)) {
                    removeRequestLocked(this.mPackageMap.get(callingPackageName));
                }
                addRequestLocked(new AppRequestInfo(callingPackageName, intent, token, prefixes, subId, true));
            }
        }
        return token;
    }

    public void getSmsMessagesForFinancialApp(String callingPkg, Bundle params, IFinancialSmsCallback callback) {
        try {
            IRoleManager.Stub.asInterface(ServiceManager.getServiceOrThrow("role")).getSmsMessagesForFinancialApp(callingPkg, params, callback);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Receive RemoteException.");
        } catch (ServiceManager.ServiceNotFoundException e2) {
            Log.e(LOG_TAG, "Service not found.");
        }
    }

    public boolean handleSmsReceivedIntent(Intent intent) {
        if (intent.getAction() != "android.provider.Telephony.SMS_DELIVER") {
            Log.wtf(LOG_TAG, "Got intent with incorrect action: " + intent.getAction());
            return false;
        }
        synchronized (this.mLock) {
            removeExpiredTokenLocked();
            String message = extractMessage(intent);
            if (TextUtils.isEmpty(message)) {
                return false;
            }
            AppRequestInfo info = findAppRequestInfoSmsIntentLocked(message);
            if (info == null) {
                return false;
            }
            try {
                info.pendingIntent.send(this.mContext, 0, new Intent().putExtras(intent.getExtras()).putExtra("android.telephony.extra.STATUS", 0).putExtra("android.telephony.extra.SMS_MESSAGE", message).putExtra("android.telephony.extra.SIM_SUBSCRIPTION_ID", info.subId).addFlags(ApnSettingHelper.TYPE_BIP6));
                removeRequestLocked(info);
                return true;
            } catch (PendingIntent.CanceledException e) {
                removeRequestLocked(info);
                return false;
            }
        }
    }

    private void removeExpiredTokenLocked() {
        long currentTimeMillis = System.currentTimeMillis();
        for (String token : this.mTokenMap.keySet()) {
            AppRequestInfo request = this.mTokenMap.get(token);
            if (request.packageBasedToken && currentTimeMillis - TIMEOUT_MILLIS > request.timestamp) {
                try {
                    request.pendingIntent.send(this.mContext, 0, new Intent().putExtra("android.telephony.extra.STATUS", 1).addFlags(ApnSettingHelper.TYPE_BIP6));
                } catch (PendingIntent.CanceledException e) {
                }
                removeRequestLocked(request);
            }
        }
    }

    private String extractMessage(Intent intent) {
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (messages == null) {
            return null;
        }
        StringBuilder fullMessageBuilder = new StringBuilder();
        for (SmsMessage message : messages) {
            if (!(message == null || message.getMessageBody() == null)) {
                fullMessageBuilder.append(message.getMessageBody());
            }
        }
        return fullMessageBuilder.toString();
    }

    private AppRequestInfo findAppRequestInfoSmsIntentLocked(String fullMessage) {
        for (String token : this.mTokenMap.keySet()) {
            if (fullMessage.trim().contains(token) && hasPrefix(token, fullMessage)) {
                return this.mTokenMap.get(token);
            }
        }
        return null;
    }

    private String generateNonce() {
        byte[] bytes = new byte[8];
        this.mRandom.nextBytes(bytes);
        return Base64.encodeToString(bytes, 11);
    }

    private boolean hasPrefix(String token, String message) {
        AppRequestInfo request = this.mTokenMap.get(token);
        if (TextUtils.isEmpty(request.prefixes)) {
            return true;
        }
        for (String prefix : request.prefixes.split(",")) {
            if (message.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private void removeRequestLocked(AppRequestInfo info) {
        this.mTokenMap.remove(info.token);
        this.mPackageMap.remove(info.packageName);
    }

    private void addRequestLocked(AppRequestInfo info) {
        this.mTokenMap.put(info.token, info);
        this.mPackageMap.put(info.packageName, info);
    }

    /* access modifiers changed from: private */
    public final class AppRequestInfo {
        public final boolean packageBasedToken;
        public final String packageName;
        public final PendingIntent pendingIntent;
        public final String prefixes;
        public final int subId;
        public final long timestamp;
        public final String token;

        AppRequestInfo(AppSmsManager appSmsManager, String packageName2, PendingIntent pendingIntent2, String token2) {
            this(packageName2, pendingIntent2, token2, null, -1, false);
        }

        AppRequestInfo(String packageName2, PendingIntent pendingIntent2, String token2, String prefixes2, int subId2, boolean packageBasedToken2) {
            this.packageName = packageName2;
            this.pendingIntent = pendingIntent2;
            this.token = token2;
            this.timestamp = System.currentTimeMillis();
            this.prefixes = prefixes2;
            this.subId = subId2;
            this.packageBasedToken = packageBasedToken2;
        }
    }
}
