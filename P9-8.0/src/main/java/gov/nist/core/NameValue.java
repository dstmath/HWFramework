package gov.nist.core;

import java.util.Map.Entry;

public class NameValue extends GenericObject implements Entry<String, String> {
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

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    public StringBuffer encode(StringBuffer buffer) {
        if (this.name == null || this.value == null || (this.isFlagParameter ^ 1) == 0) {
            if (this.name != null || this.value == null) {
                if (this.name == null || (this.value != null && !this.isFlagParameter)) {
                    return buffer;
                }
                buffer.append(this.name);
                return buffer;
            } else if (GenericObject.isMySubclass(this.value.getClass())) {
                ((GenericObject) this.value).encode(buffer);
                return buffer;
            } else if (GenericObjectList.isMySubclass(this.value.getClass())) {
                buffer.append(((GenericObjectList) this.value).encode());
                return buffer;
            } else {
                buffer.append(this.quotes).append(this.value.toString()).append(this.quotes);
                return buffer;
            }
        } else if (GenericObject.isMySubclass(this.value.getClass())) {
            GenericObject gv = this.value;
            buffer.append(this.name).append(this.separator).append(this.quotes);
            gv.encode(buffer);
            buffer.append(this.quotes);
            return buffer;
        } else if (GenericObjectList.isMySubclass(this.value.getClass())) {
            buffer.append(this.name).append(this.separator).append(this.value.encode());
            return buffer;
        } else if (this.value.toString().length() != 0) {
            buffer.append(this.name).append(this.separator).append(this.quotes).append(this.value.toString()).append(this.quotes);
            return buffer;
        } else if (this.isQuotedString) {
            buffer.append(this.name).append(this.separator).append(this.quotes).append(this.quotes);
            return buffer;
        } else {
            buffer.append(this.name).append(this.separator);
            return buffer;
        }
    }

    public Object clone() {
        NameValue retval = (NameValue) super.clone();
        if (this.value != null) {
            retval.value = GenericObject.makeClone(this.value);
        }
        return retval;
    }

    public boolean equals(Object other) {
        boolean z = true;
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
        if (this.name != null && that.name != null && this.name.compareToIgnoreCase(that.name) != 0) {
            return false;
        }
        if ((this.value != null && that.value == null) || (this.value == null && that.value != null)) {
            return false;
        }
        if (this.value == that.value) {
            return true;
        }
        if (!(this.value instanceof String)) {
            return this.value.equals(that.value);
        }
        if (this.isQuotedString) {
            return this.value.equals(that.value);
        }
        if (this.value.compareToIgnoreCase(that.value) != 0) {
            z = false;
        }
        return z;
    }

    public String getKey() {
        return this.name;
    }

    public String getValue() {
        return this.value == null ? null : this.value.toString();
    }

    public String setValue(String value) {
        String retval = this.value == null ? null : value;
        this.value = value;
        return retval;
    }

    public int hashCode() {
        return encode().toLowerCase().hashCode();
    }
}
