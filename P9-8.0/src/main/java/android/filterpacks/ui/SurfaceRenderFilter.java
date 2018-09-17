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
import android.hardware.camera2.params.TonemapCurve;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

public class SurfaceRenderFilter extends Filter implements Callback {
    private static final String TAG = "SurfaceRenderFilter";
    private final int RENDERMODE_FILL_CROP = 2;
    private final int RENDERMODE_FIT = 1;
    private final int RENDERMODE_STRETCH = 0;
    private float mAspectRatio = 1.0f;
    private boolean mIsBound = false;
    private boolean mLogVerbose = Log.isLoggable(TAG, 2);
    private ShaderProgram mProgram;
    private int mRenderMode = 1;
    @GenerateFieldPort(hasDefault = true, name = "renderMode")
    private String mRenderModeString;
    private GLFrame mScreen;
    private int mScreenHeight;
    private int mScreenWidth;
    @GenerateFinalPort(name = "surfaceView")
    private FilterSurfaceView mSurfaceView;

    public SurfaceRenderFilter(String name) {
        super(name);
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
        this.mProgram.setSourceRect(TonemapCurve.LEVEL_BLACK, 1.0f, 1.0f, -1.0f);
        this.mProgram.setClearsOutput(true);
        this.mProgram.setClearColor(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK);
        updateRenderMode();
        this.mScreen = (GLFrame) context.getFrameManager().newBoundFrame(ImageFormat.create(this.mSurfaceView.getWidth(), this.mSurfaceView.getHeight(), 3, 3), 101, 0);
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
                case 0:
                    this.mProgram.setTargetRect(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, 1.0f, 1.0f);
                    return;
                case 1:
                    if (relativeAspectRatio > 1.0f) {
                        this.mProgram.setTargetRect(0.5f - (0.5f / relativeAspectRatio), TonemapCurve.LEVEL_BLACK, 1.0f / relativeAspectRatio, 1.0f);
                        return;
                    } else {
                        this.mProgram.setTargetRect(TonemapCurve.LEVEL_BLACK, 0.5f - (0.5f * relativeAspectRatio), 1.0f, relativeAspectRatio);
                        return;
                    }
                case 2:
                    if (relativeAspectRatio > 1.0f) {
                        this.mProgram.setTargetRect(TonemapCurve.LEVEL_BLACK, 0.5f - (0.5f * relativeAspectRatio), 1.0f, relativeAspectRatio);
                        return;
                    } else {
                        this.mProgram.setTargetRect(0.5f - (0.5f / relativeAspectRatio), TonemapCurve.LEVEL_BLACK, 1.0f / relativeAspectRatio, 1.0f);
                        return;
                    }
                default:
                    return;
            }
        }
    }
}
