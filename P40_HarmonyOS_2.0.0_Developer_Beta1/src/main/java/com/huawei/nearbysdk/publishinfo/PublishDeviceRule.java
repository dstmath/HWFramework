package com.huawei.nearbysdk.publishinfo;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.nearbysdk.NearbyConfig;
import com.huawei.nearbysdk.NearbySDKUtils;
import java.io.Serializable;
import java.util.Objects;

public class PublishDeviceRule implements Parcelable, Serializable {
    public static final byte BUSINESS_ICONNECT = 1;
    public static final Parcelable.Creator<PublishDeviceRule> CREATOR = new Parcelable.Creator<PublishDeviceRule>() {
        /* class com.huawei.nearbysdk.publishinfo.PublishDeviceRule.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PublishDeviceRule createFromParcel(Parcel source) {
            boolean isSupportScreenOffAdv;
            boolean isForceUpdate;
            int businessId = source.readInt();
            NearbyConfig.BusinessTypeEnum businessType = NearbySDKUtils.getEnumFromInt(source.readInt());
            int typeChannel = source.readInt();
            int publishTimeout = source.readInt();
            if (source.readInt() == 1) {
                isSupportScreenOffAdv = true;
            } else {
                isSupportScreenOffAdv = false;
            }
            if (source.readByte() == 1) {
                isForceUpdate = true;
            } else {
                isForceUpdate = false;
            }
            return new Builder().withTypeChannel(typeChannel).withPublishTimeout(publishTimeout).withBusinessId(businessId).withSuppportScreenOffAdv(isSupportScreenOffAdv).withForceUpdateHostAp(isForceUpdate).withBusinessType(businessType).build();
        }

        @Override // android.os.Parcelable.Creator
        public PublishDeviceRule[] newArray(int size) {
            return new PublishDeviceRule[size];
        }
    };
    public static final byte DEFAULT_BUSINESS_ABILITY = 1;
    private static final NearbyConfig.BusinessTypeEnum DEFAULT_BUSINESS_TYPE = NearbyConfig.BusinessTypeEnum.InstantMessage;
    private static final int DEFAULT_PUBLISH_TIMEOUT = 120000;
    private static final int DEFAULT_TYPECHANNEL_BR = 2;
    private static final boolean IS_FORCE_UPDATE_HOSTAP = true;
    private static final boolean IS_SUPPORT_SCREENOFF_ADV = true;
    private static final long serialVersionUID = 1;
    private byte mBusinessAbility;
    private int mBusinessId;
    private NearbyConfig.BusinessTypeEnum mBusinessType;
    private boolean mIsForceUpdateHostAp;
    private boolean mIsSuppportScreenOffAdv;
    private int mPublishTimeout;
    private int mTypeChannel;

    private PublishDeviceRule(Builder builder) {
        this.mTypeChannel = builder.mTypeChannel;
        this.mBusinessType = builder.mBusinessType;
        this.mIsSuppportScreenOffAdv = builder.mIsSuppportScreenOffAdv;
        this.mBusinessId = builder.mBusinessId;
        this.mPublishTimeout = builder.mPublishTimeout;
        this.mIsForceUpdateHostAp = builder.mIsForceUpdateHostAp;
    }

    public PublishDeviceRule() {
        this(19, DEFAULT_BUSINESS_TYPE, 2);
    }

    public PublishDeviceRule(int businessId) {
        this(businessId, DEFAULT_BUSINESS_TYPE, 2);
    }

    public PublishDeviceRule(int businessId, int typeChannel) {
        this(businessId, DEFAULT_BUSINESS_TYPE, typeChannel);
    }

    public PublishDeviceRule(int businessId, NearbyConfig.BusinessTypeEnum businessType, int typeChannel) {
        this.mBusinessId = businessId;
        this.mBusinessType = businessType;
        this.mTypeChannel = typeChannel;
        this.mPublishTimeout = DEFAULT_PUBLISH_TIMEOUT;
        this.mIsSuppportScreenOffAdv = true;
        this.mIsForceUpdateHostAp = true;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeInt(this.mBusinessId);
        dest.writeInt(this.mBusinessType.toNumber());
        dest.writeInt(this.mTypeChannel);
        dest.writeInt(this.mPublishTimeout);
        if (this.mIsSuppportScreenOffAdv) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.mIsForceUpdateHostAp) {
            i2 = 0;
        }
        dest.writeInt(i2);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public NearbyConfig.BusinessTypeEnum getBusinessType() {
        return this.mBusinessType;
    }

    public void setBusinessType(NearbyConfig.BusinessTypeEnum businessType) {
        this.mBusinessType = businessType;
    }

    public int getBusinessId() {
        return this.mBusinessId;
    }

    public void setBusinessId(int businessId) {
        this.mBusinessId = businessId;
    }

    public int getTypeChannel() {
        return this.mTypeChannel;
    }

    public void setTypeChannel(int typeChannel) {
        this.mTypeChannel = typeChannel;
    }

    public int getPublishTimeout() {
        return this.mPublishTimeout;
    }

    public void setPublishTimeout(int publishTimeout) {
        this.mPublishTimeout = publishTimeout;
    }

    public boolean isSuppportScreenOffAdv() {
        return this.mIsSuppportScreenOffAdv;
    }

    public void setIsSuppportScreenOffAdv(boolean isSuppportScreenOffAdv) {
        this.mIsSuppportScreenOffAdv = isSuppportScreenOffAdv;
    }

    public boolean isForceUpdateHostAp() {
        return this.mIsForceUpdateHostAp;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PublishDeviceRule)) {
            return false;
        }
        PublishDeviceRule deviceRule = (PublishDeviceRule) obj;
        return this.mBusinessId == deviceRule.mBusinessId && this.mTypeChannel == deviceRule.mTypeChannel && this.mBusinessType == deviceRule.mBusinessType;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(this.mBusinessType, Integer.valueOf(this.mBusinessId), Integer.valueOf(this.mTypeChannel));
    }

    public static class Builder {
        private int mBusinessId = 19;
        private NearbyConfig.BusinessTypeEnum mBusinessType = PublishDeviceRule.DEFAULT_BUSINESS_TYPE;
        private boolean mIsForceUpdateHostAp = true;
        private boolean mIsSuppportScreenOffAdv = true;
        private int mPublishTimeout = PublishDeviceRule.DEFAULT_PUBLISH_TIMEOUT;
        private int mTypeChannel = 2;

        public Builder withForceUpdateHostAp(boolean isForceUpdate) {
            this.mIsForceUpdateHostAp = isForceUpdate;
            return this;
        }

        public Builder withTypeChannel(int typeChannel) {
            this.mTypeChannel = typeChannel;
            return this;
        }

        public Builder withBusinessId(int businessId) {
            this.mBusinessId = businessId;
            return this;
        }

        public Builder withPublishTimeout(int publishTimeout) {
            this.mPublishTimeout = publishTimeout;
            return this;
        }

        public Builder withSuppportScreenOffAdv(boolean isSuppportScreenOffAdv) {
            this.mIsSuppportScreenOffAdv = isSuppportScreenOffAdv;
            return this;
        }

        public Builder withBusinessType(NearbyConfig.BusinessTypeEnum businessType) {
            this.mBusinessType = businessType;
            return this;
        }

        public PublishDeviceRule build() {
            return new PublishDeviceRule(this);
        }
    }
}
