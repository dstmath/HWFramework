package java.security;

public final class AccessController {
    private AccessController() {
    }

    public static <T> T doPrivileged(PrivilegedAction<T> action) {
        return action.run();
    }

    public static <T> T doPrivilegedWithCombiner(PrivilegedAction<T> action) {
        return action.run();
    }

    public static <T> T doPrivileged(PrivilegedAction<T> action, AccessControlContext context) {
        return action.run();
    }

    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e2) {
            throw new PrivilegedActionException(e2);
        }
    }

    public static <T> T doPrivilegedWithCombiner(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        return doPrivileged((PrivilegedExceptionAction) action);
    }

    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action, AccessControlContext context) throws PrivilegedActionException {
        return doPrivileged((PrivilegedExceptionAction) action);
    }

    public static AccessControlContext getContext() {
        return new AccessControlContext(null);
    }

    public static void checkPermission(Permission perm) throws AccessControlException {
    }
}
