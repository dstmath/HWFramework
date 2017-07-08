package android.filterfw.core;

import android.graphics.SurfaceTexture;
import android.media.MediaRecorder;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

public class GLEnvironment {
    private int glEnvId;
    private boolean mManageContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.filterfw.core.GLEnvironment.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.filterfw.core.GLEnvironment.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.filterfw.core.GLEnvironment.<clinit>():void");
    }

    private native boolean nativeActivate();

    private native boolean nativeActivateSurfaceId(int i);

    private native int nativeAddSurface(Surface surface);

    private native int nativeAddSurfaceFromMediaRecorder(MediaRecorder mediaRecorder);

    private native int nativeAddSurfaceWidthHeight(Surface surface, int i, int i2);

    private native boolean nativeAllocate();

    private native boolean nativeDeactivate();

    private native boolean nativeDeallocate();

    private native boolean nativeDisconnectSurfaceMediaSource(MediaRecorder mediaRecorder);

    private native boolean nativeInitWithCurrentContext();

    private native boolean nativeInitWithNewContext();

    private native boolean nativeIsActive();

    private static native boolean nativeIsAnyContextActive();

    private native boolean nativeIsContextActive();

    private native boolean nativeRemoveSurfaceId(int i);

    private native boolean nativeSetSurfaceTimestamp(long j);

    private native boolean nativeSwapBuffers();

    public GLEnvironment() {
        this.mManageContext = true;
        nativeAllocate();
    }

    private GLEnvironment(NativeAllocatorTag tag) {
        this.mManageContext = true;
    }

    public synchronized void tearDown() {
        if (this.glEnvId != -1) {
            nativeDeallocate();
            this.glEnvId = -1;
        }
    }

    protected void finalize() throws Throwable {
        tearDown();
    }

    public void initWithNewContext() {
        this.mManageContext = true;
        if (!nativeInitWithNewContext()) {
            throw new RuntimeException("Could not initialize GLEnvironment with new context!");
        }
    }

    public void initWithCurrentContext() {
        this.mManageContext = false;
        if (!nativeInitWithCurrentContext()) {
            throw new RuntimeException("Could not initialize GLEnvironment with current context!");
        }
    }

    public boolean isActive() {
        return nativeIsActive();
    }

    public boolean isContextActive() {
        return nativeIsContextActive();
    }

    public static boolean isAnyContextActive() {
        return nativeIsAnyContextActive();
    }

    public void activate() {
        if (Looper.myLooper() != null && Looper.myLooper().equals(Looper.getMainLooper())) {
            Log.e("FilterFramework", "Activating GL context in UI thread!");
        }
        if (this.mManageContext && !nativeActivate()) {
            throw new RuntimeException("Could not activate GLEnvironment!");
        }
    }

    public void deactivate() {
        if (this.mManageContext && !nativeDeactivate()) {
            throw new RuntimeException("Could not deactivate GLEnvironment!");
        }
    }

    public void swapBuffers() {
        if (!nativeSwapBuffers()) {
            throw new RuntimeException("Error swapping EGL buffers!");
        }
    }

    public int registerSurface(Surface surface) {
        int result = nativeAddSurface(surface);
        if (result >= 0) {
            return result;
        }
        throw new RuntimeException("Error registering surface " + surface + "!");
    }

    public int registerSurfaceTexture(SurfaceTexture surfaceTexture, int width, int height) {
        Surface surface = new Surface(surfaceTexture);
        int result = nativeAddSurfaceWidthHeight(surface, width, height);
        surface.release();
        if (result >= 0) {
            return result;
        }
        throw new RuntimeException("Error registering surfaceTexture " + surfaceTexture + "!");
    }

    public int registerSurfaceFromMediaRecorder(MediaRecorder mediaRecorder) {
        int result = nativeAddSurfaceFromMediaRecorder(mediaRecorder);
        if (result >= 0) {
            return result;
        }
        throw new RuntimeException("Error registering surface from MediaRecorder" + mediaRecorder + "!");
    }

    public void activateSurfaceWithId(int surfaceId) {
        if (!nativeActivateSurfaceId(surfaceId)) {
            throw new RuntimeException("Could not activate surface " + surfaceId + "!");
        }
    }

    public void unregisterSurfaceId(int surfaceId) {
        if (!nativeRemoveSurfaceId(surfaceId)) {
            throw new RuntimeException("Could not unregister surface " + surfaceId + "!");
        }
    }

    public void setSurfaceTimestamp(long timestamp) {
        if (!nativeSetSurfaceTimestamp(timestamp)) {
            throw new RuntimeException("Could not set timestamp for current surface!");
        }
    }
}
