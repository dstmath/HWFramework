package java.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

class DeleteOnExitHook {
    private static LinkedHashSet<String> files;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.io.DeleteOnExitHook.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.io.DeleteOnExitHook.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.io.DeleteOnExitHook.<clinit>():void");
    }

    private DeleteOnExitHook() {
    }

    static synchronized void add(String file) {
        synchronized (DeleteOnExitHook.class) {
            if (files == null) {
                throw new IllegalStateException("Shutdown in progress");
            }
            files.add(file);
        }
    }

    static void runHooks() {
        Collection theFiles;
        synchronized (DeleteOnExitHook.class) {
            theFiles = files;
            files = null;
        }
        ArrayList<String> toBeDeleted = new ArrayList(theFiles);
        Collections.reverse(toBeDeleted);
        for (String filename : toBeDeleted) {
            new File(filename).delete();
        }
    }
}
