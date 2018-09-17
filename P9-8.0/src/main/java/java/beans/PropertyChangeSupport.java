package java.beans;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

public class PropertyChangeSupport implements Serializable {
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("children", Hashtable.class), new ObjectStreamField("source", Object.class), new ObjectStreamField("propertyChangeSupportSerializedDataVersion", Integer.TYPE)};
    static final long serialVersionUID = 6401253773779951803L;
    private PropertyChangeListenerMap map = new PropertyChangeListenerMap();
    private Object source;

    private static final class PropertyChangeListenerMap extends ChangeListenerMap<PropertyChangeListener> {
        private static final PropertyChangeListener[] EMPTY = new PropertyChangeListener[0];

        /* synthetic */ PropertyChangeListenerMap(PropertyChangeListenerMap -this0) {
            this();
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

    public PropertyChangeSupport(Object sourceBean) {
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
            EventListener extract = this.map.extract(listener);
            if (extract != null) {
                this.map.add(propertyName, extract);
            }
        }
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (listener != null && propertyName != null) {
            EventListener extract = this.map.extract(listener);
            if (extract != null) {
                this.map.remove(propertyName, extract);
            }
        }
    }

    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return (PropertyChangeListener[]) this.map.getListeners(propertyName);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (oldValue == null || newValue == null || (oldValue.equals(newValue) ^ 1) != 0) {
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
        if (oldValue == null || newValue == null || (oldValue.equals(newValue) ^ 1) != 0) {
            PropertyChangeListener[] named;
            String name = event.getPropertyName();
            PropertyChangeListener[] common = (PropertyChangeListener[]) this.map.get(null);
            if (name != null) {
                named = (PropertyChangeListener[]) this.map.get(name);
            } else {
                named = null;
            }
            fire(common, event);
            fire(named, event);
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
        if (oldValue == null || newValue == null || (oldValue.equals(newValue) ^ 1) != 0) {
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

    /* JADX WARNING: Missing block: B:19:0x0052, code:
            r5 = r16.putFields();
            r5.put("children", (java.lang.Object) r2);
            r5.put("source", r15.source);
            r5.put("propertyChangeSupportSerializedDataVersion", 2);
            r16.writeFields();
     */
    /* JADX WARNING: Missing block: B:20:0x006e, code:
            if (r7 == null) goto L_0x0082;
     */
    /* JADX WARNING: Missing block: B:21:0x0070, code:
            r10 = 0;
            r11 = r7.length;
     */
    /* JADX WARNING: Missing block: B:22:0x0072, code:
            if (r10 >= r11) goto L_0x0082;
     */
    /* JADX WARNING: Missing block: B:23:0x0074, code:
            r6 = r7[r10];
     */
    /* JADX WARNING: Missing block: B:24:0x0078, code:
            if ((r6 instanceof java.io.Serializable) == false) goto L_0x007f;
     */
    /* JADX WARNING: Missing block: B:25:0x007a, code:
            r16.writeObject(r6);
     */
    /* JADX WARNING: Missing block: B:26:0x007f, code:
            r10 = r10 + 1;
     */
    /* JADX WARNING: Missing block: B:27:0x0082, code:
            r16.writeObject(null);
     */
    /* JADX WARNING: Missing block: B:28:0x0087, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeObject(ObjectOutputStream s) throws IOException {
        Throwable th;
        Hashtable<String, PropertyChangeSupport> children = null;
        PropertyChangeListener[] listeners = null;
        synchronized (this.map) {
            try {
                Iterator entry$iterator = this.map.getEntries().iterator();
                while (true) {
                    Hashtable<String, PropertyChangeSupport> children2;
                    try {
                        children2 = children;
                        if (entry$iterator.hasNext()) {
                            Entry<String, PropertyChangeListener[]> entry = (Entry) entry$iterator.next();
                            String property = (String) entry.getKey();
                            if (property == null) {
                                listeners = (PropertyChangeListener[]) entry.getValue();
                                children = children2;
                            } else {
                                if (children2 == null) {
                                    children = new Hashtable();
                                } else {
                                    children = children2;
                                }
                                PropertyChangeSupport pcs = new PropertyChangeSupport(this.source);
                                pcs.map.set(null, (PropertyChangeListener[]) entry.getValue());
                                children.put(property, pcs);
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        Object obj = children2;
                        throw th;
                    }
                }
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
