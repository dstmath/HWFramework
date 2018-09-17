package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1Choice;
import com.android.org.bouncycastle.asn1.ASN1GeneralizedTime;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.ASN1UTCTime;
import com.android.org.bouncycastle.asn1.DERGeneralizedTime;
import com.android.org.bouncycastle.asn1.DERUTCTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

public class Time extends ASN1Object implements ASN1Choice {
    ASN1Primitive time;

    public static Time getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(obj.getObject());
    }

    public Time(ASN1Primitive time) {
        if ((time instanceof ASN1UTCTime) || ((time instanceof ASN1GeneralizedTime) ^ 1) == 0) {
            this.time = time;
            return;
        }
        throw new IllegalArgumentException("unknown object passed to Time");
    }

    public Time(Date time) {
        SimpleTimeZone tz = new SimpleTimeZone(0, "Z");
        SimpleDateFormat dateF = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        dateF.setTimeZone(tz);
        String d = dateF.format(time) + "Z";
        int year = Integer.parseInt(d.substring(0, 4));
        if (year < 1950 || year > 2049) {
            this.time = new DERGeneralizedTime(d);
        } else {
            this.time = new DERUTCTime(d.substring(2));
        }
    }

    public Time(Date time, Locale locale) {
        SimpleTimeZone tz = new SimpleTimeZone(0, "Z");
        SimpleDateFormat dateF = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        dateF.setCalendar(Calendar.getInstance(locale));
        dateF.setTimeZone(tz);
        String d = dateF.format(time) + "Z";
        int year = Integer.parseInt(d.substring(0, 4));
        if (year < 1950 || year > 2049) {
            this.time = new DERGeneralizedTime(d);
        } else {
            this.time = new DERUTCTime(d.substring(2));
        }
    }

    public static Time getInstance(Object obj) {
        if (obj == null || (obj instanceof Time)) {
            return (Time) obj;
        }
        if (obj instanceof ASN1UTCTime) {
            return new Time((ASN1UTCTime) obj);
        }
        if (obj instanceof ASN1GeneralizedTime) {
            return new Time((ASN1GeneralizedTime) obj);
        }
        throw new IllegalArgumentException("unknown object in factory: " + obj.getClass().getName());
    }

    public String getTime() {
        if (this.time instanceof ASN1UTCTime) {
            return ((ASN1UTCTime) this.time).getAdjustedTime();
        }
        return ((ASN1GeneralizedTime) this.time).getTime();
    }

    public Date getDate() {
        try {
            if (this.time instanceof ASN1UTCTime) {
                return ((ASN1UTCTime) this.time).getAdjustedDate();
            }
            return ((ASN1GeneralizedTime) this.time).getDate();
        } catch (ParseException e) {
            throw new IllegalStateException("invalid date string: " + e.getMessage());
        }
    }

    public ASN1Primitive toASN1Primitive() {
        return this.time;
    }

    public String toString() {
        return getTime();
    }
}
