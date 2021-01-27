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
import android.util.Log;
import android.view.SurfaceHolder;

public class SurfaceRenderFilter extends Filter implements SurfaceHolder.Callback {
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

    @Override // android.filterfw.core.Filter
    public void setupPorts() {
        if (this.mSurfaceView != null) {
            addMaskedInputPort("frame", ImageFormat.create(3));
            return;
        }
        throw new RuntimeException("NULL SurfaceView passed to SurfaceRenderFilter");
    }

    public void updateRenderMode() {
        String str = this.mRenderModeString;
        if (str != null) {
            if (str.equals("stretch")) {
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

    @Override // android.filterfw.core.Filter
    public void prepare(FilterContext context) {
        this.mProgram = ShaderProgram.createIdentity(context);
        this.mProgram.setSourceRect(0.0f, 1.0f, 1.0f, -1.0f);
        this.mProgram.setClearsOutput(true);
        this.mProgram.setClearColor(0.0f, 0.0f, 0.0f);
        updateRenderMode();
        this.mScreen = (GLFrame) context.getFrameManager().newBoundFrame(ImageFormat.create(this.mSurfaceView.getWidth(), this.mSurfaceView.getHeight(), 3, 3), 101, 0);
    }

    @Override // android.filterfw.core.Filter
    public void open(FilterContext context) {
        this.mSurfaceView.unbind();
        this.mSurfaceView.bindToListener(this, context.getGLEnvironment());
    }

    @Override // android.filterfw.core.Filter
    public void process(FilterContext context) {
        Frame gpuFrame;
        if (!this.mIsBound) {
            Log.w(TAG, this + ": Ignoring frame as there is no surface to render to!");
            return;
        }
        if (this.mLogVerbose) {
            Log.v(TAG, "Starting frame processing");
        }
        GLEnvironment glEnv = this.mSurfaceView.getGLEnv();
        if (glEnv == context.getGLEnvironment()) {
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
                return;
            }
            return;
        }
        throw new RuntimeException("Surface created under different GLEnvironment!");
    }

    @Override // android.filterfw.core.Filter
    public void fieldPortValueUpdated(String name, FilterContext context) {
        updateTargetRect();
    }

    @Override // android.filterfw.core.Filter
    public void close(FilterContext context) {
        this.mSurfaceView.unbind();
    }

    @Override // android.filterfw.core.Filter
    public void tearDown(FilterContext context) {
        GLFrame gLFrame = this.mScreen;
        if (gLFrame != null) {
            gLFrame.release();
        }
    }

    @Override // android.view.SurfaceHolder.Callback
    public synchronized void surfaceCreated(SurfaceHolder holder) {
        this.mIsBound = true;
    }

    @Override // android.view.SurfaceHolder.Callback
    public synchronized void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (this.mScreen != null) {
            this.mScreenWidth = width;
            this.mScreenHeight = height;
            this.mScreen.setViewport(0, 0, this.mScreenWidth, this.mScreenHeight);
            updateTargetRect();
        }
    }

    @Override // android.view.SurfaceHolder.Callback
    public synchronized void surfaceDestroyed(SurfaceHolder holder) {
        this.mIsBound = false;
    }

    private void updateTargetRect() {
        int i;
        ShaderProgram shaderProgram;
        int i2 = this.mScreenWidth;
        if (i2 > 0 && (i = this.mScreenHeight) > 0 && (shaderProgram = this.mProgram) != null) {
            float relativeAspectRatio = (((float) i2) / ((float) i)) / this.mAspectRatio;
            int i3 = this.mRenderMode;
            if (i3 == 0) {
                shaderProgram.setTargetRect(0.0f, 0.0f, 1.0f, 1.0f);
            } else if (i3 != 1) {
                if (i3 == 2) {
                    if (relativeAspectRatio > 1.0f) {
                        shaderProgram.setTargetRect(0.0f, 0.5f - (relativeAspectRatio * 0.5f), 1.0f, relativeAspectRatio);
                    } else {
                        shaderProgram.setTargetRect(0.5f - (0.5f / relativeAspectRatio), 0.0f, 1.0f / relativeAspectRatio, 1.0f);
                    }
                }
            } else if (relativeAspectRatio > 1.0f) {
                shaderProgram.setTargetRect(0.5f - (0.5f / relativeAspectRatio), 0.0f, 1.0f / relativeAspectRatio, 1.0f);
            } else {
                shaderProgram.setTargetRect(0.0f, 0.5f - (relativeAspectRatio * 0.5f), 1.0f, relativeAspectRatio);
            }
        }
    }
}
