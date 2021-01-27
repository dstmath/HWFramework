package com.android.server.swing;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import com.huawei.common.service.IDecision;
import com.huawei.common.service.IDecisionCallback;
import java.util.Map;

public class HwSwingDecisionUtil {
    private static final String ACTION_COMMON_SERVICE_NAME = "com.huawei.recsys.decision.action.BIND_DECISION_SERVICE";
    private static final String ACTION_SWING_GRAB_SCREEN_SHOT = "com.huawei.screenshot.intent.action.SWINGScreenshot";
    private static final String CATEGORY_KEY = "category";
    private static final String ID_KEY = "id";
    private static final String PACKAGE_NAME = "com.huawei.recsys";
    public static final String SWING_GRAB_SCREEN_SHOT_TYPE = "swing_grab_screen_shot";
    private static final String TAG = "HwSwingDecisionUtil";
    private static HwSwingDecisionUtil sInstance;
    private Context mContext = null;
    private IDecision mDecisionApi = null;
    private ServiceConnection mDecisionConnection = new ServiceConnection() {
        /* class com.android.server.swing.HwSwingDecisionUtil.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(HwSwingDecisionUtil.TAG, "service connected , mType : " + HwSwingDecisionUtil.this.mType);
            HwSwingDecisionUtil.this.mDecisionApi = IDecision.Stub.asInterface(service);
            if (HwSwingDecisionUtil.SWING_GRAB_SCREEN_SHOT_TYPE.equals(HwSwingDecisionUtil.this.mType)) {
                Log.i(HwSwingDecisionUtil.TAG, "Swing grab screenshot incident has been successfully escalated !");
                HwSwingDecisionUtil.this.executeEvent(HwSwingDecisionUtil.ACTION_SWING_GRAB_SCREEN_SHOT);
            } else {
                Log.i(HwSwingDecisionUtil.TAG, "Execute Event failed !");
            }
            HwSwingDecisionUtil.this.unbindService();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Log.i(HwSwingDecisionUtil.TAG, "service disconnect.");
            HwSwingDecisionUtil.this.unbindService();
        }
    };
    private String mType = null;

    public void bindService(Context context, String type) {
        this.mType = type;
        if (this.mType != null) {
            if (context == null || this.mDecisionApi != null) {
                Log.i(TAG, "service already bind ");
                return;
            }
            this.mContext = context;
            Intent actionService = new Intent(ACTION_COMMON_SERVICE_NAME);
            actionService.setPackage(PACKAGE_NAME);
            try {
                Log.i(TAG, "bindService.");
                context.bindService(actionService, this.mDecisionConnection, 1);
            } catch (RuntimeException e) {
                Log.e(TAG, "bind Service Failed !");
            }
        }
    }

    public void unbindService() {
        if (this.mContext != null) {
            Log.i(TAG, "unbindService.");
            try {
                this.mType = null;
                this.mContext.unbindService(this.mDecisionConnection);
                this.mDecisionApi = null;
            } catch (RuntimeException e) {
                Log.e(TAG, "unbind Service Failed !");
            }
        }
    }

    public boolean executeEvent(String eventName) {
        return executeEvent(eventName, null, null, null);
    }

    public boolean executeEvent(String eventName, Map<String, Object> extras) {
        return executeEvent(eventName, null, extras, null);
    }

    public boolean executeEvent(String eventName, String dataId, Map<String, Object> extras, IDecisionCallback callback) {
        if (this.mDecisionApi == null) {
            return false;
        }
        ArrayMap<String, Object> extra2 = new ArrayMap<>();
        if (extras != null) {
            extra2.putAll(extras);
        }
        extra2.put(ID_KEY, dataId != null ? dataId : "");
        if (eventName != null && !eventName.equals(dataId)) {
            extra2.put(CATEGORY_KEY, eventName);
        }
        try {
            this.mDecisionApi.executeEvent(extra2, callback);
            return true;
        } catch (RuntimeException e) {
            return false;
        } catch (RemoteException e2) {
            return false;
        }
    }

    public static synchronized HwSwingDecisionUtil getInstance() {
        HwSwingDecisionUtil hwSwingDecisionUtil;
        synchronized (HwSwingDecisionUtil.class) {
            if (sInstance == null) {
                sInstance = new HwSwingDecisionUtil();
            }
            hwSwingDecisionUtil = sInstance;
        }
        return hwSwingDecisionUtil;
    }

    public static void reportEvent(Context context, String action) {
        getInstance().bindService(context, action);
    }
}
