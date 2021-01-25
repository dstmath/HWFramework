package ohos.hiviewdfx;

import java.util.Iterator;
import ohos.utils.zson.ZSONArray;
import ohos.utils.zson.ZSONObject;

class JsonPayload implements Payload {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218115329, "JsonPayload");
    private ZSONObject mJson;

    public JsonPayload() {
        this.mJson = new ZSONObject();
    }

    public JsonPayload(String str) {
        try {
            this.mJson = ZSONObject.stringToZSON(str);
        } catch (Exception e) {
            HiLog.error(LABEL, "new JsonPayload exception:%{public}s", e.getMessage());
            this.mJson = new ZSONObject();
        }
    }

    public JsonPayload(ZSONObject zSONObject) {
        this.mJson = zSONObject;
    }

    public String toString() {
        return ZSONObject.toZSONString(this.mJson);
    }

    public ZSONObject toJson() {
        return this.mJson;
    }

    @Override // ohos.hiviewdfx.Payload
    public int size() {
        return this.mJson.size();
    }

    @Override // ohos.hiviewdfx.Payload
    public void merge(Payload payload) {
        Iterator<String> keys = payload.keys();
        while (keys.hasNext()) {
            String next = keys.next();
            try {
                this.mJson.put(next, payload.get(next));
            } catch (Exception e) {
                HiLog.error(LABEL, "merge error:%{public}s", e.getMessage());
            }
        }
    }

    @Override // ohos.hiviewdfx.Payload
    public void clear() {
        this.mJson = null;
        this.mJson = new ZSONObject();
    }

    @Override // ohos.hiviewdfx.Payload
    public Object get(String str) {
        try {
            return this.mJson.get(str);
        } catch (Exception e) {
            HiLog.error(LABEL, "get error: %{public}s", e.getMessage());
            return null;
        }
    }

    @Override // ohos.hiviewdfx.Payload
    public Iterator<String> keys() {
        return this.mJson.keySet().iterator();
    }

    @Override // ohos.hiviewdfx.Payload
    public void put(String str, boolean z) {
        try {
            this.mJson.put(str, (Object) Integer.valueOf(z ? 1 : 0));
        } catch (Exception e) {
            HiLog.error(LABEL, "put boolean error: %{public}s", e.getMessage());
        }
    }

    @Override // ohos.hiviewdfx.Payload
    public void put(String str, byte b) {
        try {
            this.mJson.put(str, (Object) Byte.valueOf(b));
        } catch (Exception e) {
            HiLog.error(LABEL, "put byte error: %{public}s", e.getMessage());
        }
    }

    @Override // ohos.hiviewdfx.Payload
    public void put(String str, short s) {
        try {
            this.mJson.put(str, (Object) Short.valueOf(s));
        } catch (Exception e) {
            HiLog.error(LABEL, "put short error: %{public}s", e.getMessage());
        }
    }

    @Override // ohos.hiviewdfx.Payload
    public void put(String str, int i) {
        try {
            this.mJson.put(str, (Object) Integer.valueOf(i));
        } catch (Exception e) {
            HiLog.error(LABEL, "put int error: %{public}s", e.getMessage());
        }
    }

    @Override // ohos.hiviewdfx.Payload
    public void put(String str, long j) {
        try {
            this.mJson.put(str, (Object) Long.valueOf(j));
        } catch (Exception e) {
            HiLog.error(LABEL, "put long error: %{public}s", e.getMessage());
        }
    }

    @Override // ohos.hiviewdfx.Payload
    public void put(String str, float f) {
        try {
            this.mJson.put(str, (Object) Float.valueOf(f));
        } catch (Exception e) {
            HiLog.error(LABEL, "put float error: %{public}s", e.getMessage());
        }
    }

    @Override // ohos.hiviewdfx.Payload
    public void put(String str, String str2) {
        try {
            this.mJson.put(str, (Object) str2);
        } catch (Exception e) {
            HiLog.error(LABEL, "put string error: %{public}s", e.getMessage());
        }
    }

    @Override // ohos.hiviewdfx.Payload
    public void put(String str, Payload payload) {
        try {
            if (payload instanceof JsonPayload) {
                this.mJson.put(str, (Object) ((JsonPayload) payload).toJson());
            } else {
                HiLog.error(LABEL, "put Payload type unmatched", new Object[0]);
            }
        } catch (Exception e) {
            HiLog.error(LABEL, "put Payload error: %{public}s", e.getMessage());
        }
    }

    @Override // ohos.hiviewdfx.Payload
    public void put(String str, Object obj) {
        try {
            this.mJson.put(str, obj);
        } catch (Exception e) {
            HiLog.error(LABEL, "put Object error: %{public}s", e.getMessage());
        }
    }

    private void appendInternal(String str, Object obj) {
        ZSONArray zSONArray;
        try {
            zSONArray = this.mJson.getZSONArray(str);
        } catch (Exception e) {
            HiLog.error(LABEL, "getJSONArray failed: %{public}s", e.getMessage());
            zSONArray = null;
        }
        if (zSONArray != null) {
            zSONArray.add(obj);
        } else {
            zSONArray = new ZSONArray();
            zSONArray.add(obj);
        }
        try {
            this.mJson.put(str, (Object) zSONArray);
        } catch (Exception e2) {
            HiLog.error(LABEL, "put array error: %{public}s", e2.getMessage());
        }
    }

    @Override // ohos.hiviewdfx.Payload
    public void append(String str, boolean z) {
        appendInternal(str, Integer.valueOf(z ? 1 : 0));
    }

    @Override // ohos.hiviewdfx.Payload
    public void append(String str, byte b) {
        appendInternal(str, Byte.valueOf(b));
    }

    @Override // ohos.hiviewdfx.Payload
    public void append(String str, short s) {
        appendInternal(str, Short.valueOf(s));
    }

    @Override // ohos.hiviewdfx.Payload
    public void append(String str, int i) {
        appendInternal(str, Integer.valueOf(i));
    }

    @Override // ohos.hiviewdfx.Payload
    public void append(String str, long j) {
        appendInternal(str, Long.valueOf(j));
    }

    @Override // ohos.hiviewdfx.Payload
    public void append(String str, float f) {
        appendInternal(str, Float.valueOf(f));
    }

    @Override // ohos.hiviewdfx.Payload
    public void append(String str, String str2) {
        appendInternal(str, str2);
    }

    @Override // ohos.hiviewdfx.Payload
    public void append(String str, Payload payload) {
        appendInternal(str, payload);
    }
}
