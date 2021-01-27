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
import android.hardware.Camera;
import android.opengl.Matrix;
import android.telephony.SmsManager;
import android.util.Log;
import java.io.IOException;
import java.util.List;

public class CameraSource extends Filter {
    private static final int NEWFRAME_TIMEOUT = 100;
    private static final int NEWFRAME_TIMEOUT_REPEAT = 10;
    private static final String TAG = "CameraSource";
    private static final String mFrameShader = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n";
    private static final float[] mSourceCoords = {0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f};
    private Camera mCamera;
    private GLFrame mCameraFrame;
    @GenerateFieldPort(hasDefault = true, name = "id")
    private int mCameraId = 0;
    private Camera.Parameters mCameraParameters;
    private float[] mCameraTransform = new float[16];
    @GenerateFieldPort(hasDefault = true, name = "framerate")
    private int mFps = 30;
    private ShaderProgram mFrameExtractor;
    @GenerateFieldPort(hasDefault = true, name = "height")
    private int mHeight = 240;
    private final boolean mLogVerbose = Log.isLoggable(TAG, 2);
    private float[] mMappedCoords = new float[16];
    private boolean mNewFrameAvailable;
    private MutableFrameFormat mOutputFormat;
    private SurfaceTexture mSurfaceTexture;
    @GenerateFinalPort(hasDefault = true, name = "waitForNewFrame")
    private boolean mWaitForNewFrame = true;
    @GenerateFieldPort(hasDefault = true, name = "width")
    private int mWidth = 320;
    private SurfaceTexture.OnFrameAvailableListener onCameraFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
        /* class android.filterpacks.videosrc.CameraSource.AnonymousClass1 */

