package java.security;

import java.util.Enumeration;

@Deprecated
public abstract class IdentityScope extends Identity {
    private static IdentityScope scope = null;
    private static final long serialVersionUID = -2337346281189773310L;

    public abstract void addIdentity(Identity identity) throws KeyManagementException;

    public abstract Identity getIdentity(String str);

    public abstract Identity getIdentity(PublicKey publicKey);

    public abstract Enumeration<Identity> identities();

    public abstract void removeIdentity(Identity identity) throws KeyManagementException;

    public abstract int size();

    private static void initializeSystemScope() {
        String classname = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty("system.scope");
            }
        });
        if (classname != null) {
            try {
                scope = (IdentityScope) Class.forName(classname).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected IdentityScope() {
        this("restoring...");
    }

    public IdentityScope(String name) {
        super(name);
    }

    public IdentityScope(String name, IdentityScope scope) throws KeyManagementException {
        super(name, scope);
    }

    public static IdentityScope getSystemScope() {
        if (scope == null) {
            initializeSystemScope();
        }
        return scope;
    }

    protected static void setSystemScope(IdentityScope scope) {
        check("setSystemScope");
        scope = scope;
    }

    public Identity getIdentity(Principal principal) {
        return getIdentity(principal.getName());
    }

    public String toString() {
        return super.toString() + "[" + size() + "]";
    }

    private static void check(String directive) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSecurityAccess(directive);
        }
    }
}
