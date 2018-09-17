package android.icu.text;

import android.icu.impl.UCaseProps;
import android.icu.lang.UCharacter;
import android.icu.text.Transliterator.Factory;
import android.icu.text.Transliterator.Position;

class CaseFoldTransliterator extends Transliterator {
    static final String _ID = "Any-CaseFold";
    static SourceTargetUtility sourceTargetUtility = null;
    private final UCaseProps csp = UCaseProps.INSTANCE;
    private ReplaceableContextIterator iter = new ReplaceableContextIterator();
    private StringBuilder result = new StringBuilder();

    static void register() {
        Transliterator.registerFactory(_ID, new Factory() {
            public Transliterator getInstance(String ID) {
                return new CaseFoldTransliterator();
            }
        });
        Transliterator.registerSpecialInverse("CaseFold", "Upper", false);
    }

    public CaseFoldTransliterator() {
        super(_ID, null);
    }

    protected synchronized void handleTransliterate(Replaceable text, Position offsets, boolean isIncremental) {
        if (this.csp != null) {
            if (offsets.start < offsets.limit) {
                this.iter.setText(text);
                this.result.setLength(0);
                this.iter.setIndex(offsets.start);
                this.iter.setLimit(offsets.limit);
                this.iter.setContextLimits(offsets.contextStart, offsets.contextLimit);
                while (true) {
                    int c = this.iter.nextCaseMapCP();
                    if (c >= 0) {
                        c = this.csp.toFullFolding(c, this.result, 0);
                        if (this.iter.didReachLimit() && isIncremental) {
                            offsets.start = this.iter.getCaseMapCPStart();
                            return;
                        } else if (c >= 0) {
                            int delta;
                            if (c <= 31) {
                                delta = this.iter.replace(this.result.toString());
                                this.result.setLength(0);
                            } else {
                                delta = this.iter.replace(UTF16.valueOf(c));
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
        synchronized (UppercaseTransliterator.class) {
            if (sourceTargetUtility == null) {
                sourceTargetUtility = new SourceTargetUtility(new Transform<String, String>() {
                    public String transform(String source) {
                        return UCharacter.foldCase(source, true);
                    }
                });
            }
        }
        sourceTargetUtility.addSourceTargetSet(this, inputFilter, sourceSet, targetSet);
    }
}
