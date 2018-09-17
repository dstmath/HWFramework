package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import com.android.org.bouncycastle.util.Strings;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

public class ASN1UTCTime extends ASN1Primitive {
    private byte[] time;

    public static ASN1UTCTime getInstance(Object obj) {
        if (obj == null || (obj instanceof ASN1UTCTime)) {
            return (ASN1UTCTime) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (ASN1UTCTime) ASN1Primitive.fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    public static ASN1UTCTime getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Object o = obj.getObject();
        if (explicit || (o instanceof ASN1UTCTime)) {
            return getInstance(o);
        }
        return new ASN1UTCTime(((ASN1OctetString) o).getOctets());
    }

    public ASN1UTCTime(String time) {
        this.time = Strings.toByteArray(time);
        try {
            getDate();
        } catch (ParseException e) {
            throw new IllegalArgumentException("invalid date string: " + e.getMessage());
        }
    }

    public ASN1UTCTime(Date time) {
        SimpleDateFormat dateF = new SimpleDateFormat("yyMMddHHmmss'Z'", Locale.US);
        dateF.setTimeZone(new SimpleTimeZone(0, "Z"));
        this.time = Strings.toByteArray(dateF.format(time));
    }

    public ASN1UTCTime(Date time, Locale locale) {
        SimpleDateFormat dateF = new SimpleDateFormat("yyMMddHHmmss'Z'", Locale.US);
        dateF.setCalendar(Calendar.getInstance(locale));
        dateF.setTimeZone(new SimpleTimeZone(0, "Z"));
        this.time = Strings.toByteArray(dateF.format(time));
    }

    ASN1UTCTime(byte[] time) {
        this.time = time;
    }

    public Date getDate() throws ParseException {
        return new SimpleDateFormat("yyMMddHHmmssz", Locale.US).parse(getTime());
    }

    public Date getAdjustedDate() throws ParseException {
        SimpleDateFormat dateF = new SimpleDateFormat("yyyyMMddHHmmssz", Locale.US);
        dateF.setTimeZone(new SimpleTimeZone(0, "Z"));
        return dateF.parse(getAdjustedTime());
    }

    public String getTime() {
        String stime = Strings.fromByteArray(this.time);
        if (stime.indexOf(45) >= 0 || stime.indexOf(43) >= 0) {
            int index = stime.indexOf(45);
            if (index < 0) {
                index = stime.indexOf(43);
            }
            String d = stime;
            if (index == stime.length() - 3) {
                d = stime + "00";
            }
            if (index == 10) {
                return d.substring(0, 10) + "00GMT" + d.substring(10, 13) + ":" + d.substring(13, 15);
            }
            return d.substring(0, 12) + "GMT" + d.substring(12, 15) + ":" + d.substring(15, 17);
        } else if (stime.length() == 11) {
            return stime.substring(0, 10) + "00GMT+00:00";
        } else {
            return stime.substring(0, 12) + "GMT+00:00";
        }
    }

    public String getAdjustedTime() {
        String d = getTime();
        if (d.charAt(0) < '5') {
            return "20" + d;
        }
        return "19" + d;
    }

    boolean isConstructed() {
        return false;
    }

    int encodedLength() {
        int length = this.time.length;
        return (StreamUtil.calculateBodyLength(length) + 1) + length;
    }

    void encode(ASN1OutputStream out) throws IOException {
        out.write(23);
        int length = this.time.length;
        out.writeLength(length);
        for (int i = 0; i != length; i++) {
            out.write(this.time[i]);
        }
    }

    boolean asn1Equals(ASN1Primitive o) {
        if (o instanceof ASN1UTCTime) {
            return Arrays.areEqual(this.time, ((ASN1UTCTime) o).time);
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(this.time);
    }

    public String toString() {
        return Strings.fromByteArray(this.time);
    }
}
