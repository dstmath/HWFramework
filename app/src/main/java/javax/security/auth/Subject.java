package javax.security.auth;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
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
import java.util.Set;
import sun.security.util.ResourcesMgr;

public final class Subject implements Serializable {
    private static final ProtectionDomain[] NULL_PD_ARRAY = null;
    private static final int PRINCIPAL_SET = 1;
    private static final int PRIV_CREDENTIAL_SET = 3;
    private static final int PUB_CREDENTIAL_SET = 2;
    private static final long serialVersionUID = -8308522755600156056L;
    Set<Principal> principals;
    transient Set<Object> privCredentials;
    transient Set<Object> pubCredentials;
    private volatile boolean readOnly;

    /* renamed from: javax.security.auth.Subject.1 */
    static class AnonymousClass1 implements PrivilegedAction<Subject> {
        final /* synthetic */ AccessControlContext val$acc;

        AnonymousClass1(AccessControlContext val$acc) {
            this.val$acc = val$acc;
        }

        public Subject run() {
            DomainCombiner dc = this.val$acc.getDomainCombiner();
            if (dc instanceof SubjectDomainCombiner) {
                return ((SubjectDomainCombiner) dc).getSubject();
            }
            return null;
        }
    }

    /* renamed from: javax.security.auth.Subject.2 */
    static class AnonymousClass2 implements PrivilegedAction<AccessControlContext> {
        final /* synthetic */ AccessControlContext val$acc;
        final /* synthetic */ Subject val$subject;

        AnonymousClass2(Subject val$subject, AccessControlContext val$acc) {
            this.val$subject = val$subject;
            this.val$acc = val$acc;
        }

        public AccessControlContext run() {
            if (this.val$subject == null) {
                return new AccessControlContext(this.val$acc, null);
            }
            return new AccessControlContext(this.val$acc, new SubjectDomainCombiner(this.val$subject));
        }
    }

    static class AuthPermissionHolder {
        static final AuthPermission DO_AS_PERMISSION = null;
        static final AuthPermission DO_AS_PRIVILEGED_PERMISSION = null;
        static final AuthPermission GET_SUBJECT_PERMISSION = null;
        static final AuthPermission MODIFY_PRINCIPALS_PERMISSION = null;
        static final AuthPermission MODIFY_PRIVATE_CREDENTIALS_PERMISSION = null;
        static final AuthPermission MODIFY_PUBLIC_CREDENTIALS_PERMISSION = null;
        static final AuthPermission SET_READ_ONLY_PERMISSION = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.security.auth.Subject.AuthPermissionHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.security.auth.Subject.AuthPermissionHolder.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: javax.security.auth.Subject.AuthPermissionHolder.<clinit>():void");
        }

