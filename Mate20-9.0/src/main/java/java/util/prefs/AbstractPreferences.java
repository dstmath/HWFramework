package java.util.prefs;

import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

public abstract class AbstractPreferences extends Preferences {
    private static final AbstractPreferences[] EMPTY_ABSTRACT_PREFS_ARRAY = new AbstractPreferences[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static Thread eventDispatchThread = null;
    /* access modifiers changed from: private */
    public static final List<EventObject> eventQueue = new LinkedList();
    private final String absolutePath;
    private Map<String, AbstractPreferences> kidCache = new HashMap();
    protected final Object lock = new Object();
    private final String name;
    protected boolean newNode = false;
    private final ArrayList<NodeChangeListener> nodeListeners = new ArrayList<>();
    final AbstractPreferences parent;
    private final ArrayList<PreferenceChangeListener> prefListeners = new ArrayList<>();
    private boolean removed = false;
    /* access modifiers changed from: private */
    public final AbstractPreferences root;

    private static class EventDispatchThread extends Thread {
        private EventDispatchThread() {
        }

        public void run() {
            int i;
            EventObject event;
            while (true) {
                synchronized (AbstractPreferences.eventQueue) {
                    while (AbstractPreferences.eventQueue.isEmpty()) {
                        try {
                            AbstractPreferences.eventQueue.wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    i = 0;
                    event = (EventObject) AbstractPreferences.eventQueue.remove(0);
                }
                AbstractPreferences src = (AbstractPreferences) event.getSource();
                if (event instanceof PreferenceChangeEvent) {
                    PreferenceChangeEvent pce = (PreferenceChangeEvent) event;
                    PreferenceChangeListener[] listeners = src.prefListeners();
                    while (i < listeners.length) {
                        listeners[i].preferenceChange(pce);
                        i++;
                    }
                } else {
                    NodeChangeEvent nce = (NodeChangeEvent) event;
                    NodeChangeListener[] listeners2 = src.nodeListeners();
                    if (nce instanceof NodeAddedEvent) {
                        while (i < listeners2.length) {
                            listeners2[i].childAdded(nce);
                            i++;
                        }
                    } else {
                        while (i < listeners2.length) {
                            listeners2[i].childRemoved(nce);
                            i++;
                        }
                    }
                }
            }
        }
    }

    private class NodeAddedEvent extends NodeChangeEvent {
        private static final long serialVersionUID = -6743557530157328528L;

        NodeAddedEvent(Preferences parent, Preferences child) {
            super(parent, child);
        }
    }

    private class NodeRemovedEvent extends NodeChangeEvent {
        private static final long serialVersionUID = 8735497392918824837L;

        NodeRemovedEvent(Preferences parent, Preferences child) {
            super(parent, child);
        }
    }

    /* access modifiers changed from: protected */
    public abstract AbstractPreferences childSpi(String str);

    /* access modifiers changed from: protected */
    public abstract String[] childrenNamesSpi() throws BackingStoreException;

    /* access modifiers changed from: protected */
    public abstract void flushSpi() throws BackingStoreException;

    /* access modifiers changed from: protected */
    public abstract String getSpi(String str);

    /* access modifiers changed from: protected */
    public abstract String[] keysSpi() throws BackingStoreException;

    /* access modifiers changed from: protected */
    public abstract void putSpi(String str, String str2);

    /* access modifiers changed from: protected */
    public abstract void removeNodeSpi() throws BackingStoreException;

    /* access modifiers changed from: protected */
    public abstract void removeSpi(String str);

    /* access modifiers changed from: protected */
    public abstract void syncSpi() throws BackingStoreException;

    protected AbstractPreferences(AbstractPreferences parent2, String name2) {
        String str;
        if (parent2 == null) {
            if (name2.equals("")) {
                this.absolutePath = "/";
                this.root = this;
            } else {
                throw new IllegalArgumentException("Root name '" + name2 + "' must be \"\"");
            }
        } else if (name2.indexOf(47) != -1) {
            throw new IllegalArgumentException("Name '" + name2 + "' contains '/'");
        } else if (!name2.equals("")) {
            this.root = parent2.root;
            if (parent2 == this.root) {
                str = "/" + name2;
            } else {
                str = parent2.absolutePath() + "/" + name2;
            }
            this.absolutePath = str;
        } else {
            throw new IllegalArgumentException("Illegal name: empty string");
        }
        this.name = name2;
        this.parent = parent2;
    }

    public void put(String key, String value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        } else if (key.length() > 80) {
            throw new IllegalArgumentException("Key too long: " + key);
        } else if (value.length() <= 8192) {
            synchronized (this.lock) {
                if (!this.removed) {
                    putSpi(key, value);
                    enqueuePreferenceChangeEvent(key, value);
                } else {
                    throw new IllegalStateException("Node has been removed.");
                }
            }
        } else {
            throw new IllegalArgumentException("Value too long: " + value);
        }
    }

    public String get(String key, String def) {
        String str;
        if (key != null) {
            synchronized (this.lock) {
                if (!this.removed) {
                    String result = null;
                    try {
                        result = getSpi(key);
                    } catch (Exception e) {
                    }
                    str = result == null ? def : result;
                } else {
                    throw new IllegalStateException("Node has been removed.");
                }
            }
            return str;
        }
        throw new NullPointerException("Null key");
    }

    public void remove(String key) {
        Objects.requireNonNull(key, "Specified key cannot be null");
        synchronized (this.lock) {
            if (!this.removed) {
                removeSpi(key);
                enqueuePreferenceChangeEvent(key, null);
            } else {
                throw new IllegalStateException("Node has been removed.");
            }
        }
    }

    public void clear() throws BackingStoreException {
        synchronized (this.lock) {
            String[] keys = keys();
            for (String remove : keys) {
                remove(remove);
            }
        }
    }

    public void putInt(String key, int value) {
        put(key, Integer.toString(value));
    }

    public int getInt(String key, int def) {
        int result = def;
        try {
            String value = get(key, null);
            if (value != null) {
                return Integer.parseInt(value);
            }
            return result;
        } catch (NumberFormatException e) {
            return result;
        }
    }

    public void putLong(String key, long value) {
        put(key, Long.toString(value));
    }

    public long getLong(String key, long def) {
        long result = def;
        try {
            String value = get(key, null);
            if (value != null) {
                return Long.parseLong(value);
            }
            return result;
        } catch (NumberFormatException e) {
            return result;
        }
    }

    public void putBoolean(String key, boolean value) {
        put(key, String.valueOf(value));
    }

    public boolean getBoolean(String key, boolean def) {
        boolean result = def;
        String value = get(key, null);
        if (value == null) {
            return result;
        }
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        return result;
    }

    public void putFloat(String key, float value) {
        put(key, Float.toString(value));
    }

    public float getFloat(String key, float def) {
        float result = def;
        try {
            String value = get(key, null);
            if (value != null) {
                return Float.parseFloat(value);
            }
            return result;
        } catch (NumberFormatException e) {
            return result;
        }
    }

    public void putDouble(String key, double value) {
        put(key, Double.toString(value));
    }

    public double getDouble(String key, double def) {
        double result = def;
        try {
            String value = get(key, null);
            if (value != null) {
                return Double.parseDouble(value);
            }
            return result;
        } catch (NumberFormatException e) {
            return result;
        }
    }

    public void putByteArray(String key, byte[] value) {
        put(key, Base64.byteArrayToBase64(value));
    }

    public byte[] getByteArray(String key, byte[] def) {
        byte[] result = def;
        String value = get(key, null);
        if (value == null) {
            return result;
        }
        try {
            return Base64.base64ToByteArray(value);
        } catch (RuntimeException e) {
            return result;
        }
    }

    public String[] keys() throws BackingStoreException {
        String[] keysSpi;
        synchronized (this.lock) {
            if (!this.removed) {
                keysSpi = keysSpi();
            } else {
                throw new IllegalStateException("Node has been removed.");
            }
        }
        return keysSpi;
    }

    public String[] childrenNames() throws BackingStoreException {
        String[] strArr;
        synchronized (this.lock) {
            if (!this.removed) {
                Set<String> s = new TreeSet<>((Collection<? extends String>) this.kidCache.keySet());
                for (String kid : childrenNamesSpi()) {
                    s.add(kid);
                }
                strArr = (String[]) s.toArray(EMPTY_STRING_ARRAY);
            } else {
                throw new IllegalStateException("Node has been removed.");
            }
        }
        return strArr;
    }

    /* access modifiers changed from: protected */
    public final AbstractPreferences[] cachedChildren() {
        return (AbstractPreferences[]) this.kidCache.values().toArray(EMPTY_ABSTRACT_PREFS_ARRAY);
    }

    public Preferences parent() {
        AbstractPreferences abstractPreferences;
        synchronized (this.lock) {
            if (!this.removed) {
                abstractPreferences = this.parent;
            } else {
                throw new IllegalStateException("Node has been removed.");
            }
        }
        return abstractPreferences;
    }

    public Preferences node(String path) {
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            } else if (path.equals("")) {
                return this;
            } else {
                if (path.equals("/")) {
                    AbstractPreferences abstractPreferences = this.root;
                    return abstractPreferences;
                } else if (path.charAt(0) == '/') {
                    return this.root.node(new StringTokenizer(path.substring(1), "/", true));
                } else {
                    Preferences node = node(new StringTokenizer(path, "/", true));
                    return node;
                }
            }
        }
    }

