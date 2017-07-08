package android.view.inputmethod;

import android.util.PtmLog;
import java.lang.reflect.Modifier;
import java.util.Map;

public final class InputConnectionInspector {
    private static final Map<Class, Integer> sMissingMethodsMap = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.inputmethod.InputConnectionInspector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.inputmethod.InputConnectionInspector.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.inputmethod.InputConnectionInspector.<clinit>():void");
    }

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
        sMissingMethodsMap.put(clazz, Integer.valueOf(flags));
        return flags;
    }

    private static boolean hasGetSelectedText(Class clazz) {
        boolean z = false;
        try {
            if (!Modifier.isAbstract(clazz.getMethod("getSelectedText", new Class[]{Integer.TYPE}).getModifiers())) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasSetComposingRegion(Class clazz) {
        boolean z = false;
        try {
            if (!Modifier.isAbstract(clazz.getMethod("setComposingRegion", new Class[]{Integer.TYPE, Integer.TYPE}).getModifiers())) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasCommitCorrection(Class clazz) {
        boolean z = false;
        try {
            if (!Modifier.isAbstract(clazz.getMethod("commitCorrection", new Class[]{CorrectionInfo.class}).getModifiers())) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasRequestCursorUpdate(Class clazz) {
        boolean z = false;
        try {
            if (!Modifier.isAbstract(clazz.getMethod("requestCursorUpdates", new Class[]{Integer.TYPE}).getModifiers())) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasDeleteSurroundingTextInCodePoints(Class clazz) {
        boolean z = false;
        try {
            if (!Modifier.isAbstract(clazz.getMethod("deleteSurroundingTextInCodePoints", new Class[]{Integer.TYPE, Integer.TYPE}).getModifiers())) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasGetHandler(Class clazz) {
        boolean z = false;
        try {
            if (!Modifier.isAbstract(clazz.getMethod("getHandler", new Class[0]).getModifiers())) {
                z = true;
            }
            return z;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean hasCloseConnection(Class clazz) {
        boolean z = false;
        try {
            if (!Modifier.isAbstract(clazz.getMethod("closeConnection", new Class[0]).getModifiers())) {
                z = true;
            }
            return z;
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
                sb.append(PtmLog.PAIRE_DELIMETER);
            }
            sb.append("setComposingRegion(int, int)");
            isEmpty = false;
        }
        if ((flags & 4) != 0) {
            if (!isEmpty) {
                sb.append(PtmLog.PAIRE_DELIMETER);
            }
            sb.append("commitCorrection(CorrectionInfo)");
            isEmpty = false;
        }
        if ((flags & 8) != 0) {
            if (!isEmpty) {
                sb.append(PtmLog.PAIRE_DELIMETER);
            }
            sb.append("requestCursorUpdate(int)");
            isEmpty = false;
        }
        if ((flags & 16) != 0) {
            if (!isEmpty) {
                sb.append(PtmLog.PAIRE_DELIMETER);
            }
            sb.append("deleteSurroundingTextInCodePoints(int, int)");
            isEmpty = false;
        }
        if ((flags & 32) != 0) {
            if (!isEmpty) {
                sb.append(PtmLog.PAIRE_DELIMETER);
            }
            sb.append("getHandler()");
        }
        if ((flags & 64) != 0) {
            if (!isEmpty) {
                sb.append(PtmLog.PAIRE_DELIMETER);
            }
            sb.append("closeConnection()");
        }
        return sb.toString();
    }
}
