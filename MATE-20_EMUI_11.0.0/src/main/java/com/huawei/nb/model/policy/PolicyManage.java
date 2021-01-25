package com.huawei.nb.model.policy;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class PolicyManage extends AManagedObject {
    public static final Parcelable.Creator<PolicyManage> CREATOR = new Parcelable.Creator<PolicyManage>() {
        /* class com.huawei.nb.model.policy.PolicyManage.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PolicyManage createFromParcel(Parcel parcel) {
            return new PolicyManage(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public PolicyManage[] newArray(int i) {
            return new PolicyManage[i];
        }
    };
    private String policyFile;
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
        return "com.huawei.nb.model.policy.PolicyManage";
    }

    public String getEntityVersion() {
        return "0.0.2";
    }

    public int getEntityVersionCode() {
        return 2;
    }

    public PolicyManage(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.policyName = cursor.getString(1);
        this.serviceName = cursor.getString(2);
        this.policyFile = cursor.getString(3);
    }

    public PolicyManage(Parcel parcel) {
        super(parcel);
        String str = null;
        this.policyName = parcel.readByte() == 0 ? null : parcel.readString();
        this.serviceName = parcel.readByte() == 0 ? null : parcel.readString();
        this.policyFile = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private PolicyManage(String str, String str2, String str3) {
        this.policyName = str;
        this.serviceName = str2;
        this.policyFile = str3;
    }

    public PolicyManage() {
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
    public AEntityHelper<PolicyManage> getHelper() {
        return PolicyManageHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "PolicyManage { policyName: " + this.policyName + ", serviceName: " + this.serviceName + ", policyFile: " + this.policyFile + " }";
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
