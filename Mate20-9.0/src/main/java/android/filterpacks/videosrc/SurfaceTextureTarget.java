package android.filterpacks.videosrc;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.GLEnvironment;
import android.filterfw.core.GLFrame;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.GenerateFinalPort;
import android.filterfw.core.MutableFrameFormat;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.filterfw.geometry.Point;
import android.filterfw.geometry.Quad;
import android.graphics.SurfaceTexture;
import android.util.Log;

public class SurfaceTextureTarget extends Filter {
    private static final String TAG = "SurfaceTextureTarget";
    private final int RENDERMODE_CUSTOMIZE = 3;
    private final int RENDERMODE_FILL_CROP = 2;
    private final int RENDERMODE_FIT = 1;
    private final int RENDERMODE_STRETCH = 0;
    private float mAspectRatio = 1.0f;
    private boolean mLogVerbose = Log.isLoggable(TAG, 2);
    private ShaderProgram mProgram;
    private int mRenderMode = 1;
    @GenerateFieldPort(hasDefault = true, name = "renderMode")
    private String mRenderModeString;
    private GLFrame mScreen;
    @GenerateFinalPort(name = "height")
    private int mScreenHeight;
    @GenerateFinalPort(name = "width")
    private int mScreenWidth;
    @GenerateFieldPort(hasDefault = true, name = "sourceQuad")
    private Quad mSourceQuad = new Quad(new Point(0.0f, 1.0f), new Point(1.0f, 1.0f), new Point(0.0f, 0.0f), new Point(1.0f, 0.0f));
    private int mSurfaceId;
    @GenerateFinalPort(name = "surfaceTexture")
    private SurfaceTexture mSurfaceTexture;
    @GenerateFieldPort(hasDefault = true, name = "targetQuad")
    private Quad mTargetQuad = new Quad(new Point(0.0f, 0.0f), new Point(1.0f, 0.0f), new Point(0.0f, 1.0f), new Point(1.0f, 1.0f));

    public SurfaceTextureTarget(String name) {
        super(name);
    }

    public synchronized void setupPorts() {
        if (this.mSurfaceTexture != null) {
            addMaskedInputPort("frame", ImageFormat.create(3));
        } else {
            throw new RuntimeException("Null SurfaceTexture passed to SurfaceTextureTarget");
        }
    }

    public void updateRenderMode() {
        if (this.mLogVerbose) {
            Log.v(TAG, "updateRenderMode. Thread: " + Thread.currentThread());
        }
        if (this.mRenderModeString != null) {
            if (this.mRenderModeString.equals("stretch")) {
                this.mRenderMode = 0;
            } else if (this.mRenderModeString.equals("fit")) {
                this.mRenderMode = 1;
            } else if (this.mRenderModeString.equals("fill_crop")) {
                this.mRenderMode = 2;
            } else if (this.mRenderModeString.equals("customize")) {
                this.mRenderMode = 3;
            } else {
                throw new RuntimeException("Unknown render mode '" + this.mRenderModeString + "'!");
            }
        }
        updateTargetRect();
    }

