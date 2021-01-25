package org.ksoap2.serialization;

import java.util.Hashtable;
import java.util.Vector;

public class SoapObject extends AttributeContainer implements KvmSerializable, HasInnerText {
    private static final String EMPTY_STRING = "";
    protected Object innerText;
    protected String name;
    protected String namespace;
    protected Vector properties;

    public SoapObject() {
        this(EMPTY_STRING, EMPTY_STRING);
    }

    public SoapObject(String namespace2, String name2) {
        this.properties = new Vector();
        this.namespace = namespace2;
        this.name = name2;
    }

    public boolean equals(Object obj) {
        int numProperties;
        if (!(obj instanceof SoapObject)) {
            return false;
        }
        SoapObject otherSoapObject = (SoapObject) obj;
        if (!this.name.equals(otherSoapObject.name) || !this.namespace.equals(otherSoapObject.namespace) || (numProperties = this.properties.size()) != otherSoapObject.properties.size()) {
            return false;
        }
        for (int propIndex = 0; propIndex < numProperties; propIndex++) {
            if (!otherSoapObject.isPropertyEqual(this.properties.elementAt(propIndex), propIndex)) {
                return false;
            }
        }
        return attributesAreEqual(otherSoapObject);
    }

    public boolean isPropertyEqual(Object otherProp, int index) {
        if (index >= getPropertyCount()) {
            return false;
        }
        Object thisProp = this.properties.elementAt(index);
        if ((otherProp instanceof PropertyInfo) && (thisProp instanceof PropertyInfo)) {
            PropertyInfo otherPropInfo = (PropertyInfo) otherProp;
            PropertyInfo thisPropInfo = (PropertyInfo) thisProp;
            if (!otherPropInfo.getName().equals(thisPropInfo.getName()) || !otherPropInfo.getValue().equals(thisPropInfo.getValue())) {
                return false;
            }
            return true;
        } else if (!(otherProp instanceof SoapObject) || !(thisProp instanceof SoapObject)) {
            return false;
        } else {
            return ((SoapObject) otherProp).equals((SoapObject) thisProp);
        }
    }

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return this.namespace;
    }

    @Override // org.ksoap2.serialization.KvmSerializable
    public Object getProperty(int index) {
        Object prop = this.properties.elementAt(index);
        if (prop instanceof PropertyInfo) {
            return ((PropertyInfo) prop).getValue();
        }
        return (SoapObject) prop;
    }

    public String getPropertyAsString(int index) {
        return ((PropertyInfo) this.properties.elementAt(index)).getValue().toString();
    }

    public Object getProperty(String name2) {
        Integer index = propertyIndex(name2);
        if (index != null) {
            return getProperty(index.intValue());
        }
        throw new RuntimeException("illegal property: " + name2);
    }

    public Object getProperty(String namespace2, String name2) {
        Integer index = propertyIndex(namespace2, name2);
        if (index != null) {
            return getProperty(index.intValue());
        }
        throw new RuntimeException("illegal property: " + name2);
    }

    public Object getPropertyByNamespaceSafely(String namespace2, String name2) {
        Integer i = propertyIndex(namespace2, name2);
        if (i != null) {
            return getProperty(i.intValue());
        }
        return new NullSoapObject();
    }

    public String getPropertyByNamespaceSafelyAsString(String namespace2, String name2) {
        Object foo;
        Integer i = propertyIndex(namespace2, name2);
        if (i == null || (foo = getProperty(i.intValue())) == null) {
            return EMPTY_STRING;
        }
        return foo.toString();
    }

    public Object getPropertySafely(String namespace2, String name2, Object defaultThing) {
        Integer i = propertyIndex(namespace2, name2);
        if (i != null) {
            return getProperty(i.intValue());
        }
        return defaultThing;
    }

    public String getPropertySafelyAsString(String namespace2, String name2, Object defaultThing) {
        Integer i = propertyIndex(namespace2, name2);
        if (i != null) {
            Object property = getProperty(i.intValue());
            if (property != null) {
                return property.toString();
            }
            return EMPTY_STRING;
        } else if (defaultThing != null) {
            return defaultThing.toString();
        } else {
            return EMPTY_STRING;
        }
    }

    public Object getPrimitiveProperty(String namespace2, String name2) {
        Integer index = propertyIndex(namespace2, name2);
        if (index != null) {
            PropertyInfo propertyInfo = (PropertyInfo) this.properties.elementAt(index.intValue());
            if (propertyInfo.getType() != SoapObject.class && propertyInfo.getValue() != null) {
                return propertyInfo.getValue();
            }
            PropertyInfo propertyInfo2 = new PropertyInfo();
            propertyInfo2.setType(String.class);
            propertyInfo2.setValue(EMPTY_STRING);
            propertyInfo2.setName(name2);
            propertyInfo2.setNamespace(namespace2);
            return propertyInfo2.getValue();
        }
        throw new RuntimeException("illegal property: " + name2);
    }

    public String getPrimitivePropertyAsString(String namespace2, String name2) {
        Integer index = propertyIndex(namespace2, name2);
        if (index != null) {
            PropertyInfo propertyInfo = (PropertyInfo) this.properties.elementAt(index.intValue());
            if (propertyInfo.getType() == SoapObject.class || propertyInfo.getValue() == null) {
                return EMPTY_STRING;
            }
            return propertyInfo.getValue().toString();
        }
        throw new RuntimeException("illegal property: " + name2);
    }

    public Object getPrimitivePropertySafely(String namespace2, String name2) {
        Integer index = propertyIndex(namespace2, name2);
        if (index == null) {
            return new NullSoapObject();
        }
        PropertyInfo propertyInfo = (PropertyInfo) this.properties.elementAt(index.intValue());
        if (propertyInfo.getType() != SoapObject.class && propertyInfo.getValue() != null) {
            return propertyInfo.getValue().toString();
        }
        PropertyInfo propertyInfo2 = new PropertyInfo();
        propertyInfo2.setType(String.class);
        propertyInfo2.setValue(EMPTY_STRING);
        propertyInfo2.setName(name2);
        propertyInfo2.setNamespace(namespace2);
        return propertyInfo2.getValue();
    }

    public String getPrimitivePropertySafelyAsString(String namespace2, String name2) {
        Integer index = propertyIndex(namespace2, name2);
        if (index == null) {
            return EMPTY_STRING;
        }
        PropertyInfo propertyInfo = (PropertyInfo) this.properties.elementAt(index.intValue());
        if (propertyInfo.getType() == SoapObject.class || propertyInfo.getValue() == null) {
            return EMPTY_STRING;
        }
        return propertyInfo.getValue().toString();
    }

    public boolean hasProperty(String namespace2, String name2) {
        if (propertyIndex(namespace2, name2) != null) {
            return true;
        }
        return false;
    }

    public String getPropertyAsString(String namespace2, String name2) {
        Integer index = propertyIndex(namespace2, name2);
        if (index != null) {
            return getProperty(index.intValue()).toString();
        }
        throw new RuntimeException("illegal property: " + name2);
    }

    public String getPropertyAsString(String name2) {
        Integer index = propertyIndex(name2);
        if (index != null) {
            return getProperty(index.intValue()).toString();
        }
        throw new RuntimeException("illegal property: " + name2);
    }

    public boolean hasProperty(String name2) {
        if (propertyIndex(name2) != null) {
            return true;
        }
        return false;
    }

    public Object getPropertySafely(String name2) {
        Integer i = propertyIndex(name2);
        if (i != null) {
            return getProperty(i.intValue());
        }
        return new NullSoapObject();
    }

    public String getPropertySafelyAsString(String name2) {
        Object foo;
        Integer i = propertyIndex(name2);
        if (i == null || (foo = getProperty(i.intValue())) == null) {
            return EMPTY_STRING;
        }
        return foo.toString();
    }

    public Object getPropertySafely(String name2, Object defaultThing) {
        Integer i = propertyIndex(name2);
        if (i != null) {
            return getProperty(i.intValue());
        }
        return defaultThing;
    }

    public String getPropertySafelyAsString(String name2, Object defaultThing) {
        Integer i = propertyIndex(name2);
        if (i != null) {
            Object property = getProperty(i.intValue());
            if (property != null) {
                return property.toString();
            }
            return EMPTY_STRING;
        } else if (defaultThing != null) {
            return defaultThing.toString();
        } else {
            return EMPTY_STRING;
        }
    }

    public Object getPrimitiveProperty(String name2) {
        Integer index = propertyIndex(name2);
        if (index != null) {
            PropertyInfo propertyInfo = (PropertyInfo) this.properties.elementAt(index.intValue());
            if (propertyInfo.getType() != SoapObject.class && propertyInfo.getValue() != null) {
                return propertyInfo.getValue();
            }
            PropertyInfo propertyInfo2 = new PropertyInfo();
            propertyInfo2.setType(String.class);
            propertyInfo2.setValue(EMPTY_STRING);
            propertyInfo2.setName(name2);
            return propertyInfo2.getValue();
        }
        throw new RuntimeException("illegal property: " + name2);
    }

    public String getPrimitivePropertyAsString(String name2) {
        Integer index = propertyIndex(name2);
        if (index != null) {
            PropertyInfo propertyInfo = (PropertyInfo) this.properties.elementAt(index.intValue());
            if (propertyInfo.getType() == SoapObject.class || propertyInfo.getValue() == null) {
                return EMPTY_STRING;
            }
            return propertyInfo.getValue().toString();
        }
        throw new RuntimeException("illegal property: " + name2);
    }

    public Object getPrimitivePropertySafely(String name2) {
        Integer index = propertyIndex(name2);
        if (index == null) {
            return new NullSoapObject();
        }
        PropertyInfo propertyInfo = (PropertyInfo) this.properties.elementAt(index.intValue());
        if (propertyInfo.getType() != SoapObject.class && propertyInfo.getValue() != null) {
            return propertyInfo.getValue().toString();
        }
        PropertyInfo propertyInfo2 = new PropertyInfo();
        propertyInfo2.setType(String.class);
        propertyInfo2.setValue(EMPTY_STRING);
        propertyInfo2.setName(name2);
        return propertyInfo2.getValue();
    }

    public String getPrimitivePropertySafelyAsString(String name2) {
        Integer index = propertyIndex(name2);
        if (index == null) {
            return EMPTY_STRING;
        }
        PropertyInfo propertyInfo = (PropertyInfo) this.properties.elementAt(index.intValue());
        if (propertyInfo.getType() == SoapObject.class || propertyInfo.getValue() == null) {
            return EMPTY_STRING;
        }
        return propertyInfo.getValue().toString();
    }

    private Integer propertyIndex(String name2) {
        if (name2 == null) {
            return null;
        }
        for (int i = 0; i < this.properties.size(); i++) {
            if (name2.equals(((PropertyInfo) this.properties.elementAt(i)).getName())) {
                return new Integer(i);
            }
        }
        return null;
    }

    private Integer propertyIndex(String namespace2, String name2) {
        if (name2 == null || namespace2 == null) {
            return null;
        }
        for (int i = 0; i < this.properties.size(); i++) {
            PropertyInfo info = (PropertyInfo) this.properties.elementAt(i);
            if (name2.equals(info.getName()) && namespace2.equals(info.getNamespace())) {
                return new Integer(i);
            }
        }
        return null;
    }

    @Override // org.ksoap2.serialization.KvmSerializable
    public int getPropertyCount() {
        return this.properties.size();
    }

    @Override // org.ksoap2.serialization.KvmSerializable
    public void getPropertyInfo(int index, Hashtable properties2, PropertyInfo propertyInfo) {
        getPropertyInfo(index, propertyInfo);
    }

    public void getPropertyInfo(int index, PropertyInfo propertyInfo) {
        Object element = this.properties.elementAt(index);
        if (element instanceof PropertyInfo) {
            PropertyInfo p = (PropertyInfo) element;
            propertyInfo.name = p.name;
            propertyInfo.namespace = p.namespace;
            propertyInfo.flags = p.flags;
            propertyInfo.type = p.type;
            propertyInfo.elementType = p.elementType;
            propertyInfo.value = p.value;
            propertyInfo.multiRef = p.multiRef;
            return;
        }
        propertyInfo.name = null;
        propertyInfo.namespace = null;
        propertyInfo.flags = 0;
        propertyInfo.type = null;
        propertyInfo.elementType = null;
        propertyInfo.value = element;
        propertyInfo.multiRef = false;
    }

    public PropertyInfo getPropertyInfo(int index) {
        Object element = this.properties.elementAt(index);
        if (element instanceof PropertyInfo) {
            return (PropertyInfo) element;
        }
        return null;
    }

    public SoapObject newInstance() {
        SoapObject o = new SoapObject(this.namespace, this.name);
        for (int propIndex = 0; propIndex < this.properties.size(); propIndex++) {
            Object prop = this.properties.elementAt(propIndex);
            if (prop instanceof PropertyInfo) {
                o.addProperty((PropertyInfo) ((PropertyInfo) this.properties.elementAt(propIndex)).clone());
            } else if (prop instanceof SoapObject) {
                o.addSoapObject(((SoapObject) prop).newInstance());
            }
        }
        for (int attribIndex = 0; attribIndex < getAttributeCount(); attribIndex++) {
            AttributeInfo newAI = new AttributeInfo();
            getAttributeInfo(attribIndex, newAI);
            o.addAttribute(newAI);
        }
        return o;
    }

    @Override // org.ksoap2.serialization.KvmSerializable
    public void setProperty(int index, Object value) {
        Object prop = this.properties.elementAt(index);
        if (prop instanceof PropertyInfo) {
            ((PropertyInfo) prop).setValue(value);
        }
    }

    public SoapObject addProperty(String name2, Object value) {
        Class<?> cls;
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.name = name2;
        if (value == null) {
            cls = PropertyInfo.OBJECT_CLASS;
        } else {
            cls = value.getClass();
        }
        propertyInfo.type = cls;
        propertyInfo.value = value;
        return addProperty(propertyInfo);
    }

    public SoapObject addProperty(String namespace2, String name2, Object value) {
        Class<?> cls;
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.name = name2;
        propertyInfo.namespace = namespace2;
        if (value == null) {
            cls = PropertyInfo.OBJECT_CLASS;
        } else {
            cls = value.getClass();
        }
        propertyInfo.type = cls;
        propertyInfo.value = value;
        return addProperty(propertyInfo);
    }

    public SoapObject addPropertyIfValue(String namespace2, String name2, Object value) {
        if (value != null) {
            return addProperty(namespace2, name2, value);
        }
        return this;
    }

    public SoapObject addPropertyIfValue(String name2, Object value) {
        if (value != null) {
            return addProperty(name2, value);
        }
        return this;
    }

    public SoapObject addPropertyIfValue(PropertyInfo propertyInfo, Object value) {
        if (value == null) {
            return this;
        }
        propertyInfo.setValue(value);
        return addProperty(propertyInfo);
    }

    public SoapObject addProperty(PropertyInfo propertyInfo) {
        this.properties.addElement(propertyInfo);
        return this;
    }

    public SoapObject addPropertyIfValue(PropertyInfo propertyInfo) {
        if (propertyInfo.value == null) {
            return this;
        }
        this.properties.addElement(propertyInfo);
        return this;
    }

    public SoapObject addSoapObject(SoapObject soapObject) {
        this.properties.addElement(soapObject);
        return this;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(EMPTY_STRING + this.name + "{");
        for (int i = 0; i < getPropertyCount(); i++) {
            Object prop = this.properties.elementAt(i);
            if (prop instanceof PropertyInfo) {
                buf.append(EMPTY_STRING);
                buf.append(((PropertyInfo) prop).getName());
                buf.append("=");
                buf.append(getProperty(i));
                buf.append("; ");
            } else {
                buf.append(((SoapObject) prop).toString());
            }
        }
        buf.append("}");
        return buf.toString();
    }

    @Override // org.ksoap2.serialization.HasInnerText
    public Object getInnerText() {
        return this.innerText;
    }

    @Override // org.ksoap2.serialization.HasInnerText
    public void setInnerText(Object innerText2) {
        this.innerText = innerText2;
    }

    public void removePropertyInfo(Object info) {
        this.properties.remove(info);
    }
}
