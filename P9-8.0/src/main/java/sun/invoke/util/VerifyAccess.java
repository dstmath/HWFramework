package sun.invoke.util;

import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;

public class VerifyAccess {
    static final /* synthetic */ boolean -assertionsDisabled = (VerifyAccess.class.desiredAssertionStatus() ^ 1);
    private static final boolean ALLOW_NESTMATE_ACCESS = false;
    private static final int ALL_ACCESS_MODES = 7;
    private static final int PACKAGE_ALLOWED = 8;
    private static final int PACKAGE_ONLY = 0;
    private static final int PROTECTED_OR_PACKAGE_ALLOWED = 12;

    private VerifyAccess() {
    }

    public static boolean isMemberAccessible(Class<?> refc, Class<?> defc, int mods, Class<?> lookupClass, int allowedModes) {
        boolean z = false;
        if (allowedModes == 0) {
            return false;
        }
        if (!-assertionsDisabled && ((allowedModes & 1) == 0 || (allowedModes & -16) != 0)) {
            throw new AssertionError();
        } else if (!isClassAccessible(refc, lookupClass, allowedModes)) {
            return false;
        } else {
            if (defc == lookupClass && (allowedModes & 2) != 0) {
                return true;
            }
            switch (mods & 7) {
                case 0:
                    if (-assertionsDisabled || !defc.isInterface()) {
                        if ((allowedModes & 8) != 0) {
                            z = isSamePackage(defc, lookupClass);
                        }
                        return z;
                    }
                    throw new AssertionError();
                case 1:
                    return true;
                case 2:
                    return false;
                case 4:
                    if (!-assertionsDisabled && defc.isInterface()) {
                        throw new AssertionError();
                    } else if ((allowedModes & 12) != 0 && isSamePackage(defc, lookupClass)) {
                        return true;
                    } else {
                        if ((allowedModes & 4) == 0) {
                            return false;
                        }
                        return ((mods & 8) == 0 || (isRelatedClass(refc, lookupClass) ^ 1) == 0) && (allowedModes & 4) != 0 && isSubClass(lookupClass, defc);
                    }
                default:
                    throw new IllegalArgumentException("bad modifiers: " + Modifier.toString(mods));
            }
        }
    }

    static boolean isRelatedClass(Class<?> refc, Class<?> lookupClass) {
        if (refc == lookupClass || isSubClass(refc, lookupClass)) {
            return true;
        }
        return isSubClass(lookupClass, refc);
    }

    static boolean isSubClass(Class<?> lookupClass, Class<?> defc) {
        if (defc.isAssignableFrom(lookupClass)) {
            return lookupClass.isInterface() ^ 1;
        }
        return false;
    }

    public static boolean isClassAccessible(Class<?> refc, Class<?> lookupClass, int allowedModes) {
        if (allowedModes == 0) {
            return false;
        }
        if (Modifier.isPublic(refc.getModifiers())) {
            return true;
        }
        return (allowedModes & 8) != 0 && isSamePackage(lookupClass, refc);
    }

    public static boolean isTypeVisible(Class<?> type, Class<?> refc) {
        boolean z = true;
        if (type == refc) {
            return true;
        }
        while (type.isArray()) {
            type = type.getComponentType();
        }
        if (type.isPrimitive() || type == Object.class) {
            return true;
        }
        ClassLoader parent = type.getClassLoader();
        if (parent == null) {
            return true;
        }
        ClassLoader child = refc.getClassLoader();
        if (child == null) {
            return false;
        }
        if (parent == child || loadersAreRelated(parent, child, true)) {
            return true;
        }
        try {
            if (type != child.loadClass(type.getName())) {
                z = false;
            }
            return z;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isTypeVisible(MethodType type, Class<?> refc) {
        int n = -1;
        int max = type.parameterCount();
        while (n < max) {
            if (!isTypeVisible(n < 0 ? type.returnType() : type.parameterType(n), (Class) refc)) {
                return false;
            }
            n++;
        }
        return true;
    }

    public static boolean isSamePackage(Class<?> class1, Class<?> class2) {
        if (class1.isArray() || class2.isArray()) {
            throw new IllegalArgumentException();
        } else if (class1 == class2) {
            return true;
        } else {
            if (class1.getClassLoader() != class2.getClassLoader()) {
                return false;
            }
            String name1 = class1.getName();
            String name2 = class2.getName();
            int dot = name1.lastIndexOf(46);
            if (dot != name2.lastIndexOf(46)) {
                return false;
            }
            for (int i = 0; i < dot; i++) {
                if (name1.charAt(i) != name2.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static String getPackageName(Class<?> cls) {
        if (-assertionsDisabled || !cls.isArray()) {
            String name = cls.getName();
            int dot = name.lastIndexOf(46);
            if (dot < 0) {
                return "";
            }
            return name.substring(0, dot);
        }
        throw new AssertionError();
    }

    public static boolean isSamePackageMember(Class<?> class1, Class<?> class2) {
        if (class1 == class2) {
            return true;
        }
        return isSamePackage(class1, class2) && getOutermostEnclosingClass(class1) == getOutermostEnclosingClass(class2);
    }

    private static Class<?> getOutermostEnclosingClass(Class<?> c) {
        Class<?> pkgmem = c;
        Class<?> enc = c;
        while (true) {
            enc = enc.getEnclosingClass();
            if (enc == null) {
                return pkgmem;
            }
            pkgmem = enc;
        }
    }

    private static boolean loadersAreRelated(ClassLoader loader1, ClassLoader loader2, boolean loader1MustBeParent) {
        if (loader1 == loader2 || loader1 == null || (loader2 == null && (loader1MustBeParent ^ 1) != 0)) {
            return true;
        }
        for (ClassLoader scan2 = loader2; scan2 != null; scan2 = scan2.getParent()) {
            if (scan2 == loader1) {
                return true;
            }
        }
        if (loader1MustBeParent) {
            return false;
        }
        for (ClassLoader scan1 = loader1; scan1 != null; scan1 = scan1.getParent()) {
            if (scan1 == loader2) {
                return true;
            }
        }
        return false;
    }

    public static boolean classLoaderIsAncestor(Class<?> parentClass, Class<?> childClass) {
        return loadersAreRelated(parentClass.getClassLoader(), childClass.getClassLoader(), true);
    }
}
