package android.icu.text;

import android.icu.impl.ICUBinary;
import android.icu.impl.ICUBinary.Authenticate;
import android.icu.impl.Trie2;
import android.icu.impl.Trie2Writable;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import android.icu.lang.UScript;
import android.icu.util.ULocale;
import dalvik.bytecode.Opcodes;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;

public class SpoofChecker {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final int ALL_CHECKS = -1;
    public static final int ANY_CASE = 8;
    public static final int CHAR_LIMIT = 64;
    @Deprecated
    public static final UnicodeSet INCLUSION = null;
    public static final int INVISIBLE = 32;
    static final int KEY_LENGTH_SHIFT = 29;
    static final int KEY_MULTIPLE_VALUES = 268435456;
    static final int MAGIC = 944111087;
    static final int MA_TABLE_FLAG = 134217728;
    @Deprecated
    public static final int MIXED_NUMBERS = 128;
    public static final int MIXED_SCRIPT_CONFUSABLE = 2;
    static final int ML_TABLE_FLAG = 67108864;
    @Deprecated
    public static final UnicodeSet RECOMMENDED = null;
    @Deprecated
    public static final int RESTRICTION_LEVEL = 16;
    static final int SA_TABLE_FLAG = 33554432;
    @Deprecated
    public static final int SINGLE_SCRIPT = 16;
    public static final int SINGLE_SCRIPT_CONFUSABLE = 1;
    static final int SL_TABLE_FLAG = 16777216;
    public static final int WHOLE_SCRIPT_CONFUSABLE = 4;
    private static Normalizer2 nfdNormalizer;
    private UnicodeSet fAllowedCharsSet;
    private Set<ULocale> fAllowedLocales;
    private IdentifierInfo fCachedIdentifierInfo;
    private int fChecks;
    private RestrictionLevel fRestrictionLevel;
    private SpoofData fSpoofData;

    public static class Builder {
        final UnicodeSet fAllowedCharsSet;
        final Set<ULocale> fAllowedLocales;
        int fChecks;
        private RestrictionLevel fRestrictionLevel;
        SpoofData fSpoofData;

        private static class ConfusabledataBuilder {
            static final /* synthetic */ boolean -assertionsDisabled = false;
            private UnicodeSet fKeySet;
            private ArrayList<Integer> fKeyVec;
            private int fLineNum;
            private Hashtable<Integer, SPUString> fMATable;
            private Hashtable<Integer, SPUString> fMLTable;
            private Pattern fParseHexNum;
            private Pattern fParseLine;
            private Hashtable<Integer, SPUString> fSATable;
            private Hashtable<Integer, SPUString> fSLTable;
            private ArrayList<Integer> fStringLengthsTable;
            private StringBuffer fStringTable;
            private ArrayList<Integer> fValueVec;
            private SPUStringPool stringPool;

            private static class SPUString {
                String fStr;
                int fStrTableIndex;

                SPUString(String s) {
                    this.fStr = s;
                    this.fStrTableIndex = 0;
                }
            }

            private static class SPUStringComparator implements Comparator<SPUString> {
                private SPUStringComparator() {
                }

                public int compare(SPUString sL, SPUString sR) {
                    int lenL = sL.fStr.length();
                    int lenR = sR.fStr.length();
                    if (lenL < lenR) {
                        return SpoofChecker.ALL_CHECKS;
                    }
                    if (lenL > lenR) {
                        return SpoofChecker.SINGLE_SCRIPT_CONFUSABLE;
                    }
                    return sL.fStr.compareTo(sR.fStr);
                }
            }

            private static class SPUStringPool {
                private Hashtable<String, SPUString> fHash;
                private Vector<SPUString> fVec;

                public SPUStringPool() {
                    this.fVec = new Vector();
                    this.fHash = new Hashtable();
                }

                public int size() {
                    return this.fVec.size();
                }

                public SPUString getByIndex(int index) {
                    return (SPUString) this.fVec.elementAt(index);
                }

                public SPUString addString(String src) {
                    SPUString hashedString = (SPUString) this.fHash.get(src);
                    if (hashedString != null) {
                        return hashedString;
                    }
                    hashedString = new SPUString(src);
                    this.fHash.put(src, hashedString);
                    this.fVec.addElement(hashedString);
                    return hashedString;
                }

                public void sort() {
                    Collections.sort(this.fVec, new SPUStringComparator());
                }
            }

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.SpoofChecker.Builder.ConfusabledataBuilder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.SpoofChecker.Builder.ConfusabledataBuilder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 9 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 10 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: android.icu.text.SpoofChecker.Builder.ConfusabledataBuilder.<clinit>():void");
            }

            ConfusabledataBuilder() {
                this.fSLTable = new Hashtable();
                this.fSATable = new Hashtable();
                this.fMLTable = new Hashtable();
                this.fMATable = new Hashtable();
                this.fKeySet = new UnicodeSet();
                this.fKeyVec = new ArrayList();
                this.fValueVec = new ArrayList();
                this.stringPool = new SPUStringPool();
            }

