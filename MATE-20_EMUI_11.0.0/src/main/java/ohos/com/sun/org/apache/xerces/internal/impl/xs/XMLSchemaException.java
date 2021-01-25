package ohos.com.sun.org.apache.xerces.internal.impl.xs;

public class XMLSchemaException extends Exception {
    static final long serialVersionUID = -9096984648537046218L;
    Object[] args;
    String key;

    public XMLSchemaException(String str, Object[] objArr) {
        this.key = str;
        this.args = objArr;
    }

    public String getKey() {
        return this.key;
    }

    public Object[] getArgs() {
        return this.args;
    }
}
