package com.android.server.emcom.grabservice;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import com.android.server.emcom.grabservice.AppInfo.EventInfo;
import com.android.server.emcom.grabservice.AutoGrabService.AccessibilityEventCallback;
import com.huawei.android.bastet.IBastetManager;
import com.huawei.android.bastet.IBastetManager.Stub;
import java.util.ArrayList;
import java.util.List;

class AppStateWatcher implements AccessibilityEventCallback {
    private static final String TAG = "GrabService";
    private Context mContext;
    private List<EventInfo> mWatchEvents;

    AppStateWatcher(Context context) {
        this.mWatchEvents = new ArrayList();
        this.mContext = context;
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        for (EventInfo info : this.mWatchEvents) {
            if (info.packageName.equals(event.getPackageName()) && info.eventType == event.getEventType() && info.eventClassName.equals(event.getClassName())) {
                Log.d(TAG, "event type and className match.");
                if (!TextUtils.isEmpty(info.description)) {
                    CharSequence description = event.getContentDescription();
                    Log.d(TAG, "check description." + info.description);
                    if (description == null || !description.toString().contains(info.description)) {
                        Log.d(TAG, "description  not match!");
                    }
                }
                if (!TextUtils.isEmpty(info.text) && !isTextMatch(event, info)) {
                    Log.d(TAG, "text not match!");
                } else if (TextUtils.isEmpty(info.activity) || AutoGrabTools.getTopActivity(this.mContext).equals(info.activity)) {
                    sendAccelerateAcion(info.uid);
                    return;
                } else {
                    Log.d(TAG, "activity not match!");
                }
            }
        }
    }

    private void sendAccelerateAcion(int uid) {
        IBastetManager bastet = Stub.asInterface(ServiceManager.getService("BastetService"));
        if (bastet != null) {
            try {
                bastet.indicateAction(0, 100, uid);
                Log.d(TAG, "send accelerate action to bastet, uid:" + uid);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed calling BastetService");
            }
        }
    }

    private boolean isTextMatch(AccessibilityEvent event, EventInfo info) {
        boolean isMatch = false;
        List<CharSequence> texts = event.getText();
        if (texts != null && texts.size() > 0) {
            for (CharSequence cs : texts) {
                if (cs.toString().equals(info.text)) {
                    isMatch = true;
                }
            }
        }
        return isMatch;
    }

    public void deleteAppConfig(String packageName) {
        for (int i = 0; i < this.mWatchEvents.size(); i++) {
            EventInfo info = (EventInfo) this.mWatchEvents.get(i);
            if (info.packageName.equals(packageName)) {
                this.mWatchEvents.remove(info);
            }
        }
    }

    public void deleteAllConfig() {
        this.mWatchEvents.clear();
    }

    public void addAppConfig(AppInfo appInfo) {
        deleteAppConfig(appInfo.packageName);
        for (EventInfo info : appInfo.events) {
            this.mWatchEvents.add(info);
        }
    }
}
