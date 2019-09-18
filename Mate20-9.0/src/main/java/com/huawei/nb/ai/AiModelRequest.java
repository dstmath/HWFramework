package com.huawei.nb.ai;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nb.model.aimodel.AiModel;

public class AiModelRequest implements Parcelable {
    public static final Parcelable.Creator<AiModelRequest> CREATOR = new Parcelable.Creator<AiModelRequest>() {
        public AiModelRequest createFromParcel(Parcel in) {
            return new AiModelRequest(in);
        }

        public AiModelRequest[] newArray(int size) {
            return new AiModelRequest[size];
        }
    };
    private AiModel mAiModel = null;
    private boolean mNeedEncrypt = false;
    private boolean mNeedLatestVersion = false;
    private boolean mNeedMeanModel = false;
    private boolean mNeedWeightModel = false;
    private String mPublicKey = null;

    public AiModelRequest() {
    }

    public AiModel getAiModel() {
        return this.mAiModel;
    }

    public String getPublicKey() {
        return this.mPublicKey;
    }

    public boolean isNeedEncrypt() {
        return this.mNeedEncrypt;
    }

    public boolean isNeedWeightModel() {
        return this.mNeedWeightModel;
    }

    public boolean isNeedMeanModel() {
        return this.mNeedMeanModel;
    }

    public boolean isNeedLatestVersion() {
        return this.mNeedLatestVersion;
    }

    public AiModelRequest setNeedWeightModel() {
        this.mNeedWeightModel = true;
        return this;
    }

    public AiModelRequest setNeedMeanModel() {
        this.mNeedMeanModel = true;
        return this;
    }

    public AiModelRequest setNeedEncrypt() {
        this.mNeedEncrypt = true;
        return this;
    }

    public AiModelRequest setNeedLatestVersion() {
        this.mNeedLatestVersion = true;
        return this;
    }

    public AiModelRequest setAiModel(AiModel aiModel) {
        this.mAiModel = aiModel;
        return this;
    }

    public AiModelRequest setPublicKey(String key) {
        this.mPublicKey = key;
        return this;
    }

    public boolean isValid() {
        return this.mAiModel != null && this.mAiModel.getIs_none().intValue() == 0;
    }

    protected AiModelRequest(Parcel in) {
        boolean z;
        boolean z2;
        boolean z3 = true;
        String str = null;
        if (in.readByte() == 0) {
            this.mAiModel = null;
        } else {
            this.mAiModel = new AiModel(in);
        }
        this.mPublicKey = in.readByte() != 0 ? in.readString() : str;
        if (in.readByte() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mNeedEncrypt = z;
        if (in.readByte() != 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.mNeedWeightModel = z2;
        this.mNeedMeanModel = in.readByte() == 0 ? false : z3;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int i) {
        if (this.mAiModel != null) {
            out.writeByte((byte) 1);
            this.mAiModel.writeToParcel(out, i);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mPublicKey != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mPublicKey);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mNeedEncrypt) {
            out.writeByte((byte) 1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mNeedWeightModel) {
            out.writeByte((byte) 1);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mNeedMeanModel) {
            out.writeByte((byte) 1);
        } else {
            out.writeByte((byte) 0);
        }
    }
}
