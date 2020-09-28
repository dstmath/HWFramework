package huawei.android.security.voicerecognition;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.huawei.hwpartsecurity.BuildConfig;
import java.util.ArrayList;
import java.util.List;

public class VoiceCommandList implements Parcelable {
    public static final Parcelable.Creator<VoiceCommandList> CREATOR = new Parcelable.Creator<VoiceCommandList>() {
        /* class huawei.android.security.voicerecognition.VoiceCommandList.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public VoiceCommandList createFromParcel(Parcel in) {
            return new VoiceCommandList(in);
        }

        @Override // android.os.Parcelable.Creator
        public VoiceCommandList[] newArray(int size) {
            return new VoiceCommandList[size];
        }
    };
    private static final int DEFAULT_LEN = 16;
    private static final int INDEX_ALGO_ID = 2;
    private static final int INDEX_HEADSET_ID = 1;
    private static final String TAG = VoiceCommandList.class.getSimpleName();
    private List<VoiceConfiguration> mConfigList;
    private int mIdForUnlock;
    private String mVersion;

    public VoiceCommandList(String version, int idForUnlock) {
        this.mVersion = BuildConfig.FLAVOR;
        this.mIdForUnlock = -1;
        this.mConfigList = new ArrayList(16);
        this.mVersion = version;
        this.mIdForUnlock = idForUnlock;
    }

    public VoiceCommandList(String version, int idForLock, List<VoiceConfiguration> voiceList) {
        this.mVersion = BuildConfig.FLAVOR;
        this.mIdForUnlock = -1;
        this.mConfigList = new ArrayList(16);
        this.mVersion = version;
        this.mIdForUnlock = idForLock;
        this.mConfigList.addAll(voiceList);
    }

    private VoiceCommandList(Parcel in) {
        this.mVersion = BuildConfig.FLAVOR;
        this.mIdForUnlock = -1;
        this.mConfigList = new ArrayList(16);
        this.mVersion = in.readString();
        this.mIdForUnlock = in.readInt();
        this.mConfigList = in.readArrayList(VoiceConfiguration.class.getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mVersion);
        out.writeInt(this.mIdForUnlock);
        out.writeList(this.mConfigList);
    }

    public String toString() {
        return "VoiceCommandList";
    }

    public int getListSize() {
        List<VoiceConfiguration> list = this.mConfigList;
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public List<VoiceConfiguration> getConfigList() {
        return this.mConfigList;
    }

    public String getConfigVersion() {
        return this.mVersion;
    }

    public int getUnlockId() {
        return this.mIdForUnlock;
    }

    public void setVersion(String version) {
        this.mVersion = version;
    }

    public VoiceConfiguration getConfigurationByHeadsetId(int headsetId) {
        return getConfigurationCommon(1, headsetId);
    }

    public VoiceConfiguration getConfigurationByAlgoId(int algoId) {
        return getConfigurationCommon(2, algoId);
    }

    private VoiceConfiguration getConfigurationCommon(int index, int id) {
        int size = this.mConfigList.size();
        for (int i = 0; i < size; i++) {
            VoiceConfiguration config = this.mConfigList.get(i);
            if (isIdEquals(config, index, id)) {
                return config;
            }
        }
        return null;
    }

    private boolean isIdEquals(VoiceConfiguration config, int index, int id) {
        if (config == null) {
            return false;
        }
        if (index == 1) {
            return config.isHeadsetIdEquals(id);
        }
        if (index != 2) {
            return false;
        }
        return config.isAlgoIdEquals(id);
    }

    public void addConfig(String headsetIdStr, String commandStr, String algoIdStr, String voiceTagStr, String callFlagStr) {
        try {
            this.mConfigList.add(new VoiceConfiguration(Integer.parseInt(headsetIdStr), commandStr, Integer.parseInt(algoIdStr), voiceTagStr, Integer.parseInt(callFlagStr)));
        } catch (NumberFormatException ex) {
            String str = TAG;
            Log.e(str, "invalid config value:" + ex.getMessage());
        }
    }

    public static class VoiceConfiguration implements Parcelable {
        public static final Parcelable.Creator<VoiceConfiguration> CREATOR = new Parcelable.Creator<VoiceConfiguration>() {
            /* class huawei.android.security.voicerecognition.VoiceCommandList.VoiceConfiguration.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public VoiceConfiguration createFromParcel(Parcel in) {
                return new VoiceConfiguration(in);
            }

            @Override // android.os.Parcelable.Creator
            public VoiceConfiguration[] newArray(int size) {
                return new VoiceConfiguration[size];
            }
        };
        private int mAlgoId;
        private int mCallFlag;
        private String mCommandString;
        private int mHeadsetId;
        private String mVoiceTag;

        public VoiceConfiguration(int headsetId, String commandStr, int algoId, String voiceTag, int callflag) {
            this.mCommandString = null;
            this.mVoiceTag = null;
            this.mCallFlag = 0;
            this.mHeadsetId = headsetId;
            this.mCommandString = commandStr;
            this.mAlgoId = algoId;
            this.mVoiceTag = voiceTag;
            this.mCallFlag = callflag;
        }

        private VoiceConfiguration(Parcel in) {
            this.mCommandString = null;
            this.mVoiceTag = null;
            this.mCallFlag = 0;
            this.mHeadsetId = in.readInt();
            this.mCommandString = in.readString();
            this.mAlgoId = in.readInt();
            this.mVoiceTag = in.readString();
            this.mCallFlag = in.readInt();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mHeadsetId);
            dest.writeString(this.mCommandString);
            dest.writeInt(this.mAlgoId);
            dest.writeString(this.mVoiceTag);
            dest.writeInt(this.mCallFlag);
        }

        public String toString() {
            return "VoiceConfiguration{mHeadsetId=" + this.mHeadsetId + ", mCommandString=" + this.mCommandString + ", mAlgoId=" + this.mAlgoId + ", mVoiceTag=" + this.mVoiceTag + ", mCallFlag=" + this.mCallFlag + "}";
        }

        public boolean isHeadsetIdEquals(int headsetId) {
            return this.mHeadsetId == headsetId;
        }

        public boolean isAlgoIdEquals(int algoId) {
            return this.mAlgoId == algoId;
        }

        public int getHeadsetId() {
            return this.mHeadsetId;
        }

        public String getCommandString() {
            return this.mCommandString;
        }

        public int getAlgoId() {
            return this.mAlgoId;
        }

        public int getCallFlag() {
            return this.mCallFlag;
        }

        public String getVoiceTag() {
            return this.mVoiceTag;
        }
    }
}
