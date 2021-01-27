package com.android.server.pm.auth.processor;

import android.content.pm.PackageParser;
import android.text.TextUtils;
import com.android.server.pm.auth.HwCertification;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;

public class ValidPeriodProcessor implements IProcessor {
    private static final String DATE_ELEMENT = "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})";
    private static final String DATE_REGEX = "from (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})";

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean readCert(String certLine, HwCertification.CertificationData certData) {
        if (TextUtils.isEmpty(certLine) || certData == null || !certLine.startsWith(HwCertification.KEY_VALID_PERIOD) || certLine.length() <= HwCertification.KEY_VALID_PERIOD.length() + 1) {
            return false;
        }
        String validPeriod = certLine.substring(HwCertification.KEY_VALID_PERIOD.length() + 1);
        if (TextUtils.isEmpty(validPeriod)) {
            HwAuthLogger.error(IProcessor.TAG, "VP_RC empty!");
            return false;
        }
        certData.setPeriodString(validPeriod);
        return true;
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseCert(HwCertification hwCert) {
        HwCertification.CertificationData certData;
        if (hwCert == null || (certData = hwCert.getCertificationData()) == null) {
            return false;
        }
        String period = certData.getPeriodString();
        if (period == null) {
            HwAuthLogger.error(IProcessor.TAG, "VP_PC error time is null!");
            return false;
        }
        Matcher matcher = Pattern.compile(DATE_REGEX).matcher(period);
        if (matcher.matches()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                hwCert.setFromDate(dateFormat.parse(matcher.group(1)));
                hwCert.setToDate(dateFormat.parse(matcher.group(2)));
                return true;
            } catch (ParseException e) {
                HwAuthLogger.error(IProcessor.TAG, "VP_PC time parser exception!");
                HwAuthLogger.info(IProcessor.TAG, "period:" + period + ", m.groupCount():" + matcher.groupCount());
                int groupCount = matcher.groupCount();
                for (int i = 1; i <= groupCount; i++) {
                    HwAuthLogger.info(IProcessor.TAG, "m.group " + i + AwarenessInnerConstants.COLON_KEY + matcher.group(i));
                }
                return false;
            }
        } else {
            int groupCount2 = matcher.groupCount();
            for (int i2 = 1; i2 <= groupCount2; i2++) {
                HwAuthLogger.info(IProcessor.TAG, "m.group " + i2 + AwarenessInnerConstants.COLON_KEY + matcher.group(i2));
            }
            HwAuthLogger.error(IProcessor.TAG, "VP_PC error!");
            return false;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean verifyCert(PackageParser.Package pkg, HwCertification hwCert) {
        if (pkg == null || hwCert == null) {
            return false;
        }
        if ("2".equals(hwCert.getVersion())) {
            return true;
        }
        Date fromDate = hwCert.getFromDate();
        Date toDate = hwCert.getToDate();
        if (fromDate == null || toDate == null) {
            return false;
        }
        if (!toDate.after(fromDate)) {
            HwAuthLogger.error(IProcessor.TAG, "VP_VC date error from time is :" + fromDate + ", toTime is:" + toDate);
            return false;
        } else if (!isCurrentDataExpired(toDate)) {
            return true;
        } else {
            if (hwCert.getCertificationData() != null) {
                HwAuthLogger.error(IProcessor.TAG, "VP_VC date expired " + hwCert.getCertificationData().getPeriodString());
            }
            return false;
        }
    }

    @Override // com.android.server.pm.auth.processor.IProcessor
    public boolean parseXmlTag(String tag, XmlPullParser parser, HwCertification hwCert) {
        if (!TextUtils.isEmpty(tag) && parser != null && hwCert != null && HwCertification.KEY_VALID_PERIOD.equals(tag)) {
            String datePeriodString = parser.getAttributeValue(null, "value");
            HwCertification.CertificationData certData = hwCert.getCertificationData();
            if (certData != null) {
                certData.setPeriodString(datePeriodString);
                return true;
            }
        }
        return false;
    }

    private boolean isCurrentDataExpired(Date toDate) {
        return System.currentTimeMillis() > toDate.getTime();
    }
}
