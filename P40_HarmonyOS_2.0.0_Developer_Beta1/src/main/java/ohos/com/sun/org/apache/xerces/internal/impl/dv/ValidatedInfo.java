package ohos.com.sun.org.apache.xerces.internal.impl.dv;

import ohos.com.sun.org.apache.xerces.internal.xs.ShortList;

public class ValidatedInfo {
    public Object actualValue;
    public short actualValueType;
    public ShortList itemValueTypes;
    public XSSimpleType memberType;
    public XSSimpleType[] memberTypes;
    public String normalizedValue;

    public void reset() {
        this.normalizedValue = null;
        this.actualValue = null;
        this.memberType = null;
        this.memberTypes = null;
    }

    public String stringValue() {
        Object obj = this.actualValue;
        if (obj == null) {
            return this.normalizedValue;
        }
        return obj.toString();
    }
}
