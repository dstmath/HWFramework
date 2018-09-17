package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser.Package;
import com.android.server.pm.auth.HwCertXmlHandler;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.HwCertification.CertificationData;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;

public class ValidPeriodProcessor extends BaseProcessor {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean readCert(String line, CertificationData rawCert) {
        if (line == null || line.isEmpty() || !line.startsWith(HwCertification.KEY_VALID_PERIOD)) {
            return false;
        }
        String key = line.substring(HwCertification.KEY_VALID_PERIOD.length() + 1);
        if (key == null || key.isEmpty()) {
            HwAuthLogger.e(Utils.TAG, "readCert:ValidPeriod is empty");
            return false;
        }
        rawCert.mPeriodString = key;
        return true;
    }

    public boolean parserCert(HwCertification rawCert) {
        int i;
        String period = rawCert.mCertificationData.mPeriodString;
        if (period == null) {
            HwAuthLogger.e(Utils.TAG, "parserCert:ValidPeriod error,period time is null");
            return false;
        }
        String dateElem = "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})";
        Matcher m = Pattern.compile("from " + dateElem + " " + HwCertification.KEY_DATE_TO + " " + dateElem).matcher(period);
        if (m.matches()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                rawCert.setFromDate(dateFormat.parse(m.group(1)));
                rawCert.setToDate(dateFormat.parse(m.group(2)));
                return true;
            } catch (ParseException e) {
                HwAuthLogger.e(Utils.TAG, "parserCert:ValidPeriod error,catch ParseException");
                if (HwAuthLogger.getHWFLOW()) {
                    HwAuthLogger.i(Utils.TAG, "period:" + period + "m.groupCount():" + m.groupCount());
                    for (i = 1; i <= m.groupCount(); i++) {
                        HwAuthLogger.i(Utils.TAG, "m.group" + i + ":" + m.group(i));
                    }
                }
                return false;
            }
        }
        if (HwAuthLogger.getHWFLOW()) {
            for (i = 1; i <= m.groupCount(); i++) {
                HwAuthLogger.i(Utils.TAG, "m.group" + i + ":" + m.group(i));
            }
        }
        HwAuthLogger.e(Utils.TAG, "parserCert:ValidPeriod error");
        return false;
    }

    public boolean verifyCert(Package pkg, HwCertification cert) {
        if (HwAuthLogger.getHWFLOW()) {
            HwAuthLogger.i(Utils.TAG, "--Verify date--");
        }
        Date fromDate = cert.getFromDate();
        Date toDate = cert.getToDate();
        if (fromDate == null || toDate == null) {
            return false;
        }
        if (!toDate.after(fromDate)) {
            HwAuthLogger.e(Utils.TAG, "verifyCert:date error, fromTime is : " + fromDate + ", toTime is:" + toDate);
            return false;
        } else if (!isCurrentDataExpired(toDate)) {
            return true;
        } else {
            HwAuthLogger.e(Utils.TAG, "verifyCert:date expired," + cert.mCertificationData.mPeriodString);
            return false;
        }
    }

    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification cert) {
        if (!HwCertification.KEY_VALID_PERIOD.equals(tag)) {
            return false;
        }
        cert.mCertificationData.mPeriodString = parser.getAttributeValue(null, HwCertXmlHandler.TAG_VALUE);
        return true;
    }

    private boolean isCurrentDataExpired(Date toDate) {
        return System.currentTimeMillis() > toDate.getTime();
    }
}
