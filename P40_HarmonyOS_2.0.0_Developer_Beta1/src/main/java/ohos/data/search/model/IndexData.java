package ohos.data.search.model;

import java.util.HashMap;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class IndexData {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109504, "IndexData");
    private Map<String, Object> values = new HashMap();

    public Map<String, Object> getValues() {
        return this.values;
    }

    public void setValues(Map<String, Object> map) {
        this.values = map;
    }

    public int size() {
        return this.values.size();
    }

    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    public void remove(String str) {
        this.values.remove(str);
    }

    public void clear() {
        this.values.clear();
    }

    public void put(String str, String str2) {
        if (str == null || str2 == null) {
            HiLog.error(LABEL, "null String value cannot put in IndexData", new Object[0]);
        } else {
            this.values.put(str, str2);
        }
    }

    public void put(String str, Integer num) {
        if (str == null || num == null) {
            HiLog.error(LABEL, "null Integer value cannot put in IndexData", new Object[0]);
        } else {
            this.values.put(str, num);
        }
    }

    public void put(String str, Float f) {
        if (str == null || f == null) {
            HiLog.error(LABEL, "null Float value cannot put in IndexData", new Object[0]);
        } else {
            this.values.put(str, f);
        }
    }

    public void put(String str, Double d) {
        if (str == null || d == null) {
            HiLog.error(LABEL, "null Double value cannot put in IndexData", new Object[0]);
        } else {
            this.values.put(str, d);
        }
    }

    public void put(String str, Long l) {
        if (str == null || l == null) {
            HiLog.error(LABEL, "null Long value cannot put in IndexData", new Object[0]);
        } else {
            this.values.put(str, l);
        }
    }

    public Object get(String str) {
        return this.values.get(str);
    }

    public String getAsString(String str) {
        Object obj = this.values.get(str);
        if (obj != null) {
            return obj.toString();
        }
        return null;
    }

    public Integer getAsInteger(String str) {
        Object obj = this.values.get(str);
        if (obj instanceof Number) {
            return Integer.valueOf(((Number) obj).intValue());
        }
        if (obj instanceof CharSequence) {
            try {
                return Integer.valueOf(obj.toString());
            } catch (NumberFormatException unused) {
                HiLog.error(LABEL, "Cannot parse value to a Integer", new Object[0]);
                return null;
            }
        } else {
            HiLog.error(LABEL, "Cannot cast value to a Integer", new Object[0]);
            return null;
        }
    }

    public Float getAsFloat(String str) {
        Object obj = this.values.get(str);
        if (obj instanceof Number) {
            return Float.valueOf(((Number) obj).floatValue());
        }
        if (obj instanceof CharSequence) {
            try {
                return Float.valueOf(obj.toString());
            } catch (NumberFormatException unused) {
                HiLog.error(LABEL, "Cannot parse value to a Float", new Object[0]);
                return null;
            }
        } else {
            HiLog.error(LABEL, "Cannot cast value to a Float", new Object[0]);
            return null;
        }
    }

    public Double getAsDouble(String str) {
        Object obj = this.values.get(str);
        if (obj instanceof Number) {
            return Double.valueOf(((Number) obj).doubleValue());
        }
        if (obj instanceof CharSequence) {
            try {
                return Double.valueOf(obj.toString());
            } catch (NumberFormatException unused) {
                HiLog.error(LABEL, "Cannot parse value to a Double", new Object[0]);
                return null;
            }
        } else {
            HiLog.error(LABEL, "Cannot cast value to a Double", new Object[0]);
            return null;
        }
    }

    public Long getAsLong(String str) {
        Object obj = this.values.get(str);
        if (obj instanceof Number) {
            return Long.valueOf(((Number) obj).longValue());
        }
        if (obj instanceof CharSequence) {
            try {
                return Long.valueOf(obj.toString());
            } catch (NumberFormatException unused) {
                HiLog.error(LABEL, "Cannot parse value to a Long", new Object[0]);
                return null;
            }
        } else {
            HiLog.error(LABEL, "Cannot cast value to a Long", new Object[0]);
            return null;
        }
    }
}
