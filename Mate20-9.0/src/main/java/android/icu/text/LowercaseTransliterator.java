package android.icu.text;

import android.icu.impl.UCaseProps;
import android.icu.lang.UCharacter;
import android.icu.text.Transliterator;
import android.icu.util.ULocale;

class LowercaseTransliterator extends Transliterator {
    static final String _ID = "Any-Lower";
    private int caseLocale;
    private final UCaseProps csp;
    private ReplaceableContextIterator iter;
    /* access modifiers changed from: private */
    public final ULocale locale;
    private StringBuilder result;
    SourceTargetUtility sourceTargetUtility = null;

    static void register() {
        Transliterator.registerFactory(_ID, new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new LowercaseTransliterator(ULocale.US);
            }
        });
        Transliterator.registerSpecialInverse("Lower", "Upper", true);
    }

    public LowercaseTransliterator(ULocale loc) {
        super(_ID, null);
        this.locale = loc;
        this.csp = UCaseProps.INSTANCE;
        this.iter = new ReplaceableContextIterator();
        this.result = new StringBuilder();
        this.caseLocale = UCaseProps.getCaseLocale(this.locale);
    }

    /* access modifiers changed from: protected */
    public synchronized void handleTransliterate(Replaceable text, Transliterator.Position offsets, boolean isIncremental) {
        int delta;
        if (this.csp != null) {
            if (offsets.start < offsets.limit) {
                this.iter.setText(text);
                this.result.setLength(0);
                this.iter.setIndex(offsets.start);
                this.iter.setLimit(offsets.limit);
                this.iter.setContextLimits(offsets.contextStart, offsets.contextLimit);
                while (true) {
                    int nextCaseMapCP = this.iter.nextCaseMapCP();
                    int c = nextCaseMapCP;
                    if (nextCaseMapCP >= 0) {
                        int c2 = this.csp.toFullLower(c, this.iter, this.result, this.caseLocale);
                        if (this.iter.didReachLimit() && isIncremental) {
                            offsets.start = this.iter.getCaseMapCPStart();
                            return;
                        } else if (c2 >= 0) {
                            if (c2 <= 31) {
                                delta = this.iter.replace(this.result.toString());
                                this.result.setLength(0);
                            } else {
                                delta = this.iter.replace(UTF16.valueOf(c2));
                            }
                            if (delta != 0) {
                                offsets.limit += delta;
                                offsets.contextLimit += delta;
                            }
                        }
                    } else {
                        offsets.start = offsets.limit;
                        return;
                    }
                }
            }
        }
    }

    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        synchronized (this) {
            if (this.sourceTargetUtility == null) {
                this.sourceTargetUtility = new SourceTargetUtility(new Transform<String, String>() {
                    public String transform(String source) {
                        return UCharacter.toLowerCase(LowercaseTransliterator.this.locale, source);
                    }
                });
            }
        }
        this.sourceTargetUtility.addSourceTargetSet(this, inputFilter, sourceSet, targetSet);
    }
}
