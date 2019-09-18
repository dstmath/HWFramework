package sun.reflect;

import java.lang.reflect.Modifier;

public class Reflection {
    public static void ensureMemberAccess(Class<?> currentClass, Class<?> memberClass, Object target, int modifiers) throws IllegalAccessException {
        if (currentClass == null || memberClass == null) {
            throw new InternalError();
        } else if (!verifyMemberAccess(currentClass, memberClass, target, modifiers)) {
            throw new IllegalAccessException("Class " + currentClass.getName() + " can not access a member of class " + memberClass.getName() + " with modifiers \"" + Modifier.toString(modifiers) + "\"");
        }
    }

    public static boolean verifyMemberAccess(Class<?> currentClass, Class<?> memberClass, Object target, int modifiers) {
        boolean gotIsSameClassPackage = false;
        boolean isSameClassPackage = false;
        if (currentClass == memberClass) {
            return true;
        }
        if (!Modifier.isPublic(memberClass.getAccessFlags())) {
            isSameClassPackage = isSameClassPackage(currentClass, memberClass);
            gotIsSameClassPackage = true;
            if (!isSameClassPackage) {
                return false;
            }
        }
        if (Modifier.isPublic(modifiers)) {
            return true;
        }
        boolean successSoFar = false;
        if (Modifier.isProtected(modifiers) && isSubclassOf(currentClass, memberClass)) {
            successSoFar = true;
        }
        if (!successSoFar && !Modifier.isPrivate(modifiers)) {
            if (!gotIsSameClassPackage) {
                isSameClassPackage = isSameClassPackage(currentClass, memberClass);
                gotIsSameClassPackage = true;
            }
            if (isSameClassPackage) {
                successSoFar = true;
            }
        }
        if (!successSoFar) {
            return false;
        }
        if (Modifier.isProtected(modifiers)) {
            Class<?> targetClass = target == null ? memberClass : target.getClass();
            if (targetClass != currentClass) {
                if (!gotIsSameClassPackage) {
                    isSameClassPackage = isSameClassPackage(currentClass, memberClass);
                }
                if (isSameClassPackage || isSubclassOf(targetClass, currentClass)) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    private static boolean isSameClassPackage(Class<?> c1, Class<?> c2) {
        return isSameClassPackage(c1.getClassLoader(), c1.getName(), c2.getClassLoader(), c2.getName());
    }

    private static boolean isSameClassPackage(ClassLoader loader1, String name1, ClassLoader loader2, String name2) {
        String str = name1;
        String str2 = name2;
        boolean z = false;
        if (loader1 != loader2) {
            return false;
        }
        int lastDot1 = str.lastIndexOf(46);
        int lastDot2 = str2.lastIndexOf(46);
        if (lastDot1 == -1 || lastDot2 == -1) {
            if (lastDot1 == lastDot2) {
                z = true;
            }
            return z;
        }
        int idx1 = 0;
        int idx2 = 0;
        if (str.charAt(0) == '[') {
            do {
                idx1++;
            } while (str.charAt(idx1) == '[');
            if (str.charAt(idx1) != 'L') {
                throw new InternalError("Illegal class name " + str);
            }
        }
        int idx12 = idx1;
        if (str2.charAt(0) == 91) {
            do {
                idx2++;
            } while (str2.charAt(idx2) == '[');
            if (str2.charAt(idx2) != 'L') {
                throw new InternalError("Illegal class name " + str2);
            }
        }
        int idx22 = idx2;
        int length1 = lastDot1 - idx12;
        if (length1 != lastDot2 - idx22) {
            return false;
        }
        return str.regionMatches(false, idx12, str2, idx22, length1);
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=java.lang.Class<?>, code=java.lang.Class, for r1v0, types: [java.lang.Class<?>] */
    static boolean isSubclassOf(Class queryClass, Class<?> ofClass) {
        while (queryClass != null) {
            if (queryClass == ofClass) {
                return true;
            }
            queryClass = queryClass.getSuperclass();
        }
        return false;
    }
}
