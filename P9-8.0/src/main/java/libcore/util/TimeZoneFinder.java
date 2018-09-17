package libcore.util;

import android.icu.util.TimeZone;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import libcore.util.-$Lambda$09wOAsWezlJeOaYdPI5ZpaVhwSY.AnonymousClass1;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class TimeZoneFinder {
    private static final String COUNTRY_CODE_ATTRIBUTE = "code";
    private static final String COUNTRY_ELEMENT = "country";
    private static final String COUNTRY_ZONES_ELEMENT = "countryzones";
    private static final String ID_ELEMENT = "id";
    private static final String TIMEZONES_ELEMENT = "timezones";
    private static final String TZLOOKUP_FILE_NAME = "tzlookup.xml";
    private static TimeZoneFinder instance;
    private String lastCountryIso;
    private List<TimeZone> lastCountryTimeZones;
    private final ReaderSupplier xmlSource;

    private interface ReaderSupplier {
        Reader get() throws IOException;

        static ReaderSupplier forFile(String fileName, Charset charSet) throws IOException {
            Path file = Paths.get(fileName, new String[0]);
            if (!Files.exists(file, new LinkOption[0])) {
                throw new FileNotFoundException(fileName + " does not exist");
            } else if (Files.isRegularFile(file, new LinkOption[0]) || !Files.isReadable(file)) {
                return new AnonymousClass1(file, charSet);
            } else {
                throw new IOException(fileName + " must be a regular readable file.");
            }
        }

        static ReaderSupplier forString(String xml) {
            return new -$Lambda$09wOAsWezlJeOaYdPI5ZpaVhwSY(xml);
        }
    }

    private interface CountryZonesProcessor {
        public static final boolean CONTINUE = true;
        public static final boolean HALT = false;

        boolean process(String str, List<String> list, String str2) throws XmlPullParserException;
    }

    private static class CountryZonesExtractor implements CountryZonesProcessor {
        private final String countryCodeToMatch;
        private List<TimeZone> matchedZones;

        /* synthetic */ CountryZonesExtractor(String countryCodeToMatch, CountryZonesExtractor -this1) {
            this(countryCodeToMatch);
        }

        private CountryZonesExtractor(String countryCodeToMatch) {
            this.countryCodeToMatch = countryCodeToMatch;
        }

        public boolean process(String countryCode, List<String> timeZoneIds, String debugInfo) {
            if (!this.countryCodeToMatch.equals(countryCode)) {
                return true;
            }
            List<TimeZone> timeZones = new ArrayList();
            for (String zoneIdString : timeZoneIds) {
                TimeZone tz = TimeZone.getTimeZone(zoneIdString);
                if (tz.getID().equals(TimeZone.UNKNOWN_ZONE_ID)) {
                    System.logW("Skipping invalid zone: " + zoneIdString + " at " + debugInfo);
                } else {
                    timeZones.add(tz.freeze());
                }
            }
            this.matchedZones = Collections.unmodifiableList(timeZones);
            return false;
        }

        List<TimeZone> getMatchedZones() {
            return this.matchedZones;
        }
    }

    private static class CountryZonesValidator implements CountryZonesProcessor {
        private final Set<String> knownCountryCodes;

        /* synthetic */ CountryZonesValidator(CountryZonesValidator -this0) {
            this();
        }

        private CountryZonesValidator() {
            this.knownCountryCodes = new HashSet();
        }

        public boolean process(String countryCode, List<String> timeZoneIds, String debugInfo) throws XmlPullParserException {
            if (this.knownCountryCodes.contains(countryCode)) {
                throw new XmlPullParserException("Second entry for country code: " + countryCode + " at " + debugInfo);
            } else if (timeZoneIds.isEmpty()) {
                throw new XmlPullParserException("No time zone IDs for country code: " + countryCode + " at " + debugInfo);
            } else {
                this.knownCountryCodes.add(countryCode);
                return true;
            }
        }
    }

    private TimeZoneFinder(ReaderSupplier xmlSource) {
        this.xmlSource = xmlSource;
    }

    public static TimeZoneFinder getInstance() {
        synchronized (TimeZoneFinder.class) {
            if (instance == null) {
                String[] tzLookupFilePaths = TimeZoneDataFiles.getTimeZoneFilePaths(TZLOOKUP_FILE_NAME);
                instance = createInstanceWithFallback(tzLookupFilePaths[0], tzLookupFilePaths[1]);
            }
        }
        return instance;
    }

    public static TimeZoneFinder createInstanceWithFallback(String... tzLookupFilePaths) {
        int i = 0;
        int length = tzLookupFilePaths.length;
        while (i < length) {
            String tzLookupFilePath = tzLookupFilePaths[i];
            try {
                return createInstance(tzLookupFilePath);
            } catch (IOException e) {
                System.logE("Unable to process file: " + tzLookupFilePath + " Trying next one.", e);
                i++;
            }
        }
        System.logE("No valid file found in set: " + Arrays.toString(tzLookupFilePaths) + " Falling back to empty map.");
        return createInstanceForTests("<timezones><countryzones /></timezones>");
    }

    public static TimeZoneFinder createInstance(String path) throws IOException {
        return new TimeZoneFinder(ReaderSupplier.forFile(path, StandardCharsets.UTF_8));
    }

    public static TimeZoneFinder createInstanceForTests(String xml) {
        return new TimeZoneFinder(ReaderSupplier.forString(xml));
    }

    public void validate() throws IOException {
        try {
            processXml(new CountryZonesValidator());
        } catch (XmlPullParserException e) {
            throw new IOException("Parsing error", e);
        }
    }

    public TimeZone lookupTimeZoneByCountryAndOffset(String countryIso, int offsetSeconds, boolean isDst, long whenMillis, TimeZone bias) {
        List<TimeZone> candidates = lookupTimeZonesByCountry(countryIso);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        TimeZone firstMatch = null;
        for (int i = 0; i < candidates.size(); i++) {
            TimeZone match = (TimeZone) candidates.get(i);
            if (offsetMatchesAtTime(match, offsetSeconds, isDst, whenMillis)) {
                if (firstMatch == null) {
                    if (bias == null) {
                        return match;
                    }
                    firstMatch = match;
                }
                if (match.getID().equals(bias.getID())) {
                    return match;
                }
            }
        }
        return firstMatch;
    }

    private static boolean offsetMatchesAtTime(TimeZone timeZone, int offsetMillis, boolean isDst, long whenMillis) {
        boolean z = true;
        int[] offsets = new int[2];
        timeZone.getOffset(whenMillis, false, offsets);
        if (isDst != (offsets[1] != 0)) {
            return false;
        }
        if (offsetMillis != offsets[0] + offsets[1]) {
            z = false;
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:8:0x000e, code:
            r3 = new libcore.util.TimeZoneFinder.CountryZonesExtractor(r6, null);
            r0 = null;
     */
    /* JADX WARNING: Missing block: B:10:?, code:
            processXml(r3);
            r0 = r3.getMatchedZones();
     */
    /* JADX WARNING: Missing block: B:19:0x0026, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:20:0x0027, code:
            java.lang.System.logW("Error reading country zones ", r2);
     */
    /* JADX WARNING: Missing block: B:21:0x002e, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:0x002f, code:
            java.lang.System.logW("Error reading country zones ", r1);
            r6 = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<TimeZone> lookupTimeZonesByCountry(String countryIso) {
        synchronized (this) {
            if (countryIso.equals(this.lastCountryIso)) {
                List<TimeZone> list = this.lastCountryTimeZones;
                return list;
            }
        }
        synchronized (this) {
            this.lastCountryIso = countryIso;
            this.lastCountryTimeZones = countryTimeZones;
        }
        return countryTimeZones;
    }

    private void processXml(CountryZonesProcessor processor) throws XmlPullParserException, IOException {
        Throwable th;
        Throwable th2 = null;
        Reader reader = null;
        try {
            reader = this.xmlSource.get();
            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            xmlPullParserFactory.setNamespaceAware(false);
            XmlPullParser parser = xmlPullParserFactory.newPullParser();
            parser.setInput(reader);
            findRequiredStartTag(parser, TIMEZONES_ELEMENT);
            findRequiredStartTag(parser, COUNTRY_ZONES_ELEMENT);
            if (processCountryZones(parser, processor)) {
                checkOnEndTag(parser, COUNTRY_ZONES_ELEMENT);
                parser.next();
                consumeUntilEndTag(parser, TIMEZONES_ELEMENT);
                checkOnEndTag(parser, TIMEZONES_ELEMENT);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    throw th2;
                }
                return;
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable th4) {
                    th2 = th4;
                }
            }
            if (th2 != null) {
                throw th2;
            }
            return;
        } catch (Throwable th22) {
            Throwable th5 = th22;
            th22 = th;
            th = th5;
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (Throwable th6) {
                if (th22 == null) {
                    th22 = th6;
                } else if (th22 != th6) {
                    th22.addSuppressed(th6);
                }
            }
        }
        if (th22 != null) {
            throw th22;
        }
        throw th;
    }

    private static boolean processCountryZones(XmlPullParser parser, CountryZonesProcessor processor) throws IOException, XmlPullParserException {
        while (findOptionalStartTag(parser, COUNTRY_ELEMENT)) {
            if (processor == null) {
                consumeUntilEndTag(parser, COUNTRY_ELEMENT);
            } else {
                String code = parser.getAttributeValue(null, COUNTRY_CODE_ATTRIBUTE);
                if (code == null || code.isEmpty()) {
                    throw new XmlPullParserException("Unable to find country code: " + parser.getPositionDescription());
                }
                if (!processor.process(code, parseZoneIds(parser), parser.getPositionDescription())) {
                    return false;
                }
            }
            checkOnEndTag(parser, COUNTRY_ELEMENT);
        }
        return true;
    }

    private static List<String> parseZoneIds(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<String> timeZones = new ArrayList();
        while (findOptionalStartTag(parser, ID_ELEMENT)) {
            String zoneIdString = consumeText(parser);
            checkOnEndTag(parser, ID_ELEMENT);
            timeZones.add(zoneIdString);
        }
        return Collections.unmodifiableList(timeZones);
    }

    private static void findRequiredStartTag(XmlPullParser parser, String elementName) throws IOException, XmlPullParserException {
        findStartTag(parser, elementName, true);
    }

    private static boolean findOptionalStartTag(XmlPullParser parser, String elementName) throws IOException, XmlPullParserException {
        return findStartTag(parser, elementName, false);
    }

    private static boolean findStartTag(XmlPullParser parser, String elementName, boolean elementRequired) throws IOException, XmlPullParserException {
        while (true) {
            int type = parser.next();
            if (type != 1) {
                switch (type) {
                    case 2:
                        String currentElementName = parser.getName();
                        if (!elementName.equals(currentElementName)) {
                            parser.next();
                            consumeUntilEndTag(parser, currentElementName);
                            break;
                        }
                        return true;
                    case 3:
                        if (!elementRequired) {
                            return false;
                        }
                        throw new XmlPullParserException("No child element found with name " + elementName);
                    default:
                        break;
                }
            }
            throw new XmlPullParserException("Unexpected end of document while looking for " + elementName);
        }
    }

    private static void consumeUntilEndTag(XmlPullParser parser, String elementName) throws IOException, XmlPullParserException {
        if (parser.getEventType() != 3 || !elementName.equals(parser.getName())) {
            int requiredDepth = parser.getDepth();
            if (parser.getEventType() == 2) {
                requiredDepth--;
            }
            while (parser.getEventType() != 1) {
                int type = parser.next();
                int currentDepth = parser.getDepth();
                if (currentDepth < requiredDepth) {
                    throw new XmlPullParserException("Unexpected depth while looking for end tag: " + parser.getPositionDescription());
                } else if (currentDepth == requiredDepth && type == 3) {
                    if (!elementName.equals(parser.getName())) {
                        throw new XmlPullParserException("Unexpected eng tag: " + parser.getPositionDescription());
                    }
                    return;
                }
            }
            throw new XmlPullParserException("Unexpected end of document");
        }
    }

    private static String consumeText(XmlPullParser parser) throws IOException, XmlPullParserException {
        int type = parser.next();
        if (type == 4) {
            String text = parser.getText();
            type = parser.next();
            if (type == 3) {
                return text;
            }
            throw new XmlPullParserException("Unexpected nested tag or end of document when expecting text: type=" + type + " at " + parser.getPositionDescription());
        }
        throw new XmlPullParserException("Text not found. Found type=" + type + " at " + parser.getPositionDescription());
    }

    private static void checkOnEndTag(XmlPullParser parser, String elementName) throws XmlPullParserException {
        boolean equals;
        if (parser.getEventType() == 3) {
            equals = parser.getName().equals(elementName);
        } else {
            equals = false;
        }
        if (!equals) {
            throw new XmlPullParserException("Unexpected tag encountered: " + parser.getPositionDescription());
        }
    }
}
