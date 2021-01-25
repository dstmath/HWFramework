package ohos.hiviewdfx;

import ohos.app.Context;
import ohos.utils.zson.ZSONObject;

public class HiView {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218115329, "HiView");
    private static Writer writer;

    public enum PayloadMode {
        KVPAIRS,
        VONLY
    }

    public interface Reportable {
        HiEvent toHiEvent();
    }

    public static void report(HiEvent hiEvent) {
        reportImpl(hiEvent);
    }

    public static void report(Reportable reportable) {
        reportImpl(reportable.toHiEvent());
    }

    public static HiEvent byVariadic(int i, PayloadMode payloadMode, Object... objArr) {
        HiEvent hiEvent = new HiEvent(i);
        int length = objArr.length;
        if (length != 0 && length <= 256) {
            Payload payload = hiEvent.getPayload();
            if (payloadMode == PayloadMode.KVPAIRS) {
                variadicKVPairs(payload, objArr);
            }
            if (payloadMode == PayloadMode.VONLY) {
                variadicVOnly(payload, objArr);
            }
        }
        return hiEvent;
    }

    public static HiEvent byPair(int i, String str, boolean z) {
        HiEvent hiEvent = new HiEvent(i);
        hiEvent.putBool(str, z);
        return hiEvent;
    }

    public static HiEvent byPair(int i, String str, byte b) {
        HiEvent hiEvent = new HiEvent(i);
        hiEvent.putByte(str, b);
        return hiEvent;
    }

    public static HiEvent byPair(int i, String str, int i2) {
        HiEvent hiEvent = new HiEvent(i);
        hiEvent.putInt(str, i2);
        return hiEvent;
    }

    public static HiEvent byPair(int i, String str, long j) {
        HiEvent hiEvent = new HiEvent(i);
        hiEvent.putLong(str, j);
        return hiEvent;
    }

    public static HiEvent byPair(int i, String str, float f) {
        HiEvent hiEvent = new HiEvent(i);
        hiEvent.putFloat(str, f);
        return hiEvent;
    }

    public static HiEvent byPair(int i, String str, String str2) {
        HiEvent hiEvent = new HiEvent(i);
        hiEvent.putString(str, str2);
        return hiEvent;
    }

    public static HiEvent byJson(int i, String str) {
        HiEvent hiEvent = new HiEvent(i);
        hiEvent.putPayload(new JsonPayload(str));
        return hiEvent;
    }

    public static HiEvent byJson(int i, ZSONObject zSONObject) {
        HiEvent hiEvent = new HiEvent(i);
        hiEvent.putPayload(new JsonPayload(zSONObject));
        return hiEvent;
    }

    public static HiEvent byContent(int i, Context context, String str) {
        if (str == null || str.isEmpty()) {
            return new HiEvent(i);
        }
        if (!str.startsWith("{") && !str.endsWith("}")) {
            str = "{" + str + "}";
        }
        HiEvent byJson = byJson(i, str);
        byJson.putAppInfo(context);
        return byJson;
    }

    private static synchronized void reportImpl(HiEvent hiEvent) {
        synchronized (HiView.class) {
            if (writer == null) {
                writer = new NativeWriter();
            }
            try {
                hiEvent.setTraceInfo();
                writer.write(hiEvent.flatten());
            } catch (UnsatisfiedLinkError e) {
                HiLog.error(LABEL, "NativeWriter Exception: %{public}s", e.getMessage());
            }
        }
    }

    private static void variadicKVPairs(Payload payload, Object... objArr) {
        try {
            int i = 512;
            if (objArr.length < 512) {
                i = objArr.length;
            }
            String str = "";
            for (int i2 = 0; i2 < i; i2++) {
                if ((i2 & 1) == 1) {
                    payload.put(str, objArr[i2]);
                } else if (objArr[i2] instanceof String) {
                    str = (String) objArr[i2];
                } else {
                    str = "";
                }
            }
        } catch (ClassCastException unused) {
            HiLog.error(LABEL, "byVariadic payload's key must be String", new Object[0]);
        }
    }

    private static void variadicVOnly(Payload payload, Object... objArr) {
        int i = 256;
        if (objArr.length < 256) {
            i = objArr.length;
        }
        for (int i2 = 0; i2 < i; i2++) {
            payload.put(String.valueOf(i2), objArr[i2]);
        }
    }
}
