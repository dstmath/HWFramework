package java.lang;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import libcore.io.ClassPathURLStreamHandler;

class VMClassLoader {
    private static final ClassPathURLStreamHandler[] bootClassPathUrlHandlers = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.VMClassLoader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.VMClassLoader.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.lang.VMClassLoader.<clinit>():void");
    }

    static native Class findLoadedClass(ClassLoader classLoader, String str);

    private static native String[] getBootClassPathEntries();

    VMClassLoader() {
    }

    private static ClassPathURLStreamHandler[] createBootClassPathUrlHandlers() {
        String[] bootClassPathEntries = getBootClassPathEntries();
        ArrayList<String> zipFileUris = new ArrayList(bootClassPathEntries.length);
        ArrayList<URLStreamHandler> urlStreamHandlers = new ArrayList(bootClassPathEntries.length);
        for (String bootClassPathEntry : bootClassPathEntries) {
            try {
                String entryUri = new File(bootClassPathEntry).toURI().toString();
                URLStreamHandler urlStreamHandler = new ClassPathURLStreamHandler(bootClassPathEntry);
                zipFileUris.add(entryUri);
                urlStreamHandlers.add(urlStreamHandler);
            } catch (IOException e) {
                System.logE("Unable to open boot classpath entry: " + bootClassPathEntry, e);
            }
        }
        return (ClassPathURLStreamHandler[]) urlStreamHandlers.toArray(new ClassPathURLStreamHandler[urlStreamHandlers.size()]);
    }

    static URL getResource(String name) {
        for (ClassPathURLStreamHandler urlHandler : bootClassPathUrlHandlers) {
            URL url = urlHandler.getEntryUrlOrNull(name);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    static List<URL> getResources(String name) {
        ArrayList<URL> list = new ArrayList();
        for (ClassPathURLStreamHandler urlHandler : bootClassPathUrlHandlers) {
            URL url = urlHandler.getEntryUrlOrNull(name);
            if (url != null) {
                list.add(url);
            }
        }
        return list;
    }
}
