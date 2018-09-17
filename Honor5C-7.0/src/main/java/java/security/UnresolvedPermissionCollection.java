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
import java.util.Map.Entry;
import java.util.Vector;

final class UnresolvedPermissionCollection extends PermissionCollection implements Serializable {
    private static final ObjectStreamField[] serialPersistentFields = null;
    private static final long serialVersionUID = -7176153071733132400L;
    private transient Map<String, List<UnresolvedPermission>> perms;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.security.UnresolvedPermissionCollection.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.security.UnresolvedPermissionCollection.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.security.UnresolvedPermissionCollection.<clinit>():void");
    }

    public UnresolvedPermissionCollection() {
        this.perms = new HashMap(11);
    }

    public void add(Permission permission) {
        if (permission instanceof UnresolvedPermission) {
            List<UnresolvedPermission> v;
            UnresolvedPermission up = (UnresolvedPermission) permission;
            synchronized (this) {
                v = (List) this.perms.get(up.getName());
                if (v == null) {
                    v = new ArrayList();
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

    List<UnresolvedPermission> getUnresolvedPermissions(Permission p) {
        List<UnresolvedPermission> list;
        synchronized (this) {
            list = (List) this.perms.get(p.getClass().getName());
        }
        return list;
    }

    public boolean implies(Permission permission) {
        return false;
    }

    public Enumeration<Permission> elements() {
        List<Permission> results = new ArrayList();
        synchronized (this) {
            for (List<UnresolvedPermission> l : this.perms.values()) {
                synchronized (l) {
                    results.addAll(l);
                }
            }
        }
        return Collections.enumeration(results);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        Object permissions = new Hashtable(this.perms.size() * 2);
        synchronized (this) {
            for (Entry<String, List<UnresolvedPermission>> e : this.perms.entrySet()) {
                List<UnresolvedPermission> list = (List) e.getValue();
                Vector<UnresolvedPermission> vec = new Vector(list.size());
                synchronized (list) {
                    vec.addAll(list);
                }
                permissions.put((String) e.getKey(), vec);
            }
        }
        out.putFields().put("permissions", permissions);
        out.writeFields();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Hashtable<String, Vector<UnresolvedPermission>> permissions = (Hashtable) in.readFields().get("permissions", null);
        this.perms = new HashMap(permissions.size() * 2);
        for (Entry<String, Vector<UnresolvedPermission>> e : permissions.entrySet()) {
            Vector<UnresolvedPermission> vec = (Vector) e.getValue();
            List<UnresolvedPermission> list = new ArrayList(vec.size());
            list.addAll(vec);
            this.perms.put((String) e.getKey(), list);
        }
    }
}
