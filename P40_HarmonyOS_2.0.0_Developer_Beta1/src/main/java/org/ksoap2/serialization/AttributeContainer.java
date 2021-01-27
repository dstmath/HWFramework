package org.ksoap2.serialization;

import java.util.Vector;

public class AttributeContainer implements HasAttributes {
    protected Vector attributes = new Vector();

    @Override // org.ksoap2.serialization.HasAttributes
    public void getAttributeInfo(int index, AttributeInfo attributeInfo) {
        AttributeInfo p = (AttributeInfo) this.attributes.elementAt(index);
        attributeInfo.name = p.name;
        attributeInfo.namespace = p.namespace;
        attributeInfo.flags = p.flags;
        attributeInfo.type = p.type;
        attributeInfo.elementType = p.elementType;
        attributeInfo.value = p.getValue();
    }

    public Object getAttribute(int index) {
        return ((AttributeInfo) this.attributes.elementAt(index)).getValue();
    }

    public String getAttributeAsString(int index) {
        return ((AttributeInfo) this.attributes.elementAt(index)).getValue().toString();
    }

    public Object getAttribute(String name) {
        Integer i = attributeIndex(name);
        if (i != null) {
            return getAttribute(i.intValue());
        }
        throw new RuntimeException("illegal property: " + name);
    }

    public Object getAttribute(String namespace, String name) {
        Integer i = attributeIndex(namespace, name);
        if (i != null) {
            return getAttribute(i.intValue());
        }
        throw new RuntimeException("illegal property: " + name);
    }

    public String getAttributeAsString(String name) {
        Integer i = attributeIndex(name);
        if (i != null) {
            return getAttribute(i.intValue()).toString();
        }
        throw new RuntimeException("illegal property: " + name);
    }

    public String getAttributeAsString(String namespace, String name) {
        Integer i = attributeIndex(namespace, name);
        if (i != null) {
            return getAttribute(i.intValue()).toString();
        }
        throw new RuntimeException("illegal property: " + name);
    }

    public boolean hasAttribute(String name) {
        if (attributeIndex(name) != null) {
            return true;
        }
        return false;
    }

    public boolean hasAttribute(String namespace, String name) {
        if (attributeIndex(namespace, name) != null) {
            return true;
        }
        return false;
    }

    public Object getAttributeSafely(String name) {
        Integer i = attributeIndex(name);
        if (i != null) {
            return getAttribute(i.intValue());
        }
        return null;
    }

    public Object getAttributeSafely(String namespace, String name) {
        Integer i = attributeIndex(namespace, name);
        if (i != null) {
            return getAttribute(i.intValue());
        }
        return null;
    }

    public Object getAttributeSafelyAsString(String name) {
        Integer i = attributeIndex(name);
        if (i != null) {
            return getAttribute(i.intValue()).toString();
        }
        return "";
    }

    public Object getAttributeSafelyAsString(String namespace, String name) {
        Integer i = attributeIndex(namespace, name);
        if (i != null) {
            return getAttribute(i.intValue()).toString();
        }
        return "";
    }

    private Integer attributeIndex(String name) {
        for (int i = 0; i < this.attributes.size(); i++) {
            if (name.equals(((AttributeInfo) this.attributes.elementAt(i)).getName())) {
                return new Integer(i);
            }
        }
        return null;
    }

    private Integer attributeIndex(String namespace, String name) {
        for (int i = 0; i < this.attributes.size(); i++) {
            AttributeInfo attrInfo = (AttributeInfo) this.attributes.elementAt(i);
            if (name.equals(attrInfo.getName()) && namespace.equals(attrInfo.getNamespace())) {
                return new Integer(i);
            }
        }
        return null;
    }

    @Override // org.ksoap2.serialization.HasAttributes
    public int getAttributeCount() {
        return this.attributes.size();
    }

    /* access modifiers changed from: protected */
    public boolean attributesAreEqual(AttributeContainer other) {
        int numAttributes = getAttributeCount();
        if (numAttributes != other.getAttributeCount()) {
            return false;
        }
        for (int attribIndex = 0; attribIndex < numAttributes; attribIndex++) {
            AttributeInfo thisAttrib = (AttributeInfo) this.attributes.elementAt(attribIndex);
            Object thisAttribValue = thisAttrib.getValue();
            if (!(other.hasAttribute(thisAttrib.getName()) && thisAttribValue.equals(other.getAttributeSafely(thisAttrib.getName())))) {
                return false;
            }
        }
        return true;
    }

    public void addAttribute(String name, Object value) {
        addAttribute(null, name, value);
    }

    public void addAttribute(String namespace, String name, Object value) {
        AttributeInfo attributeInfo = new AttributeInfo();
        attributeInfo.name = name;
        attributeInfo.namespace = namespace;
        attributeInfo.type = value == null ? PropertyInfo.OBJECT_CLASS : value.getClass();
        attributeInfo.value = value;
        addAttribute(attributeInfo);
    }

    public void addAttributeIfValue(String name, Object value) {
        if (value != null) {
            addAttribute(name, value);
        }
    }

    public void addAttributeIfValue(String namespace, String name, Object value) {
        if (value != null) {
            addAttribute(namespace, name, value);
        }
    }

    public void addAttribute(AttributeInfo attributeInfo) {
        this.attributes.addElement(attributeInfo);
    }

    public void addAttributeIfValue(AttributeInfo attributeInfo) {
        if (attributeInfo.value != null) {
            this.attributes.addElement(attributeInfo);
        }
    }

    @Override // org.ksoap2.serialization.HasAttributes
    public void setAttribute(AttributeInfo info) {
    }

    @Override // org.ksoap2.serialization.HasAttributes
    public void getAttribute(int index, AttributeInfo info) {
    }
}
