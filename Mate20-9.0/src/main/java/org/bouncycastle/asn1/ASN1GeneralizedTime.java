package org.bouncycastle.asn1;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

public class ASN1GeneralizedTime extends ASN1Primitive {
    protected byte[] time;

    public ASN1GeneralizedTime(String str) {
        this.time = Strings.toByteArray(str);
        try {
            getDate();
        } catch (ParseException e) {
            throw new IllegalArgumentException("invalid date string: " + e.getMessage());
        }
    }

    public ASN1GeneralizedTime(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
        simpleDateFormat.setTimeZone(new SimpleTimeZone(0, "Z"));
        this.time = Strings.toByteArray(simpleDateFormat.format(date));
    }

    public ASN1GeneralizedTime(Date date, Locale locale) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss'Z'", locale);
        simpleDateFormat.setTimeZone(new SimpleTimeZone(0, "Z"));
        this.time = Strings.toByteArray(simpleDateFormat.format(date));
    }

    ASN1GeneralizedTime(byte[] bArr) {
        this.time = bArr;
    }

    private String calculateGMTOffset() {
        String str = "+";
        TimeZone timeZone = TimeZone.getDefault();
        int rawOffset = timeZone.getRawOffset();
        if (rawOffset < 0) {
            str = "-";
            rawOffset = -rawOffset;
        }
        int i = rawOffset / 3600000;
        int i2 = (rawOffset - (((i * 60) * 60) * 1000)) / 60000;
        try {
            if (timeZone.useDaylightTime() && timeZone.inDaylightTime(getDate())) {
                i += str.equals("+") ? 1 : -1;
            }
        } catch (ParseException e) {
        }
        return "GMT" + str + convert(i) + ":" + convert(i2);
    }

    private String convert(int i) {
        if (i >= 10) {
            return Integer.toString(i);
        }
        return "0" + i;
    }

    public static ASN1GeneralizedTime getInstance(Object obj) {
        if (obj == null || (obj instanceof ASN1GeneralizedTime)) {
            return (ASN1GeneralizedTime) obj;
        }
        if (obj instanceof byte[]) {
            try {
                return (ASN1GeneralizedTime) fromByteArray((byte[]) obj);
            } catch (Exception e) {
                throw new IllegalArgumentException("encoding error in getInstance: " + e.toString());
            }
        } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
        }
    }

    public static ASN1GeneralizedTime getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        ASN1Primitive object = aSN1TaggedObject.getObject();
        return (z || (object instanceof ASN1GeneralizedTime)) ? getInstance(object) : new ASN1GeneralizedTime(((ASN1OctetString) object).getOctets());
    }

    private boolean isDigit(int i) {
        return this.time.length > i && this.time[i] >= 48 && this.time[i] <= 57;
    }

    /* access modifiers changed from: package-private */
    public boolean asn1Equals(ASN1Primitive aSN1Primitive) {
        if (!(aSN1Primitive instanceof ASN1GeneralizedTime)) {
            return false;
        }
        return Arrays.areEqual(this.time, ((ASN1GeneralizedTime) aSN1Primitive).time);
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream aSN1OutputStream) throws IOException {
        aSN1OutputStream.writeEncoded(24, this.time);
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() {
        int length = this.time.length;
        return 1 + StreamUtil.calculateBodyLength(length) + length;
    }

    public Date getDate() throws ParseException {
        SimpleDateFormat simpleDateFormat;
        SimpleTimeZone simpleTimeZone;
        String str;
        StringBuilder sb;
        String fromByteArray = Strings.fromByteArray(this.time);
        if (fromByteArray.endsWith("Z")) {
            simpleDateFormat = hasFractionalSeconds() ? new SimpleDateFormat("yyyyMMddHHmmss.SSS'Z'") : hasSeconds() ? new SimpleDateFormat("yyyyMMddHHmmss'Z'") : hasMinutes() ? new SimpleDateFormat("yyyyMMddHHmm'Z'") : new SimpleDateFormat("yyyyMMddHH'Z'");
            simpleTimeZone = new SimpleTimeZone(0, "Z");
        } else if (fromByteArray.indexOf(45) > 0 || fromByteArray.indexOf(43) > 0) {
            fromByteArray = getTime();
            simpleDateFormat = hasFractionalSeconds() ? new SimpleDateFormat("yyyyMMddHHmmss.SSSz") : hasSeconds() ? new SimpleDateFormat("yyyyMMddHHmmssz") : hasMinutes() ? new SimpleDateFormat("yyyyMMddHHmmz") : new SimpleDateFormat("yyyyMMddHHz");
            simpleTimeZone = new SimpleTimeZone(0, "Z");
        } else {
            simpleDateFormat = hasFractionalSeconds() ? new SimpleDateFormat("yyyyMMddHHmmss.SSS") : hasSeconds() ? new SimpleDateFormat("yyyyMMddHHmmss") : hasMinutes() ? new SimpleDateFormat("yyyyMMddHHmm") : new SimpleDateFormat("yyyyMMddHH");
            simpleTimeZone = new SimpleTimeZone(0, TimeZone.getDefault().getID());
        }
        simpleDateFormat.setTimeZone(simpleTimeZone);
        if (hasFractionalSeconds()) {
            String substring = fromByteArray.substring(14);
            int i = 1;
            while (i < substring.length()) {
                char charAt = substring.charAt(i);
                if ('0' > charAt || charAt > '9') {
                    break;
                }
                i++;
            }
            int i2 = i - 1;
            if (i2 > 3) {
                str = substring.substring(0, 4) + substring.substring(i);
                sb = new StringBuilder();
            } else if (i2 == 1) {
                str = substring.substring(0, i) + "00" + substring.substring(i);
                sb = new StringBuilder();
            } else if (i2 == 2) {
                str = substring.substring(0, i) + "0" + substring.substring(i);
                sb = new StringBuilder();
            }
            sb.append(fromByteArray.substring(0, 14));
            sb.append(str);
            fromByteArray = sb.toString();
        }
        return simpleDateFormat.parse(fromByteArray);
    }

    public String getTime() {
        String fromByteArray = Strings.fromByteArray(this.time);
        if (fromByteArray.charAt(fromByteArray.length() - 1) == 'Z') {
            return fromByteArray.substring(0, fromByteArray.length() - 1) + "GMT+00:00";
        }
        int length = fromByteArray.length() - 5;
        char charAt = fromByteArray.charAt(length);
        if (charAt == '-' || charAt == '+') {
            StringBuilder sb = new StringBuilder();
            sb.append(fromByteArray.substring(0, length));
            sb.append("GMT");
            int i = length + 3;
            sb.append(fromByteArray.substring(length, i));
            sb.append(":");
            sb.append(fromByteArray.substring(i));
            return sb.toString();
        }
        char charAt2 = fromByteArray.charAt(fromByteArray.length() - 3);
        if (charAt2 == '-' || charAt2 == '+') {
            return fromByteArray.substring(0, r1) + "GMT" + fromByteArray.substring(r1) + ":00";
        }
        return fromByteArray + calculateGMTOffset();
    }

    public String getTimeString() {
        return Strings.fromByteArray(this.time);
    }

    /* access modifiers changed from: protected */
    public boolean hasFractionalSeconds() {
        for (int i = 0; i != this.time.length; i++) {
            if (this.time[i] == 46 && i == 14) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean hasMinutes() {
        return isDigit(10) && isDigit(11);
    }

    /* access modifiers changed from: protected */
    public boolean hasSeconds() {
        return isDigit(12) && isDigit(13);
    }

    public int hashCode() {
        return Arrays.hashCode(this.time);
    }

    /* access modifiers changed from: package-private */
    public boolean isConstructed() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public ASN1Primitive toDERObject() {
        return new DERGeneralizedTime(this.time);
    }
}
