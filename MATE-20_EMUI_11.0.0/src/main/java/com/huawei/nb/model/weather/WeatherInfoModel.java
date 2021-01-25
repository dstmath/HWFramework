package com.huawei.nb.model.weather;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class WeatherInfoModel extends AManagedObject {
    public static final Parcelable.Creator<WeatherInfoModel> CREATOR = new Parcelable.Creator<WeatherInfoModel>() {
        /* class com.huawei.nb.model.weather.WeatherInfoModel.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public WeatherInfoModel createFromParcel(Parcel parcel) {
            return new WeatherInfoModel(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public WeatherInfoModel[] newArray(int i) {
            return new WeatherInfoModel[i];
        }
    };
    private Long _id;
    private float air_pressure = -1.0f;
    private String city_code;
    private float co;
    private String humidity;
    private int isday_light;
    private String mobile_link;
    private String ninety_mobile_link;
    private float no2;
    private float o3;
    private long observation_time;
    private String p_desc_cn;
    private String p_desc_en;
    private int p_num;
    private String p_status_cn;
    private String p_status_en;
    private float pm10;
    private float pm2_5;
    private float realfeel = -99.0f;
    private float so2;
    private int status;
    private float temperature;
    private String time_zone;
    private long update_time;
    private int uv_index = -1;
    private int weather_icon;
    private String weather_text;
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
        return "com.huawei.nb.model.weather.WeatherInfoModel";
    }

    public String getEntityVersion() {
        return "0.0.14";
    }

    public int getEntityVersionCode() {
        return 14;
    }

    public WeatherInfoModel(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        this._id = cursor.isNull(1) ? null : Long.valueOf(cursor.getLong(1));
        this.status = cursor.getInt(2);
        this.city_code = cursor.getString(3);
        this.time_zone = cursor.getString(4);
        this.update_time = cursor.getLong(5);
        this.isday_light = cursor.getInt(6);
        this.temperature = cursor.getFloat(7);
        this.weather_icon = cursor.getInt(8);
        this.weather_text = cursor.getString(9);
        this.observation_time = cursor.getLong(10);
        this.wind_speed = cursor.getInt(11);
        this.wind_direction = cursor.getString(12);
        this.p_num = cursor.getInt(13);
        this.p_status_cn = cursor.getString(14);
        this.p_status_en = cursor.getString(15);
        this.pm10 = cursor.getFloat(16);
        this.pm2_5 = cursor.getFloat(17);
        this.no2 = cursor.getFloat(18);
        this.so2 = cursor.getFloat(19);
        this.o3 = cursor.getFloat(20);
        this.co = cursor.getFloat(21);
        this.p_desc_en = cursor.getString(22);
        this.p_desc_cn = cursor.getString(23);
        this.humidity = cursor.getString(24);
        this.realfeel = cursor.getFloat(25);
        this.mobile_link = cursor.getString(26);
        this.ninety_mobile_link = cursor.getString(27);
        this.uv_index = cursor.getInt(28);
        this.air_pressure = cursor.getFloat(29);
    }

    public WeatherInfoModel(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this._id = null;
            parcel.readLong();
        } else {
            this._id = Long.valueOf(parcel.readLong());
        }
        this.status = parcel.readInt();
        this.city_code = parcel.readByte() == 0 ? null : parcel.readString();
        this.time_zone = parcel.readByte() == 0 ? null : parcel.readString();
        this.update_time = parcel.readLong();
        this.isday_light = parcel.readInt();
        this.temperature = parcel.readFloat();
        this.weather_icon = parcel.readInt();
        this.weather_text = parcel.readByte() == 0 ? null : parcel.readString();
        this.observation_time = parcel.readLong();
        this.wind_speed = parcel.readInt();
        this.wind_direction = parcel.readByte() == 0 ? null : parcel.readString();
        this.p_num = parcel.readInt();
        this.p_status_cn = parcel.readByte() == 0 ? null : parcel.readString();
        this.p_status_en = parcel.readByte() == 0 ? null : parcel.readString();
        this.pm10 = parcel.readFloat();
        this.pm2_5 = parcel.readFloat();
        this.no2 = parcel.readFloat();
        this.so2 = parcel.readFloat();
        this.o3 = parcel.readFloat();
        this.co = parcel.readFloat();
        this.p_desc_en = parcel.readByte() == 0 ? null : parcel.readString();
        this.p_desc_cn = parcel.readByte() == 0 ? null : parcel.readString();
        this.humidity = parcel.readByte() == 0 ? null : parcel.readString();
        this.realfeel = parcel.readFloat();
        this.mobile_link = parcel.readByte() == 0 ? null : parcel.readString();
        this.ninety_mobile_link = parcel.readByte() != 0 ? parcel.readString() : str;
        this.uv_index = parcel.readInt();
        this.air_pressure = parcel.readFloat();
    }

    private WeatherInfoModel(Long l, int i, String str, String str2, long j, int i2, float f, int i3, String str3, long j2, int i4, String str4, int i5, String str5, String str6, float f2, float f3, float f4, float f5, float f6, float f7, String str7, String str8, String str9, float f8, String str10, String str11, int i6, float f9) {
        this._id = l;
        this.status = i;
        this.city_code = str;
        this.time_zone = str2;
        this.update_time = j;
        this.isday_light = i2;
        this.temperature = f;
        this.weather_icon = i3;
        this.weather_text = str3;
        this.observation_time = j2;
        this.wind_speed = i4;
        this.wind_direction = str4;
        this.p_num = i5;
        this.p_status_cn = str5;
        this.p_status_en = str6;
        this.pm10 = f2;
        this.pm2_5 = f3;
        this.no2 = f4;
        this.so2 = f5;
        this.o3 = f6;
        this.co = f7;
        this.p_desc_en = str7;
        this.p_desc_cn = str8;
        this.humidity = str9;
        this.realfeel = f8;
        this.mobile_link = str10;
        this.ninety_mobile_link = str11;
        this.uv_index = i6;
        this.air_pressure = f9;
    }

    public WeatherInfoModel() {
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long l) {
        this._id = l;
        setValue();
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int i) {
        this.status = i;
        setValue();
    }

    public String getCity_code() {
        return this.city_code;
    }

    public void setCity_code(String str) {
        this.city_code = str;
        setValue();
    }

    public String getTime_zone() {
        return this.time_zone;
    }

    public void setTime_zone(String str) {
        this.time_zone = str;
        setValue();
    }

    public long getUpdate_time() {
        return this.update_time;
    }

    public void setUpdate_time(long j) {
        this.update_time = j;
        setValue();
    }

    public int getIsday_light() {
        return this.isday_light;
    }

    public void setIsday_light(int i) {
        this.isday_light = i;
        setValue();
    }

    public float getTemperature() {
        return this.temperature;
    }

    public void setTemperature(float f) {
        this.temperature = f;
        setValue();
    }

    public int getWeather_icon() {
        return this.weather_icon;
    }

    public void setWeather_icon(int i) {
        this.weather_icon = i;
        setValue();
    }

    public String getWeather_text() {
        return this.weather_text;
    }

    public void setWeather_text(String str) {
        this.weather_text = str;
        setValue();
    }

    public long getObservation_time() {
        return this.observation_time;
    }

    public void setObservation_time(long j) {
        this.observation_time = j;
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

    public int getP_num() {
        return this.p_num;
    }

    public void setP_num(int i) {
        this.p_num = i;
        setValue();
    }

    public String getP_status_cn() {
        return this.p_status_cn;
    }

    public void setP_status_cn(String str) {
        this.p_status_cn = str;
        setValue();
    }

    public String getP_status_en() {
        return this.p_status_en;
    }

    public void setP_status_en(String str) {
        this.p_status_en = str;
        setValue();
    }

    public float getPm10() {
        return this.pm10;
    }

    public void setPm10(float f) {
        this.pm10 = f;
        setValue();
    }

    public float getPm2_5() {
        return this.pm2_5;
    }

    public void setPm2_5(float f) {
        this.pm2_5 = f;
        setValue();
    }

    public float getNo2() {
        return this.no2;
    }

    public void setNo2(float f) {
        this.no2 = f;
        setValue();
    }

    public float getSo2() {
        return this.so2;
    }

    public void setSo2(float f) {
        this.so2 = f;
        setValue();
    }

    public float getO3() {
        return this.o3;
    }

    public void setO3(float f) {
        this.o3 = f;
        setValue();
    }

    public float getCo() {
        return this.co;
    }

    public void setCo(float f) {
        this.co = f;
        setValue();
    }

    public String getP_desc_en() {
        return this.p_desc_en;
    }

    public void setP_desc_en(String str) {
        this.p_desc_en = str;
        setValue();
    }

    public String getP_desc_cn() {
        return this.p_desc_cn;
    }

    public void setP_desc_cn(String str) {
        this.p_desc_cn = str;
        setValue();
    }

    public String getHumidity() {
        return this.humidity;
    }

    public void setHumidity(String str) {
        this.humidity = str;
        setValue();
    }

    public float getRealfeel() {
        return this.realfeel;
    }

    public void setRealfeel(float f) {
        this.realfeel = f;
        setValue();
    }

    public String getMobile_link() {
        return this.mobile_link;
    }

    public void setMobile_link(String str) {
        this.mobile_link = str;
        setValue();
    }

    public String getNinety_mobile_link() {
        return this.ninety_mobile_link;
    }

    public void setNinety_mobile_link(String str) {
        this.ninety_mobile_link = str;
        setValue();
    }

    public int getUv_index() {
        return this.uv_index;
    }

    public void setUv_index(int i) {
        this.uv_index = i;
        setValue();
    }

    public float getAir_pressure() {
        return this.air_pressure;
    }

    public void setAir_pressure(float f) {
        this.air_pressure = f;
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
        parcel.writeInt(this.status);
        if (this.city_code != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.city_code);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.time_zone != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.time_zone);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeLong(this.update_time);
        parcel.writeInt(this.isday_light);
        parcel.writeFloat(this.temperature);
        parcel.writeInt(this.weather_icon);
        if (this.weather_text != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.weather_text);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeLong(this.observation_time);
        parcel.writeInt(this.wind_speed);
        if (this.wind_direction != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.wind_direction);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeInt(this.p_num);
        if (this.p_status_cn != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.p_status_cn);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.p_status_en != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.p_status_en);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeFloat(this.pm10);
        parcel.writeFloat(this.pm2_5);
        parcel.writeFloat(this.no2);
        parcel.writeFloat(this.so2);
        parcel.writeFloat(this.o3);
        parcel.writeFloat(this.co);
        if (this.p_desc_en != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.p_desc_en);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.p_desc_cn != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.p_desc_cn);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.humidity != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.humidity);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeFloat(this.realfeel);
        if (this.mobile_link != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mobile_link);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.ninety_mobile_link != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.ninety_mobile_link);
        } else {
            parcel.writeByte((byte) 0);
        }
        parcel.writeInt(this.uv_index);
        parcel.writeFloat(this.air_pressure);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<WeatherInfoModel> getHelper() {
        return WeatherInfoModelHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "WeatherInfoModel { _id: " + this._id + ", status: " + this.status + ", city_code: " + this.city_code + ", time_zone: " + this.time_zone + ", update_time: " + this.update_time + ", isday_light: " + this.isday_light + ", temperature: " + this.temperature + ", weather_icon: " + this.weather_icon + ", weather_text: " + this.weather_text + ", observation_time: " + this.observation_time + ", wind_speed: " + this.wind_speed + ", wind_direction: " + this.wind_direction + ", p_num: " + this.p_num + ", p_status_cn: " + this.p_status_cn + ", p_status_en: " + this.p_status_en + ", pm10: " + this.pm10 + ", pm2_5: " + this.pm2_5 + ", no2: " + this.no2 + ", so2: " + this.so2 + ", o3: " + this.o3 + ", co: " + this.co + ", p_desc_en: " + this.p_desc_en + ", p_desc_cn: " + this.p_desc_cn + ", humidity: " + this.humidity + ", realfeel: " + this.realfeel + ", mobile_link: " + this.mobile_link + ", ninety_mobile_link: " + this.ninety_mobile_link + ", uv_index: " + this.uv_index + ", air_pressure: " + this.air_pressure + " }";
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