        AuthPermissionHolder() {
        }
    }

    private class ClassSet<T> extends AbstractSet<T> {
        private Class<T> c;
        private Set<T> set;
        final /* synthetic */ Subject this$0;
        private int which;

        /* renamed from: javax.security.auth.Subject.ClassSet.1 */
        class AnonymousClass1 implements PrivilegedAction<Object> {
            final /* synthetic */ ClassSet this$1;
            final /* synthetic */ Iterator val$iterator;

            AnonymousClass1(ClassSet this$1, Iterator val$iterator) {
                this.this$1 = this$1;
                this.val$iterator = val$iterator;
            }

            public Object run() {
                return this.val$iterator.next();
            }
        }

        ClassSet(Subject this$0, int which, Class<T> c) {
            this.this$0 = this$0;
            this.which = which;
            this.c = c;
            this.set = new HashSet();
            Set set;
            switch (which) {
                case Subject.PRINCIPAL_SET /*1*/:
                    set = this$0.principals;
                    synchronized (set) {
                        break;
                    }
                    populateSet();
                    break;
                case Subject.PUB_CREDENTIAL_SET /*2*/:
                    set = this$0.pubCredentials;
                    synchronized (set) {
                        break;
                    }
                    populateSet();
                    break;
                default:
                    set = this$0.privCredentials;
                    synchronized (set) {
                        break;
                    }
                    populateSet();
                    break;
            }
        }

        private void populateSet() {
            Iterator<?> iterator;
            switch (this.which) {
                case Subject.PRINCIPAL_SET /*1*/:
                    iterator = this.this$0.principals.iterator();
                    break;
                case Subject.PUB_CREDENTIAL_SET /*2*/:
                    iterator = this.this$0.pubCredentials.iterator();
                    break;
                default:
                    iterator = this.this$0.privCredentials.iterator();
                    break;
            }
            while (iterator.hasNext()) {
                Object next;
                if (this.which == Subject.PRIV_CREDENTIAL_SET) {
                    next = AccessController.doPrivileged(new AnonymousClass1(this, iterator));
                } else {
                    next = iterator.next();
                }
                if (this.c.isAssignableFrom(next.getClass())) {
                    if (this.which != Subject.PRIV_CREDENTIAL_SET) {
                        this.set.add(next);
                    } else {
                        SecurityManager sm = System.getSecurityManager();
                        if (sm != null) {
                            sm.checkPermission(new PrivateCredentialPermission(next.getClass().getName(), this.this$0.getPrincipals()));
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
            MessageFormat form = new MessageFormat(ResourcesMgr.getString("attempting.to.add.an.object.which.is.not.an.instance.of.class"));
            Object[] source = new Object[Subject.PRINCIPAL_SET];
            source[0] = this.c.toString();
            throw new SecurityException(form.format(source));
        }
    }

    private static class SecureSet<E> extends AbstractSet<E> implements Serializable {
        private static final ObjectStreamField[] serialPersistentFields = null;
        private static final long serialVersionUID = 7911754171111800359L;
        LinkedList<E> elements;
        Subject subject;
        private int which;

        /* renamed from: javax.security.auth.Subject.SecureSet.1 */
        class AnonymousClass1 implements Iterator<E> {
            ListIterator<E> i;
            final /* synthetic */ SecureSet this$1;
            final /* synthetic */ LinkedList val$list;

            AnonymousClass1(SecureSet this$1, LinkedList val$list) {
                this.this$1 = this$1;
                this.val$list = val$list;
                this.i = this.val$list.listIterator(0);
            }

            public boolean hasNext() {
                return this.i.hasNext();
            }

            public E next() {
                if (this.this$1.which != Subject.PRIV_CREDENTIAL_SET) {
                    return this.i.next();
                }
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    try {
                        sm.checkPermission(new PrivateCredentialPermission(this.val$list.get(this.i.nextIndex()).getClass().getName(), this.this$1.subject.getPrincipals()));
                    } catch (SecurityException se) {
                        this.i.next();
                        throw se;
                    }
                }
                return this.i.next();
            }

            public void remove() {
                if (this.this$1.subject.isReadOnly()) {
                    throw new IllegalStateException(ResourcesMgr.getString("Subject.is.read.only"));
                }
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    switch (this.this$1.which) {
                        case Subject.PRINCIPAL_SET /*1*/:
                            sm.checkPermission(AuthPermissionHolder.MODIFY_PRINCIPALS_PERMISSION);
                            break;
                        case Subject.PUB_CREDENTIAL_SET /*2*/:
                            sm.checkPermission(AuthPermissionHolder.MODIFY_PUBLIC_CREDENTIALS_PERMISSION);
                            break;
                        default:
                            sm.checkPermission(AuthPermissionHolder.MODIFY_PRIVATE_CREDENTIALS_PERMISSION);
                            break;
                    }
                }
                this.i.remove();
            }
        }

        /* renamed from: javax.security.auth.Subject.SecureSet.2 */
        class AnonymousClass2 implements PrivilegedAction<E> {
            final /* synthetic */ SecureSet this$1;
            final /* synthetic */ Iterator val$e;

            AnonymousClass2(SecureSet this$1, Iterator val$e) {
                this.this$1 = this$1;
                this.val$e = val$e;
            }

            public E run() {
                return this.val$e.next();
            }
        }

        /* renamed from: javax.security.auth.Subject.SecureSet.3 */
        class AnonymousClass3 implements PrivilegedAction<E> {
            final /* synthetic */ SecureSet this$1;
            final /* synthetic */ Iterator val$e;

            AnonymousClass3(SecureSet this$1, Iterator val$e) {
                this.this$1 = this$1;
                this.val$e = val$e;
            }

            public E run() {
                return this.val$e.next();
            }
        }

        /* renamed from: javax.security.auth.Subject.SecureSet.4 */
        class AnonymousClass4 implements PrivilegedAction<E> {
            final /* synthetic */ SecureSet this$1;
            final /* synthetic */ Iterator val$e;

            AnonymousClass4(SecureSet this$1, Iterator val$e) {
                this.this$1 = this$1;
                this.val$e = val$e;
            }

            public E run() {
                return this.val$e.next();
            }
        }

        /* renamed from: javax.security.auth.Subject.SecureSet.5 */
        class AnonymousClass5 implements PrivilegedAction<E> {
            final /* synthetic */ SecureSet this$1;
            final /* synthetic */ Iterator val$e;

            AnonymousClass5(SecureSet this$1, Iterator val$e) {
                this.this$1 = this$1;
                this.val$e = val$e;
            }

            public E run() {
                return this.val$e.next();
            }
        }

        /* renamed from: javax.security.auth.Subject.SecureSet.6 */
        class AnonymousClass6 implements PrivilegedAction<E> {
            final /* synthetic */ SecureSet this$1;
            final /* synthetic */ Iterator val$e;

            AnonymousClass6(SecureSet this$1, Iterator val$e) {
                this.this$1 = this$1;
                this.val$e = val$e;
            }

            public E run() {
                return this.val$e.next();
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.security.auth.Subject.SecureSet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.security.auth.Subject.SecureSet.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: javax.security.auth.Subject.SecureSet.<clinit>():void");
        }

        SecureSet(Subject subject, int which) {
            this.subject = subject;
            this.which = which;
            this.elements = new LinkedList();
        }

        SecureSet(Subject subject, int which, Set<? extends E> set) {
            this.subject = subject;
            this.which = which;
            this.elements = new LinkedList(set);
        }

        public int size() {
            return this.elements.size();
        }

        public Iterator<E> iterator() {
            return new AnonymousClass1(this, this.elements);
        }

        public boolean add(E o) {
            if (this.subject.isReadOnly()) {
                throw new IllegalStateException(ResourcesMgr.getString("Subject.is.read.only"));
            }
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                switch (this.which) {
                    case Subject.PRINCIPAL_SET /*1*/:
                        sm.checkPermission(AuthPermissionHolder.MODIFY_PRINCIPALS_PERMISSION);
                        break;
                    case Subject.PUB_CREDENTIAL_SET /*2*/:
                        sm.checkPermission(AuthPermissionHolder.MODIFY_PUBLIC_CREDENTIALS_PERMISSION);
                        break;
                    default:
                        sm.checkPermission(AuthPermissionHolder.MODIFY_PRIVATE_CREDENTIALS_PERMISSION);
                        break;
                }
            }
            switch (this.which) {
                case Subject.PRINCIPAL_SET /*1*/:
                    if (!(o instanceof Principal)) {
                        throw new SecurityException(ResourcesMgr.getString("attempting.to.add.an.object.which.is.not.an.instance.of.java.security.Principal.to.a.Subject.s.Principal.Set"));
                    }
                    break;
            }
            if (this.elements.contains(o)) {
                return false;
            }
            return this.elements.add(o);
        }

        public boolean remove(Object o) {
            Iterator<E> e = iterator();
            while (e.hasNext()) {
                E next;
                if (this.which != Subject.PRIV_CREDENTIAL_SET) {
                    next = e.next();
                } else {
                    next = AccessController.doPrivileged(new AnonymousClass2(this, e));
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
            Iterator<E> e = iterator();
            while (e.hasNext()) {
                E next;
                if (this.which != Subject.PRIV_CREDENTIAL_SET) {
                    next = e.next();
                } else {
                    SecurityManager sm = System.getSecurityManager();
                    if (sm != null) {
                        sm.checkPermission(new PrivateCredentialPermission(o.getClass().getName(), this.subject.getPrincipals()));
                    }
                    next = AccessController.doPrivileged(new AnonymousClass3(this, e));
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
            boolean modified = false;
            Iterator<E> e = iterator();
            while (e.hasNext()) {
                E next;
                if (this.which != Subject.PRIV_CREDENTIAL_SET) {
                    next = e.next();
                } else {
                    next = AccessController.doPrivileged(new AnonymousClass4(this, e));
                }
                for (Object o : c) {
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
            boolean modified = false;
            Iterator<E> e = iterator();
            while (e.hasNext()) {
                E next;
                boolean retain = false;
                if (this.which != Subject.PRIV_CREDENTIAL_SET) {
                    next = e.next();
                } else {
                    next = AccessController.doPrivileged(new AnonymousClass5(this, e));
                }
                for (Object o : c) {
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
            Iterator<E> e = iterator();
            while (e.hasNext()) {
                if (this.which != Subject.PRIV_CREDENTIAL_SET) {
                    E next = e.next();
                } else {
                    Object doPrivileged = AccessController.doPrivileged(new AnonymousClass6(this, e));
                }
                e.remove();
            }
        }

        private void writeObject(ObjectOutputStream oos) throws IOException {
            if (this.which == Subject.PRIV_CREDENTIAL_SET) {
                Iterator<E> i = iterator();
                while (i.hasNext()) {
                    i.next();
                }
            }
            PutField fields = oos.putFields();
            fields.put("this$0", this.subject);
            fields.put("elements", this.elements);
            fields.put("which", this.which);
            oos.writeFields();
        }

        private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            GetField fields = ois.readFields();
            this.subject = (Subject) fields.get("this$0", null);
            this.elements = (LinkedList) fields.get("elements", null);
            this.which = fields.get("which", 0);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.security.auth.Subject.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.security.auth.Subject.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: javax.security.auth.Subject.<clinit>():void");
    }

    public Subject() {
        this.readOnly = false;
        this.principals = Collections.synchronizedSet(new SecureSet(this, PRINCIPAL_SET));
        this.pubCredentials = Collections.synchronizedSet(new SecureSet(this, PUB_CREDENTIAL_SET));
        this.privCredentials = Collections.synchronizedSet(new SecureSet(this, PRIV_CREDENTIAL_SET));
    }

    public Subject(boolean readOnly, Set<? extends Principal> principals, Set<?> pubCredentials, Set<?> privCredentials) {
        this.readOnly = false;
        if (principals == null || pubCredentials == null || privCredentials == null) {
            throw new NullPointerException(ResourcesMgr.getString("invalid.null.input.s."));
        }
        this.principals = Collections.synchronizedSet(new SecureSet(this, PRINCIPAL_SET, principals));
        this.pubCredentials = Collections.synchronizedSet(new SecureSet(this, PUB_CREDENTIAL_SET, pubCredentials));
        this.privCredentials = Collections.synchronizedSet(new SecureSet(this, PRIV_CREDENTIAL_SET, privCredentials));
        this.readOnly = readOnly;
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

    public static Subject getSubject(AccessControlContext acc) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.GET_SUBJECT_PERMISSION);
        }
        if (acc != null) {
            return (Subject) AccessController.doPrivileged(new AnonymousClass1(acc));
        }
        throw new NullPointerException(ResourcesMgr.getString("invalid.null.AccessControlContext.provided"));
    }

    public static <T> T doAs(Subject subject, PrivilegedAction<T> action) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.DO_AS_PERMISSION);
        }
        if (action != null) {
            return AccessController.doPrivileged((PrivilegedAction) action, createContext(subject, AccessController.getContext()));
        }
        throw new NullPointerException(ResourcesMgr.getString("invalid.null.action.provided"));
    }

    public static <T> T doAs(Subject subject, PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.DO_AS_PERMISSION);
        }
        if (action != null) {
            return AccessController.doPrivileged((PrivilegedExceptionAction) action, createContext(subject, AccessController.getContext()));
        }
        throw new NullPointerException(ResourcesMgr.getString("invalid.null.action.provided"));
    }

    public static <T> T doAsPrivileged(Subject subject, PrivilegedAction<T> action, AccessControlContext acc) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.DO_AS_PRIVILEGED_PERMISSION);
        }
        if (action == null) {
            throw new NullPointerException(ResourcesMgr.getString("invalid.null.action.provided"));
        }
        AccessControlContext callerAcc;
        if (acc == null) {
            callerAcc = new AccessControlContext(NULL_PD_ARRAY);
        } else {
            callerAcc = acc;
        }
        return AccessController.doPrivileged((PrivilegedAction) action, createContext(subject, callerAcc));
    }

    public static <T> T doAsPrivileged(Subject subject, PrivilegedExceptionAction<T> action, AccessControlContext acc) throws PrivilegedActionException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(AuthPermissionHolder.DO_AS_PRIVILEGED_PERMISSION);
        }
        if (action == null) {
            throw new NullPointerException(ResourcesMgr.getString("invalid.null.action.provided"));
        }
        AccessControlContext callerAcc;
        if (acc == null) {
            callerAcc = new AccessControlContext(NULL_PD_ARRAY);
        } else {
            callerAcc = acc;
        }
        return AccessController.doPrivileged((PrivilegedExceptionAction) action, createContext(subject, callerAcc));
    }

    private static AccessControlContext createContext(Subject subject, AccessControlContext acc) {
        return (AccessControlContext) AccessController.doPrivileged(new AnonymousClass2(subject, acc));
    }

    public Set<Principal> getPrincipals() {
        return this.principals;
    }

    public <T extends Principal> Set<T> getPrincipals(Class<T> c) {
        if (c != null) {
            return new ClassSet(this, PRINCIPAL_SET, c);
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
            return new ClassSet(this, PUB_CREDENTIAL_SET, c);
        }
        throw new NullPointerException(ResourcesMgr.getString("invalid.null.Class.provided"));
    }

    public <T> Set<T> getPrivateCredentials(Class<T> c) {
        if (c != null) {
            return new ClassSet(this, PRIV_CREDENTIAL_SET, c);
        }
        throw new NullPointerException(ResourcesMgr.getString("invalid.null.Class.provided"));
    }

    public boolean equals(Object o) {
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
            Set<Principal> thatPrincipals = new HashSet(that.principals);
        }
        if (!this.principals.equals(thatPrincipals)) {
            return false;
        }
        synchronized (that.pubCredentials) {
            Set<Object> thatPubCredentials = new HashSet(that.pubCredentials);
        }
        if (!this.pubCredentials.equals(thatPubCredentials)) {
            return false;
        }
        Set<Object> thatPrivCredentials;
        synchronized (that.privCredentials) {
            thatPrivCredentials = new HashSet(that.privCredentials);
        }
        return this.privCredentials.equals(thatPrivCredentials);
    }

    public String toString() {
        return toString(true);
    }

    String toString(boolean includePrivateCredentials) {
        String s = ResourcesMgr.getString("Subject.");
        String suffix = "";
        synchronized (this.principals) {
            for (Principal p : this.principals) {
                suffix = suffix + ResourcesMgr.getString(".Principal.") + p.toString() + ResourcesMgr.getString("NEWLINE");
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
        s.defaultReadObject();
        this.pubCredentials = Collections.synchronizedSet(new SecureSet(this, PUB_CREDENTIAL_SET));
        this.privCredentials = Collections.synchronizedSet(new SecureSet(this, PRIV_CREDENTIAL_SET));
    }
}
