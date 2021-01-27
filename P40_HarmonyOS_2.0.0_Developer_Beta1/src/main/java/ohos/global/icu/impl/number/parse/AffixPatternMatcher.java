package ohos.global.icu.impl.number.parse;

import ohos.global.icu.impl.number.AffixUtils;

public class AffixPatternMatcher extends SeriesMatcher implements AffixUtils.TokenConsumer {
    private final String affixPattern;
    private AffixTokenMatcherFactory factory;
    private IgnorablesMatcher ignorables;
    private int lastTypeOrCp;

    private AffixPatternMatcher(String str) {
        this.affixPattern = str;
    }

    public static AffixPatternMatcher fromAffixPattern(String str, AffixTokenMatcherFactory affixTokenMatcherFactory, int i) {
        IgnorablesMatcher ignorablesMatcher;
        if (str.isEmpty()) {
            return null;
        }
        AffixPatternMatcher affixPatternMatcher = new AffixPatternMatcher(str);
        affixPatternMatcher.factory = affixTokenMatcherFactory;
        if ((i & 512) != 0) {
            ignorablesMatcher = null;
        } else {
            ignorablesMatcher = affixTokenMatcherFactory.ignorables();
        }
        affixPatternMatcher.ignorables = ignorablesMatcher;
        affixPatternMatcher.lastTypeOrCp = 0;
        AffixUtils.iterateWithConsumer(str, affixPatternMatcher);
        affixPatternMatcher.factory = null;
        affixPatternMatcher.ignorables = null;
        affixPatternMatcher.lastTypeOrCp = 0;
        affixPatternMatcher.freeze();
        return affixPatternMatcher;
    }

    @Override // ohos.global.icu.impl.number.AffixUtils.TokenConsumer
    public void consumeToken(int i) {
        if (this.ignorables != null && length() > 0 && (this.lastTypeOrCp < 0 || !this.ignorables.getSet().contains(this.lastTypeOrCp))) {
            addMatcher(this.ignorables);
        }
        if (i < 0) {
            switch (i) {
                case -9:
                case -8:
                case -7:
                case -6:
                case -5:
                    addMatcher(this.factory.currency());
                    break;
                case -4:
                    addMatcher(this.factory.permille());
                    break;
                case -3:
                    addMatcher(this.factory.percent());
                    break;
                case -2:
                    addMatcher(this.factory.plusSign());
                    break;
                case -1:
                    addMatcher(this.factory.minusSign());
                    break;
                default:
                    throw new AssertionError();
            }
        } else {
            IgnorablesMatcher ignorablesMatcher = this.ignorables;
            if (ignorablesMatcher == null || !ignorablesMatcher.getSet().contains(i)) {
                addMatcher(CodePointMatcher.getInstance(i));
            }
        }
        this.lastTypeOrCp = i;
    }

    public String getPattern() {
        return this.affixPattern;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AffixPatternMatcher)) {
            return false;
        }
        return this.affixPattern.equals(((AffixPatternMatcher) obj).affixPattern);
    }

    public int hashCode() {
        return this.affixPattern.hashCode();
    }

    @Override // ohos.global.icu.impl.number.parse.SeriesMatcher
    public String toString() {
        return this.affixPattern;
    }
}
