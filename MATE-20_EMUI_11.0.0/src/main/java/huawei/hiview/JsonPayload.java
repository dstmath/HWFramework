package huawei.hiview;

import android.util.Log;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonPayload implements Payload {
    private static final String TAG = "HiEvent.Payload";
    private JSONObject mJson;

    public JsonPayload() {
        this.mJson = new JSONObject();
    }

    public JsonPayload(String json) {
        try {
            this.mJson = new JSONObject(json);
        } catch (JSONException e) {
            Log.e(TAG, "new JsonPayload exception:" + e.getMessage());
            this.mJson = new JSONObject();
        }
    }

    public JsonPayload(JSONObject json) {
        this.mJson = json;
    }

    public String toString() {
        return this.mJson.toString();
    }

    public JSONObject toJson() {
        return this.mJson;
    }

    @Override // huawei.hiview.Payload
    public int size() {
        return this.mJson.length();
    }

    @Override // huawei.hiview.Payload
    public void merge(Payload other) {
        Iterator<String> iterator = other.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            try {
                this.mJson.put(key, other.get(key));
            } catch (JSONException e) {
                Log.e(TAG, "merge error:" + e.getMessage());
            }
        }
    }

    @Override // huawei.hiview.Payload
    public void clear() {
        this.mJson = null;
        this.mJson = new JSONObject();
    }

    @Override // huawei.hiview.Payload
    public Object get(String key) {
        try {
            return this.mJson.get(key);
        } catch (JSONException e) {
            Log.e(TAG, "get error: " + e.getMessage());
            return null;
        }
    }

    @Override // huawei.hiview.Payload
    public Iterator<String> keys() {
        return this.mJson.keys();
    }

    @Override // huawei.hiview.Payload
    public void put(String key, boolean value) {
        try {
            this.mJson.put(key, value ? 1 : 0);
        } catch (JSONException e) {
            Log.e(TAG, "put boolean error: " + e.getMessage());
        }
    }

    @Override // huawei.hiview.Payload
    public void put(String key, byte value) {
        try {
            this.mJson.put(key, (int) value);
        } catch (JSONException e) {
            Log.e(TAG, "put byte error: " + e.getMessage());
        }
    }

    @Override // huawei.hiview.Payload
    public void put(String key, short value) {
        try {
            this.mJson.put(key, (int) value);
        } catch (JSONException e) {
            Log.e(TAG, "put short error: " + e.getMessage());
        }
    }

    @Override // huawei.hiview.Payload
    public void put(String key, int value) {
        try {
            this.mJson.put(key, value);
        } catch (JSONException e) {
            Log.e(TAG, "put int error: " + e.getMessage());
        }
    }

    @Override // huawei.hiview.Payload
    public void put(String key, long value) {
        try {
            this.mJson.put(key, value);
        } catch (JSONException e) {
            Log.e(TAG, "put long error: " + e.getMessage());
        }
    }

    @Override // huawei.hiview.Payload
    public void put(String key, float value) {
        try {
            this.mJson.put(key, (double) value);
        } catch (JSONException e) {
            Log.e(TAG, "put float error: " + e.getMessage());
        }
    }

    @Override // huawei.hiview.Payload
    public void put(String key, String value) {
        try {
            this.mJson.put(key, value);
        } catch (JSONException e) {
            Log.e(TAG, "put string error: " + e.getMessage());
        }
    }

    @Override // huawei.hiview.Payload
    public void put(String key, Payload value) {
        try {
            if (value instanceof JsonPayload) {
                this.mJson.putOpt(key, ((JsonPayload) value).toJson());
            } else {
                Log.e(TAG, "put Payload type unmatched");
            }
        } catch (JSONException e) {
            Log.e(TAG, "put Payload error: " + e.getMessage());
        }
    }

    @Override // huawei.hiview.Payload
    public void put(String key, Object value) {
        try {
            this.mJson.putOpt(key, value);
        } catch (JSONException e) {
            Log.e(TAG, "put Object error: " + e.getMessage());
        }
    }

    private void appendInternal(String key, Object value) {
        JSONArray array = this.mJson.optJSONArray(key);
        if (array != null) {
            array.put(value);
            return;
        }
        JSONArray array2 = new JSONArray();
        array2.put(value);
        try {
            this.mJson.put(key, array2);
        } catch (JSONException e) {
            Log.e(TAG, "put array error: " + e.getMessage());
        }
    }

    @Override // huawei.hiview.Payload
    public void append(String key, boolean value) {
        appendInternal(key, Integer.valueOf(value ? 1 : 0));
    }

    @Override // huawei.hiview.Payload
    public void append(String key, byte value) {
        appendInternal(key, Byte.valueOf(value));
    }

    @Override // huawei.hiview.Payload
    public void append(String key, short value) {
        appendInternal(key, Short.valueOf(value));
    }

    @Override // huawei.hiview.Payload
    public void append(String key, int value) {
        appendInternal(key, Integer.valueOf(value));
    }

    @Override // huawei.hiview.Payload
    public void append(String key, long value) {
        appendInternal(key, Long.valueOf(value));
    }

    @Override // huawei.hiview.Payload
    public void append(String key, float value) {
        appendInternal(key, Float.valueOf(value));
    }

    @Override // huawei.hiview.Payload
    public void append(String key, String value) {
        appendInternal(key, value);
    }

    @Override // huawei.hiview.Payload
    public void append(String key, Payload value) {
        if (value != null) {
            if (value instanceof JsonPayload) {
                appendInternal(key, ((JsonPayload) value).toJson());
            } else {
                Log.e(TAG, "append Payload type unmatched");
            }
        }
    }
}
