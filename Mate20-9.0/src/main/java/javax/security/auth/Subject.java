package javax.security.auth;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.DomainCombiner;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.text.MessageFormat;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import sun.security.util.ResourcesMgr;

public final class Subject implements Serializable {
    private static final ProtectionDomain[] NULL_PD_ARRAY = new ProtectionDomain[0];
    private static final int PRINCIPAL_SET = 1;
    private static final int PRIV_CREDENTIAL_SET = 3;
    private static final int PUB_CREDENTIAL_SET = 2;
    private static final long serialVersionUID = -8308522755600156056L;
    Set<Principal> principals;
    transient Set<Object> privCredentials;
    transient Set<Object> pubCredentials;
    private volatile boolean readOnly;

    static class AuthPermissionHolder {
        static final AuthPermission DO_AS_PERMISSION = new AuthPermission("doAs");
        static final AuthPermission DO_AS_PRIVILEGED_PERMISSION = new AuthPermission("doAsPrivileged");
        static final AuthPermission GET_SUBJECT_PERMISSION = new AuthPermission("getSubject");
        static final AuthPermission MODIFY_PRINCIPALS_PERMISSION = new AuthPermission("modifyPrincipals");
        static final AuthPermission MODIFY_PRIVATE_CREDENTIALS_PERMISSION = new AuthPermission("modifyPrivateCredentials");
        static final AuthPermission MODIFY_PUBLIC_CREDENTIALS_PERMISSION = new AuthPermission("modifyPublicCredentials");
        static final AuthPermission SET_READ_ONLY_PERMISSION = new AuthPermission("setReadOnly");

        AuthPermissionHolder() {
        }
    }

    private class ClassSet<T> extends AbstractSet<T> {
        private Class<T> c;
        private Set<T> set = new HashSet();
        private int which;

        ClassSet(int which2, Class<T> c2) {
            this.which = which2;
            this.c = c2;
            switch (which2) {
                case 1:
                    synchronized (Subject.this.principals) {
                        populateSet();
                    }
                    return;
                case 2:
                    synchronized (Subject.this.pubCredentials) {
                        populateSet();
                    }
                    return;
                default:
                    synchronized (Subject.this.privCredentials) {
                        populateSet();
                    }
                    return;
            }
        }