    public void prepare(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Prepare. Thread: " + Thread.currentThread());
        }
        this.mProgram = ShaderProgram.createIdentity(context);
        this.mProgram.setSourceRect(0.0f, 1.0f, 1.0f, -1.0f);
        this.mProgram.setClearColor(0.0f, 0.0f, 0.0f);
        updateRenderMode();
        MutableFrameFormat screenFormat = new MutableFrameFormat(2, 3);
        screenFormat.setBytesPerSample(4);
        screenFormat.setDimensions(this.mScreenWidth, this.mScreenHeight);
        this.mScreen = (GLFrame) context.getFrameManager().newBoundFrame(screenFormat, 101, 0);
    }

    public synchronized void open(FilterContext context) {
        if (this.mSurfaceTexture != null) {
            this.mSurfaceId = context.getGLEnvironment().registerSurfaceTexture(this.mSurfaceTexture, this.mScreenWidth, this.mScreenHeight);
            if (this.mSurfaceId <= 0) {
                throw new RuntimeException("Could not register SurfaceTexture: " + this.mSurfaceTexture);
            }
        } else {
            Log.e(TAG, "SurfaceTexture is null!!");
            throw new RuntimeException("Could not register SurfaceTexture: " + this.mSurfaceTexture);
        }
    }

    public synchronized void close(FilterContext context) {
        if (this.mSurfaceId > 0) {
            context.getGLEnvironment().unregisterSurfaceId(this.mSurfaceId);
            this.mSurfaceId = -1;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002d, code lost:
        return;
     */
    public synchronized void disconnect(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "disconnect");
        }
        if (this.mSurfaceTexture == null) {
            Log.d(TAG, "SurfaceTexture is already null. Nothing to disconnect.");
            return;
        }
        this.mSurfaceTexture = null;
        if (this.mSurfaceId > 0) {
            context.getGLEnvironment().unregisterSurfaceId(this.mSurfaceId);
            this.mSurfaceId = -1;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0095, code lost:
        return;
     */
    public synchronized void process(FilterContext context) {
        Frame gpuFrame;
        if (this.mSurfaceId > 0) {
            GLEnvironment glEnv = context.getGLEnvironment();
            Frame input = pullInput("frame");
            boolean createdFrame = false;
            float currentAspectRatio = ((float) input.getFormat().getWidth()) / ((float) input.getFormat().getHeight());
            if (currentAspectRatio != this.mAspectRatio) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Process. New aspect ratio: " + currentAspectRatio + ", previously: " + this.mAspectRatio + ". Thread: " + Thread.currentThread());
                }
                this.mAspectRatio = currentAspectRatio;
                updateTargetRect();
            }
            if (input.getFormat().getTarget() != 3) {
                gpuFrame = context.getFrameManager().duplicateFrameToTarget(input, 3);
                createdFrame = true;
            } else {
                gpuFrame = input;
            }
            glEnv.activateSurfaceWithId(this.mSurfaceId);
            this.mProgram.process(gpuFrame, (Frame) this.mScreen);
            glEnv.setSurfaceTimestamp(input.getTimestamp());
            glEnv.swapBuffers();
            if (createdFrame) {
                gpuFrame.release();
            }
        }
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "FPVU. Thread: " + Thread.currentThread());
        }
        updateRenderMode();
    }

    public void tearDown(FilterContext context) {
        if (this.mScreen != null) {
            this.mScreen.release();
        }
    }

    private void updateTargetRect() {
        if (this.mLogVerbose) {
            Log.v(TAG, "updateTargetRect. Thread: " + Thread.currentThread());
        }
        if (this.mScreenWidth > 0 && this.mScreenHeight > 0 && this.mProgram != null) {
            float screenAspectRatio = ((float) this.mScreenWidth) / ((float) this.mScreenHeight);
            float relativeAspectRatio = screenAspectRatio / this.mAspectRatio;
            if (this.mLogVerbose) {
                Log.v(TAG, "UTR. screen w = " + ((float) this.mScreenWidth) + " x screen h = " + ((float) this.mScreenHeight) + " Screen AR: " + screenAspectRatio + ", frame AR: " + this.mAspectRatio + ", relative AR: " + relativeAspectRatio);
            }
            if (relativeAspectRatio != 1.0f || this.mRenderMode == 3) {
                switch (this.mRenderMode) {
                    case 0:
                        this.mTargetQuad.p0.set(0.0f, 0.0f);
                        this.mTargetQuad.p1.set(1.0f, 0.0f);
                        this.mTargetQuad.p2.set(0.0f, 1.0f);
                        this.mTargetQuad.p3.set(1.0f, 1.0f);
                        this.mProgram.setClearsOutput(false);
                        break;
                    case 1:
                        if (relativeAspectRatio > 1.0f) {
                            this.mTargetQuad.p0.set(0.5f - (0.5f / relativeAspectRatio), 0.0f);
                            this.mTargetQuad.p1.set((0.5f / relativeAspectRatio) + 0.5f, 0.0f);
                            this.mTargetQuad.p2.set(0.5f - (0.5f / relativeAspectRatio), 1.0f);
                            this.mTargetQuad.p3.set(0.5f + (0.5f / relativeAspectRatio), 1.0f);
                        } else {
                            this.mTargetQuad.p0.set(0.0f, 0.5f - (0.5f * relativeAspectRatio));
                            this.mTargetQuad.p1.set(1.0f, 0.5f - (0.5f * relativeAspectRatio));
                            this.mTargetQuad.p2.set(0.0f, (0.5f * relativeAspectRatio) + 0.5f);
                            this.mTargetQuad.p3.set(1.0f, 0.5f + (0.5f * relativeAspectRatio));
                        }
                        this.mProgram.setClearsOutput(true);
                        break;
                    case 2:
                        if (relativeAspectRatio > 1.0f) {
                            this.mTargetQuad.p0.set(0.0f, 0.5f - (0.5f * relativeAspectRatio));
                            this.mTargetQuad.p1.set(1.0f, 0.5f - (0.5f * relativeAspectRatio));
                            this.mTargetQuad.p2.set(0.0f, (0.5f * relativeAspectRatio) + 0.5f);
                            this.mTargetQuad.p3.set(1.0f, 0.5f + (0.5f * relativeAspectRatio));
                        } else {
                            this.mTargetQuad.p0.set(0.5f - (0.5f / relativeAspectRatio), 0.0f);
                            this.mTargetQuad.p1.set((0.5f / relativeAspectRatio) + 0.5f, 0.0f);
                            this.mTargetQuad.p2.set(0.5f - (0.5f / relativeAspectRatio), 1.0f);
                            this.mTargetQuad.p3.set(0.5f + (0.5f / relativeAspectRatio), 1.0f);
                        }
                        this.mProgram.setClearsOutput(true);
                        break;
                    case 3:
                        this.mProgram.setSourceRegion(this.mSourceQuad);
                        break;
                }
                if (this.mLogVerbose) {
                    Log.v(TAG, "UTR. quad: " + this.mTargetQuad);
                }
                this.mProgram.setTargetRegion(this.mTargetQuad);
                return;
            }
            this.mProgram.setTargetRect(0.0f, 0.0f, 1.0f, 1.0f);
            this.mProgram.setClearsOutput(false);
        }
    }
}
