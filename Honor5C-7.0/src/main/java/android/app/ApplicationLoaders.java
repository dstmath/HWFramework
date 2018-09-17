package android.app;

import android.os.Trace;
import android.util.ArrayMap;
import com.android.internal.os.PathClassLoaderFactory;
import dalvik.system.PathClassLoader;

class ApplicationLoaders {
    private static final ApplicationLoaders gApplicationLoaders = null;
    private final ArrayMap<String, ClassLoader> mLoaders;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.ApplicationLoaders.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.ApplicationLoaders.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.ApplicationLoaders.<clinit>():void");
    }

    private static native void setupVulkanLayerPath(ClassLoader classLoader, String str);

    ApplicationLoaders() {
        this.mLoaders = new ArrayMap();
    }

    public static ApplicationLoaders getDefault() {
        return gApplicationLoaders;
    }

    public ClassLoader getClassLoader(String zip, int targetSdkVersion, boolean isBundled, String librarySearchPath, String libraryPermittedPath, ClassLoader parent) {
        ClassLoader baseParent = ClassLoader.getSystemClassLoader().getParent();
        synchronized (this.mLoaders) {
            if (parent == null) {
                parent = baseParent;
            }
            if (parent == baseParent) {
                ClassLoader loader = (ClassLoader) this.mLoaders.get(zip);
                if (loader != null) {
                    return loader;
                }
                Trace.traceBegin(64, zip);
                PathClassLoader pathClassloader = PathClassLoaderFactory.createClassLoader(zip, librarySearchPath, libraryPermittedPath, parent, targetSdkVersion, isBundled);
                Trace.traceEnd(64);
                Trace.traceBegin(64, "setupVulkanLayerPath");
                setupVulkanLayerPath(pathClassloader, librarySearchPath);
                Trace.traceEnd(64);
                this.mLoaders.put(zip, pathClassloader);
                return pathClassloader;
            }
            Trace.traceBegin(64, zip);
            pathClassloader = new PathClassLoader(zip, parent);
            Trace.traceEnd(64);
            return pathClassloader;
        }
    }

    void addPath(ClassLoader classLoader, String dexPath) {
        if (classLoader instanceof PathClassLoader) {
            ((PathClassLoader) classLoader).addDexPath(dexPath);
            return;
        }
        throw new IllegalStateException("class loader is not a PathClassLoader");
    }
}