        private void populateSet() {
            final Iterator<Object> it;
            Object next;
            switch (this.which) {
                case 1:
                    it = Subject.this.principals.iterator();
                    break;
                case 2:
                    it = Subject.this.pubCredentials.iterator();
                    break;
                default:
                    it = Subject.this.privCredentials.iterator();
                    break;
            }
            while (it.hasNext()) {
                if (this.which == 3) {
                    next = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        public Object run() {
                            return it.next();
                        }
                    });
                } else {
                    next = it.next();
                }
                if (this.c.isAssignableFrom(next.getClass())) {
                    if (this.which != 3) {
                        this.set.add(next);
                    } else {
                        SecurityManager sm = System.getSecurityManager();
                        if (sm != null) {
                            sm.checkPermission(new PrivateCredentialPermission(next.getClass().getName(), Subject.this.getPrincipals()));
                        }
                        this.set.add(next);
                    }
                }
            }
        }

        public int size() {
            return this.set.size();
        }

        public Iterator<T> iterator() {
            return this.set.iterator();
        }

        public boolean add(T o) {
            if (o.getClass().isAssignableFrom(this.c)) {
                return this.set.add(o);
            }
            throw new SecurityException(new MessageFormat(ResourcesMgr.getString("attempting.to.add.an.object.which.is.not.an.instance.of.class")).format(new Object[]{this.c.toString()}));
        }
    }

    private static class SecureSet<E> extends AbstractSet<E> implements Serializable {
        private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("this$0", Subject.class), new ObjectStreamField("elements", LinkedList.class), new ObjectStreamField("which", Integer.TYPE)};
        private static final long serialVersionUID = 7911754171111800359L;
        LinkedList<E> elements;
        Subject subject;
        /* access modifiers changed from: private */
        public int which;

        SecureSet(Subject subject2, int which2) {
            this.subject = subject2;
            this.which = which2;
            this.elements = new LinkedList<>();
        }

        SecureSet(Subject subject2, int which2, Set<? extends E> set) {
            this.subject = subject2;
            this.which = which2;
            this.elements = new LinkedList<>(set);
        }

        public int size() {
            return this.elements.size();
        }

        public Iterator<E> iterator() {
            final LinkedList<E> list = this.elements;
            return new Iterator<E>() {
                ListIterator<E> i = list.listIterator(0);

                public boolean hasNext() {
                    return this.i.hasNext();
                }

                public E next() {
                    if (SecureSet.this.which != 3) {
                        return this.i.next();
                    }
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        try {
                            sm.checkPermission(new PrivateCredentialPermission(list.get(this.i.nextIndex()).getClass().getName(), SecureSet.this.subject.getPrincipals()));
                        } catch (SecurityException se) {
                            this.i.next();
                            throw se;
                        }
                    }
                    return this.i.next();
                }

                public void remove() {
                    if (!SecureSet.this.subject.isReadOnly()) {
                        SecurityManager sm = System.getSecurityManager();
                        if (sm != null) {
                            switch (SecureSet.this.which) {
                                case 1:
                                    sm.checkPermission(AuthPermissionHolder.MODIFY_PRINCIPALS_PERMISSION);
                                    break;
                                case 2:
                                    sm.checkPermission(AuthPermissionHolder.MODIFY_PUBLIC_CREDENTIALS_PERMISSION);
                                    break;
                                default:
                                    sm.checkPermission(AuthPermissionHolder.MODIFY_PRIVATE_CREDENTIALS_PERMISSION);
                                    break;
                            }
                        }
                        this.i.remove();
                        return;
                    }
                    throw new IllegalStateException(ResourcesMgr.getString("Subject.is.read.only"));
                }
            };
        }

        public boolean add(E o) {
            if (!this.subject.isReadOnly()) {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    switch (this.which) {
                        case 1:
                            sm.checkPermission(AuthPermissionHolder.MODIFY_PRINCIPALS_PERMISSION);
                            break;
                        case 2:
                            sm.checkPermission(AuthPermissionHolder.MODIFY_PUBLIC_CREDENTIALS_PERMISSION);
                            break;
                        default:
                            sm.checkPermission(AuthPermissionHolder.MODIFY_PRIVATE_CREDENTIALS_PERMISSION);
                            break;
                    }
                }
                if (this.which == 1 && !(o instanceof Principal)) {
                    throw new SecurityException(ResourcesMgr.getString("attempting.to.add.an.object.which.is.not.an.instance.of.java.security.Principal.to.a.Subject.s.Principal.Set"));
                } else if (!this.elements.contains(o)) {
                    return this.elements.add(o);
                } else {
                    return false;
                }
            } else {
                throw new IllegalStateException(ResourcesMgr.getString("Subject.is.read.only"));
            }
        }

        public boolean remove(Object o) {
            E next;
            final Iterator<E> e = iterator();
            while (e.hasNext()) {
                if (this.which != 3) {
                    next = e.next();
                } else {
                    next = AccessController.doPrivileged(new PrivilegedAction<E>() {
                        public E run() {
                            return e.next();
                        }
                    });
                }
                if (next == null) {
                    if (o == null) {
                        e.remove();
                        return true;
                    }
                } else if (next.equals(o)) {
                    e.remove();
                    return true;
                }
            }
            return false;
        }

        public boolean contains(Object o) {
            E next;
            final Iterator<E> e = iterator();
            while (e.hasNext()) {
                if (this.which != 3) {
                    next = e.next();
                } else {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkPermission(new PrivateCredentialPermission(o.getClass().getName(), this.subject.getPrincipals()));
                    }
                    next = AccessController.doPrivileged(new PrivilegedAction<E>() {
                        public E run() {
                            return e.next();
                        }
                    });
                }
                if (next == null) {
                    if (o == null) {
                        return true;
                    }
                } else if (next.equals(o)) {
                    return true;
                }
            }
            return false;
        }

        public boolean removeAll(Collection<?> c) {
            E next;
            Objects.requireNonNull(c);
            boolean modified = false;
            final Iterator<E> e = iterator();
            while (e.hasNext()) {
                if (this.which != 3) {
                    next = e.next();
                } else {
                    next = AccessController.doPrivileged(new PrivilegedAction<E>() {
                        public E run() {
                            return e.next();
                        }
                    });
                }
                Iterator<?> ce = c.iterator();
                while (true) {
                    if (!ce.hasNext()) {
                        break;
                    }
                    Object o = ce.next();
                    if (next == null) {
                        if (o == null) {
                            e.remove();
                            modified = true;
                            break;
                        }
                    } else if (next.equals(o)) {
                        e.remove();
                        modified = true;
                        break;
                    }
                }
            }
            return modified;
        }

        public boolean retainAll(Collection<?> c) {
            E next;
            Objects.requireNonNull(c);
            boolean modified = false;
            final Iterator<E> e = iterator();
            while (e.hasNext()) {
                boolean retain = false;
                if (this.which != 3) {
                    next = e.next();
                } else {
                    next = AccessController.doPrivileged(new PrivilegedAction<E>() {
                        public E run() {
                            return e.next();
                        }
                    });
                }
                Iterator<?> ce = c.iterator();
                while (true) {
                    if (!ce.hasNext()) {
                        break;
                    }
                    Object o = ce.next();
                    if (next == null) {
                        if (o == null) {
                            retain = true;
                            break;
                        }
                    } else if (next.equals(o)) {
                        retain = true;
                        break;
                    }
                }
                if (!retain) {
                    e.remove();
                    modified = true;
                }
            }
            return modified;
        }

        public void clear() {
            final Iterator<E> e = iterator();
            while (e.hasNext()) {
                if (this.which != 3) {
                    E next = e.next();
                } else {
                    Object doPrivileged = AccessController.doPrivileged(new PrivilegedAction<E>() {
                        public E run() {
                            return e.next();
                        }
                    });
                }
                e.remove();
            }
        }

        private void writeObject(ObjectOutputStream oos) throws IOException {
            if (this.which == 3) {
                Iterator<E> i = iterator();
                while (i.hasNext()) {
                    i.next();
                }
            }
            ObjectOutputStream.PutField fields = oos.putFields();
            fields.put("this$0", (Object) this.subject);
            fields.put("elements", (Object) this.elements);
            fields.put("which", this.which);
            oos.writeFields();
        }

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            ObjectInputStream.GetField fields = ois.readFields();
            this.subject = (Subject) fields.get("this$0", (Object) null);
            this.which = fields.get("which", 0);
            LinkedList<E> tmp = (LinkedList) fields.get("elements", (Object) null);
            if (tmp.getClass() != LinkedList.class) {
                this.elements = new LinkedList<>(tmp);
            } else {
                this.elements = tmp;
            }
        }
    }

    public Subject() {
        this.readOnly = false;
        this.principals = Collections.synchronizedSet(new SecureSet(this, 1));
        this.pubCredentials = Collections.synchronizedSet(new SecureSet(this, 2));
        this.privCredentials = Collections.synchronizedSet(new SecureSet(this, 3));
    }

    public Subject(boolean readOnly2, Set<? extends Principal> principals2, Set<?> pubCredentials2, Set<?> privCredentials2) {
        this.readOnly = false;
        if (principals2 == null || pubCredentials2 == null || privCredentials2 == null) {
            throw new NullPointerException(ResourcesMgr.getString("invalid.null.input.s."));
        }
        this.principals = Collections.synchronizedSet(new SecureSet(this, 1, principals2));
        this.pubCredentials = Collections.synchronizedSet(new SecureSet(this, 2, pubCredentials2));
        this.privCredentials = Collections.synchronizedSet(new SecureSet(this, 3, privCredentials2));
        this.readOnly = readOnly2;
    }

    public void setReadOnly() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.SET_READ_ONLY_PERMISSION);
        }
        this.readOnly = true;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public static Subject getSubject(final AccessControlContext acc) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.GET_SUBJECT_PERMISSION);
        }
        if (acc != null) {
            return (Subject) AccessController.doPrivileged(new PrivilegedAction<Subject>() {
                public Subject run() {
                    DomainCombiner dc = AccessControlContext.this.getDomainCombiner();
                    if (!(dc instanceof SubjectDomainCombiner)) {
                        return null;
                    }
                    return ((SubjectDomainCombiner) dc).getSubject();
                }
            });
        }
        throw new NullPointerException(ResourcesMgr.getString("invalid.null.AccessControlContext.provided"));
    }

    public static <T> T doAs(Subject subject, PrivilegedAction<T> action) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.DO_AS_PERMISSION);
        }
        if (action != null) {
            return AccessController.doPrivileged(action, createContext(subject, AccessController.getContext()));
        }
        throw new NullPointerException(ResourcesMgr.getString("invalid.null.action.provided"));
    }

    public static <T> T doAs(Subject subject, PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.DO_AS_PERMISSION);
        }
        if (action != null) {
            return AccessController.doPrivileged(action, createContext(subject, AccessController.getContext()));
        }
        throw new NullPointerException(ResourcesMgr.getString("invalid.null.action.provided"));
    }

    public static <T> T doAsPrivileged(Subject subject, PrivilegedAction<T> action, AccessControlContext acc) {
        AccessControlContext callerAcc;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.DO_AS_PRIVILEGED_PERMISSION);
        }
        if (action != null) {
            if (acc == null) {
                callerAcc = new AccessControlContext(NULL_PD_ARRAY);
            } else {
                callerAcc = acc;
            }
            return AccessController.doPrivileged(action, createContext(subject, callerAcc));
        }
        throw new NullPointerException(ResourcesMgr.getString("invalid.null.action.provided"));
    }

    public static <T> T doAsPrivileged(Subject subject, PrivilegedExceptionAction<T> action, AccessControlContext acc) throws PrivilegedActionException {
        AccessControlContext callerAcc;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.DO_AS_PRIVILEGED_PERMISSION);
        }
        if (action != null) {
            if (acc == null) {
                callerAcc = new AccessControlContext(NULL_PD_ARRAY);
            } else {
                callerAcc = acc;
            }
            return AccessController.doPrivileged(action, createContext(subject, callerAcc));
        }
        throw new NullPointerException(ResourcesMgr.getString("invalid.null.action.provided"));
    }

    private static AccessControlContext createContext(Subject subject, final AccessControlContext acc) {
        return (AccessControlContext) AccessController.doPrivileged(new PrivilegedAction<AccessControlContext>() {
            public AccessControlContext run() {
                if (Subject.this == null) {
                    return new AccessControlContext(acc, null);
                }
                return new AccessControlContext(acc, new SubjectDomainCombiner(Subject.this));
            }
        });
    }

    public Set<Principal> getPrincipals() {
        return this.principals;
    }

    public <T extends Principal> Set<T> getPrincipals(Class<T> c) {
        if (c != null) {
            return new ClassSet(1, c);
        }
        throw new NullPointerException(ResourcesMgr.getString("invalid.null.Class.provided"));
    }

    public Set<Object> getPublicCredentials() {
        return this.pubCredentials;
    }

    public Set<Object> getPrivateCredentials() {
        return this.privCredentials;
    }

    public <T> Set<T> getPublicCredentials(Class<T> c) {
        if (c != null) {
            return new ClassSet(2, c);
        }
        throw new NullPointerException(ResourcesMgr.getString("invalid.null.Class.provided"));
    }

    public <T> Set<T> getPrivateCredentials(Class<T> c) {
        if (c != null) {
            return new ClassSet(3, c);
        }
        throw new NullPointerException(ResourcesMgr.getString("invalid.null.Class.provided"));
    }

    public boolean equals(Object o) {
        Set<Principal> thatPrincipals;
        Set<Object> thatPubCredentials;
        Set<Object> thatPrivCredentials;
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof Subject)) {
            return false;
        }
        Subject that = (Subject) o;
        synchronized (that.principals) {
            thatPrincipals = new HashSet<>((Collection<? extends Principal>) that.principals);
        }
        if (!this.principals.equals(thatPrincipals)) {
            return false;
        }
        synchronized (that.pubCredentials) {
            thatPubCredentials = new HashSet<>((Collection<? extends Object>) that.pubCredentials);
        }
        if (!this.pubCredentials.equals(thatPubCredentials)) {
            return false;
        }
        synchronized (that.privCredentials) {
            thatPrivCredentials = new HashSet<>((Collection<? extends Object>) that.privCredentials);
        }
        if (!this.privCredentials.equals(thatPrivCredentials)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return toString(true);
    }

    /* access modifiers changed from: package-private */
    public String toString(boolean includePrivateCredentials) {
        String s = ResourcesMgr.getString("Subject.");
        String suffix = "";
        synchronized (this.principals) {
            while (this.principals.iterator().hasNext()) {
                suffix = suffix + ResourcesMgr.getString(".Principal.") + pI.next().toString() + ResourcesMgr.getString("NEWLINE");
            }
        }
        synchronized (this.pubCredentials) {
            for (Object o : this.pubCredentials) {
                suffix = suffix + ResourcesMgr.getString(".Public.Credential.") + o.toString() + ResourcesMgr.getString("NEWLINE");
            }
        }
        if (includePrivateCredentials) {
            synchronized (this.privCredentials) {
                for (Object o2 : this.privCredentials) {
                    try {
                        suffix = suffix + ResourcesMgr.getString(".Private.Credential.") + o2.toString() + ResourcesMgr.getString("NEWLINE");
                    } catch (SecurityException e) {
                        suffix = suffix + ResourcesMgr.getString(".Private.Credential.inaccessible.");
                    }
                }
            }
        }
        return s + suffix;
    }

    public int hashCode() {
        int hashCode = 0;
        synchronized (this.principals) {
            for (Principal p : this.principals) {
                hashCode ^= p.hashCode();
            }
        }
        synchronized (this.pubCredentials) {
            for (Object credHashCode : this.pubCredentials) {
                hashCode ^= getCredHashCode(credHashCode);
            }
        }
        return hashCode;
    }

    private int getCredHashCode(Object o) {
        try {
            return o.hashCode();
        } catch (IllegalStateException e) {
            return o.getClass().toString().hashCode();
        }
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        synchronized (this.principals) {
            oos.defaultWriteObject();
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField gf = s.readFields();
        this.readOnly = gf.get("readOnly", false);
        Set<Principal> inputPrincs = (Set) gf.get("principals", (Object) null);
        if (inputPrincs != null) {
            try {
                this.principals = Collections.synchronizedSet(new SecureSet(this, 1, inputPrincs));
            } catch (NullPointerException e) {
                this.principals = Collections.synchronizedSet(new SecureSet(this, 1));
            }
            this.pubCredentials = Collections.synchronizedSet(new SecureSet(this, 2));
            this.privCredentials = Collections.synchronizedSet(new SecureSet(this, 3));
            return;
        }
        throw new NullPointerException(ResourcesMgr.getString("invalid.null.input.s."));
    }
}
