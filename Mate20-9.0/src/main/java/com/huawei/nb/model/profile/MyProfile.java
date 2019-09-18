package com.huawei.nb.model.profile;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class MyProfile extends AManagedObject {
    public static final Parcelable.Creator<MyProfile> CREATOR = new Parcelable.Creator<MyProfile>() {
        public MyProfile createFromParcel(Parcel in) {
            return new MyProfile(in);
        }

        public MyProfile[] newArray(int size) {
            return new MyProfile[size];
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

    public MyProfile(Cursor cursor) {
        Float valueOf;
        Float f = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
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
        if (cursor.isNull(32)) {
            valueOf = null;
        } else {
            valueOf = Float.valueOf(cursor.getFloat(32));
        }
        this.photoPreferTurism = valueOf;
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MyProfile(Parcel in) {
        super(in);
        Float f = null;
        if (in.readByte() == 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.deviceID = in.readByte() == 0 ? null : in.readString();
        this.hwId = in.readByte() == 0 ? null : in.readString();
        this.age = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.sexuality = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.contactNumber = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.accomGPS = in.readByte() == 0 ? null : in.readString();
        this.accomCellId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.accomCellLac = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.workPlaceGPS = in.readByte() == 0 ? null : in.readString();
        this.workPlaceCellId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.workPlaceCellLac = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.poi1GPS = in.readByte() == 0 ? null : in.readString();
        this.poi1CellId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.poi1CellLac = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.poi2GPS = in.readByte() == 0 ? null : in.readString();
        this.poi2CellId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.poi2CellLac = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.poi3GPS = in.readByte() == 0 ? null : in.readString();
        this.poi3CellId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.poi3CellLac = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.poi4GPS = in.readByte() == 0 ? null : in.readString();
        this.poi4CellId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.poi4CellLac = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.poi5GPS = in.readByte() == 0 ? null : in.readString();
        this.poi5CellId = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.poi5CellLac = in.readByte() == 0 ? null : Integer.valueOf(in.readInt());
        this.baseCity = in.readByte() == 0 ? null : in.readString();
        this.roamingRadius = in.readByte() == 0 ? null : Double.valueOf(in.readDouble());
        this.buziTripPrefer = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.photoPrefer = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.photoPreferTurism = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.topMode = in.readByte() == 0 ? null : in.readString();
        this.top1Tag = in.readByte() == 0 ? null : in.readString();
        this.top2Tag = in.readByte() == 0 ? null : in.readString();
        this.top3Tag = in.readByte() == 0 ? null : in.readString();
        this.top4Tag = in.readByte() == 0 ? null : in.readString();
        this.top5Tag = in.readByte() == 0 ? null : in.readString();
        this.top6Tag = in.readByte() == 0 ? null : in.readString();
        this.top7Tag = in.readByte() == 0 ? null : in.readString();
        this.top8Tag = in.readByte() == 0 ? null : in.readString();
        this.top9Tag = in.readByte() == 0 ? null : in.readString();
        this.top10Tag = in.readByte() == 0 ? null : in.readString();
        this.onTimeWorkday = in.readByte() == 0 ? null : in.readString();
        this.leaveHomeTimeWorkday = in.readByte() == 0 ? null : in.readString();
        this.arriveWorkplaceTimeWorkday = in.readByte() == 0 ? null : in.readString();
        this.leaveWorkplaceTimeWorkday = in.readByte() == 0 ? null : in.readString();
        this.arriveHomeTimeWorkday = in.readByte() == 0 ? null : in.readString();
        this.offTimeWorkday = in.readByte() == 0 ? null : in.readString();
        this.onTimeWeekend = in.readByte() == 0 ? null : in.readString();
        this.leaveHomeTimeWeekend = in.readByte() == 0 ? null : in.readString();
        this.arriveHomeTimeWeekend = in.readByte() == 0 ? null : in.readString();
        this.offTimeWeekend = in.readByte() == 0 ? null : in.readString();
        this.imeiConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.hwIdConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.ageConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.sexualityConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.contactNumberConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.accomConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.workplaceConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.poi1Confidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.poi2Confidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.poi3Confidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.poi4Confidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.poi5Confidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.baseCityConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.roamingRadiusConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.buziTripPreferConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.photoPreferConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.photoPreferTurismConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.topModeConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.top1TagConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.top2TagConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.top3TagConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.top4TagConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.top5TagConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.top6TagConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.top7TagConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.top8TagConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.top9TagConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.top10TagConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.onTimeWorkdayConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.leaveHomeTimeWorkdayConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.arriveWorkplaceTimeWorkdayConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.leaveWorkplaceTimeWorkdayConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.arriveHomeTimeWorkdayConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.offTimeWorkdayConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.onTimeWeekendConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.leaveHomeTimeWeekendConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.arriveHomeTimeWeekendConfidence = in.readByte() == 0 ? null : Float.valueOf(in.readFloat());
        this.offTimeWeekendConfidence = in.readByte() != 0 ? Float.valueOf(in.readFloat()) : f;
    }

    private MyProfile(Integer id2, String deviceID2, String hwId2, Double age2, Double sexuality2, Integer contactNumber2, String accomGPS2, Integer accomCellId2, Integer accomCellLac2, String workPlaceGPS2, Integer workPlaceCellId2, Integer workPlaceCellLac2, String poi1GPS2, Integer poi1CellId2, Integer poi1CellLac2, String poi2GPS2, Integer poi2CellId2, Integer poi2CellLac2, String poi3GPS2, Integer poi3CellId2, Integer poi3CellLac2, String poi4GPS2, Integer poi4CellId2, Integer poi4CellLac2, String poi5GPS2, Integer poi5CellId2, Integer poi5CellLac2, String baseCity2, Double roamingRadius2, Float buziTripPrefer2, Float photoPrefer2, Float photoPreferTurism2, String topMode2, String top1Tag2, String top2Tag2, String top3Tag2, String top4Tag2, String top5Tag2, String top6Tag2, String top7Tag2, String top8Tag2, String top9Tag2, String top10Tag2, String onTimeWorkday2, String leaveHomeTimeWorkday2, String arriveWorkplaceTimeWorkday2, String leaveWorkplaceTimeWorkday2, String arriveHomeTimeWorkday2, String offTimeWorkday2, String onTimeWeekend2, String leaveHomeTimeWeekend2, String arriveHomeTimeWeekend2, String offTimeWeekend2, Float imeiConfidence2, Float hwIdConfidence2, Float ageConfidence2, Float sexualityConfidence2, Float contactNumberConfidence2, Float accomConfidence2, Float workplaceConfidence2, Float poi1Confidence2, Float poi2Confidence2, Float poi3Confidence2, Float poi4Confidence2, Float poi5Confidence2, Float baseCityConfidence2, Float roamingRadiusConfidence2, Float buziTripPreferConfidence2, Float photoPreferConfidence2, Float photoPreferTurismConfidence2, Float topModeConfidence2, Float top1TagConfidence2, Float top2TagConfidence2, Float top3TagConfidence2, Float top4TagConfidence2, Float top5TagConfidence2, Float top6TagConfidence2, Float top7TagConfidence2, Float top8TagConfidence2, Float top9TagConfidence2, Float top10TagConfidence2, Float onTimeWorkdayConfidence2, Float leaveHomeTimeWorkdayConfidence2, Float arriveWorkplaceTimeWorkdayConfidence2, Float leaveWorkplaceTimeWorkdayConfidence2, Float arriveHomeTimeWorkdayConfidence2, Float offTimeWorkdayConfidence2, Float onTimeWeekendConfidence2, Float leaveHomeTimeWeekendConfidence2, Float arriveHomeTimeWeekendConfidence2, Float offTimeWeekendConfidence2) {
        this.id = id2;
        this.deviceID = deviceID2;
        this.hwId = hwId2;
        this.age = age2;
        this.sexuality = sexuality2;
        this.contactNumber = contactNumber2;
        this.accomGPS = accomGPS2;
        this.accomCellId = accomCellId2;
        this.accomCellLac = accomCellLac2;
        this.workPlaceGPS = workPlaceGPS2;
        this.workPlaceCellId = workPlaceCellId2;
        this.workPlaceCellLac = workPlaceCellLac2;
        this.poi1GPS = poi1GPS2;
        this.poi1CellId = poi1CellId2;
        this.poi1CellLac = poi1CellLac2;
        this.poi2GPS = poi2GPS2;
        this.poi2CellId = poi2CellId2;
        this.poi2CellLac = poi2CellLac2;
        this.poi3GPS = poi3GPS2;
        this.poi3CellId = poi3CellId2;
        this.poi3CellLac = poi3CellLac2;
        this.poi4GPS = poi4GPS2;
        this.poi4CellId = poi4CellId2;
        this.poi4CellLac = poi4CellLac2;
        this.poi5GPS = poi5GPS2;
        this.poi5CellId = poi5CellId2;
        this.poi5CellLac = poi5CellLac2;
        this.baseCity = baseCity2;
        this.roamingRadius = roamingRadius2;
        this.buziTripPrefer = buziTripPrefer2;
        this.photoPrefer = photoPrefer2;
        this.photoPreferTurism = photoPreferTurism2;
        this.topMode = topMode2;
        this.top1Tag = top1Tag2;
        this.top2Tag = top2Tag2;
        this.top3Tag = top3Tag2;
        this.top4Tag = top4Tag2;
        this.top5Tag = top5Tag2;
        this.top6Tag = top6Tag2;
        this.top7Tag = top7Tag2;
        this.top8Tag = top8Tag2;
        this.top9Tag = top9Tag2;
        this.top10Tag = top10Tag2;
        this.onTimeWorkday = onTimeWorkday2;
        this.leaveHomeTimeWorkday = leaveHomeTimeWorkday2;
        this.arriveWorkplaceTimeWorkday = arriveWorkplaceTimeWorkday2;
        this.leaveWorkplaceTimeWorkday = leaveWorkplaceTimeWorkday2;
        this.arriveHomeTimeWorkday = arriveHomeTimeWorkday2;
        this.offTimeWorkday = offTimeWorkday2;
        this.onTimeWeekend = onTimeWeekend2;
        this.leaveHomeTimeWeekend = leaveHomeTimeWeekend2;
        this.arriveHomeTimeWeekend = arriveHomeTimeWeekend2;
        this.offTimeWeekend = offTimeWeekend2;
        this.imeiConfidence = imeiConfidence2;
        this.hwIdConfidence = hwIdConfidence2;
        this.ageConfidence = ageConfidence2;
        this.sexualityConfidence = sexualityConfidence2;
        this.contactNumberConfidence = contactNumberConfidence2;
        this.accomConfidence = accomConfidence2;
        this.workplaceConfidence = workplaceConfidence2;
        this.poi1Confidence = poi1Confidence2;
        this.poi2Confidence = poi2Confidence2;
        this.poi3Confidence = poi3Confidence2;
        this.poi4Confidence = poi4Confidence2;
        this.poi5Confidence = poi5Confidence2;
        this.baseCityConfidence = baseCityConfidence2;
        this.roamingRadiusConfidence = roamingRadiusConfidence2;
        this.buziTripPreferConfidence = buziTripPreferConfidence2;
        this.photoPreferConfidence = photoPreferConfidence2;
        this.photoPreferTurismConfidence = photoPreferTurismConfidence2;
        this.topModeConfidence = topModeConfidence2;
        this.top1TagConfidence = top1TagConfidence2;
        this.top2TagConfidence = top2TagConfidence2;
        this.top3TagConfidence = top3TagConfidence2;
        this.top4TagConfidence = top4TagConfidence2;
        this.top5TagConfidence = top5TagConfidence2;
        this.top6TagConfidence = top6TagConfidence2;
        this.top7TagConfidence = top7TagConfidence2;
        this.top8TagConfidence = top8TagConfidence2;
        this.top9TagConfidence = top9TagConfidence2;
        this.top10TagConfidence = top10TagConfidence2;
        this.onTimeWorkdayConfidence = onTimeWorkdayConfidence2;
        this.leaveHomeTimeWorkdayConfidence = leaveHomeTimeWorkdayConfidence2;
        this.arriveWorkplaceTimeWorkdayConfidence = arriveWorkplaceTimeWorkdayConfidence2;
        this.leaveWorkplaceTimeWorkdayConfidence = leaveWorkplaceTimeWorkdayConfidence2;
        this.arriveHomeTimeWorkdayConfidence = arriveHomeTimeWorkdayConfidence2;
        this.offTimeWorkdayConfidence = offTimeWorkdayConfidence2;
        this.onTimeWeekendConfidence = onTimeWeekendConfidence2;
        this.leaveHomeTimeWeekendConfidence = leaveHomeTimeWeekendConfidence2;
        this.arriveHomeTimeWeekendConfidence = arriveHomeTimeWeekendConfidence2;
        this.offTimeWeekendConfidence = offTimeWeekendConfidence2;
    }

    public MyProfile() {
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id2) {
        this.id = id2;
        setValue();
    }

    public String getDeviceID() {
        return this.deviceID;
    }

    public void setDeviceID(String deviceID2) {
        this.deviceID = deviceID2;
        setValue();
    }

    public String getHwId() {
        return this.hwId;
    }

    public void setHwId(String hwId2) {
        this.hwId = hwId2;
        setValue();
    }

    public Double getAge() {
        return this.age;
    }

    public void setAge(Double age2) {
        this.age = age2;
        setValue();
    }

    public Double getSexuality() {
        return this.sexuality;
    }

    public void setSexuality(Double sexuality2) {
        this.sexuality = sexuality2;
        setValue();
    }

    public Integer getContactNumber() {
        return this.contactNumber;
    }

    public void setContactNumber(Integer contactNumber2) {
        this.contactNumber = contactNumber2;
        setValue();
    }

    public String getAccomGPS() {
        return this.accomGPS;
    }

    public void setAccomGPS(String accomGPS2) {
        this.accomGPS = accomGPS2;
        setValue();
    }

    public Integer getAccomCellId() {
        return this.accomCellId;
    }

    public void setAccomCellId(Integer accomCellId2) {
        this.accomCellId = accomCellId2;
        setValue();
    }

    public Integer getAccomCellLac() {
        return this.accomCellLac;
    }

    public void setAccomCellLac(Integer accomCellLac2) {
        this.accomCellLac = accomCellLac2;
        setValue();
    }

    public String getWorkPlaceGPS() {
        return this.workPlaceGPS;
    }

    public void setWorkPlaceGPS(String workPlaceGPS2) {
        this.workPlaceGPS = workPlaceGPS2;
        setValue();
    }

    public Integer getWorkPlaceCellId() {
        return this.workPlaceCellId;
    }

    public void setWorkPlaceCellId(Integer workPlaceCellId2) {
        this.workPlaceCellId = workPlaceCellId2;
        setValue();
    }

    public Integer getWorkPlaceCellLac() {
        return this.workPlaceCellLac;
    }

    public void setWorkPlaceCellLac(Integer workPlaceCellLac2) {
        this.workPlaceCellLac = workPlaceCellLac2;
        setValue();
    }

    public String getPoi1GPS() {
        return this.poi1GPS;
    }

    public void setPoi1GPS(String poi1GPS2) {
        this.poi1GPS = poi1GPS2;
        setValue();
    }

    public Integer getPoi1CellId() {
        return this.poi1CellId;
    }

    public void setPoi1CellId(Integer poi1CellId2) {
        this.poi1CellId = poi1CellId2;
        setValue();
    }

    public Integer getPoi1CellLac() {
        return this.poi1CellLac;
    }

    public void setPoi1CellLac(Integer poi1CellLac2) {
        this.poi1CellLac = poi1CellLac2;
        setValue();
    }

    public String getPoi2GPS() {
        return this.poi2GPS;
    }

    public void setPoi2GPS(String poi2GPS2) {
        this.poi2GPS = poi2GPS2;
        setValue();
    }

    public Integer getPoi2CellId() {
        return this.poi2CellId;
    }

    public void setPoi2CellId(Integer poi2CellId2) {
        this.poi2CellId = poi2CellId2;
        setValue();
    }

    public Integer getPoi2CellLac() {
        return this.poi2CellLac;
    }

    public void setPoi2CellLac(Integer poi2CellLac2) {
        this.poi2CellLac = poi2CellLac2;
        setValue();
    }

    public String getPoi3GPS() {
        return this.poi3GPS;
    }

    public void setPoi3GPS(String poi3GPS2) {
        this.poi3GPS = poi3GPS2;
        setValue();
    }

    public Integer getPoi3CellId() {
        return this.poi3CellId;
    }

    public void setPoi3CellId(Integer poi3CellId2) {
        this.poi3CellId = poi3CellId2;
        setValue();
    }

    public Integer getPoi3CellLac() {
        return this.poi3CellLac;
    }

    public void setPoi3CellLac(Integer poi3CellLac2) {
        this.poi3CellLac = poi3CellLac2;
        setValue();
    }

    public String getPoi4GPS() {
        return this.poi4GPS;
    }

    public void setPoi4GPS(String poi4GPS2) {
        this.poi4GPS = poi4GPS2;
        setValue();
    }

    public Integer getPoi4CellId() {
        return this.poi4CellId;
    }

    public void setPoi4CellId(Integer poi4CellId2) {
        this.poi4CellId = poi4CellId2;
        setValue();
    }

    public Integer getPoi4CellLac() {
        return this.poi4CellLac;
    }

    public void setPoi4CellLac(Integer poi4CellLac2) {
        this.poi4CellLac = poi4CellLac2;
        setValue();
    }

    public String getPoi5GPS() {
        return this.poi5GPS;
    }

    public void setPoi5GPS(String poi5GPS2) {
        this.poi5GPS = poi5GPS2;
        setValue();
    }

    public Integer getPoi5CellId() {
        return this.poi5CellId;
    }

    public void setPoi5CellId(Integer poi5CellId2) {
        this.poi5CellId = poi5CellId2;
        setValue();
    }

    public Integer getPoi5CellLac() {
        return this.poi5CellLac;
    }

    public void setPoi5CellLac(Integer poi5CellLac2) {
        this.poi5CellLac = poi5CellLac2;
        setValue();
    }

    public String getBaseCity() {
        return this.baseCity;
    }

    public void setBaseCity(String baseCity2) {
        this.baseCity = baseCity2;
        setValue();
    }

    public Double getRoamingRadius() {
        return this.roamingRadius;
    }

    public void setRoamingRadius(Double roamingRadius2) {
        this.roamingRadius = roamingRadius2;
        setValue();
    }

    public Float getBuziTripPrefer() {
        return this.buziTripPrefer;
    }

    public void setBuziTripPrefer(Float buziTripPrefer2) {
        this.buziTripPrefer = buziTripPrefer2;
        setValue();
    }

    public Float getPhotoPrefer() {
        return this.photoPrefer;
    }

    public void setPhotoPrefer(Float photoPrefer2) {
        this.photoPrefer = photoPrefer2;
        setValue();
    }

    public Float getPhotoPreferTurism() {
        return this.photoPreferTurism;
    }

    public void setPhotoPreferTurism(Float photoPreferTurism2) {
        this.photoPreferTurism = photoPreferTurism2;
        setValue();
    }

    public String getTopMode() {
        return this.topMode;
    }

    public void setTopMode(String topMode2) {
        this.topMode = topMode2;
        setValue();
    }

    public String getTop1Tag() {
        return this.top1Tag;
    }

    public void setTop1Tag(String top1Tag2) {
        this.top1Tag = top1Tag2;
        setValue();
    }

    public String getTop2Tag() {
        return this.top2Tag;
    }

    public void setTop2Tag(String top2Tag2) {
        this.top2Tag = top2Tag2;
        setValue();
    }

    public String getTop3Tag() {
        return this.top3Tag;
    }

    public void setTop3Tag(String top3Tag2) {
        this.top3Tag = top3Tag2;
        setValue();
    }

    public String getTop4Tag() {
        return this.top4Tag;
    }

    public void setTop4Tag(String top4Tag2) {
        this.top4Tag = top4Tag2;
        setValue();
    }

    public String getTop5Tag() {
        return this.top5Tag;
    }

    public void setTop5Tag(String top5Tag2) {
        this.top5Tag = top5Tag2;
        setValue();
    }

    public String getTop6Tag() {
        return this.top6Tag;
    }

    public void setTop6Tag(String top6Tag2) {
        this.top6Tag = top6Tag2;
        setValue();
    }

    public String getTop7Tag() {
        return this.top7Tag;
    }

    public void setTop7Tag(String top7Tag2) {
        this.top7Tag = top7Tag2;
        setValue();
    }

    public String getTop8Tag() {
        return this.top8Tag;
    }

    public void setTop8Tag(String top8Tag2) {
        this.top8Tag = top8Tag2;
        setValue();
    }

    public String getTop9Tag() {
        return this.top9Tag;
    }

    public void setTop9Tag(String top9Tag2) {
        this.top9Tag = top9Tag2;
        setValue();
    }

    public String getTop10Tag() {
        return this.top10Tag;
    }

    public void setTop10Tag(String top10Tag2) {
        this.top10Tag = top10Tag2;
        setValue();
    }

    public String getOnTimeWorkday() {
        return this.onTimeWorkday;
    }

    public void setOnTimeWorkday(String onTimeWorkday2) {
        this.onTimeWorkday = onTimeWorkday2;
        setValue();
    }

    public String getLeaveHomeTimeWorkday() {
        return this.leaveHomeTimeWorkday;
    }

    public void setLeaveHomeTimeWorkday(String leaveHomeTimeWorkday2) {
        this.leaveHomeTimeWorkday = leaveHomeTimeWorkday2;
        setValue();
    }

    public String getArriveWorkplaceTimeWorkday() {
        return this.arriveWorkplaceTimeWorkday;
    }

    public void setArriveWorkplaceTimeWorkday(String arriveWorkplaceTimeWorkday2) {
        this.arriveWorkplaceTimeWorkday = arriveWorkplaceTimeWorkday2;
        setValue();
    }

    public String getLeaveWorkplaceTimeWorkday() {
        return this.leaveWorkplaceTimeWorkday;
    }

    public void setLeaveWorkplaceTimeWorkday(String leaveWorkplaceTimeWorkday2) {
        this.leaveWorkplaceTimeWorkday = leaveWorkplaceTimeWorkday2;
        setValue();
    }

    public String getArriveHomeTimeWorkday() {
        return this.arriveHomeTimeWorkday;
    }

    public void setArriveHomeTimeWorkday(String arriveHomeTimeWorkday2) {
        this.arriveHomeTimeWorkday = arriveHomeTimeWorkday2;
        setValue();
    }

    public String getOffTimeWorkday() {
        return this.offTimeWorkday;
    }

    public void setOffTimeWorkday(String offTimeWorkday2) {
        this.offTimeWorkday = offTimeWorkday2;
        setValue();
    }

    public String getOnTimeWeekend() {
        return this.onTimeWeekend;
    }

    public void setOnTimeWeekend(String onTimeWeekend2) {
        this.onTimeWeekend = onTimeWeekend2;
        setValue();
    }

    public String getLeaveHomeTimeWeekend() {
        return this.leaveHomeTimeWeekend;
    }

    public void setLeaveHomeTimeWeekend(String leaveHomeTimeWeekend2) {
        this.leaveHomeTimeWeekend = leaveHomeTimeWeekend2;
        setValue();
    }

    public String getArriveHomeTimeWeekend() {
        return this.arriveHomeTimeWeekend;
    }

    public void setArriveHomeTimeWeekend(String arriveHomeTimeWeekend2) {
        this.arriveHomeTimeWeekend = arriveHomeTimeWeekend2;
        setValue();
    }

    public String getOffTimeWeekend() {
        return this.offTimeWeekend;
    }

    public void setOffTimeWeekend(String offTimeWeekend2) {
        this.offTimeWeekend = offTimeWeekend2;
        setValue();
    }

    public Float getImeiConfidence() {
        return this.imeiConfidence;
    }

    public void setImeiConfidence(Float imeiConfidence2) {
        this.imeiConfidence = imeiConfidence2;
        setValue();
    }

    public Float getHwIdConfidence() {
        return this.hwIdConfidence;
    }

    public void setHwIdConfidence(Float hwIdConfidence2) {
        this.hwIdConfidence = hwIdConfidence2;
        setValue();
    }

    public Float getAgeConfidence() {
        return this.ageConfidence;
    }

    public void setAgeConfidence(Float ageConfidence2) {
        this.ageConfidence = ageConfidence2;
        setValue();
    }

    public Float getSexualityConfidence() {
        return this.sexualityConfidence;
    }

    public void setSexualityConfidence(Float sexualityConfidence2) {
        this.sexualityConfidence = sexualityConfidence2;
        setValue();
    }

    public Float getContactNumberConfidence() {
        return this.contactNumberConfidence;
    }

    public void setContactNumberConfidence(Float contactNumberConfidence2) {
        this.contactNumberConfidence = contactNumberConfidence2;
        setValue();
    }

    public Float getAccomConfidence() {
        return this.accomConfidence;
    }

    public void setAccomConfidence(Float accomConfidence2) {
        this.accomConfidence = accomConfidence2;
        setValue();
    }

    public Float getWorkplaceConfidence() {
        return this.workplaceConfidence;
    }

    public void setWorkplaceConfidence(Float workplaceConfidence2) {
        this.workplaceConfidence = workplaceConfidence2;
        setValue();
    }

    public Float getPoi1Confidence() {
        return this.poi1Confidence;
    }

    public void setPoi1Confidence(Float poi1Confidence2) {
        this.poi1Confidence = poi1Confidence2;
        setValue();
    }

    public Float getPoi2Confidence() {
        return this.poi2Confidence;
    }

    public void setPoi2Confidence(Float poi2Confidence2) {
        this.poi2Confidence = poi2Confidence2;
        setValue();
    }

    public Float getPoi3Confidence() {
        return this.poi3Confidence;
    }

    public void setPoi3Confidence(Float poi3Confidence2) {
        this.poi3Confidence = poi3Confidence2;
        setValue();
    }

    public Float getPoi4Confidence() {
        return this.poi4Confidence;
    }

    public void setPoi4Confidence(Float poi4Confidence2) {
        this.poi4Confidence = poi4Confidence2;
        setValue();
    }

    public Float getPoi5Confidence() {
        return this.poi5Confidence;
    }

    public void setPoi5Confidence(Float poi5Confidence2) {
        this.poi5Confidence = poi5Confidence2;
        setValue();
    }

    public Float getBaseCityConfidence() {
        return this.baseCityConfidence;
    }

    public void setBaseCityConfidence(Float baseCityConfidence2) {
        this.baseCityConfidence = baseCityConfidence2;
        setValue();
    }

    public Float getRoamingRadiusConfidence() {
        return this.roamingRadiusConfidence;
    }

    public void setRoamingRadiusConfidence(Float roamingRadiusConfidence2) {
        this.roamingRadiusConfidence = roamingRadiusConfidence2;
        setValue();
    }

    public Float getBuziTripPreferConfidence() {
        return this.buziTripPreferConfidence;
    }

    public void setBuziTripPreferConfidence(Float buziTripPreferConfidence2) {
        this.buziTripPreferConfidence = buziTripPreferConfidence2;
        setValue();
    }

    public Float getPhotoPreferConfidence() {
        return this.photoPreferConfidence;
    }

    public void setPhotoPreferConfidence(Float photoPreferConfidence2) {
        this.photoPreferConfidence = photoPreferConfidence2;
        setValue();
    }

    public Float getPhotoPreferTurismConfidence() {
        return this.photoPreferTurismConfidence;
    }

    public void setPhotoPreferTurismConfidence(Float photoPreferTurismConfidence2) {
        this.photoPreferTurismConfidence = photoPreferTurismConfidence2;
        setValue();
    }

    public Float getTopModeConfidence() {
        return this.topModeConfidence;
    }

    public void setTopModeConfidence(Float topModeConfidence2) {
        this.topModeConfidence = topModeConfidence2;
        setValue();
    }

    public Float getTop1TagConfidence() {
        return this.top1TagConfidence;
    }

    public void setTop1TagConfidence(Float top1TagConfidence2) {
        this.top1TagConfidence = top1TagConfidence2;
        setValue();
    }

    public Float getTop2TagConfidence() {
        return this.top2TagConfidence;
    }

    public void setTop2TagConfidence(Float top2TagConfidence2) {
        this.top2TagConfidence = top2TagConfidence2;
        setValue();
    }

    public Float getTop3TagConfidence() {
        return this.top3TagConfidence;
    }

    public void setTop3TagConfidence(Float top3TagConfidence2) {
        this.top3TagConfidence = top3TagConfidence2;
        setValue();
    }

    public Float getTop4TagConfidence() {
        return this.top4TagConfidence;
    }

    public void setTop4TagConfidence(Float top4TagConfidence2) {
        this.top4TagConfidence = top4TagConfidence2;
        setValue();
    }

    public Float getTop5TagConfidence() {
        return this.top5TagConfidence;
    }

    public void setTop5TagConfidence(Float top5TagConfidence2) {
        this.top5TagConfidence = top5TagConfidence2;
        setValue();
    }

    public Float getTop6TagConfidence() {
        return this.top6TagConfidence;
    }

    public void setTop6TagConfidence(Float top6TagConfidence2) {
        this.top6TagConfidence = top6TagConfidence2;
        setValue();
    }

    public Float getTop7TagConfidence() {
        return this.top7TagConfidence;
    }

    public void setTop7TagConfidence(Float top7TagConfidence2) {
        this.top7TagConfidence = top7TagConfidence2;
        setValue();
    }

    public Float getTop8TagConfidence() {
        return this.top8TagConfidence;
    }

    public void setTop8TagConfidence(Float top8TagConfidence2) {
        this.top8TagConfidence = top8TagConfidence2;
        setValue();
    }

    public Float getTop9TagConfidence() {
        return this.top9TagConfidence;
    }

    public void setTop9TagConfidence(Float top9TagConfidence2) {
        this.top9TagConfidence = top9TagConfidence2;
        setValue();
    }

    public Float getTop10TagConfidence() {
        return this.top10TagConfidence;
    }

    public void setTop10TagConfidence(Float top10TagConfidence2) {
        this.top10TagConfidence = top10TagConfidence2;
        setValue();
    }

    public Float getOnTimeWorkdayConfidence() {
        return this.onTimeWorkdayConfidence;
    }

    public void setOnTimeWorkdayConfidence(Float onTimeWorkdayConfidence2) {
        this.onTimeWorkdayConfidence = onTimeWorkdayConfidence2;
        setValue();
    }

    public Float getLeaveHomeTimeWorkdayConfidence() {
        return this.leaveHomeTimeWorkdayConfidence;
    }

    public void setLeaveHomeTimeWorkdayConfidence(Float leaveHomeTimeWorkdayConfidence2) {
        this.leaveHomeTimeWorkdayConfidence = leaveHomeTimeWorkdayConfidence2;
        setValue();
    }

    public Float getArriveWorkplaceTimeWorkdayConfidence() {
        return this.arriveWorkplaceTimeWorkdayConfidence;
    }

    public void setArriveWorkplaceTimeWorkdayConfidence(Float arriveWorkplaceTimeWorkdayConfidence2) {
        this.arriveWorkplaceTimeWorkdayConfidence = arriveWorkplaceTimeWorkdayConfidence2;
        setValue();
    }

    public Float getLeaveWorkplaceTimeWorkdayConfidence() {
        return this.leaveWorkplaceTimeWorkdayConfidence;
    }

    public void setLeaveWorkplaceTimeWorkdayConfidence(Float leaveWorkplaceTimeWorkdayConfidence2) {
        this.leaveWorkplaceTimeWorkdayConfidence = leaveWorkplaceTimeWorkdayConfidence2;
        setValue();
    }

    public Float getArriveHomeTimeWorkdayConfidence() {
        return this.arriveHomeTimeWorkdayConfidence;
    }

    public void setArriveHomeTimeWorkdayConfidence(Float arriveHomeTimeWorkdayConfidence2) {
        this.arriveHomeTimeWorkdayConfidence = arriveHomeTimeWorkdayConfidence2;
        setValue();
    }

    public Float getOffTimeWorkdayConfidence() {
        return this.offTimeWorkdayConfidence;
    }

    public void setOffTimeWorkdayConfidence(Float offTimeWorkdayConfidence2) {
        this.offTimeWorkdayConfidence = offTimeWorkdayConfidence2;
        setValue();
    }

    public Float getOnTimeWeekendConfidence() {
        return this.onTimeWeekendConfidence;
    }

    public void setOnTimeWeekendConfidence(Float onTimeWeekendConfidence2) {
        this.onTimeWeekendConfidence = onTimeWeekendConfidence2;
        setValue();
    }

    public Float getLeaveHomeTimeWeekendConfidence() {
        return this.leaveHomeTimeWeekendConfidence;
    }

    public void setLeaveHomeTimeWeekendConfidence(Float leaveHomeTimeWeekendConfidence2) {
        this.leaveHomeTimeWeekendConfidence = leaveHomeTimeWeekendConfidence2;
        setValue();
    }

    public Float getArriveHomeTimeWeekendConfidence() {
        return this.arriveHomeTimeWeekendConfidence;
    }

    public void setArriveHomeTimeWeekendConfidence(Float arriveHomeTimeWeekendConfidence2) {
        this.arriveHomeTimeWeekendConfidence = arriveHomeTimeWeekendConfidence2;
        setValue();
    }

    public Float getOffTimeWeekendConfidence() {
        return this.offTimeWeekendConfidence;
    }

    public void setOffTimeWeekendConfidence(Float offTimeWeekendConfidence2) {
        this.offTimeWeekendConfidence = offTimeWeekendConfidence2;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.deviceID != null) {
            out.writeByte((byte) 1);
            out.writeString(this.deviceID);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.hwId != null) {
            out.writeByte((byte) 1);
            out.writeString(this.hwId);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.age != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.age.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.sexuality != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.sexuality.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.contactNumber != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.contactNumber.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.accomGPS != null) {
            out.writeByte((byte) 1);
            out.writeString(this.accomGPS);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.accomCellId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.accomCellId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.accomCellLac != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.accomCellLac.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.workPlaceGPS != null) {
            out.writeByte((byte) 1);
            out.writeString(this.workPlaceGPS);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.workPlaceCellId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.workPlaceCellId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.workPlaceCellLac != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.workPlaceCellLac.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi1GPS != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi1GPS);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi1CellId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.poi1CellId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi1CellLac != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.poi1CellLac.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi2GPS != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi2GPS);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi2CellId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.poi2CellId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi2CellLac != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.poi2CellLac.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi3GPS != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi3GPS);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi3CellId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.poi3CellId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi3CellLac != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.poi3CellLac.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi4GPS != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi4GPS);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi4CellId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.poi4CellId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi4CellLac != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.poi4CellLac.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi5GPS != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi5GPS);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi5CellId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.poi5CellId.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi5CellLac != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.poi5CellLac.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.baseCity != null) {
            out.writeByte((byte) 1);
            out.writeString(this.baseCity);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.roamingRadius != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.roamingRadius.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.buziTripPrefer != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.buziTripPrefer.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.photoPrefer != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.photoPrefer.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.photoPreferTurism != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.photoPreferTurism.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.topMode != null) {
            out.writeByte((byte) 1);
            out.writeString(this.topMode);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top1Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top1Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top2Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top2Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top3Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top3Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top4Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top4Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top5Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top5Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top6Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top6Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top7Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top7Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top8Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top8Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top9Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top9Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top10Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top10Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.onTimeWorkday != null) {
            out.writeByte((byte) 1);
            out.writeString(this.onTimeWorkday);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.leaveHomeTimeWorkday != null) {
            out.writeByte((byte) 1);
            out.writeString(this.leaveHomeTimeWorkday);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.arriveWorkplaceTimeWorkday != null) {
            out.writeByte((byte) 1);
            out.writeString(this.arriveWorkplaceTimeWorkday);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.leaveWorkplaceTimeWorkday != null) {
            out.writeByte((byte) 1);
            out.writeString(this.leaveWorkplaceTimeWorkday);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.arriveHomeTimeWorkday != null) {
            out.writeByte((byte) 1);
            out.writeString(this.arriveHomeTimeWorkday);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.offTimeWorkday != null) {
            out.writeByte((byte) 1);
            out.writeString(this.offTimeWorkday);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.onTimeWeekend != null) {
            out.writeByte((byte) 1);
            out.writeString(this.onTimeWeekend);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.leaveHomeTimeWeekend != null) {
            out.writeByte((byte) 1);
            out.writeString(this.leaveHomeTimeWeekend);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.arriveHomeTimeWeekend != null) {
            out.writeByte((byte) 1);
            out.writeString(this.arriveHomeTimeWeekend);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.offTimeWeekend != null) {
            out.writeByte((byte) 1);
            out.writeString(this.offTimeWeekend);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.imeiConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.imeiConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.hwIdConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.hwIdConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.ageConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.ageConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.sexualityConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.sexualityConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.contactNumberConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.contactNumberConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.accomConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.accomConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.workplaceConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.workplaceConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi1Confidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.poi1Confidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi2Confidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.poi2Confidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi3Confidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.poi3Confidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi4Confidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.poi4Confidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi5Confidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.poi5Confidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.baseCityConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.baseCityConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.roamingRadiusConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.roamingRadiusConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.buziTripPreferConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.buziTripPreferConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.photoPreferConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.photoPreferConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.photoPreferTurismConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.photoPreferTurismConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.topModeConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.topModeConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top1TagConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.top1TagConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top2TagConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.top2TagConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top3TagConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.top3TagConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top4TagConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.top4TagConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top5TagConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.top5TagConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top6TagConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.top6TagConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top7TagConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.top7TagConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top8TagConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.top8TagConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top9TagConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.top9TagConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top10TagConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.top10TagConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.onTimeWorkdayConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.onTimeWorkdayConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.leaveHomeTimeWorkdayConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.leaveHomeTimeWorkdayConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.arriveWorkplaceTimeWorkdayConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.arriveWorkplaceTimeWorkdayConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.leaveWorkplaceTimeWorkdayConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.leaveWorkplaceTimeWorkdayConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.arriveHomeTimeWorkdayConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.arriveHomeTimeWorkdayConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.offTimeWorkdayConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.offTimeWorkdayConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.onTimeWeekendConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.onTimeWeekendConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.leaveHomeTimeWeekendConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.leaveHomeTimeWeekendConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.arriveHomeTimeWeekendConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.arriveHomeTimeWeekendConfidence.floatValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.offTimeWeekendConfidence != null) {
            out.writeByte((byte) 1);
            out.writeFloat(this.offTimeWeekendConfidence.floatValue());
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<MyProfile> getHelper() {
        return MyProfileHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.profile.MyProfile";
    }

    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("MyProfile { id: ").append(this.id);
        sb.append(", deviceID: ").append(this.deviceID);
        sb.append(", hwId: ").append(this.hwId);
        sb.append(", age: ").append(this.age);
        sb.append(", sexuality: ").append(this.sexuality);
        sb.append(", contactNumber: ").append(this.contactNumber);
        sb.append(", accomGPS: ").append(this.accomGPS);
        sb.append(", accomCellId: ").append(this.accomCellId);
        sb.append(", accomCellLac: ").append(this.accomCellLac);
        sb.append(", workPlaceGPS: ").append(this.workPlaceGPS);
        sb.append(", workPlaceCellId: ").append(this.workPlaceCellId);
        sb.append(", workPlaceCellLac: ").append(this.workPlaceCellLac);
        sb.append(", poi1GPS: ").append(this.poi1GPS);
        sb.append(", poi1CellId: ").append(this.poi1CellId);
        sb.append(", poi1CellLac: ").append(this.poi1CellLac);
        sb.append(", poi2GPS: ").append(this.poi2GPS);
        sb.append(", poi2CellId: ").append(this.poi2CellId);
        sb.append(", poi2CellLac: ").append(this.poi2CellLac);
        sb.append(", poi3GPS: ").append(this.poi3GPS);
        sb.append(", poi3CellId: ").append(this.poi3CellId);
        sb.append(", poi3CellLac: ").append(this.poi3CellLac);
        sb.append(", poi4GPS: ").append(this.poi4GPS);
        sb.append(", poi4CellId: ").append(this.poi4CellId);
        sb.append(", poi4CellLac: ").append(this.poi4CellLac);
        sb.append(", poi5GPS: ").append(this.poi5GPS);
        sb.append(", poi5CellId: ").append(this.poi5CellId);
        sb.append(", poi5CellLac: ").append(this.poi5CellLac);
        sb.append(", baseCity: ").append(this.baseCity);
        sb.append(", roamingRadius: ").append(this.roamingRadius);
        sb.append(", buziTripPrefer: ").append(this.buziTripPrefer);
        sb.append(", photoPrefer: ").append(this.photoPrefer);
        sb.append(", photoPreferTurism: ").append(this.photoPreferTurism);
        sb.append(", topMode: ").append(this.topMode);
        sb.append(", top1Tag: ").append(this.top1Tag);
        sb.append(", top2Tag: ").append(this.top2Tag);
        sb.append(", top3Tag: ").append(this.top3Tag);
        sb.append(", top4Tag: ").append(this.top4Tag);
        sb.append(", top5Tag: ").append(this.top5Tag);
        sb.append(", top6Tag: ").append(this.top6Tag);
        sb.append(", top7Tag: ").append(this.top7Tag);
        sb.append(", top8Tag: ").append(this.top8Tag);
        sb.append(", top9Tag: ").append(this.top9Tag);
        sb.append(", top10Tag: ").append(this.top10Tag);
        sb.append(", onTimeWorkday: ").append(this.onTimeWorkday);
        sb.append(", leaveHomeTimeWorkday: ").append(this.leaveHomeTimeWorkday);
        sb.append(", arriveWorkplaceTimeWorkday: ").append(this.arriveWorkplaceTimeWorkday);
        sb.append(", leaveWorkplaceTimeWorkday: ").append(this.leaveWorkplaceTimeWorkday);
        sb.append(", arriveHomeTimeWorkday: ").append(this.arriveHomeTimeWorkday);
        sb.append(", offTimeWorkday: ").append(this.offTimeWorkday);
        sb.append(", onTimeWeekend: ").append(this.onTimeWeekend);
        sb.append(", leaveHomeTimeWeekend: ").append(this.leaveHomeTimeWeekend);
        sb.append(", arriveHomeTimeWeekend: ").append(this.arriveHomeTimeWeekend);
        sb.append(", offTimeWeekend: ").append(this.offTimeWeekend);
        sb.append(", imeiConfidence: ").append(this.imeiConfidence);
        sb.append(", hwIdConfidence: ").append(this.hwIdConfidence);
        sb.append(", ageConfidence: ").append(this.ageConfidence);
        sb.append(", sexualityConfidence: ").append(this.sexualityConfidence);
        sb.append(", contactNumberConfidence: ").append(this.contactNumberConfidence);
        sb.append(", accomConfidence: ").append(this.accomConfidence);
        sb.append(", workplaceConfidence: ").append(this.workplaceConfidence);
        sb.append(", poi1Confidence: ").append(this.poi1Confidence);
        sb.append(", poi2Confidence: ").append(this.poi2Confidence);
        sb.append(", poi3Confidence: ").append(this.poi3Confidence);
        sb.append(", poi4Confidence: ").append(this.poi4Confidence);
        sb.append(", poi5Confidence: ").append(this.poi5Confidence);
        sb.append(", baseCityConfidence: ").append(this.baseCityConfidence);
        sb.append(", roamingRadiusConfidence: ").append(this.roamingRadiusConfidence);
        sb.append(", buziTripPreferConfidence: ").append(this.buziTripPreferConfidence);
        sb.append(", photoPreferConfidence: ").append(this.photoPreferConfidence);
        sb.append(", photoPreferTurismConfidence: ").append(this.photoPreferTurismConfidence);
        sb.append(", topModeConfidence: ").append(this.topModeConfidence);
        sb.append(", top1TagConfidence: ").append(this.top1TagConfidence);
        sb.append(", top2TagConfidence: ").append(this.top2TagConfidence);
        sb.append(", top3TagConfidence: ").append(this.top3TagConfidence);
        sb.append(", top4TagConfidence: ").append(this.top4TagConfidence);
        sb.append(", top5TagConfidence: ").append(this.top5TagConfidence);
        sb.append(", top6TagConfidence: ").append(this.top6TagConfidence);
        sb.append(", top7TagConfidence: ").append(this.top7TagConfidence);
        sb.append(", top8TagConfidence: ").append(this.top8TagConfidence);
        sb.append(", top9TagConfidence: ").append(this.top9TagConfidence);
        sb.append(", top10TagConfidence: ").append(this.top10TagConfidence);
        sb.append(", onTimeWorkdayConfidence: ").append(this.onTimeWorkdayConfidence);
        sb.append(", leaveHomeTimeWorkdayConfidence: ").append(this.leaveHomeTimeWorkdayConfidence);
        sb.append(", arriveWorkplaceTimeWorkdayConfidence: ").append(this.arriveWorkplaceTimeWorkdayConfidence);
        sb.append(", leaveWorkplaceTimeWorkdayConfidence: ").append(this.leaveWorkplaceTimeWorkdayConfidence);
        sb.append(", arriveHomeTimeWorkdayConfidence: ").append(this.arriveHomeTimeWorkdayConfidence);
        sb.append(", offTimeWorkdayConfidence: ").append(this.offTimeWorkdayConfidence);
        sb.append(", onTimeWeekendConfidence: ").append(this.onTimeWeekendConfidence);
        sb.append(", leaveHomeTimeWeekendConfidence: ").append(this.leaveHomeTimeWeekendConfidence);
        sb.append(", arriveHomeTimeWeekendConfidence: ").append(this.arriveHomeTimeWeekendConfidence);
        sb.append(", offTimeWeekendConfidence: ").append(this.offTimeWeekendConfidence);
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
        return "0.0.12";
    }

    public int getDatabaseVersionCode() {
        return 12;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
