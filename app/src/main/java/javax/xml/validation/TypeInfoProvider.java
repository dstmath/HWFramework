package javax.xml.validation;

import org.w3c.dom.TypeInfo;

public abstract class TypeInfoProvider {
    public abstract TypeInfo getAttributeTypeInfo(int i);

    public abstract TypeInfo getElementTypeInfo();

    public abstract boolean isIdAttribute(int i);

    public abstract boolean isSpecified(int i);

    protected TypeInfoProvider() {
    }
}
