package android.filterfw.core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class FilterSurfaceView extends SurfaceView implements Callback {
    private static int STATE_ALLOCATED;
    private static int STATE_CREATED;
    private static int STATE_INITIALIZED;
    private int mFormat;
    private GLEnvironment mGLEnv;
    private int mHeight;
    private Callback mListener;
    private int mState;
    private int mSurfaceId;
    private int mWidth;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.filterfw.core.FilterSurfaceView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.filterfw.core.FilterSurfaceView.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.filterfw.core.FilterSurfaceView.<clinit>():void");
    }

    public FilterSurfaceView(Context context) {
        super(context);
        this.mState = STATE_ALLOCATED;
        this.mSurfaceId = -1;
        getHolder().addCallback(this);
    }

    public FilterSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mState = STATE_ALLOCATED;
        this.mSurfaceId = -1;
        getHolder().addCallback(this);
    }

    public synchronized void bindToListener(Callback listener, GLEnvironment glEnv) {
        if (listener == null) {
            throw new NullPointerException("Attempting to bind null filter to SurfaceView!");
        } else if (this.mListener == null || this.mListener == listener) {
            this.mListener = listener;
            if (!(this.mGLEnv == null || this.mGLEnv == glEnv)) {
                this.mGLEnv.unregisterSurfaceId(this.mSurfaceId);
            }
            this.mGLEnv = glEnv;
            if (this.mState >= STATE_CREATED) {
                registerSurface();
                this.mListener.surfaceCreated(getHolder());
                if (this.mState == STATE_INITIALIZED) {
                    this.mListener.surfaceChanged(getHolder(), this.mFormat, this.mWidth, this.mHeight);
                }
            }
        } else {
            throw new RuntimeException("Attempting to bind filter " + listener + " to SurfaceView with another open " + "filter " + this.mListener + " attached already!");
        }
    }

    public synchronized void unbind() {
        this.mListener = null;
    }

    public synchronized int getSurfaceId() {
        return this.mSurfaceId;
    }

    public synchronized GLEnvironment getGLEnv() {
        return this.mGLEnv;
    }

    public synchronized void surfaceCreated(SurfaceHolder holder) {
        this.mState = STATE_CREATED;
        if (this.mGLEnv != null) {
            registerSurface();
        }
        if (this.mListener != null) {
            this.mListener.surfaceCreated(holder);
        }
    }

    public synchronized void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.mFormat = format;
        this.mWidth = width;
        this.mHeight = height;
        this.mState = STATE_INITIALIZED;
        if (this.mListener != null) {
            this.mListener.surfaceChanged(holder, format, width, height);
        }
    }

    public synchronized void surfaceDestroyed(SurfaceHolder holder) {
        this.mState = STATE_ALLOCATED;
        if (this.mListener != null) {
            this.mListener.surfaceDestroyed(holder);
        }
        unregisterSurface();
    }

    private void registerSurface() {
        this.mSurfaceId = this.mGLEnv.registerSurface(getHolder().getSurface());
        if (this.mSurfaceId < 0) {
            throw new RuntimeException("Could not register Surface: " + getHolder().getSurface() + " in FilterSurfaceView!");
        }
    }

    private void unregisterSurface() {
        if (this.mGLEnv != null && this.mSurfaceId > 0) {
            this.mGLEnv.unregisterSurfaceId(this.mSurfaceId);
        }
    }
}
