package com.huawei.nb.ai;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nb.model.aimodel.AiModel;

public class AiModelRequest implements Parcelable {
    public static final Parcelable.Creator<AiModelRequest> CREATOR = new Parcelable.Creator<AiModelRequest>() {
        /* class com.huawei.nb.ai.AiModelRequest.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AiModelRequest createFromParcel(Parcel parcel) {
            return new AiModelRequest(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public AiModelRequest[] newArray(int i) {
            return new AiModelRequest[i];
        }
    };
    private AiModel mAiModel;
    private boolean mIsNeedEncrypt;
    private boolean mIsNeedLatestVersion;
    private boolean mIsNeedMeanModel;
    private boolean mIsNeedWeightModel;
    private String mPublicKey;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public AiModelRequest() {
        this.mAiModel = null;
        this.mPublicKey = null;
        this.mIsNeedEncrypt = false;
        this.mIsNeedWeightModel = false;
        this.mIsNeedMeanModel = false;
        this.mIsNeedLatestVersion = false;
    }

    protected AiModelRequest(Parcel parcel) {
        String str = null;
        this.mAiModel = null;
        this.mPublicKey = null;
        boolean z = false;
        this.mIsNeedEncrypt = false;
        this.mIsNeedWeightModel = false;
        this.mIsNeedMeanModel = false;
        this.mIsNeedLatestVersion = false;
        if (parcel.readByte() == 0) {
            this.mAiModel = null;
        } else {
            this.mAiModel = new AiModel(parcel);
        }
        this.mPublicKey = parcel.readByte() != 0 ? parcel.readString() : str;
        this.mIsNeedEncrypt = parcel.readByte() != 0;
        this.mIsNeedWeightModel = parcel.readByte() != 0;
        this.mIsNeedMeanModel = parcel.readByte() != 0 ? true : z;
    }

    public AiModel getAiModel() {
        return this.mAiModel;
    }

    public String getPublicKey() {
        return this.mPublicKey;
    }

    public boolean isNeedEncrypt() {
        return this.mIsNeedEncrypt;
    }

    public boolean isNeedWeightModel() {
        return this.mIsNeedWeightModel;
    }

    public boolean isNeedMeanModel() {
        return this.mIsNeedMeanModel;
    }

    public boolean isNeedLatestVersion() {
        return this.mIsNeedLatestVersion;
    }

    public AiModelRequest setNeedWeightModel() {
        this.mIsNeedWeightModel = true;
        return this;
    }

    public AiModelRequest setNeedMeanModel() {
        this.mIsNeedMeanModel = true;
        return this;
    }

    public AiModelRequest setNeedEncrypt() {
        this.mIsNeedEncrypt = true;
        return this;
    }

    public AiModelRequest setNeedLatestVersion() {
        this.mIsNeedLatestVersion = true;
        return this;
    }

    public AiModelRequest setAiModel(AiModel aiModel) {
        this.mAiModel = aiModel;
        return this;
    }

    public AiModelRequest setPublicKey(String str) {
        this.mPublicKey = str;
        return this;
    }

    public boolean isValid() {
        AiModel aiModel = this.mAiModel;
        return aiModel != null && aiModel.getIs_none().intValue() == 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        if (this.mAiModel != null) {
            parcel.writeByte((byte) 1);
            this.mAiModel.writeToParcel(parcel, i);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mPublicKey != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mPublicKey);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mIsNeedEncrypt) {
            parcel.writeByte((byte) 1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mIsNeedWeightModel) {
            parcel.writeByte((byte) 1);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mIsNeedMeanModel) {
            parcel.writeByte((byte) 1);
        } else {
            parcel.writeByte((byte) 0);
        }
    }
}
