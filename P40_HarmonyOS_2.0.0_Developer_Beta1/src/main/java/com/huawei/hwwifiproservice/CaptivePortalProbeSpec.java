package com.huawei.hwwifiproservice;

import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class CaptivePortalProbeSpec {
    private static final String REGEX_SEPARATOR = "@@/@@";
    private static final String SPEC_SEPARATOR = "@@,@@";
    private static final String TAG = CaptivePortalProbeSpec.class.getSimpleName();
    private final String mEncodedSpec;
    private final URL mUrl;

    public abstract CaptivePortalProbeResult getResult(int i, String str);

    CaptivePortalProbeSpec(String encodedSpec, URL url) {
        this.mEncodedSpec = (String) checkNotNull(encodedSpec);
        this.mUrl = (URL) checkNotNull(url);
    }

    public static CaptivePortalProbeSpec parseSpec(String spec) throws ParseException, MalformedURLException {
        if (!TextUtils.isEmpty(spec)) {
            String[] splits = TextUtils.split(spec, REGEX_SEPARATOR);
            if (splits.length == 3) {
                int statusRegexPos = splits[0].length() + REGEX_SEPARATOR.length();
                int locationRegexPos = splits[1].length() + statusRegexPos + REGEX_SEPARATOR.length();
                return new RegexMatchProbeSpec(spec, new URL(splits[0]), parsePatternIfNonEmpty(splits[1], statusRegexPos), parsePatternIfNonEmpty(splits[2], locationRegexPos));
            }
            throw new ParseException("Probe spec does not have 3 parts", 0);
        }
        throw new ParseException("Empty probe spec", 0);
    }

    private static Pattern parsePatternIfNonEmpty(String pattern, int pos) throws ParseException {
        if (TextUtils.isEmpty(pattern)) {
            return null;
        }
        try {
            return Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            throw new ParseException(String.format("Invalid status pattern [%s]: %s", pattern, e), pos);
        }
    }

    public static CaptivePortalProbeSpec parseSpecOrNull(String spec) {
        if (spec == null) {
            return null;
        }
        try {
            return parseSpec(spec);
        } catch (MalformedURLException | ParseException e) {
            HwHiLog.e(TAG, false, "Invalid probe spec: %{public}s", new Object[]{spec});
            return null;
        }
    }

    public static Collection<CaptivePortalProbeSpec> parseCaptivePortalProbeSpecs(String settingsVal) {
        List<CaptivePortalProbeSpec> specs = new ArrayList<>();
        if (settingsVal != null) {
            String[] split = TextUtils.split(settingsVal, SPEC_SEPARATOR);
            for (String spec : split) {
                try {
                    specs.add(parseSpec(spec));
                } catch (MalformedURLException | ParseException e) {
                    HwHiLog.e(TAG, false, "Invalid probe spec: %{public}s", new Object[]{spec});
                }
            }
        }
        if (specs.isEmpty()) {
            HwHiLog.e(TAG, false, "could not create any validation spec from %{public}s", new Object[]{settingsVal});
        }
        return specs;
    }

    public URL getUrl() {
        return this.mUrl;
    }

    /* access modifiers changed from: private */
    public static class RegexMatchProbeSpec extends CaptivePortalProbeSpec {
        final Pattern mLocationHeaderRegex;
        final Pattern mStatusRegex;

        RegexMatchProbeSpec(String spec, URL url, Pattern statusRegex, Pattern locationHeaderRegex) {
            super(spec, url);
            this.mStatusRegex = statusRegex;
            this.mLocationHeaderRegex = locationHeaderRegex;
        }

        @Override // com.huawei.hwwifiproservice.CaptivePortalProbeSpec
        public CaptivePortalProbeResult getResult(int status, String locationHeader) {
            return new CaptivePortalProbeResult((!CaptivePortalProbeSpec.isSafeMatch(String.valueOf(status), this.mStatusRegex) || !CaptivePortalProbeSpec.isSafeMatch(locationHeader, this.mLocationHeaderRegex)) ? CaptivePortalProbeResult.PORTAL_CODE : 204, locationHeader, getUrl().toString(), this);
        }
    }

    /* access modifiers changed from: private */
    public static boolean isSafeMatch(String value, Pattern pattern) {
        return pattern == null || TextUtils.isEmpty(value) || pattern.matcher(value).matches();
    }

    private static <T> T checkNotNull(T object) {
        if (object != null) {
            return object;
        }
        throw new NullPointerException();
    }
}
