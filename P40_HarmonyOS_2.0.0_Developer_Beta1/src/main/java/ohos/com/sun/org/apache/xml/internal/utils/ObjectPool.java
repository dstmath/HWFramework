package ohos.com.sun.org.apache.xml.internal.utils;

import java.io.Serializable;
import java.util.ArrayList;
import ohos.com.sun.org.apache.xalan.internal.utils.ObjectFactory;
import ohos.com.sun.org.apache.xml.internal.res.XMLMessages;

public class ObjectPool implements Serializable {
    static final long serialVersionUID = -8519013691660936643L;
    private final ArrayList freeStack;
    private final Class objectType;

    public ObjectPool(Class cls) {
        this.objectType = cls;
        this.freeStack = new ArrayList();
    }

    public ObjectPool(String str) {
        try {
            this.objectType = ObjectFactory.findProviderClass(str, true);
            this.freeStack = new ArrayList();
        } catch (ClassNotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    public ObjectPool(Class cls, int i) {
        this.objectType = cls;
        this.freeStack = new ArrayList(i);
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
            } catch (IllegalAccessException | InstantiationException unused) {
                throw new RuntimeException(XMLMessages.createXMLMessage("ER_EXCEPTION_CREATING_POOL", null));
            }
        } else {
            return this.freeStack.remove(this.freeStack.size() - 1);
        }
    }

    public synchronized void freeInstance(Object obj) {
        this.freeStack.add(obj);
    }
}
