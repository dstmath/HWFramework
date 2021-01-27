package org.apache.http.message;

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.LangUtils;

@Deprecated
public class BasicHeaderElement implements HeaderElement, Cloneable {
    private final String name;
    private final NameValuePair[] parameters;
    private final String value;

    public BasicHeaderElement(String name2, String value2, NameValuePair[] parameters2) {
        if (name2 != null) {
            this.name = name2;
            this.value = value2;
            if (parameters2 != null) {
                this.parameters = parameters2;
            } else {
                this.parameters = new NameValuePair[0];
            }
        } else {
            throw new IllegalArgumentException("Name may not be null");
        }
    }

    public BasicHeaderElement(String name2, String value2) {
        this(name2, value2, null);
    }

    @Override // org.apache.http.HeaderElement
    public String getName() {
        return this.name;
    }

    @Override // org.apache.http.HeaderElement
    public String getValue() {
        return this.value;
    }

    @Override // org.apache.http.HeaderElement
    public NameValuePair[] getParameters() {
        return (NameValuePair[]) this.parameters.clone();
    }

    @Override // org.apache.http.HeaderElement
    public int getParameterCount() {
        return this.parameters.length;
    }

    @Override // org.apache.http.HeaderElement
    public NameValuePair getParameter(int index) {
        return this.parameters[index];
    }

    @Override // org.apache.http.HeaderElement
    public NameValuePair getParameterByName(String name2) {
        if (name2 != null) {
            int i = 0;
            while (true) {
                NameValuePair[] nameValuePairArr = this.parameters;
                if (i >= nameValuePairArr.length) {
                    return null;
                }
                NameValuePair current = nameValuePairArr[i];
                if (current.getName().equalsIgnoreCase(name2)) {
                    return current;
                }
                i++;
            }
        } else {
            throw new IllegalArgumentException("Name may not be null");
        }
    }

    @Override // java.lang.Object
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (!(object instanceof HeaderElement)) {
            return false;
        }
        BasicHeaderElement that = (BasicHeaderElement) object;
        if (!this.name.equals(that.name) || !LangUtils.equals(this.value, that.value) || !LangUtils.equals((Object[]) this.parameters, (Object[]) that.parameters)) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        int hash = LangUtils.hashCode(LangUtils.hashCode(17, this.name), this.value);
        int i = 0;
        while (true) {
            NameValuePair[] nameValuePairArr = this.parameters;
            if (i >= nameValuePairArr.length) {
                return hash;
            }
            hash = LangUtils.hashCode(hash, nameValuePairArr[i]);
            i++;
        }
    }

    @Override // java.lang.Object
    public String toString() {
        CharArrayBuffer buffer = new CharArrayBuffer(64);
        buffer.append(this.name);
        if (this.value != null) {
            buffer.append("=");
            buffer.append(this.value);
        }
        for (int i = 0; i < this.parameters.length; i++) {
            buffer.append("; ");
            buffer.append(this.parameters[i]);
        }
        return buffer.toString();
    }

    @Override // java.lang.Object
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
