package com.huawei.nb.model.profile;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class MyProfile extends AManagedObject {
    public static final Parcelable.Creator<MyProfile> CREATOR = new Parcelable.Creator<MyProfile>() {
        /* class com.huawei.nb.model.profile.MyProfile.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MyProfile createFromParcel(Parcel parcel) {
            return new MyProfile(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public MyProfile[] newArray(int i) {
            return new MyProfile[i];
        }
    };
    private Integer accomCellId;
    private Integer accomCellLac;
    private Float accomConfidence;
    private String accomGPS;
    private Double age;
    private Float ageConfidence;
    private String arriveHomeTimeWeekend;
    private Float arriveHomeTimeWeekendConfidence;
    private String arriveHomeTimeWorkday;
    private Float arriveHomeTimeWorkdayConfidence;
    private String arriveWorkplaceTimeWorkday;
    private Float arriveWorkplaceTimeWorkdayConfidence;
    private String baseCity;
    private Float baseCityConfidence;
    private Float buziTripPrefer;
    private Float buziTripPreferConfidence;
    private Integer contactNumber;
    private Float contactNumberConfidence;
    private String deviceID;
    private String hwId;
    private Float hwIdConfidence;
    private Integer id;
    private Float imeiConfidence;
    private String leaveHomeTimeWeekend;
    private Float leaveHomeTimeWeekendConfidence;
    private String leaveHomeTimeWorkday;
    private Float leaveHomeTimeWorkdayConfidence;
    private String leaveWorkplaceTimeWorkday;
    private Float leaveWorkplaceTimeWorkdayConfidence;
    private String offTimeWeekend;
    private Float offTimeWeekendConfidence;
    private String offTimeWorkday;
    private Float offTimeWorkdayConfidence;
    private String onTimeWeekend;
    private Float onTimeWeekendConfidence;
    private String onTimeWorkday;
    private Float onTimeWorkdayConfidence;
    private Float photoPrefer;
    private Float photoPreferConfidence;
    private Float photoPreferTurism;
    private Float photoPreferTurismConfidence;
    private Integer poi1CellId;
    private Integer poi1CellLac;
    private Float poi1Confidence;
    private String poi1GPS;
    private Integer poi2CellId;
    private Integer poi2CellLac;
    private Float poi2Confidence;
    private String poi2GPS;
    private Integer poi3CellId;
    private Integer poi3CellLac;
    private Float poi3Confidence;
    private String poi3GPS;
    private Integer poi4CellId;
    private Integer poi4CellLac;
    private Float poi4Confidence;
    private String poi4GPS;
    private Integer poi5CellId;
    private Integer poi5CellLac;
    private Float poi5Confidence;
    private String poi5GPS;
    private Double roamingRadius;
    private Float roamingRadiusConfidence;
    private Double sexuality;
    private Float sexualityConfidence;
    private String top10Tag;
    private Float top10TagConfidence;
    private String top1Tag;
    private Float top1TagConfidence;
    private String top2Tag;
    private Float top2TagConfidence;
    private String top3Tag;
    private Float top3TagConfidence;
    private String top4Tag;
    private Float top4TagConfidence;
    private String top5Tag;
    private Float top5TagConfidence;
    private String top6Tag;
    private Float top6TagConfidence;
    private String top7Tag;
    private Float top7TagConfidence;
    private String top8Tag;
    private Float top8TagConfidence;
    private String top9Tag;
    private Float top9TagConfidence;
    private String topMode;
    private Float topModeConfidence;
    private Integer workPlaceCellId;
    private Integer workPlaceCellLac;
    private String workPlaceGPS;
    private Float workplaceConfidence;

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
        return "com.huawei.nb.model.profile.MyProfile";
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }

    public MyProfile(Cursor cursor) {
        setRowId(Long.valueOf(cursor.getLong(0)));
        Float f = null;
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.deviceID = cursor.getString(2);
        this.hwId = cursor.getString(3);
        this.age = cursor.isNull(4) ? null : Double.valueOf(cursor.getDouble(4));
        this.sexuality = cursor.isNull(5) ? null : Double.valueOf(cursor.getDouble(5));
        this.contactNumber = cursor.isNull(6) ? null : Integer.valueOf(cursor.getInt(6));
        this.accomGPS = cursor.getString(7);
        this.accomCellId = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        this.accomCellLac = cursor.isNull(9) ? null : Integer.valueOf(cursor.getInt(9));
        this.workPlaceGPS = cursor.getString(10);
        this.workPlaceCellId = cursor.isNull(11) ? null : Integer.valueOf(cursor.getInt(11));
        this.workPlaceCellLac = cursor.isNull(12) ? null : Integer.valueOf(cursor.getInt(12));
        this.poi1GPS = cursor.getString(13);
        this.poi1CellId = cursor.isNull(14) ? null : Integer.valueOf(cursor.getInt(14));
        this.poi1CellLac = cursor.isNull(15) ? null : Integer.valueOf(cursor.getInt(15));
        this.poi2GPS = cursor.getString(16);
        this.poi2CellId = cursor.isNull(17) ? null : Integer.valueOf(cursor.getInt(17));
        this.poi2CellLac = cursor.isNull(18) ? null : Integer.valueOf(cursor.getInt(18));
        this.poi3GPS = cursor.getString(19);
        this.poi3CellId = cursor.isNull(20) ? null : Integer.valueOf(cursor.getInt(20));
        this.poi3CellLac = cursor.isNull(21) ? null : Integer.valueOf(cursor.getInt(21));
        this.poi4GPS = cursor.getString(22);
        this.poi4CellId = cursor.isNull(23) ? null : Integer.valueOf(cursor.getInt(23));
        this.poi4CellLac = cursor.isNull(24) ? null : Integer.valueOf(cursor.getInt(24));
        this.poi5GPS = cursor.getString(25);
        this.poi5CellId = cursor.isNull(26) ? null : Integer.valueOf(cursor.getInt(26));
        this.poi5CellLac = cursor.isNull(27) ? null : Integer.valueOf(cursor.getInt(27));
        this.baseCity = cursor.getString(28);
        this.roamingRadius = cursor.isNull(29) ? null : Double.valueOf(cursor.getDouble(29));
        this.buziTripPrefer = cursor.isNull(30) ? null : Float.valueOf(cursor.getFloat(30));
        this.photoPrefer = cursor.isNull(31) ? null : Float.valueOf(cursor.getFloat(31));
        this.photoPreferTurism = cursor.isNull(32) ? null : Float.valueOf(cursor.getFloat(32));
        this.topMode = cursor.getString(33);
        this.top1Tag = cursor.getString(34);
        this.top2Tag = cursor.getString(35);
        this.top3Tag = cursor.getString(36);
        this.top4Tag = cursor.getString(37);
        this.top5Tag = cursor.getString(38);
        this.top6Tag = cursor.getString(39);
        this.top7Tag = cursor.getString(40);
        this.top8Tag = cursor.getString(41);
        this.top9Tag = cursor.getString(42);
        this.top10Tag = cursor.getString(43);
        this.onTimeWorkday = cursor.getString(44);
        this.leaveHomeTimeWorkday = cursor.getString(45);
        this.arriveWorkplaceTimeWorkday = cursor.getString(46);
        this.leaveWorkplaceTimeWorkday = cursor.getString(47);
        this.arriveHomeTimeWorkday = cursor.getString(48);
        this.offTimeWorkday = cursor.getString(49);
        this.onTimeWeekend = cursor.getString(50);
        this.leaveHomeTimeWeekend = cursor.getString(51);
        this.arriveHomeTimeWeekend = cursor.getString(52);
        this.offTimeWeekend = cursor.getString(53);
        this.imeiConfidence = cursor.isNull(54) ? null : Float.valueOf(cursor.getFloat(54));
        this.hwIdConfidence = cursor.isNull(55) ? null : Float.valueOf(cursor.getFloat(55));
        this.ageConfidence = cursor.isNull(56) ? null : Float.valueOf(cursor.getFloat(56));
        this.sexualityConfidence = cursor.isNull(57) ? null : Float.valueOf(cursor.getFloat(57));
        this.contactNumberConfidence = cursor.isNull(58) ? null : Float.valueOf(cursor.getFloat(58));
        this.accomConfidence = cursor.isNull(59) ? null : Float.valueOf(cursor.getFloat(59));
        this.workplaceConfidence = cursor.isNull(60) ? null : Float.valueOf(cursor.getFloat(60));
        this.poi1Confidence = cursor.isNull(61) ? null : Float.valueOf(cursor.getFloat(61));
        this.poi2Confidence = cursor.isNull(62) ? null : Float.valueOf(cursor.getFloat(62));
        this.poi3Confidence = cursor.isNull(63) ? null : Float.valueOf(cursor.getFloat(63));
        this.poi4Confidence = cursor.isNull(64) ? null : Float.valueOf(cursor.getFloat(64));
        this.poi5Confidence = cursor.isNull(65) ? null : Float.valueOf(cursor.getFloat(65));
        this.baseCityConfidence = cursor.isNull(66) ? null : Float.valueOf(cursor.getFloat(66));
        this.roamingRadiusConfidence = cursor.isNull(67) ? null : Float.valueOf(cursor.getFloat(67));
        this.buziTripPreferConfidence = cursor.isNull(68) ? null : Float.valueOf(cursor.getFloat(68));
        this.photoPreferConfidence = cursor.isNull(69) ? null : Float.valueOf(cursor.getFloat(69));
        this.photoPreferTurismConfidence = cursor.isNull(70) ? null : Float.valueOf(cursor.getFloat(70));
        this.topModeConfidence = cursor.isNull(71) ? null : Float.valueOf(cursor.getFloat(71));
        this.top1TagConfidence = cursor.isNull(72) ? null : Float.valueOf(cursor.getFloat(72));
        this.top2TagConfidence = cursor.isNull(73) ? null : Float.valueOf(cursor.getFloat(73));
        this.top3TagConfidence = cursor.isNull(74) ? null : Float.valueOf(cursor.getFloat(74));
        this.top4TagConfidence = cursor.isNull(75) ? null : Float.valueOf(cursor.getFloat(75));
        this.top5TagConfidence = cursor.isNull(76) ? null : Float.valueOf(cursor.getFloat(76));
        this.top6TagConfidence = cursor.isNull(77) ? null : Float.valueOf(cursor.getFloat(77));
        this.top7TagConfidence = cursor.isNull(78) ? null : Float.valueOf(cursor.getFloat(78));
        this.top8TagConfidence = cursor.isNull(79) ? null : Float.valueOf(cursor.getFloat(79));
        this.top9TagConfidence = cursor.isNull(80) ? null : Float.valueOf(cursor.getFloat(80));
        this.top10TagConfidence = cursor.isNull(81) ? null : Float.valueOf(cursor.getFloat(81));
        this.onTimeWorkdayConfidence = cursor.isNull(82) ? null : Float.valueOf(cursor.getFloat(82));
        this.leaveHomeTimeWorkdayConfidence = cursor.isNull(83) ? null : Float.valueOf(cursor.getFloat(83));
        this.arriveWorkplaceTimeWorkdayConfidence = cursor.isNull(84) ? null : Float.valueOf(cursor.getFloat(84));
        this.leaveWorkplaceTimeWorkdayConfidence = cursor.isNull(85) ? null : Float.valueOf(cursor.getFloat(85));
        this.arriveHomeTimeWorkdayConfidence = cursor.isNull(86) ? null : Float.valueOf(cursor.getFloat(86));
        this.offTimeWorkdayConfidence = cursor.isNull(87) ? null : Float.valueOf(cursor.getFloat(87));
        this.onTimeWeekendConfidence = cursor.isNull(88) ? null : Float.valueOf(cursor.getFloat(88));
        this.leaveHomeTimeWeekendConfidence = cursor.isNull(89) ? null : Float.valueOf(cursor.getFloat(89));
        this.arriveHomeTimeWeekendConfidence = cursor.isNull(90) ? null : Float.valueOf(cursor.getFloat(90));
        this.offTimeWeekendConfidence = !cursor.isNull(91) ? Float.valueOf(cursor.getFloat(91)) : f;
    }

    public MyProfile(Parcel parcel) {
        super(parcel);
        Float f = null;
        if (parcel.readByte() == 0) {
            this.id = null;
            parcel.readInt();
        } else {
            this.id = Integer.valueOf(parcel.readInt());
        }
        this.deviceID = parcel.readByte() == 0 ? null : parcel.readString();
        this.hwId = parcel.readByte() == 0 ? null : parcel.readString();
        this.age = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.sexuality = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.contactNumber = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.accomGPS = parcel.readByte() == 0 ? null : parcel.readString();
        this.accomCellId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.accomCellLac = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.workPlaceGPS = parcel.readByte() == 0 ? null : parcel.readString();
        this.workPlaceCellId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.workPlaceCellLac = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.poi1GPS = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi1CellId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.poi1CellLac = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.poi2GPS = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi2CellId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.poi2CellLac = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.poi3GPS = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi3CellId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.poi3CellLac = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.poi4GPS = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi4CellId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.poi4CellLac = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.poi5GPS = parcel.readByte() == 0 ? null : parcel.readString();
        this.poi5CellId = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.poi5CellLac = parcel.readByte() == 0 ? null : Integer.valueOf(parcel.readInt());
        this.baseCity = parcel.readByte() == 0 ? null : parcel.readString();
        this.roamingRadius = parcel.readByte() == 0 ? null : Double.valueOf(parcel.readDouble());
        this.buziTripPrefer = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.photoPrefer = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.photoPreferTurism = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.topMode = parcel.readByte() == 0 ? null : parcel.readString();
        this.top1Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top2Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top3Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top4Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top5Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top6Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top7Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top8Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top9Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.top10Tag = parcel.readByte() == 0 ? null : parcel.readString();
        this.onTimeWorkday = parcel.readByte() == 0 ? null : parcel.readString();
        this.leaveHomeTimeWorkday = parcel.readByte() == 0 ? null : parcel.readString();
        this.arriveWorkplaceTimeWorkday = parcel.readByte() == 0 ? null : parcel.readString();
        this.leaveWorkplaceTimeWorkday = parcel.readByte() == 0 ? null : parcel.readString();
        this.arriveHomeTimeWorkday = parcel.readByte() == 0 ? null : parcel.readString();
        this.offTimeWorkday = parcel.readByte() == 0 ? null : parcel.readString();
        this.onTimeWeekend = parcel.readByte() == 0 ? null : parcel.readString();
        this.leaveHomeTimeWeekend = parcel.readByte() == 0 ? null : parcel.readString();
        this.arriveHomeTimeWeekend = parcel.readByte() == 0 ? null : parcel.readString();
        this.offTimeWeekend = parcel.readByte() == 0 ? null : parcel.readString();
        this.imeiConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.hwIdConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.ageConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.sexualityConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.contactNumberConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.accomConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.workplaceConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.poi1Confidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.poi2Confidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.poi3Confidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.poi4Confidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.poi5Confidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.baseCityConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.roamingRadiusConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.buziTripPreferConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.photoPreferConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.photoPreferTurismConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.topModeConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.top1TagConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.top2TagConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.top3TagConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.top4TagConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.top5TagConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.top6TagConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.top7TagConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.top8TagConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.top9TagConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.top10TagConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.onTimeWorkdayConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.leaveHomeTimeWorkdayConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.arriveWorkplaceTimeWorkdayConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.leaveWorkplaceTimeWorkdayConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.arriveHomeTimeWorkdayConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.offTimeWorkdayConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.onTimeWeekendConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.leaveHomeTimeWeekendConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.arriveHomeTimeWeekendConfidence = parcel.readByte() == 0 ? null : Float.valueOf(parcel.readFloat());
        this.offTimeWeekendConfidence = parcel.readByte() != 0 ? Float.valueOf(parcel.readFloat()) : f;
    }

    private MyProfile(Integer num, String str, String str2, Double d, Double d2, Integer num2, String str3, Integer num3, Integer num4, String str4, Integer num5, Integer num6, String str5, Integer num7, Integer num8, String str6, Integer num9, Integer num10, String str7, Integer num11, Integer num12, String str8, Integer num13, Integer num14, String str9, Integer num15, Integer num16, String str10, Double d3, Float f, Float f2, Float f3, String str11, String str12, String str13, String str14, String str15, String str16, String str17, String str18, String str19, String str20, String str21, String str22, String str23, String str24, String str25, String str26, String str27, String str28, String str29, String str30, String str31, Float f4, Float f5, Float f6, Float f7, Float f8, Float f9, Float f10, Float f11, Float f12, Float f13, Float f14, Float f15, Float f16, Float f17, Float f18, Float f19, Float f20, Float f21, Float f22, Float f23, Float f24, Float f25, Float f26, Float f27, Float f28, Float f29, Float f30, Float f31, Float f32, Float f33, Float f34, Float f35, Float f36, Float f37, Float f38, Float f39, Float f40, Float f41) {
        this.id = num;
        this.deviceID = str;
        this.hwId = str2;
        this.age = d;
        this.sexuality = d2;
        this.contactNumber = num2;
        this.accomGPS = str3;
        this.accomCellId = num3;
        this.accomCellLac = num4;
        this.workPlaceGPS = str4;
        this.workPlaceCellId = num5;
        this.workPlaceCellLac = num6;
        this.poi1GPS = str5;
        this.poi1CellId = num7;
        this.poi1CellLac = num8;
        this.poi2GPS = str6;
        this.poi2CellId = num9;
        this.poi2CellLac = num10;
        this.poi3GPS = str7;
        this.poi3CellId = num11;
        this.poi3CellLac = num12;
        this.poi4GPS = str8;
        this.poi4CellId = num13;
        this.poi4CellLac = num14;
        this.poi5GPS = str9;
        this.poi5CellId = num15;
        this.poi5CellLac = num16;
        this.baseCity = str10;
        this.roamingRadius = d3;
        this.buziTripPrefer = f;
        this.photoPrefer = f2;
        this.photoPreferTurism = f3;
        this.topMode = str11;
        this.top1Tag = str12;
        this.top2Tag = str13;
        this.top3Tag = str14;
        this.top4Tag = str15;
        this.top5Tag = str16;
        this.top6Tag = str17;
        this.top7Tag = str18;
        this.top8Tag = str19;
        this.top9Tag = str20;
        this.top10Tag = str21;
        this.onTimeWorkday = str22;
        this.leaveHomeTimeWorkday = str23;
        this.arriveWorkplaceTimeWorkday = str24;
        this.leaveWorkplaceTimeWorkday = str25;
        this.arriveHomeTimeWorkday = str26;
        this.offTimeWorkday = str27;
        this.onTimeWeekend = str28;
        this.leaveHomeTimeWeekend = str29;
        this.arriveHomeTimeWeekend = str30;
        this.offTimeWeekend = str31;
        this.imeiConfidence = f4;
        this.hwIdConfidence = f5;
        this.ageConfidence = f6;
        this.sexualityConfidence = f7;
        this.contactNumberConfidence = f8;
        this.accomConfidence = f9;
        this.workplaceConfidence = f10;
        this.poi1Confidence = f11;
        this.poi2Confidence = f12;
        this.poi3Confidence = f13;
        this.poi4Confidence = f14;
        this.poi5Confidence = f15;
        this.baseCityConfidence = f16;
        this.roamingRadiusConfidence = f17;
        this.buziTripPreferConfidence = f18;
        this.photoPreferConfidence = f19;
        this.photoPreferTurismConfidence = f20;
        this.topModeConfidence = f21;
        this.top1TagConfidence = f22;
        this.top2TagConfidence = f23;
        this.top3TagConfidence = f24;
        this.top4TagConfidence = f25;
        this.top5TagConfidence = f26;
        this.top6TagConfidence = f27;
        this.top7TagConfidence = f28;
        this.top8TagConfidence = f29;
        this.top9TagConfidence = f30;
        this.top10TagConfidence = f31;
        this.onTimeWorkdayConfidence = f32;
        this.leaveHomeTimeWorkdayConfidence = f33;
        this.arriveWorkplaceTimeWorkdayConfidence = f34;
        this.leaveWorkplaceTimeWorkdayConfidence = f35;
        this.arriveHomeTimeWorkdayConfidence = f36;
        this.offTimeWorkdayConfidence = f37;
        this.onTimeWeekendConfidence = f38;
        this.leaveHomeTimeWeekendConfidence = f39;
        this.arriveHomeTimeWeekendConfidence = f40;
        this.offTimeWeekendConfidence = f41;
    }

    public MyProfile() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer num) {
        this.id = num;
        setValue();
    }

    public String getDeviceID() {
        return this.deviceID;
    }

    public void setDeviceID(String str) {
        this.deviceID = str;
        setValue();
    }

    public String getHwId() {
        return this.hwId;
    }

    public void setHwId(String str) {
        this.hwId = str;
        setValue();
    }

    public Double getAge() {
        return this.age;
    }

    public void setAge(Double d) {
        this.age = d;
        setValue();
    }

    public Double getSexuality() {
        return this.sexuality;
    }

    public void setSexuality(Double d) {
        this.sexuality = d;
        setValue();
    }

    public Integer getContactNumber() {
        return this.contactNumber;
    }

    public void setContactNumber(Integer num) {
        this.contactNumber = num;
        setValue();
    }

    public String getAccomGPS() {
        return this.accomGPS;
    }

    public void setAccomGPS(String str) {
        this.accomGPS = str;
        setValue();
    }

    public Integer getAccomCellId() {
        return this.accomCellId;
    }

    public void setAccomCellId(Integer num) {
        this.accomCellId = num;
        setValue();
    }

    public Integer getAccomCellLac() {
        return this.accomCellLac;
    }

    public void setAccomCellLac(Integer num) {
        this.accomCellLac = num;
        setValue();
    }

    public String getWorkPlaceGPS() {
        return this.workPlaceGPS;
    }

    public void setWorkPlaceGPS(String str) {
        this.workPlaceGPS = str;
        setValue();
    }

    public Integer getWorkPlaceCellId() {
        return this.workPlaceCellId;
    }

    public void setWorkPlaceCellId(Integer num) {
        this.workPlaceCellId = num;
        setValue();
    }

    public Integer getWorkPlaceCellLac() {
        return this.workPlaceCellLac;
    }

    public void setWorkPlaceCellLac(Integer num) {
        this.workPlaceCellLac = num;
        setValue();
    }

    public String getPoi1GPS() {
        return this.poi1GPS;
    }

    public void setPoi1GPS(String str) {
        this.poi1GPS = str;
        setValue();
    }

    public Integer getPoi1CellId() {
        return this.poi1CellId;
    }

    public void setPoi1CellId(Integer num) {
        this.poi1CellId = num;
        setValue();
    }

    public Integer getPoi1CellLac() {
        return this.poi1CellLac;
    }

    public void setPoi1CellLac(Integer num) {
        this.poi1CellLac = num;
        setValue();
    }

    public String getPoi2GPS() {
        return this.poi2GPS;
    }

    public void setPoi2GPS(String str) {
        this.poi2GPS = str;
        setValue();
    }

    public Integer getPoi2CellId() {
        return this.poi2CellId;
    }

    public void setPoi2CellId(Integer num) {
        this.poi2CellId = num;
        setValue();
    }

    public Integer getPoi2CellLac() {
        return this.poi2CellLac;
    }

    public void setPoi2CellLac(Integer num) {
        this.poi2CellLac = num;
        setValue();
    }

    public String getPoi3GPS() {
        return this.poi3GPS;
    }

    public void setPoi3GPS(String str) {
        this.poi3GPS = str;
        setValue();
    }

    public Integer getPoi3CellId() {
        return this.poi3CellId;
    }

    public void setPoi3CellId(Integer num) {
        this.poi3CellId = num;
        setValue();
    }

    public Integer getPoi3CellLac() {
        return this.poi3CellLac;
    }

    public void setPoi3CellLac(Integer num) {
        this.poi3CellLac = num;
        setValue();
    }

    public String getPoi4GPS() {
        return this.poi4GPS;
    }

    public void setPoi4GPS(String str) {
        this.poi4GPS = str;
        setValue();
    }

    public Integer getPoi4CellId() {
        return this.poi4CellId;
    }

    public void setPoi4CellId(Integer num) {
        this.poi4CellId = num;
        setValue();
    }

    public Integer getPoi4CellLac() {
        return this.poi4CellLac;
    }

    public void setPoi4CellLac(Integer num) {
        this.poi4CellLac = num;
        setValue();
    }

    public String getPoi5GPS() {
        return this.poi5GPS;
    }

    public void setPoi5GPS(String str) {
        this.poi5GPS = str;
        setValue();
    }

    public Integer getPoi5CellId() {
        return this.poi5CellId;
    }

    public void setPoi5CellId(Integer num) {
        this.poi5CellId = num;
        setValue();
    }

    public Integer getPoi5CellLac() {
        return this.poi5CellLac;
    }

    public void setPoi5CellLac(Integer num) {
        this.poi5CellLac = num;
        setValue();
    }

    public String getBaseCity() {
        return this.baseCity;
    }

    public void setBaseCity(String str) {
        this.baseCity = str;
        setValue();
    }

    public Double getRoamingRadius() {
        return this.roamingRadius;
    }

    public void setRoamingRadius(Double d) {
        this.roamingRadius = d;
        setValue();
    }

    public Float getBuziTripPrefer() {
        return this.buziTripPrefer;
    }

    public void setBuziTripPrefer(Float f) {
        this.buziTripPrefer = f;
        setValue();
    }

    public Float getPhotoPrefer() {
        return this.photoPrefer;
    }

    public void setPhotoPrefer(Float f) {
        this.photoPrefer = f;
        setValue();
    }

    public Float getPhotoPreferTurism() {
        return this.photoPreferTurism;
    }

    public void setPhotoPreferTurism(Float f) {
        this.photoPreferTurism = f;
        setValue();
    }

    public String getTopMode() {
        return this.topMode;
    }

    public void setTopMode(String str) {
        this.topMode = str;
        setValue();
    }

    public String getTop1Tag() {
        return this.top1Tag;
    }

    public void setTop1Tag(String str) {
        this.top1Tag = str;
        setValue();
    }

    public String getTop2Tag() {
        return this.top2Tag;
    }

    public void setTop2Tag(String str) {
        this.top2Tag = str;
        setValue();
    }

    public String getTop3Tag() {
        return this.top3Tag;
    }

    public void setTop3Tag(String str) {
        this.top3Tag = str;
        setValue();
    }

    public String getTop4Tag() {
        return this.top4Tag;
    }

    public void setTop4Tag(String str) {
        this.top4Tag = str;
        setValue();
    }

    public String getTop5Tag() {
        return this.top5Tag;
    }

    public void setTop5Tag(String str) {
        this.top5Tag = str;
        setValue();
    }

    public String getTop6Tag() {
        return this.top6Tag;
    }

    public void setTop6Tag(String str) {
        this.top6Tag = str;
        setValue();
    }

    public String getTop7Tag() {
        return this.top7Tag;
    }

    public void setTop7Tag(String str) {
        this.top7Tag = str;
        setValue();
    }

    public String getTop8Tag() {
        return this.top8Tag;
    }

    public void setTop8Tag(String str) {
        this.top8Tag = str;
        setValue();
    }

    public String getTop9Tag() {
        return this.top9Tag;
    }

    public void setTop9Tag(String str) {
        this.top9Tag = str;
        setValue();
    }

    public String getTop10Tag() {
        return this.top10Tag;
    }

    public void setTop10Tag(String str) {
        this.top10Tag = str;
        setValue();
    }

    public String getOnTimeWorkday() {
        return this.onTimeWorkday;
    }

    public void setOnTimeWorkday(String str) {
        this.onTimeWorkday = str;
        setValue();
    }

    public String getLeaveHomeTimeWorkday() {
        return this.leaveHomeTimeWorkday;
    }

    public void setLeaveHomeTimeWorkday(String str) {
        this.leaveHomeTimeWorkday = str;
        setValue();
    }

    public String getArriveWorkplaceTimeWorkday() {
        return this.arriveWorkplaceTimeWorkday;
    }

    public void setArriveWorkplaceTimeWorkday(String str) {
        this.arriveWorkplaceTimeWorkday = str;
        setValue();
    }

    public String getLeaveWorkplaceTimeWorkday() {
        return this.leaveWorkplaceTimeWorkday;
    }

    public void setLeaveWorkplaceTimeWorkday(String str) {
        this.leaveWorkplaceTimeWorkday = str;
        setValue();
    }

    public String getArriveHomeTimeWorkday() {
        return this.arriveHomeTimeWorkday;
    }

    public void setArriveHomeTimeWorkday(String str) {
        this.arriveHomeTimeWorkday = str;
        setValue();
    }

    public String getOffTimeWorkday() {
        return this.offTimeWorkday;
    }

    public void setOffTimeWorkday(String str) {
        this.offTimeWorkday = str;
        setValue();
    }

    public String getOnTimeWeekend() {
        return this.onTimeWeekend;
    }

    public void setOnTimeWeekend(String str) {
        this.onTimeWeekend = str;
        setValue();
    }

    public String getLeaveHomeTimeWeekend() {
        return this.leaveHomeTimeWeekend;
    }

    public void setLeaveHomeTimeWeekend(String str) {
        this.leaveHomeTimeWeekend = str;
        setValue();
    }

    public String getArriveHomeTimeWeekend() {
        return this.arriveHomeTimeWeekend;
    }

    public void setArriveHomeTimeWeekend(String str) {
        this.arriveHomeTimeWeekend = str;
        setValue();
    }

    public String getOffTimeWeekend() {
        return this.offTimeWeekend;
    }

    public void setOffTimeWeekend(String str) {
        this.offTimeWeekend = str;
        setValue();
    }

    public Float getImeiConfidence() {
        return this.imeiConfidence;
    }

    public void setImeiConfidence(Float f) {
        this.imeiConfidence = f;
        setValue();
    }

    public Float getHwIdConfidence() {
        return this.hwIdConfidence;
    }

    public void setHwIdConfidence(Float f) {
        this.hwIdConfidence = f;
        setValue();
    }

    public Float getAgeConfidence() {
        return this.ageConfidence;
    }

    public void setAgeConfidence(Float f) {
        this.ageConfidence = f;
        setValue();
    }

    public Float getSexualityConfidence() {
        return this.sexualityConfidence;
    }

    public void setSexualityConfidence(Float f) {
        this.sexualityConfidence = f;
        setValue();
    }

    public Float getContactNumberConfidence() {
        return this.contactNumberConfidence;
    }

    public void setContactNumberConfidence(Float f) {
        this.contactNumberConfidence = f;
        setValue();
    }

    public Float getAccomConfidence() {
        return this.accomConfidence;
    }

    public void setAccomConfidence(Float f) {
        this.accomConfidence = f;
        setValue();
    }

    public Float getWorkplaceConfidence() {
        return this.workplaceConfidence;
    }

    public void setWorkplaceConfidence(Float f) {
        this.workplaceConfidence = f;
        setValue();
    }

    public Float getPoi1Confidence() {
        return this.poi1Confidence;
    }

    public void setPoi1Confidence(Float f) {
        this.poi1Confidence = f;
        setValue();
    }

    public Float getPoi2Confidence() {
        return this.poi2Confidence;
    }

    public void setPoi2Confidence(Float f) {
        this.poi2Confidence = f;
        setValue();
    }

    public Float getPoi3Confidence() {
        return this.poi3Confidence;
    }

    public void setPoi3Confidence(Float f) {
        this.poi3Confidence = f;
        setValue();
    }

    public Float getPoi4Confidence() {
        return this.poi4Confidence;
    }

    public void setPoi4Confidence(Float f) {
        this.poi4Confidence = f;
        setValue();
    }

    public Float getPoi5Confidence() {
        return this.poi5Confidence;
    }

    public void setPoi5Confidence(Float f) {
        this.poi5Confidence = f;
        setValue();
    }

    public Float getBaseCityConfidence() {
        return this.baseCityConfidence;
    }

    public void setBaseCityConfidence(Float f) {
        this.baseCityConfidence = f;
        setValue();
    }

    public Float getRoamingRadiusConfidence() {
        return this.roamingRadiusConfidence;
    }

    public void setRoamingRadiusConfidence(Float f) {
        this.roamingRadiusConfidence = f;
        setValue();
    }

    public Float getBuziTripPreferConfidence() {
        return this.buziTripPreferConfidence;
    }

    public void setBuziTripPreferConfidence(Float f) {
        this.buziTripPreferConfidence = f;
        setValue();
    }

    public Float getPhotoPreferConfidence() {
        return this.photoPreferConfidence;
    }

    public void setPhotoPreferConfidence(Float f) {
        this.photoPreferConfidence = f;
        setValue();
    }

    public Float getPhotoPreferTurismConfidence() {
        return this.photoPreferTurismConfidence;
    }

    public void setPhotoPreferTurismConfidence(Float f) {
        this.photoPreferTurismConfidence = f;
        setValue();
    }

    public Float getTopModeConfidence() {
        return this.topModeConfidence;
    }

    public void setTopModeConfidence(Float f) {
        this.topModeConfidence = f;
        setValue();
    }

    public Float getTop1TagConfidence() {
        return this.top1TagConfidence;
    }

    public void setTop1TagConfidence(Float f) {
        this.top1TagConfidence = f;
        setValue();
    }

    public Float getTop2TagConfidence() {
        return this.top2TagConfidence;
    }

    public void setTop2TagConfidence(Float f) {
        this.top2TagConfidence = f;
        setValue();
    }

    public Float getTop3TagConfidence() {
        return this.top3TagConfidence;
    }

    public void setTop3TagConfidence(Float f) {
        this.top3TagConfidence = f;
        setValue();
    }

    public Float getTop4TagConfidence() {
        return this.top4TagConfidence;
    }

    public void setTop4TagConfidence(Float f) {
        this.top4TagConfidence = f;
        setValue();
    }

    public Float getTop5TagConfidence() {
        return this.top5TagConfidence;
    }

    public void setTop5TagConfidence(Float f) {
        this.top5TagConfidence = f;
        setValue();
    }

    public Float getTop6TagConfidence() {
        return this.top6TagConfidence;
    }

    public void setTop6TagConfidence(Float f) {
        this.top6TagConfidence = f;
        setValue();
    }

    public Float getTop7TagConfidence() {
        return this.top7TagConfidence;
    }

    public void setTop7TagConfidence(Float f) {
        this.top7TagConfidence = f;
        setValue();
    }

    public Float getTop8TagConfidence() {
        return this.top8TagConfidence;
    }

    public void setTop8TagConfidence(Float f) {
        this.top8TagConfidence = f;
        setValue();
    }

    public Float getTop9TagConfidence() {
        return this.top9TagConfidence;
    }

    public void setTop9TagConfidence(Float f) {
        this.top9TagConfidence = f;
        setValue();
    }

    public Float getTop10TagConfidence() {
        return this.top10TagConfidence;
    }

    public void setTop10TagConfidence(Float f) {
        this.top10TagConfidence = f;
        setValue();
    }

    public Float getOnTimeWorkdayConfidence() {
        return this.onTimeWorkdayConfidence;
    }

    public void setOnTimeWorkdayConfidence(Float f) {
        this.onTimeWorkdayConfidence = f;
        setValue();
    }

    public Float getLeaveHomeTimeWorkdayConfidence() {
        return this.leaveHomeTimeWorkdayConfidence;
    }

    public void setLeaveHomeTimeWorkdayConfidence(Float f) {
        this.leaveHomeTimeWorkdayConfidence = f;
        setValue();
    }

    public Float getArriveWorkplaceTimeWorkdayConfidence() {
        return this.arriveWorkplaceTimeWorkdayConfidence;
    }

    public void setArriveWorkplaceTimeWorkdayConfidence(Float f) {
        this.arriveWorkplaceTimeWorkdayConfidence = f;
        setValue();
    }

    public Float getLeaveWorkplaceTimeWorkdayConfidence() {
        return this.leaveWorkplaceTimeWorkdayConfidence;
    }

    public void setLeaveWorkplaceTimeWorkdayConfidence(Float f) {
        this.leaveWorkplaceTimeWorkdayConfidence = f;
        setValue();
    }

    public Float getArriveHomeTimeWorkdayConfidence() {
        return this.arriveHomeTimeWorkdayConfidence;
    }

    public void setArriveHomeTimeWorkdayConfidence(Float f) {
        this.arriveHomeTimeWorkdayConfidence = f;
        setValue();
    }

    public Float getOffTimeWorkdayConfidence() {
        return this.offTimeWorkdayConfidence;
    }

    public void setOffTimeWorkdayConfidence(Float f) {
        this.offTimeWorkdayConfidence = f;
        setValue();
    }

    public Float getOnTimeWeekendConfidence() {
        return this.onTimeWeekendConfidence;
    }

    public void setOnTimeWeekendConfidence(Float f) {
        this.onTimeWeekendConfidence = f;
        setValue();
    }

    public Float getLeaveHomeTimeWeekendConfidence() {
        return this.leaveHomeTimeWeekendConfidence;
    }

    public void setLeaveHomeTimeWeekendConfidence(Float f) {
        this.leaveHomeTimeWeekendConfidence = f;
        setValue();
    }

    public Float getArriveHomeTimeWeekendConfidence() {
        return this.arriveHomeTimeWeekendConfidence;
    }

    public void setArriveHomeTimeWeekendConfidence(Float f) {
        this.arriveHomeTimeWeekendConfidence = f;
        setValue();
    }

    public Float getOffTimeWeekendConfidence() {
        return this.offTimeWeekendConfidence;
    }

    public void setOffTimeWeekendConfidence(Float f) {
        this.offTimeWeekendConfidence = f;
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
        if (this.deviceID != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.deviceID);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.hwId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.hwId);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.age != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.age.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.sexuality != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.sexuality.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.contactNumber != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.contactNumber.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.accomGPS != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.accomGPS);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.accomCellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.accomCellId.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.accomCellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.accomCellLac.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.workPlaceGPS != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.workPlaceGPS);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.workPlaceCellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.workPlaceCellId.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.workPlaceCellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.workPlaceCellLac.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi1GPS != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi1GPS);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi1CellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.poi1CellId.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi1CellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.poi1CellLac.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi2GPS != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi2GPS);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi2CellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.poi2CellId.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi2CellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.poi2CellLac.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi3GPS != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi3GPS);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi3CellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.poi3CellId.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi3CellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.poi3CellLac.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi4GPS != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi4GPS);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi4CellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.poi4CellId.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi4CellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.poi4CellLac.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi5GPS != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.poi5GPS);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi5CellId != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.poi5CellId.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi5CellLac != null) {
            parcel.writeByte((byte) 1);
            parcel.writeInt(this.poi5CellLac.intValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.baseCity != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.baseCity);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.roamingRadius != null) {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(this.roamingRadius.doubleValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.buziTripPrefer != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.buziTripPrefer.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.photoPrefer != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.photoPrefer.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.photoPreferTurism != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.photoPreferTurism.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.topMode != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.topMode);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top1Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top1Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top2Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top2Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top3Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top3Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top4Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top4Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top5Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top5Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top6Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top6Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top7Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top7Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top8Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top8Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top9Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top9Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top10Tag != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.top10Tag);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.onTimeWorkday != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.onTimeWorkday);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.leaveHomeTimeWorkday != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.leaveHomeTimeWorkday);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.arriveWorkplaceTimeWorkday != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.arriveWorkplaceTimeWorkday);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.leaveWorkplaceTimeWorkday != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.leaveWorkplaceTimeWorkday);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.arriveHomeTimeWorkday != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.arriveHomeTimeWorkday);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.offTimeWorkday != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.offTimeWorkday);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.onTimeWeekend != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.onTimeWeekend);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.leaveHomeTimeWeekend != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.leaveHomeTimeWeekend);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.arriveHomeTimeWeekend != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.arriveHomeTimeWeekend);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.offTimeWeekend != null) {
            parcel.writeByte((byte) 1);
            parcel.writeString(this.offTimeWeekend);
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.imeiConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.imeiConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.hwIdConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.hwIdConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.ageConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.ageConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.sexualityConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.sexualityConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.contactNumberConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.contactNumberConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.accomConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.accomConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.workplaceConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.workplaceConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi1Confidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.poi1Confidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi2Confidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.poi2Confidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi3Confidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.poi3Confidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi4Confidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.poi4Confidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.poi5Confidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.poi5Confidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.baseCityConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.baseCityConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.roamingRadiusConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.roamingRadiusConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.buziTripPreferConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.buziTripPreferConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.photoPreferConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.photoPreferConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.photoPreferTurismConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.photoPreferTurismConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.topModeConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.topModeConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top1TagConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.top1TagConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top2TagConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.top2TagConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top3TagConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.top3TagConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top4TagConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.top4TagConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top5TagConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.top5TagConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top6TagConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.top6TagConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top7TagConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.top7TagConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top8TagConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.top8TagConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top9TagConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.top9TagConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.top10TagConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.top10TagConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.onTimeWorkdayConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.onTimeWorkdayConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.leaveHomeTimeWorkdayConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.leaveHomeTimeWorkdayConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.arriveWorkplaceTimeWorkdayConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.arriveWorkplaceTimeWorkdayConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.leaveWorkplaceTimeWorkdayConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.leaveWorkplaceTimeWorkdayConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.arriveHomeTimeWorkdayConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.arriveHomeTimeWorkdayConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.offTimeWorkdayConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.offTimeWorkdayConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.onTimeWeekendConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.onTimeWeekendConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.leaveHomeTimeWeekendConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.leaveHomeTimeWeekendConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.arriveHomeTimeWeekendConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.arriveHomeTimeWeekendConfidence.floatValue());
        } else {
            parcel.writeByte((byte) 0);
        }
        if (this.offTimeWeekendConfidence != null) {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(this.offTimeWeekendConfidence.floatValue());
            return;
        }
        parcel.writeByte((byte) 0);
    }

    @Override // com.huawei.odmf.core.AManagedObject
    public AEntityHelper<MyProfile> getHelper() {
        return MyProfileHelper.getInstance();
    }

    @Override // java.lang.Object
    public String toString() {
        return "MyProfile { id: " + this.id + ", deviceID: " + this.deviceID + ", hwId: " + this.hwId + ", age: " + this.age + ", sexuality: " + this.sexuality + ", contactNumber: " + this.contactNumber + ", accomGPS: " + this.accomGPS + ", accomCellId: " + this.accomCellId + ", accomCellLac: " + this.accomCellLac + ", workPlaceGPS: " + this.workPlaceGPS + ", workPlaceCellId: " + this.workPlaceCellId + ", workPlaceCellLac: " + this.workPlaceCellLac + ", poi1GPS: " + this.poi1GPS + ", poi1CellId: " + this.poi1CellId + ", poi1CellLac: " + this.poi1CellLac + ", poi2GPS: " + this.poi2GPS + ", poi2CellId: " + this.poi2CellId + ", poi2CellLac: " + this.poi2CellLac + ", poi3GPS: " + this.poi3GPS + ", poi3CellId: " + this.poi3CellId + ", poi3CellLac: " + this.poi3CellLac + ", poi4GPS: " + this.poi4GPS + ", poi4CellId: " + this.poi4CellId + ", poi4CellLac: " + this.poi4CellLac + ", poi5GPS: " + this.poi5GPS + ", poi5CellId: " + this.poi5CellId + ", poi5CellLac: " + this.poi5CellLac + ", baseCity: " + this.baseCity + ", roamingRadius: " + this.roamingRadius + ", buziTripPrefer: " + this.buziTripPrefer + ", photoPrefer: " + this.photoPrefer + ", photoPreferTurism: " + this.photoPreferTurism + ", topMode: " + this.topMode + ", top1Tag: " + this.top1Tag + ", top2Tag: " + this.top2Tag + ", top3Tag: " + this.top3Tag + ", top4Tag: " + this.top4Tag + ", top5Tag: " + this.top5Tag + ", top6Tag: " + this.top6Tag + ", top7Tag: " + this.top7Tag + ", top8Tag: " + this.top8Tag + ", top9Tag: " + this.top9Tag + ", top10Tag: " + this.top10Tag + ", onTimeWorkday: " + this.onTimeWorkday + ", leaveHomeTimeWorkday: " + this.leaveHomeTimeWorkday + ", arriveWorkplaceTimeWorkday: " + this.arriveWorkplaceTimeWorkday + ", leaveWorkplaceTimeWorkday: " + this.leaveWorkplaceTimeWorkday + ", arriveHomeTimeWorkday: " + this.arriveHomeTimeWorkday + ", offTimeWorkday: " + this.offTimeWorkday + ", onTimeWeekend: " + this.onTimeWeekend + ", leaveHomeTimeWeekend: " + this.leaveHomeTimeWeekend + ", arriveHomeTimeWeekend: " + this.arriveHomeTimeWeekend + ", offTimeWeekend: " + this.offTimeWeekend + ", imeiConfidence: " + this.imeiConfidence + ", hwIdConfidence: " + this.hwIdConfidence + ", ageConfidence: " + this.ageConfidence + ", sexualityConfidence: " + this.sexualityConfidence + ", contactNumberConfidence: " + this.contactNumberConfidence + ", accomConfidence: " + this.accomConfidence + ", workplaceConfidence: " + this.workplaceConfidence + ", poi1Confidence: " + this.poi1Confidence + ", poi2Confidence: " + this.poi2Confidence + ", poi3Confidence: " + this.poi3Confidence + ", poi4Confidence: " + this.poi4Confidence + ", poi5Confidence: " + this.poi5Confidence + ", baseCityConfidence: " + this.baseCityConfidence + ", roamingRadiusConfidence: " + this.roamingRadiusConfidence + ", buziTripPreferConfidence: " + this.buziTripPreferConfidence + ", photoPreferConfidence: " + this.photoPreferConfidence + ", photoPreferTurismConfidence: " + this.photoPreferTurismConfidence + ", topModeConfidence: " + this.topModeConfidence + ", top1TagConfidence: " + this.top1TagConfidence + ", top2TagConfidence: " + this.top2TagConfidence + ", top3TagConfidence: " + this.top3TagConfidence + ", top4TagConfidence: " + this.top4TagConfidence + ", top5TagConfidence: " + this.top5TagConfidence + ", top6TagConfidence: " + this.top6TagConfidence + ", top7TagConfidence: " + this.top7TagConfidence + ", top8TagConfidence: " + this.top8TagConfidence + ", top9TagConfidence: " + this.top9TagConfidence + ", top10TagConfidence: " + this.top10TagConfidence + ", onTimeWorkdayConfidence: " + this.onTimeWorkdayConfidence + ", leaveHomeTimeWorkdayConfidence: " + this.leaveHomeTimeWorkdayConfidence + ", arriveWorkplaceTimeWorkdayConfidence: " + this.arriveWorkplaceTimeWorkdayConfidence + ", leaveWorkplaceTimeWorkdayConfidence: " + this.leaveWorkplaceTimeWorkdayConfidence + ", arriveHomeTimeWorkdayConfidence: " + this.arriveHomeTimeWorkdayConfidence + ", offTimeWorkdayConfidence: " + this.offTimeWorkdayConfidence + ", onTimeWeekendConfidence: " + this.onTimeWeekendConfidence + ", leaveHomeTimeWeekendConfidence: " + this.leaveHomeTimeWeekendConfidence + ", arriveHomeTimeWeekendConfidence: " + this.arriveHomeTimeWeekendConfidence + ", offTimeWeekendConfidence: " + this.offTimeWeekendConfidence + " }";
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
