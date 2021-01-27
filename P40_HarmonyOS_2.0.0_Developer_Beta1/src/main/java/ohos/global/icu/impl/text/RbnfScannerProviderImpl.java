package ohos.global.icu.impl.text;

import java.util.HashMap;
import java.util.Map;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.global.icu.impl.ICUDebug;
import ohos.global.icu.text.CollationElementIterator;
import ohos.global.icu.text.Collator;
import ohos.global.icu.text.RbnfLenientScanner;
import ohos.global.icu.text.RbnfLenientScannerProvider;
import ohos.global.icu.text.RuleBasedCollator;
import ohos.global.icu.util.ULocale;

@Deprecated
public class RbnfScannerProviderImpl implements RbnfLenientScannerProvider {
    private static final boolean DEBUG = ICUDebug.enabled("rbnf");
    private Map<String, RbnfLenientScanner> cache = new HashMap();

    @Deprecated
    public RbnfLenientScanner get(ULocale uLocale, String str) {
        String str2 = uLocale.toString() + PsuedoNames.PSEUDONAME_ROOT + str;
        synchronized (this.cache) {
            RbnfLenientScanner rbnfLenientScanner = this.cache.get(str2);
            if (rbnfLenientScanner != null) {
                return rbnfLenientScanner;
            }
            RbnfLenientScanner createScanner = createScanner(uLocale, str);
            synchronized (this.cache) {
                this.cache.put(str2, createScanner);
            }
            return createScanner;
        }
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public RbnfLenientScanner createScanner(ULocale uLocale, String str) {
        RuleBasedCollator ruleBasedCollator;
        try {
            ruleBasedCollator = (RuleBasedCollator) Collator.getInstance(uLocale.toLocale());
            if (str != null) {
                ruleBasedCollator = new RuleBasedCollator(ruleBasedCollator.getRules() + str);
            }
            ruleBasedCollator.setDecomposition(17);
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
                System.out.println("++++");
            }
            ruleBasedCollator = null;
        }
        return new RbnfLenientScannerImpl(ruleBasedCollator);
    }

    /* access modifiers changed from: private */
    public static class RbnfLenientScannerImpl implements RbnfLenientScanner {
        private final RuleBasedCollator collator;

        private RbnfLenientScannerImpl(RuleBasedCollator ruleBasedCollator) {
            this.collator = ruleBasedCollator;
        }

        public boolean allIgnorable(String str) {
            CollationElementIterator collationElementIterator = this.collator.getCollationElementIterator(str);
            int next = collationElementIterator.next();
            while (next != -1 && CollationElementIterator.primaryOrder(next) == 0) {
                next = collationElementIterator.next();
            }
            return next == -1;
        }

        public int[] findText(String str, String str2, int i) {
            int i2 = 0;
            while (i < str.length() && i2 == 0) {
                i2 = prefixLength(str.substring(i), str2);
                if (i2 != 0) {
                    return new int[]{i, i2};
                }
                i++;
            }
            return new int[]{-1, 0};
        }

        public int[] findText2(String str, String str2, int i) {
            int i2;
            CollationElementIterator collationElementIterator = this.collator.getCollationElementIterator(str);
            CollationElementIterator collationElementIterator2 = this.collator.getCollationElementIterator(str2);
            collationElementIterator.setOffset(i);
            int next = collationElementIterator.next();
            int next2 = collationElementIterator2.next();
            loop0:
            while (true) {
                i2 = -1;
                while (next2 != -1) {
                    while (next != -1 && CollationElementIterator.primaryOrder(next) == 0) {
                        next = collationElementIterator.next();
                    }
                    while (next2 != -1 && CollationElementIterator.primaryOrder(next2) == 0) {
                        next2 = collationElementIterator2.next();
                    }
                    if (next == -1) {
                        return new int[]{-1, 0};
                    }
                    if (next2 == -1) {
                        break loop0;
                    } else if (CollationElementIterator.primaryOrder(next) == CollationElementIterator.primaryOrder(next2)) {
                        i2 = collationElementIterator.getOffset();
                        next = collationElementIterator.next();
                        next2 = collationElementIterator2.next();
                    } else if (i2 != -1) {
                        collationElementIterator2.reset();
                    } else {
                        next = collationElementIterator.next();
                    }
                }
                break loop0;
            }
            return new int[]{i2, collationElementIterator.getOffset() - i2};
        }

        public int prefixLength(String str, String str2) {
            CollationElementIterator collationElementIterator = this.collator.getCollationElementIterator(str);
            CollationElementIterator collationElementIterator2 = this.collator.getCollationElementIterator(str2);
            int next = collationElementIterator.next();
            int next2 = collationElementIterator2.next();
            while (next2 != -1) {
                while (CollationElementIterator.primaryOrder(next) == 0 && next != -1) {
                    next = collationElementIterator.next();
                }
                while (CollationElementIterator.primaryOrder(next2) == 0 && next2 != -1) {
                    next2 = collationElementIterator2.next();
                }
                if (next2 == -1) {
                    break;
                } else if (next == -1 || CollationElementIterator.primaryOrder(next) != CollationElementIterator.primaryOrder(next2)) {
                    return 0;
                } else {
                    next = collationElementIterator.next();
                    next2 = collationElementIterator2.next();
                }
            }
            int offset = collationElementIterator.getOffset();
            return next != -1 ? offset - 1 : offset;
        }
    }
}
