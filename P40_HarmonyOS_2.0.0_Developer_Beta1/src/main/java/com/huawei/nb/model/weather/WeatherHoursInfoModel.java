package com.huawei.nb.model.weather;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class WeatherHoursInfoModel extends AManagedObject {
    public static final Parcelable.Creator<WeatherHoursInfoModel> CREATOR = new Parcelable.Creator<WeatherHoursInfoModel>() {
        /* class com.huawei.nb.model.weather.WeatherHoursInfoModel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WeatherHoursInfoModel createFromParcel(Parcel parcel) {
            return new WeatherHoursInfoModel(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public WeatherHoursInfoModel[] newArray(int i) {
            return new WeatherHoursInfoModel[i];
        }
    };
    private Long _id;
    private long forcase_date_time;
    private float hour_temprature;
    private int is_day_light;
    private String mobile_link;
    private float rain_probability;
    private int weather_icon;
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
        return "com.huawei.nb.model.weather.WeatherHoursInfoModel";
    }

    public String getEntityVersion() {
        return "0.0.14";
    }

    public int getEntityVersionCode() {
        return 14;
    }

    public WeatherHoursInfoModel(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this._id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.weather_id = cursor.getLong(2);
        this.forcase_date_time = cursor.getLong(3);
        this.weather_icon = cursor.getInt(4);
        this.hour_temprature = cursor.getFloat(5);
        this.is_day_light = cursor.getInt(6);
        this.rain_probability = cursor.getFloat(7);
        this.mobile_link = cursor.getString(8);
    }

    public WeatherHoursInfoModel(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this._id = null;
            parcel.readLong();
        } else {
            this._id = Long.valueOf(parcel.readLong());
        }
        this.weather_id = parcel.readLong();
        this.forcase_date_time = parcel.readLong();
        this.weather_icon = parcel.readInt();
        this.hour_temprature = parcel.readFloat();
        this.is_day_light = parcel.readInt();
        this.rain_probability = parcel.readFloat();
        this.mobile_link = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private WeatherHoursInfoModel(Long l, long j, long j2, int i, float f, int i2, float f2, String str) {
        this._id = l;
        this.weather_id = j;
        this.forcase_date_time = j2;
        this.weather_icon = i;
        this.hour_temprature = f;
        this.is_day_light = i2;
        this.rain_probability = f2;
        this.mobile_link = str;
    }

    public WeatherHoursInfoModel() {
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

    public long getForcase_date_time() {
        return this.forcase_date_time;
    }

    public void setForcase_date_time(long j) {
        this.forcase_date_time = j;
        setValue();
    }

    public int getWeather_icon() {
        return this.weather_icon;
    }

    public void setWeather_icon(int i) {
        this.weather_icon = i;
        setValue();
    }

    public float getHour_temprature() {
        return this.hour_temprature;
    }

    public void setHour_temprature(float f) {
        this.hour_temprature = f;
        setValue();
    }

    public int getIs_day_light() {
        return this.is_day_light;
    }

    public void setIs_day_light(int i) {
        this.is_day_light = i;
        setValue();
    }

    public float getRain_probability() {
        return this.rain_probability;
    }

    public void setRain_probability(float f) {
        this.rain_probability = f;
        setValue();
    }

    public String getMobile_link() {
        return this.mobile_link;
    }

    public void setMobile_link(String str) {
        this.mobile_link = str;
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
        parcel.writeLong(this.forcase_date_time);
        parcel.writeInt(this.weather_icon);
        parcel.writeFloat(this.hour_temprature);
        parcel.writeInt(this.is_day_light);
        parcel.writeFloat(this.rain_probability);
        if (this.mobile_link != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mobile_link);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<WeatherHoursInfoModel> getHelper() {
        return WeatherHoursInfoModelHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "WeatherHoursInfoModel { _id: " + this._id + ", weather_id: " + this.weather_id + ", forcase_date_time: " + this.forcase_date_time + ", weather_icon: " + this.weather_icon + ", hour_temprature: " + this.hour_temprature + ", is_day_light: " + this.is_day_light + ", rain_probability: " + this.rain_probability + ", mobile_link: " + this.mobile_link + " }";
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
