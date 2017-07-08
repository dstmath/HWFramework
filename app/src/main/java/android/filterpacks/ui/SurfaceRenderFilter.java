package android.filterpacks.ui;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.FilterSurfaceView;
import android.filterfw.core.Frame;
import android.filterfw.core.GLEnvironment;
import android.filterfw.core.GLFrame;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.GenerateFinalPort;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.rms.HwSysResource;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

public class SurfaceRenderFilter extends Filter implements Callback {
    private static final String TAG = "SurfaceRenderFilter";
    private final int RENDERMODE_FILL_CROP;
    private final int RENDERMODE_FIT;
    private final int RENDERMODE_STRETCH;
    private float mAspectRatio;
    private boolean mIsBound;
    private boolean mLogVerbose;
    private ShaderProgram mProgram;
    private int mRenderMode;
    @GenerateFieldPort(hasDefault = true, name = "renderMode")
    private String mRenderModeString;
    private GLFrame mScreen;
    private int mScreenHeight;
    private int mScreenWidth;
    @GenerateFinalPort(name = "surfaceView")
    private FilterSurfaceView mSurfaceView;

    public SurfaceRenderFilter(String name) {
        super(name);
        this.RENDERMODE_STRETCH = 0;
        this.RENDERMODE_FIT = 1;
        this.RENDERMODE_FILL_CROP = 2;
        this.mIsBound = false;
        this.mRenderMode = 1;
        this.mAspectRatio = Engine.DEFAULT_VOLUME;
        this.mLogVerbose = Log.isLoggable(TAG, 2);
    }

    public void setupPorts() {
        if (this.mSurfaceView == null) {
            throw new RuntimeException("NULL SurfaceView passed to SurfaceRenderFilter");
        }
        addMaskedInputPort("frame", ImageFormat.create(3));
    }

    public void updateRenderMode() {
        if (this.mRenderModeString != null) {
            if (this.mRenderModeString.equals("stretch")) {
                this.mRenderMode = 0;
            } else if (this.mRenderModeString.equals("fit")) {
                this.mRenderMode = 1;
            } else if (this.mRenderModeString.equals("fill_crop")) {
                this.mRenderMode = 2;
            } else {
                throw new RuntimeException("Unknown render mode '" + this.mRenderModeString + "'!");
            }
        }
        updateTargetRect();
    }

    public void prepare(FilterContext context) {
        this.mProgram = ShaderProgram.createIdentity(context);
        this.mProgram.setSourceRect(0.0f, Engine.DEFAULT_VOLUME, Engine.DEFAULT_VOLUME, ScaledLayoutParams.SCALE_UNSPECIFIED);
        this.mProgram.setClearsOutput(true);
        this.mProgram.setClearColor(0.0f, 0.0f, 0.0f);
        updateRenderMode();
        this.mScreen = (GLFrame) context.getFrameManager().newBoundFrame(ImageFormat.create(this.mSurfaceView.getWidth(), this.mSurfaceView.getHeight(), 3, 3), HwSysResource.MAINSERVICES, 0);
    }

    public void open(FilterContext context) {
        this.mSurfaceView.unbind();
        this.mSurfaceView.bindToListener(this, context.getGLEnvironment());
    }

    public void process(FilterContext context) {
        if (this.mIsBound) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Starting frame processing");
            }
            GLEnvironment glEnv = this.mSurfaceView.getGLEnv();
            if (glEnv != context.getGLEnvironment()) {
                throw new RuntimeException("Surface created under different GLEnvironment!");
            }
            Frame gpuFrame;
            Frame input = pullInput("frame");
            boolean createdFrame = false;
            float currentAspectRatio = ((float) input.getFormat().getWidth()) / ((float) input.getFormat().getHeight());
            if (currentAspectRatio != this.mAspectRatio) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "New aspect ratio: " + currentAspectRatio + ", previously: " + this.mAspectRatio);
                }
                this.mAspectRatio = currentAspectRatio;
                updateTargetRect();
            }
            if (this.mLogVerbose) {
                Log.v(TAG, "Got input format: " + input.getFormat());
            }
            if (input.getFormat().getTarget() != 3) {
                gpuFrame = context.getFrameManager().duplicateFrameToTarget(input, 3);
                createdFrame = true;
            } else {
                gpuFrame = input;
            }
            glEnv.activateSurfaceWithId(this.mSurfaceView.getSurfaceId());
            this.mProgram.process(gpuFrame, this.mScreen);
            glEnv.swapBuffers();
            if (createdFrame) {
                gpuFrame.release();
            }
            return;
        }
        Log.w(TAG, this + ": Ignoring frame as there is no surface to render to!");
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        updateTargetRect();
    }

    public void close(FilterContext context) {
        this.mSurfaceView.unbind();
    }

    public void tearDown(FilterContext context) {
        if (this.mScreen != null) {
            this.mScreen.release();
        }
    }

    public synchronized void surfaceCreated(SurfaceHolder holder) {
        this.mIsBound = true;
    }

    public synchronized void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (this.mScreen != null) {
            this.mScreenWidth = width;
            this.mScreenHeight = height;
            this.mScreen.setViewport(0, 0, this.mScreenWidth, this.mScreenHeight);
            updateTargetRect();
        }
    }

    public synchronized void surfaceDestroyed(SurfaceHolder holder) {
        this.mIsBound = false;
    }

    private void updateTargetRect() {
        if (this.mScreenWidth > 0 && this.mScreenHeight > 0 && this.mProgram != null) {
            float relativeAspectRatio = (((float) this.mScreenWidth) / ((float) this.mScreenHeight)) / this.mAspectRatio;
            switch (this.mRenderMode) {
                case TextToSpeech.SUCCESS /*0*/:
                    this.mProgram.setTargetRect(0.0f, 0.0f, Engine.DEFAULT_VOLUME, Engine.DEFAULT_VOLUME);
                case AudioState.ROUTE_EARPIECE /*1*/:
                    if (relativeAspectRatio > Engine.DEFAULT_VOLUME) {
                        this.mProgram.setTargetRect(NetworkHistoryUtils.RECOVERY_PERCENTAGE - (NetworkHistoryUtils.RECOVERY_PERCENTAGE / relativeAspectRatio), 0.0f, Engine.DEFAULT_VOLUME / relativeAspectRatio, Engine.DEFAULT_VOLUME);
                    } else {
                        this.mProgram.setTargetRect(0.0f, NetworkHistoryUtils.RECOVERY_PERCENTAGE - (NetworkHistoryUtils.RECOVERY_PERCENTAGE * relativeAspectRatio), Engine.DEFAULT_VOLUME, relativeAspectRatio);
                    }
                case AudioState.ROUTE_BLUETOOTH /*2*/:
                    if (relativeAspectRatio > Engine.DEFAULT_VOLUME) {
                        this.mProgram.setTargetRect(0.0f, NetworkHistoryUtils.RECOVERY_PERCENTAGE - (NetworkHistoryUtils.RECOVERY_PERCENTAGE * relativeAspectRatio), Engine.DEFAULT_VOLUME, relativeAspectRatio);
                    } else {
                        this.mProgram.setTargetRect(NetworkHistoryUtils.RECOVERY_PERCENTAGE - (NetworkHistoryUtils.RECOVERY_PERCENTAGE / relativeAspectRatio), 0.0f, Engine.DEFAULT_VOLUME / relativeAspectRatio, Engine.DEFAULT_VOLUME);
                    }
                default:
            }
        }
    }
}
