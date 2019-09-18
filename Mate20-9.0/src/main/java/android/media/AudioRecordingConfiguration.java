package android.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Objects;

public final class AudioRecordingConfiguration implements Parcelable {
    public static final Parcelable.Creator<AudioRecordingConfiguration> CREATOR = new Parcelable.Creator<AudioRecordingConfiguration>() {
        public AudioRecordingConfiguration createFromParcel(Parcel p) {
            return new AudioRecordingConfiguration(p);
        }

        public AudioRecordingConfiguration[] newArray(int size) {
            return new AudioRecordingConfiguration[size];
        }
    };
    private static final String TAG = new String("AudioRecordingConfiguration");
    private final AudioFormat mClientFormat;
    private final String mClientPackageName;
    private final int mClientSource;
    private final int mClientUid;
    private final AudioFormat mDeviceFormat;
    private final int mPatchHandle;
    private final int mSessionId;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AudioSource {
    }

    public AudioRecordingConfiguration(int uid, int session, int source, AudioFormat clientFormat, AudioFormat devFormat, int patchHandle, String packageName) {
        this.mClientUid = uid;
        this.mSessionId = session;
        this.mClientSource = source;
        this.mDeviceFormat = devFormat;
        this.mClientFormat = clientFormat;
        this.mPatchHandle = patchHandle;
        this.mClientPackageName = packageName;
    }

    public void dump(PrintWriter pw) {
        pw.println("  " + toLogFriendlyString(this));
    }

    public static String toLogFriendlyString(AudioRecordingConfiguration arc) {
        return new String("session:" + arc.mSessionId + " -- source:" + MediaRecorder.toLogFriendlyAudioSource(arc.mClientSource) + " -- uid:" + arc.mClientUid + " -- patch:" + arc.mPatchHandle + " -- pack:" + arc.mClientPackageName + " -- format client=" + arc.mClientFormat.toLogFriendlyString() + ", dev=" + arc.mDeviceFormat.toLogFriendlyString());
    }

    public static AudioRecordingConfiguration anonymizedCopy(AudioRecordingConfiguration in) {
        AudioRecordingConfiguration audioRecordingConfiguration = new AudioRecordingConfiguration(-1, in.mSessionId, in.mClientSource, in.mClientFormat, in.mDeviceFormat, in.mPatchHandle, "");
        return audioRecordingConfiguration;
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

    public String getClientPackageName() {
        return this.mClientPackageName;
    }

    public int getClientUid() {
        return this.mClientUid;
    }

    public AudioDeviceInfo getAudioDevice() {
        ArrayList<AudioPatch> patches = new ArrayList<>();
        if (AudioManager.listAudioPatches(patches) != 0) {
            Log.e(TAG, "Error retrieving list of audio patches");
            return null;
        }
        int i = 0;
        while (true) {
            if (i >= patches.size()) {
                break;
            }
            AudioPatch patch = patches.get(i);
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
            } else {
                i++;
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
        dest.writeString(this.mClientPackageName);
        dest.writeInt(this.mClientUid);
    }

    private AudioRecordingConfiguration(Parcel in) {
        this.mSessionId = in.readInt();
        this.mClientSource = in.readInt();
        this.mClientFormat = AudioFormat.CREATOR.createFromParcel(in);
        this.mDeviceFormat = AudioFormat.CREATOR.createFromParcel(in);
        this.mPatchHandle = in.readInt();
        this.mClientPackageName = in.readString();
        this.mClientUid = in.readInt();
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof AudioRecordingConfiguration)) {
            return false;
        }
        AudioRecordingConfiguration that = (AudioRecordingConfiguration) o;
        if (!(this.mClientUid == that.mClientUid && this.mSessionId == that.mSessionId && this.mClientSource == that.mClientSource && this.mPatchHandle == that.mPatchHandle && this.mClientFormat.equals(that.mClientFormat) && this.mDeviceFormat.equals(that.mDeviceFormat) && this.mClientPackageName.equals(that.mClientPackageName))) {
            z = false;
        }
        return z;
    }
}
