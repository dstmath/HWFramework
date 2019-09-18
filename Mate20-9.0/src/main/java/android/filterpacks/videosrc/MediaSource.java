package android.filterpacks.videosrc;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
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
import android.media.MediaCodec;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;
import java.io.IOException;

public class MediaSource extends Filter {
    private static final int NEWFRAME_TIMEOUT = 100;
    private static final int NEWFRAME_TIMEOUT_REPEAT = 10;
    private static final int PREP_TIMEOUT = 100;
    private static final int PREP_TIMEOUT_REPEAT = 100;
    private static final String TAG = "MediaSource";
    private static final float[] mSourceCoords_0 = {1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] mSourceCoords_180 = {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f};
    private static final float[] mSourceCoords_270 = {0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] mSourceCoords_90 = {1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f};
    /* access modifiers changed from: private */
    public boolean mCompleted;
    @GenerateFieldPort(hasDefault = true, name = "context")
    private Context mContext = null;
    private ShaderProgram mFrameExtractor;
    private final String mFrameShader = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n";
    /* access modifiers changed from: private */
    public boolean mGotSize;
    /* access modifiers changed from: private */
    public int mHeight;
    /* access modifiers changed from: private */
    public final boolean mLogVerbose = Log.isLoggable(TAG, 2);
    @GenerateFieldPort(hasDefault = true, name = "loop")
    private boolean mLooping = true;
    private GLFrame mMediaFrame;
    private MediaPlayer mMediaPlayer;
    /* access modifiers changed from: private */
    public boolean mNewFrameAvailable = false;
    /* access modifiers changed from: private */
    @GenerateFieldPort(hasDefault = true, name = "orientation")
    public int mOrientation = 0;
    private boolean mOrientationUpdated;
    /* access modifiers changed from: private */
    public MutableFrameFormat mOutputFormat;
    private boolean mPaused;
    private boolean mPlaying;
    /* access modifiers changed from: private */
    public boolean mPrepared;
    @GenerateFieldPort(hasDefault = true, name = "sourceIsUrl")
    private boolean mSelectedIsUrl = false;
    @GenerateFieldPort(hasDefault = true, name = "sourceAsset")
    private AssetFileDescriptor mSourceAsset = null;
    @GenerateFieldPort(hasDefault = true, name = "sourceUrl")
    private String mSourceUrl = "";
    private SurfaceTexture mSurfaceTexture;
    @GenerateFieldPort(hasDefault = true, name = "volume")
    private float mVolume = 0.0f;
    @GenerateFinalPort(hasDefault = true, name = "waitForNewFrame")
    private boolean mWaitForNewFrame = true;
    /* access modifiers changed from: private */
    public int mWidth;
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            if (MediaSource.this.mLogVerbose) {
                Log.v(MediaSource.TAG, "MediaPlayer has completed playback");
            }
            synchronized (MediaSource.this) {
                boolean unused = MediaSource.this.mCompleted = true;
            }
        }
    };
    private SurfaceTexture.OnFrameAvailableListener onMediaFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            if (MediaSource.this.mLogVerbose) {
                Log.v(MediaSource.TAG, "New frame from media player");
            }
            synchronized (MediaSource.this) {
                if (MediaSource.this.mLogVerbose) {
                    Log.v(MediaSource.TAG, "New frame: notify");
                }
                boolean unused = MediaSource.this.mNewFrameAvailable = true;
                MediaSource.this.notify();
                if (MediaSource.this.mLogVerbose) {
                    Log.v(MediaSource.TAG, "New frame: notify done");
                }
            }
        }
    };
    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            if (MediaSource.this.mLogVerbose) {
                Log.v(MediaSource.TAG, "MediaPlayer is prepared");
            }
            synchronized (MediaSource.this) {
                boolean unused = MediaSource.this.mPrepared = true;
                MediaSource.this.notify();
            }
        }
    };
    private MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            if (MediaSource.this.mLogVerbose) {
                Log.v(MediaSource.TAG, "MediaPlayer sent dimensions: " + width + " x " + height);
            }
            if (!MediaSource.this.mGotSize) {
                if (MediaSource.this.mOrientation == 0 || MediaSource.this.mOrientation == 180) {
                    MediaSource.this.mOutputFormat.setDimensions(width, height);
                } else {
                    MediaSource.this.mOutputFormat.setDimensions(height, width);
                }
                int unused = MediaSource.this.mWidth = width;
                int unused2 = MediaSource.this.mHeight = height;
            } else if (!(MediaSource.this.mOutputFormat.getWidth() == width && MediaSource.this.mOutputFormat.getHeight() == height)) {
                Log.e(MediaSource.TAG, "Multiple video size change events received!");
            }
            synchronized (MediaSource.this) {
                boolean unused3 = MediaSource.this.mGotSize = true;
                MediaSource.this.notify();
            }
        }
    };

    public MediaSource(String name) {
        super(name);
    }

    public void setupPorts() {
        addOutputPort(MediaCodec.MetricsConstants.MODE_VIDEO, ImageFormat.create(3, 3));
    }

    private void createFormats() {
        this.mOutputFormat = ImageFormat.create(3, 3);
    }

    /* access modifiers changed from: protected */
    public void prepare(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Preparing MediaSource");
        }
        this.mFrameExtractor = new ShaderProgram(context, "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nuniform samplerExternalOES tex_sampler_0;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler_0, v_texcoord);\n}\n");
        this.mFrameExtractor.setSourceRect(0.0f, 1.0f, 1.0f, -1.0f);
        createFormats();
    }

    public void open(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Opening MediaSource");
            if (this.mSelectedIsUrl) {
                Log.v(TAG, "Current URL is " + this.mSourceUrl);
            } else {
                Log.v(TAG, "Current source is Asset!");
            }
        }
        this.mMediaFrame = (GLFrame) context.getFrameManager().newBoundFrame(this.mOutputFormat, 104, 0);
        this.mSurfaceTexture = new SurfaceTexture(this.mMediaFrame.getTextureId());
        if (!setupMediaPlayer(this.mSelectedIsUrl)) {
            throw new RuntimeException("Error setting up MediaPlayer!");
        }
    }

    public void process(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Processing new frame");
        }
        if (this.mMediaPlayer == null) {
            throw new NullPointerException("Unexpected null media player!");
        } else if (this.mCompleted) {
            closeOutputPort(MediaCodec.MetricsConstants.MODE_VIDEO);
        } else {
            if (!this.mPlaying) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Waiting for preparation to complete");
                }
                int waitCount = 0;
                do {
                    if (this.mGotSize == 0 || !this.mPrepared) {
                        try {
                            wait(100);
                        } catch (InterruptedException e) {
                        }
                        if (this.mCompleted) {
                            closeOutputPort(MediaCodec.MetricsConstants.MODE_VIDEO);
                            return;
                        }
                        waitCount++;
                    } else {
                        if (this.mLogVerbose) {
                            Log.v(TAG, "Starting playback");
                        }
                        this.mMediaPlayer.start();
                    }
                } while (waitCount != 100);
                this.mMediaPlayer.release();
                throw new RuntimeException("MediaPlayer timed out while preparing!");
            }
            if (!this.mPaused || !this.mPlaying) {
                if (this.mWaitForNewFrame) {
                    if (this.mLogVerbose) {
                        Log.v(TAG, "Waiting for new frame");
                    }
                    int waitCount2 = 0;
                    while (true) {
                        int waitCount3 = waitCount2;
                        if (this.mNewFrameAvailable != 0) {
                            this.mNewFrameAvailable = false;
                            if (this.mLogVerbose) {
                                Log.v(TAG, "Got new frame");
                            }
                        } else if (waitCount3 != 10) {
                            try {
                                wait(100);
                            } catch (InterruptedException e2) {
                                InterruptedException interruptedException = e2;
                                if (this.mLogVerbose) {
                                    Log.v(TAG, "interrupted");
                                }
                            }
                            waitCount2 = waitCount3 + 1;
                        } else if (this.mCompleted) {
                            closeOutputPort(MediaCodec.MetricsConstants.MODE_VIDEO);
                            return;
                        } else {
                            throw new RuntimeException("Timeout waiting for new frame!");
                        }
                    }
                }
                this.mSurfaceTexture.updateTexImage();
                this.mOrientationUpdated = true;
            }
            if (this.mOrientationUpdated) {
                float[] surfaceTransform = new float[16];
                this.mSurfaceTexture.getTransformMatrix(surfaceTransform);
                float[] sourceCoords = new float[16];
                int i = this.mOrientation;
                if (i == 90) {
                    Matrix.multiplyMM(sourceCoords, 0, surfaceTransform, 0, mSourceCoords_90, 0);
                } else if (i == 180) {
                    Matrix.multiplyMM(sourceCoords, 0, surfaceTransform, 0, mSourceCoords_180, 0);
                } else if (i != 270) {
                    Matrix.multiplyMM(sourceCoords, 0, surfaceTransform, 0, mSourceCoords_0, 0);
                } else {
                    Matrix.multiplyMM(sourceCoords, 0, surfaceTransform, 0, mSourceCoords_270, 0);
                }
                if (this.mLogVerbose) {
                    Log.v(TAG, "OrientationHint = " + this.mOrientation);
                    Log.v(TAG, String.format("SetSourceRegion: %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f", new Object[]{Float.valueOf(sourceCoords[4]), Float.valueOf(sourceCoords[5]), Float.valueOf(sourceCoords[0]), Float.valueOf(sourceCoords[1]), Float.valueOf(sourceCoords[12]), Float.valueOf(sourceCoords[13]), Float.valueOf(sourceCoords[8]), Float.valueOf(sourceCoords[9])}));
                }
                this.mFrameExtractor.setSourceRegion(sourceCoords[4], sourceCoords[5], sourceCoords[0], sourceCoords[1], sourceCoords[12], sourceCoords[13], sourceCoords[8], sourceCoords[9]);
                this.mOrientationUpdated = false;
            }
            Frame output = context.getFrameManager().newFrame(this.mOutputFormat);
            this.mFrameExtractor.process((Frame) this.mMediaFrame, output);
            long timestamp = this.mSurfaceTexture.getTimestamp();
            if (this.mLogVerbose) {
                Log.v(TAG, "Timestamp: " + (((double) timestamp) / 1.0E9d) + " s");
            }
            output.setTimestamp(timestamp);
            pushOutput(MediaCodec.MetricsConstants.MODE_VIDEO, output);
            output.release();
            this.mPlaying = true;
        }
    }

    public void close(FilterContext context) {
        if (this.mMediaPlayer.isPlaying()) {
            this.mMediaPlayer.stop();
        }
        this.mPrepared = false;
        this.mGotSize = false;
        this.mPlaying = false;
        this.mPaused = false;
        this.mCompleted = false;
        this.mNewFrameAvailable = false;
        this.mMediaPlayer.release();
        this.mMediaPlayer = null;
        this.mSurfaceTexture.release();
        this.mSurfaceTexture = null;
        if (this.mLogVerbose) {
            Log.v(TAG, "MediaSource closed");
        }
    }

    public void tearDown(FilterContext context) {
        if (this.mMediaFrame != null) {
            this.mMediaFrame.release();
        }
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Parameter update");
        }
        if (name.equals("sourceUrl")) {
            if (isOpen()) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Opening new source URL");
                }
                if (this.mSelectedIsUrl) {
                    setupMediaPlayer(this.mSelectedIsUrl);
                }
            }
        } else if (name.equals("sourceAsset")) {
            if (isOpen()) {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Opening new source FD");
                }
                if (!this.mSelectedIsUrl) {
                    setupMediaPlayer(this.mSelectedIsUrl);
                }
            }
        } else if (name.equals("loop")) {
            if (isOpen()) {
                this.mMediaPlayer.setLooping(this.mLooping);
            }
        } else if (name.equals("sourceIsUrl")) {
            if (isOpen()) {
                if (this.mSelectedIsUrl) {
                    if (this.mLogVerbose) {
                        Log.v(TAG, "Opening new source URL");
                    }
                } else if (this.mLogVerbose) {
                    Log.v(TAG, "Opening new source Asset");
                }
                setupMediaPlayer(this.mSelectedIsUrl);
            }
        } else if (name.equals("volume")) {
            if (isOpen()) {
                this.mMediaPlayer.setVolume(this.mVolume, this.mVolume);
            }
        } else if (name.equals("orientation") && this.mGotSize) {
            if (this.mOrientation == 0 || this.mOrientation == 180) {
                this.mOutputFormat.setDimensions(this.mWidth, this.mHeight);
            } else {
                this.mOutputFormat.setDimensions(this.mHeight, this.mWidth);
            }
            this.mOrientationUpdated = true;
        }
    }

    public synchronized void pauseVideo(boolean pauseState) {
        if (isOpen()) {
            if (pauseState && !this.mPaused) {
                this.mMediaPlayer.pause();
            } else if (!pauseState && this.mPaused) {
                this.mMediaPlayer.start();
            }
        }
        this.mPaused = pauseState;
    }

    private synchronized boolean setupMediaPlayer(boolean useUrl) {
        this.mPrepared = false;
        this.mGotSize = false;
        this.mPlaying = false;
        this.mPaused = false;
        this.mCompleted = false;
        this.mNewFrameAvailable = false;
        if (this.mLogVerbose) {
            Log.v(TAG, "Setting up playback.");
        }
        if (this.mMediaPlayer != null) {
            if (this.mLogVerbose) {
                Log.v(TAG, "Resetting existing MediaPlayer.");
            }
            this.mMediaPlayer.reset();
        } else {
            if (this.mLogVerbose) {
                Log.v(TAG, "Creating new MediaPlayer.");
            }
            this.mMediaPlayer = new MediaPlayer();
        }
        if (this.mMediaPlayer != null) {
            if (useUrl) {
                try {
                    if (this.mLogVerbose) {
                        Log.v(TAG, "Setting MediaPlayer source to URI " + this.mSourceUrl);
                    }
                    if (this.mContext == null) {
                        this.mMediaPlayer.setDataSource(this.mSourceUrl);
                    } else {
                        this.mMediaPlayer.setDataSource(this.mContext, Uri.parse(this.mSourceUrl.toString()));
                    }
                } catch (IOException e) {
                    this.mMediaPlayer.release();
                    this.mMediaPlayer = null;
                    if (useUrl) {
                        throw new RuntimeException(String.format("Unable to set MediaPlayer to URL %s!", new Object[]{this.mSourceUrl}), e);
                    }
                    throw new RuntimeException(String.format("Unable to set MediaPlayer to asset %s!", new Object[]{this.mSourceAsset}), e);
                } catch (IllegalArgumentException e2) {
                    this.mMediaPlayer.release();
                    this.mMediaPlayer = null;
                    if (useUrl) {
                        throw new RuntimeException(String.format("Unable to set MediaPlayer to URL %s!", new Object[]{this.mSourceUrl}), e2);
                    }
                    throw new RuntimeException(String.format("Unable to set MediaPlayer to asset %s!", new Object[]{this.mSourceAsset}), e2);
                }
            } else {
                if (this.mLogVerbose) {
                    Log.v(TAG, "Setting MediaPlayer source to asset " + this.mSourceAsset);
                }
                this.mMediaPlayer.setDataSource(this.mSourceAsset.getFileDescriptor(), this.mSourceAsset.getStartOffset(), this.mSourceAsset.getLength());
            }
            this.mMediaPlayer.setLooping(this.mLooping);
            this.mMediaPlayer.setVolume(this.mVolume, this.mVolume);
            Surface surface = new Surface(this.mSurfaceTexture);
            this.mMediaPlayer.setSurface(surface);
            surface.release();
            this.mMediaPlayer.setOnVideoSizeChangedListener(this.onVideoSizeChangedListener);
            this.mMediaPlayer.setOnPreparedListener(this.onPreparedListener);
            this.mMediaPlayer.setOnCompletionListener(this.onCompletionListener);
            this.mSurfaceTexture.setOnFrameAvailableListener(this.onMediaFrameAvailableListener);
            if (this.mLogVerbose) {
                Log.v(TAG, "Preparing MediaPlayer.");
            }
            this.mMediaPlayer.prepareAsync();
        } else {
            throw new RuntimeException("Unable to create a MediaPlayer!");
        }
        return true;
    }
}
