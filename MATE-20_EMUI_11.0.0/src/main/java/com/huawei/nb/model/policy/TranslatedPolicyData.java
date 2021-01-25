package com.huawei.nb.model.policy;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class TranslatedPolicyData extends AManagedObject {
    public static final Parcelable.Creator<TranslatedPolicyData> CREATOR = new Parcelable.Creator<TranslatedPolicyData>() {
        /* class com.huawei.nb.model.policy.TranslatedPolicyData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TranslatedPolicyData createFromParcel(Parcel parcel) {
            return new TranslatedPolicyData(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public TranslatedPolicyData[] newArray(int i) {
            return new TranslatedPolicyData[i];
        }
    };
    private String policyFile;
    private Integer policyId;
    private String policyName;
    private String serviceName;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String getDatabaseVersion() {
        return "0.0.13";
    }

    public int getDatabaseVersionCode() {
        return 13;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.policy.TranslatedPolicyData";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public TranslatedPolicyData(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.policyId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.policyName = cursor.getString(2);
        this.serviceName = cursor.getString(3);
        this.policyFile = cursor.getString(4);
    }

    public TranslatedPolicyData(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.policyId = null;
            parcel.readInt();
        } else {
            this.policyId = Integer.valueOf(parcel.readInt());
        }
        this.policyName = parcel.readByte() == 0 ? null : parcel.readString();
        this.serviceName = parcel.readByte() == 0 ? null : parcel.readString();
        this.policyFile = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private TranslatedPolicyData(Integer num, String str, String str2, String str3) {
        this.policyId = num;
        this.policyName = str;
        this.serviceName = str2;
        this.policyFile = str3;
    }

    public TranslatedPolicyData() {
    }

    public Integer getPolicyId() {
        return this.policyId;
    }

    public void setPolicyId(Integer num) {
        this.policyId = num;
        setValue();
    }

    public String getPolicyName() {
        return this.policyName;
    }

    public void setPolicyName(String str) {
        this.policyName = str;
        setValue();
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(String str) {
        this.serviceName = str;
        setValue();
    }

    public String getPolicyFile() {
        return this.policyFile;
    }

    public void setPolicyFile(String str) {
        this.policyFile = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.policyId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.policyId.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
        if (this.policyName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.policyName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.serviceName != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.serviceName);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.policyFile != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.policyFile);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<TranslatedPolicyData> getHelper() {
        return TranslatedPolicyDataHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "TranslatedPolicyData { policyId: " + this.policyId + ", policyName: " + this.policyName + ", serviceName: " + this.serviceName + ", policyFile: " + this.policyFile + " }";
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override // com.huawei.odmf.core.AManagedObject, java.lang.Object
    public int hashCode() {
        return super.hashCode();
    }
}