    private Preferences node(StringTokenizer path) {
        String token = path.nextToken();
        if (!token.equals("/")) {
            synchronized (this.lock) {
                AbstractPreferences child = this.kidCache.get(token);
                if (child == null) {
                    if (token.length() <= 80) {
                        child = childSpi(token);
                        if (child.newNode) {
                            enqueueNodeAddedEvent(child);
                        }
                        this.kidCache.put(token, child);
                    } else {
                        throw new IllegalArgumentException("Node name " + token + " too long");
                    }
                }
                if (!path.hasMoreTokens()) {
                    return child;
                }
                path.nextToken();
                if (path.hasMoreTokens()) {
                    Preferences node = child.node(path);
                    return node;
                }
                throw new IllegalArgumentException("Path ends with slash");
            }
        }
        throw new IllegalArgumentException("Consecutive slashes in path");
    }

    public boolean nodeExists(String path) throws BackingStoreException {
        synchronized (this.lock) {
            if (path.equals("")) {
                boolean z = !this.removed;
                return z;
            } else if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            } else if (path.equals("/")) {
                return true;
            } else {
                if (path.charAt(0) == '/') {
                    return this.root.nodeExists(new StringTokenizer(path.substring(1), "/", true));
                }
                boolean nodeExists = nodeExists(new StringTokenizer(path, "/", true));
                return nodeExists;
            }
        }
    }

    private boolean nodeExists(StringTokenizer path) throws BackingStoreException {
        String token = path.nextToken();
        if (!token.equals("/")) {
            synchronized (this.lock) {
                AbstractPreferences child = this.kidCache.get(token);
                if (child == null) {
                    child = getChild(token);
                }
                if (child == null) {
                    return false;
                }
                if (!path.hasMoreTokens()) {
                    return true;
                }
                path.nextToken();
                if (path.hasMoreTokens()) {
                    boolean nodeExists = child.nodeExists(path);
                    return nodeExists;
                }
                throw new IllegalArgumentException("Path ends with slash");
            }
        }
        throw new IllegalArgumentException("Consecutive slashes in path");
    }

    public void removeNode() throws BackingStoreException {
        if (this != this.root) {
            synchronized (this.parent.lock) {
                removeNode2();
                this.parent.kidCache.remove(this.name);
            }
            return;
        }
        throw new UnsupportedOperationException("Can't remove the root!");
    }

    private void removeNode2() throws BackingStoreException {
        synchronized (this.lock) {
            if (!this.removed) {
                String[] kidNames = childrenNamesSpi();
                for (int i = 0; i < kidNames.length; i++) {
                    if (!this.kidCache.containsKey(kidNames[i])) {
                        this.kidCache.put(kidNames[i], childSpi(kidNames[i]));
                    }
                }
                Iterator<AbstractPreferences> i2 = this.kidCache.values().iterator();
                while (i2.hasNext()) {
                    try {
                        i2.next().removeNode2();
                        i2.remove();
                    } catch (BackingStoreException e) {
                    }
                }
                removeNodeSpi();
                this.removed = true;
                this.parent.enqueueNodeRemovedEvent(this);
            } else {
                throw new IllegalStateException("Node already removed.");
            }
        }
    }

    public String name() {
        return this.name;
    }

    public String absolutePath() {
        return this.absolutePath;
    }

    public boolean isUserNode() {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                return Boolean.valueOf(AbstractPreferences.this.root == Preferences.userRoot());
            }
        })).booleanValue();
    }

    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        if (pcl != null) {
            synchronized (this.lock) {
                if (!this.removed) {
                    this.prefListeners.add(pcl);
                } else {
                    throw new IllegalStateException("Node has been removed.");
                }
            }
            startEventDispatchThreadIfNecessary();
            return;
        }
        throw new NullPointerException("Change listener is null.");
    }

    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            } else if (this.prefListeners.contains(pcl)) {
                this.prefListeners.remove((Object) pcl);
            } else {
                throw new IllegalArgumentException("Listener not registered.");
            }
        }
    }

    public void addNodeChangeListener(NodeChangeListener ncl) {
        if (ncl != null) {
            synchronized (this.lock) {
                if (!this.removed) {
                    this.nodeListeners.add(ncl);
                } else {
                    throw new IllegalStateException("Node has been removed.");
                }
            }
            startEventDispatchThreadIfNecessary();
            return;
        }
        throw new NullPointerException("Change listener is null.");
    }

    public void removeNodeChangeListener(NodeChangeListener ncl) {
        synchronized (this.lock) {
            if (this.removed) {
                throw new IllegalStateException("Node has been removed.");
            } else if (this.nodeListeners.contains(ncl)) {
                this.nodeListeners.remove((Object) ncl);
            } else {
                throw new IllegalArgumentException("Listener not registered.");
            }
        }
    }

    /* access modifiers changed from: protected */
    public AbstractPreferences getChild(String nodeName) throws BackingStoreException {
        synchronized (this.lock) {
            String[] kidNames = childrenNames();
            for (int i = 0; i < kidNames.length; i++) {
                if (kidNames[i].equals(nodeName)) {
                    AbstractPreferences childSpi = childSpi(kidNames[i]);
                    return childSpi;
                }
            }
            return null;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(isUserNode() ? "User" : "System");
        sb.append(" Preference Node: ");
        sb.append(absolutePath());
        return sb.toString();
    }

    public void sync() throws BackingStoreException {
        sync2();
    }

    private void sync2() throws BackingStoreException {
        AbstractPreferences[] cachedKids;
        synchronized (this.lock) {
            if (!this.removed) {
                syncSpi();
                cachedKids = cachedChildren();
            } else {
                throw new IllegalStateException("Node has been removed");
            }
        }
        for (AbstractPreferences sync2 : cachedKids) {
            sync2.sync2();
        }
    }

    public void flush() throws BackingStoreException {
        flush2();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0013, code lost:
        if (r0 >= r1.length) goto L_0x001d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0015, code lost:
        r1[r0].flush2();
        r0 = r0 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0011, code lost:
        r0 = 0;
     */
    private void flush2() throws BackingStoreException {
        synchronized (this.lock) {
            flushSpi();
            if (!this.removed) {
                AbstractPreferences[] cachedKids = cachedChildren();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isRemoved() {
        boolean z;
        synchronized (this.lock) {
            z = this.removed;
        }
        return z;
    }

    private static synchronized void startEventDispatchThreadIfNecessary() {
        synchronized (AbstractPreferences.class) {
            if (eventDispatchThread == null) {
                eventDispatchThread = new EventDispatchThread();
                eventDispatchThread.setDaemon(true);
                eventDispatchThread.start();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public PreferenceChangeListener[] prefListeners() {
        PreferenceChangeListener[] preferenceChangeListenerArr;
        synchronized (this.lock) {
            preferenceChangeListenerArr = (PreferenceChangeListener[]) this.prefListeners.toArray(new PreferenceChangeListener[this.prefListeners.size()]);
        }
        return preferenceChangeListenerArr;
    }

    /* access modifiers changed from: package-private */
    public NodeChangeListener[] nodeListeners() {
        NodeChangeListener[] nodeChangeListenerArr;
        synchronized (this.lock) {
            nodeChangeListenerArr = (NodeChangeListener[]) this.nodeListeners.toArray(new NodeChangeListener[this.nodeListeners.size()]);
        }
        return nodeChangeListenerArr;
    }

    private void enqueuePreferenceChangeEvent(String key, String newValue) {
        if (!this.prefListeners.isEmpty()) {
            synchronized (eventQueue) {
                eventQueue.add(new PreferenceChangeEvent(this, key, newValue));
                eventQueue.notify();
            }
        }
    }

    private void enqueueNodeAddedEvent(Preferences child) {
        if (!this.nodeListeners.isEmpty()) {
            synchronized (eventQueue) {
                eventQueue.add(new NodeAddedEvent(this, child));
                eventQueue.notify();
            }
        }
    }

    private void enqueueNodeRemovedEvent(Preferences child) {
        if (!this.nodeListeners.isEmpty()) {
            synchronized (eventQueue) {
                eventQueue.add(new NodeRemovedEvent(this, child));
                eventQueue.notify();
            }
        }
    }

    public void exportNode(OutputStream os) throws IOException, BackingStoreException {
        XmlSupport.export(os, this, false);
    }

    public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
        XmlSupport.export(os, this, true);
    }
}
