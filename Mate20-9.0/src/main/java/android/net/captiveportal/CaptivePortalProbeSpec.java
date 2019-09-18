package android.net.captiveportal;

import android.text.TextUtils;
import android.util.Log;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class CaptivePortalProbeSpec {
    public static final String HTTP_LOCATION_HEADER_NAME = "Location";
    private static final String REGEX_SEPARATOR = "@@/@@";
    private static final String SPEC_SEPARATOR = "@@,@@";
    private static final String TAG = CaptivePortalProbeSpec.class.getSimpleName();
    private final String mEncodedSpec;
    private final URL mUrl;

    private static class RegexMatchProbeSpec extends CaptivePortalProbeSpec {
        final Pattern mLocationHeaderRegex;
        final Pattern mStatusRegex;

        RegexMatchProbeSpec(String spec, URL url, Pattern statusRegex, Pattern locationHeaderRegex) {
            super(spec, url);
            this.mStatusRegex = statusRegex;
            this.mLocationHeaderRegex = locationHeaderRegex;
        }

        public CaptivePortalProbeResult getResult(int status, String locationHeader) {
            return new CaptivePortalProbeResult((!CaptivePortalProbeSpec.safeMatch(String.valueOf(status), this.mStatusRegex) || !CaptivePortalProbeSpec.safeMatch(locationHeader, this.mLocationHeaderRegex)) ? 302 : 204, locationHeader, getUrl().toString(), this);
        }
    }

    public abstract CaptivePortalProbeResult getResult(int i, String str);

    CaptivePortalProbeSpec(String encodedSpec, URL url) {
        this.mEncodedSpec = encodedSpec;
        this.mUrl = url;
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
            throw new ParseException(String.format("Invalid status pattern [%s]: %s", new Object[]{pattern, e}), pos);
        }
    }

    public static CaptivePortalProbeSpec parseSpecOrNull(String spec) {
        if (spec != null) {
            try {
                return parseSpec(spec);
            } catch (MalformedURLException | ParseException e) {
                String str = TAG;
                Log.e(str, "Invalid probe spec: " + spec, e);
            }
        }
        return null;
    }

    public static CaptivePortalProbeSpec[] parseCaptivePortalProbeSpecs(String settingsVal) {
        List<CaptivePortalProbeSpec> specs = new ArrayList<>();
        if (settingsVal != null) {
            for (String spec : TextUtils.split(settingsVal, SPEC_SEPARATOR)) {
                try {
                    specs.add(parseSpec(spec));
                } catch (MalformedURLException | ParseException e) {
                    Log.e(TAG, "Invalid probe spec: " + spec, e);
                }
            }
        }
        if (specs.isEmpty()) {
            Log.e(TAG, String.format("could not create any validation spec from %s", new Object[]{settingsVal}));
        }
        return (CaptivePortalProbeSpec[]) specs.toArray(new CaptivePortalProbeSpec[specs.size()]);
    }

    public String getEncodedSpec() {
        return this.mEncodedSpec;
    }

    public URL getUrl() {
        return this.mUrl;
    }

    /* access modifiers changed from: private */
    public static boolean safeMatch(String value, Pattern pattern) {
        return pattern == null || TextUtils.isEmpty(value) || pattern.matcher(value).matches();
    }
}
