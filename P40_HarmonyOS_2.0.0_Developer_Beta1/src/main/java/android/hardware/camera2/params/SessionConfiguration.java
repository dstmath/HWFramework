package android.hardware.camera2.params;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.utils.HashCodeHelpers;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public final class SessionConfiguration implements Parcelable {
    public static final Parcelable.Creator<SessionConfiguration> CREATOR = new Parcelable.Creator<SessionConfiguration>() {
        /* class android.hardware.camera2.params.SessionConfiguration.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SessionConfiguration createFromParcel(Parcel source) {
            try {
                return new SessionConfiguration(source);
            } catch (Exception e) {
                Log.e(SessionConfiguration.TAG, "Exception creating SessionConfiguration from parcel", e);
                return null;
            }
        }

        @Override // android.os.Parcelable.Creator
        public SessionConfiguration[] newArray(int size) {
            return new SessionConfiguration[size];
        }
    };
    public static final int SESSION_HIGH_SPEED = 1;
    public static final int SESSION_REGULAR = 0;
    public static final int SESSION_VENDOR_START = 32768;
    private static final String TAG = "SessionConfiguration";
    private Executor mExecutor;
    private InputConfiguration mInputConfig;
    private List<OutputConfiguration> mOutputConfigurations;
    private CaptureRequest mSessionParameters;
    private int mSessionType;
    private CameraCaptureSession.StateCallback mStateCallback;

    @Retention(RetentionPolicy.SOURCE)
    public @interface SessionMode {
    }

    public SessionConfiguration(int sessionType, List<OutputConfiguration> outputs, Executor executor, CameraCaptureSession.StateCallback cb) {
        this.mExecutor = null;
        this.mInputConfig = null;
        this.mSessionParameters = null;
        this.mSessionType = sessionType;
        this.mOutputConfigurations = Collections.unmodifiableList(new ArrayList(outputs));
        this.mStateCallback = cb;
        this.mExecutor = executor;
    }

    private SessionConfiguration(Parcel source) {
        this.mExecutor = null;
        this.mInputConfig = null;
        this.mSessionParameters = null;
        int sessionType = source.readInt();
        int inputWidth = source.readInt();
        int inputHeight = source.readInt();
        int inputFormat = source.readInt();
        ArrayList<OutputConfiguration> outConfigs = new ArrayList<>();
        source.readTypedList(outConfigs, OutputConfiguration.CREATOR);
        if (inputWidth > 0 && inputHeight > 0 && inputFormat != -1) {
            this.mInputConfig = new InputConfiguration(inputWidth, inputHeight, inputFormat);
        }
        this.mSessionType = sessionType;
        this.mOutputConfigurations = outConfigs;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        if (dest != null) {
            dest.writeInt(this.mSessionType);
            InputConfiguration inputConfiguration = this.mInputConfig;
            if (inputConfiguration != null) {
                dest.writeInt(inputConfiguration.getWidth());
                dest.writeInt(this.mInputConfig.getHeight());
                dest.writeInt(this.mInputConfig.getFormat());
            } else {
                dest.writeInt(0);
                dest.writeInt(0);
                dest.writeInt(-1);
            }
            dest.writeTypedList(this.mOutputConfigurations);
            return;
        }
        throw new IllegalArgumentException("dest must not be null");
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SessionConfiguration)) {
            return false;
        }
        SessionConfiguration other = (SessionConfiguration) obj;
        if (!(this.mInputConfig == other.mInputConfig && this.mSessionType == other.mSessionType && this.mOutputConfigurations.size() == other.mOutputConfigurations.size())) {
            return false;
        }
        for (int i = 0; i < this.mOutputConfigurations.size(); i++) {
            if (!this.mOutputConfigurations.get(i).equals(other.mOutputConfigurations.get(i))) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCode(this.mOutputConfigurations.hashCode(), this.mInputConfig.hashCode(), this.mSessionType);
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
