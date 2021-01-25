package ohos.com.sun.org.apache.xalan.internal.xsltc.runtime;

public class Parameter {
    public boolean _isDefault;
    public String _name;
    public Object _value;

    public Parameter(String str, Object obj) {
        this._name = str;
        this._value = obj;
        this._isDefault = true;
    }

    public Parameter(String str, Object obj, boolean z) {
        this._name = str;
        this._value = obj;
        this._isDefault = z;
    }
}
