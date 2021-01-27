package gov.nist.core;

import java.util.Map;

public class NameValue extends GenericObject implements Map.Entry<String, String> {
    private static final long serialVersionUID = -1857729012596437950L;
    protected final boolean isFlagParameter;
    protected boolean isQuotedString;
    private String name;
    private String quotes;
    private String separator;
    private Object value;

    public NameValue() {
        this.name = null;
        this.value = "";
        this.separator = Separators.EQUALS;
        this.quotes = "";
        this.isFlagParameter = false;
    }

    public NameValue(String n, Object v, boolean isFlag) {
        this.name = n;
        this.value = v;
        this.separator = Separators.EQUALS;
        this.quotes = "";
        this.isFlagParameter = isFlag;
    }

    public NameValue(String n, Object v) {
        this(n, v, false);
    }

    public void setSeparator(String sep) {
        this.separator = sep;
    }

    public void setQuotedValue() {
        this.isQuotedString = true;
        this.quotes = Separators.DOUBLE_QUOTE;
    }

    public boolean isValueQuoted() {
        return this.isQuotedString;
    }

    public String getName() {
        return this.name;
    }

    public Object getValueAsObject() {
        return this.isFlagParameter ? "" : this.value;
    }

    public void setName(String n) {
        this.name = n;
    }

    public void setValueAsObject(Object v) {
        this.value = v;
    }

    @Override // gov.nist.core.GenericObject
    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    @Override // gov.nist.core.GenericObject
    public StringBuffer encode(StringBuffer buffer) {
        Object obj;
        Object obj2;
        if (this.name == null || (obj2 = this.value) == null || this.isFlagParameter) {
            if (this.name != null || (obj = this.value) == null) {
                if (this.name == null || (this.value != null && !this.isFlagParameter)) {
                    return buffer;
                }
                buffer.append(this.name);
                return buffer;
            } else if (GenericObject.isMySubclass(obj.getClass())) {
                ((GenericObject) this.value).encode(buffer);
                return buffer;
            } else if (GenericObjectList.isMySubclass(this.value.getClass())) {
                buffer.append(((GenericObjectList) this.value).encode());
                return buffer;
            } else {
                buffer.append(this.quotes);
                buffer.append(this.value.toString());
                buffer.append(this.quotes);
                return buffer;
            }
        } else if (GenericObject.isMySubclass(obj2.getClass())) {
            buffer.append(this.name);
            buffer.append(this.separator);
            buffer.append(this.quotes);
            ((GenericObject) this.value).encode(buffer);
            buffer.append(this.quotes);
            return buffer;
        } else if (GenericObjectList.isMySubclass(this.value.getClass())) {
            buffer.append(this.name);
            buffer.append(this.separator);
            buffer.append(((GenericObjectList) this.value).encode());
            return buffer;
        } else if (this.value.toString().length() != 0) {
            buffer.append(this.name);
            buffer.append(this.separator);
            buffer.append(this.quotes);
            buffer.append(this.value.toString());
            buffer.append(this.quotes);
            return buffer;
        } else if (this.isQuotedString) {
            buffer.append(this.name);
            buffer.append(this.separator);
            buffer.append(this.quotes);
            buffer.append(this.quotes);
            return buffer;
        } else {
            buffer.append(this.name);
            buffer.append(this.separator);
            return buffer;
        }
    }

    @Override // gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        NameValue retval = (NameValue) super.clone();
        Object obj = this.value;
        if (obj != null) {
            retval.value = makeClone(obj);
        }
        return retval;
    }

    @Override // gov.nist.core.GenericObject, java.lang.Object
    public boolean equals(Object other) {
        String str;
        if (other == null || !other.getClass().equals(getClass())) {
            return false;
        }
        NameValue that = (NameValue) other;
        if (this == that) {
            return true;
        }
        if ((this.name == null && that.name != null) || (this.name != null && that.name == null)) {
            return false;
        }
        String str2 = this.name;
        if (str2 != null && (str = that.name) != null && str2.compareToIgnoreCase(str) != 0) {
            return false;
        }
        if ((this.value != null && that.value == null) || (this.value == null && that.value != null)) {
            return false;
        }
        Object obj = this.value;
        Object obj2 = that.value;
        if (obj == obj2) {
            return true;
        }
        if (!(obj instanceof String)) {
            return obj.equals(obj2);
        }
        if (this.isQuotedString) {
            return obj.equals(obj2);
        }
        if (((String) obj).compareToIgnoreCase((String) obj2) == 0) {
            return true;
        }
        return false;
    }

    @Override // java.util.Map.Entry
    public String getKey() {
        return this.name;
    }

    @Override // java.util.Map.Entry
    public String getValue() {
        Object obj = this.value;
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public String setValue(String value2) {
        String retval = this.value == null ? null : value2;
        this.value = value2;
        return retval;
    }

    @Override // java.lang.Object, java.util.Map.Entry
    public int hashCode() {
        return encode().toLowerCase().hashCode();
    }
}
