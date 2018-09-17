package android.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.ArrayList;
import java.util.Objects;

public final class AudioRecordingConfiguration implements Parcelable {
    public static final Creator<AudioRecordingConfiguration> CREATOR = new Creator<AudioRecordingConfiguration>() {
        public AudioRecordingConfiguration createFromParcel(Parcel p) {
            return new AudioRecordingConfiguration(p, null);
        }

        public AudioRecordingConfiguration[] newArray(int size) {
            return new AudioRecordingConfiguration[size];
        }
    };
    private static final String TAG = new String("AudioRecordingConfiguration");
    private final AudioFormat mClientFormat;
    private final int mClientSource;
    private final AudioFormat mDeviceFormat;
    private final int mPatchHandle;
    private final int mSessionId;

    /* synthetic */ AudioRecordingConfiguration(Parcel in, AudioRecordingConfiguration -this1) {
        this(in);
    }

    public AudioRecordingConfiguration(int session, int source, AudioFormat devFormat, AudioFormat clientFormat, int patchHandle) {
        this.mSessionId = session;
        this.mClientSource = source;
        this.mDeviceFormat = devFormat;
        this.mClientFormat = clientFormat;
        this.mPatchHandle = patchHandle;
    }

    public int getClientAudioSource() {
        return this.mClientSource;
    }

    public int getClientAudioSessionId() {
        return this.mSessionId;
    }

    public AudioFormat getFormat() {
        return this.mDeviceFormat;
    }

    public AudioFormat getClientFormat() {
        return this.mClientFormat;
    }

    public AudioDeviceInfo getAudioDevice() {
        ArrayList<AudioPatch> patches = new ArrayList();
        if (AudioManager.listAudioPatches(patches) != 0) {
            Log.e(TAG, "Error retrieving list of audio patches");
            return null;
        }
        for (int i = 0; i < patches.size(); i++) {
            AudioPatch patch = (AudioPatch) patches.get(i);
            if (patch.id() == this.mPatchHandle) {
                AudioPortConfig[] sources = patch.sources();
                if (sources != null && sources.length > 0) {
                    int devId = sources[0].port().id();
                    AudioDeviceInfo[] devices = AudioManager.getDevicesStatic(1);
                    for (int j = 0; j < devices.length; j++) {
                        if (devices[j].getId() == devId) {
                            return devices[j];
                        }
                    }
                }
                Log.e(TAG, "Couldn't find device for recording, did recording end already?");
                return null;
            }
        }
        Log.e(TAG, "Couldn't find device for recording, did recording end already?");
        return null;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.mSessionId), Integer.valueOf(this.mClientSource)});
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSessionId);
        dest.writeInt(this.mClientSource);
        this.mClientFormat.writeToParcel(dest, 0);
        this.mDeviceFormat.writeToParcel(dest, 0);
        dest.writeInt(this.mPatchHandle);
    }

    private AudioRecordingConfiguration(Parcel in) {
        this.mSessionId = in.readInt();
        this.mClientSource = in.readInt();
        this.mClientFormat = (AudioFormat) AudioFormat.CREATOR.createFromParcel(in);
        this.mDeviceFormat = (AudioFormat) AudioFormat.CREATOR.createFromParcel(in);
        this.mPatchHandle = in.readInt();
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (this == o) {
            return true;
        }
        if (o == null || ((o instanceof AudioRecordingConfiguration) ^ 1) != 0) {
            return false;
        }
        AudioRecordingConfiguration that = (AudioRecordingConfiguration) o;
        if (this.mSessionId == that.mSessionId && this.mClientSource == that.mClientSource && this.mPatchHandle == that.mPatchHandle && this.mClientFormat.equals(that.mClientFormat)) {
            z = this.mDeviceFormat.equals(that.mDeviceFormat);
        }
        return z;
    }
}
