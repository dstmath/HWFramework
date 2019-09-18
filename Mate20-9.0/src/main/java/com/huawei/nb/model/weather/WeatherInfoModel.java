package com.huawei.nb.model.weather;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class WeatherInfoModel extends AManagedObject {
    public static final Parcelable.Creator<WeatherInfoModel> CREATOR = new Parcelable.Creator<WeatherInfoModel>() {
        public WeatherInfoModel createFromParcel(Parcel in) {
            return new WeatherInfoModel(in);
        }

        public WeatherInfoModel[] newArray(int size) {
            return new WeatherInfoModel[size];
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public WeatherInfoModel(Parcel in) {
        super(in);
        String str = null;
        if (in.readByte() == 0) {
            this._id = null;
            in.readLong();
        } else {
            this._id = Long.valueOf(in.readLong());
        }
        this.status = in.readInt();
        this.city_code = in.readByte() == 0 ? null : in.readString();
        this.time_zone = in.readByte() == 0 ? null : in.readString();
        this.update_time = in.readLong();
        this.isday_light = in.readInt();
        this.temperature = in.readFloat();
        this.weather_icon = in.readInt();
        this.weather_text = in.readByte() == 0 ? null : in.readString();
        this.observation_time = in.readLong();
        this.wind_speed = in.readInt();
        this.wind_direction = in.readByte() == 0 ? null : in.readString();
        this.p_num = in.readInt();
        this.p_status_cn = in.readByte() == 0 ? null : in.readString();
        this.p_status_en = in.readByte() == 0 ? null : in.readString();
        this.pm10 = in.readFloat();
        this.pm2_5 = in.readFloat();
        this.no2 = in.readFloat();
        this.so2 = in.readFloat();
        this.o3 = in.readFloat();
        this.co = in.readFloat();
        this.p_desc_en = in.readByte() == 0 ? null : in.readString();
        this.p_desc_cn = in.readByte() == 0 ? null : in.readString();
        this.humidity = in.readByte() == 0 ? null : in.readString();
        this.realfeel = in.readFloat();
        this.mobile_link = in.readByte() == 0 ? null : in.readString();
        this.ninety_mobile_link = in.readByte() != 0 ? in.readString() : str;
        this.uv_index = in.readInt();
        this.air_pressure = in.readFloat();
    }

    private WeatherInfoModel(Long _id2, int status2, String city_code2, String time_zone2, long update_time2, int isday_light2, float temperature2, int weather_icon2, String weather_text2, long observation_time2, int wind_speed2, String wind_direction2, int p_num2, String p_status_cn2, String p_status_en2, float pm102, float pm2_52, float no22, float so22, float o32, float co2, String p_desc_en2, String p_desc_cn2, String humidity2, float realfeel2, String mobile_link2, String ninety_mobile_link2, int uv_index2, float air_pressure2) {
        this._id = _id2;
        this.status = status2;
        this.city_code = city_code2;
        this.time_zone = time_zone2;
        this.update_time = update_time2;
        this.isday_light = isday_light2;
        this.temperature = temperature2;
        this.weather_icon = weather_icon2;
        this.weather_text = weather_text2;
        this.observation_time = observation_time2;
        this.wind_speed = wind_speed2;
        this.wind_direction = wind_direction2;
        this.p_num = p_num2;
        this.p_status_cn = p_status_cn2;
        this.p_status_en = p_status_en2;
        this.pm10 = pm102;
        this.pm2_5 = pm2_52;
        this.no2 = no22;
        this.so2 = so22;
        this.o3 = o32;
        this.co = co2;
        this.p_desc_en = p_desc_en2;
        this.p_desc_cn = p_desc_cn2;
        this.humidity = humidity2;
        this.realfeel = realfeel2;
        this.mobile_link = mobile_link2;
        this.ninety_mobile_link = ninety_mobile_link2;
        this.uv_index = uv_index2;
        this.air_pressure = air_pressure2;
    }

    public WeatherInfoModel() {
    }

    public int describeContents() {
        return 0;
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long _id2) {
        this._id = _id2;
        setValue();
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status2) {
        this.status = status2;
        setValue();
    }

    public String getCity_code() {
        return this.city_code;
    }

    public void setCity_code(String city_code2) {
        this.city_code = city_code2;
        setValue();
    }

    public String getTime_zone() {
        return this.time_zone;
    }

    public void setTime_zone(String time_zone2) {
        this.time_zone = time_zone2;
        setValue();
    }

    public long getUpdate_time() {
        return this.update_time;
    }

    public void setUpdate_time(long update_time2) {
        this.update_time = update_time2;
        setValue();
    }

    public int getIsday_light() {
        return this.isday_light;
    }

    public void setIsday_light(int isday_light2) {
        this.isday_light = isday_light2;
        setValue();
    }

    public float getTemperature() {
        return this.temperature;
    }

    public void setTemperature(float temperature2) {
        this.temperature = temperature2;
        setValue();
    }

    public int getWeather_icon() {
        return this.weather_icon;
    }

    public void setWeather_icon(int weather_icon2) {
        this.weather_icon = weather_icon2;
        setValue();
    }

    public String getWeather_text() {
        return this.weather_text;
    }

    public void setWeather_text(String weather_text2) {
        this.weather_text = weather_text2;
        setValue();
    }

    public long getObservation_time() {
        return this.observation_time;
    }

    public void setObservation_time(long observation_time2) {
        this.observation_time = observation_time2;
        setValue();
    }

    public int getWind_speed() {
        return this.wind_speed;
    }

    public void setWind_speed(int wind_speed2) {
        this.wind_speed = wind_speed2;
        setValue();
    }

    public String getWind_direction() {
        return this.wind_direction;
    }

    public void setWind_direction(String wind_direction2) {
        this.wind_direction = wind_direction2;
        setValue();
    }

    public int getP_num() {
        return this.p_num;
    }

    public void setP_num(int p_num2) {
        this.p_num = p_num2;
        setValue();
    }

    public String getP_status_cn() {
        return this.p_status_cn;
    }

    public void setP_status_cn(String p_status_cn2) {
        this.p_status_cn = p_status_cn2;
        setValue();
    }

    public String getP_status_en() {
        return this.p_status_en;
    }

    public void setP_status_en(String p_status_en2) {
        this.p_status_en = p_status_en2;
        setValue();
    }

    public float getPm10() {
        return this.pm10;
    }

    public void setPm10(float pm102) {
        this.pm10 = pm102;
        setValue();
    }

    public float getPm2_5() {
        return this.pm2_5;
    }

    public void setPm2_5(float pm2_52) {
        this.pm2_5 = pm2_52;
        setValue();
    }

    public float getNo2() {
        return this.no2;
    }

    public void setNo2(float no22) {
        this.no2 = no22;
        setValue();
    }

    public float getSo2() {
        return this.so2;
    }

    public void setSo2(float so22) {
        this.so2 = so22;
        setValue();
    }

    public float getO3() {
        return this.o3;
    }

    public void setO3(float o32) {
        this.o3 = o32;
        setValue();
    }

    public float getCo() {
        return this.co;
    }

    public void setCo(float co2) {
        this.co = co2;
        setValue();
    }

    public String getP_desc_en() {
        return this.p_desc_en;
    }

    public void setP_desc_en(String p_desc_en2) {
        this.p_desc_en = p_desc_en2;
        setValue();
    }

    public String getP_desc_cn() {
        return this.p_desc_cn;
    }

    public void setP_desc_cn(String p_desc_cn2) {
        this.p_desc_cn = p_desc_cn2;
        setValue();
    }

    public String getHumidity() {
        return this.humidity;
    }

    public void setHumidity(String humidity2) {
        this.humidity = humidity2;
        setValue();
    }

    public float getRealfeel() {
        return this.realfeel;
    }

    public void setRealfeel(float realfeel2) {
        this.realfeel = realfeel2;
        setValue();
    }

    public String getMobile_link() {
        return this.mobile_link;
    }

    public void setMobile_link(String mobile_link2) {
        this.mobile_link = mobile_link2;
        setValue();
    }

    public String getNinety_mobile_link() {
        return this.ninety_mobile_link;
    }

    public void setNinety_mobile_link(String ninety_mobile_link2) {
        this.ninety_mobile_link = ninety_mobile_link2;
        setValue();
    }

    public int getUv_index() {
        return this.uv_index;
    }

    public void setUv_index(int uv_index2) {
        this.uv_index = uv_index2;
        setValue();
    }

    public float getAir_pressure() {
        return this.air_pressure;
    }

    public void setAir_pressure(float air_pressure2) {
        this.air_pressure = air_pressure2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this._id != null) {
            out.writeByte((byte) 1);
            out.writeLong(this._id.longValue());
        } else {
            out.writeByte((byte) 0);
            out.writeLong(1);
        }
        out.writeInt(this.status);
        if (this.city_code != null) {
            out.writeByte((byte) 1);
            out.writeString(this.city_code);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.time_zone != null) {
            out.writeByte((byte) 1);
            out.writeString(this.time_zone);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeLong(this.update_time);
        out.writeInt(this.isday_light);
        out.writeFloat(this.temperature);
        out.writeInt(this.weather_icon);
        if (this.weather_text != null) {
            out.writeByte((byte) 1);
            out.writeString(this.weather_text);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeLong(this.observation_time);
        out.writeInt(this.wind_speed);
        if (this.wind_direction != null) {
            out.writeByte((byte) 1);
            out.writeString(this.wind_direction);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeInt(this.p_num);
        if (this.p_status_cn != null) {
            out.writeByte((byte) 1);
            out.writeString(this.p_status_cn);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.p_status_en != null) {
            out.writeByte((byte) 1);
            out.writeString(this.p_status_en);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeFloat(this.pm10);
        out.writeFloat(this.pm2_5);
        out.writeFloat(this.no2);
        out.writeFloat(this.so2);
        out.writeFloat(this.o3);
        out.writeFloat(this.co);
        if (this.p_desc_en != null) {
            out.writeByte((byte) 1);
            out.writeString(this.p_desc_en);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.p_desc_cn != null) {
            out.writeByte((byte) 1);
            out.writeString(this.p_desc_cn);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.humidity != null) {
            out.writeByte((byte) 1);
            out.writeString(this.humidity);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeFloat(this.realfeel);
        if (this.mobile_link != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mobile_link);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.ninety_mobile_link != null) {
            out.writeByte((byte) 1);
            out.writeString(this.ninety_mobile_link);
        } else {
            out.writeByte((byte) 0);
        }
        out.writeInt(this.uv_index);
        out.writeFloat(this.air_pressure);
    }

    public AEntityHelper<WeatherInfoModel> getHelper() {
        return WeatherInfoModelHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.weather.WeatherInfoModel";
    }

    public String getDatabaseName() {
        return "dsWeather";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("WeatherInfoModel { _id: ").append(this._id);
        sb.append(", status: ").append(this.status);
        sb.append(", city_code: ").append(this.city_code);
        sb.append(", time_zone: ").append(this.time_zone);
        sb.append(", update_time: ").append(this.update_time);
        sb.append(", isday_light: ").append(this.isday_light);
        sb.append(", temperature: ").append(this.temperature);
        sb.append(", weather_icon: ").append(this.weather_icon);
        sb.append(", weather_text: ").append(this.weather_text);
        sb.append(", observation_time: ").append(this.observation_time);
        sb.append(", wind_speed: ").append(this.wind_speed);
        sb.append(", wind_direction: ").append(this.wind_direction);
        sb.append(", p_num: ").append(this.p_num);
        sb.append(", p_status_cn: ").append(this.p_status_cn);
        sb.append(", p_status_en: ").append(this.p_status_en);
        sb.append(", pm10: ").append(this.pm10);
        sb.append(", pm2_5: ").append(this.pm2_5);
        sb.append(", no2: ").append(this.no2);
        sb.append(", so2: ").append(this.so2);
        sb.append(", o3: ").append(this.o3);
        sb.append(", co: ").append(this.co);
        sb.append(", p_desc_en: ").append(this.p_desc_en);
        sb.append(", p_desc_cn: ").append(this.p_desc_cn);
        sb.append(", humidity: ").append(this.humidity);
        sb.append(", realfeel: ").append(this.realfeel);
        sb.append(", mobile_link: ").append(this.mobile_link);
        sb.append(", ninety_mobile_link: ").append(this.ninety_mobile_link);
        sb.append(", uv_index: ").append(this.uv_index);
        sb.append(", air_pressure: ").append(this.air_pressure);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.17";
    }

    public int getDatabaseVersionCode() {
        return 17;
    }

    public String getEntityVersion() {
        return "0.0.14";
    }

    public int getEntityVersionCode() {
        return 14;
    }
}
