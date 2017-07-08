package android.graphics;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import java.lang.ref.WeakReference;

public class SurfaceTexture {
    private final Looper mCreatorLooper;
    private long mFrameAvailableListener;
    private Handler mOnFrameAvailableHandler;
    private long mProducer;
    private long mSurfaceTexture;

    public interface OnFrameAvailableListener {
        void onFrameAvailable(SurfaceTexture surfaceTexture);
    }

    /* renamed from: android.graphics.SurfaceTexture.1 */
    class AnonymousClass1 extends Handler {
        final /* synthetic */ OnFrameAvailableListener val$listener;

        AnonymousClass1(Looper $anonymous0, Callback $anonymous1, boolean $anonymous2, OnFrameAvailableListener val$listener) {
            this.val$listener = val$listener;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void handleMessage(Message msg) {
            this.val$listener.onFrameAvailable(SurfaceTexture.this);
        }
    }

    @Deprecated
    public static class OutOfResourcesException extends Exception {
        public OutOfResourcesException(String name) {
            super(name);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.SurfaceTexture.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.SurfaceTexture.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.SurfaceTexture.<clinit>():void");
    }

    private native int nativeAttachToGLContext(int i);

    private static native void nativeClassInit();

    private native int nativeDetachFromGLContext();

    private native void nativeFinalize();

    private native int nativeGetQueuedCount();

    private native long nativeGetTimestamp();

    private native void nativeGetTransformMatrix(float[] fArr);

    private native void nativeInit(boolean z, int i, boolean z2, WeakReference<SurfaceTexture> weakReference) throws android.view.Surface.OutOfResourcesException;

    private native boolean nativeIsReleased();

    private native void nativeRelease();

    private native void nativeReleaseTexImage();

    private native void nativeSetDefaultBufferSize(int i, int i2);

    private native void nativeUpdateTexImage();

    public SurfaceTexture(int texName) {
        this(texName, false);
    }

    public SurfaceTexture(int texName, boolean singleBufferMode) {
        this.mCreatorLooper = Looper.myLooper();
        nativeInit(false, texName, singleBufferMode, new WeakReference(this));
    }

    public SurfaceTexture(boolean singleBufferMode) {
        this.mCreatorLooper = Looper.myLooper();
        nativeInit(true, 0, singleBufferMode, new WeakReference(this));
    }

    public void setOnFrameAvailableListener(OnFrameAvailableListener listener) {
        setOnFrameAvailableListener(listener, null);
    }

    public void setOnFrameAvailableListener(OnFrameAvailableListener listener, Handler handler) {
        if (listener != null) {
            Looper looper = handler != null ? handler.getLooper() : this.mCreatorLooper != null ? this.mCreatorLooper : Looper.getMainLooper();
            this.mOnFrameAvailableHandler = new AnonymousClass1(looper, null, true, listener);
            return;
        }
        this.mOnFrameAvailableHandler = null;
    }

    public void setDefaultBufferSize(int width, int height) {
        nativeSetDefaultBufferSize(width, height);
    }

    public void updateTexImage() {
        nativeUpdateTexImage();
    }

    public void releaseTexImage() {
        nativeReleaseTexImage();
    }

    public void detachFromGLContext() {
        if (nativeDetachFromGLContext() != 0) {
            throw new RuntimeException("Error during detachFromGLContext (see logcat for details)");
        }
    }

    public void attachToGLContext(int texName) {
        if (nativeAttachToGLContext(texName) != 0) {
            throw new RuntimeException("Error during attachToGLContext (see logcat for details)");
        }
    }

    public void getTransformMatrix(float[] mtx) {
        if (mtx.length != 16) {
            throw new IllegalArgumentException();
        }
        nativeGetTransformMatrix(mtx);
    }

    public long getTimestamp() {
        return nativeGetTimestamp();
    }

    public void release() {
        nativeRelease();
    }

    public boolean isReleased() {
        return nativeIsReleased();
    }

    protected void finalize() throws Throwable {
        try {
            nativeFinalize();
        } finally {
            super.finalize();
        }
    }

    private static void postEventFromNative(WeakReference<SurfaceTexture> weakSelf) {
        SurfaceTexture st = (SurfaceTexture) weakSelf.get();
        if (st != null) {
            Handler handler = st.mOnFrameAvailableHandler;
            if (handler != null) {
                handler.sendEmptyMessage(0);
            }
        }
    }
}
