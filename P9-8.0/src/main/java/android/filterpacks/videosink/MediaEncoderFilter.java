package android.filterpacks.videosink;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.Frame;
import android.filterfw.core.GLEnvironment;
import android.filterfw.core.GLFrame;
import android.filterfw.core.GenerateFieldPort;
import android.filterfw.core.MutableFrameFormat;
import android.filterfw.core.ShaderProgram;
import android.filterfw.format.ImageFormat;
import android.filterfw.geometry.Point;
import android.filterfw.geometry.Quad;
import android.hardware.camera2.params.TonemapCurve;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.IOException;

public class MediaEncoderFilter extends Filter {
    private static final int NO_AUDIO_SOURCE = -1;
    private static final String TAG = "MediaEncoderFilter";
    @GenerateFieldPort(hasDefault = true, name = "audioSource")
    private int mAudioSource = -1;
    private boolean mCaptureTimeLapse = false;
    @GenerateFieldPort(hasDefault = true, name = "errorListener")
    private OnErrorListener mErrorListener = null;
    @GenerateFieldPort(hasDefault = true, name = "outputFileDescriptor")
    private FileDescriptor mFd = null;
    @GenerateFieldPort(hasDefault = true, name = "framerate")
    private int mFps = 30;
    @GenerateFieldPort(hasDefault = true, name = "height")
    private int mHeight = 0;
    @GenerateFieldPort(hasDefault = true, name = "infoListener")
    private OnInfoListener mInfoListener = null;
    private long mLastTimeLapseFrameRealTimestampNs = 0;
    private boolean mLogVerbose = Log.isLoggable(TAG, 2);
    @GenerateFieldPort(hasDefault = true, name = "maxDurationMs")
    private int mMaxDurationMs = 0;
    @GenerateFieldPort(hasDefault = true, name = "maxFileSize")
    private long mMaxFileSize = 0;
    private MediaRecorder mMediaRecorder;
    private int mNumFramesEncoded = 0;
    @GenerateFieldPort(hasDefault = true, name = "orientationHint")
    private int mOrientationHint = 0;
    @GenerateFieldPort(hasDefault = true, name = "outputFile")
    private String mOutputFile = new String("/sdcard/MediaEncoderOut.mp4");
    @GenerateFieldPort(hasDefault = true, name = "outputFormat")
    private int mOutputFormat = 2;
    @GenerateFieldPort(hasDefault = true, name = "recordingProfile")
    private CamcorderProfile mProfile = null;
    private ShaderProgram mProgram;
    @GenerateFieldPort(hasDefault = true, name = "recording")
    private boolean mRecording = true;
    private boolean mRecordingActive = false;
    @GenerateFieldPort(hasDefault = true, name = "recordingDoneListener")
    private OnRecordingDoneListener mRecordingDoneListener = null;
    private GLFrame mScreen;
    @GenerateFieldPort(hasDefault = true, name = "inputRegion")
    private Quad mSourceRegion = new Quad(new Point(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK), new Point(1.0f, TonemapCurve.LEVEL_BLACK), new Point(TonemapCurve.LEVEL_BLACK, 1.0f), new Point(1.0f, 1.0f));
    private int mSurfaceId;
    @GenerateFieldPort(hasDefault = true, name = "timelapseRecordingIntervalUs")
    private long mTimeBetweenTimeLapseFrameCaptureUs = 0;
    private long mTimestampNs = 0;
    @GenerateFieldPort(hasDefault = true, name = "videoEncoder")
    private int mVideoEncoder = 2;
    @GenerateFieldPort(hasDefault = true, name = "width")
    private int mWidth = 0;

    public interface OnRecordingDoneListener {
        void onRecordingDone();
    }

