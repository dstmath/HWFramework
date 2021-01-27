package android.filterpacks.ui;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.GLEnvironment;
import android.filterfw.core.GLFrame;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.GenerateFinalPort;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.util.Log;
import android.view.Surface;

public class SurfaceTargetFilter extends Filter {
    private static final String TAG = "SurfaceRenderFilter";
    private final int RENDERMODE_FILL_CROP = 2;
    private final int RENDERMODE_FIT = 1;
    private final int RENDERMODE_STRETCH = 0;
    private float mAspectRatio = 1.0f;
    private GLEnvironment mGlEnv;
    private boolean mLogVerbose = Log.isLoggable(TAG, 2);
    private ShaderProgram mProgram;
    private int mRenderMode = 1;
    @GenerateFieldPort(hasDefault = true, name = "renderMode")
    private String mRenderModeString;
    private GLFrame mScreen;
    @GenerateFieldPort(name = "oheight")
    private int mScreenHeight;
    @GenerateFieldPort(name = "owidth")
    private int mScreenWidth;
    @GenerateFinalPort(name = "surface")
    private Surface mSurface;
    private int mSurfaceId = -1;

    public SurfaceTargetFilter(String name) {
        super(name);
    }

    @Override // android.filterfw.core.Filter
    public void setupPorts() {
        if (this.mSurface != null) {
            addMaskedInputPort("frame", ImageFormat.create(3));
            return;
        }
        throw new RuntimeException("NULL Surface passed to SurfaceTargetFilter");
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
        this.mGlEnv = context.getGLEnvironment();
        this.mProgram = ShaderProgram.createIdentity(context);
        this.mProgram.setSourceRect(0.0f, 1.0f, 1.0f, -1.0f);
        this.mProgram.setClearsOutput(true);
        this.mProgram.setClearColor(0.0f, 0.0f, 0.0f);
        this.mScreen = (GLFrame) context.getFrameManager().newBoundFrame(ImageFormat.create(this.mScreenWidth, this.mScreenHeight, 3, 3), 101, 0);
        updateRenderMode();
    }

    @Override // android.filterfw.core.Filter
    public void open(FilterContext context) {
        registerSurface();
    }

    @Override // android.filterfw.core.Filter
    public void process(FilterContext context) {
        Frame gpuFrame;
        if (this.mLogVerbose) {
            Log.v(TAG, "Starting frame processing");
        }
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
        this.mGlEnv.activateSurfaceWithId(this.mSurfaceId);
        this.mProgram.process(gpuFrame, this.mScreen);
        this.mGlEnv.swapBuffers();
        if (createdFrame) {
            gpuFrame.release();
        }
    }

    @Override // android.filterfw.core.Filter
    public void fieldPortValueUpdated(String name, FilterContext context) {
        this.mScreen.setViewport(0, 0, this.mScreenWidth, this.mScreenHeight);
        updateTargetRect();
    }

    @Override // android.filterfw.core.Filter
    public void close(FilterContext context) {
        unregisterSurface();
    }

    @Override // android.filterfw.core.Filter
    public void tearDown(FilterContext context) {
        GLFrame gLFrame = this.mScreen;
        if (gLFrame != null) {
            gLFrame.release();
        }
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

    private void registerSurface() {
        this.mSurfaceId = this.mGlEnv.registerSurface(this.mSurface);
        if (this.mSurfaceId < 0) {
            throw new RuntimeException("Could not register Surface: " + this.mSurface);
        }
    }

    private void unregisterSurface() {
        int i = this.mSurfaceId;
        if (i > 0) {
            this.mGlEnv.unregisterSurfaceId(i);
        }
    }
}
