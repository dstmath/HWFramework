package com.huawei.nb.model.weather;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class CityInfoModel extends AManagedObject {
    public static final Parcelable.Creator<CityInfoModel> CREATOR = new Parcelable.Creator<CityInfoModel>() {
        /* class com.huawei.nb.model.weather.CityInfoModel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public CityInfoModel createFromParcel(Parcel parcel) {
            return new CityInfoModel(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public CityInfoModel[] newArray(int i) {
            return new CityInfoModel[i];
        }
    };
    private Long _id;
    private String ca;
    private String city_alias;
    private String city_code;
    private String city_name;
    private String city_native;
    private int city_type;
    private String co;
    private String country_name;
    private String country_name_cn;
    private int home_city;
    private String hw_id;
    private long insert_time;
    private int manual_set;
    private String province_name;
    private String province_name_cn;
    private long sequence_id = -1;
    private String state_name;
    private String state_name_cn;
    private String time_zone;
    private long weather_id;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsWeather";
    }

    public String getDatabaseVersion() {
        return "0.0.17";
    }

    public int getDatabaseVersionCode() {
        return 17;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.weather.CityInfoModel";
    }

    public String getEntityVersion() {
        return "0.0.14";
    }

    public int getEntityVersionCode() {
        return 14;
    }

    public CityInfoModel(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this._id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.city_name = cursor.getString(2);
        this.city_alias = cursor.getString(3);
        this.city_native = cursor.getString(4);
        this.state_name = cursor.getString(5);
        this.city_code = cursor.getString(6);
        this.city_type = cursor.getInt(7);
        this.time_zone = cursor.getString(8);
        this.insert_time = cursor.getLong(9);
        this.weather_id = cursor.getLong(10);
        this.manual_set = cursor.getInt(11);
        this.home_city = cursor.getInt(12);
        this.state_name_cn = cursor.getString(13);
        this.province_name = cursor.getString(14);
        this.province_name_cn = cursor.getString(15);
        this.country_name = cursor.getString(16);
        this.country_name_cn = cursor.getString(17);
        this.hw_id = cursor.getString(18);
        this.co = cursor.getString(19);
        this.ca = cursor.getString(20);
        this.sequence_id = cursor.getLong(21);
    }

    public CityInfoModel(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this._id = null;
            parcel.readLong();
        } else {
            this._id = Long.valueOf(parcel.readLong());
        }
        this.city_name = parcel.readByte() == 0 ? null : parcel.readString();
        this.city_alias = parcel.readByte() == 0 ? null : parcel.readString();
        this.city_native = parcel.readByte() == 0 ? null : parcel.readString();
        this.state_name = parcel.readByte() == 0 ? null : parcel.readString();
        this.city_code = parcel.readByte() == 0 ? null : parcel.readString();
        this.city_type = parcel.readInt();
        this.time_zone = parcel.readByte() == 0 ? null : parcel.readString();
        this.insert_time = parcel.readLong();
        this.weather_id = parcel.readLong();
        this.manual_set = parcel.readInt();
        this.home_city = parcel.readInt();
        this.state_name_cn = parcel.readByte() == 0 ? null : parcel.readString();
        this.province_name = parcel.readByte() == 0 ? null : parcel.readString();
        this.province_name_cn = parcel.readByte() == 0 ? null : parcel.readString();
        this.country_name = parcel.readByte() == 0 ? null : parcel.readString();
        this.country_name_cn = parcel.readByte() == 0 ? null : parcel.readString();
        this.hw_id = parcel.readByte() == 0 ? null : parcel.readString();
        this.co = parcel.readByte() == 0 ? null : parcel.readString();
        this.ca = parcel.readByte() != 0 ? parcel.readString() : str;
        this.sequence_id = parcel.readLong();
    }

    private CityInfoModel(Long l, String str, String str2, String str3, String str4, String str5, int i, String str6, long j, long j2, int i2, int i3, String str7, String str8, String str9, String str10, String str11, String str12, String str13, String str14, long j3) {
        this._id = l;
        this.city_name = str;
        this.city_alias = str2;
        this.city_native = str3;
        this.state_name = str4;
        this.city_code = str5;
        this.city_type = i;
        this.time_zone = str6;
        this.insert_time = j;
        this.weather_id = j2;
        this.manual_set = i2;
        this.home_city = i3;
        this.state_name_cn = str7;
        this.province_name = str8;
        this.province_name_cn = str9;
        this.country_name = str10;
        this.country_name_cn = str11;
        this.hw_id = str12;
        this.co = str13;
        this.ca = str14;
        this.sequence_id = j3;
    }

    public CityInfoModel() {
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long l) {
        this._id = l;
        setValue();
    }

    public String getCity_name() {
        return this.city_name;
    }

    public void setCity_name(String str) {
        this.city_name = str;
        setValue();
    }

    public String getCity_alias() {
        return this.city_alias;
    }

    public void setCity_alias(String str) {
        this.city_alias = str;
        setValue();
    }

    public String getCity_native() {
        return this.city_native;
    }

    public void setCity_native(String str) {
        this.city_native = str;
        setValue();
    }

    public String getState_name() {
        return this.state_name;
    }

    public void setState_name(String str) {
        this.state_name = str;
        setValue();
    }

    public String getCity_code() {
        return this.city_code;
    }

    public void setCity_code(String str) {
        this.city_code = str;
        setValue();
    }

    public int getCity_type() {
        return this.city_type;
    }

    public void setCity_type(int i) {
        this.city_type = i;
        setValue();
    }

    public String getTime_zone() {
        return this.time_zone;
    }

    public void setTime_zone(String str) {
        this.time_zone = str;
        setValue();
    }

    public long getInsert_time() {
        return this.insert_time;
    }

    public void setInsert_time(long j) {
        this.insert_time = j;
        setValue();
    }

    public long getWeather_id() {
        return this.weather_id;
    }

    public void setWeather_id(long j) {
        this.weather_id = j;
        setValue();
    }

    public int getManual_set() {
        return this.manual_set;
    }

    public void setManual_set(int i) {
        this.manual_set = i;
        setValue();
    }

    public int getHome_city() {
        return this.home_city;
    }

    public void setHome_city(int i) {
        this.home_city = i;
        setValue();
    }

    public String getState_name_cn() {
        return this.state_name_cn;
    }

    public void setState_name_cn(String str) {
        this.state_name_cn = str;
        setValue();
    }

    public String getProvince_name() {
        return this.province_name;
    }

    public void setProvince_name(String str) {
        this.province_name = str;
        setValue();
    }

    public String getProvince_name_cn() {
        return this.province_name_cn;
    }

    public void setProvince_name_cn(String str) {
        this.province_name_cn = str;
        setValue();
    }

    public String getCountry_name() {
        return this.country_name;
    }

    public void setCountry_name(String str) {
        this.country_name = str;
        setValue();
    }

    public String getCountry_name_cn() {
        return this.country_name_cn;
    }

    public void setCountry_name_cn(String str) {
        this.country_name_cn = str;
        setValue();
    }

    public String getHw_id() {
        return this.hw_id;
    }

    public void setHw_id(String str) {
        this.hw_id = str;
        setValue();
    }

    public String getCo() {
        return this.co;
    }

    public void setCo(String str) {
        this.co = str;
        setValue();
    }

    public String getCa() {
        return this.ca;
    }

    public void setCa(String str) {
        this.ca = str;
        setValue();
    }

    public long getSequence_id() {
        return this.sequence_id;
    }

    public void setSequence_id(long j) {
        this.sequence_id = j;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this._id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this._id.longValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeLong(1);
        }
        if (this.city_name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.city_name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.city_alias != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.city_alias);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.city_native != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.city_native);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.state_name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.state_name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.city_code != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.city_code);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeInt(this.city_type);
        if (this.time_zone != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.time_zone);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeLong(this.insert_time);
        parcel.writeLong(this.weather_id);
        parcel.writeInt(this.manual_set);
        parcel.writeInt(this.home_city);
        if (this.state_name_cn != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.state_name_cn);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.province_name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.province_name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.province_name_cn != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.province_name_cn);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.country_name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.country_name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.country_name_cn != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.country_name_cn);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.hw_id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.hw_id);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.co != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.co);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.ca != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.ca);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeLong(this.sequence_id);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<CityInfoModel> getHelper() {
        return CityInfoModelHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "CityInfoModel { _id: " + this._id + ", city_name: " + this.city_name + ", city_alias: " + this.city_alias + ", city_native: " + this.city_native + ", state_name: " + this.state_name + ", city_code: " + this.city_code + ", city_type: " + this.city_type + ", time_zone: " + this.time_zone + ", insert_time: " + this.insert_time + ", weather_id: " + this.weather_id + ", manual_set: " + this.manual_set + ", home_city: " + this.home_city + ", state_name_cn: " + this.state_name_cn + ", province_name: " + this.province_name + ", province_name_cn: " + this.province_name_cn + ", country_name: " + this.country_name + ", country_name_cn: " + this.country_name_cn + ", hw_id: " + this.hw_id + ", co: " + this.co + ", ca: " + this.ca + ", sequence_id: " + this.sequence_id + " }";
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
