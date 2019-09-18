package java.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

final class UnresolvedPermissionCollection extends PermissionCollection implements Serializable {
    private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("permissions", Hashtable.class)};
    private static final long serialVersionUID = -7176153071733132400L;
    private transient Map<String, List<UnresolvedPermission>> perms = new HashMap(11);

    public void add(Permission permission) {
        List<UnresolvedPermission> v;
        if (permission instanceof UnresolvedPermission) {
            UnresolvedPermission up = (UnresolvedPermission) permission;
            synchronized (this) {
                v = this.perms.get(up.getName());
                if (v == null) {
                    v = new ArrayList<>();
                    this.perms.put(up.getName(), v);
                }
            }
            synchronized (v) {
                v.add(up);
            }
            return;
        }
        throw new IllegalArgumentException("invalid permission: " + permission);
    }

    /* access modifiers changed from: package-private */
    public List<UnresolvedPermission> getUnresolvedPermissions(Permission p) {
        List<UnresolvedPermission> list;
        synchronized (this) {
            list = this.perms.get(p.getClass().getName());
        }
        return list;
    }

    public boolean implies(Permission permission) {
        return false;
    }

    public Enumeration<Permission> elements() {
        List<Permission> results = new ArrayList<>();
        synchronized (this) {
            for (List<UnresolvedPermission> l : this.perms.values()) {
                synchronized (l) {
                    results.addAll(l);
                }
            }
        }
        return Collections.enumeration(results);
    }

    /* JADX WARNING: CFG modification limit reached, blocks count: 127 */
    private void writeObject(ObjectOutputStream out) throws IOException {
        Hashtable<String, Vector<UnresolvedPermission>> permissions = new Hashtable<>(this.perms.size() * 2);
        synchronized (this) {
            for (Map.Entry<String, List<UnresolvedPermission>> e : this.perms.entrySet()) {
                List<UnresolvedPermission> list = e.getValue();
                Vector<UnresolvedPermission> vec = new Vector<>(list.size());
                synchronized (list) {
                    try {
                        vec.addAll(list);
                    } catch (Throwable th) {
                        while (true) {
                            throw th;
                        }
                    }
                }
                permissions.put(e.getKey(), vec);
            }
        }
        out.putFields().put("permissions", (Object) permissions);
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Hashtable<String, Vector<UnresolvedPermission>> permissions = (Hashtable) in.readFields().get("permissions", (Object) null);
        this.perms = new HashMap(permissions.size() * 2);
        for (Map.Entry<String, Vector<UnresolvedPermission>> e : permissions.entrySet()) {
            Vector<UnresolvedPermission> vec = e.getValue();
            List<UnresolvedPermission> list = new ArrayList<>(vec.size());
            list.addAll(vec);
            this.perms.put(e.getKey(), list);
        }
    }
}
