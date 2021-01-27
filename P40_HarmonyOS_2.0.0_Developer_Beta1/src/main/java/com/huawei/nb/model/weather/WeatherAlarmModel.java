package com.huawei.nb.model.weather;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class WeatherAlarmModel extends AManagedObject {
    public static final Parcelable.Creator<WeatherAlarmModel> CREATOR = new Parcelable.Creator<WeatherAlarmModel>() {
        /* class com.huawei.nb.model.weather.WeatherAlarmModel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WeatherAlarmModel createFromParcel(Parcel parcel) {
            return new WeatherAlarmModel(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public WeatherAlarmModel[] newArray(int i) {
            return new WeatherAlarmModel[i];
        }
    };
    private Long _id;
    private String alarm_content;
    private String alarm_id;
    private int alarm_type;
    private String alarm_type_name;
    private String city_name;
    private String county_name;
    private int level;
    private String level_name;
    private long observationtime;
    private String province_name;
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
        return "com.huawei.nb.model.weather.WeatherAlarmModel";
    }

    public String getEntityVersion() {
        return "0.0.14";
    }

    public int getEntityVersionCode() {
        return 14;
    }

    public WeatherAlarmModel(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this._id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.weather_id = cursor.getLong(2);
        this.alarm_id = cursor.getString(3);
        this.province_name = cursor.getString(4);
        this.city_name = cursor.getString(5);
        this.county_name = cursor.getString(6);
        this.alarm_type = cursor.getInt(7);
        this.alarm_type_name = cursor.getString(8);
        this.level = cursor.getInt(9);
        this.level_name = cursor.getString(10);
        this.observationtime = cursor.getLong(11);
        this.alarm_content = cursor.getString(12);
    }

    public WeatherAlarmModel(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this._id = null;
            parcel.readLong();
        } else {
            this._id = Long.valueOf(parcel.readLong());
        }
        this.weather_id = parcel.readLong();
        this.alarm_id = parcel.readByte() == 0 ? null : parcel.readString();
        this.province_name = parcel.readByte() == 0 ? null : parcel.readString();
        this.city_name = parcel.readByte() == 0 ? null : parcel.readString();
        this.county_name = parcel.readByte() == 0 ? null : parcel.readString();
        this.alarm_type = parcel.readInt();
        this.alarm_type_name = parcel.readByte() == 0 ? null : parcel.readString();
        this.level = parcel.readInt();
        this.level_name = parcel.readByte() == 0 ? null : parcel.readString();
        this.observationtime = parcel.readLong();
        this.alarm_content = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private WeatherAlarmModel(Long l, long j, String str, String str2, String str3, String str4, int i, String str5, int i2, String str6, long j2, String str7) {
        this._id = l;
        this.weather_id = j;
        this.alarm_id = str;
        this.province_name = str2;
        this.city_name = str3;
        this.county_name = str4;
        this.alarm_type = i;
        this.alarm_type_name = str5;
        this.level = i2;
        this.level_name = str6;
        this.observationtime = j2;
        this.alarm_content = str7;
    }

    public WeatherAlarmModel() {
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long l) {
        this._id = l;
        setValue();
    }

    public long getWeather_id() {
        return this.weather_id;
    }

    public void setWeather_id(long j) {
        this.weather_id = j;
        setValue();
    }

    public String getAlarm_id() {
        return this.alarm_id;
    }

    public void setAlarm_id(String str) {
        this.alarm_id = str;
        setValue();
    }

    public String getProvince_name() {
        return this.province_name;
    }

    public void setProvince_name(String str) {
        this.province_name = str;
        setValue();
    }

    public String getCity_name() {
        return this.city_name;
    }

    public void setCity_name(String str) {
        this.city_name = str;
        setValue();
    }

    public String getCounty_name() {
        return this.county_name;
    }

    public void setCounty_name(String str) {
        this.county_name = str;
        setValue();
    }

    public int getAlarm_type() {
        return this.alarm_type;
    }

    public void setAlarm_type(int i) {
        this.alarm_type = i;
        setValue();
    }

    public String getAlarm_type_name() {
        return this.alarm_type_name;
    }

    public void setAlarm_type_name(String str) {
        this.alarm_type_name = str;
        setValue();
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int i) {
        this.level = i;
        setValue();
    }

    public String getLevel_name() {
        return this.level_name;
    }

    public void setLevel_name(String str) {
        this.level_name = str;
        setValue();
    }

    public long getObservationtime() {
        return this.observationtime;
    }

    public void setObservationtime(long j) {
        this.observationtime = j;
        setValue();
    }

    public String getAlarm_content() {
        return this.alarm_content;
    }

    public void setAlarm_content(String str) {
        this.alarm_content = str;
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
        parcel.writeLong(this.weather_id);
        if (this.alarm_id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.alarm_id);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.province_name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.province_name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.city_name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.city_name);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.county_name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.county_name);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeInt(this.alarm_type);
        if (this.alarm_type_name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.alarm_type_name);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeInt(this.level);
        if (this.level_name != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.level_name);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeLong(this.observationtime);
        if (this.alarm_content != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.alarm_content);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<WeatherAlarmModel> getHelper() {
        return WeatherAlarmModelHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "WeatherAlarmModel { _id: " + this._id + ", weather_id: " + this.weather_id + ", alarm_id: " + this.alarm_id + ", province_name: " + this.province_name + ", city_name: " + this.city_name + ", county_name: " + this.county_name + ", alarm_type: " + this.alarm_type + ", alarm_type_name: " + this.alarm_type_name + ", level: " + this.level + ", level_name: " + this.level_name + ", observationtime: " + this.observationtime + ", alarm_content: " + this.alarm_content + " }";
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
