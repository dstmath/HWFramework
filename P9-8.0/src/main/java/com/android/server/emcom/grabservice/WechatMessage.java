package com.android.server.emcom.grabservice;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WechatMessage implements IMessage, Serializable {
    private static final String TAG = "GrabService";
    private static final long serialVersionUID = -8988492431073638830L;
    private Context context;
    private String conversation = "";
    private String from = "";
    private String message = "";
    PendingIntent pendingIntent;
    private int type = -1;
    String wechatId = "";

    public WechatMessage(Context paramContext, Notification paramNotification, String typeKey, String idKey) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        this.context = paramContext;
        this.conversation = getNotificationTitle(paramNotification);
        if (TextUtils.isEmpty(this.conversation)) {
            this.conversation = getAppName("com.tencent.mm");
        }
        this.message = getNotificationContent(paramNotification);
        if (this.message != null) {
            setFromAndMessage();
        }
        this.pendingIntent = paramNotification.contentIntent;
        if (this.pendingIntent != null) {
            setTypeAndId(typeKey, idKey);
        }
    }

    private void setFromAndMessage() {
        Matcher localMatcher = Pattern.compile("^\\[(.*?)条\\](.*?):(.*)").matcher(this.message);
        if (localMatcher.find()) {
            setFromAndMessage(localMatcher);
        }
    }

    private void setFromAndMessage(Matcher localMatcher) {
        try {
            this.from = localMatcher.group(2);
            this.message = localMatcher.group(3);
        } catch (IllegalStateException illegalStateException) {
            Log.e(TAG, "IllegalStateException occur.", illegalStateException);
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            Log.e(TAG, "IndexOutOfBoundsException occur.", indexOutOfBoundsException);
        }
    }

    private void setTypeAndId(String typeKey, String idKey) {
        try {
            Intent localIntent = null;
            try {
                localIntent = (Intent) this.pendingIntent.getClass().getDeclaredMethod("getIntent", new Class[0]).invoke(this.pendingIntent, new Object[0]);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "access illegal.", e);
            } catch (InvocationTargetException e2) {
                Log.e(TAG, "invocation error.", e2);
            }
            if (!(localIntent == null || localIntent.getExtras() == null)) {
                Bundle localBundle = localIntent.getExtras();
                if (localBundle != null) {
                    this.type = localBundle.getInt(typeKey);
                    this.wechatId = localBundle.getString(idKey);
                }
            }
        } catch (NoSuchMethodException e3) {
            Log.e(TAG, "no such method.");
        }
    }

    private String getAppName(String paramString) {
        if (TextUtils.isEmpty(paramString)) {
            return "";
        }
        try {
            ApplicationInfo localApplicationInfo = this.context.getPackageManager().getApplicationInfo(paramString, 0);
            CharSequence localObject = null;
            if (localApplicationInfo != null) {
                localObject = this.context.getPackageManager().getApplicationLabel(localApplicationInfo);
            }
            if (localObject != null) {
                return localObject.toString();
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "class name not found!.");
        }
        return "";
    }

    public boolean isGroupMessage(String identifier) {
        if (TextUtils.isEmpty(this.wechatId)) {
            return this.conversation.equals(this.from) ^ 1;
        }
        return this.wechatId.contains(identifier);
    }

    public boolean isMoney(String identifier, int typeCode) {
        boolean z = false;
        if (TextUtils.isEmpty(this.message)) {
            return false;
        }
        if (this.type == -1) {
            return this.message.contains(identifier);
        }
        if (this.type == typeCode) {
            z = this.message.contains(identifier);
        }
        return z;
    }

    private String getNotificationContent(Notification notification) {
        CharSequence localCharSequence = null;
        if (VERSION.SDK_INT >= 19) {
            localCharSequence = null;
            if (notification.extras != null) {
                localCharSequence = notification.extras.getCharSequence("android.text");
            }
        }
        if (TextUtils.isEmpty(localCharSequence)) {
            localCharSequence = notification.tickerText;
        }
        if (localCharSequence == null) {
            return null;
        }
        return localCharSequence.toString();
    }

    private String getNotificationTitle(Notification notification) {
        CharSequence localCharSequence = null;
        if (VERSION.SDK_INT >= 19) {
            localCharSequence = notification.extras.getCharSequence("android.title");
        }
        if (localCharSequence == null) {
            return null;
        }
        return localCharSequence.toString();
    }
}
