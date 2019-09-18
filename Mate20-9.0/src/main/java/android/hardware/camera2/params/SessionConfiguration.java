package android.hardware.camera2.params;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public final class SessionConfiguration {
    public static final int SESSION_HIGH_SPEED = 1;
    public static final int SESSION_REGULAR = 0;
    public static final int SESSION_VENDOR_START = 32768;
    private Executor mExecutor = null;
    private InputConfiguration mInputConfig = null;
    private List<OutputConfiguration> mOutputConfigurations;
    private CaptureRequest mSessionParameters = null;
    private int mSessionType;
    private CameraCaptureSession.StateCallback mStateCallback;

    @Retention(RetentionPolicy.SOURCE)
    public @interface SessionMode {
    }

    public SessionConfiguration(int sessionType, List<OutputConfiguration> outputs, Executor executor, CameraCaptureSession.StateCallback cb) {
        this.mSessionType = sessionType;
        this.mOutputConfigurations = Collections.unmodifiableList(new ArrayList(outputs));
        this.mStateCallback = cb;
        this.mExecutor = executor;
    }

    public int getSessionType() {
        return this.mSessionType;
    }

    public List<OutputConfiguration> getOutputConfigurations() {
        return this.mOutputConfigurations;
    }

    public CameraCaptureSession.StateCallback getStateCallback() {
        return this.mStateCallback;
    }

    public Executor getExecutor() {
        return this.mExecutor;
    }

    public void setInputConfiguration(InputConfiguration input) {
        if (this.mSessionType != 1) {
            this.mInputConfig = input;
            return;
        }
        throw new UnsupportedOperationException("Method not supported for high speed session types");
    }

    public InputConfiguration getInputConfiguration() {
        return this.mInputConfig;
    }

    public void setSessionParameters(CaptureRequest params) {
        this.mSessionParameters = params;
    }

    public CaptureRequest getSessionParameters() {
        return this.mSessionParameters;
    }
}
