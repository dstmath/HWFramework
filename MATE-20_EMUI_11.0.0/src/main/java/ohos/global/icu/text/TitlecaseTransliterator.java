package ohos.global.icu.text;

import ohos.global.icu.impl.UCaseProps;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.Transliterator;
import ohos.global.icu.util.ULocale;

/* access modifiers changed from: package-private */
public class TitlecaseTransliterator extends Transliterator {
    static final String _ID = "Any-Title";
    private int caseLocale;
    private final UCaseProps csp;
    private ReplaceableContextIterator iter;
    private final ULocale locale;
    private StringBuilder result;
    SourceTargetUtility sourceTargetUtility = null;

    static void register() {
        Transliterator.registerFactory(_ID, new Transliterator.Factory() {
            /* class ohos.global.icu.text.TitlecaseTransliterator.AnonymousClass1 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new TitlecaseTransliterator(ULocale.US);
            }
        });
        registerSpecialInverse("Title", "Lower", false);
    }

    public TitlecaseTransliterator(ULocale uLocale) {
        super(_ID, null);
        this.locale = uLocale;
        setMaximumContextLength(2);
        this.csp = UCaseProps.INSTANCE;
        this.iter = new ReplaceableContextIterator();
        this.result = new StringBuilder();
        this.caseLocale = UCaseProps.getCaseLocale(this.locale);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0029, code lost:
        r0 = true;
     */
    @Override // ohos.global.icu.text.Transliterator
    public synchronized void handleTransliterate(Replaceable replaceable, Transliterator.Position position, boolean z) {
        boolean z2;
        int i;
        int i2;
        if (position.start < position.limit) {
            int i3 = position.start - 1;
            while (true) {
                if (i3 < position.contextStart) {
                    break;
                }
                int char32At = replaceable.char32At(i3);
                int typeOrIgnorable = this.csp.getTypeOrIgnorable(char32At);
                if (typeOrIgnorable > 0) {
                    z2 = false;
                    break;
                } else if (typeOrIgnorable == 0) {
                    break;
                } else {
                    i3 -= UTF16.getCharCount(char32At);
                }
            }
            this.iter.setText(replaceable);
            this.iter.setIndex(position.start);
            this.iter.setLimit(position.limit);
            this.iter.setContextLimits(position.contextStart, position.contextLimit);
            this.result.setLength(0);
            while (true) {
                int nextCaseMapCP = this.iter.nextCaseMapCP();
                if (nextCaseMapCP >= 0) {
                    int typeOrIgnorable2 = this.csp.getTypeOrIgnorable(nextCaseMapCP);
                    if (typeOrIgnorable2 >= 0) {
                        if (z2) {
                            i = this.csp.toFullTitle(nextCaseMapCP, this.iter, this.result, this.caseLocale);
                        } else {
                            i = this.csp.toFullLower(nextCaseMapCP, this.iter, this.result, this.caseLocale);
                        }
                        z2 = typeOrIgnorable2 == 0;
                        if (this.iter.didReachLimit() && z) {
                            position.start = this.iter.getCaseMapCPStart();
                            return;
                        } else if (i >= 0) {
                            if (i <= 31) {
                                i2 = this.iter.replace(this.result.toString());
                                this.result.setLength(0);
                            } else {
                                i2 = this.iter.replace(UTF16.valueOf(i));
                            }
                            if (i2 != 0) {
                                position.limit += i2;
                                position.contextLimit += i2;
                            }
                        }
                    }
                } else {
                    position.start = position.limit;
                    return;
                }
            }
        }
    }

    @Override // ohos.global.icu.text.Transliterator
    public void addSourceTargetSet(UnicodeSet unicodeSet, UnicodeSet unicodeSet2, UnicodeSet unicodeSet3) {
        synchronized (this) {
            if (this.sourceTargetUtility == null) {
                this.sourceTargetUtility = new SourceTargetUtility(new Transform<String, String>() {
                    /* class ohos.global.icu.text.TitlecaseTransliterator.AnonymousClass2 */

                    public String transform(String str) {
                        return UCharacter.toTitleCase(TitlecaseTransliterator.this.locale, str, (BreakIterator) null);
                    }
                });
            }
        }
        this.sourceTargetUtility.addSourceTargetSet(this, unicodeSet, unicodeSet2, unicodeSet3);
    }
}
