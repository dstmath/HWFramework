package android.icu.impl;

import android.icu.impl.locale.BaseLocale;
import android.icu.text.PluralRanges;
import android.icu.text.PluralRules;
import android.icu.text.PluralRules.Factory;
import android.icu.text.PluralRules.PluralType;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;

public class PluralRulesLoader extends Factory {
    private static final PluralRanges UNKNOWN_RANGE = null;
    public static final PluralRulesLoader loader = null;
    private static Map<String, PluralRanges> localeIdToPluralRanges;
    private Map<String, String> localeIdToCardinalRulesId;
    private Map<String, String> localeIdToOrdinalRulesId;
    private Map<String, ULocale> rulesIdToEquivalentULocale;
    private final Map<String, PluralRules> rulesIdToRules;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.PluralRulesLoader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.PluralRulesLoader.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.PluralRulesLoader.<clinit>():void");
    }

    private PluralRulesLoader() {
        this.rulesIdToRules = new HashMap();
    }

    public ULocale[] getAvailableULocales() {
        Set<String> keys = getLocaleIdToRulesIdMap(PluralType.CARDINAL).keySet();
        ULocale[] locales = new ULocale[keys.size()];
        int n = 0;
        for (String createCanonical : keys) {
            int n2 = n + 1;
            locales[n] = ULocale.createCanonical(createCanonical);
            n = n2;
        }
        return locales;
    }

    public ULocale getFunctionalEquivalent(ULocale locale, boolean[] isAvailable) {
        if (isAvailable != null && isAvailable.length > 0) {
            isAvailable[0] = getLocaleIdToRulesIdMap(PluralType.CARDINAL).containsKey(ULocale.canonicalize(locale.getBaseName()));
        }
        String rulesId = getRulesIdForLocale(locale, PluralType.CARDINAL);
        if (rulesId == null || rulesId.trim().length() == 0) {
            return ULocale.ROOT;
        }
        ULocale result = (ULocale) getRulesIdToEquivalentULocaleMap().get(rulesId);
        if (result == null) {
            return ULocale.ROOT;
        }
        return result;
    }

    private Map<String, String> getLocaleIdToRulesIdMap(PluralType type) {
        checkBuildRulesIdMaps();
        return type == PluralType.CARDINAL ? this.localeIdToCardinalRulesId : this.localeIdToOrdinalRulesId;
    }

    private Map<String, ULocale> getRulesIdToEquivalentULocaleMap() {
        checkBuildRulesIdMaps();
        return this.rulesIdToEquivalentULocale;
    }

    private void checkBuildRulesIdMaps() {
        synchronized (this) {
            boolean haveMap = this.localeIdToCardinalRulesId != null;
        }
        if (!haveMap) {
            Map<String, String> tempLocaleIdToCardinalRulesId;
            Map<String, ULocale> tempRulesIdToEquivalentULocale;
            Map<String, String> tempLocaleIdToOrdinalRulesId;
            try {
                int i;
                UResourceBundle b;
                UResourceBundle pluralb = getPluralBundle();
                UResourceBundle localeb = pluralb.get("locales");
                tempLocaleIdToCardinalRulesId = new TreeMap();
                tempRulesIdToEquivalentULocale = new HashMap();
                for (i = 0; i < localeb.getSize(); i++) {
                    b = localeb.get(i);
                    String id = b.getKey();
                    String value = b.getString().intern();
                    tempLocaleIdToCardinalRulesId.put(id, value);
                    if (!tempRulesIdToEquivalentULocale.containsKey(value)) {
                        tempRulesIdToEquivalentULocale.put(value, new ULocale(id));
                    }
                }
                localeb = pluralb.get("locales_ordinals");
                tempLocaleIdToOrdinalRulesId = new TreeMap();
                for (i = 0; i < localeb.getSize(); i++) {
                    b = localeb.get(i);
                    tempLocaleIdToOrdinalRulesId.put(b.getKey(), b.getString().intern());
                }
            } catch (MissingResourceException e) {
                tempLocaleIdToCardinalRulesId = Collections.emptyMap();
                tempLocaleIdToOrdinalRulesId = Collections.emptyMap();
                tempRulesIdToEquivalentULocale = Collections.emptyMap();
            }
            synchronized (this) {
                if (this.localeIdToCardinalRulesId == null) {
                    this.localeIdToCardinalRulesId = tempLocaleIdToCardinalRulesId;
                    this.localeIdToOrdinalRulesId = tempLocaleIdToOrdinalRulesId;
                    this.rulesIdToEquivalentULocale = tempRulesIdToEquivalentULocale;
                }
            }
        }
    }

    public String getRulesIdForLocale(ULocale locale, PluralType type) {
        String rulesId;
        Map<String, String> idMap = getLocaleIdToRulesIdMap(type);
        String localeId = ULocale.canonicalize(locale.getBaseName());
        while (true) {
            rulesId = (String) idMap.get(localeId);
            if (rulesId != null) {
                break;
            }
            int ix = localeId.lastIndexOf(BaseLocale.SEP);
            if (ix == -1) {
                break;
            }
            localeId = localeId.substring(0, ix);
        }
        return rulesId;
    }

    public PluralRules getRulesForRulesId(String rulesId) {
        PluralRules pluralRules = null;
        synchronized (this.rulesIdToRules) {
            boolean hasRules = this.rulesIdToRules.containsKey(rulesId);
            if (hasRules) {
                pluralRules = (PluralRules) this.rulesIdToRules.get(rulesId);
            }
        }
        if (!hasRules) {
            try {
                UResourceBundle setb = getPluralBundle().get("rules").get(rulesId);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < setb.getSize(); i++) {
                    UResourceBundle b = setb.get(i);
                    if (i > 0) {
                        sb.append("; ");
                    }
                    sb.append(b.getKey());
                    sb.append(PluralRules.KEYWORD_RULE_SEPARATOR);
                    sb.append(b.getString());
                }
                pluralRules = PluralRules.parseDescription(sb.toString());
            } catch (ParseException e) {
            } catch (MissingResourceException e2) {
            }
            synchronized (this.rulesIdToRules) {
                if (this.rulesIdToRules.containsKey(rulesId)) {
                    pluralRules = (PluralRules) this.rulesIdToRules.get(rulesId);
                } else {
                    this.rulesIdToRules.put(rulesId, pluralRules);
                }
            }
        }
        return pluralRules;
    }

    public UResourceBundle getPluralBundle() throws MissingResourceException {
        return ICUResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "plurals", ICUResourceBundle.ICU_DATA_CLASS_LOADER, true);
    }

    public PluralRules forLocale(ULocale locale, PluralType type) {
        String rulesId = getRulesIdForLocale(locale, type);
        if (rulesId == null || rulesId.trim().length() == 0) {
            return PluralRules.DEFAULT;
        }
        PluralRules rules = getRulesForRulesId(rulesId);
        if (rules == null) {
            rules = PluralRules.DEFAULT;
        }
        return rules;
    }

    public boolean hasOverride(ULocale locale) {
        return false;
    }

    public PluralRanges getPluralRanges(ULocale locale) {
        String localeId = ULocale.canonicalize(locale.getBaseName());
        while (true) {
            PluralRanges result = (PluralRanges) localeIdToPluralRanges.get(localeId);
            if (result != null) {
                return result;
            }
            int ix = localeId.lastIndexOf(BaseLocale.SEP);
            if (ix == -1) {
                return UNKNOWN_RANGE;
            }
            localeId = localeId.substring(0, ix);
        }
    }

    public boolean isPluralRangesAvailable(ULocale locale) {
        return getPluralRanges(locale) == UNKNOWN_RANGE;
    }
}
