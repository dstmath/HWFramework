package com.huawei.android.app;

import android.content.Context;
import android.util.Log;
import android.util.Singleton;
import huawei.hiview.HiView;
import org.json.JSONObject;

public class HiView {
    private static final Singleton<Boolean> SINGLETON_HIVIEW = new Singleton<Boolean>() {
        /* class com.huawei.android.app.HiView.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public Boolean create() {
            boolean hiViewDefined = true;
            try {
                Class.forName("huawei.hiview.HiView");
            } catch (ClassNotFoundException e) {
                hiViewDefined = false;
                Log.e(HiView.TAG, "ClassNotFoundException: huawei.hiview.HiView not exsist.");
            }
            return new Boolean(hiViewDefined);
        }
    };
    private static final String TAG = "HiView";

    public enum PayloadMode {
        KVPAIRS,
        VONLY
    }

    public interface Reportable {
        HiEvent toHiEvent();
    }

    private static boolean isHiEventSdkExist() {
        return ((Boolean) SINGLETON_HIVIEW.get()).booleanValue();
    }

    public static void report(HiEvent event) {
        if (!isHiEventSdkExist()) {
            Log.i(TAG, "huawei.hiview.HiView not defined in platform.");
        } else if (event != null) {
            huawei.hiview.HiView.report(event.getHiEvent());
        }
    }

    public static void report(Reportable reportable) {
        if (reportable != null) {
            report(reportable.toHiEvent());
        }
    }

    public static HiEvent byVariadic(int eventId, PayloadMode mode, Object... payload) {
        HiEvent eventExt = new HiEvent(eventId);
        if (isHiEventSdkExist() && mode != null) {
            if (mode == PayloadMode.KVPAIRS) {
                eventExt.setHiEvent(huawei.hiview.HiView.byVariadic(eventId, HiView.PayloadMode.KVPairs, payload));
            } else if (mode != PayloadMode.VONLY) {
                return eventExt;
            } else {
                eventExt.setHiEvent(huawei.hiview.HiView.byVariadic(eventId, HiView.PayloadMode.VOnly, payload));
            }
        }
        return eventExt;
    }

    public static HiEvent byPair(int eventId, String key, boolean value) {
        HiEvent eventExt = new HiEvent(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(huawei.hiview.HiView.byPair(eventId, key, value));
        return eventExt;
    }

    public static HiEvent byPair(int eventId, String key, byte value) {
        HiEvent eventExt = new HiEvent(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(huawei.hiview.HiView.byPair(eventId, key, value));
        return eventExt;
    }

    public static HiEvent byPair(int eventId, String key, int value) {
        HiEvent eventExt = new HiEvent(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(huawei.hiview.HiView.byPair(eventId, key, value));
        return eventExt;
    }

    public static HiEvent byPair(int eventId, String key, long value) {
        HiEvent eventExt = new HiEvent(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(huawei.hiview.HiView.byPair(eventId, key, value));
        return eventExt;
    }

    public static HiEvent byPair(int eventId, String key, float value) {
        HiEvent eventExt = new HiEvent(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(huawei.hiview.HiView.byPair(eventId, key, value));
        return eventExt;
    }

    public static HiEvent byPair(int eventId, String key, String value) {
        HiEvent eventExt = new HiEvent(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(huawei.hiview.HiView.byPair(eventId, key, value));
        return eventExt;
    }

    public static HiEvent byJson(int eventId, String json) {
        HiEvent eventExt = new HiEvent(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(huawei.hiview.HiView.byJson(eventId, json));
        return eventExt;
    }

    public static HiEvent byJson(int eventId, JSONObject json) {
        HiEvent eventExt = new HiEvent(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(huawei.hiview.HiView.byJson(eventId, json));
        return eventExt;
    }

    public static HiEvent byContent(int eventId, Context context, String content) {
        HiEvent eventExt = new HiEvent(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        if (content != null && !content.isEmpty()) {
            if (!content.startsWith("{") && !content.endsWith("}")) {
                StringBuilder json = new StringBuilder(content.length() + 2);
                json.append("{");
                json.append(content);
                json.append("}");
                content = json.toString();
            }
            eventExt.setHiEvent(huawei.hiview.HiView.byJson(eventId, content));
        }
        eventExt.putAppInfo(context);
        return eventExt;
    }

    public static int getId() {
        if (!isHiEventSdkExist()) {
            return -1;
        }
        return huawei.hiview.HiView.getId();
    }

    public static void beginApi(int id, String callerPackage, String service, String version, String apiName) {
        if (isHiEventSdkExist()) {
            huawei.hiview.HiView.beginApi(id, callerPackage, service, version, apiName);
        }
    }

    public static void beginApi(int id, String service, String version, String apiName) {
        if (isHiEventSdkExist()) {
            huawei.hiview.HiView.beginApi(id, service, version, apiName);
        }
    }

    public static void endApi(int id, int result) {
        if (isHiEventSdkExist()) {
            huawei.hiview.HiView.endApi(id, result);
        }
    }

    public static HiEvent buildRawEvent(int eventId, Object... values) {
        HiEvent eventExt = new HiEvent(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(huawei.hiview.HiView.buildRawEvent(eventId, values));
        return eventExt;
    }

    public static void sendRawEvent(int eventId, Object... values) {
        if (!isHiEventSdkExist()) {
            Log.i(TAG, "huawei.hiview.HiView not defined in platform.");
        } else {
            huawei.hiview.HiView.sendRawEvent(eventId, values);
        }
    }
}
