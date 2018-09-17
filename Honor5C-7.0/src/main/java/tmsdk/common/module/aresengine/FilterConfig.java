package tmsdk.common.module.aresengine;

import java.util.HashMap;
import java.util.Map.Entry;

/* compiled from: Unknown */
public final class FilterConfig {
    public static final int STATE_ACCEPTABLE = 0;
    public static final int STATE_DISABLE = 3;
    public static final int STATE_EMTPY = 4;
    public static final int STATE_ENABLE = 2;
    public static final int STATE_REJECTABLE = 1;
    private HashMap<Integer, Integer> Cp;

    public FilterConfig() {
        this(null);
    }

    public FilterConfig(String str) {
        this.Cp = new HashMap();
        if (str != null) {
            String[] split = str.trim().split(",");
            for (int i = STATE_ACCEPTABLE; i < split.length; i += STATE_ENABLE) {
                set(Integer.parseInt(split[i]), Integer.parseInt(split[i + STATE_REJECTABLE]));
            }
        }
    }

    public String dump() {
        StringBuffer stringBuffer = new StringBuffer();
        for (Entry entry : this.Cp.entrySet()) {
            stringBuffer.append((entry.getKey() + "," + entry.getValue()) + ",");
        }
        if (stringBuffer.length() > 0) {
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        }
        return stringBuffer.toString();
    }

    public int get(int i) {
        Integer num = (Integer) this.Cp.get(Integer.valueOf(i));
        return num == null ? STATE_EMTPY : num.intValue();
    }

    public void reset() {
        this.Cp.clear();
    }

    public void set(int i, int i2) {
        if (i2 == 0 || i2 == STATE_REJECTABLE || i2 == STATE_ENABLE || i2 == STATE_DISABLE || i2 == STATE_EMTPY) {
            this.Cp.put(Integer.valueOf(i), Integer.valueOf(i2));
            return;
        }
        throw new IllegalStateException("the state " + i2 + " is not define.");
    }
}
