package com.huawei.nb.model.weather;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class WeatherDayInfoModel extends AManagedObject {
    public static final Parcelable.Creator<WeatherDayInfoModel> CREATOR = new Parcelable.Creator<WeatherDayInfoModel>() {
        /* class com.huawei.nb.model.weather.WeatherDayInfoModel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WeatherDayInfoModel createFromParcel(Parcel parcel) {
            return new WeatherDayInfoModel(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public WeatherDayInfoModel[] newArray(int i) {
            return new WeatherDayInfoModel[i];
        }
    };
    private Long _id;
    private String day_code;
    private int day_index;
    private float high_temp;
    private float low_temp;
    private String mobile_link;
    private int moon_type = -1;
    private float night_high_temp;
    private float night_low_temp;
    private String night_text_long;
    private String night_text_short;
    private int night_weather_icon;
    private String night_wind_direction;
    private int night_wind_speed;
    private long obs_date;
    private long sun_rise_time;
    private long sun_set_time;
    private String text_long;
    private String text_short;
    private int weather_icon;
    private long weather_info_id;
    private String wind_direction;
    private int wind_speed;

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
        return "com.huawei.nb.model.weather.WeatherDayInfoModel";
    }

    public String getEntityVersion() {
        return "0.0.14";
    }

    public int getEntityVersionCode() {
        return 14;
    }

    public WeatherDayInfoModel(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this._id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.weather_info_id = cursor.getLong(2);
        this.day_index = cursor.getInt(3);
        this.obs_date = cursor.getLong(4);
        this.day_code = cursor.getString(5);
        this.sun_rise_time = cursor.getLong(6);
        this.sun_set_time = cursor.getLong(7);
        this.high_temp = cursor.getFloat(8);
        this.low_temp = cursor.getFloat(9);
        this.weather_icon = cursor.getInt(10);
        this.wind_speed = cursor.getInt(11);
        this.wind_direction = cursor.getString(12);
        this.text_short = cursor.getString(13);
        this.text_long = cursor.getString(14);
        this.night_high_temp = cursor.getFloat(15);
        this.night_low_temp = cursor.getFloat(16);
        this.night_weather_icon = cursor.getInt(17);
        this.night_wind_speed = cursor.getInt(18);
        this.night_wind_direction = cursor.getString(19);
        this.night_text_short = cursor.getString(20);
        this.night_text_long = cursor.getString(21);
        this.moon_type = cursor.getInt(22);
        this.mobile_link = cursor.getString(23);
    }

    public WeatherDayInfoModel(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this._id = null;
            parcel.readLong();
        } else {
            this._id = Long.valueOf(parcel.readLong());
        }
        this.weather_info_id = parcel.readLong();
        this.day_index = parcel.readInt();
        this.obs_date = parcel.readLong();
        this.day_code = parcel.readByte() == 0 ? null : parcel.readString();
        this.sun_rise_time = parcel.readLong();
        this.sun_set_time = parcel.readLong();
        this.high_temp = parcel.readFloat();
        this.low_temp = parcel.readFloat();
        this.weather_icon = parcel.readInt();
        this.wind_speed = parcel.readInt();
        this.wind_direction = parcel.readByte() == 0 ? null : parcel.readString();
        this.text_short = parcel.readByte() == 0 ? null : parcel.readString();
        this.text_long = parcel.readByte() == 0 ? null : parcel.readString();
        this.night_high_temp = parcel.readFloat();
        this.night_low_temp = parcel.readFloat();
        this.night_weather_icon = parcel.readInt();
        this.night_wind_speed = parcel.readInt();
        this.night_wind_direction = parcel.readByte() == 0 ? null : parcel.readString();
        this.night_text_short = parcel.readByte() == 0 ? null : parcel.readString();
        this.night_text_long = parcel.readByte() == 0 ? null : parcel.readString();
        this.moon_type = parcel.readInt();
        this.mobile_link = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private WeatherDayInfoModel(Long l, long j, int i, long j2, String str, long j3, long j4, float f, float f2, int i2, int i3, String str2, String str3, String str4, float f3, float f4, int i4, int i5, String str5, String str6, String str7, int i6, String str8) {
        this._id = l;
        this.weather_info_id = j;
        this.day_index = i;
        this.obs_date = j2;
        this.day_code = str;
        this.sun_rise_time = j3;
        this.sun_set_time = j4;
        this.high_temp = f;
        this.low_temp = f2;
        this.weather_icon = i2;
        this.wind_speed = i3;
        this.wind_direction = str2;
        this.text_short = str3;
        this.text_long = str4;
        this.night_high_temp = f3;
        this.night_low_temp = f4;
        this.night_weather_icon = i4;
        this.night_wind_speed = i5;
        this.night_wind_direction = str5;
        this.night_text_short = str6;
        this.night_text_long = str7;
        this.moon_type = i6;
        this.mobile_link = str8;
    }

    public WeatherDayInfoModel() {
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long l) {
        this._id = l;
        setValue();
    }

    public long getWeather_info_id() {
        return this.weather_info_id;
    }

    public void setWeather_info_id(long j) {
        this.weather_info_id = j;
        setValue();
    }

    public int getDay_index() {
        return this.day_index;
    }

    public void setDay_index(int i) {
        this.day_index = i;
        setValue();
    }

    public long getObs_date() {
        return this.obs_date;
    }

    public void setObs_date(long j) {
        this.obs_date = j;
        setValue();
    }

    public String getDay_code() {
        return this.day_code;
    }

    public void setDay_code(String str) {
        this.day_code = str;
        setValue();
    }

    public long getSun_rise_time() {
        return this.sun_rise_time;
    }

    public void setSun_rise_time(long j) {
        this.sun_rise_time = j;
        setValue();
    }

    public long getSun_set_time() {
        return this.sun_set_time;
    }

    public void setSun_set_time(long j) {
        this.sun_set_time = j;
        setValue();
    }

    public float getHigh_temp() {
        return this.high_temp;
    }

    public void setHigh_temp(float f) {
        this.high_temp = f;
        setValue();
    }

    public float getLow_temp() {
        return this.low_temp;
    }

    public void setLow_temp(float f) {
        this.low_temp = f;
        setValue();
    }

    public int getWeather_icon() {
        return this.weather_icon;
    }

    public void setWeather_icon(int i) {
        this.weather_icon = i;
        setValue();
    }

    public int getWind_speed() {
        return this.wind_speed;
    }

    public void setWind_speed(int i) {
        this.wind_speed = i;
        setValue();
    }

    public String getWind_direction() {
        return this.wind_direction;
    }

    public void setWind_direction(String str) {
        this.wind_direction = str;
        setValue();
    }

    public String getText_short() {
        return this.text_short;
    }

    public void setText_short(String str) {
        this.text_short = str;
        setValue();
    }

    public String getText_long() {
        return this.text_long;
    }

    public void setText_long(String str) {
        this.text_long = str;
        setValue();
    }

    public float getNight_high_temp() {
        return this.night_high_temp;
    }

    public void setNight_high_temp(float f) {
        this.night_high_temp = f;
        setValue();
    }

    public float getNight_low_temp() {
        return this.night_low_temp;
    }

    public void setNight_low_temp(float f) {
        this.night_low_temp = f;
        setValue();
    }

    public int getNight_weather_icon() {
        return this.night_weather_icon;
    }

    public void setNight_weather_icon(int i) {
        this.night_weather_icon = i;
        setValue();
    }

    public int getNight_wind_speed() {
        return this.night_wind_speed;
    }

    public void setNight_wind_speed(int i) {
        this.night_wind_speed = i;
        setValue();
    }

    public String getNight_wind_direction() {
        return this.night_wind_direction;
    }

    public void setNight_wind_direction(String str) {
        this.night_wind_direction = str;
        setValue();
    }

    public String getNight_text_short() {
        return this.night_text_short;
    }

    public void setNight_text_short(String str) {
        this.night_text_short = str;
        setValue();
    }

    public String getNight_text_long() {
        return this.night_text_long;
    }

    public void setNight_text_long(String str) {
        this.night_text_long = str;
        setValue();
    }

    public int getMoon_type() {
        return this.moon_type;
    }

    public void setMoon_type(int i) {
        this.moon_type = i;
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
        parcel.writeLong(this.weather_info_id);
        parcel.writeInt(this.day_index);
        parcel.writeLong(this.obs_date);
        if (this.day_code != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.day_code);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeLong(this.sun_rise_time);
        parcel.writeLong(this.sun_set_time);
        parcel.writeFloat(this.high_temp);
        parcel.writeFloat(this.low_temp);
        parcel.writeInt(this.weather_icon);
        parcel.writeInt(this.wind_speed);
        if (this.wind_direction != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.wind_direction);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.text_short != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.text_short);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.text_long != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.text_long);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeFloat(this.night_high_temp);
        parcel.writeFloat(this.night_low_temp);
        parcel.writeInt(this.night_weather_icon);
        parcel.writeInt(this.night_wind_speed);
        if (this.night_wind_direction != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.night_wind_direction);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.night_text_short != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.night_text_short);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.night_text_long != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.night_text_long);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeInt(this.moon_type);
        if (this.mobile_link != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mobile_link);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<WeatherDayInfoModel> getHelper() {
        return WeatherDayInfoModelHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "WeatherDayInfoModel { _id: " + this._id + ", weather_info_id: " + this.weather_info_id + ", day_index: " + this.day_index + ", obs_date: " + this.obs_date + ", day_code: " + this.day_code + ", sun_rise_time: " + this.sun_rise_time + ", sun_set_time: " + this.sun_set_time + ", high_temp: " + this.high_temp + ", low_temp: " + this.low_temp + ", weather_icon: " + this.weather_icon + ", wind_speed: " + this.wind_speed + ", wind_direction: " + this.wind_direction + ", text_short: " + this.text_short + ", text_long: " + this.text_long + ", night_high_temp: " + this.night_high_temp + ", night_low_temp: " + this.night_low_temp + ", night_weather_icon: " + this.night_weather_icon + ", night_wind_speed: " + this.night_wind_speed + ", night_wind_direction: " + this.night_wind_direction + ", night_text_short: " + this.night_text_short + ", night_text_long: " + this.night_text_long + ", moon_type: " + this.moon_type + ", mobile_link: " + this.mobile_link + " }";
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
