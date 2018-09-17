package org.apache.xml.utils;

import java.io.Serializable;
import java.util.ArrayList;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;

public class ObjectPool implements Serializable {
    static final long serialVersionUID = -8519013691660936643L;
    private final ArrayList freeStack;
    private final Class objectType;

    public ObjectPool(Class type) {
        this.objectType = type;
        this.freeStack = new ArrayList();
    }

    public ObjectPool(String className) {
        try {
            this.objectType = ObjectFactory.findProviderClass(className, ObjectFactory.findClassLoader(), true);
            this.freeStack = new ArrayList();
        } catch (ClassNotFoundException cnfe) {
            throw new WrappedRuntimeException(cnfe);
        }
    }

    public ObjectPool(Class type, int size) {
        this.objectType = type;
        this.freeStack = new ArrayList(size);
    }

    public ObjectPool() {
        this.objectType = null;
        this.freeStack = new ArrayList();
    }

    public synchronized Object getInstanceIfFree() {
        if (this.freeStack.isEmpty()) {
            return null;
        }
        return this.freeStack.remove(this.freeStack.size() - 1);
    }

    public synchronized Object getInstance() {
        if (this.freeStack.isEmpty()) {
            try {
                return this.objectType.newInstance();
            } catch (InstantiationException e) {
            } catch (IllegalAccessException e2) {
            }
        } else {
            return this.freeStack.remove(this.freeStack.size() - 1);
        }
        throw new RuntimeException(XMLMessages.createXMLMessage(XMLErrorResources.ER_EXCEPTION_CREATING_POOL, null));
    }

    public synchronized void freeInstance(Object obj) {
        this.freeStack.add(obj);
    }
}
