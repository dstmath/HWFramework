package com.huawei.zxing.client.result;

import com.huawei.zxing.Result;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GeoResultParser extends ResultParser {
    private static final Pattern GEO_URL_PATTERN = Pattern.compile("geo:([\\-0-9.]+),([\\-0-9.]+)(?:,([\\-0-9.]+))?(?:\\?(.*))?", 2);

    @Override // com.huawei.zxing.client.result.ResultParser
    public GeoParsedResult parse(Result result) {
        double altitude;
        Matcher matcher = GEO_URL_PATTERN.matcher(getMassagedText(result));
        if (!matcher.matches()) {
            return null;
        }
        String query = matcher.group(4);
        try {
            double latitude = Double.parseDouble(matcher.group(1));
            if (latitude <= 90.0d) {
                if (latitude >= -90.0d) {
                    double longitude = Double.parseDouble(matcher.group(2));
                    if (longitude <= 180.0d) {
                        if (longitude >= -180.0d) {
                            if (matcher.group(3) == null) {
                                altitude = 0.0d;
                            } else {
                                double altitude2 = Double.parseDouble(matcher.group(3));
                                if (altitude2 < 0.0d) {
                                    return null;
                                }
                                altitude = altitude2;
                            }
                            return new GeoParsedResult(latitude, longitude, altitude, query);
                        }
                    }
                    return null;
                }
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
