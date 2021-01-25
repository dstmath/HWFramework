package android.support.v4.text.util;

import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
class FindAddress {
    private static final String HOUSE_COMPONENT = "(?:one|\\d+([a-z](?=[^a-z]|$)|st|nd|rd|th)?)";
    private static final String HOUSE_END = "(?=[,\"'\t                　\n\u000b\f\r  ]|$)";
    private static final String HOUSE_POST_DELIM = ",\"'\t                　\n\u000b\f\r  ";
    private static final String HOUSE_PRE_DELIM = ":,\"'\t                　\n\u000b\f\r  ";
    private static final int MAX_ADDRESS_LINES = 5;
    private static final int MAX_ADDRESS_WORDS = 14;
    private static final int MAX_LOCATION_NAME_DISTANCE = 5;
    private static final int MIN_ADDRESS_WORDS = 4;
    private static final String NL = "\n\u000b\f\r  ";
    private static final String SP = "\t                　";
    private static final String WORD_DELIM = ",*•\t                　\n\u000b\f\r  ";
    private static final String WORD_END = "(?=[,*•\t                　\n\u000b\f\r  ]|$)";
    private static final String WS = "\t                　\n\u000b\f\r  ";
    private static final int kMaxAddressNameWordLength = 25;
    private static final Pattern sHouseNumberRe = Pattern.compile("(?:one|\\d+([a-z](?=[^a-z]|$)|st|nd|rd|th)?)(?:-(?:one|\\d+([a-z](?=[^a-z]|$)|st|nd|rd|th)?))*(?=[,\"'\t                　\n\u000b\f\r  ]|$)", 2);
    private static final Pattern sLocationNameRe = Pattern.compile("(?:alley|annex|arcade|ave[.]?|avenue|alameda|bayou|beach|bend|bluffs?|bottom|boulevard|branch|bridge|brooks?|burgs?|bypass|broadway|camino|camp|canyon|cape|causeway|centers?|circles?|cliffs?|club|common|corners?|course|courts?|coves?|creek|crescent|crest|crossing|crossroad|curve|circulo|dale|dam|divide|drives?|estates?|expressway|extensions?|falls?|ferry|fields?|flats?|fords?|forest|forges?|forks?|fort|freeway|gardens?|gateway|glens?|greens?|groves?|harbors?|haven|heights|highway|hills?|hollow|inlet|islands?|isle|junctions?|keys?|knolls?|lakes?|land|landing|lane|lights?|loaf|locks?|lodge|loop|mall|manors?|meadows?|mews|mills?|mission|motorway|mount|mountains?|neck|orchard|oval|overpass|parks?|parkways?|pass|passage|path|pike|pines?|plains?|plaza|points?|ports?|prairie|privada|radial|ramp|ranch|rapids?|rd[.]?|rest|ridges?|river|roads?|route|row|rue|run|shoals?|shores?|skyway|springs?|spurs?|squares?|station|stravenue|stream|st[.]?|streets?|summit|speedway|terrace|throughway|trace|track|trafficway|trail|tunnel|turnpike|underpass|unions?|valleys?|viaduct|views?|villages?|ville|vista|walks?|wall|ways?|wells?|xing|xrd)(?=[,*•\t                　\n\u000b\f\r  ]|$)", 2);
    private static final Pattern sStateRe = Pattern.compile("(?:(ak|alaska)|(al|alabama)|(ar|arkansas)|(as|american[\t                　]+samoa)|(az|arizona)|(ca|california)|(co|colorado)|(ct|connecticut)|(dc|district[\t                　]+of[\t                　]+columbia)|(de|delaware)|(fl|florida)|(fm|federated[\t                　]+states[\t                　]+of[\t                　]+micronesia)|(ga|georgia)|(gu|guam)|(hi|hawaii)|(ia|iowa)|(id|idaho)|(il|illinois)|(in|indiana)|(ks|kansas)|(ky|kentucky)|(la|louisiana)|(ma|massachusetts)|(md|maryland)|(me|maine)|(mh|marshall[\t                　]+islands)|(mi|michigan)|(mn|minnesota)|(mo|missouri)|(mp|northern[\t                　]+mariana[\t                　]+islands)|(ms|mississippi)|(mt|montana)|(nc|north[\t                　]+carolina)|(nd|north[\t                　]+dakota)|(ne|nebraska)|(nh|new[\t                　]+hampshire)|(nj|new[\t                　]+jersey)|(nm|new[\t                　]+mexico)|(nv|nevada)|(ny|new[\t                　]+york)|(oh|ohio)|(ok|oklahoma)|(or|oregon)|(pa|pennsylvania)|(pr|puerto[\t                　]+rico)|(pw|palau)|(ri|rhode[\t                　]+island)|(sc|south[\t                　]+carolina)|(sd|south[\t                　]+dakota)|(tn|tennessee)|(tx|texas)|(ut|utah)|(va|virginia)|(vi|virgin[\t                　]+islands)|(vt|vermont)|(wa|washington)|(wi|wisconsin)|(wv|west[\t                　]+virginia)|(wy|wyoming))(?=[,*•\t                　\n\u000b\f\r  ]|$)", 2);
    private static final ZipRange[] sStateZipCodeRanges = {new ZipRange(99, 99, -1, -1), new ZipRange(35, 36, -1, -1), new ZipRange(71, 72, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(85, 86, -1, -1), new ZipRange(90, 96, -1, -1), new ZipRange(80, 81, -1, -1), new ZipRange(6, 6, -1, -1), new ZipRange(20, 20, -1, -1), new ZipRange(19, 19, -1, -1), new ZipRange(32, 34, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(30, 31, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(50, 52, -1, -1), new ZipRange(83, 83, -1, -1), new ZipRange(60, 62, -1, -1), new ZipRange(46, 47, -1, -1), new ZipRange(66, 67, 73, -1), new ZipRange(40, 42, -1, -1), new ZipRange(70, 71, -1, -1), new ZipRange(1, 2, -1, -1), new ZipRange(20, 21, -1, -1), new ZipRange(3, 4, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(48, 49, -1, -1), new ZipRange(55, 56, -1, -1), new ZipRange(63, 65, -1, -1), new ZipRange(96, 96, -1, -1), new ZipRange(38, 39, -1, -1), new ZipRange(55, 56, -1, -1), new ZipRange(27, 28, -1, -1), new ZipRange(58, 58, -1, -1), new ZipRange(68, 69, -1, -1), new ZipRange(3, 4, -1, -1), new ZipRange(7, 8, -1, -1), new ZipRange(87, 88, 86, -1), new ZipRange(88, 89, 96, -1), new ZipRange(10, 14, 0, 6), new ZipRange(43, 45, -1, -1), new ZipRange(73, 74, -1, -1), new ZipRange(97, 97, -1, -1), new ZipRange(15, 19, -1, -1), new ZipRange(6, 6, 0, 9), new ZipRange(96, 96, -1, -1), new ZipRange(2, 2, -1, -1), new ZipRange(29, 29, -1, -1), new ZipRange(57, 57, -1, -1), new ZipRange(37, 38, -1, -1), new ZipRange(75, 79, 87, 88), new ZipRange(84, 84, -1, -1), new ZipRange(22, 24, 20, -1), new ZipRange(6, 9, -1, -1), new ZipRange(5, 5, -1, -1), new ZipRange(98, 99, -1, -1), new ZipRange(53, 54, -1, -1), new ZipRange(24, 26, -1, -1), new ZipRange(82, 83, -1, -1)};
    private static final Pattern sSuffixedNumberRe = Pattern.compile("(\\d+)(st|nd|rd|th)", 2);
    private static final Pattern sWordRe = Pattern.compile("[^,*•\t                　\n\u000b\f\r  ]+(?=[,*•\t                　\n\u000b\f\r  ]|$)", 2);
    private static final Pattern sZipCodeRe = Pattern.compile("(?:\\d{5}(?:-\\d{4})?)(?=[,*•\t                　\n\u000b\f\r  ]|$)", 2);

    /* access modifiers changed from: private */
    public static class ZipRange {
        int mException1;
        int mException2;
        int mHigh;
        int mLow;

        ZipRange(int low, int high, int exception1, int exception2) {
            this.mLow = low;
            this.mHigh = high;
            this.mException1 = exception1;
            this.mException2 = exception1;
        }

        /* access modifiers changed from: package-private */
        public boolean matches(String zipCode) {
            int prefix = Integer.parseInt(zipCode.substring(0, 2));
            if ((this.mLow <= prefix && prefix <= this.mHigh) || prefix == this.mException1 || prefix == this.mException2) {
                return true;
            }
            return false;
        }
    }

    private static boolean checkHouseNumber(String houseNumber) {
        int digitCount = 0;
        for (int i = 0; i < houseNumber.length(); i++) {
            if (Character.isDigit(houseNumber.charAt(i))) {
                digitCount++;
            }
        }
        if (digitCount > 5) {
            return false;
        }
        Matcher suffixMatcher = sSuffixedNumberRe.matcher(houseNumber);
        if (!suffixMatcher.find()) {
            return true;
        }
        int num = Integer.parseInt(suffixMatcher.group(1));
        if (num == 0) {
            return false;
        }
        String suffix = suffixMatcher.group(2).toLowerCase(Locale.getDefault());
        switch (num % 10) {
            case 1:
                return suffix.equals(num % 100 == 11 ? "th" : "st");
            case 2:
                return suffix.equals(num % 100 == 12 ? "th" : "nd");
            case 3:
                return suffix.equals(num % 100 == 13 ? "th" : "rd");
            default:
                return suffix.equals("th");
        }
    }

    @VisibleForTesting
    public static MatchResult matchHouseNumber(String content, int offset) {
        if (offset > 0 && HOUSE_PRE_DELIM.indexOf(content.charAt(offset - 1)) == -1) {
            return null;
        }
        Matcher matcher = sHouseNumberRe.matcher(content).region(offset, content.length());
        if (matcher.lookingAt()) {
            MatchResult matchResult = matcher.toMatchResult();
            if (checkHouseNumber(matchResult.group(0))) {
                return matchResult;
            }
        }
        return null;
    }

    @VisibleForTesting
    public static MatchResult matchState(String content, int offset) {
        if (offset > 0 && WORD_DELIM.indexOf(content.charAt(offset - 1)) == -1) {
            return null;
        }
        Matcher stateMatcher = sStateRe.matcher(content).region(offset, content.length());
        if (stateMatcher.lookingAt()) {
            return stateMatcher.toMatchResult();
        }
        return null;
    }

    private static boolean isValidZipCode(String zipCode, MatchResult stateMatch) {
        if (stateMatch == null) {
            return false;
        }
        int stateIndex = stateMatch.groupCount();
        while (true) {
            if (stateIndex <= 0) {
                break;
            }
            int stateIndex2 = stateIndex - 1;
            if (stateMatch.group(stateIndex) != null) {
                stateIndex = stateIndex2;
                break;
            }
            stateIndex = stateIndex2;
        }
        if (!sZipCodeRe.matcher(zipCode).matches() || !sStateZipCodeRanges[stateIndex].matches(zipCode)) {
            return false;
        }
        return true;
    }

    @VisibleForTesting
    public static boolean isValidZipCode(String zipCode, String state) {
        return isValidZipCode(zipCode, matchState(state, 0));
    }

    @VisibleForTesting
    public static boolean isValidZipCode(String zipCode) {
        return sZipCodeRe.matcher(zipCode).matches();
    }

    @VisibleForTesting
    public static boolean isValidLocationName(String location) {
        return sLocationNameRe.matcher(location).matches();
    }

    private static int attemptMatch(String content, MatchResult houseNumberMatch) {
        MatchResult stateMatch;
        int restartPos = -1;
        int nonZipMatch = -1;
        int it = houseNumberMatch.end();
        int numLines = 1;
        boolean consecutiveHouseNumbers = true;
        boolean foundLocationName = false;
        int wordCount = 1;
        String lastWord = "";
        Matcher matcher = sWordRe.matcher(content);
        while (true) {
            if (it < content.length()) {
                if (matcher.find(it)) {
                    if (matcher.end() - matcher.start() <= 25) {
                        while (it < matcher.start()) {
                            int it2 = it + 1;
                            if (NL.indexOf(content.charAt(it)) != -1) {
                                numLines++;
                            }
                            it = it2;
                        }
                        if (numLines > 5 || (wordCount = wordCount + 1) > 14) {
                            break;
                        }
                        if (matchHouseNumber(content, it) == null) {
                            consecutiveHouseNumbers = false;
                            if (!isValidLocationName(matcher.group(0))) {
                                if (wordCount != 5 || foundLocationName) {
                                    if (foundLocationName && wordCount > 4 && (stateMatch = matchState(content, it)) != null) {
                                        if (lastWord.equals("et") && stateMatch.group(0).equals("al")) {
                                            it = stateMatch.end();
                                            break;
                                        }
                                        Matcher zipMatcher = sWordRe.matcher(content);
                                        if (!zipMatcher.find(stateMatch.end())) {
                                            nonZipMatch = stateMatch.end();
                                        } else if (isValidZipCode(zipMatcher.group(0), stateMatch)) {
                                            return zipMatcher.end();
                                        }
                                    }
                                } else {
                                    it = matcher.end();
                                    break;
                                }
                            } else {
                                foundLocationName = true;
                            }
                        } else if (consecutiveHouseNumbers && numLines > 1) {
                            return -it;
                        } else {
                            if (restartPos == -1) {
                                restartPos = it;
                            }
                        }
                        lastWord = matcher.group(0);
                        it = matcher.end();
                    } else {
                        return -matcher.end();
                    }
                } else {
                    return -content.length();
                }
            } else {
                break;
            }
        }
        if (nonZipMatch > 0) {
            return nonZipMatch;
        }
        return -(restartPos > 0 ? restartPos : it);
    }

    static String findAddress(String content) {
        Matcher houseNumberMatcher = sHouseNumberRe.matcher(content);
        int start = 0;
        while (houseNumberMatcher.find(start)) {
            if (checkHouseNumber(houseNumberMatcher.group(0))) {
                int start2 = houseNumberMatcher.start();
                int end = attemptMatch(content, houseNumberMatcher);
                if (end > 0) {
                    return content.substring(start2, end);
                }
                start = -end;
            } else {
                start = houseNumberMatcher.end();
            }
        }
        return null;
    }

    private FindAddress() {
    }
}
