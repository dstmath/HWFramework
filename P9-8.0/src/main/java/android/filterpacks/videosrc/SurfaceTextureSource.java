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
import android.hardware.camera2.params.TonemapCurve;
import android.media.MediaCodec.MetricsConstants;
import android.media.MediaFormat;
import android.opengl.Matrix;
import android.os.ConditionVariable;
import android.util.Log;

public class SurfaceTextureSource extends Filter {
    private static final String TAG = "SurfaceTextureSource";
    private static final boolean mLogVerbose = Log.isLoggable(TAG, 2);
    private static final float[] mSourceCoords = new float[]{TonemapCurve.LEVEL_BLACK, 1.0f, TonemapCurve.LEVEL_BLACK, 1.0f, 1.0f, 1.0f, TonemapCurve.LEVEL_BLACK, 1.0f, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, 1.0f, 1.0f, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, 1.0f};
    @GenerateFieldPort(hasDefault = true, name = "closeOnTimeout")
    private boolean mCloseOnTimeout = false;
    private boolean mFirstFrame;
    private ShaderProgram mFrameExtractor;
    private float[] mFrameTransform = new float[16];
    @GenerateFieldPort(name = "height")
    private int mHeight;
    private float[] mMappedCoords = new float[16];
    private GLFrame mMediaFrame;
    private ConditionVariable mNewFrameAvailable = new ConditionVariable();
    private MutableFrameFormat mOutputFormat;
    private final String mRenderShader = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n";
    @GenerateFinalPort(name = "sourceListener")
    private SurfaceTextureSourceListener mSourceListener;
    private SurfaceTexture mSurfaceTexture;
    @GenerateFieldPort(hasDefault = true, name = "waitForNewFrame")
    private boolean mWaitForNewFrame = true;
    @GenerateFieldPort(hasDefault = true, name = "waitTimeout")
    private int mWaitTimeout = 1000;
    @GenerateFieldPort(name = "width")
    private int mWidth;
    private OnFrameAvailableListener onFrameAvailableListener = new OnFrameAvailableListener() {
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            if (SurfaceTextureSource.mLogVerbose) {
                Log.v(SurfaceTextureSource.TAG, "New frame from SurfaceTexture");
            }
            SurfaceTextureSource.this.mNewFrameAvailable.open();
        }
    };

    public interface SurfaceTextureSourceListener {
        void onSurfaceTextureSourceReady(SurfaceTexture surfaceTexture);
    }

    public SurfaceTextureSource(String name) {
        super(name);
    }

    public void setupPorts() {
        addOutputPort(MetricsConstants.MODE_VIDEO, ImageFormat.create(3, 3));
    }

    private void createFormats() {
        this.mOutputFormat = ImageFormat.create(this.mWidth, this.mHeight, 3, 3);
    }

    protected void prepare(FilterContext context) {
        if (mLogVerbose) {
            Log.v(TAG, "Preparing SurfaceTextureSource");
        }
        createFormats();
        this.mMediaFrame = (GLFrame) context.getFrameManager().newBoundFrame(this.mOutputFormat, 104, 0);
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
                    closeOutputPort(MetricsConstants.MODE_VIDEO);
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
        pushOutput(MetricsConstants.MODE_VIDEO, output);
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
        if (name.equals(MediaFormat.KEY_WIDTH) || name.equals(MediaFormat.KEY_HEIGHT)) {
            this.mOutputFormat.setDimensions(this.mWidth, this.mHeight);
        }
    }
}
