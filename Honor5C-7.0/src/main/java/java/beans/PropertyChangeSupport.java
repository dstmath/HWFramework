package java.beans;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map.Entry;

public class PropertyChangeSupport implements Serializable {
    private static final ObjectStreamField[] serialPersistentFields = null;
    static final long serialVersionUID = 6401253773779951803L;
    private PropertyChangeListenerMap map;
    private Object source;

    private static final class PropertyChangeListenerMap extends ChangeListenerMap<PropertyChangeListener> {
        private static final PropertyChangeListener[] EMPTY = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.beans.PropertyChangeSupport.PropertyChangeListenerMap.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.beans.PropertyChangeSupport.PropertyChangeListenerMap.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.beans.PropertyChangeSupport.PropertyChangeListenerMap.<clinit>():void");
        }

        private PropertyChangeListenerMap() {
        }

        protected PropertyChangeListener[] newArray(int length) {
            if (length > 0) {
                return new PropertyChangeListener[length];
            }
            return EMPTY;
        }

        protected PropertyChangeListener newProxy(String name, PropertyChangeListener listener) {
            return new PropertyChangeListenerProxy(name, listener);
        }

        public final PropertyChangeListener extract(PropertyChangeListener listener) {
            while (listener instanceof PropertyChangeListenerProxy) {
                listener = (PropertyChangeListener) ((PropertyChangeListenerProxy) listener).getListener();
            }
            return listener;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.beans.PropertyChangeSupport.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.beans.PropertyChangeSupport.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.beans.PropertyChangeSupport.<clinit>():void");
    }

    public PropertyChangeSupport(Object sourceBean) {
        this.map = new PropertyChangeListenerMap();
        if (sourceBean == null) {
            throw new NullPointerException();
        }
        this.source = sourceBean;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            if (listener instanceof PropertyChangeListenerProxy) {
                PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
                addPropertyChangeListener(proxy.getPropertyName(), (PropertyChangeListener) proxy.getListener());
            } else {
                this.map.add(null, listener);
            }
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            if (listener instanceof PropertyChangeListenerProxy) {
                PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
                removePropertyChangeListener(proxy.getPropertyName(), (PropertyChangeListener) proxy.getListener());
            } else {
                this.map.remove(null, listener);
            }
        }
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return (PropertyChangeListener[]) this.map.getListeners();
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (listener != null && propertyName != null) {
            listener = this.map.extract(listener);
            if (listener != null) {
                this.map.add(propertyName, listener);
            }
        }
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (listener != null && propertyName != null) {
            listener = this.map.extract(listener);
            if (listener != null) {
                this.map.remove(propertyName, listener);
            }
        }
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return (PropertyChangeListener[]) this.map.getListeners(propertyName);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            firePropertyChange(new PropertyChangeEvent(this.source, propertyName, oldValue, newValue));
        }
    }

    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        if (oldValue != newValue) {
            firePropertyChange(propertyName, Integer.valueOf(oldValue), Integer.valueOf(newValue));
        }
    }

    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
            firePropertyChange(propertyName, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
        }
    }

    public void firePropertyChange(PropertyChangeEvent event) {
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            PropertyChangeListener[] propertyChangeListenerArr;
            String name = event.getPropertyName();
            PropertyChangeListener[] common = (PropertyChangeListener[]) this.map.get(null);
            if (name != null) {
                propertyChangeListenerArr = (PropertyChangeListener[]) this.map.get(name);
            } else {
                propertyChangeListenerArr = null;
            }
            fire(common, event);
            fire(propertyChangeListenerArr, event);
        }
    }

    private static void fire(PropertyChangeListener[] listeners, PropertyChangeEvent event) {
        if (listeners != null) {
            for (PropertyChangeListener listener : listeners) {
                listener.propertyChange(event);
            }
        }
    }

    public void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        if (oldValue == null || newValue == null || !oldValue.equals(newValue)) {
            firePropertyChange(new IndexedPropertyChangeEvent(this.source, propertyName, oldValue, newValue, index));
        }
    }

    public void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
        if (oldValue != newValue) {
            fireIndexedPropertyChange(propertyName, index, Integer.valueOf(oldValue), Integer.valueOf(newValue));
        }
    }

    public void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
            fireIndexedPropertyChange(propertyName, index, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
        }
    }

    public boolean hasListeners(String propertyName) {
        return this.map.hasListeners(propertyName);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeObject(ObjectOutputStream s) throws IOException {
        Throwable th;
        PropertyChangeListener[] listeners = null;
        synchronized (this.map) {
            try {
                Hashtable<String, PropertyChangeSupport> children = null;
                for (Entry<String, PropertyChangeListener[]> entry : this.map.getEntries()) {
                    try {
                        Hashtable<String, PropertyChangeSupport> children2;
                        String property = (String) entry.getKey();
                        if (property == null) {
                            listeners = (PropertyChangeListener[]) entry.getValue();
                            children2 = children;
                        } else {
                            if (children == null) {
                                children2 = new Hashtable();
                            } else {
                                children2 = children;
                            }
                            PropertyChangeSupport pcs = new PropertyChangeSupport(this.source);
                            pcs.map.set(null, (PropertyChangeListener[]) entry.getValue());
                            children2.put(property, pcs);
                        }
                        children = children2;
                    } catch (Throwable th2) {
                        th = th2;
                        Object obj = children;
                    }
                }
                PutField fields = s.putFields();
                fields.put("children", (Object) children);
                fields.put("source", this.source);
                fields.put("propertyChangeSupportSerializedDataVersion", 2);
                s.writeFields();
                if (listeners != null) {
                    for (PropertyChangeListener l : listeners) {
                        if (l instanceof Serializable) {
                            s.writeObject(l);
                        }
                    }
                }
                s.writeObject(null);
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        this.map = new PropertyChangeListenerMap();
        GetField fields = s.readFields();
        Hashtable<String, PropertyChangeSupport> children = (Hashtable) fields.get("children", null);
        this.source = fields.get("source", null);
        fields.get("propertyChangeSupportSerializedDataVersion", 2);
        while (true) {
            Object listenerOrNull = s.readObject();
            if (listenerOrNull == null) {
                break;
            }
            this.map.add(null, (PropertyChangeListener) listenerOrNull);
        }
        if (children != null) {
            for (Entry<String, PropertyChangeSupport> entry : children.entrySet()) {
                for (PropertyChangeListener listener : ((PropertyChangeSupport) entry.getValue()).getPropertyChangeListeners()) {
                    this.map.add((String) entry.getKey(), listener);
                }
            }
        }
    }
}
