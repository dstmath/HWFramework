package ohos.global.icu.impl.number.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import ohos.dmsdp.sdk.DMSDPConfig;
import ohos.global.icu.impl.StandardPlural;
import ohos.global.icu.impl.StringSegment;
import ohos.global.icu.impl.number.AffixPatternProvider;
import ohos.global.icu.impl.number.AffixUtils;
import ohos.global.icu.impl.number.PatternStringUtils;
import ohos.global.icu.number.NumberFormatter;

public class AffixMatcher implements NumberParseMatcher {
    public static final Comparator<AffixMatcher> COMPARATOR = new Comparator<AffixMatcher>() {
        /* class ohos.global.icu.impl.number.parse.AffixMatcher.AnonymousClass1 */

        public int compare(AffixMatcher affixMatcher, AffixMatcher affixMatcher2) {
            if (AffixMatcher.length(affixMatcher.prefix) != AffixMatcher.length(affixMatcher2.prefix)) {
                return AffixMatcher.length(affixMatcher.prefix) > AffixMatcher.length(affixMatcher2.prefix) ? -1 : 1;
            }
            if (AffixMatcher.length(affixMatcher.suffix) != AffixMatcher.length(affixMatcher2.suffix)) {
                return AffixMatcher.length(affixMatcher.suffix) > AffixMatcher.length(affixMatcher2.suffix) ? -1 : 1;
            }
            if (!affixMatcher.equals(affixMatcher2)) {
                return affixMatcher.hashCode() > affixMatcher2.hashCode() ? -1 : 1;
            }
            return 0;
        }
    };
    private final int flags;
    private final AffixPatternMatcher prefix;
    private final AffixPatternMatcher suffix;

    private static boolean isInteresting(AffixPatternProvider affixPatternProvider, IgnorablesMatcher ignorablesMatcher, int i) {
        String str;
        String string = affixPatternProvider.getString(256);
        String string2 = affixPatternProvider.getString(0);
        String str2 = null;
        if (affixPatternProvider.hasNegativeSubpattern()) {
            str2 = affixPatternProvider.getString(768);
            str = affixPatternProvider.getString(512);
        } else {
            str = null;
        }
        if ((i & 256) != 0 || !AffixUtils.containsOnlySymbolsAndIgnorables(string, ignorablesMatcher.getSet()) || !AffixUtils.containsOnlySymbolsAndIgnorables(string2, ignorablesMatcher.getSet()) || !AffixUtils.containsOnlySymbolsAndIgnorables(str2, ignorablesMatcher.getSet()) || !AffixUtils.containsOnlySymbolsAndIgnorables(str, ignorablesMatcher.getSet()) || AffixUtils.containsType(string2, -2) || AffixUtils.containsType(string2, -1) || AffixUtils.containsType(str, -2) || AffixUtils.containsType(str, -1)) {
            return true;
        }
        return false;
    }

    public static void createMatchers(AffixPatternProvider affixPatternProvider, NumberParserImpl numberParserImpl, AffixTokenMatcherFactory affixTokenMatcherFactory, IgnorablesMatcher ignorablesMatcher, int i) {
        NumberFormatter.SignDisplay signDisplay;
        AffixPatternMatcher affixPatternMatcher;
        AffixPatternMatcher affixPatternMatcher2;
        AffixPatternMatcher affixPatternMatcher3;
        if (isInteresting(affixPatternProvider, ignorablesMatcher, i)) {
            StringBuilder sb = new StringBuilder();
            ArrayList arrayList = new ArrayList(6);
            boolean z = (i & 128) != 0;
            if ((i & 1024) != 0) {
                signDisplay = NumberFormatter.SignDisplay.ALWAYS;
            } else {
                signDisplay = NumberFormatter.SignDisplay.AUTO;
            }
            AffixPatternMatcher affixPatternMatcher4 = null;
            AffixPatternMatcher affixPatternMatcher5 = null;
            int i2 = 1;
            while (i2 >= -1) {
                AffixPatternMatcher affixPatternMatcher6 = affixPatternMatcher4;
                PatternStringUtils.patternInfoToStringBuilder(affixPatternProvider, true, i2, signDisplay, StandardPlural.OTHER, false, sb);
                AffixPatternMatcher fromAffixPattern = AffixPatternMatcher.fromAffixPattern(sb.toString(), affixTokenMatcherFactory, i);
                PatternStringUtils.patternInfoToStringBuilder(affixPatternProvider, false, i2, signDisplay, StandardPlural.OTHER, false, sb);
                AffixPatternMatcher fromAffixPattern2 = AffixPatternMatcher.fromAffixPattern(sb.toString(), affixTokenMatcherFactory, i);
                if (i2 == 1) {
                    affixPatternMatcher6 = fromAffixPattern2;
                    affixPatternMatcher3 = fromAffixPattern;
                    affixPatternMatcher = affixPatternMatcher3;
                } else {
                    affixPatternMatcher = affixPatternMatcher5;
                    affixPatternMatcher3 = fromAffixPattern;
                    if (Objects.equals(affixPatternMatcher3, affixPatternMatcher) && Objects.equals(fromAffixPattern2, affixPatternMatcher6)) {
                        affixPatternMatcher4 = affixPatternMatcher6;
                        affixPatternMatcher2 = null;
                        i2--;
                        affixPatternMatcher5 = affixPatternMatcher;
                    }
                }
                int i3 = i2 == -1 ? 1 : 0;
                arrayList.add(getInstance(affixPatternMatcher3, fromAffixPattern2, i3));
                if (!z || affixPatternMatcher3 == null || fromAffixPattern2 == null) {
                    affixPatternMatcher2 = null;
                } else {
                    if (i2 == 1 || !Objects.equals(affixPatternMatcher3, affixPatternMatcher)) {
                        affixPatternMatcher2 = null;
                        arrayList.add(getInstance(affixPatternMatcher3, null, i3));
                    } else {
                        affixPatternMatcher2 = null;
                    }
                    if (i2 == 1 || !Objects.equals(fromAffixPattern2, affixPatternMatcher6)) {
                        arrayList.add(getInstance(affixPatternMatcher2, fromAffixPattern2, i3));
                    }
                }
                affixPatternMatcher4 = affixPatternMatcher6;
                i2--;
                affixPatternMatcher5 = affixPatternMatcher;
            }
            Collections.sort(arrayList, COMPARATOR);
            numberParserImpl.addMatchers(arrayList);
        }
    }

