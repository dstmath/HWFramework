package android.view.inputmethod;

import android.os.Bundle;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class InputConnectionInspector {
    private static final Map<Class, Integer> sMissingMethodsMap = Collections.synchronizedMap(new WeakHashMap());

    public static int getMissingMethodFlags(InputConnection ic) {
        if (ic == null || (ic instanceof BaseInputConnection)) {
            return 0;
        }
        if (ic instanceof InputConnectionWrapper) {
            return ((InputConnectionWrapper) ic).getMissingMethodFlags();
        }
        return getMissingMethodFlagsInternal(ic.getClass());
    }

    public static int getMissingMethodFlagsInternal(Class clazz) {
        Integer cachedFlags = (Integer) sMissingMethodsMap.get(clazz);
        if (cachedFlags != null) {
            return cachedFlags.intValue();
        }
        int flags = 0;
        if (!hasGetSelectedText(clazz)) {
            flags = 1;
        }
        if (!hasSetComposingRegion(clazz)) {
            flags |= 2;
        }
        if (!hasCommitCorrection(clazz)) {
            flags |= 4;
        }
        if (!hasRequestCursorUpdate(clazz)) {
            flags |= 8;
        }
        if (!hasDeleteSurroundingTextInCodePoints(clazz)) {
            flags |= 16;
        }
        if (!hasGetHandler(clazz)) {
            flags |= 32;
        }
        if (!hasCloseConnection(clazz)) {
            flags |= 64;
        }
        if (!hasCommitContent(clazz)) {
            flags |= 128;
        }
        sMissingMethodsMap.put(clazz, Integer.valueOf(flags));
        return flags;
    }

    private static boolean hasGetSelectedText(Class clazz) {
        try {
            return Modifier.isAbstract(clazz.getMethod("getSelectedText", new Class[]{Integer.TYPE}).getModifiers()) ^ 1;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasSetComposingRegion(Class clazz) {
        try {
            return Modifier.isAbstract(clazz.getMethod("setComposingRegion", new Class[]{Integer.TYPE, Integer.TYPE}).getModifiers()) ^ 1;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasCommitCorrection(Class clazz) {
        try {
            return Modifier.isAbstract(clazz.getMethod("commitCorrection", new Class[]{CorrectionInfo.class}).getModifiers()) ^ 1;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasRequestCursorUpdate(Class clazz) {
        try {
            return Modifier.isAbstract(clazz.getMethod("requestCursorUpdates", new Class[]{Integer.TYPE}).getModifiers()) ^ 1;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasDeleteSurroundingTextInCodePoints(Class clazz) {
        try {
            return Modifier.isAbstract(clazz.getMethod("deleteSurroundingTextInCodePoints", new Class[]{Integer.TYPE, Integer.TYPE}).getModifiers()) ^ 1;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasGetHandler(Class clazz) {
        try {
            return Modifier.isAbstract(clazz.getMethod("getHandler", new Class[0]).getModifiers()) ^ 1;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasCloseConnection(Class clazz) {
        try {
            return Modifier.isAbstract(clazz.getMethod("closeConnection", new Class[0]).getModifiers()) ^ 1;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasCommitContent(Class clazz) {
        try {
            return Modifier.isAbstract(clazz.getMethod("commitContent", new Class[]{InputContentInfo.class, Integer.TYPE, Bundle.class}).getModifiers()) ^ 1;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static String getMissingMethodFlagsAsString(int flags) {
        StringBuilder sb = new StringBuilder();
        boolean isEmpty = true;
        if ((flags & 1) != 0) {
            sb.append("getSelectedText(int)");
            isEmpty = false;
        }
        if ((flags & 2) != 0) {
            if (!isEmpty) {
                sb.append(",");
            }
            sb.append("setComposingRegion(int, int)");
            isEmpty = false;
        }
        if ((flags & 4) != 0) {
            if (!isEmpty) {
                sb.append(",");
            }
            sb.append("commitCorrection(CorrectionInfo)");
            isEmpty = false;
        }
        if ((flags & 8) != 0) {
            if (!isEmpty) {
                sb.append(",");
            }
            sb.append("requestCursorUpdate(int)");
            isEmpty = false;
        }
        if ((flags & 16) != 0) {
            if (!isEmpty) {
                sb.append(",");
            }
            sb.append("deleteSurroundingTextInCodePoints(int, int)");
            isEmpty = false;
        }
        if ((flags & 32) != 0) {
            if (!isEmpty) {
                sb.append(",");
            }
            sb.append("getHandler()");
        }
        if ((flags & 64) != 0) {
            if (!isEmpty) {
                sb.append(",");
            }
            sb.append("closeConnection()");
        }
        if ((flags & 128) != 0) {
            if (!isEmpty) {
                sb.append(",");
            }
            sb.append("commitContent(InputContentInfo, Bundle)");
        }
        return sb.toString();
    }
}
