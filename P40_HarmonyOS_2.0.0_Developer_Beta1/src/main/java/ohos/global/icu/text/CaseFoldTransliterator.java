package ohos.global.icu.text;

import ohos.global.icu.impl.UCaseProps;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.Transliterator;

class CaseFoldTransliterator extends Transliterator {
    static final String _ID = "Any-CaseFold";
    static SourceTargetUtility sourceTargetUtility;
    private final UCaseProps csp = UCaseProps.INSTANCE;
    private ReplaceableContextIterator iter = new ReplaceableContextIterator();
    private StringBuilder result = new StringBuilder();

    static void register() {
        Transliterator.registerFactory(_ID, new Transliterator.Factory() {
            /* class ohos.global.icu.text.CaseFoldTransliterator.AnonymousClass1 */

            @Override // ohos.global.icu.text.Transliterator.Factory
            public Transliterator getInstance(String str) {
                return new CaseFoldTransliterator();
            }
        });
        Transliterator.registerSpecialInverse("CaseFold", "Upper", false);
    }

    public CaseFoldTransliterator() {
        super(_ID, null);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.text.Transliterator
    public synchronized void handleTransliterate(Replaceable replaceable, Transliterator.Position position, boolean z) {
        int i;
        if (this.csp != null) {
            if (position.start < position.limit) {
                this.iter.setText(replaceable);
                this.result.setLength(0);
                this.iter.setIndex(position.start);
                this.iter.setLimit(position.limit);
                this.iter.setContextLimits(position.contextStart, position.contextLimit);
                while (true) {
                    int nextCaseMapCP = this.iter.nextCaseMapCP();
                    if (nextCaseMapCP >= 0) {
                        int fullFolding = this.csp.toFullFolding(nextCaseMapCP, this.result, 0);
                        if (this.iter.didReachLimit() && z) {
                            position.start = this.iter.getCaseMapCPStart();
                            return;
                        } else if (fullFolding >= 0) {
                            if (fullFolding <= 31) {
                                i = this.iter.replace(this.result.toString());
                                this.result.setLength(0);
                            } else {
                                i = this.iter.replace(UTF16.valueOf(fullFolding));
                            }
                            if (i != 0) {
                                position.limit += i;
                                position.contextLimit += i;
                            }
                        }
                    } else {
                        position.start = position.limit;
                        return;
                    }
                }
            }
        }
    }

    @Override // ohos.global.icu.text.Transliterator
    public void addSourceTargetSet(UnicodeSet unicodeSet, UnicodeSet unicodeSet2, UnicodeSet unicodeSet3) {
        synchronized (UppercaseTransliterator.class) {
            if (sourceTargetUtility == null) {
                sourceTargetUtility = new SourceTargetUtility(new Transform<String, String>() {
                    /* class ohos.global.icu.text.CaseFoldTransliterator.AnonymousClass2 */

                    public String transform(String str) {
                        return UCharacter.foldCase(str, true);
                    }
                });
            }
        }
        sourceTargetUtility.addSourceTargetSet(this, unicodeSet, unicodeSet2, unicodeSet3);
    }
}