            void build(Reader confusables, SpoofData dest) throws ParseException, IOException {
                int i;
                StringBuffer fInput = new StringBuffer();
                WSConfusableDataBuilder.readWholeFileToString(confusables, fInput);
                this.fParseLine = Pattern.compile("(?m)^[ \\t]*([0-9A-Fa-f]+)[ \\t]+;[ \\t]*([0-9A-Fa-f]+(?:[ \\t]+[0-9A-Fa-f]+)*)[ \\t]*;\\s*(?:(SL)|(SA)|(ML)|(MA))[ \\t]*(?:#.*?)?$|^([ \\t]*(?:#.*?)?)$|^(.*?)$");
                this.fParseHexNum = Pattern.compile("\\s*([0-9A-F]+)");
                if (fInput.charAt(0) == '\ufeff') {
                    fInput.setCharAt(0, ' ');
                }
                Matcher matcher = this.fParseLine.matcher(fInput);
                while (matcher.find()) {
                    int keyChar;
                    this.fLineNum += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE;
                    if (matcher.start(7) < 0) {
                        if (matcher.start(SpoofChecker.ANY_CASE) >= 0) {
                            throw new ParseException("Confusables, line " + this.fLineNum + ": Unrecognized Line: " + matcher.group(SpoofChecker.ANY_CASE), matcher.start(SpoofChecker.ANY_CASE));
                        }
                        keyChar = Integer.parseInt(matcher.group(SpoofChecker.SINGLE_SCRIPT_CONFUSABLE), SpoofChecker.SINGLE_SCRIPT);
                        if (keyChar > 1114111) {
                            throw new ParseException("Confusables, line " + this.fLineNum + ": Bad code point: " + matcher.group(SpoofChecker.SINGLE_SCRIPT_CONFUSABLE), matcher.start(SpoofChecker.SINGLE_SCRIPT_CONFUSABLE));
                        }
                        Hashtable<Integer, SPUString> hashtable;
                        Matcher m = this.fParseHexNum.matcher(matcher.group(SpoofChecker.MIXED_SCRIPT_CONFUSABLE));
                        StringBuilder mapString = new StringBuilder();
                        while (m.find()) {
                            int c = Integer.parseInt(m.group(SpoofChecker.SINGLE_SCRIPT_CONFUSABLE), SpoofChecker.SINGLE_SCRIPT);
                            if (keyChar > 1114111) {
                                throw new ParseException("Confusables, line " + this.fLineNum + ": Bad code point: " + Integer.toString(c, SpoofChecker.SINGLE_SCRIPT), matcher.start(SpoofChecker.MIXED_SCRIPT_CONFUSABLE));
                            }
                            mapString.appendCodePoint(c);
                        }
                        if (!-assertionsDisabled) {
                            if ((mapString.length() >= SpoofChecker.SINGLE_SCRIPT_CONFUSABLE ? SpoofChecker.SINGLE_SCRIPT_CONFUSABLE : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        SPUString smapString = this.stringPool.addString(mapString.toString());
                        if (matcher.start(3) >= 0) {
                            hashtable = this.fSLTable;
                        } else if (matcher.start(SpoofChecker.WHOLE_SCRIPT_CONFUSABLE) >= 0) {
                            hashtable = this.fSATable;
                        } else if (matcher.start(5) >= 0) {
                            hashtable = this.fMLTable;
                        } else if (matcher.start(6) >= 0) {
                            hashtable = this.fMATable;
                        } else {
                            hashtable = null;
                        }
                        if (!-assertionsDisabled) {
                            if ((hashtable != null ? SpoofChecker.SINGLE_SCRIPT_CONFUSABLE : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        if (hashtable != this.fMATable) {
                            throw new ParseException("Confusables, line " + this.fLineNum + ": Table must be 'MA'.", 0);
                        }
                        this.fSLTable.put(Integer.valueOf(keyChar), smapString);
                        this.fSATable.put(Integer.valueOf(keyChar), smapString);
                        this.fMLTable.put(Integer.valueOf(keyChar), smapString);
                        this.fMATable.put(Integer.valueOf(keyChar), smapString);
                        this.fKeySet.add(keyChar);
                    }
                }
                this.stringPool.sort();
                this.fStringTable = new StringBuffer();
                this.fStringLengthsTable = new ArrayList();
                int previousStringLength = 0;
                int previousStringIndex = 0;
                int poolSize = this.stringPool.size();
                for (i = 0; i < poolSize; i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                    SPUString s = this.stringPool.getByIndex(i);
                    int strLen = s.fStr.length();
                    int strIndex = this.fStringTable.length();
                    if (!-assertionsDisabled) {
                        if ((strLen >= previousStringLength ? SpoofChecker.SINGLE_SCRIPT_CONFUSABLE : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    if (strLen == SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                        s.fStrTableIndex = s.fStr.charAt(0);
                    } else {
                        if (strLen > previousStringLength && previousStringLength >= SpoofChecker.WHOLE_SCRIPT_CONFUSABLE) {
                            this.fStringLengthsTable.add(Integer.valueOf(previousStringIndex));
                            this.fStringLengthsTable.add(Integer.valueOf(previousStringLength));
                        }
                        s.fStrTableIndex = strIndex;
                        this.fStringTable.append(s.fStr);
                    }
                    previousStringLength = strLen;
                    previousStringIndex = strIndex;
                }
                if (previousStringLength >= SpoofChecker.WHOLE_SCRIPT_CONFUSABLE) {
                    this.fStringLengthsTable.add(Integer.valueOf(previousStringIndex));
                    this.fStringLengthsTable.add(Integer.valueOf(previousStringLength));
                }
                for (String keyCharStr : this.fKeySet) {
                    keyChar = keyCharStr.codePointAt(0);
                    addKeyEntry(keyChar, this.fSLTable, SpoofChecker.SL_TABLE_FLAG);
                    addKeyEntry(keyChar, this.fSATable, SpoofChecker.SA_TABLE_FLAG);
                    addKeyEntry(keyChar, this.fMLTable, SpoofChecker.ML_TABLE_FLAG);
                    addKeyEntry(keyChar, this.fMATable, SpoofChecker.MA_TABLE_FLAG);
                }
                int numKeys = this.fKeyVec.size();
                dest.fCFUKeys = new int[numKeys];
                int previousKey = 0;
                for (i = 0; i < numKeys; i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                    int key = ((Integer) this.fKeyVec.get(i)).intValue();
                    if (!-assertionsDisabled) {
                        if (((16777215 & key) >= (16777215 & previousKey) ? SpoofChecker.SINGLE_SCRIPT_CONFUSABLE : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    if (!-assertionsDisabled) {
                        if (((-16777216 & key) != 0 ? SpoofChecker.SINGLE_SCRIPT_CONFUSABLE : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    dest.fCFUKeys[i] = key;
                    previousKey = key;
                }
                int numValues = this.fValueVec.size();
                if (!-assertionsDisabled) {
                    if ((numKeys == numValues ? SpoofChecker.SINGLE_SCRIPT_CONFUSABLE : null) == null) {
                        throw new AssertionError();
                    }
                }
                dest.fCFUValues = new short[numValues];
                i = 0;
                for (Integer intValue : this.fValueVec) {
                    int value = intValue.intValue();
                    if (!-assertionsDisabled) {
                        if ((value < 65535 ? SpoofChecker.SINGLE_SCRIPT_CONFUSABLE : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    int i2 = i + SpoofChecker.SINGLE_SCRIPT_CONFUSABLE;
                    dest.fCFUValues[i] = (short) value;
                    i = i2;
                }
                dest.fCFUStrings = this.fStringTable.toString();
                int previousLength = 0;
                int stringLengthsSize = this.fStringLengthsTable.size() / SpoofChecker.MIXED_SCRIPT_CONFUSABLE;
                dest.fCFUStringLengths = new SpoofStringLengthsElement[stringLengthsSize];
                for (i = 0; i < stringLengthsSize; i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                    int offset = ((Integer) this.fStringLengthsTable.get(i * SpoofChecker.MIXED_SCRIPT_CONFUSABLE)).intValue();
                    int length = ((Integer) this.fStringLengthsTable.get((i * SpoofChecker.MIXED_SCRIPT_CONFUSABLE) + SpoofChecker.SINGLE_SCRIPT_CONFUSABLE)).intValue();
                    if (!-assertionsDisabled) {
                        if ((offset < dest.fCFUStrings.length() ? SpoofChecker.SINGLE_SCRIPT_CONFUSABLE : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    if (!-assertionsDisabled) {
                        if ((length < 40 ? SpoofChecker.SINGLE_SCRIPT_CONFUSABLE : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    if (!-assertionsDisabled) {
                        if ((length > previousLength ? SpoofChecker.SINGLE_SCRIPT_CONFUSABLE : null) == null) {
                            throw new AssertionError();
                        }
                    }
                    dest.fCFUStringLengths[i] = new SpoofStringLengthsElement();
                    dest.fCFUStringLengths[i].fLastString = offset;
                    dest.fCFUStringLengths[i].fStrLength = length;
                    previousLength = length;
                }
            }

            void addKeyEntry(int keyChar, Hashtable<Integer, SPUString> table, int tableFlag) {
                SPUString targetMapping = (SPUString) table.get(Integer.valueOf(keyChar));
                if (targetMapping != null) {
                    boolean keyHasMultipleValues = SpoofChecker.-assertionsDisabled;
                    int i = this.fKeyVec.size() + SpoofChecker.ALL_CHECKS;
                    while (i >= 0) {
                        int key = ((Integer) this.fKeyVec.get(i)).intValue();
                        if ((16777215 & key) != keyChar) {
                            break;
                        } else if (getMapping(i).equals(targetMapping.fStr)) {
                            this.fKeyVec.set(i, Integer.valueOf(key | tableFlag));
                            return;
                        } else {
                            keyHasMultipleValues = true;
                            i += SpoofChecker.ALL_CHECKS;
                        }
                    }
                    int newKey = keyChar | tableFlag;
                    if (keyHasMultipleValues) {
                        newKey |= SpoofChecker.KEY_MULTIPLE_VALUES;
                    }
                    int adjustedMappingLength = targetMapping.fStr.length() + SpoofChecker.ALL_CHECKS;
                    if (adjustedMappingLength > 3) {
                        adjustedMappingLength = 3;
                    }
                    newKey |= adjustedMappingLength << SpoofChecker.KEY_LENGTH_SHIFT;
                    int newData = targetMapping.fStrTableIndex;
                    this.fKeyVec.add(Integer.valueOf(newKey));
                    this.fValueVec.add(Integer.valueOf(newData));
                    if (keyHasMultipleValues) {
                        int previousKeyIndex = this.fKeyVec.size() - 2;
                        this.fKeyVec.set(previousKeyIndex, Integer.valueOf(((Integer) this.fKeyVec.get(previousKeyIndex)).intValue() | SpoofChecker.KEY_MULTIPLE_VALUES));
                    }
                }
            }

            String getMapping(int index) {
                int key = ((Integer) this.fKeyVec.get(index)).intValue();
                int value = ((Integer) this.fValueVec.get(index)).intValue();
                int length = SpoofChecker.getKeyLength(key);
                switch (length) {
                    case XmlPullParser.START_DOCUMENT /*0*/:
                        char[] cs = new char[SpoofChecker.SINGLE_SCRIPT_CONFUSABLE];
                        cs[0] = (char) value;
                        return new String(cs);
                    case SpoofChecker.SINGLE_SCRIPT_CONFUSABLE /*1*/:
                    case SpoofChecker.MIXED_SCRIPT_CONFUSABLE /*2*/:
                        return this.fStringTable.substring(value, (value + length) + SpoofChecker.SINGLE_SCRIPT_CONFUSABLE);
                    case XmlPullParser.END_TAG /*3*/:
                        length = 0;
                        for (int i = 0; i < this.fStringLengthsTable.size(); i += SpoofChecker.MIXED_SCRIPT_CONFUSABLE) {
                            if (value <= ((Integer) this.fStringLengthsTable.get(i)).intValue()) {
                                length = ((Integer) this.fStringLengthsTable.get(i + SpoofChecker.SINGLE_SCRIPT_CONFUSABLE)).intValue();
                                if (!-assertionsDisabled) {
                                    if ((length < 3 ? SpoofChecker.SINGLE_SCRIPT_CONFUSABLE : 0) == 0) {
                                        throw new AssertionError();
                                    }
                                }
                                return this.fStringTable.substring(value, value + length);
                            }
                        }
                        if (-assertionsDisabled) {
                            if (length < 3) {
                            }
                            if ((length < 3 ? SpoofChecker.SINGLE_SCRIPT_CONFUSABLE : 0) == 0) {
                                throw new AssertionError();
                            }
                        }
                        return this.fStringTable.substring(value, value + length);
                    default:
                        if (-assertionsDisabled) {
                            return XmlPullParser.NO_NAMESPACE;
                        }
                        throw new AssertionError();
                }
            }

            public static void buildConfusableData(Reader confusables, SpoofData dest) throws IOException, ParseException {
                new ConfusabledataBuilder().build(confusables, dest);
            }
        }

        private static class WSConfusableDataBuilder {
            static final /* synthetic */ boolean -assertionsDisabled = false;
            static String parseExp;

            static class BuilderScriptSet {
                int codePoint;
                int index;
                int rindex;
                ScriptSet sset;
                Trie2Writable trie;

                BuilderScriptSet() {
                    this.codePoint = SpoofChecker.ALL_CHECKS;
                    this.trie = null;
                    this.sset = null;
                    this.index = 0;
                    this.rindex = 0;
                }
            }

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.SpoofChecker.Builder.WSConfusableDataBuilder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.SpoofChecker.Builder.WSConfusableDataBuilder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 9 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 10 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: android.icu.text.SpoofChecker.Builder.WSConfusableDataBuilder.<clinit>():void");
            }

            private WSConfusableDataBuilder() {
            }

            static void readWholeFileToString(Reader reader, StringBuffer buffer) throws IOException {
                LineNumberReader lnr = new LineNumberReader(reader);
                while (true) {
                    String line = lnr.readLine();
                    if (line != null) {
                        buffer.append(line);
                        buffer.append('\n');
                    } else {
                        return;
                    }
                }
            }

            static void buildWSConfusableData(Reader confusablesWS, SpoofData dest) throws ParseException, IOException {
                int i;
                StringBuffer input = new StringBuffer();
                int lineNum = 0;
                Trie2Writable anyCaseTrie = new Trie2Writable(0, 0);
                Trie2Writable trie2Writable = new Trie2Writable(0, 0);
                ArrayList<BuilderScriptSet> scriptSets = new ArrayList();
                scriptSets.add(null);
                scriptSets.add(null);
                readWholeFileToString(confusablesWS, input);
                Pattern parseRegexp = Pattern.compile(parseExp);
                if (input.charAt(0) == '\ufeff') {
                    input.setCharAt(0, ' ');
                }
                Matcher matcher = parseRegexp.matcher(input);
                while (matcher.find()) {
                    lineNum += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE;
                    if (matcher.start(SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) < 0) {
                        if (matcher.start(SpoofChecker.ANY_CASE) >= 0) {
                            throw new ParseException("ConfusablesWholeScript, line " + lineNum + ": Unrecognized input: " + matcher.group(), matcher.start());
                        }
                        int startCodePoint = Integer.parseInt(matcher.group(SpoofChecker.MIXED_SCRIPT_CONFUSABLE), SpoofChecker.SINGLE_SCRIPT);
                        if (startCodePoint > 1114111) {
                            throw new ParseException("ConfusablesWholeScript, line " + lineNum + ": out of range code point: " + matcher.group(SpoofChecker.MIXED_SCRIPT_CONFUSABLE), matcher.start(SpoofChecker.MIXED_SCRIPT_CONFUSABLE));
                        }
                        int endCodePoint = startCodePoint;
                        if (matcher.start(3) >= 0) {
                            endCodePoint = Integer.parseInt(matcher.group(3), SpoofChecker.SINGLE_SCRIPT);
                        }
                        if (endCodePoint > 1114111) {
                            throw new ParseException("ConfusablesWholeScript, line " + lineNum + ": out of range code point: " + matcher.group(3), matcher.start(3));
                        }
                        String srcScriptName = matcher.group(SpoofChecker.WHOLE_SCRIPT_CONFUSABLE);
                        String targScriptName = matcher.group(5);
                        int srcScript = UCharacter.getPropertyValueEnum(UProperty.SCRIPT, srcScriptName);
                        int targScript = UCharacter.getPropertyValueEnum(UProperty.SCRIPT, targScriptName);
                        if (srcScript == SpoofChecker.ALL_CHECKS) {
                            throw new ParseException("ConfusablesWholeScript, line " + lineNum + ": Invalid script code t: " + matcher.group(SpoofChecker.WHOLE_SCRIPT_CONFUSABLE), matcher.start(SpoofChecker.WHOLE_SCRIPT_CONFUSABLE));
                        } else if (targScript == SpoofChecker.ALL_CHECKS) {
                            throw new ParseException("ConfusablesWholeScript, line " + lineNum + ": Invalid script code t: " + matcher.group(5), matcher.start(5));
                        } else {
                            Trie2Writable table = anyCaseTrie;
                            if (matcher.start(7) >= 0) {
                                table = trie2Writable;
                            }
                            for (int cp = startCodePoint; cp <= endCodePoint; cp += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                                BuilderScriptSet bsset;
                                int setIndex = table.get(cp);
                                if (setIndex > 0) {
                                    if (!-assertionsDisabled) {
                                        if ((setIndex < scriptSets.size() ? SpoofChecker.SINGLE_SCRIPT_CONFUSABLE : null) == null) {
                                            throw new AssertionError();
                                        }
                                    }
                                    bsset = (BuilderScriptSet) scriptSets.get(setIndex);
                                } else {
                                    bsset = new BuilderScriptSet();
                                    bsset.codePoint = cp;
                                    bsset.trie = table;
                                    bsset.sset = new ScriptSet();
                                    setIndex = scriptSets.size();
                                    bsset.index = setIndex;
                                    bsset.rindex = 0;
                                    scriptSets.add(bsset);
                                    table.set(cp, setIndex);
                                }
                                bsset.sset.Union(targScript);
                                bsset.sset.Union(srcScript);
                                if (UScript.getScript(cp) != srcScript) {
                                    throw new ParseException("ConfusablesWholeScript, line " + lineNum + ": Mismatch between source script and code point " + Integer.toString(cp, SpoofChecker.SINGLE_SCRIPT), matcher.start(5));
                                }
                            }
                            continue;
                        }
                    }
                }
                int rtScriptSetsCount = SpoofChecker.MIXED_SCRIPT_CONFUSABLE;
                for (int outeri = SpoofChecker.MIXED_SCRIPT_CONFUSABLE; outeri < scriptSets.size(); outeri += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                    BuilderScriptSet outerSet = (BuilderScriptSet) scriptSets.get(outeri);
                    int i2 = outerSet.index;
                    if (r0 == outeri) {
                        int rtScriptSetsCount2 = rtScriptSetsCount + SpoofChecker.SINGLE_SCRIPT_CONFUSABLE;
                        outerSet.rindex = rtScriptSetsCount;
                        for (int inneri = outeri + SpoofChecker.SINGLE_SCRIPT_CONFUSABLE; inneri < scriptSets.size(); inneri += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                            BuilderScriptSet innerSet = (BuilderScriptSet) scriptSets.get(inneri);
                            if (outerSet.sset.equals(innerSet.sset) && outerSet.sset != innerSet.sset) {
                                innerSet.sset = outerSet.sset;
                                innerSet.index = outeri;
                                innerSet.rindex = outerSet.rindex;
                            }
                        }
                        rtScriptSetsCount = rtScriptSetsCount2;
                    }
                }
                for (i = SpoofChecker.MIXED_SCRIPT_CONFUSABLE; i < scriptSets.size(); i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                    BuilderScriptSet bSet = (BuilderScriptSet) scriptSets.get(i);
                    i2 = bSet.rindex;
                    if (r0 != i) {
                        bSet.trie.set(bSet.codePoint, bSet.rindex);
                    }
                }
                UnicodeSet ignoreSet = new UnicodeSet();
                ignoreSet.applyIntPropertyValue(UProperty.SCRIPT, 0);
                UnicodeSet inheritedSet = new UnicodeSet();
                inheritedSet.applyIntPropertyValue(UProperty.SCRIPT, SpoofChecker.SINGLE_SCRIPT_CONFUSABLE);
                ignoreSet.addAll(inheritedSet);
                for (int rn = 0; rn < ignoreSet.getRangeCount(); rn += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                    int rangeStart = ignoreSet.getRangeStart(rn);
                    int rangeEnd = ignoreSet.getRangeEnd(rn);
                    anyCaseTrie.setRange(rangeStart, rangeEnd, SpoofChecker.SINGLE_SCRIPT_CONFUSABLE, true);
                    trie2Writable.setRange(rangeStart, rangeEnd, SpoofChecker.SINGLE_SCRIPT_CONFUSABLE, true);
                }
                dest.fAnyCaseTrie = anyCaseTrie.toTrie2_16();
                dest.fLowerCaseTrie = trie2Writable.toTrie2_16();
                dest.fScriptSets = new ScriptSet[rtScriptSetsCount];
                dest.fScriptSets[0] = new ScriptSet();
                dest.fScriptSets[SpoofChecker.SINGLE_SCRIPT_CONFUSABLE] = new ScriptSet();
                int rindex = SpoofChecker.MIXED_SCRIPT_CONFUSABLE;
                for (i = SpoofChecker.MIXED_SCRIPT_CONFUSABLE; i < scriptSets.size(); i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                    bSet = (BuilderScriptSet) scriptSets.get(i);
                    i2 = bSet.rindex;
                    if (r0 >= rindex) {
                        if (!-assertionsDisabled) {
                            if ((rindex == bSet.rindex ? SpoofChecker.SINGLE_SCRIPT_CONFUSABLE : null) == null) {
                                throw new AssertionError();
                            }
                        }
                        dest.fScriptSets[rindex] = bSet.sset;
                        rindex += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE;
                    }
                }
            }
        }

        public Builder() {
            this.fAllowedCharsSet = new UnicodeSet(0, (int) UnicodeSet.MAX_VALUE);
            this.fAllowedLocales = new LinkedHashSet();
            this.fChecks = SpoofChecker.ALL_CHECKS;
            this.fSpoofData = null;
            this.fRestrictionLevel = RestrictionLevel.HIGHLY_RESTRICTIVE;
        }

        public Builder(SpoofChecker src) {
            this.fAllowedCharsSet = new UnicodeSet(0, (int) UnicodeSet.MAX_VALUE);
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
            result.fChecks = this.fChecks;
            result.fSpoofData = this.fSpoofData;
            result.fAllowedCharsSet = (UnicodeSet) this.fAllowedCharsSet.clone();
            result.fAllowedCharsSet.freeze();
            result.fAllowedLocales = new HashSet(this.fAllowedLocales);
            result.fRestrictionLevel = this.fRestrictionLevel;
            return result;
        }

        public Builder setData(Reader confusables, Reader confusablesWholeScript) throws ParseException, IOException {
            this.fSpoofData = new SpoofData();
            ConfusabledataBuilder.buildConfusableData(confusables, this.fSpoofData);
            WSConfusableDataBuilder.buildWSConfusableData(confusablesWholeScript, this.fSpoofData);
            return this;
        }

        public Builder setChecks(int checks) {
            if ((checks & 0) != 0) {
                throw new IllegalArgumentException("Bad Spoof Checks value.");
            }
            this.fChecks = checks & SpoofChecker.ALL_CHECKS;
            return this;
        }

        public Builder setAllowedLocales(Set<ULocale> locales) {
            this.fAllowedCharsSet.clear();
            for (ULocale locale : locales) {
                addScriptChars(locale, this.fAllowedCharsSet);
            }
            this.fAllowedLocales.clear();
            if (locales.size() == 0) {
                this.fAllowedCharsSet.add(0, UnicodeSet.MAX_VALUE);
                this.fChecks &= -65;
                return this;
            }
            UnicodeSet tempSet = new UnicodeSet();
            tempSet.applyIntPropertyValue(UProperty.SCRIPT, 0);
            this.fAllowedCharsSet.addAll(tempSet);
            tempSet.applyIntPropertyValue(UProperty.SCRIPT, SpoofChecker.SINGLE_SCRIPT_CONFUSABLE);
            this.fAllowedCharsSet.addAll(tempSet);
            this.fAllowedLocales.clear();
            this.fAllowedLocales.addAll(locales);
            this.fChecks |= SpoofChecker.CHAR_LIMIT;
            return this;
        }

        public Builder setAllowedJavaLocales(Set<Locale> locales) {
            HashSet<ULocale> ulocales = new HashSet(locales.size());
            for (Locale locale : locales) {
                ulocales.add(ULocale.forLocale(locale));
            }
            return setAllowedLocales(ulocales);
        }

        private void addScriptChars(ULocale locale, UnicodeSet allowedChars) {
            int[] scripts = UScript.getCode(locale);
            UnicodeSet tmpSet = new UnicodeSet();
            for (int i = 0; i < scripts.length; i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                tmpSet.applyIntPropertyValue(UProperty.SCRIPT, scripts[i]);
                allowedChars.addAll(tmpSet);
            }
        }

        public Builder setAllowedChars(UnicodeSet chars) {
            this.fAllowedCharsSet.set(chars);
            this.fAllowedLocales.clear();
            this.fChecks |= SpoofChecker.CHAR_LIMIT;
            return this;
        }

        @Deprecated
        public Builder setRestrictionLevel(RestrictionLevel restrictionLevel) {
            this.fRestrictionLevel = restrictionLevel;
            this.fChecks |= SpoofChecker.SINGLE_SCRIPT;
            return this;
        }
    }

    public static class CheckResult {
        public int checks;
        @Deprecated
        public UnicodeSet numerics;
        @Deprecated
        public int position;
        @Deprecated
        public RestrictionLevel restrictionLevel;

        public CheckResult() {
            this.checks = 0;
            this.position = 0;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("checks:");
            if (this.checks == 0) {
                sb.append(" none");
            } else if (this.checks == SpoofChecker.ALL_CHECKS) {
                sb.append(" all");
            } else {
                if ((this.checks & SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) != 0) {
                    sb.append(" SINGLE_SCRIPT_CONFUSABLE");
                }
                if ((this.checks & SpoofChecker.MIXED_SCRIPT_CONFUSABLE) != 0) {
                    sb.append(" MIXED_SCRIPT_CONFUSABLE");
                }
                if ((this.checks & SpoofChecker.WHOLE_SCRIPT_CONFUSABLE) != 0) {
                    sb.append(" WHOLE_SCRIPT_CONFUSABLE");
                }
                if ((this.checks & SpoofChecker.ANY_CASE) != 0) {
                    sb.append(" ANY_CASE");
                }
                if ((this.checks & SpoofChecker.SINGLE_SCRIPT) != 0) {
                    sb.append(" RESTRICTION_LEVEL");
                }
                if ((this.checks & SpoofChecker.INVISIBLE) != 0) {
                    sb.append(" INVISIBLE");
                }
                if ((this.checks & SpoofChecker.CHAR_LIMIT) != 0) {
                    sb.append(" CHAR_LIMIT");
                }
                if ((this.checks & SpoofChecker.MIXED_NUMBERS) != 0) {
                    sb.append(" MIXED_NUMBERS");
                }
            }
            sb.append(", numerics: ").append(this.numerics.toPattern(SpoofChecker.-assertionsDisabled));
            sb.append(", position: ").append(this.position);
            sb.append(", restrictionLevel: ").append(this.restrictionLevel);
            return sb.toString();
        }
    }

    public enum RestrictionLevel {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.SpoofChecker.RestrictionLevel.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.SpoofChecker.RestrictionLevel.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.SpoofChecker.RestrictionLevel.<clinit>():void");
        }
    }

    static class ScriptSet {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private int[] bits;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.SpoofChecker.ScriptSet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.SpoofChecker.ScriptSet.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.SpoofChecker.ScriptSet.<clinit>():void");
        }

        public ScriptSet() {
            this.bits = new int[6];
        }

        public ScriptSet(ByteBuffer bytes) throws IOException {
            this.bits = new int[6];
            for (int j = 0; j < this.bits.length; j += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                this.bits[j] = bytes.getInt();
            }
        }

        public void output(DataOutputStream os) throws IOException {
            for (int i = 0; i < this.bits.length; i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                os.writeInt(this.bits[i]);
            }
        }

        public boolean equals(Object other) {
            if (!(other instanceof ScriptSet)) {
                return SpoofChecker.-assertionsDisabled;
            }
            return Arrays.equals(this.bits, ((ScriptSet) other).bits);
        }

        public void Union(int script) {
            int i = SpoofChecker.SINGLE_SCRIPT_CONFUSABLE;
            int index = script / SpoofChecker.INVISIBLE;
            int bit = SpoofChecker.SINGLE_SCRIPT_CONFUSABLE << (script & 31);
            if (!-assertionsDisabled) {
                if (index >= (this.bits.length * SpoofChecker.WHOLE_SCRIPT_CONFUSABLE) * SpoofChecker.WHOLE_SCRIPT_CONFUSABLE) {
                    i = 0;
                }
                if (i == 0) {
                    throw new AssertionError();
                }
            }
            int[] iArr = this.bits;
            iArr[index] = iArr[index] | bit;
        }

        public void Union(ScriptSet other) {
            for (int i = 0; i < this.bits.length; i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                int[] iArr = this.bits;
                iArr[i] = iArr[i] | other.bits[i];
            }
        }

        public void intersect(ScriptSet other) {
            for (int i = 0; i < this.bits.length; i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                int[] iArr = this.bits;
                iArr[i] = iArr[i] & other.bits[i];
            }
        }

        public void intersect(int script) {
            int i;
            int i2 = SpoofChecker.SINGLE_SCRIPT_CONFUSABLE;
            int index = script / SpoofChecker.INVISIBLE;
            int bit = SpoofChecker.SINGLE_SCRIPT_CONFUSABLE << (script & 31);
            if (!-assertionsDisabled) {
                if (index >= (this.bits.length * SpoofChecker.WHOLE_SCRIPT_CONFUSABLE) * SpoofChecker.WHOLE_SCRIPT_CONFUSABLE) {
                    i2 = 0;
                }
                if (i2 == 0) {
                    throw new AssertionError();
                }
            }
            for (i = 0; i < index; i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                this.bits[i] = 0;
            }
            int[] iArr = this.bits;
            iArr[index] = iArr[index] & bit;
            for (i = index + SpoofChecker.SINGLE_SCRIPT_CONFUSABLE; i < this.bits.length; i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                this.bits[i] = 0;
            }
        }

        public void setAll() {
            for (int i = 0; i < this.bits.length; i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                this.bits[i] = SpoofChecker.ALL_CHECKS;
            }
        }

        public void resetAll() {
            for (int i = 0; i < this.bits.length; i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                this.bits[i] = 0;
            }
        }

        public int countMembers() {
            int count = 0;
            for (int i = 0; i < this.bits.length; i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                for (int x = this.bits[i]; x != 0; x &= x + SpoofChecker.ALL_CHECKS) {
                    count += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE;
                }
            }
            return count;
        }
    }

    private static class SpoofData {
        private static final int DATA_FORMAT = 1130788128;
        private static final IsAcceptable IS_ACCEPTABLE = null;
        Trie2 fAnyCaseTrie;
        int[] fCFUKeys;
        SpoofStringLengthsElement[] fCFUStringLengths;
        String fCFUStrings;
        short[] fCFUValues;
        Trie2 fLowerCaseTrie;
        ScriptSet[] fScriptSets;

        private static final class DefaultData {
            private static SpoofData INSTANCE;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.SpoofChecker.SpoofData.DefaultData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.SpoofChecker.SpoofData.DefaultData.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 9 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 10 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: android.icu.text.SpoofChecker.SpoofData.DefaultData.<clinit>():void");
            }

            private DefaultData() {
            }
        }

        private static final class IsAcceptable implements Authenticate {
            /* synthetic */ IsAcceptable(IsAcceptable isAcceptable) {
                this();
            }

            private IsAcceptable() {
            }

            public boolean isDataVersionAcceptable(byte[] version) {
                return version[0] == (byte) 1 ? true : SpoofChecker.-assertionsDisabled;
            }
        }

        static class SpoofStringLengthsElement {
            int fLastString;
            int fStrLength;

            SpoofStringLengthsElement() {
            }

            public boolean equals(Object other) {
                boolean z = SpoofChecker.-assertionsDisabled;
                if (!(other instanceof SpoofStringLengthsElement)) {
                    return SpoofChecker.-assertionsDisabled;
                }
                SpoofStringLengthsElement otherEl = (SpoofStringLengthsElement) other;
                if (this.fLastString == otherEl.fLastString && this.fStrLength == otherEl.fStrLength) {
                    z = true;
                }
                return z;
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.SpoofChecker.SpoofData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.SpoofChecker.SpoofData.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.SpoofChecker.SpoofData.<clinit>():void");
        }

        static SpoofData getDefault() {
            return DefaultData.INSTANCE;
        }

        SpoofData() {
        }

        SpoofData(ByteBuffer bytes) throws IOException {
            ICUBinary.readHeader(bytes, DATA_FORMAT, IS_ACCEPTABLE);
            bytes.mark();
            readData(bytes);
        }

        public boolean equals(Object other) {
            if (!(other instanceof SpoofData)) {
                return SpoofChecker.-assertionsDisabled;
            }
            SpoofData otherData = (SpoofData) other;
            if (!Arrays.equals(this.fCFUKeys, otherData.fCFUKeys) || !Arrays.equals(this.fCFUValues, otherData.fCFUValues) || !Arrays.deepEquals(this.fCFUStringLengths, otherData.fCFUStringLengths)) {
                return SpoofChecker.-assertionsDisabled;
            }
            if (this.fCFUStrings != otherData.fCFUStrings && this.fCFUStrings != null && !this.fCFUStrings.equals(otherData.fCFUStrings)) {
                return SpoofChecker.-assertionsDisabled;
            }
            if (this.fAnyCaseTrie != otherData.fAnyCaseTrie && this.fAnyCaseTrie != null && !this.fAnyCaseTrie.equals(otherData.fAnyCaseTrie)) {
                return SpoofChecker.-assertionsDisabled;
            }
            if ((this.fLowerCaseTrie == otherData.fLowerCaseTrie || this.fLowerCaseTrie == null || this.fLowerCaseTrie.equals(otherData.fLowerCaseTrie)) && Arrays.deepEquals(this.fScriptSets, otherData.fScriptSets)) {
                return true;
            }
            return SpoofChecker.-assertionsDisabled;
        }

        void readData(ByteBuffer bytes) throws IOException {
            if (bytes.getInt() != SpoofChecker.MAGIC) {
                throw new IllegalArgumentException("Bad Spoof Check Data.");
            }
            int i;
            int dataFormatVersion = bytes.getInt();
            int dataLength = bytes.getInt();
            int CFUKeysOffset = bytes.getInt();
            int CFUKeysSize = bytes.getInt();
            int CFUValuesOffset = bytes.getInt();
            int CFUValuesSize = bytes.getInt();
            int CFUStringTableOffset = bytes.getInt();
            int CFUStringTableSize = bytes.getInt();
            int CFUStringLengthsOffset = bytes.getInt();
            int CFUStringLengthsSize = bytes.getInt();
            int anyCaseTrieOffset = bytes.getInt();
            int anyCaseTrieSize = bytes.getInt();
            int lowerCaseTrieOffset = bytes.getInt();
            int lowerCaseTrieLength = bytes.getInt();
            int scriptSetsOffset = bytes.getInt();
            int scriptSetslength = bytes.getInt();
            this.fCFUKeys = null;
            this.fCFUValues = null;
            this.fCFUStringLengths = null;
            this.fCFUStrings = null;
            bytes.reset();
            ICUBinary.skipBytes(bytes, CFUKeysOffset);
            this.fCFUKeys = ICUBinary.getInts(bytes, CFUKeysSize, 0);
            bytes.reset();
            ICUBinary.skipBytes(bytes, CFUValuesOffset);
            this.fCFUValues = ICUBinary.getShorts(bytes, CFUValuesSize, 0);
            bytes.reset();
            ICUBinary.skipBytes(bytes, CFUStringTableOffset);
            this.fCFUStrings = ICUBinary.getString(bytes, CFUStringTableSize, 0);
            bytes.reset();
            ICUBinary.skipBytes(bytes, CFUStringLengthsOffset);
            this.fCFUStringLengths = new SpoofStringLengthsElement[CFUStringLengthsSize];
            for (i = 0; i < CFUStringLengthsSize; i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                this.fCFUStringLengths[i] = new SpoofStringLengthsElement();
                SpoofStringLengthsElement spoofStringLengthsElement = this.fCFUStringLengths[i];
                spoofStringLengthsElement.fLastString = bytes.getShort();
                spoofStringLengthsElement = this.fCFUStringLengths[i];
                spoofStringLengthsElement.fStrLength = bytes.getShort();
            }
            bytes.reset();
            ICUBinary.skipBytes(bytes, anyCaseTrieOffset);
            this.fAnyCaseTrie = Trie2.createFromSerialized(bytes);
            bytes.reset();
            ICUBinary.skipBytes(bytes, lowerCaseTrieOffset);
            this.fLowerCaseTrie = Trie2.createFromSerialized(bytes);
            bytes.reset();
            ICUBinary.skipBytes(bytes, scriptSetsOffset);
            this.fScriptSets = new ScriptSet[scriptSetslength];
            for (i = 0; i < scriptSetslength; i += SpoofChecker.SINGLE_SCRIPT_CONFUSABLE) {
                this.fScriptSets[i] = new ScriptSet(bytes);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.SpoofChecker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.SpoofChecker.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.SpoofChecker.<clinit>():void");
    }

    /* synthetic */ SpoofChecker(SpoofChecker spoofChecker) {
        this();
    }

    private SpoofChecker() {
        this.fCachedIdentifierInfo = null;
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
        HashSet<Locale> locales = new HashSet(this.fAllowedLocales.size());
        for (ULocale uloc : this.fAllowedLocales) {
            locales.add(uloc.toLocale());
        }
        return locales;
    }

    public UnicodeSet getAllowedChars() {
        return this.fAllowedCharsSet;
    }

    public boolean failsChecks(String text, CheckResult checkResult) {
        int i;
        int c;
        int length = text.length();
        int result = 0;
        if (checkResult != null) {
            checkResult.position = 0;
            checkResult.numerics = null;
            checkResult.restrictionLevel = null;
        }
        IdentifierInfo identifierInfo = null;
        if ((this.fChecks & Opcodes.OP_ADD_INT) != 0) {
            identifierInfo = getIdentifierInfo().setIdentifier(text).setIdentifierProfile(this.fAllowedCharsSet);
        }
        if ((this.fChecks & SINGLE_SCRIPT) != 0) {
            RestrictionLevel textRestrictionLevel = identifierInfo.getRestrictionLevel();
            if (textRestrictionLevel.compareTo(this.fRestrictionLevel) > 0) {
                result = SINGLE_SCRIPT;
            }
            if (checkResult != null) {
                checkResult.restrictionLevel = textRestrictionLevel;
            }
        }
        if ((this.fChecks & MIXED_NUMBERS) != 0) {
            UnicodeSet numerics = identifierInfo.getNumerics();
            if (numerics.size() > SINGLE_SCRIPT_CONFUSABLE) {
                result |= MIXED_NUMBERS;
            }
            if (checkResult != null) {
                checkResult.numerics = numerics;
            }
        }
        if ((this.fChecks & CHAR_LIMIT) != 0) {
            i = 0;
            while (i < length) {
                c = Character.codePointAt(text, i);
                i = Character.offsetByCodePoints(text, i, SINGLE_SCRIPT_CONFUSABLE);
                if (!this.fAllowedCharsSet.contains(c)) {
                    result |= CHAR_LIMIT;
                    break;
                }
            }
        }
        if ((this.fChecks & 38) != 0) {
            String nfdText = nfdNormalizer.normalize(text);
            if ((this.fChecks & INVISIBLE) != 0) {
                int firstNonspacingMark = 0;
                boolean haveMultipleMarks = -assertionsDisabled;
                UnicodeSet marksSeenSoFar = new UnicodeSet();
                i = 0;
                while (i < length) {
                    c = Character.codePointAt(nfdText, i);
                    i = Character.offsetByCodePoints(nfdText, i, SINGLE_SCRIPT_CONFUSABLE);
                    if (Character.getType(c) != 6) {
                        firstNonspacingMark = 0;
                        if (haveMultipleMarks) {
                            marksSeenSoFar.clear();
                            haveMultipleMarks = -assertionsDisabled;
                        }
                    } else if (firstNonspacingMark == 0) {
                        firstNonspacingMark = c;
                    } else {
                        if (!haveMultipleMarks) {
                            marksSeenSoFar.add(firstNonspacingMark);
                            haveMultipleMarks = true;
                        }
                        if (marksSeenSoFar.contains(c)) {
                            result |= INVISIBLE;
                            break;
                        }
                        marksSeenSoFar.add(c);
                    }
                }
            }
            if ((this.fChecks & 6) != 0) {
                if (identifierInfo == null) {
                    identifierInfo = getIdentifierInfo();
                    identifierInfo.setIdentifier(text);
                }
                int scriptCount = identifierInfo.getScriptCount();
                ScriptSet scripts = new ScriptSet();
                wholeScriptCheck(nfdText, scripts);
                int confusableScriptCount = scripts.countMembers();
                if ((this.fChecks & WHOLE_SCRIPT_CONFUSABLE) != 0 && confusableScriptCount >= MIXED_SCRIPT_CONFUSABLE && scriptCount == SINGLE_SCRIPT_CONFUSABLE) {
                    result |= WHOLE_SCRIPT_CONFUSABLE;
                }
                if ((this.fChecks & MIXED_SCRIPT_CONFUSABLE) != 0 && confusableScriptCount >= SINGLE_SCRIPT_CONFUSABLE && scriptCount > SINGLE_SCRIPT_CONFUSABLE) {
                    result |= MIXED_SCRIPT_CONFUSABLE;
                }
            }
        }
        if (checkResult != null) {
            checkResult.checks = result;
        }
        releaseIdentifierInfo(identifierInfo);
        if (result != 0) {
            return true;
        }
        return -assertionsDisabled;
    }

    public boolean failsChecks(String text) {
        return failsChecks(text, null);
    }

    public int areConfusable(String s1, String s2) {
        if ((this.fChecks & 7) == 0) {
            throw new IllegalArgumentException("No confusable checks are enabled.");
        }
        int flagsForSkeleton = this.fChecks & ANY_CASE;
        int result = 0;
        IdentifierInfo identifierInfo = getIdentifierInfo();
        identifierInfo.setIdentifier(s1);
        int s1ScriptCount = identifierInfo.getScriptCount();
        int s1FirstScript = identifierInfo.getScripts().nextSetBit(0);
        identifierInfo.setIdentifier(s2);
        int s2ScriptCount = identifierInfo.getScriptCount();
        int s2FirstScript = identifierInfo.getScripts().nextSetBit(0);
        releaseIdentifierInfo(identifierInfo);
        if ((this.fChecks & SINGLE_SCRIPT_CONFUSABLE) != 0 && s1ScriptCount <= SINGLE_SCRIPT_CONFUSABLE && s2ScriptCount <= SINGLE_SCRIPT_CONFUSABLE && s1FirstScript == s2FirstScript) {
            flagsForSkeleton |= SINGLE_SCRIPT_CONFUSABLE;
            if (getSkeleton(flagsForSkeleton, s1).equals(getSkeleton(flagsForSkeleton, s2))) {
                result = SINGLE_SCRIPT_CONFUSABLE;
            }
        }
        if ((result & SINGLE_SCRIPT_CONFUSABLE) != 0) {
            return result;
        }
        boolean possiblyWholeScriptConfusables = (s1ScriptCount > SINGLE_SCRIPT_CONFUSABLE || s2ScriptCount > SINGLE_SCRIPT_CONFUSABLE) ? -assertionsDisabled : (this.fChecks & WHOLE_SCRIPT_CONFUSABLE) != 0 ? true : -assertionsDisabled;
        if ((this.fChecks & MIXED_SCRIPT_CONFUSABLE) != 0 || possiblyWholeScriptConfusables) {
            flagsForSkeleton &= -2;
            if (getSkeleton(flagsForSkeleton, s1).equals(getSkeleton(flagsForSkeleton, s2))) {
                result |= MIXED_SCRIPT_CONFUSABLE;
                if (possiblyWholeScriptConfusables) {
                    result |= WHOLE_SCRIPT_CONFUSABLE;
                }
            }
        }
        return result;
    }

    public String getSkeleton(int type, String id) {
        int tableMask;
        switch (type) {
            case XmlPullParser.START_DOCUMENT /*0*/:
                tableMask = ML_TABLE_FLAG;
                break;
            case SINGLE_SCRIPT_CONFUSABLE /*1*/:
                tableMask = SL_TABLE_FLAG;
                break;
            case ANY_CASE /*8*/:
                tableMask = MA_TABLE_FLAG;
                break;
            case XmlPullParser.COMMENT /*9*/:
                tableMask = SA_TABLE_FLAG;
                break;
            default:
                throw new IllegalArgumentException("SpoofChecker.getSkeleton(), bad type value.");
        }
        String nfdId = nfdNormalizer.normalize(id);
        int normalizedLen = nfdId.length();
        StringBuilder skelSB = new StringBuilder();
        int inputIndex = 0;
        while (inputIndex < normalizedLen) {
            int c = Character.codePointAt(nfdId, inputIndex);
            inputIndex += Character.charCount(c);
            confusableLookup(c, tableMask, skelSB);
        }
        return nfdNormalizer.normalize(skelSB.toString());
    }

    @Deprecated
    public boolean equals(Object other) {
        if (!(other instanceof SpoofChecker)) {
            return -assertionsDisabled;
        }
        SpoofChecker otherSC = (SpoofChecker) other;
        if ((this.fSpoofData != otherSC.fSpoofData && this.fSpoofData != null && !this.fSpoofData.equals(otherSC.fSpoofData)) || this.fChecks != otherSC.fChecks) {
            return -assertionsDisabled;
        }
        if (this.fAllowedLocales != otherSC.fAllowedLocales && this.fAllowedLocales != null && !this.fAllowedLocales.equals(otherSC.fAllowedLocales)) {
            return -assertionsDisabled;
        }
        if ((this.fAllowedCharsSet == otherSC.fAllowedCharsSet || this.fAllowedCharsSet == null || this.fAllowedCharsSet.equals(otherSC.fAllowedCharsSet)) && this.fRestrictionLevel == otherSC.fRestrictionLevel) {
            return true;
        }
        return -assertionsDisabled;
    }

    @Deprecated
    public int hashCode() {
        if (-assertionsDisabled) {
            return 1234;
        }
        throw new AssertionError();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void confusableLookup(int inChar, int tableMask, StringBuilder dest) {
        int low = 0;
        int limit = this.fSpoofData.fCFUKeys.length;
        boolean foundChar = -assertionsDisabled;
        while (true) {
            int mid = low + ((limit - low) / MIXED_SCRIPT_CONFUSABLE);
            int midc = this.fSpoofData.fCFUKeys[mid] & DictionaryData.TRANSFORM_OFFSET_MASK;
            if (inChar != midc) {
                if (inChar < midc) {
                    limit = mid;
                } else {
                    low = mid + SINGLE_SCRIPT_CONFUSABLE;
                }
                if (low >= limit) {
                    break;
                }
            } else {
                break;
            }
        }
        if (foundChar) {
            boolean foundKey = -assertionsDisabled;
            int keyFlags = this.fSpoofData.fCFUKeys[mid] & -16777216;
            if ((keyFlags & tableMask) == 0) {
                if ((KEY_MULTIPLE_VALUES & keyFlags) != 0) {
                    int altMid = mid + ALL_CHECKS;
                    while (true) {
                        if ((this.fSpoofData.fCFUKeys[altMid] & 16777215) != inChar) {
                            break;
                        }
                        keyFlags = this.fSpoofData.fCFUKeys[altMid] & -16777216;
                        if ((keyFlags & tableMask) != 0) {
                            break;
                        }
                        altMid += ALL_CHECKS;
                    }
                    if (!foundKey) {
                        altMid = mid + SINGLE_SCRIPT_CONFUSABLE;
                        while (true) {
                            if ((this.fSpoofData.fCFUKeys[altMid] & 16777215) != inChar) {
                                break;
                            }
                            keyFlags = this.fSpoofData.fCFUKeys[altMid] & -16777216;
                            if ((keyFlags & tableMask) != 0) {
                                break;
                            }
                            altMid += SINGLE_SCRIPT_CONFUSABLE;
                        }
                        mid = altMid;
                        foundKey = true;
                    }
                }
                if (!foundKey) {
                    dest.appendCodePoint(inChar);
                    return;
                }
            }
            int stringLen = getKeyLength(keyFlags) + SINGLE_SCRIPT_CONFUSABLE;
            int keyTableIndex = mid;
            short value = this.fSpoofData.fCFUValues[keyTableIndex];
            if (stringLen == SINGLE_SCRIPT_CONFUSABLE) {
                dest.append((char) value);
                return;
            }
            if (stringLen == WHOLE_SCRIPT_CONFUSABLE) {
                boolean dataOK = -assertionsDisabled;
                SpoofStringLengthsElement[] spoofStringLengthsElementArr = this.fSpoofData.fCFUStringLengths;
                int length = spoofStringLengthsElementArr.length;
                for (int i = 0; i < length; i += SINGLE_SCRIPT_CONFUSABLE) {
                    SpoofStringLengthsElement el = spoofStringLengthsElementArr[i];
                    short s = el.fLastString;
                    if (r0 >= value) {
                        stringLen = el.fStrLength;
                        dataOK = true;
                        break;
                    }
                }
                if (!(-assertionsDisabled || dataOK)) {
                    throw new AssertionError();
                }
            }
            dest.append(this.fSpoofData.fCFUStrings, value, value + stringLen);
            return;
        }
        dest.appendCodePoint(inChar);
    }

    private void wholeScriptCheck(CharSequence text, ScriptSet result) {
        int inputIdx = 0;
        Trie2 table = (this.fChecks & ANY_CASE) != 0 ? this.fSpoofData.fAnyCaseTrie : this.fSpoofData.fLowerCaseTrie;
        result.setAll();
        while (inputIdx < text.length()) {
            int c = Character.codePointAt(text, inputIdx);
            inputIdx = Character.offsetByCodePoints(text, inputIdx, SINGLE_SCRIPT_CONFUSABLE);
            int index = table.get(c);
            if (index == 0) {
                int cpScript = UScript.getScript(c);
                if (!-assertionsDisabled) {
                    if ((cpScript > SINGLE_SCRIPT_CONFUSABLE ? SINGLE_SCRIPT_CONFUSABLE : 0) == 0) {
                        throw new AssertionError();
                    }
                }
                result.intersect(cpScript);
            } else if (index != SINGLE_SCRIPT_CONFUSABLE) {
                result.intersect(this.fSpoofData.fScriptSets[index]);
            }
        }
    }

    private IdentifierInfo getIdentifierInfo() {
        synchronized (this) {
            IdentifierInfo returnIdInfo = this.fCachedIdentifierInfo;
            this.fCachedIdentifierInfo = null;
        }
        if (returnIdInfo == null) {
            return new IdentifierInfo();
        }
        return returnIdInfo;
    }

    private void releaseIdentifierInfo(IdentifierInfo idInfo) {
        if (idInfo != null) {
            synchronized (this) {
                if (this.fCachedIdentifierInfo == null) {
                    this.fCachedIdentifierInfo = idInfo;
                }
            }
        }
    }

    static final int getKeyLength(int x) {
        return (x >> KEY_LENGTH_SHIFT) & 3;
    }
}
