package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class MetaWeatherInfo extends AManagedObject {
    public static final Parcelable.Creator<MetaWeatherInfo> CREATOR = new Parcelable.Creator<MetaWeatherInfo>() {
        /* class com.huawei.nb.model.collectencrypt.MetaWeatherInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MetaWeatherInfo createFromParcel(Parcel parcel) {
            return new MetaWeatherInfo(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MetaWeatherInfo[] newArray(int i) {
            return new MetaWeatherInfo[i];
        }
    };
    private Float air_pressure = Float.valueOf(-1.0f);
    private String alarm_level;
    private String alarm_type;
    private String city_code;
    private Float co;
    private String humidity;
    private Integer id;
    private Integer mReservedInt;
    private String mReservedText;
    private Float no2;
    private Float o3;
    private Integer p_num;
    private String p_status_cn;
    private String p_status_en;
    private Float pm10;
    private Float pm2_5;
    private Float so2;
    private Float temperature_high;
    private Float temperature_low;
    private String time_zone;
    private Long update_time;
    private String weather_text;
    private String wind_direction;
    private Integer wind_speed;

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String getDatabaseVersion() {
        return "0.0.14";
    }

    public int getDatabaseVersionCode() {
        return 14;
    }

    @Override // com.huawei.odmf.core.AManagedObject, com.huawei.odmf.core.ManagedObject
    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.MetaWeatherInfo";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MetaWeatherInfo(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Integer num = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.city_code = cursor.getString(2);
        this.time_zone = cursor.getString(3);
        this.update_time = cursor.isNull(4) ? null : Long.valueOf(cursor.getLong(4));
        this.temperature_high = cursor.isNull(5) ? null : Float.valueOf(cursor.getFloat(5));
        this.temperature_low = cursor.isNull(6) ? null : Float.valueOf(cursor.getFloat(6));
        this.weather_text = cursor.getString(7);
        this.wind_speed = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        this.wind_direction = cursor.getString(9);
        this.p_num = cursor.isNull(10) ? null : Integer.valueOf(cursor.getInt(10));
        this.p_status_cn = cursor.getString(11);
        this.p_status_en = cursor.getString(12);
        this.pm10 = cursor.isNull(13) ? null : Float.valueOf(cursor.getFloat(13));
        this.pm2_5 = cursor.isNull(14) ? null : Float.valueOf(cursor.getFloat(14));
        this.no2 = cursor.isNull(15) ? null : Float.valueOf(cursor.getFloat(15));
        this.so2 = cursor.isNull(16) ? null : Float.valueOf(cursor.getFloat(16));
        this.o3 = cursor.isNull(17) ? null : Float.valueOf(cursor.getFloat(17));
        this.co = cursor.isNull(18) ? null : Float.valueOf(cursor.getFloat(18));
        this.humidity = cursor.getString(19);
        this.air_pressure = cursor.isNull(20) ? null : Float.valueOf(cursor.getFloat(20));
        this.alarm_type = cursor.getString(21);
        this.alarm_level = cursor.getString(22);
        this.mReservedInt = !cursor.isNull(23) ? Integer.valueOf(cursor.getInt(23)) : num;
        this.mReservedText = cursor.getString(24);
    }

    public MetaWeatherInfo(Parcel parcel) {
        super(parcel);
        String str = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.city_code = parcel.readByte() == 0 ? null : parcel.readString();
        this.time_zone = parcel.readByte() == 0 ? null : parcel.readString();
        this.update_time = parcel.readByte() == 0 ? null : Long.valueOf(parcel.readLong());
        this.temperature_high = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.temperature_low = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.weather_text = parcel.readByte() == 0 ? null : parcel.readString();
        this.wind_speed = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.wind_direction = parcel.readByte() == 0 ? null : parcel.readString();
        this.p_num = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.p_status_cn = parcel.readByte() == 0 ? null : parcel.readString();
        this.p_status_en = parcel.readByte() == 0 ? null : parcel.readString();
        this.pm10 = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.pm2_5 = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.no2 = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.so2 = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.o3 = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.co = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.humidity = parcel.readByte() == 0 ? null : parcel.readString();
        this.air_pressure = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.alarm_type = parcel.readByte() == 0 ? null : parcel.readString();
        this.alarm_level = parcel.readByte() == 0 ? null : parcel.readString();
        this.mReservedInt = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.mReservedText = parcel.readByte() != 0 ? parcel.readString() : str;
    }

    private MetaWeatherInfo(Integer num, String str, String str2, Long l, Float f, Float f2, String str3, Integer num2, String str4, Integer num3, String str5, String str6, Float f3, Float f4, Float f5, Float f6, Float f7, Float f8, String str7, Float f9, String str8, String str9, Integer num4, String str10) {
        this.id = num;
        this.city_code = str;
        this.time_zone = str2;
        this.update_time = l;
        this.temperature_high = f;
        this.temperature_low = f2;
        this.weather_text = str3;
        this.wind_speed = num2;
        this.wind_direction = str4;
        this.p_num = num3;
        this.p_status_cn = str5;
        this.p_status_en = str6;
        this.pm10 = f3;
        this.pm2_5 = f4;
        this.no2 = f5;
        this.so2 = f6;
        this.o3 = f7;
        this.co = f8;
        this.humidity = str7;
        this.air_pressure = f9;
        this.alarm_type = str8;
        this.alarm_level = str9;
        this.mReservedInt = num4;
        this.mReservedText = str10;
    }

    public MetaWeatherInfo() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
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

    public Long getUpdate_time() {
        return this.update_time;
    }

    public void setUpdate_time(Long l) {
        this.update_time = l;
        setValue();
    }

    public Float getTemperature_high() {
        return this.temperature_high;
    }

    public void setTemperature_high(Float f) {
        this.temperature_high = f;
        setValue();
    }

    public Float getTemperature_low() {
        return this.temperature_low;
    }

    public void setTemperature_low(Float f) {
        this.temperature_low = f;
        setValue();
    }

    public String getWeather_text() {
        return this.weather_text;
    }

    public void setWeather_text(String str) {
        this.weather_text = str;
        setValue();
    }

    public Integer getWind_speed() {
        return this.wind_speed;
    }

    public void setWind_speed(Integer num) {
        this.wind_speed = num;
        setValue();
    }

    public String getWind_direction() {
        return this.wind_direction;
    }

    public void setWind_direction(String str) {
        this.wind_direction = str;
        setValue();
    }

    public Integer getP_num() {
        return this.p_num;
    }

    public void setP_num(Integer num) {
        this.p_num = num;
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

    public Float getPm10() {
        return this.pm10;
    }

    public void setPm10(Float f) {
        this.pm10 = f;
        setValue();
    }

    public Float getPm2_5() {
        return this.pm2_5;
    }

    public void setPm2_5(Float f) {
        this.pm2_5 = f;
        setValue();
    }

    public Float getNo2() {
        return this.no2;
    }

    public void setNo2(Float f) {
        this.no2 = f;
        setValue();
    }

    public Float getSo2() {
        return this.so2;
    }

    public void setSo2(Float f) {
        this.so2 = f;
        setValue();
    }

    public Float getO3() {
        return this.o3;
    }

    public void setO3(Float f) {
        this.o3 = f;
        setValue();
    }

    public Float getCo() {
        return this.co;
    }

    public void setCo(Float f) {
        this.co = f;
        setValue();
    }

    public String getHumidity() {
        return this.humidity;
    }

    public void setHumidity(String str) {
        this.humidity = str;
        setValue();
    }

    public Float getAir_pressure() {
        return this.air_pressure;
    }

    public void setAir_pressure(Float f) {
        this.air_pressure = f;
        setValue();
    }

    public String getAlarm_type() {
        return this.alarm_type;
    }

    public void setAlarm_type(String str) {
        this.alarm_type = str;
        setValue();
    }

    public String getAlarm_level() {
        return this.alarm_level;
    }

    public void setAlarm_level(String str) {
        this.alarm_level = str;
        setValue();
    }

    public Integer getMReservedInt() {
        return this.mReservedInt;
    }

    public void setMReservedInt(Integer num) {
        this.mReservedInt = num;
        setValue();
    }

    public String getMReservedText() {
        return this.mReservedText;
    }

    public void setMReservedText(String str) {
        this.mReservedText = str;
        setValue();
    }

    @Override // com.huawei.odmf.core.AManagedObject, android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (this.id != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.id.intValue());
        } else {
            parcel.writeByte((byte) 0);
            parcel.writeInt(1);
        }
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
        if (this.update_time != null) {
            parcel.writeByte((byte) 1);
            parcel.writeLong(this.update_time.longValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.temperature_high != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.temperature_high.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.temperature_low != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.temperature_low.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.weather_text != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.weather_text);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.wind_speed != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.wind_speed.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.wind_direction != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.wind_direction);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.p_num != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.p_num.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
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
        if (this.pm10 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.pm10.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.pm2_5 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.pm2_5.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.no2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.no2.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.so2 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.so2.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.o3 != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.o3.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.co != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.co.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.humidity != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.humidity);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.air_pressure != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.air_pressure.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.alarm_type != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.alarm_type);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.alarm_level != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.alarm_level);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReservedInt != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.mReservedInt.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.mReservedText != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.mReservedText);
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<MetaWeatherInfo> getHelper() {
        return MetaWeatherInfoHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MetaWeatherInfo { id: " + this.id + ", city_code: " + this.city_code + ", time_zone: " + this.time_zone + ", update_time: " + this.update_time + ", temperature_high: " + this.temperature_high + ", temperature_low: " + this.temperature_low + ", weather_text: " + this.weather_text + ", wind_speed: " + this.wind_speed + ", wind_direction: " + this.wind_direction + ", p_num: " + this.p_num + ", p_status_cn: " + this.p_status_cn + ", p_status_en: " + this.p_status_en + ", pm10: " + this.pm10 + ", pm2_5: " + this.pm2_5 + ", no2: " + this.no2 + ", so2: " + this.so2 + ", o3: " + this.o3 + ", co: " + this.co + ", humidity: " + this.humidity + ", air_pressure: " + this.air_pressure + ", alarm_type: " + this.alarm_type + ", alarm_level: " + this.alarm_level + ", mReservedInt: " + this.mReservedInt + ", mReservedText: " + this.mReservedText + " }";
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
