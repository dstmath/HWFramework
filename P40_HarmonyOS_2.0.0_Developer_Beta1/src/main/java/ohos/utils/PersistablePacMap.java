package ohos.utils;

import java.math.BigDecimal;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.fastjson.JSONArray;
import ohos.utils.fastjson.JSONObject;
import ohos.utils.zson.ZSONObject;

public class PersistablePacMap extends BasePacMap {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218119424, "PersistablePacMap");

    public PersistablePacMap deepCopy() {
        PersistablePacMap persistablePacMap = new PersistablePacMap(false);
        persistablePacMap.copyFrom(this, true);
        return persistablePacMap;
    }

    PersistablePacMap(boolean z) {
        super(z);
    }

    public static boolean isValidValueType(Object obj) {
        return (obj instanceof Integer) || (obj instanceof Long) || (obj instanceof Short) || (obj instanceof Double) || (obj instanceof String) || (obj instanceof int[]) || (obj instanceof long[]) || (obj instanceof double[]) || (obj instanceof short[]) || (obj instanceof PersistablePacMap) || obj == null || (obj instanceof Boolean) || (obj instanceof boolean[]);
    }

    public PersistablePacMap() {
    }

    public PersistablePacMap(int i) {
        super(i);
    }

    public void putPersistablePacMap(String str, PersistablePacMap persistablePacMap) {
        super.putObjectValue(str, persistablePacMap.dataMap);
    }

    public PersistablePacMap getPersistablePacMap(String str) {
        Object obj = this.dataMap.get(str);
        if (obj != null && (obj instanceof JSONObject)) {
            Map<String, Object> innerMap = ((JSONObject) obj).getInnerMap();
            if (innerMap == null) {
                return null;
            }
            PersistablePacMap persistablePacMap = new PersistablePacMap();
            persistablePacMap.putAll(innerMap);
            return persistablePacMap;
        } else if (!(obj instanceof Map)) {
            return null;
        } else {
            PersistablePacMap persistablePacMap2 = new PersistablePacMap();
            persistablePacMap2.putAll((Map) obj);
            return persistablePacMap2;
        }
    }

    @Override // ohos.utils.BasePacMap
    public short getShortValue(String str) {
        Object obj = this.dataMap.get(str);
        if (obj == null || !(obj instanceof Integer)) {
            return super.getShortValue(str);
        }
        return ((Integer) obj).shortValue();
    }

    @Override // ohos.utils.BasePacMap
    public long getLongValue(String str) {
        Object obj = this.dataMap.get(str);
        if (obj == null || !(obj instanceof Integer)) {
            return super.getLongValue(str);
        }
        return ((Integer) obj).longValue();
    }

    @Override // ohos.utils.BasePacMap
    public double getDoubleValue(String str) {
        Object obj = this.dataMap.get(str);
        if (obj == null || !(obj instanceof BigDecimal)) {
            return super.getDoubleValue(str);
        }
        return ((BigDecimal) obj).doubleValue();
    }

    @Override // ohos.utils.BasePacMap
    public float getFloatValue(String str) {
        Object obj = this.dataMap.get(str);
        if (obj == null || !(obj instanceof BigDecimal)) {
            return super.getFloatValue(str);
        }
        return ((BigDecimal) obj).floatValue();
    }

    @Override // ohos.utils.BasePacMap
    public int[] getIntValueArray(String str) {
        Object obj = this.dataMap.get(str);
        if (obj == null || !(obj instanceof JSONArray)) {
            return super.getIntValueArray(str);
        }
        JSONArray jSONArray = (JSONArray) obj;
        int[] iArr = new int[jSONArray.size()];
        for (int i = 0; i < jSONArray.size(); i++) {
            iArr[i] = jSONArray.getIntValue(i);
        }
        return iArr;
    }

    @Override // ohos.utils.BasePacMap
    public short[] getShortValueArray(String str) {
        Object obj = this.dataMap.get(str);
        if (obj == null || !(obj instanceof JSONArray)) {
            return super.getShortValueArray(str);
        }
        JSONArray jSONArray = (JSONArray) obj;
        short[] sArr = new short[jSONArray.size()];
        for (int i = 0; i < jSONArray.size(); i++) {
            sArr[i] = jSONArray.getInteger(i).shortValue();
        }
        return sArr;
    }

    @Override // ohos.utils.BasePacMap
    public long[] getLongValueArray(String str) {
        Object obj = this.dataMap.get(str);
        if (obj == null || !(obj instanceof JSONArray)) {
            return super.getLongValueArray(str);
        }
        JSONArray jSONArray = (JSONArray) obj;
        long[] jArr = new long[jSONArray.size()];
        for (int i = 0; i < jSONArray.size(); i++) {
            jArr[i] = jSONArray.getInteger(i).longValue();
        }
        return jArr;
    }

    @Override // ohos.utils.BasePacMap
    public boolean[] getBooleanValueArray(String str) {
        Object obj = this.dataMap.get(str);
        if (obj == null || !(obj instanceof JSONArray)) {
            return super.getBooleanValueArray(str);
        }
        JSONArray jSONArray = (JSONArray) obj;
        boolean[] zArr = new boolean[jSONArray.size()];
        for (int i = 0; i < jSONArray.size(); i++) {
            if (jSONArray.getBoolean(i) != null) {
                zArr[i] = jSONArray.getBoolean(i).booleanValue();
            } else {
                HiLog.error(LABEL, "the jsonArray is not a boolean array. return null!", new Object[0]);
                return null;
            }
        }
        return zArr;
    }

    @Override // ohos.utils.BasePacMap
    public double[] getDoubleValueArray(String str) {
        Object obj = this.dataMap.get(str);
        if (obj == null || !(obj instanceof JSONArray)) {
            return super.getDoubleValueArray(str);
        }
        JSONArray jSONArray = (JSONArray) obj;
        double[] dArr = new double[jSONArray.size()];
        for (int i = 0; i < jSONArray.size(); i++) {
            dArr[i] = jSONArray.getBigDecimal(i).doubleValue();
        }
        return dArr;
    }

    @Override // ohos.utils.BasePacMap
    public float[] getFloatValueArray(String str) {
        Object obj = this.dataMap.get(str);
        if (obj == null || !(obj instanceof JSONArray)) {
            return super.getFloatValueArray(str);
        }
        JSONArray jSONArray = (JSONArray) obj;
        float[] fArr = new float[jSONArray.size()];
        for (int i = 0; i < jSONArray.size(); i++) {
            fArr[i] = jSONArray.getBigDecimal(i).floatValue();
        }
        return fArr;
    }

    public static PersistablePacMap fromJsonString(String str) {
        Map<String, Object> map = (Map) ZSONObject.parseObject(str, Map.class);
        if (map != null) {
            PersistablePacMap persistablePacMap = new PersistablePacMap();
            persistablePacMap.putAll(map);
            return persistablePacMap;
        }
        HiLog.error(LABEL, "wrong json format!", new Object[0]);
        return null;
    }

    public String toJsonString() {
        return ZSONObject.toZSONString(this.dataMap);
    }
}
