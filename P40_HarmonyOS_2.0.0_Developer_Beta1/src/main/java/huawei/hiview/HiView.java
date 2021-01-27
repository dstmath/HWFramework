package huawei.hiview;

import android.os.Process;
import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
import java.util.Date;
import java.util.Random;
import org.json.JSONObject;

public class HiView {
    private static final int BEGIN_API_ID = 9001;
    private static final int BIT_NANO2MICR = 1000;
    private static final int END_API_ID = 9002;
    private static final int ID_RANDOM_RANGE = 10000;
    private static final int MAX_RAW_PARAMS_SIZE = 15;
    private static final int MAX_RAW_TYPE_SIZE = 5;
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

    public static HiEvent buildRawEvent(int eventId, Object... values) {
        HiEvent event = new HiEvent(eventId);
        if (values == null || values.length == 0) {
            return event;
        }
        event.setTime(new Date());
        String[] longParaKeys = {"LONG1", "LONG2", "LONG3", "LONG4", "LONG5"};
        String[] floatParaKeys = {"FLOAT1", "FLOAT2", "FLOAT3", "FLOAT4", "FLOAT5"};
        String[] stringParaKeys = {"STR1", "STR2", "STR3", "STR4", "STR5"};
        int length = Math.min(values.length, 15);
        int index = 0;
        int indexLongParams = 0;
        int indexFloatParams = 0;
        int indexStringParams = 0;
        while (index < length && values[index] != null) {
            if (((values[index] instanceof Byte) || (values[index] instanceof Short) || (values[index] instanceof Integer) || (values[index] instanceof Long)) && indexLongParams < 5) {
                event.putLong(longParaKeys[indexLongParams], ((Number) values[index]).longValue());
                indexLongParams++;
            } else if ((values[index] instanceof Boolean) && indexLongParams < 5) {
                event.putBool(longParaKeys[indexLongParams], ((Boolean) values[index]).booleanValue());
                indexLongParams++;
            } else if (((values[index] instanceof Float) || (values[index] instanceof Double)) && indexFloatParams < 5) {
                float floatValue = ((Number) values[index]).floatValue();
                if (Float.isInfinite(floatValue)) {
                    Log.e(TAG, "The value exceeds the Float range, and set floatValue 0");
                    floatValue = 0.0f;
                }
                event.putFloat(floatParaKeys[indexFloatParams], floatValue);
                indexFloatParams++;
            } else if (!(values[index] instanceof String) || indexStringParams >= 5) {
                Log.e(TAG, "The raw event parameter error, the parameter index is " + index);
            } else {
                event.putString(stringParaKeys[indexStringParams], (String) values[index]);
                indexStringParams++;
            }
            index++;
        }
        return event;
    }

    public static void sendRawEvent(int eventId, Object... values) {
        report(buildRawEvent(eventId, values));
    }

    public static int getId() {
        return (Process.myTid() * 10000) + (new Random().nextInt(10000) % 10000);
    }

    public static void beginApi(int id, String callerPackage, String service, String version, String apiName) {
        HiEvent beginEvent = new HiEvent(BEGIN_API_ID);
        beginEvent.putInt("ID", id);
        beginEvent.putString("CALLERPACKAGE", callerPackage);
        beginEvent.putString("SERVICE", service);
        beginEvent.putString("VERSION", version);
        beginEvent.putString("APINAME", apiName);
        beginEvent.putInt("TIME", (int) ((System.nanoTime() / 1000) & 2147483647L));
        report(beginEvent);
    }

    public static void beginApi(int id, String service, String version, String apiName) {
        beginApi(id, StorageManagerExt.INVALID_KEY_DESC, service, version, apiName);
    }

    public static void endApi(int id, int result) {
        HiEvent endEvent = new HiEvent(END_API_ID);
        endEvent.putInt("ID", id);
        endEvent.putInt("RESULT", result);
        endEvent.putInt("TIME", (int) ((System.nanoTime() / 1000) & 2147483647L));
        report(endEvent);
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
