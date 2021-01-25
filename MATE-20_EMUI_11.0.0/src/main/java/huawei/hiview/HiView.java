package huawei.hiview;

import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
import org.json.JSONObject;

public class HiView {
    private static final String TAG = "HiView";
    private static Writer writer;

    public enum PayloadMode {
        KVPairs,
        VOnly
    }

    public interface Reportable {
        HiEvent toHiEvent();
    }

    public static void report(HiEvent event) {
        reportImpl(event);
    }

    public static void report(Reportable reportable) {
        reportImpl(reportable.toHiEvent());
    }

    public static HiEvent byVariadic(int eventID, PayloadMode mode, Object... payload) {
        HiEvent event = new HiEvent(eventID);
        int size = payload.length;
        Log.i(TAG, "HiView ByVariadic payload size:" + size);
        if (size == 0 || size > 256) {
            return event;
        }
        Payload eventPayload = event.getPayload();
        if (mode == PayloadMode.KVPairs) {
            variadicKVPairs(eventPayload, payload);
        }
        if (mode == PayloadMode.VOnly) {
            variadicVOnly(eventPayload, payload);
        }
        return event;
    }

    public static HiEvent byPair(int eventID, String key, boolean value) {
        HiEvent event = new HiEvent(eventID);
        event.putBool(key, value);
        return event;
    }

    public static HiEvent byPair(int eventID, String key, byte value) {
        HiEvent event = new HiEvent(eventID);
        event.putByte(key, value);
        return event;
    }

    public static HiEvent byPair(int eventID, String key, int value) {
        HiEvent event = new HiEvent(eventID);
        event.putInt(key, value);
        return event;
    }

    public static HiEvent byPair(int eventID, String key, long value) {
        HiEvent event = new HiEvent(eventID);
        event.putLong(key, value);
        return event;
    }

    public static HiEvent byPair(int eventID, String key, float value) {
        HiEvent event = new HiEvent(eventID);
        event.putFloat(key, value);
        return event;
    }

    public static HiEvent byPair(int eventID, String key, String value) {
        HiEvent event = new HiEvent(eventID);
        event.putString(key, value);
        return event;
    }

    public static HiEvent byJson(int eventID, String json) {
        HiEvent event = new HiEvent(eventID);
        event.putPayload(new JsonPayload(json));
        return event;
    }

    public static HiEvent byJson(int eventID, JSONObject json) {
        HiEvent event = new HiEvent(eventID);
        event.putPayload(new JsonPayload(json));
        return event;
    }

    private static synchronized void reportImpl(HiEvent event) {
        synchronized (HiView.class) {
            if (writer == null) {
                writer = new NativeWriter();
            }
            try {
                event.setTraceInfo();
                writer.write(event.flatten());
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "NativeWriter Exception: " + e.getMessage());
            }
        }
    }

    private static void variadicKVPairs(Payload pl, Object... payload) {
        String key = StorageManagerExt.INVALID_KEY_DESC;
        int len = 512;
        if (payload.length < 512) {
            len = payload.length;
        }
        for (int pos = 0; pos < len; pos++) {
            if ((pos & 1) == 1) {
                pl.put(key, payload[pos]);
            } else if (payload[pos] instanceof String) {
                key = (String) payload[pos];
            } else {
                key = StorageManagerExt.INVALID_KEY_DESC;
            }
        }
    }

    private static void variadicVOnly(Payload pl, Object... payload) {
        int len = 256;
        if (payload.length < 256) {
            len = payload.length;
        }
        for (int pos = 0; pos < len; pos++) {
            pl.put(String.valueOf(pos), payload[pos]);
        }
    }
}
