package android.filterpacks.videosrc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.GLFrame;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.GenerateFinalPort;
import android.filterfw.core.MutableFrameFormat;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.MediaFile;
import android.opengl.Matrix;
import android.os.ConditionVariable;
import android.os.Process;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;

public class SurfaceTextureSource extends Filter {
    private static final String TAG = "SurfaceTextureSource";
    private static final boolean mLogVerbose = false;
    private static final float[] mSourceCoords = null;
    @GenerateFieldPort(hasDefault = true, name = "closeOnTimeout")
    private boolean mCloseOnTimeout;
    private boolean mFirstFrame;
    private ShaderProgram mFrameExtractor;
    private float[] mFrameTransform;
    @GenerateFieldPort(name = "height")
    private int mHeight;
    private float[] mMappedCoords;
    private GLFrame mMediaFrame;
    private ConditionVariable mNewFrameAvailable;
    private MutableFrameFormat mOutputFormat;
    private final String mRenderShader;
    @GenerateFinalPort(name = "sourceListener")
    private SurfaceTextureSourceListener mSourceListener;
    private SurfaceTexture mSurfaceTexture;
    @GenerateFieldPort(hasDefault = true, name = "waitForNewFrame")
    private boolean mWaitForNewFrame;
    @GenerateFieldPort(hasDefault = true, name = "waitTimeout")
    private int mWaitTimeout;
    @GenerateFieldPort(name = "width")
    private int mWidth;
    private OnFrameAvailableListener onFrameAvailableListener;

    public interface SurfaceTextureSourceListener {
        void onSurfaceTextureSourceReady(SurfaceTexture surfaceTexture);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.filterpacks.videosrc.SurfaceTextureSource.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.filterpacks.videosrc.SurfaceTextureSource.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.filterpacks.videosrc.SurfaceTextureSource.<clinit>():void");
    }

    public SurfaceTextureSource(String name) {
        super(name);
        this.mWaitForNewFrame = true;
        this.mWaitTimeout = Process.SYSTEM_UID;
        this.mCloseOnTimeout = false;
        this.mRenderShader = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n";
        this.onFrameAvailableListener = new OnFrameAvailableListener() {
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                if (SurfaceTextureSource.mLogVerbose) {
                    Log.v(SurfaceTextureSource.TAG, "New frame from SurfaceTexture");
                }
                SurfaceTextureSource.this.mNewFrameAvailable.open();
            }
        };
        this.mNewFrameAvailable = new ConditionVariable();
        this.mFrameTransform = new float[16];
        this.mMappedCoords = new float[16];
    }

    public void setupPorts() {
        addOutputPort("video", ImageFormat.create(3, 3));
    }

    private void createFormats() {
        this.mOutputFormat = ImageFormat.create(this.mWidth, this.mHeight, 3, 3);
    }

    protected void prepare(FilterContext context) {
        if (mLogVerbose) {
            Log.v(TAG, "Preparing SurfaceTextureSource");
        }
        createFormats();
        this.mMediaFrame = (GLFrame) context.getFrameManager().newBoundFrame(this.mOutputFormat, MediaFile.FILE_TYPE_MS_WORD, 0);
        this.mFrameExtractor = new ShaderProgram(context, "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n");
    }

    public void open(FilterContext context) {
        if (mLogVerbose) {
            Log.v(TAG, "Opening SurfaceTextureSource");
        }
        this.mSurfaceTexture = new SurfaceTexture(this.mMediaFrame.getTextureId());
        this.mSurfaceTexture.setOnFrameAvailableListener(this.onFrameAvailableListener);
        this.mSourceListener.onSurfaceTextureSourceReady(this.mSurfaceTexture);
        this.mFirstFrame = true;
    }

    public void process(FilterContext context) {
        if (mLogVerbose) {
            Log.v(TAG, "Processing new frame");
        }
        if (this.mWaitForNewFrame || this.mFirstFrame) {
            if (this.mWaitTimeout == 0) {
                this.mNewFrameAvailable.block();
            } else if (!this.mNewFrameAvailable.block((long) this.mWaitTimeout)) {
                if (this.mCloseOnTimeout) {
                    if (mLogVerbose) {
                        Log.v(TAG, "Timeout waiting for a new frame. Closing.");
                    }
                    closeOutputPort("video");
                    return;
                }
                throw new RuntimeException("Timeout waiting for new frame");
            }
            this.mNewFrameAvailable.close();
            this.mFirstFrame = false;
        }
        this.mSurfaceTexture.updateTexImage();
        this.mSurfaceTexture.getTransformMatrix(this.mFrameTransform);
        Matrix.multiplyMM(this.mMappedCoords, 0, this.mFrameTransform, 0, mSourceCoords, 0);
        this.mFrameExtractor.setSourceRegion(this.mMappedCoords[0], this.mMappedCoords[1], this.mMappedCoords[4], this.mMappedCoords[5], this.mMappedCoords[8], this.mMappedCoords[9], this.mMappedCoords[12], this.mMappedCoords[13]);
        Frame output = context.getFrameManager().newFrame(this.mOutputFormat);
        this.mFrameExtractor.process(this.mMediaFrame, output);
        output.setTimestamp(this.mSurfaceTexture.getTimestamp());
        pushOutput("video", output);
        output.release();
    }

    public void close(FilterContext context) {
        if (mLogVerbose) {
            Log.v(TAG, "SurfaceTextureSource closed");
        }
        this.mSourceListener.onSurfaceTextureSourceReady(null);
        this.mSurfaceTexture.release();
        this.mSurfaceTexture = null;
    }

    public void tearDown(FilterContext context) {
        if (this.mMediaFrame != null) {
            this.mMediaFrame.release();
        }
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (name.equals(Thumbnails.WIDTH) || name.equals(Thumbnails.HEIGHT)) {
            this.mOutputFormat.setDimensions(this.mWidth, this.mHeight);
        }
    }
}
