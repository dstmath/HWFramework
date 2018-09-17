package tmsdk.common.module.aresengine;

import java.util.HashMap;
import java.util.Map.Entry;

public final class FilterConfig {
    public static final int STATE_ACCEPTABLE = 0;
    public static final int STATE_DISABLE = 3;
    public static final int STATE_EMTPY = 4;
    public static final int STATE_ENABLE = 2;
    public static final int STATE_REJECTABLE = 1;
    private HashMap<Integer, Integer> Ab;

    public FilterConfig() {
        this(null);
    }

    public FilterConfig(String str) {
        this.Ab = new HashMap();
        if (str != null) {
            String[] split = str.trim().split(",");
            for (int i = 0; i < split.length; i += 2) {
                set(Integer.parseInt(split[i]), Integer.parseInt(split[i + 1]));
            }
        }
    }

    public String dump() {
        StringBuffer stringBuffer = new StringBuffer();
        for (Entry entry : this.Ab.entrySet()) {
            stringBuffer.append((entry.getKey() + "," + entry.getValue()) + ",");
        }
        if (stringBuffer.length() > 0) {
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        }
        return stringBuffer.toString();
    }

    public int get(int i) {
        Integer num = (Integer) this.Ab.get(Integer.valueOf(i));
        return num == null ? 4 : num.intValue();
    }

    public void reset() {
        this.Ab.clear();
    }

    public void set(int i, int i2) {
        if (i2 == 0 || i2 == 1 || i2 == 2 || i2 == 3 || i2 == 4) {
            this.Ab.put(Integer.valueOf(i), Integer.valueOf(i2));
            return;
        }
        throw new IllegalStateException("the state " + i2 + " is not define.");
    }
}
