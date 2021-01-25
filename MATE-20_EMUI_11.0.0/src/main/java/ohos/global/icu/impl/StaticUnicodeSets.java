package ohos.global.icu.impl;

import java.util.EnumMap;
import java.util.Map;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.impl.number.parse.ParsingUtils;
import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public class StaticUnicodeSets {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final Map<Key, UnicodeSet> unicodeSets = new EnumMap(Key.class);

    public enum Key {
        EMPTY,
        DEFAULT_IGNORABLES,
        STRICT_IGNORABLES,
        COMMA,
        PERIOD,
        STRICT_COMMA,
        STRICT_PERIOD,
        APOSTROPHE_SIGN,
        OTHER_GROUPING_SEPARATORS,
        ALL_SEPARATORS,
        STRICT_ALL_SEPARATORS,
        MINUS_SIGN,
        PLUS_SIGN,
        PERCENT_SIGN,
        PERMILLE_SIGN,
        INFINITY_SIGN,
        DOLLAR_SIGN,
        POUND_SIGN,
        RUPEE_SIGN,
        YEN_SIGN,
        WON_SIGN,
        DIGITS,
        DIGITS_OR_ALL_SEPARATORS,
        DIGITS_OR_STRICT_ALL_SEPARATORS
    }

    static {
        unicodeSets.put(Key.EMPTY, new UnicodeSet("[]").freeze());
        unicodeSets.put(Key.DEFAULT_IGNORABLES, new UnicodeSet("[[:Zs:][\\u0009][:Bidi_Control:][:Variation_Selector:]]").freeze());
        unicodeSets.put(Key.STRICT_IGNORABLES, new UnicodeSet("[[:Bidi_Control:]]").freeze());
        UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, ULocale.ROOT).getAllItemsWithFallback("parse", new ParseDataSink());
        UnicodeSet unicodeSet = new UnicodeSet("[٬‘\\u0020\\u00A0\\u2000-\\u200A\\u202F\\u205F\\u3000]");
        unicodeSet.addAll(unicodeSets.get(Key.APOSTROPHE_SIGN));
        unicodeSets.put(Key.OTHER_GROUPING_SEPARATORS, unicodeSet.freeze());
        unicodeSets.put(Key.ALL_SEPARATORS, computeUnion(Key.COMMA, Key.PERIOD, Key.OTHER_GROUPING_SEPARATORS));
        unicodeSets.put(Key.STRICT_ALL_SEPARATORS, computeUnion(Key.STRICT_COMMA, Key.STRICT_PERIOD, Key.OTHER_GROUPING_SEPARATORS));
        unicodeSets.put(Key.INFINITY_SIGN, new UnicodeSet("[∞]").freeze());
        unicodeSets.put(Key.DIGITS, new UnicodeSet("[:digit:]").freeze());
        unicodeSets.put(Key.DIGITS_OR_ALL_SEPARATORS, computeUnion(Key.DIGITS, Key.ALL_SEPARATORS));
        unicodeSets.put(Key.DIGITS_OR_STRICT_ALL_SEPARATORS, computeUnion(Key.DIGITS, Key.STRICT_ALL_SEPARATORS));
    }

    public static UnicodeSet get(Key key) {
        UnicodeSet unicodeSet = unicodeSets.get(key);
        return unicodeSet == null ? UnicodeSet.EMPTY : unicodeSet;
    }

    public static Key chooseFrom(String str, Key key) {
        if (ParsingUtils.safeContains(get(key), str)) {
            return key;
        }
        return null;
    }

    public static Key chooseFrom(String str, Key key, Key key2) {
        return ParsingUtils.safeContains(get(key), str) ? key : chooseFrom(str, key2);
    }

    public static Key chooseCurrency(String str) {
        if (get(Key.DOLLAR_SIGN).contains(str)) {
            return Key.DOLLAR_SIGN;
        }
        if (get(Key.POUND_SIGN).contains(str)) {
            return Key.POUND_SIGN;
        }
        if (get(Key.RUPEE_SIGN).contains(str)) {
            return Key.RUPEE_SIGN;
        }
        if (get(Key.YEN_SIGN).contains(str)) {
            return Key.YEN_SIGN;
        }
        if (get(Key.WON_SIGN).contains(str)) {
            return Key.WON_SIGN;
        }
        return null;
    }

    private static UnicodeSet computeUnion(Key key, Key key2) {
        return new UnicodeSet().addAll(get(key)).addAll(get(key2)).freeze();
    }

    private static UnicodeSet computeUnion(Key key, Key key2, Key key3) {
        return new UnicodeSet().addAll(get(key)).addAll(get(key2)).addAll(get(key3)).freeze();
    }

    /* access modifiers changed from: private */
    public static void saveSet(Key key, String str) {
        unicodeSets.put(key, new UnicodeSet(str).freeze());
    }

    static class ParseDataSink extends UResource.Sink {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        ParseDataSink() {
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                if (!key.contentEquals(SchemaSymbols.ATTVAL_DATE)) {
                    UResource.Table table2 = value.getTable();
                    for (int i2 = 0; table2.getKeyAndValue(i2, key, value); i2++) {
                        boolean contentEquals = key.contentEquals("lenient");
                        UResource.Array array = value.getArray();
                        for (int i3 = 0; i3 < array.getSize(); i3++) {
                            array.getValue(i3, value);
                            String value2 = value.toString();
                            if (value2.indexOf(46) != -1) {
                                StaticUnicodeSets.saveSet(contentEquals ? Key.PERIOD : Key.STRICT_PERIOD, value2);
                            } else if (value2.indexOf(44) != -1) {
                                StaticUnicodeSets.saveSet(contentEquals ? Key.COMMA : Key.STRICT_COMMA, value2);
                            } else if (value2.indexOf(43) != -1) {
                                StaticUnicodeSets.saveSet(Key.PLUS_SIGN, value2);
                            } else if (value2.indexOf(45) != -1) {
                                StaticUnicodeSets.saveSet(Key.MINUS_SIGN, value2);
                            } else if (value2.indexOf(36) != -1) {
                                StaticUnicodeSets.saveSet(Key.DOLLAR_SIGN, value2);
                            } else if (value2.indexOf(163) != -1) {
                                StaticUnicodeSets.saveSet(Key.POUND_SIGN, value2);
                            } else if (value2.indexOf(8377) != -1) {
                                StaticUnicodeSets.saveSet(Key.RUPEE_SIGN, value2);
                            } else if (value2.indexOf(165) != -1) {
                                StaticUnicodeSets.saveSet(Key.YEN_SIGN, value2);
                            } else if (value2.indexOf(8361) != -1) {
                                StaticUnicodeSets.saveSet(Key.WON_SIGN, value2);
                            } else if (value2.indexOf(37) != -1) {
                                StaticUnicodeSets.saveSet(Key.PERCENT_SIGN, value2);
                            } else if (value2.indexOf(8240) != -1) {
                                StaticUnicodeSets.saveSet(Key.PERMILLE_SIGN, value2);
                            } else if (value2.indexOf(8217) != -1) {
                                StaticUnicodeSets.saveSet(Key.APOSTROPHE_SIGN, value2);
                            } else {
                                throw new AssertionError("Unknown class of parse lenients: " + value2);
                            }
                        }
                    }
                    continue;
                }
            }
        }
    }
}
