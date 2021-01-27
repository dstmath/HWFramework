package org.ksoap2.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

public class SppPropertyInfo implements Serializable {
    public static final Class BOOLEAN_CLASS = new Boolean(true).getClass();
    public static final Class INTEGER_CLASS = new Integer(0).getClass();
    public static final Class LONG_CLASS = new Long(0).getClass();
    public static final int MULTI_REF = 2;
    public static final Class OBJECT_CLASS = new Object().getClass();
    public static final PropertyInfo OBJECT_TYPE = new PropertyInfo();
    public static final int REF_ONLY = 4;
    public static final Class STRING_CLASS = "".getClass();
    public static final int TRANSIENT = 1;
    public static final Class VECTOR_CLASS = new Vector().getClass();
    public PropertyInfo elementType;
    public int flags;
    public boolean multiRef;
    public String name;
    public String namespace;
    public Object type = OBJECT_CLASS;
    protected Object value;

    public void clear() {
        this.type = OBJECT_CLASS;
        this.flags = 0;
        this.name = null;
        this.namespace = null;
    }

    public PropertyInfo getElementType() {
        return this.elementType;
    }

    public void setElementType(PropertyInfo elementType2) {
        this.elementType = elementType2;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setFlags(int flags2) {
        this.flags = flags2;
    }

    public boolean isMultiRef() {
        return this.multiRef;
    }

    public void setMultiRef(boolean multiRef2) {
        this.multiRef = multiRef2;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace2) {
        this.namespace = namespace2;
    }

    public Object getType() {
        return this.type;
    }

    public void setType(Object type2) {
        this.type = type2;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value2) {
        this.value = value2;
    }

    @Override // java.lang.Object
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.name);
        sb.append(" : ");
        Object obj = this.value;
        if (obj != null) {
            sb.append(obj);
        } else {
            sb.append("(not set)");
        }
        return sb.toString();
    }

    @Override // java.lang.Object
    public Object clone() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            out.close();
            return new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return null;
        } catch (NotSerializableException nse) {
            nse.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
