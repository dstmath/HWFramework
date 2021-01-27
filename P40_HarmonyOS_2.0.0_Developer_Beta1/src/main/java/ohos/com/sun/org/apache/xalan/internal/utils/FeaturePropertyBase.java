package ohos.com.sun.org.apache.xalan.internal.utils;

public abstract class FeaturePropertyBase {
    State[] states = {State.DEFAULT, State.DEFAULT};
    String[] values = null;

    public enum State {
        DEFAULT,
        FSP,
        JAXPDOTPROPERTIES,
        SYSTEMPROPERTY,
        APIPROPERTY
    }

    public abstract int getIndex(String str);

    public void setValue(Enum r3, State state, String str) {
        if (state.compareTo(this.states[r3.ordinal()]) >= 0) {
            this.values[r3.ordinal()] = str;
            this.states[r3.ordinal()] = state;
        }
    }

    public void setValue(int i, State state, String str) {
        if (state.compareTo(this.states[i]) >= 0) {
            this.values[i] = str;
            this.states[i] = state;
        }
    }

    public boolean setValue(String str, State state, Object obj) {
        int index = getIndex(str);
        if (index <= -1) {
            return false;
        }
        setValue(index, state, (String) obj);
        return true;
    }

    public boolean setValue(String str, State state, boolean z) {
        int index = getIndex(str);
        if (index <= -1) {
            return false;
        }
        if (z) {
            setValue(index, state, "true");
            return true;
        }
        setValue(index, state, "false");
        return true;
    }

    public String getValue(Enum r1) {
        return this.values[r1.ordinal()];
    }

    public String getValue(String str) {
        int index = getIndex(str);
        if (index > -1) {
            return getValueByIndex(index);
        }
        return null;
    }

    public String getValueAsString(String str) {
        int index = getIndex(str);
        if (index > -1) {
            return getValueByIndex(index);
        }
        return null;
    }

    public String getValueByIndex(int i) {
        return this.values[i];
    }

    public <E extends Enum<E>> int getIndex(Class<E> cls, String str) {
        E[] enumConstants = cls.getEnumConstants();
        for (E e : enumConstants) {
            if (e.toString().equals(str)) {
                return e.ordinal();
            }
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public void getSystemProperty(Enum r3, String str) {
        try {
            String systemProperty = SecuritySupport.getSystemProperty(str);
            if (systemProperty != null) {
                this.values[r3.ordinal()] = systemProperty;
                this.states[r3.ordinal()] = State.SYSTEMPROPERTY;
                return;
            }
            String readJAXPProperty = SecuritySupport.readJAXPProperty(str);
            if (readJAXPProperty != null) {
                this.values[r3.ordinal()] = readJAXPProperty;
                this.states[r3.ordinal()] = State.JAXPDOTPROPERTIES;
            }
        } catch (NumberFormatException unused) {
        }
    }
}
