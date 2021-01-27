package com.huawei.android.app;

import android.content.Context;
import android.util.Log;
import android.util.Singleton;
import huawei.hiview.HiView;
import org.json.JSONObject;

public class HiViewEx {
    private static final Singleton<Boolean> SINGLETON_HIVIEW = new Singleton<Boolean>() {
        /* class com.huawei.android.app.HiViewEx.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public Boolean create() {
            boolean hiViewDefined = true;
            try {
                Class.forName("huawei.hiview.HiView");
            } catch (ClassNotFoundException e) {
                hiViewDefined = false;
                Log.e(HiViewEx.TAG, "ClassNotFoundException: huawei.hiview.HiView not exsist.");
            }
            return new Boolean(hiViewDefined);
        }
    };
    private static final String TAG = "HiViewEx";

    public enum PayloadMode {
        KVPAIRS,
        VONLY
    }

    public interface Reportable {
        HiEventEx toHiEvent();
    }

    private static boolean isHiEventSdkExist() {
        return ((Boolean) SINGLETON_HIVIEW.get()).booleanValue();
    }

    public static void report(HiEventEx event) {
        if (!isHiEventSdkExist()) {
            Log.i(TAG, "huawei.hiview.HiView not defined in platform.");
        } else if (event != null) {
            HiView.report(event.getHiEvent());
        }
    }

    public static void report(Reportable reportable) {
        if (reportable != null) {
            report(reportable.toHiEvent());
        }
    }

    public static HiEventEx byVariadic(int eventId, PayloadMode mode, Object... payload) {
        HiEventEx eventExt = new HiEventEx(eventId);
        if (isHiEventSdkExist() && mode != null) {
            if (mode == PayloadMode.KVPAIRS) {
                eventExt.setHiEvent(HiView.byVariadic(eventId, HiView.PayloadMode.KVPairs, payload));
            } else if (mode != PayloadMode.VONLY) {
                return eventExt;
            } else {
                eventExt.setHiEvent(HiView.byVariadic(eventId, HiView.PayloadMode.VOnly, payload));
            }
        }
        return eventExt;
    }

    public static HiEventEx byPair(int eventId, String key, boolean value) {
        HiEventEx eventExt = new HiEventEx(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(HiView.byPair(eventId, key, value));
        return eventExt;
    }

    public static HiEventEx byPair(int eventId, String key, byte value) {
        HiEventEx eventExt = new HiEventEx(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(HiView.byPair(eventId, key, value));
        return eventExt;
    }

    public static HiEventEx byPair(int eventId, String key, int value) {
        HiEventEx eventExt = new HiEventEx(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(HiView.byPair(eventId, key, value));
        return eventExt;
    }

    public static HiEventEx byPair(int eventId, String key, long value) {
        HiEventEx eventExt = new HiEventEx(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(HiView.byPair(eventId, key, value));
        return eventExt;
    }

    public static HiEventEx byPair(int eventId, String key, float value) {
        HiEventEx eventExt = new HiEventEx(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(HiView.byPair(eventId, key, value));
        return eventExt;
    }

    public static HiEventEx byPair(int eventId, String key, String value) {
        HiEventEx eventExt = new HiEventEx(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(HiView.byPair(eventId, key, value));
        return eventExt;
    }

    public static HiEventEx byJson(int eventId, String json) {
        HiEventEx eventExt = new HiEventEx(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(HiView.byJson(eventId, json));
        return eventExt;
    }

    public static HiEventEx byJson(int eventId, JSONObject json) {
        HiEventEx eventExt = new HiEventEx(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        eventExt.setHiEvent(HiView.byJson(eventId, json));
        return eventExt;
    }

    public static HiEventEx byContent(int eventId, Context context, String content) {
        HiEventEx eventExt = new HiEventEx(eventId);
        if (!isHiEventSdkExist()) {
            return eventExt;
        }
        if (content != null && !content.isEmpty()) {
            if (!content.startsWith("{") && !content.endsWith("}")) {
                content = "{" + content + "}";
            }
            eventExt.setHiEvent(HiView.byJson(eventId, content));
        }
        eventExt.putAppInfo(context);
        return eventExt;
    }
}
