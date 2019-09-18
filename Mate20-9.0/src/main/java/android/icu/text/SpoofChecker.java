package android.icu.text;

import android.icu.impl.ICUBinary;
import android.icu.impl.Utility;
import android.icu.impl.number.Padder;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.lang.UScript;
import android.icu.util.ULocale;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpoofChecker {
    public static final int ALL_CHECKS = -1;
    @Deprecated
    public static final int ANY_CASE = 8;
    static final UnicodeSet ASCII = new UnicodeSet(0, 127).freeze();
    public static final int CHAR_LIMIT = 64;
    public static final int CONFUSABLE = 7;
    public static final UnicodeSet INCLUSION = new UnicodeSet("['\\-.\\:\\u00B7\\u0375\\u058A\\u05F3\\u05F4\\u06FD\\u06FE\\u0F0B\\u200C\\u200D\\u2010\\u2019\\u2027\\u30A0\\u30FB]").freeze();
    public static final int INVISIBLE = 32;
    public static final int MIXED_NUMBERS = 128;
    public static final int MIXED_SCRIPT_CONFUSABLE = 2;
    public static final UnicodeSet RECOMMENDED = new UnicodeSet("[0-9A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u0131\\u0134-\\u013E\\u0141-\\u0148\\u014A-\\u017E\\u018F\\u01A0\\u01A1\\u01AF\\u01B0\\u01CD-\\u01DC\\u01DE-\\u01E3\\u01E6-\\u01F0\\u01F4\\u01F5\\u01F8-\\u021B\\u021E\\u021F\\u0226-\\u0233\\u0259\\u02BB\\u02BC\\u02EC\\u0300-\\u0304\\u0306-\\u030C\\u030F-\\u0311\\u0313\\u0314\\u031B\\u0323-\\u0328\\u032D\\u032E\\u0330\\u0331\\u0335\\u0338\\u0339\\u0342\\u0345\\u037B-\\u037D\\u0386\\u0388-\\u038A\\u038C\\u038E-\\u03A1\\u03A3-\\u03CE\\u03FC-\\u045F\\u048A-\\u0529\\u052E\\u052F\\u0531-\\u0556\\u0559\\u0561-\\u0586\\u05B4\\u05D0-\\u05EA\\u05F0-\\u05F2\\u0620-\\u063F\\u0641-\\u0655\\u0660-\\u0669\\u0670-\\u0672\\u0674\\u0679-\\u068D\\u068F-\\u06D3\\u06D5\\u06E5\\u06E6\\u06EE-\\u06FC\\u06FF\\u0750-\\u07B1\\u08A0-\\u08AC\\u08B2\\u08B6-\\u08BD\\u0901-\\u094D\\u094F\\u0950\\u0956\\u0957\\u0960-\\u0963\\u0966-\\u096F\\u0971-\\u0977\\u0979-\\u097F\\u0981-\\u0983\\u0985-\\u098C\\u098F\\u0990\\u0993-\\u09A8\\u09AA-\\u09B0\\u09B2\\u09B6-\\u09B9\\u09BC-\\u09C4\\u09C7\\u09C8\\u09CB-\\u09CE\\u09D7\\u09E0-\\u09E3\\u09E6-\\u09F1\\u0A01-\\u0A03\\u0A05-\\u0A0A\\u0A0F\\u0A10\\u0A13-\\u0A28\\u0A2A-\\u0A30\\u0A32\\u0A35\\u0A38\\u0A39\\u0A3C\\u0A3E-\\u0A42\\u0A47\\u0A48\\u0A4B-\\u0A4D\\u0A5C\\u0A66-\\u0A74\\u0A81-\\u0A83\\u0A85-\\u0A8D\\u0A8F-\\u0A91\\u0A93-\\u0AA8\\u0AAA-\\u0AB0\\u0AB2\\u0AB3\\u0AB5-\\u0AB9\\u0ABC-\\u0AC5\\u0AC7-\\u0AC9\\u0ACB-\\u0ACD\\u0AD0\\u0AE0-\\u0AE3\\u0AE6-\\u0AEF\\u0B01-\\u0B03\\u0B05-\\u0B0C\\u0B0F\\u0B10\\u0B13-\\u0B28\\u0B2A-\\u0B30\\u0B32\\u0B33\\u0B35-\\u0B39\\u0B3C-\\u0B43\\u0B47\\u0B48\\u0B4B-\\u0B4D\\u0B56\\u0B57\\u0B5F-\\u0B61\\u0B66-\\u0B6F\\u0B71\\u0B82\\u0B83\\u0B85-\\u0B8A\\u0B8E-\\u0B90\\u0B92-\\u0B95\\u0B99\\u0B9A\\u0B9C\\u0B9E\\u0B9F\\u0BA3\\u0BA4\\u0BA8-\\u0BAA\\u0BAE-\\u0BB9\\u0BBE-\\u0BC2\\u0BC6-\\u0BC8\\u0BCA-\\u0BCD\\u0BD0\\u0BD7\\u0BE6-\\u0BEF\\u0C01-\\u0C03\\u0C05-\\u0C0C\\u0C0E-\\u0C10\\u0C12-\\u0C28\\u0C2A-\\u0C33\\u0C35-\\u0C39\\u0C3D-\\u0C44\\u0C46-\\u0C48\\u0C4A-\\u0C4D\\u0C55\\u0C56\\u0C60\\u0C61\\u0C66-\\u0C6F\\u0C80\\u0C82\\u0C83\\u0C85-\\u0C8C\\u0C8E-\\u0C90\\u0C92-\\u0CA8\\u0CAA-\\u0CB3\\u0CB5-\\u0CB9\\u0CBC-\\u0CC4\\u0CC6-\\u0CC8\\u0CCA-\\u0CCD\\u0CD5\\u0CD6\\u0CE0-\\u0CE3\\u0CE6-\\u0CEF\\u0CF1\\u0CF2\\u0D02\\u0D03\\u0D05-\\u0D0C\\u0D0E-\\u0D10\\u0D12-\\u0D3A\\u0D3D-\\u0D43\\u0D46-\\u0D48\\u0D4A-\\u0D4E\\u0D54-\\u0D57\\u0D60\\u0D61\\u0D66-\\u0D6F\\u0D7A-\\u0D7F\\u0D82\\u0D83\\u0D85-\\u0D8E\\u0D91-\\u0D96\\u0D9A-\\u0DA5\\u0DA7-\\u0DB1\\u0DB3-\\u0DBB\\u0DBD\\u0DC0-\\u0DC6\\u0DCA\\u0DCF-\\u0DD4\\u0DD6\\u0DD8-\\u0DDE\\u0DF2\\u0E01-\\u0E32\\u0E34-\\u0E3A\\u0E40-\\u0E4E\\u0E50-\\u0E59\\u0E81\\u0E82\\u0E84\\u0E87\\u0E88\\u0E8A\\u0E8D\\u0E94-\\u0E97\\u0E99-\\u0E9F\\u0EA1-\\u0EA3\\u0EA5\\u0EA7\\u0EAA\\u0EAB\\u0EAD-\\u0EB2\\u0EB4-\\u0EB9\\u0EBB-\\u0EBD\\u0EC0-\\u0EC4\\u0EC6\\u0EC8-\\u0ECD\\u0ED0-\\u0ED9\\u0EDE\\u0EDF\\u0F00\\u0F20-\\u0F29\\u0F35\\u0F37\\u0F3E-\\u0F42\\u0F44-\\u0F47\\u0F49-\\u0F4C\\u0F4E-\\u0F51\\u0F53-\\u0F56\\u0F58-\\u0F5B\\u0F5D-\\u0F68\\u0F6A-\\u0F6C\\u0F71\\u0F72\\u0F74\\u0F7A-\\u0F80\\u0F82-\\u0F84\\u0F86-\\u0F92\\u0F94-\\u0F97\\u0F99-\\u0F9C\\u0F9E-\\u0FA1\\u0FA3-\\u0FA6\\u0FA8-\\u0FAB\\u0FAD-\\u0FB8\\u0FBA-\\u0FBC\\u0FC6\\u1000-\\u1049\\u1050-\\u109D\\u10C7\\u10CD\\u10D0-\\u10F0\\u10F7-\\u10FA\\u10FD-\\u10FF\\u1200-\\u1248\\u124A-\\u124D\\u1250-\\u1256\\u1258\\u125A-\\u125D\\u1260-\\u1288\\u128A-\\u128D\\u1290-\\u12B0\\u12B2-\\u12B5\\u12B8-\\u12BE\\u12C0\\u12C2-\\u12C5\\u12C8-\\u12D6\\u12D8-\\u1310\\u1312-\\u1315\\u1318-\\u135A\\u135D-\\u135F\\u1380-\\u138F\\u1780-\\u17A2\\u17A5-\\u17A7\\u17A9-\\u17B3\\u17B6-\\u17CA\\u17D2\\u17D7\\u17DC\\u17E0-\\u17E9\\u1C80-\\u1C88\\u1E00-\\u1E99\\u1E9E\\u1EA0-\\u1EF9\\u1F00-\\u1F15\\u1F18-\\u1F1D\\u1F20-\\u1F45\\u1F48-\\u1F4D\\u1F50-\\u1F57\\u1F59\\u1F5B\\u1F5D\\u1F5F-\\u1F70\\u1F72\\u1F74\\u1F76\\u1F78\\u1F7A\\u1F7C\\u1F80-\\u1FB4\\u1FB6-\\u1FBA\\u1FBC\\u1FC2-\\u1FC4\\u1FC6-\\u1FC8\\u1FCA\\u1FCC\\u1FD0-\\u1FD2\\u1FD6-\\u1FDA\\u1FE0-\\u1FE2\\u1FE4-\\u1FEA\\u1FEC\\u1FF2-\\u1FF4\\u1FF6-\\u1FF8\\u1FFA\\u1FFC\\u2D27\\u2D2D\\u2D80-\\u2D96\\u2DA0-\\u2DA6\\u2DA8-\\u2DAE\\u2DB0-\\u2DB6\\u2DB8-\\u2DBE\\u2DC0-\\u2DC6\\u2DC8-\\u2DCE\\u2DD0-\\u2DD6\\u2DD8-\\u2DDE\\u3005-\\u3007\\u3041-\\u3096\\u3099\\u309A\\u309D\\u309E\\u30A1-\\u30FA\\u30FC-\\u30FE\\u3105-\\u312D\\u31A0-\\u31BA\\u3400-\\u4DB5\\u4E00-\\u9FD5\\uA660\\uA661\\uA674-\\uA67B\\uA67F\\uA69F\\uA717-\\uA71F\\uA788\\uA78D\\uA78E\\uA790-\\uA793\\uA7A0-\\uA7AA\\uA7AE\\uA7FA\\uA9E7-\\uA9FE\\uAA60-\\uAA76\\uAA7A-\\uAA7F\\uAB01-\\uAB06\\uAB09-\\uAB0E\\uAB11-\\uAB16\\uAB20-\\uAB26\\uAB28-\\uAB2E\\uAC00-\\uD7A3\\uFA0E\\uFA0F\\uFA11\\uFA13\\uFA14\\uFA1F\\uFA21\\uFA23\\uFA24\\uFA27-\\uFA29\\U00020000-\\U0002A6D6\\U0002A700-\\U0002B734\\U0002B740-\\U0002B81D\\U0002B820-\\U0002CEA1]").freeze();
    public static final int RESTRICTION_LEVEL = 16;
    @Deprecated
    public static final int SINGLE_SCRIPT = 16;
    public static final int SINGLE_SCRIPT_CONFUSABLE = 1;
    public static final int WHOLE_SCRIPT_CONFUSABLE = 4;
    private static Normalizer2 nfdNormalizer = Normalizer2.getNFDInstance();
    /* access modifiers changed from: private */
    public UnicodeSet fAllowedCharsSet;
    /* access modifiers changed from: private */
    public Set<ULocale> fAllowedLocales;
    /* access modifiers changed from: private */
    public int fChecks;
    /* access modifiers changed from: private */
    public RestrictionLevel fRestrictionLevel;
    /* access modifiers changed from: private */
    public SpoofData fSpoofData;

    public static class Builder {
        final UnicodeSet fAllowedCharsSet;
        final Set<ULocale> fAllowedLocales;
        int fChecks;
        private RestrictionLevel fRestrictionLevel;
        SpoofData fSpoofData;

        private static class ConfusabledataBuilder {
            static final /* synthetic */ boolean $assertionsDisabled = false;
            private UnicodeSet fKeySet = new UnicodeSet();
            private ArrayList<Integer> fKeyVec = new ArrayList<>();
            private int fLineNum;
            private Pattern fParseHexNum;
            private Pattern fParseLine;
            private StringBuffer fStringTable;
            private Hashtable<Integer, SPUString> fTable = new Hashtable<>();
            private ArrayList<Integer> fValueVec = new ArrayList<>();
            private SPUStringPool stringPool = new SPUStringPool();

            private static class SPUString {
                int fCharOrStrTableIndex = 0;
                String fStr;

                SPUString(String s) {
                    this.fStr = s;
                }
            }

            private static class SPUStringComparator implements Comparator<SPUString> {
                static final SPUStringComparator INSTANCE = new SPUStringComparator();

                private SPUStringComparator() {
                }

                public int compare(SPUString sL, SPUString sR) {
                    int lenL = sL.fStr.length();
                    int lenR = sR.fStr.length();
                    if (lenL < lenR) {
                        return -1;
                    }
                    if (lenL > lenR) {
                        return 1;
                    }
                    return sL.fStr.compareTo(sR.fStr);
                }
            }

            private static class SPUStringPool {
                private Hashtable<String, SPUString> fHash = new Hashtable<>();
                private Vector<SPUString> fVec = new Vector<>();

                public int size() {
                    return this.fVec.size();
                }

                public SPUString getByIndex(int index) {
                    return this.fVec.elementAt(index);
                }

                public SPUString addString(String src) {
                    SPUString hashedString = this.fHash.get(src);
                    if (hashedString != null) {
                        return hashedString;
                    }
                    SPUString hashedString2 = new SPUString(src);
                    this.fHash.put(src, hashedString2);
                    this.fVec.addElement(hashedString2);
                    return hashedString2;
                }

                public void sort() {
                    Collections.sort(this.fVec, SPUStringComparator.INSTANCE);
                }
            }

            static {
                Class<SpoofChecker> cls = SpoofChecker.class;
            }

            ConfusabledataBuilder() {
            }

            /* access modifiers changed from: package-private */
            public void build(Reader confusables, SpoofData dest) throws ParseException, IOException {
                SpoofData spoofData = dest;
                StringBuffer fInput = new StringBuffer();
                LineNumberReader lnr = new LineNumberReader(confusables);
                while (true) {
                    String line = lnr.readLine();
                    if (line == null) {
                        break;
                    }
                    fInput.append(line);
                    fInput.append(10);
                }
                this.fParseLine = Pattern.compile("(?m)^[ \\t]*([0-9A-Fa-f]+)[ \\t]+;[ \\t]*([0-9A-Fa-f]+(?:[ \\t]+[0-9A-Fa-f]+)*)[ \\t]*;\\s*(?:(SL)|(SA)|(ML)|(MA))[ \\t]*(?:#.*?)?$|^([ \\t]*(?:#.*?)?)$|^(.*?)$");
                this.fParseHexNum = Pattern.compile("\\s*([0-9A-F]+)");
                int i = 0;
                if (fInput.charAt(0) == 65279) {
                    fInput.setCharAt(0, ' ');
                }
                Matcher matcher = this.fParseLine.matcher(fInput);
                while (matcher.find()) {
                    this.fLineNum++;
                    if (matcher.start(7) < 0) {
                        if (matcher.start(8) < 0) {
                            int keyChar = Integer.parseInt(matcher.group(1), 16);
                            if (keyChar <= 1114111) {
                                Matcher m = this.fParseHexNum.matcher(matcher.group(2));
                                StringBuilder mapString = new StringBuilder();
                                while (m.find()) {
                                    int c = Integer.parseInt(m.group(1), 16);
                                    if (c <= 1114111) {
                                        mapString.appendCodePoint(c);
                                    } else {
                                        throw new ParseException("Confusables, line " + this.fLineNum + ": Bad code point: " + Integer.toString(c, 16), matcher.start(2));
                                    }
                                }
                                this.fTable.put(Integer.valueOf(keyChar), this.stringPool.addString(mapString.toString()));
                                this.fKeySet.add(keyChar);
                            } else {
                                throw new ParseException("Confusables, line " + this.fLineNum + ": Bad code point: " + matcher.group(1), matcher.start(1));
                            }
                        } else {
                            throw new ParseException("Confusables, line " + this.fLineNum + ": Unrecognized Line: " + matcher.group(8), matcher.start(8));
                        }
                    }
                }
                this.stringPool.sort();
                this.fStringTable = new StringBuffer();
                int poolSize = this.stringPool.size();
                for (int i2 = 0; i2 < poolSize; i2++) {
                    SPUString s = this.stringPool.getByIndex(i2);
                    int strLen = s.fStr.length();
                    int strIndex = this.fStringTable.length();
                    if (strLen == 1) {
                        s.fCharOrStrTableIndex = s.fStr.charAt(0);
                    } else {
                        s.fCharOrStrTableIndex = strIndex;
                        this.fStringTable.append(s.fStr);
                    }
                }
                Iterator<String> it = this.fKeySet.iterator();
                while (it.hasNext()) {
                    int keyChar2 = it.next().codePointAt(i);
                    SPUString targetMapping = this.fTable.get(Integer.valueOf(keyChar2));
                    if (targetMapping.fStr.length() <= 256) {
                        int key = ConfusableDataUtils.codePointAndLengthToKey(keyChar2, targetMapping.fStr.length());
                        int value = targetMapping.fCharOrStrTableIndex;
                        this.fKeyVec.add(Integer.valueOf(key));
                        this.fValueVec.add(Integer.valueOf(value));
                        i = 0;
                    } else {
                        throw new IllegalArgumentException("Confusable prototypes cannot be longer than 256 entries.");
                    }
                }
                int numKeys = this.fKeyVec.size();
                spoofData.fCFUKeys = new int[numKeys];
                for (int i3 = 0; i3 < numKeys; i3++) {
                    int key2 = this.fKeyVec.get(i3).intValue();
                    int codePoint = ConfusableDataUtils.keyToCodePoint(key2);
                    spoofData.fCFUKeys[i3] = key2;
                    int previousCodePoint = codePoint;
                }
                spoofData.fCFUValues = new short[this.fValueVec.size()];
                int i4 = 0;
                Iterator<Integer> it2 = this.fValueVec.iterator();
                while (it2.hasNext()) {
                    spoofData.fCFUValues[i4] = (short) it2.next().intValue();
                    i4++;
                }
                spoofData.fCFUStrings = this.fStringTable.toString();
            }

            public static void buildConfusableData(Reader confusables, SpoofData dest) throws IOException, ParseException {
                new ConfusabledataBuilder().build(confusables, dest);
            }
        }

        public Builder() {
            this.fAllowedCharsSet = new UnicodeSet(0, 1114111);
            this.fAllowedLocales = new LinkedHashSet();
            this.fChecks = -1;
            this.fSpoofData = null;
            this.fRestrictionLevel = RestrictionLevel.HIGHLY_RESTRICTIVE;
        }

        public Builder(SpoofChecker src) {
            this.fAllowedCharsSet = new UnicodeSet(0, 1114111);
            this.fAllowedLocales = new LinkedHashSet();
            this.fChecks = src.fChecks;
            this.fSpoofData = src.fSpoofData;
            this.fAllowedCharsSet.set(src.fAllowedCharsSet);
            this.fAllowedLocales.addAll(src.fAllowedLocales);
            this.fRestrictionLevel = src.fRestrictionLevel;
        }

        public SpoofChecker build() {
            if (this.fSpoofData == null) {
                this.fSpoofData = SpoofData.getDefault();
            }
            SpoofChecker result = new SpoofChecker();
            int unused = result.fChecks = this.fChecks;
            SpoofData unused2 = result.fSpoofData = this.fSpoofData;
            UnicodeSet unused3 = result.fAllowedCharsSet = (UnicodeSet) this.fAllowedCharsSet.clone();
            result.fAllowedCharsSet.freeze();
            Set unused4 = result.fAllowedLocales = new HashSet(this.fAllowedLocales);
            RestrictionLevel unused5 = result.fRestrictionLevel = this.fRestrictionLevel;
            return result;
        }

        public Builder setData(Reader confusables) throws ParseException, IOException {
            this.fSpoofData = new SpoofData();
            ConfusabledataBuilder.buildConfusableData(confusables, this.fSpoofData);
            return this;
        }

        @Deprecated
        public Builder setData(Reader confusables, Reader confusablesWholeScript) throws ParseException, IOException {
            setData(confusables);
            return this;
        }

        public Builder setChecks(int checks) {
            if ((checks & 0) == 0) {
                this.fChecks = checks & -1;
                return this;
            }
            throw new IllegalArgumentException("Bad Spoof Checks value.");
        }

        public Builder setAllowedLocales(Set<ULocale> locales) {
            this.fAllowedCharsSet.clear();
            for (ULocale locale : locales) {
                addScriptChars(locale, this.fAllowedCharsSet);
            }
            this.fAllowedLocales.clear();
            if (locales.size() == 0) {
                this.fAllowedCharsSet.add(0, 1114111);
                this.fChecks &= -65;
                return this;
            }
            UnicodeSet tempSet = new UnicodeSet();
            tempSet.applyIntPropertyValue(UProperty.SCRIPT, 0);
            this.fAllowedCharsSet.addAll(tempSet);
            tempSet.applyIntPropertyValue(UProperty.SCRIPT, 1);
            this.fAllowedCharsSet.addAll(tempSet);
            this.fAllowedLocales.clear();
            this.fAllowedLocales.addAll(locales);
            this.fChecks |= 64;
            return this;
        }

        public Builder setAllowedJavaLocales(Set<Locale> locales) {
            HashSet<ULocale> ulocales = new HashSet<>(locales.size());
            for (Locale locale : locales) {
                ulocales.add(ULocale.forLocale(locale));
            }
            return setAllowedLocales(ulocales);
        }

        private void addScriptChars(ULocale locale, UnicodeSet allowedChars) {
            int[] scripts = UScript.getCode(locale);
            if (scripts != null) {
                UnicodeSet tmpSet = new UnicodeSet();
                for (int applyIntPropertyValue : scripts) {
                    tmpSet.applyIntPropertyValue(UProperty.SCRIPT, applyIntPropertyValue);
                    allowedChars.addAll(tmpSet);
                }
            }
        }

        public Builder setAllowedChars(UnicodeSet chars) {
            this.fAllowedCharsSet.set(chars);
            this.fAllowedLocales.clear();
            this.fChecks |= 64;
            return this;
        }

        public Builder setRestrictionLevel(RestrictionLevel restrictionLevel) {
            this.fRestrictionLevel = restrictionLevel;
            this.fChecks |= 144;
            return this;
        }
    }

    public static class CheckResult {
        public int checks = 0;
        public UnicodeSet numerics;
        @Deprecated
        public int position = 0;
        public RestrictionLevel restrictionLevel;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("checks:");
            if (this.checks == 0) {
                sb.append(" none");
            } else if (this.checks == -1) {
                sb.append(" all");
            } else {
                if ((this.checks & 1) != 0) {
                    sb.append(" SINGLE_SCRIPT_CONFUSABLE");
                }
                if ((this.checks & 2) != 0) {
                    sb.append(" MIXED_SCRIPT_CONFUSABLE");
                }
                if ((this.checks & 4) != 0) {
                    sb.append(" WHOLE_SCRIPT_CONFUSABLE");
                }
                if ((this.checks & 8) != 0) {
                    sb.append(" ANY_CASE");
                }
                if ((this.checks & 16) != 0) {
                    sb.append(" RESTRICTION_LEVEL");
                }
                if ((this.checks & 32) != 0) {
                    sb.append(" INVISIBLE");
                }
                if ((this.checks & 64) != 0) {
                    sb.append(" CHAR_LIMIT");
                }
                if ((this.checks & 128) != 0) {
                    sb.append(" MIXED_NUMBERS");
                }
            }
            sb.append(", numerics: ");
            sb.append(this.numerics.toPattern(false));
            sb.append(", position: ");
            sb.append(this.position);
            sb.append(", restrictionLevel: ");
            sb.append(this.restrictionLevel);
            return sb.toString();
        }
    }

    private static final class ConfusableDataUtils {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        public static final int FORMAT_VERSION = 2;

        static {
            Class<SpoofChecker> cls = SpoofChecker.class;
        }

        private ConfusableDataUtils() {
        }

        public static final int keyToCodePoint(int key) {
            return 16777215 & key;
        }

        public static final int keyToLength(int key) {
            return ((-16777216 & key) >> 24) + 1;
        }

        public static final int codePointAndLengthToKey(int codePoint, int length) {
            return ((length - 1) << 24) | codePoint;
        }
    }

    public enum RestrictionLevel {
        ASCII,
        SINGLE_SCRIPT_RESTRICTIVE,
        HIGHLY_RESTRICTIVE,
        MODERATELY_RESTRICTIVE,
        MINIMALLY_RESTRICTIVE,
        UNRESTRICTIVE
    }

    static class ScriptSet extends BitSet {
        private static final long serialVersionUID = 1;

        ScriptSet() {
        }

        public void and(int script) {
            clear(0, script);
            clear(script + 1, 178);
        }

        public void setAll() {
            set(0, 178);
        }

        public boolean isFull() {
            return cardinality() == 178;
        }

        public void appendStringTo(StringBuilder sb) {
            sb.append("{ ");
            if (isEmpty()) {
                sb.append("- ");
            } else if (isFull()) {
                sb.append("* ");
            } else {
                for (int script = 0; script < 178; script++) {
                    if (get(script)) {
                        sb.append(UScript.getShortName(script));
                        sb.append(Padder.FALLBACK_PADDING_STRING);
                    }
                }
            }
            sb.append("}");
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("<ScriptSet ");
            appendStringTo(sb);
            sb.append(">");
            return sb.toString();
        }
    }

    private static class SpoofData {
        private static final int DATA_FORMAT = 1130788128;
        private static final IsAcceptable IS_ACCEPTABLE = new IsAcceptable();
        int[] fCFUKeys;
        String fCFUStrings;
        short[] fCFUValues;

        private static final class DefaultData {
            /* access modifiers changed from: private */
            public static IOException EXCEPTION;
            /* access modifiers changed from: private */
            public static SpoofData INSTANCE;

            private DefaultData() {
            }

            static {
                INSTANCE = null;
                EXCEPTION = null;
                try {
                    INSTANCE = new SpoofData(ICUBinary.getRequiredData("confusables.cfu"));
                } catch (IOException e) {
                    EXCEPTION = e;
                }
            }
        }

        private static final class IsAcceptable implements ICUBinary.Authenticate {
            private IsAcceptable() {
            }

            public boolean isDataVersionAcceptable(byte[] version) {
                return (version[0] != 2 && version[1] == 0 && version[2] == 0 && version[3] == 0) ? false : true;
            }
        }

        public static SpoofData getDefault() {
            if (DefaultData.EXCEPTION == null) {
                return DefaultData.INSTANCE;
            }
            throw new MissingResourceException("Could not load default confusables data: " + DefaultData.EXCEPTION.getMessage(), "SpoofChecker", "");
        }

        private SpoofData() {
        }

        private SpoofData(ByteBuffer bytes) throws IOException {
            ICUBinary.readHeader(bytes, DATA_FORMAT, IS_ACCEPTABLE);
            bytes.mark();
            readData(bytes);
        }

        public boolean equals(Object other) {
            if (!(other instanceof SpoofData)) {
                return false;
            }
            SpoofData otherData = (SpoofData) other;
            if (!Arrays.equals(this.fCFUKeys, otherData.fCFUKeys) || !Arrays.equals(this.fCFUValues, otherData.fCFUValues)) {
                return false;
            }
            if (Utility.sameObjects(this.fCFUStrings, otherData.fCFUStrings) || this.fCFUStrings == null || this.fCFUStrings.equals(otherData.fCFUStrings)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (Arrays.hashCode(this.fCFUKeys) ^ Arrays.hashCode(this.fCFUValues)) ^ this.fCFUStrings.hashCode();
        }

        private void readData(ByteBuffer bytes) throws IOException {
            if (bytes.getInt() == 944111087) {
                int i = bytes.getInt();
                int i2 = bytes.getInt();
                int CFUKeysOffset = bytes.getInt();
                int CFUKeysSize = bytes.getInt();
                int CFUValuesOffset = bytes.getInt();
                int CFUValuesSize = bytes.getInt();
                int CFUStringTableOffset = bytes.getInt();
                int CFUStringTableSize = bytes.getInt();
                bytes.reset();
                ICUBinary.skipBytes(bytes, CFUKeysOffset);
                this.fCFUKeys = ICUBinary.getInts(bytes, CFUKeysSize, 0);
                bytes.reset();
                ICUBinary.skipBytes(bytes, CFUValuesOffset);
                this.fCFUValues = ICUBinary.getShorts(bytes, CFUValuesSize, 0);
                bytes.reset();
                ICUBinary.skipBytes(bytes, CFUStringTableOffset);
                this.fCFUStrings = ICUBinary.getString(bytes, CFUStringTableSize, 0);
                return;
            }
            throw new IllegalArgumentException("Bad Spoof Check Data.");
        }

        public void confusableLookup(int inChar, StringBuilder dest) {
            int lo = 0;
            int hi = length();
            while (true) {
                int mid = (lo + hi) / 2;
                if (codePointAt(mid) <= inChar) {
                    if (codePointAt(mid) >= inChar) {
                        lo = mid;
                        break;
                    }
                    lo = mid;
                } else {
                    hi = mid;
                }
                if (hi - lo <= 1) {
                    break;
                }
            }
            if (codePointAt(lo) != inChar) {
                dest.appendCodePoint(inChar);
            } else {
                appendValueTo(lo, dest);
            }
        }

        public int length() {
            return this.fCFUKeys.length;
        }

        public int codePointAt(int index) {
            return ConfusableDataUtils.keyToCodePoint(this.fCFUKeys[index]);
        }

        public void appendValueTo(int index, StringBuilder dest) {
            int stringLength = ConfusableDataUtils.keyToLength(this.fCFUKeys[index]);
            short value = this.fCFUValues[index];
            if (stringLength == 1) {
                dest.append((char) value);
            } else {
                dest.append(this.fCFUStrings, value, value + stringLength);
            }
        }
    }

    private SpoofChecker() {
    }

    @Deprecated
    public RestrictionLevel getRestrictionLevel() {
        return this.fRestrictionLevel;
    }

    public int getChecks() {
        return this.fChecks;
    }

    public Set<ULocale> getAllowedLocales() {
        return Collections.unmodifiableSet(this.fAllowedLocales);
    }

    public Set<Locale> getAllowedJavaLocales() {
        HashSet<Locale> locales = new HashSet<>(this.fAllowedLocales.size());
        for (ULocale uloc : this.fAllowedLocales) {
            locales.add(uloc.toLocale());
        }
        return locales;
    }

    public UnicodeSet getAllowedChars() {
        return this.fAllowedCharsSet;
    }

    public boolean failsChecks(String text, CheckResult checkResult) {
        int length = text.length();
        int result = 0;
        if (checkResult != null) {
            checkResult.position = 0;
            checkResult.numerics = null;
            checkResult.restrictionLevel = null;
        }
        if ((this.fChecks & 16) != 0) {
            RestrictionLevel textRestrictionLevel = getRestrictionLevel(text);
            if (textRestrictionLevel.compareTo(this.fRestrictionLevel) > 0) {
                result = 0 | 16;
            }
            if (checkResult != null) {
                checkResult.restrictionLevel = textRestrictionLevel;
            }
        }
        if ((this.fChecks & 128) != 0) {
            UnicodeSet numerics = new UnicodeSet();
            getNumerics(text, numerics);
            if (numerics.size() > 1) {
                result |= 128;
            }
            if (checkResult != null) {
                checkResult.numerics = numerics;
            }
        }
        if ((this.fChecks & 64) != 0) {
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                int c = Character.codePointAt(text, i);
                i = Character.offsetByCodePoints(text, i, 1);
                if (!this.fAllowedCharsSet.contains(c)) {
                    result |= 64;
                    break;
                }
            }
        }
        if ((this.fChecks & 32) != 0) {
            String nfdText = nfdNormalizer.normalize(text);
            UnicodeSet marksSeenSoFar = new UnicodeSet();
            boolean haveMultipleMarks = false;
            int firstNonspacingMark = 0;
            int i2 = 0;
            while (true) {
                if (i2 >= length) {
                    break;
                }
                int c2 = Character.codePointAt(nfdText, i2);
                i2 = Character.offsetByCodePoints(nfdText, i2, 1);
                if (Character.getType(c2) != 6) {
                    firstNonspacingMark = 0;
                    if (haveMultipleMarks) {
                        marksSeenSoFar.clear();
                        haveMultipleMarks = false;
                    }
                } else if (firstNonspacingMark == 0) {
                    firstNonspacingMark = c2;
                } else {
                    if (!haveMultipleMarks) {
                        marksSeenSoFar.add(firstNonspacingMark);
                        haveMultipleMarks = true;
                    }
                    if (marksSeenSoFar.contains(c2)) {
                        result |= 32;
                        break;
                    }
                    marksSeenSoFar.add(c2);
                }
            }
        }
        if (checkResult != null) {
            checkResult.checks = result;
        }
        if (result != 0) {
            return true;
        }
        return false;
    }

    public boolean failsChecks(String text) {
        return failsChecks(text, null);
    }

    public int areConfusable(String s1, String s2) {
        int result;
        if ((this.fChecks & 7) == 0) {
            throw new IllegalArgumentException("No confusable checks are enabled.");
        } else if (!getSkeleton(s1).equals(getSkeleton(s2))) {
            return 0;
        } else {
            ScriptSet s1RSS = new ScriptSet();
            getResolvedScriptSet(s1, s1RSS);
            ScriptSet s2RSS = new ScriptSet();
            getResolvedScriptSet(s2, s2RSS);
            if (s1RSS.intersects(s2RSS)) {
                result = 0 | 1;
            } else {
                result = 0 | 2;
                if (!s1RSS.isEmpty() && !s2RSS.isEmpty()) {
                    result |= 4;
                }
            }
            return result & this.fChecks;
        }
    }

    public String getSkeleton(CharSequence str) {
        String nfdId = nfdNormalizer.normalize(str);
        int normalizedLen = nfdId.length();
        StringBuilder skelSB = new StringBuilder();
        int inputIndex = 0;
        while (inputIndex < normalizedLen) {
            int c = Character.codePointAt(nfdId, inputIndex);
            inputIndex += Character.charCount(c);
            this.fSpoofData.confusableLookup(c, skelSB);
        }
        return nfdNormalizer.normalize(skelSB.toString());
    }

    @Deprecated
    public String getSkeleton(int type, String id) {
        return getSkeleton(id);
    }

    public boolean equals(Object other) {
        if (!(other instanceof SpoofChecker)) {
            return false;
        }
        SpoofChecker otherSC = (SpoofChecker) other;
        if ((this.fSpoofData != otherSC.fSpoofData && this.fSpoofData != null && !this.fSpoofData.equals(otherSC.fSpoofData)) || this.fChecks != otherSC.fChecks) {
            return false;
        }
        if (this.fAllowedLocales != otherSC.fAllowedLocales && this.fAllowedLocales != null && !this.fAllowedLocales.equals(otherSC.fAllowedLocales)) {
            return false;
        }
        if ((this.fAllowedCharsSet == otherSC.fAllowedCharsSet || this.fAllowedCharsSet == null || this.fAllowedCharsSet.equals(otherSC.fAllowedCharsSet)) && this.fRestrictionLevel == otherSC.fRestrictionLevel) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (((this.fChecks ^ this.fSpoofData.hashCode()) ^ this.fAllowedLocales.hashCode()) ^ this.fAllowedCharsSet.hashCode()) ^ this.fRestrictionLevel.ordinal();
    }

    private static void getAugmentedScriptSet(int codePoint, ScriptSet result) {
        result.clear();
        UScript.getScriptExtensions(codePoint, result);
        if (result.get(17)) {
            result.set(172);
            result.set(105);
            result.set(119);
        }
        if (result.get(20)) {
            result.set(105);
        }
        if (result.get(22)) {
            result.set(105);
        }
        if (result.get(18)) {
            result.set(119);
        }
        if (result.get(5)) {
            result.set(172);
        }
        if (result.get(0) || result.get(1)) {
            result.setAll();
        }
    }

    private void getResolvedScriptSet(CharSequence input, ScriptSet result) {
        getResolvedScriptSetWithout(input, 178, result);
    }

    private void getResolvedScriptSetWithout(CharSequence input, int script, ScriptSet result) {
        result.setAll();
        ScriptSet temp = new ScriptSet();
        int utf16Offset = 0;
        while (utf16Offset < input.length()) {
            int codePoint = Character.codePointAt(input, utf16Offset);
            utf16Offset += Character.charCount(codePoint);
            getAugmentedScriptSet(codePoint, temp);
            if (script == 178 || !temp.get(script)) {
                result.and(temp);
            }
        }
    }

    private void getNumerics(String input, UnicodeSet result) {
        result.clear();
        int utf16Offset = 0;
        while (utf16Offset < input.length()) {
            int codePoint = Character.codePointAt(input, utf16Offset);
            utf16Offset += Character.charCount(codePoint);
            if (UCharacter.getType(codePoint) == 9) {
                result.add(codePoint - UCharacter.getNumericValue(codePoint));
            }
        }
    }

    private RestrictionLevel getRestrictionLevel(String input) {
        if (!this.fAllowedCharsSet.containsAll(input)) {
            return RestrictionLevel.UNRESTRICTIVE;
        }
        if (ASCII.containsAll(input)) {
            return RestrictionLevel.ASCII;
        }
        ScriptSet resolvedScriptSet = new ScriptSet();
        getResolvedScriptSet(input, resolvedScriptSet);
        if (!resolvedScriptSet.isEmpty()) {
            return RestrictionLevel.SINGLE_SCRIPT_RESTRICTIVE;
        }
        ScriptSet resolvedNoLatn = new ScriptSet();
        getResolvedScriptSetWithout(input, 25, resolvedNoLatn);
        if (resolvedNoLatn.get(172) || resolvedNoLatn.get(105) || resolvedNoLatn.get(119)) {
            return RestrictionLevel.HIGHLY_RESTRICTIVE;
        }
        if (resolvedNoLatn.isEmpty() || resolvedNoLatn.get(8) || resolvedNoLatn.get(14) || resolvedNoLatn.get(6)) {
            return RestrictionLevel.MINIMALLY_RESTRICTIVE;
        }
        return RestrictionLevel.MODERATELY_RESTRICTIVE;
    }
}