    private static final AffixMatcher getInstance(AffixPatternMatcher affixPatternMatcher, AffixPatternMatcher affixPatternMatcher2, int i) {
        return new AffixMatcher(affixPatternMatcher, affixPatternMatcher2, i);
    }

    private AffixMatcher(AffixPatternMatcher affixPatternMatcher, AffixPatternMatcher affixPatternMatcher2, int i) {
        this.prefix = affixPatternMatcher;
        this.suffix = affixPatternMatcher2;
        this.flags = i;
    }

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public boolean match(StringSegment stringSegment, ParsedNumber parsedNumber) {
        boolean z = false;
        if (!parsedNumber.seenNumber()) {
            if (parsedNumber.prefix == null && this.prefix != null) {
                int offset = stringSegment.getOffset();
                z = this.prefix.match(stringSegment, parsedNumber);
                if (offset != stringSegment.getOffset()) {
                    parsedNumber.prefix = this.prefix.getPattern();
                }
            }
            return z;
        }
        if (parsedNumber.suffix == null && this.suffix != null && matched(this.prefix, parsedNumber.prefix)) {
            int offset2 = stringSegment.getOffset();
            z = this.suffix.match(stringSegment, parsedNumber);
            if (offset2 != stringSegment.getOffset()) {
                parsedNumber.suffix = this.suffix.getPattern();
            }
        }
        return z;
    }

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public boolean smokeTest(StringSegment stringSegment) {
        AffixPatternMatcher affixPatternMatcher;
        AffixPatternMatcher affixPatternMatcher2 = this.prefix;
        return (affixPatternMatcher2 != null && affixPatternMatcher2.smokeTest(stringSegment)) || ((affixPatternMatcher = this.suffix) != null && affixPatternMatcher.smokeTest(stringSegment));
    }

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public void postProcess(ParsedNumber parsedNumber) {
        if (matched(this.prefix, parsedNumber.prefix) && matched(this.suffix, parsedNumber.suffix)) {
            if (parsedNumber.prefix == null) {
                parsedNumber.prefix = "";
            }
            if (parsedNumber.suffix == null) {
                parsedNumber.suffix = "";
            }
            parsedNumber.flags |= this.flags;
            AffixPatternMatcher affixPatternMatcher = this.prefix;
            if (affixPatternMatcher != null) {
                affixPatternMatcher.postProcess(parsedNumber);
            }
            AffixPatternMatcher affixPatternMatcher2 = this.suffix;
            if (affixPatternMatcher2 != null) {
                affixPatternMatcher2.postProcess(parsedNumber);
            }
        }
    }

    static boolean matched(AffixPatternMatcher affixPatternMatcher, String str) {
        return (affixPatternMatcher == null && str == null) || (affixPatternMatcher != null && affixPatternMatcher.getPattern().equals(str));
    }

    /* access modifiers changed from: private */
    public static int length(AffixPatternMatcher affixPatternMatcher) {
        if (affixPatternMatcher == null) {
            return 0;
        }
        return affixPatternMatcher.getPattern().length();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof AffixMatcher)) {
            return false;
        }
        AffixMatcher affixMatcher = (AffixMatcher) obj;
        if (!Objects.equals(this.prefix, affixMatcher.prefix) || !Objects.equals(this.suffix, affixMatcher.suffix) || this.flags != affixMatcher.flags) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.flags ^ (Objects.hashCode(this.prefix) ^ Objects.hashCode(this.suffix));
    }

    public String toString() {
        boolean z = true;
        if ((this.flags & 1) == 0) {
            z = false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<AffixMatcher");
        sb.append(z ? ":negative " : " ");
        sb.append(this.prefix);
        sb.append(DMSDPConfig.SPLIT);
        sb.append(this.suffix);
        sb.append(">");
        return sb.toString();
    }
}