        @Override // android.graphics.SurfaceTexture.OnFrameAvailableListener
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            if (CameraSource.this.mLogVerbose) {
                Log.v(CameraSource.TAG, "New frame from camera");
            }
            synchronized (CameraSource.this) {
                CameraSource.this.mNewFrameAvailable = true;
                CameraSource.this.notify();
            }
        }
    };

    public CameraSource(String name) {
        super(name);
    }

    @Override // android.filterfw.core.Filter
    public void setupPorts() {
        addOutputPort("video", ImageFormat.create(3, 3));
    }

    private void createFormats() {
        this.mOutputFormat = ImageFormat.create(this.mWidth, this.mHeight, 3, 3);
    }

    @Override // android.filterfw.core.Filter
    public void prepare(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Preparing");
        }
        this.mFrameExtractor = new ShaderProgram(context, mFrameShader);
    }

    @Override // android.filterfw.core.Filter
    public void open(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Opening");
        }
        this.mCamera = Camera.open(this.mCameraId);
        getCameraParameters();
        this.mCamera.setParameters(this.mCameraParameters);
        createFormats();
        this.mCameraFrame = (GLFrame) context.getFrameManager().newBoundFrame(this.mOutputFormat, 104, 0);
        this.mSurfaceTexture = new SurfaceTexture(this.mCameraFrame.getTextureId());
        try {
            this.mCamera.setPreviewTexture(this.mSurfaceTexture);
            this.mSurfaceTexture.setOnFrameAvailableListener(this.onCameraFrameAvailableListener);
            this.mNewFrameAvailable = false;
            this.mCamera.startPreview();
        } catch (IOException e) {
            throw new RuntimeException("Could not bind camera surface texture: " + e.getMessage() + "!");
        }
    }

    @Override // android.filterfw.core.Filter
    public void process(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Processing new frame");
        }
        if (this.mWaitForNewFrame) {
            while (!this.mNewFrameAvailable) {
                if (0 != 10) {
                    try {
                        wait(100);
                    } catch (InterruptedException e) {
                        if (this.mLogVerbose) {
                            Log.v(TAG, "Interrupted while waiting for new frame");
                        }
                    }
                } else {
                    throw new RuntimeException("Timeout waiting for new frame");
                }
            }
            this.mNewFrameAvailable = false;
            if (this.mLogVerbose) {
                Log.v(TAG, "Got new frame");
            }
        }
        this.mSurfaceTexture.updateTexImage();
        if (this.mLogVerbose) {
            Log.v(TAG, "Using frame extractor in thread: " + Thread.currentThread());
        }
        this.mSurfaceTexture.getTransformMatrix(this.mCameraTransform);
        Matrix.multiplyMM(this.mMappedCoords, 0, this.mCameraTransform, 0, mSourceCoords, 0);
        ShaderProgram shaderProgram = this.mFrameExtractor;
        float[] fArr = this.mMappedCoords;
        shaderProgram.setSourceRegion(fArr[0], fArr[1], fArr[4], fArr[5], fArr[8], fArr[9], fArr[12], fArr[13]);
        Frame output = context.getFrameManager().newFrame(this.mOutputFormat);
        this.mFrameExtractor.process(this.mCameraFrame, output);
        long timestamp = this.mSurfaceTexture.getTimestamp();
        if (this.mLogVerbose) {
            Log.v(TAG, "Timestamp: " + (((double) timestamp) / 1.0E9d) + " s");
        }
        output.setTimestamp(timestamp);
        pushOutput("video", output);
        output.release();
        if (this.mLogVerbose) {
            Log.v(TAG, "Done processing new frame");
        }
    }

    @Override // android.filterfw.core.Filter
    public void close(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Closing");
        }
        this.mCamera.release();
        this.mCamera = null;
        this.mSurfaceTexture.release();
        this.mSurfaceTexture = null;
    }

    @Override // android.filterfw.core.Filter
    public void tearDown(FilterContext context) {
        GLFrame gLFrame = this.mCameraFrame;
        if (gLFrame != null) {
            gLFrame.release();
        }
    }

    @Override // android.filterfw.core.Filter
    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (name.equals("framerate")) {
            getCameraParameters();
            int[] closestRange = findClosestFpsRange(this.mFps, this.mCameraParameters);
            this.mCameraParameters.setPreviewFpsRange(closestRange[0], closestRange[1]);
            this.mCamera.setParameters(this.mCameraParameters);
        }
    }

    public synchronized Camera.Parameters getCameraParameters() {
        boolean closeCamera = false;
        if (this.mCameraParameters == null) {
            if (this.mCamera == null) {
                this.mCamera = Camera.open(this.mCameraId);
                closeCamera = true;
            }
            this.mCameraParameters = this.mCamera.getParameters();
            if (closeCamera) {
                this.mCamera.release();
                this.mCamera = null;
            }
        }
        int[] closestSize = findClosestSize(this.mWidth, this.mHeight, this.mCameraParameters);
        this.mWidth = closestSize[0];
        this.mHeight = closestSize[1];
        this.mCameraParameters.setPreviewSize(this.mWidth, this.mHeight);
        int[] closestRange = findClosestFpsRange(this.mFps, this.mCameraParameters);
        this.mCameraParameters.setPreviewFpsRange(closestRange[0], closestRange[1]);
        return this.mCameraParameters;
    }

    public synchronized void setCameraParameters(Camera.Parameters params) {
        params.setPreviewSize(this.mWidth, this.mHeight);
        this.mCameraParameters = params;
        if (isOpen()) {
            this.mCamera.setParameters(this.mCameraParameters);
        }
    }

    private int[] findClosestSize(int width, int height, Camera.Parameters parameters) {
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        int closestWidth = -1;
        int closestHeight = -1;
        int smallestWidth = previewSizes.get(0).width;
        int smallestHeight = previewSizes.get(0).height;
        for (Camera.Size size : previewSizes) {
            if (size.width <= width && size.height <= height && size.width >= closestWidth && size.height >= closestHeight) {
                closestWidth = size.width;
                closestHeight = size.height;
            }
            if (size.width < smallestWidth && size.height < smallestHeight) {
                smallestWidth = size.width;
                smallestHeight = size.height;
            }
        }
        if (closestWidth == -1) {
            closestWidth = smallestWidth;
            closestHeight = smallestHeight;
        }
        if (this.mLogVerbose) {
            Log.v(TAG, "Requested resolution: (" + width + ", " + height + "). Closest match: (" + closestWidth + ", " + closestHeight + ").");
        }
        return new int[]{closestWidth, closestHeight};
    }

    private int[] findClosestFpsRange(int fps, Camera.Parameters params) {
        List<int[]> supportedFpsRanges = params.getSupportedPreviewFpsRange();
        int[] closestRange = supportedFpsRanges.get(0);
        for (int[] range : supportedFpsRanges) {
            if (range[0] < fps * 1000 && range[1] > fps * 1000 && range[0] > closestRange[0] && range[1] < closestRange[1]) {
                closestRange = range;
            }
        }
        if (this.mLogVerbose) {
            Log.v(TAG, "Requested fps: " + fps + ".Closest frame rate range: [" + (((double) closestRange[0]) / 1000.0d) + SmsManager.REGEX_PREFIX_DELIMITER + (((double) closestRange[1]) / 1000.0d) + "]");
        }
        return closestRange;
    }
}
