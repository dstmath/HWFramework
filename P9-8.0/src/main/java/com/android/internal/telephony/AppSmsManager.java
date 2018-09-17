package com.android.internal.telephony;

import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.provider.Telephony.Sms.Intents;
import android.telephony.SmsMessage;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import java.security.SecureRandom;
import java.util.Map;

public class AppSmsManager {
    private static final String LOG_TAG = "AppSmsManager";
    private final Context mContext;
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private final Map<String, AppRequestInfo> mPackageMap = new ArrayMap();
    private final SecureRandom mRandom = new SecureRandom();
    @GuardedBy("mLock")
    private final Map<String, AppRequestInfo> mTokenMap = new ArrayMap();

    private final class AppRequestInfo {
        public final String packageName;
        public final PendingIntent pendingIntent;
        public final String token;

        AppRequestInfo(String packageName, PendingIntent pendingIntent, String token) {
            this.packageName = packageName;
            this.pendingIntent = pendingIntent;
            this.token = token;
        }
    }

    public AppSmsManager(Context context) {
        this.mContext = context;
    }

    public String createAppSpecificSmsToken(String callingPkg, PendingIntent intent) {
        ((AppOpsManager) this.mContext.getSystemService("appops")).checkPackage(Binder.getCallingUid(), callingPkg);
        String token = generateNonce();
        synchronized (this.mLock) {
            if (this.mPackageMap.containsKey(callingPkg)) {
                removeRequestLocked((AppRequestInfo) this.mPackageMap.get(callingPkg));
            }
            addRequestLocked(new AppRequestInfo(callingPkg, intent, token));
        }
        return token;
    }

    public boolean handleSmsReceivedIntent(Intent intent) {
        if (intent.getAction() != "android.provider.Telephony.SMS_DELIVER") {
            Log.wtf(LOG_TAG, "Got intent with incorrect action: " + intent.getAction());
            return false;
        }
        synchronized (this.mLock) {
            AppRequestInfo info = findAppRequestInfoSmsIntentLocked(intent);
            if (info == null) {
                return false;
            }
            try {
                Intent fillIn = new Intent();
                fillIn.putExtras(intent.getExtras());
                info.pendingIntent.send(this.mContext, 0, fillIn);
                removeRequestLocked(info);
                return true;
            } catch (CanceledException e) {
                removeRequestLocked(info);
                return false;
            }
        }
    }

    private AppRequestInfo findAppRequestInfoSmsIntentLocked(Intent intent) {
        SmsMessage[] messages = Intents.getMessagesFromIntent(intent);
        if (messages == null) {
            return null;
        }
        StringBuilder fullMessageBuilder = new StringBuilder();
        for (SmsMessage message : messages) {
            if (message.getMessageBody() != null) {
                fullMessageBuilder.append(message.getMessageBody());
            }
        }
        String fullMessage = fullMessageBuilder.toString();
        for (String token : this.mTokenMap.keySet()) {
            if (fullMessage.contains(token)) {
                return (AppRequestInfo) this.mTokenMap.get(token);
            }
        }
        return null;
    }

    private String generateNonce() {
        byte[] bytes = new byte[8];
        this.mRandom.nextBytes(bytes);
        return Base64.encodeToString(bytes, 11);
    }

    private void removeRequestLocked(AppRequestInfo info) {
        this.mTokenMap.remove(info.token);
        this.mPackageMap.remove(info.packageName);
    }

    private void addRequestLocked(AppRequestInfo info) {
        this.mTokenMap.put(info.token, info);
        this.mPackageMap.put(info.packageName, info);
    }
}