    public MediaEncoderFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        addMaskedInputPort("videoframe", ImageFormat.create(3, 3));
    }

    public void fieldPortValueUpdated(String name, FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Port " + name + " has been updated");
        }
        if (!name.equals("recording")) {
            if (name.equals("inputRegion")) {
                if (isOpen()) {
                    updateSourceRegion();
                }
            } else if (isOpen() && this.mRecordingActive) {
                throw new RuntimeException("Cannot change recording parameters when the filter is recording!");
            }
        }
    }

    private void updateSourceRegion() {
        Quad flippedRegion = new Quad();
        flippedRegion.p0 = this.mSourceRegion.p2;
        flippedRegion.p1 = this.mSourceRegion.p3;
        flippedRegion.p2 = this.mSourceRegion.p0;
        flippedRegion.p3 = this.mSourceRegion.p1;
        this.mProgram.setSourceRegion(flippedRegion);
    }

    private void updateMediaRecorderParams() {
        boolean z = false;
        if (this.mTimeBetweenTimeLapseFrameCaptureUs > 0) {
            z = true;
        }
        this.mCaptureTimeLapse = z;
        this.mMediaRecorder.setVideoSource(2);
        if (!(this.mCaptureTimeLapse || this.mAudioSource == -1)) {
            this.mMediaRecorder.setAudioSource(this.mAudioSource);
        }
        if (this.mProfile != null) {
            this.mMediaRecorder.setProfile(this.mProfile);
            this.mFps = this.mProfile.videoFrameRate;
            if (this.mWidth > 0 && this.mHeight > 0) {
                this.mMediaRecorder.setVideoSize(this.mWidth, this.mHeight);
            }
        } else {
            this.mMediaRecorder.setOutputFormat(this.mOutputFormat);
            this.mMediaRecorder.setVideoEncoder(this.mVideoEncoder);
            this.mMediaRecorder.setVideoSize(this.mWidth, this.mHeight);
            this.mMediaRecorder.setVideoFrameRate(this.mFps);
        }
        this.mMediaRecorder.setOrientationHint(this.mOrientationHint);
        this.mMediaRecorder.setOnInfoListener(this.mInfoListener);
        this.mMediaRecorder.setOnErrorListener(this.mErrorListener);
        if (this.mFd != null) {
            this.mMediaRecorder.setOutputFile(this.mFd);
        } else {
            this.mMediaRecorder.setOutputFile(this.mOutputFile);
        }
        try {
            this.mMediaRecorder.setMaxFileSize(this.mMaxFileSize);
        } catch (Exception e) {
            Log.w(TAG, "Setting maxFileSize on MediaRecorder unsuccessful! " + e.getMessage());
        }
        this.mMediaRecorder.setMaxDuration(this.mMaxDurationMs);
    }

    public void prepare(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Preparing");
        }
        this.mProgram = ShaderProgram.createIdentity(context);
        this.mRecordingActive = false;
    }

    public void open(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Opening");
        }
        updateSourceRegion();
        if (this.mRecording) {
            startRecording(context);
        }
    }

    private void startRecording(FilterContext context) {
        int width;
        int height;
        if (this.mLogVerbose) {
            Log.v(TAG, "Starting recording");
        }
        MutableFrameFormat screenFormat = new MutableFrameFormat(2, 3);
        screenFormat.setBytesPerSample(4);
        boolean widthHeightSpecified = this.mWidth > 0 && this.mHeight > 0;
        if (this.mProfile == null || (widthHeightSpecified ^ 1) == 0) {
            width = this.mWidth;
            height = this.mHeight;
        } else {
            width = this.mProfile.videoFrameWidth;
            height = this.mProfile.videoFrameHeight;
        }
        screenFormat.setDimensions(width, height);
        this.mScreen = (GLFrame) context.getFrameManager().newBoundFrame(screenFormat, 101, 0);
        this.mMediaRecorder = new MediaRecorder();
        updateMediaRecorderParams();
        try {
            this.mMediaRecorder.prepare();
            this.mMediaRecorder.start();
            if (this.mLogVerbose) {
                Log.v(TAG, "Open: registering surface from Mediarecorder");
            }
            this.mSurfaceId = context.getGLEnvironment().registerSurfaceFromMediaRecorder(this.mMediaRecorder);
            this.mNumFramesEncoded = 0;
            this.mRecordingActive = true;
        } catch (IllegalStateException e) {
            throw e;
        } catch (IOException e2) {
            throw new RuntimeException("IOException inMediaRecorder.prepare()!", e2);
        } catch (Exception e3) {
            throw new RuntimeException("Unknown Exception inMediaRecorder.prepare()!", e3);
        }
    }

    public boolean skipFrameAndModifyTimestamp(long timestampNs) {
        if (this.mNumFramesEncoded == 0) {
            this.mLastTimeLapseFrameRealTimestampNs = timestampNs;
            this.mTimestampNs = timestampNs;
            if (this.mLogVerbose) {
                Log.v(TAG, "timelapse: FIRST frame, last real t= " + this.mLastTimeLapseFrameRealTimestampNs + ", setting t = " + this.mTimestampNs);
            }
            return false;
        } else if (this.mNumFramesEncoded < 2 || timestampNs >= this.mLastTimeLapseFrameRealTimestampNs + (this.mTimeBetweenTimeLapseFrameCaptureUs * 1000)) {
            if (this.mLogVerbose) {
                Log.v(TAG, "timelapse: encoding frame, Timestamp t = " + timestampNs + ", last real t= " + this.mLastTimeLapseFrameRealTimestampNs + ", interval = " + this.mTimeBetweenTimeLapseFrameCaptureUs);
            }
            this.mLastTimeLapseFrameRealTimestampNs = timestampNs;
            this.mTimestampNs += 1000000000 / ((long) this.mFps);
            if (this.mLogVerbose) {
                Log.v(TAG, "timelapse: encoding frame, setting t = " + this.mTimestampNs + ", delta t = " + (1000000000 / ((long) this.mFps)) + ", fps = " + this.mFps);
            }
            return false;
        } else {
            if (this.mLogVerbose) {
                Log.v(TAG, "timelapse: skipping intermediate frame");
            }
            return true;
        }
    }

    public void process(FilterContext context) {
        GLEnvironment glEnv = context.getGLEnvironment();
        Frame input = pullInput("videoframe");
        if (!this.mRecordingActive && this.mRecording) {
            startRecording(context);
        }
        if (this.mRecordingActive && (this.mRecording ^ 1) != 0) {
            stopRecording(context);
        }
        if (this.mRecordingActive) {
            if (!this.mCaptureTimeLapse) {
                this.mTimestampNs = input.getTimestamp();
            } else if (skipFrameAndModifyTimestamp(input.getTimestamp())) {
                return;
            }
            glEnv.activateSurfaceWithId(this.mSurfaceId);
            this.mProgram.process(input, this.mScreen);
            glEnv.setSurfaceTimestamp(this.mTimestampNs);
            glEnv.swapBuffers();
            this.mNumFramesEncoded++;
        }
    }

    private void stopRecording(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Stopping recording");
        }
        this.mRecordingActive = false;
        this.mNumFramesEncoded = 0;
        GLEnvironment glEnv = context.getGLEnvironment();
        if (this.mLogVerbose) {
            Log.v(TAG, String.format("Unregistering surface %d", new Object[]{Integer.valueOf(this.mSurfaceId)}));
        }
        glEnv.unregisterSurfaceId(this.mSurfaceId);
        try {
            this.mMediaRecorder.stop();
            this.mMediaRecorder.release();
            this.mMediaRecorder = null;
            this.mScreen.release();
            this.mScreen = null;
            if (this.mRecordingDoneListener != null) {
                this.mRecordingDoneListener.onRecordingDone();
            }
        } catch (RuntimeException e) {
            throw new MediaRecorderStopException("MediaRecorder.stop() failed!", e);
        }
    }

    public void close(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(TAG, "Closing");
        }
        if (this.mRecordingActive) {
            stopRecording(context);
        }
    }

    public void tearDown(FilterContext context) {
        if (this.mMediaRecorder != null) {
            this.mMediaRecorder.release();
        }
        if (this.mScreen != null) {
            this.mScreen.release();
        }
    }
}
