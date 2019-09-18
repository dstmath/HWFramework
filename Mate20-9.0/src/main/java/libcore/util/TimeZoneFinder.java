package libcore.util;

import android.icu.util.TimeZone;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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
import java.util.Locale;
import java.util.Set;
import libcore.util.CountryTimeZones;
import libcore.util.TimeZoneFinder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public final class TimeZoneFinder {
    private static final String COUNTRY_CODE_ATTRIBUTE = "code";
    private static final String COUNTRY_ELEMENT = "country";
    private static final String COUNTRY_ZONES_ELEMENT = "countryzones";
    private static final String DEFAULT_TIME_ZONE_ID_ATTRIBUTE = "default";
    private static final String EVER_USES_UTC_ATTRIBUTE = "everutc";
    private static final String FALSE_ATTRIBUTE_VALUE = "n";
    private static final String IANA_VERSION_ATTRIBUTE = "ianaversion";
    private static final String TIMEZONES_ELEMENT = "timezones";
    private static final String TRUE_ATTRIBUTE_VALUE = "y";
    private static final String TZLOOKUP_FILE_NAME = "tzlookup.xml";
    private static final String ZONE_ID_ELEMENT = "id";
    private static final String ZONE_NOT_USED_AFTER_ATTRIBUTE = "notafter";
    private static final String ZONE_SHOW_IN_PICKER_ATTRIBUTE = "picker";
    private static TimeZoneFinder instance;
    private CountryTimeZones lastCountryTimeZones;
    private final ReaderSupplier xmlSource;

    private static class CountryZonesLookupExtractor implements TimeZonesProcessor {
        private List<CountryTimeZones> countryTimeZonesList;

        private CountryZonesLookupExtractor() {
            this.countryTimeZonesList = new ArrayList(250);
        }

        public boolean processCountryZones(String countryIso, String defaultTimeZoneId, boolean everUsesUtc, List<CountryTimeZones.TimeZoneMapping> timeZoneMappings, String debugInfo) throws XmlPullParserException {
            this.countryTimeZonesList.add(CountryTimeZones.createValidated(countryIso, defaultTimeZoneId, everUsesUtc, timeZoneMappings, debugInfo));
            return true;
        }

        /* access modifiers changed from: package-private */
        public CountryZonesFinder getCountryZonesLookup() {
            return new CountryZonesFinder(this.countryTimeZonesList);
        }
    }

    private static class IanaVersionExtractor implements TimeZonesProcessor {
        private String ianaVersion;

        private IanaVersionExtractor() {
        }

        public boolean processHeader(String ianaVersion2) throws XmlPullParserException {
            this.ianaVersion = ianaVersion2;
            return false;
        }

        public String getIanaVersion() {
            return this.ianaVersion;
        }
    }

    private interface ReaderSupplier {
        Reader get() throws IOException;

        static ReaderSupplier forFile(String fileName, Charset charSet) throws IOException {
            Path file = Paths.get(fileName, new String[0]);
            if (!Files.exists(file, new LinkOption[0])) {
                throw new FileNotFoundException(fileName + " does not exist");
            } else if (Files.isRegularFile(file, new LinkOption[0]) || !Files.isReadable(file)) {
                return new ReaderSupplier(file, charSet) {
                    private final /* synthetic */ Path f$0;
                    private final /* synthetic */ Charset f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    public final Reader get() {
                        return Files.newBufferedReader(this.f$0, this.f$1);
                    }
                };
            } else {
                throw new IOException(fileName + " must be a regular readable file.");
            }
        }

        static ReaderSupplier forString(String xml) {
            return new ReaderSupplier(xml) {
                private final /* synthetic */ String f$0;

                {
                    this.f$0 = r1;
                }

                public final Reader get() {
                    return TimeZoneFinder.ReaderSupplier.lambda$forString$1(this.f$0);
                }
            };
        }

        static /* synthetic */ Reader lambda$forString$1(String xml) throws IOException {
            return new StringReader(xml);
        }
    }

    private static class SelectiveCountryTimeZonesExtractor implements TimeZonesProcessor {
        private final String countryCodeToMatch;
        private CountryTimeZones validatedCountryTimeZones;

        private SelectiveCountryTimeZonesExtractor(String countryCodeToMatch2) {
            this.countryCodeToMatch = TimeZoneFinder.normalizeCountryIso(countryCodeToMatch2);
        }

        public boolean processCountryZones(String countryIso, String defaultTimeZoneId, boolean everUsesUtc, List<CountryTimeZones.TimeZoneMapping> timeZoneMappings, String debugInfo) {
            String countryIso2 = TimeZoneFinder.normalizeCountryIso(countryIso);
            if (!this.countryCodeToMatch.equals(countryIso2)) {
                return true;
            }
            this.validatedCountryTimeZones = CountryTimeZones.createValidated(countryIso2, defaultTimeZoneId, everUsesUtc, timeZoneMappings, debugInfo);
            return false;
        }

        /* access modifiers changed from: package-private */
        public CountryTimeZones getValidatedCountryTimeZones() {
            return this.validatedCountryTimeZones;
        }
    }

    private interface TimeZonesProcessor {
        public static final boolean CONTINUE = true;
        public static final boolean HALT = false;

        boolean processHeader(String ianaVersion) throws XmlPullParserException {
            return true;
        }

        boolean processCountryZones(String countryIso, String defaultTimeZoneId, boolean everUsesUtc, List<CountryTimeZones.TimeZoneMapping> list, String debugInfo) throws XmlPullParserException {
            return true;
        }
    }

    private static class TimeZonesValidator implements TimeZonesProcessor {
        private final Set<String> knownCountryCodes;

        private TimeZonesValidator() {
            this.knownCountryCodes = new HashSet();
        }

        public boolean processCountryZones(String countryIso, String defaultTimeZoneId, boolean everUsesUtc, List<CountryTimeZones.TimeZoneMapping> timeZoneMappings, String debugInfo) throws XmlPullParserException {
            if (!TimeZoneFinder.normalizeCountryIso(countryIso).equals(countryIso)) {
                throw new XmlPullParserException("Country code: " + countryIso + " is not normalized at " + debugInfo);
            } else if (this.knownCountryCodes.contains(countryIso)) {
                throw new XmlPullParserException("Second entry for country code: " + countryIso + " at " + debugInfo);
            } else if (timeZoneMappings.isEmpty()) {
                throw new XmlPullParserException("No time zone IDs for country code: " + countryIso + " at " + debugInfo);
            } else if (CountryTimeZones.TimeZoneMapping.containsTimeZoneId(timeZoneMappings, defaultTimeZoneId)) {
                this.knownCountryCodes.add(countryIso);
                return true;
            } else {
                throw new XmlPullParserException("defaultTimeZoneId for country code: " + countryIso + " is not one of the zones " + timeZoneMappings + " at " + debugInfo);
            }
        }
    }

    private TimeZoneFinder(ReaderSupplier xmlSource2) {
        this.xmlSource = xmlSource2;
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
        IOException lastException = null;
        int length = tzLookupFilePaths.length;
        int i = 0;
        while (i < length) {
            try {
                return createInstance(tzLookupFilePaths[i]);
            } catch (IOException e) {
                if (lastException != null) {
                    e.addSuppressed(lastException);
                }
                lastException = e;
                i++;
            }
        }
        System.logE("No valid file found in set: " + Arrays.toString(tzLookupFilePaths) + " Printing exceptions and falling back to empty map.", lastException);
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
            processXml(new TimeZonesValidator());
        } catch (XmlPullParserException e) {
            throw new IOException("Parsing error", e);
        }
    }

    public String getIanaVersion() {
        IanaVersionExtractor ianaVersionExtractor = new IanaVersionExtractor();
        try {
            processXml(ianaVersionExtractor);
            return ianaVersionExtractor.getIanaVersion();
        } catch (IOException | XmlPullParserException e) {
            return null;
        }
    }

    public CountryZonesFinder getCountryZonesFinder() {
        CountryZonesLookupExtractor extractor = new CountryZonesLookupExtractor();
        try {
            processXml(extractor);
            return extractor.getCountryZonesLookup();
        } catch (IOException | XmlPullParserException e) {
            System.logW("Error reading country zones ", e);
            return null;
        }
    }

    public TimeZone lookupTimeZoneByCountryAndOffset(String countryIso, int offsetMillis, boolean isDst, long whenMillis, TimeZone bias) {
        CountryTimeZones countryTimeZones = lookupCountryTimeZones(countryIso);
        TimeZone timeZone = null;
        if (countryTimeZones == null) {
            return null;
        }
        CountryTimeZones.OffsetResult offsetResult = countryTimeZones.lookupByOffsetWithBias(offsetMillis, isDst, whenMillis, bias);
        if (offsetResult != null) {
            timeZone = offsetResult.mTimeZone;
        }
        return timeZone;
    }

    public String lookupDefaultTimeZoneIdByCountry(String countryIso) {
        CountryTimeZones countryTimeZones = lookupCountryTimeZones(countryIso);
        if (countryTimeZones == null) {
            return null;
        }
        return countryTimeZones.getDefaultTimeZoneId();
    }

    public List<TimeZone> lookupTimeZonesByCountry(String countryIso) {
        CountryTimeZones countryTimeZones = lookupCountryTimeZones(countryIso);
        if (countryTimeZones == null) {
            return null;
        }
        return countryTimeZones.getIcuTimeZones();
    }

    public List<String> lookupTimeZoneIdsByCountry(String countryIso) {
        CountryTimeZones countryTimeZones = lookupCountryTimeZones(countryIso);
        if (countryTimeZones == null) {
            return null;
        }
        return extractTimeZoneIds(countryTimeZones.getTimeZoneMappings());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0012, code lost:
        r0 = new libcore.util.TimeZoneFinder.SelectiveCountryTimeZonesExtractor(r5, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        processXml(r0);
        r2 = r0.getValidatedCountryTimeZones();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001f, code lost:
        if (r2 != null) goto L_0x0022;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0021, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0022, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r4.lastCountryTimeZones = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0025, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0026, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x002a, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x002b, code lost:
        java.lang.System.logW("Error reading country zones ", r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0030, code lost:
        return null;
     */
    public CountryTimeZones lookupCountryTimeZones(String countryIso) {
        synchronized (this) {
            if (this.lastCountryTimeZones != null && this.lastCountryTimeZones.isForCountryCode(countryIso)) {
                CountryTimeZones countryTimeZones = this.lastCountryTimeZones;
                return countryTimeZones;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0056, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005a, code lost:
        if (r0 != null) goto L_0x005c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005c, code lost:
        if (r1 != null) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0062, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0063, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0067, code lost:
        r0.close();
     */
    private void processXml(TimeZonesProcessor processor) throws XmlPullParserException, IOException {
        Reader reader = this.xmlSource.get();
        XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
        xmlPullParserFactory.setNamespaceAware(false);
        XmlPullParser parser = xmlPullParserFactory.newPullParser();
        parser.setInput(reader);
        findRequiredStartTag(parser, TIMEZONES_ELEMENT);
        if (!processor.processHeader(parser.getAttributeValue(null, IANA_VERSION_ATTRIBUTE))) {
            if (reader != null) {
                reader.close();
            }
            return;
        }
        findRequiredStartTag(parser, COUNTRY_ZONES_ELEMENT);
        if (!processCountryZones(parser, processor)) {
            if (reader != null) {
                reader.close();
            }
            return;
        }
        checkOnEndTag(parser, COUNTRY_ZONES_ELEMENT);
        parser.next();
        consumeUntilEndTag(parser, TIMEZONES_ELEMENT);
        checkOnEndTag(parser, TIMEZONES_ELEMENT);
        if (reader != null) {
            reader.close();
        }
        return;
        throw th;
    }

    private static boolean processCountryZones(XmlPullParser parser, TimeZonesProcessor processor) throws IOException, XmlPullParserException {
        while (findOptionalStartTag(parser, COUNTRY_ELEMENT)) {
            if (processor == null) {
                consumeUntilEndTag(parser, COUNTRY_ELEMENT);
            } else {
                String code = parser.getAttributeValue(null, COUNTRY_CODE_ATTRIBUTE);
                if (code == null || code.isEmpty()) {
                    throw new XmlPullParserException("Unable to find country code: " + parser.getPositionDescription());
                }
                String defaultTimeZoneId = parser.getAttributeValue(null, DEFAULT_TIME_ZONE_ID_ATTRIBUTE);
                if (defaultTimeZoneId == null || defaultTimeZoneId.isEmpty()) {
                    throw new XmlPullParserException("Unable to find default time zone ID: " + parser.getPositionDescription());
                }
                Boolean everUsesUtc = parseBooleanAttribute(parser, EVER_USES_UTC_ATTRIBUTE, null);
                if (everUsesUtc != null) {
                    String debugInfo = parser.getPositionDescription();
                    if (!processor.processCountryZones(code, defaultTimeZoneId, everUsesUtc.booleanValue(), parseTimeZoneMappings(parser), debugInfo)) {
                        return false;
                    }
                } else {
                    throw new XmlPullParserException("Unable to find UTC hint attribute (everutc): " + parser.getPositionDescription());
                }
            }
            checkOnEndTag(parser, COUNTRY_ELEMENT);
        }
        return true;
    }

    private static List<CountryTimeZones.TimeZoneMapping> parseTimeZoneMappings(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<CountryTimeZones.TimeZoneMapping> timeZoneMappings = new ArrayList<>();
        while (findOptionalStartTag(parser, ZONE_ID_ELEMENT)) {
            boolean showInPicker = parseBooleanAttribute(parser, ZONE_SHOW_IN_PICKER_ATTRIBUTE, true).booleanValue();
            Long notUsedAfter = parseLongAttribute(parser, ZONE_NOT_USED_AFTER_ATTRIBUTE, null);
            String zoneIdString = consumeText(parser);
            checkOnEndTag(parser, ZONE_ID_ELEMENT);
            if (zoneIdString == null || zoneIdString.length() == 0) {
                throw new XmlPullParserException("Missing text for id): " + parser.getPositionDescription());
            }
            timeZoneMappings.add(new CountryTimeZones.TimeZoneMapping(zoneIdString, showInPicker, notUsedAfter));
        }
        return Collections.unmodifiableList(timeZoneMappings);
    }

    private static Long parseLongAttribute(XmlPullParser parser, String attributeName, Long defaultValue) throws XmlPullParserException {
        String attributeValueString = parser.getAttributeValue(null, attributeName);
        if (attributeValueString == null) {
            return defaultValue;
        }
        try {
            return Long.valueOf(Long.parseLong(attributeValueString));
        } catch (NumberFormatException e) {
            throw new XmlPullParserException("Attribute \"" + attributeName + "\" is not a long value: " + parser.getPositionDescription());
        }
    }

    private static Boolean parseBooleanAttribute(XmlPullParser parser, String attributeName, Boolean defaultValue) throws XmlPullParserException {
        String attributeValueString = parser.getAttributeValue(null, attributeName);
        if (attributeValueString == null) {
            return defaultValue;
        }
        boolean isTrue = "y".equals(attributeValueString);
        if (isTrue || FALSE_ATTRIBUTE_VALUE.equals(attributeValueString)) {
            return Boolean.valueOf(isTrue);
        }
        throw new XmlPullParserException("Attribute \"" + attributeName + "\" is not \"y\" or \"n\": " + parser.getPositionDescription());
    }

    private static void findRequiredStartTag(XmlPullParser parser, String elementName) throws IOException, XmlPullParserException {
        findStartTag(parser, elementName, true);
    }

    private static boolean findOptionalStartTag(XmlPullParser parser, String elementName) throws IOException, XmlPullParserException {
        return findStartTag(parser, elementName, false);
    }

    private static boolean findStartTag(XmlPullParser parser, String elementName, boolean elementRequired) throws IOException, XmlPullParserException {
        while (true) {
            int next = parser.next();
            int type = next;
            if (next != 1) {
                switch (type) {
                    case 2:
                        String currentElementName = parser.getName();
                        if (!elementName.equals(currentElementName)) {
                            parser.next();
                            consumeUntilEndTag(parser, currentElementName);
                            break;
                        } else {
                            return true;
                        }
                    case 3:
                        if (!elementRequired) {
                            return false;
                        }
                        throw new XmlPullParserException("No child element found with name " + elementName);
                }
            } else {
                throw new XmlPullParserException("Unexpected end of document while looking for " + elementName);
            }
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
            int type2 = parser.next();
            if (type2 == 3) {
                return text;
            }
            throw new XmlPullParserException("Unexpected nested tag or end of document when expecting text: type=" + type2 + " at " + parser.getPositionDescription());
        }
        throw new XmlPullParserException("Text not found. Found type=" + type + " at " + parser.getPositionDescription());
    }

    private static void checkOnEndTag(XmlPullParser parser, String elementName) throws XmlPullParserException {
        if (parser.getEventType() != 3 || !parser.getName().equals(elementName)) {
            throw new XmlPullParserException("Unexpected tag encountered: " + parser.getPositionDescription());
        }
    }

    private static List<String> extractTimeZoneIds(List<CountryTimeZones.TimeZoneMapping> timeZoneMappings) {
        List<String> zoneIds = new ArrayList<>(timeZoneMappings.size());
        for (CountryTimeZones.TimeZoneMapping timeZoneMapping : timeZoneMappings) {
            zoneIds.add(timeZoneMapping.timeZoneId);
        }
        return Collections.unmodifiableList(zoneIds);
    }

    static String normalizeCountryIso(String countryIso) {
        return countryIso.toLowerCase(Locale.US);
    }
}
