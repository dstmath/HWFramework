package android.media.audiopolicy;

import android.annotation.SystemApi;
import android.media.AudioAttributes;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.List;

@SystemApi
public final class AudioProductStrategy implements Parcelable {
    public static final Parcelable.Creator<AudioProductStrategy> CREATOR = new Parcelable.Creator<AudioProductStrategy>() {
        /* class android.media.audiopolicy.AudioProductStrategy.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AudioProductStrategy createFromParcel(Parcel in) {
            String name = in.readString();
            int id = in.readInt();
            int nbAttributesGroups = in.readInt();
            AudioAttributesGroup[] aag = new AudioAttributesGroup[nbAttributesGroups];
            for (int index = 0; index < nbAttributesGroups; index++) {
                aag[index] = AudioAttributesGroup.CREATOR.createFromParcel(in);
            }
            return new AudioProductStrategy(name, id, aag);
        }

        @Override // android.os.Parcelable.Creator
        public AudioProductStrategy[] newArray(int size) {
            return new AudioProductStrategy[size];
        }
    };
    public static final int DEFAULT_GROUP = -1;
    private static final String TAG = "AudioProductStrategy";
    @GuardedBy({"sLock"})
    private static List<AudioProductStrategy> sAudioProductStrategies;
    public static final AudioAttributes sDefaultAttributes = new AudioAttributes.Builder().setCapturePreset(0).build();
    private static final Object sLock = new Object();
    private final AudioAttributesGroup[] mAudioAttributesGroups;
    private int mId;
    private final String mName;

    private static native int native_list_audio_product_strategies(ArrayList<AudioProductStrategy> arrayList);

    public static List<AudioProductStrategy> getAudioProductStrategies() {
        if (sAudioProductStrategies == null) {
            synchronized (sLock) {
                if (sAudioProductStrategies == null) {
                    sAudioProductStrategies = initializeAudioProductStrategies();
                }
            }
        }
        return sAudioProductStrategies;
    }

    public static AudioAttributes getAudioAttributesForStrategyWithLegacyStreamType(int streamType) {
        for (AudioProductStrategy productStrategy : getAudioProductStrategies()) {
            AudioAttributes aa = productStrategy.getAudioAttributesForLegacyStreamType(streamType);
            if (aa != null) {
                return aa;
            }
        }
        return new AudioAttributes.Builder().setContentType(0).setUsage(0).build();
    }

    public static int getLegacyStreamTypeForStrategyWithAudioAttributes(AudioAttributes audioAttributes) {
        Preconditions.checkNotNull(audioAttributes, "AudioAttributes must not be null");
        for (AudioProductStrategy productStrategy : getAudioProductStrategies()) {
            if (productStrategy.supportsAudioAttributes(audioAttributes)) {
                int streamType = productStrategy.getLegacyStreamTypeForAudioAttributes(audioAttributes);
                if (streamType != -1) {
                    return streamType;
                }
                Log.w(TAG, "Attributes " + audioAttributes.toString() + " ported by strategy " + productStrategy.getId() + " has no stream type associated, DO NOT USE STREAM TO CONTROL THE VOLUME");
                return 3;
            }
        }
        return 3;
    }

    private static List<AudioProductStrategy> initializeAudioProductStrategies() {
        ArrayList<AudioProductStrategy> apsList = new ArrayList<>();
        if (native_list_audio_product_strategies(apsList) != 0) {
            Log.w(TAG, ": initializeAudioProductStrategies failed");
        }
        return apsList;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AudioProductStrategy thatStrategy = (AudioProductStrategy) o;
        if (this.mName == thatStrategy.mName && this.mId == thatStrategy.mId && this.mAudioAttributesGroups.equals(thatStrategy.mAudioAttributesGroups)) {
            return true;
        }
        return false;
    }

    private AudioProductStrategy(String name, int id, AudioAttributesGroup[] aag) {
        Preconditions.checkNotNull(name, "name must not be null");
        Preconditions.checkNotNull(aag, "AudioAttributesGroups must not be null");
        this.mName = name;
        this.mId = id;
        this.mAudioAttributesGroups = aag;
    }

    @SystemApi
    public int getId() {
        return this.mId;
    }

    @SystemApi
    public AudioAttributes getAudioAttributes() {
        AudioAttributesGroup[] audioAttributesGroupArr = this.mAudioAttributesGroups;
        if (audioAttributesGroupArr.length == 0) {
            return new AudioAttributes.Builder().build();
        }
        return audioAttributesGroupArr[0].getAudioAttributes();
    }

    public AudioAttributes getAudioAttributesForLegacyStreamType(int streamType) {
        AudioAttributesGroup[] audioAttributesGroupArr = this.mAudioAttributesGroups;
        for (AudioAttributesGroup aag : audioAttributesGroupArr) {
            if (aag.supportsStreamType(streamType)) {
                return aag.getAudioAttributes();
            }
        }
        return null;
    }

    public int getLegacyStreamTypeForAudioAttributes(AudioAttributes aa) {
        Preconditions.checkNotNull(aa, "AudioAttributes must not be null");
        AudioAttributesGroup[] audioAttributesGroupArr = this.mAudioAttributesGroups;
        for (AudioAttributesGroup aag : audioAttributesGroupArr) {
            if (aag.supportsAttributes(aa)) {
                return aag.getStreamType();
            }
        }
        return -1;
    }

    public boolean supportsAudioAttributes(AudioAttributes aa) {
        Preconditions.checkNotNull(aa, "AudioAttributes must not be null");
        for (AudioAttributesGroup aag : this.mAudioAttributesGroups) {
            if (aag.supportsAttributes(aa)) {
                return true;
            }
        }
        return false;
    }

    public int getVolumeGroupIdForLegacyStreamType(int streamType) {
        AudioAttributesGroup[] audioAttributesGroupArr = this.mAudioAttributesGroups;
        for (AudioAttributesGroup aag : audioAttributesGroupArr) {
            if (aag.supportsStreamType(streamType)) {
                return aag.getVolumeGroupId();
            }
        }
        return -1;
    }

    public int getVolumeGroupIdForAudioAttributes(AudioAttributes aa) {
        Preconditions.checkNotNull(aa, "AudioAttributes must not be null");
        AudioAttributesGroup[] audioAttributesGroupArr = this.mAudioAttributesGroups;
        for (AudioAttributesGroup aag : audioAttributesGroupArr) {
            if (aag.supportsAttributes(aa)) {
                return aag.getVolumeGroupId();
            }
        }
        return -1;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeInt(this.mId);
        dest.writeInt(this.mAudioAttributesGroups.length);
        for (AudioAttributesGroup aag : this.mAudioAttributesGroups) {
            aag.writeToParcel(dest, flags);
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("\n Name: ");
        s.append(this.mName);
        s.append(" Id: ");
        s.append(Integer.toString(this.mId));
        for (AudioAttributesGroup aag : this.mAudioAttributesGroups) {
            s.append(aag.toString());
        }
        return s.toString();
    }

    /* access modifiers changed from: private */
    public static boolean attributesMatches(AudioAttributes refAttr, AudioAttributes attr) {
        Preconditions.checkNotNull(refAttr, "refAttr must not be null");
        Preconditions.checkNotNull(attr, "attr must not be null");
        String refFormattedTags = TextUtils.join(";", refAttr.getTags());
        String cliFormattedTags = TextUtils.join(";", attr.getTags());
        if (refAttr.equals(sDefaultAttributes)) {
            return false;
        }
        if (refAttr.getUsage() != 0 && attr.getUsage() != refAttr.getUsage()) {
            return false;
        }
        if (refAttr.getContentType() != 0 && attr.getContentType() != refAttr.getContentType()) {
            return false;
        }
        if (refAttr.getAllFlags() != 0 && (attr.getAllFlags() == 0 || (attr.getAllFlags() & refAttr.getAllFlags()) != refAttr.getAllFlags())) {
            return false;
        }
        if (refFormattedTags.length() == 0 || refFormattedTags.equals(cliFormattedTags)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static final class AudioAttributesGroup implements Parcelable {
        public static final Parcelable.Creator<AudioAttributesGroup> CREATOR = new Parcelable.Creator<AudioAttributesGroup>() {
            /* class android.media.audiopolicy.AudioProductStrategy.AudioAttributesGroup.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public AudioAttributesGroup createFromParcel(Parcel in) {
                int volumeGroupId = in.readInt();
                int streamType = in.readInt();
                int nbAttributes = in.readInt();
                AudioAttributes[] aa = new AudioAttributes[nbAttributes];
                for (int index = 0; index < nbAttributes; index++) {
                    aa[index] = AudioAttributes.CREATOR.createFromParcel(in);
                }
                return new AudioAttributesGroup(volumeGroupId, streamType, aa);
            }

            @Override // android.os.Parcelable.Creator
            public AudioAttributesGroup[] newArray(int size) {
                return new AudioAttributesGroup[size];
            }
        };
        private final AudioAttributes[] mAudioAttributes;
        private int mLegacyStreamType;
        private int mVolumeGroupId;

        AudioAttributesGroup(int volumeGroupId, int streamType, AudioAttributes[] audioAttributes) {
            this.mVolumeGroupId = volumeGroupId;
            this.mLegacyStreamType = streamType;
            this.mAudioAttributes = audioAttributes;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            AudioAttributesGroup thatAag = (AudioAttributesGroup) o;
            if (this.mVolumeGroupId == thatAag.mVolumeGroupId && this.mLegacyStreamType == thatAag.mLegacyStreamType && this.mAudioAttributes.equals(thatAag.mAudioAttributes)) {
                return true;
            }
            return false;
        }

        public int getStreamType() {
            return this.mLegacyStreamType;
        }

        public int getVolumeGroupId() {
            return this.mVolumeGroupId;
        }

        public AudioAttributes getAudioAttributes() {
            AudioAttributes[] audioAttributesArr = this.mAudioAttributes;
            if (audioAttributesArr.length == 0) {
                return new AudioAttributes.Builder().build();
            }
            return audioAttributesArr[0];
        }

        public boolean supportsAttributes(AudioAttributes attributes) {
            AudioAttributes[] audioAttributesArr = this.mAudioAttributes;
            for (AudioAttributes refAa : audioAttributesArr) {
                if (refAa.equals(attributes) || AudioProductStrategy.attributesMatches(refAa, attributes)) {
                    return true;
                }
            }
            return false;
        }

        public boolean supportsStreamType(int streamType) {
            return this.mLegacyStreamType == streamType;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mVolumeGroupId);
            dest.writeInt(this.mLegacyStreamType);
            dest.writeInt(this.mAudioAttributes.length);
            for (AudioAttributes attributes : this.mAudioAttributes) {
                attributes.writeToParcel(dest, flags | 1);
            }
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("\n    Legacy Stream Type: ");
            s.append(Integer.toString(this.mLegacyStreamType));
            s.append(" Volume Group Id: ");
            s.append(Integer.toString(this.mVolumeGroupId));
            AudioAttributes[] audioAttributesArr = this.mAudioAttributes;
            for (AudioAttributes attribute : audioAttributesArr) {
                s.append("\n    -");
                s.append(attribute.toString());
            }
            return s.toString();
        }
    }
}
