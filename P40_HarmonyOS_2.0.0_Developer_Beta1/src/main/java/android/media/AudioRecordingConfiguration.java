package android.media;

import android.annotation.UnsupportedAppUsage;
import android.media.audiofx.AudioEffect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class AudioRecordingConfiguration implements Parcelable {
    public static final Parcelable.Creator<AudioRecordingConfiguration> CREATOR = new Parcelable.Creator<AudioRecordingConfiguration>() {
        /* class android.media.AudioRecordingConfiguration.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AudioRecordingConfiguration createFromParcel(Parcel p) {
            return new AudioRecordingConfiguration(p);
        }

        @Override // android.os.Parcelable.Creator
        public AudioRecordingConfiguration[] newArray(int size) {
            return new AudioRecordingConfiguration[size];
        }
    };
    private static final String TAG = new String("AudioRecordingConfiguration");
    private final AudioEffect.Descriptor[] mClientEffects;
    private final AudioFormat mClientFormat;
    private final String mClientPackageName;
    private final int mClientPortId;
    private final int mClientSessionId;
    private boolean mClientSilenced;
    private final int mClientSource;
    private final int mClientUid;
    private final AudioEffect.Descriptor[] mDeviceEffects;
    private final AudioFormat mDeviceFormat;
    private final int mDeviceSource;
    private final int mPatchHandle;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AudioSource {
    }

    public AudioRecordingConfiguration(int uid, int session, int source, AudioFormat clientFormat, AudioFormat devFormat, int patchHandle, String packageName, int clientPortId, boolean clientSilenced, int deviceSource, AudioEffect.Descriptor[] clientEffects, AudioEffect.Descriptor[] deviceEffects) {
        this.mClientUid = uid;
        this.mClientSessionId = session;
        this.mClientSource = source;
        this.mClientFormat = clientFormat;
        this.mDeviceFormat = devFormat;
        this.mPatchHandle = patchHandle;
        this.mClientPackageName = packageName;
        this.mClientPortId = clientPortId;
        this.mClientSilenced = clientSilenced;
        this.mDeviceSource = deviceSource;
        this.mClientEffects = clientEffects;
        this.mDeviceEffects = deviceEffects;
    }

    public AudioRecordingConfiguration(int uid, int session, int source, AudioFormat clientFormat, AudioFormat devFormat, int patchHandle, String packageName) {
        this(uid, session, source, clientFormat, devFormat, patchHandle, packageName, 0, false, 0, new AudioEffect.Descriptor[0], new AudioEffect.Descriptor[0]);
    }

    public void dump(PrintWriter pw) {
        pw.println("  " + toLogFriendlyString(this));
    }

    public static String toLogFriendlyString(AudioRecordingConfiguration arc) {
        String clientEffects = new String();
        AudioEffect.Descriptor[] descriptorArr = arc.mClientEffects;
        String clientEffects2 = clientEffects;
        for (AudioEffect.Descriptor desc : descriptorArr) {
            clientEffects2 = clientEffects2 + "'" + desc.name + "' ";
        }
        String deviceEffects = new String();
        AudioEffect.Descriptor[] descriptorArr2 = arc.mDeviceEffects;
        for (AudioEffect.Descriptor desc2 : descriptorArr2) {
            deviceEffects = deviceEffects + "'" + desc2.name + "' ";
        }
        return new String("session:" + arc.mClientSessionId + " -- source client=" + MediaRecorder.toLogFriendlyAudioSource(arc.mClientSource) + ", dev=" + arc.mDeviceFormat.toLogFriendlyString() + " -- uid:" + arc.mClientUid + " -- patch:" + arc.mPatchHandle + " -- pack:" + arc.mClientPackageName + " -- format client=" + arc.mClientFormat.toLogFriendlyString() + ", dev=" + arc.mDeviceFormat.toLogFriendlyString() + " -- silenced:" + arc.mClientSilenced + " -- effects client=" + clientEffects2 + ", dev=" + deviceEffects);
    }

    public static AudioRecordingConfiguration anonymizedCopy(AudioRecordingConfiguration in) {
        return new AudioRecordingConfiguration(-1, in.mClientSessionId, in.mClientSource, in.mClientFormat, in.mDeviceFormat, in.mPatchHandle, "", in.mClientPortId, in.mClientSilenced, in.mDeviceSource, in.mClientEffects, in.mDeviceEffects);
    }

    public int getClientAudioSource() {
        return this.mClientSource;
    }

    public int getClientAudioSessionId() {
        return this.mClientSessionId;
    }

    public AudioFormat getFormat() {
        return this.mDeviceFormat;
    }

    public AudioFormat getClientFormat() {
        return this.mClientFormat;
    }

    @UnsupportedAppUsage
    public String getClientPackageName() {
        return this.mClientPackageName;
    }

    @UnsupportedAppUsage
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

    public int getClientPortId() {
        return this.mClientPortId;
    }

    public boolean isClientSilenced() {
        return this.mClientSilenced;
    }

    public int getAudioSource() {
        return this.mDeviceSource;
    }

    public List<AudioEffect.Descriptor> getClientEffects() {
        return new ArrayList(Arrays.asList(this.mClientEffects));
    }

    public List<AudioEffect.Descriptor> getEffects() {
        return new ArrayList(Arrays.asList(this.mDeviceEffects));
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mClientSessionId), Integer.valueOf(this.mClientSource));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mClientSessionId);
        dest.writeInt(this.mClientSource);
        this.mClientFormat.writeToParcel(dest, 0);
        this.mDeviceFormat.writeToParcel(dest, 0);
        dest.writeInt(this.mPatchHandle);
        dest.writeString(this.mClientPackageName);
        dest.writeInt(this.mClientUid);
        dest.writeInt(this.mClientPortId);
        dest.writeBoolean(this.mClientSilenced);
        dest.writeInt(this.mDeviceSource);
        dest.writeInt(this.mClientEffects.length);
        int i = 0;
        while (true) {
            AudioEffect.Descriptor[] descriptorArr = this.mClientEffects;
            if (i >= descriptorArr.length) {
                break;
            }
            descriptorArr[i].writeToParcel(dest);
            i++;
        }
        dest.writeInt(this.mDeviceEffects.length);
        int i2 = 0;
        while (true) {
            AudioEffect.Descriptor[] descriptorArr2 = this.mDeviceEffects;
            if (i2 < descriptorArr2.length) {
                descriptorArr2[i2].writeToParcel(dest);
                i2++;
            } else {
                return;
            }
        }
    }

    private AudioRecordingConfiguration(Parcel in) {
        this.mClientSessionId = in.readInt();
        this.mClientSource = in.readInt();
        this.mClientFormat = AudioFormat.CREATOR.createFromParcel(in);
        this.mDeviceFormat = AudioFormat.CREATOR.createFromParcel(in);
        this.mPatchHandle = in.readInt();
        this.mClientPackageName = in.readString();
        this.mClientUid = in.readInt();
        this.mClientPortId = in.readInt();
        this.mClientSilenced = in.readBoolean();
        this.mDeviceSource = in.readInt();
        this.mClientEffects = new AudioEffect.Descriptor[in.readInt()];
        int i = 0;
        while (true) {
            AudioEffect.Descriptor[] descriptorArr = this.mClientEffects;
            if (i >= descriptorArr.length) {
                break;
            }
            descriptorArr[i] = new AudioEffect.Descriptor(in);
            i++;
        }
        this.mDeviceEffects = new AudioEffect.Descriptor[in.readInt()];
        int i2 = 0;
        while (true) {
            AudioEffect.Descriptor[] descriptorArr2 = this.mDeviceEffects;
            if (i2 < descriptorArr2.length) {
                descriptorArr2[i2] = new AudioEffect.Descriptor(in);
                i2++;
            } else {
                return;
            }
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof AudioRecordingConfiguration)) {
            return false;
        }
        AudioRecordingConfiguration that = (AudioRecordingConfiguration) o;
        if (this.mClientUid == that.mClientUid && this.mClientSessionId == that.mClientSessionId && this.mClientSource == that.mClientSource && this.mPatchHandle == that.mPatchHandle && this.mClientFormat.equals(that.mClientFormat) && this.mDeviceFormat.equals(that.mDeviceFormat) && this.mClientPackageName.equals(that.mClientPackageName) && this.mClientPortId == that.mClientPortId && this.mClientSilenced == that.mClientSilenced && this.mDeviceSource == that.mDeviceSource && Arrays.equals(this.mClientEffects, that.mClientEffects) && Arrays.equals(this.mDeviceEffects, that.mDeviceEffects)) {
            return true;
        }
        return false;
    }
}
