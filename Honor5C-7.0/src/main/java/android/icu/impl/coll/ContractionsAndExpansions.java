package android.icu.impl.coll;

import android.icu.impl.Trie2.Range;
import android.icu.text.UnicodeSet;
import android.icu.util.CharsTrie;
import android.icu.util.CharsTrie.Entry;
import dalvik.bytecode.Opcodes;
import java.util.Iterator;
import libcore.icu.ICU;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public final class ContractionsAndExpansions {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private boolean addPrefixes;
    private long[] ces;
    private int checkTailored;
    private UnicodeSet contractions;
    private CollationData data;
    private UnicodeSet expansions;
    private UnicodeSet ranges;
    private CESink sink;
    private String suffix;
    private UnicodeSet tailored;
    private StringBuilder unreversedPrefix;

    public interface CESink {
        void handleCE(long j);

        void handleExpansion(long[] jArr, int i, int i2);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.ContractionsAndExpansions.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.ContractionsAndExpansions.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.ContractionsAndExpansions.<clinit>():void");
    }

    public ContractionsAndExpansions(UnicodeSet con, UnicodeSet exp, CESink s, boolean prefixes) {
        this.checkTailored = 0;
        this.tailored = new UnicodeSet();
        this.unreversedPrefix = new StringBuilder();
        this.ces = new long[31];
        this.contractions = con;
        this.expansions = exp;
        this.sink = s;
        this.addPrefixes = prefixes;
    }

    public void forData(CollationData d) {
        if (d.base != null) {
            this.checkTailored = -1;
        }
        this.data = d;
        Iterator<Range> trieIterator = this.data.trie.iterator();
        while (trieIterator.hasNext()) {
            Range range = (Range) trieIterator.next();
            if (range.leadSurrogate) {
                break;
            }
            enumCnERange(range.startCodePoint, range.endCodePoint, range.value, this);
        }
        if (d.base != null) {
            this.tailored.freeze();
            this.checkTailored = 1;
            this.data = d.base;
            trieIterator = this.data.trie.iterator();
            while (trieIterator.hasNext()) {
                range = (Range) trieIterator.next();
                if (range.leadSurrogate) {
                    break;
                }
                enumCnERange(range.startCodePoint, range.endCodePoint, range.value, this);
            }
        }
    }

    private void enumCnERange(int start, int end, int ce32, ContractionsAndExpansions cne) {
        if (cne.checkTailored != 0) {
            if (cne.checkTailored < 0) {
                if (ce32 != Opcodes.OP_AND_LONG_2ADDR) {
                    cne.tailored.add(start, end);
                } else {
                    return;
                }
            } else if (start == end) {
                if (cne.tailored.contains(start)) {
                    return;
                }
            } else if (cne.tailored.containsSome(start, end)) {
                if (cne.ranges == null) {
                    cne.ranges = new UnicodeSet();
                }
                cne.ranges.set(start, end).removeAll(cne.tailored);
                int count = cne.ranges.getRangeCount();
                for (int i = 0; i < count; i++) {
                    cne.handleCE32(cne.ranges.getRangeStart(i), cne.ranges.getRangeEnd(i), ce32);
                }
            }
        }
        cne.handleCE32(start, end, ce32);
    }

    public void forCodePoint(CollationData d, int c) {
        int ce32 = d.getCE32(c);
        if (ce32 == Opcodes.OP_AND_LONG_2ADDR) {
            d = d.base;
            ce32 = d.getCE32(c);
        }
        this.data = d;
        handleCE32(c, c, ce32);
    }

    private void handleCE32(int start, int end, int ce32) {
        while ((ce32 & Opcodes.OP_CONST_CLASS_JUMBO) >= Opcodes.OP_AND_LONG_2ADDR) {
            int length;
            Object obj;
            switch (Collation.tagFromCE32(ce32)) {
                case XmlPullParser.START_DOCUMENT /*0*/:
                    return;
                case NodeFilter.SHOW_ELEMENT /*1*/:
                    if (this.sink != null) {
                        this.sink.handleCE(Collation.ceFromLongPrimaryCE32(ce32));
                    }
                    return;
                case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                    if (this.sink != null) {
                        this.sink.handleCE(Collation.ceFromLongSecondaryCE32(ce32));
                    }
                    return;
                case XmlPullParser.END_TAG /*3*/:
                case XmlPullParser.IGNORABLE_WHITESPACE /*7*/:
                case Opcodes.OP_MOVE_EXCEPTION /*13*/:
                    throw new AssertionError(String.format("Unexpected CE32 tag type %d for ce32=0x%08x", new Object[]{Integer.valueOf(Collation.tagFromCE32(ce32)), Integer.valueOf(ce32)}));
                case NodeFilter.SHOW_TEXT /*4*/:
                    if (this.sink != null) {
                        this.ces[0] = Collation.latinCE0FromCE32(ce32);
                        this.ces[1] = Collation.latinCE1FromCE32(ce32);
                        this.sink.handleExpansion(this.ces, 0, 2);
                    }
                    if (this.unreversedPrefix.length() == 0) {
                        addExpansions(start, end);
                    }
                    return;
                case XmlPullParser.CDSECT /*5*/:
                    if (this.sink != null) {
                        int idx = Collation.indexFromCE32(ce32);
                        length = Collation.lengthFromCE32(ce32);
                        for (int i = 0; i < length; i++) {
                            this.ces[i] = Collation.ceFromCE32(this.data.ce32s[idx + i]);
                        }
                        this.sink.handleExpansion(this.ces, 0, length);
                    }
                    if (this.unreversedPrefix.length() == 0) {
                        addExpansions(start, end);
                    }
                    return;
                case XmlPullParser.ENTITY_REF /*6*/:
                    if (this.sink != null) {
                        this.sink.handleExpansion(this.data.ces, Collation.indexFromCE32(ce32), Collation.lengthFromCE32(ce32));
                    }
                    if (this.unreversedPrefix.length() == 0) {
                        addExpansions(start, end);
                    }
                    return;
                case NodeFilter.SHOW_CDATA_SECTION /*8*/:
                    handlePrefixes(start, end, ce32);
                    return;
                case XmlPullParser.COMMENT /*9*/:
                    handleContractions(start, end, ce32);
                    return;
                case XmlPullParser.DOCDECL /*10*/:
                    ce32 = this.data.ce32s[Collation.indexFromCE32(ce32)];
                    break;
                case ICU.U_TRUNCATED_CHAR_FOUND /*11*/:
                    if (!-assertionsDisabled) {
                        obj = (start == 0 && end == 0) ? 1 : null;
                        if (obj == null) {
                            throw new AssertionError();
                        }
                    }
                    ce32 = this.data.ce32s[0];
                    break;
                case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
                    if (this.sink != null) {
                        UTF16CollationIterator iter = new UTF16CollationIterator(this.data);
                        StringBuilder hangul = new StringBuilder(1);
                        for (int c = start; c <= end; c++) {
                            hangul.setLength(0);
                            hangul.appendCodePoint(c);
                            iter.setText(false, hangul, 0);
                            length = iter.fetchCEs();
                            if (!-assertionsDisabled) {
                                obj = (length < 2 || iter.getCE(length - 1) != Collation.NO_CE) ? null : 1;
                                if (obj == null) {
                                    throw new AssertionError();
                                }
                            }
                            this.sink.handleExpansion(iter.getCEs(), 0, length - 1);
                        }
                    }
                    if (this.unreversedPrefix.length() == 0) {
                        addExpansions(start, end);
                    }
                    return;
                case Opcodes.OP_RETURN_VOID /*14*/:
                    return;
                case ICU.U_BUFFER_OVERFLOW_ERROR /*15*/:
                    return;
                default:
                    break;
            }
        }
        if (this.sink != null) {
            this.sink.handleCE(Collation.ceFromSimpleCE32(ce32));
        }
    }

    private void handlePrefixes(int start, int end, int ce32) {
        int index = Collation.indexFromCE32(ce32);
        handleCE32(start, end, this.data.getCE32FromContexts(index));
        if (this.addPrefixes) {
            CharsTrie.Iterator prefixes = new CharsTrie(this.data.contexts, index + 2).iterator();
            while (prefixes.hasNext()) {
                Entry e = prefixes.next();
                setPrefix(e.chars);
                addStrings(start, end, this.contractions);
                addStrings(start, end, this.expansions);
                handleCE32(start, end, e.value);
            }
            resetPrefix();
        }
    }

    void handleContractions(int start, int end, int ce32) {
        Object obj = 1;
        Object obj2 = null;
        int index = Collation.indexFromCE32(ce32);
        if ((ce32 & NodeFilter.SHOW_DOCUMENT) == 0) {
            ce32 = this.data.getCE32FromContexts(index);
            if (!-assertionsDisabled) {
                if (!Collation.isContractionCE32(ce32)) {
                    int i = 1;
                }
                if (obj2 == null) {
                    throw new AssertionError();
                }
            }
            handleCE32(start, end, ce32);
        } else if (!-assertionsDisabled) {
            if (this.unreversedPrefix.length() == 0) {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        CharsTrie.Iterator suffixes = new CharsTrie(this.data.contexts, index + 2).iterator();
        while (suffixes.hasNext()) {
            Entry e = suffixes.next();
            this.suffix = e.chars.toString();
            addStrings(start, end, this.contractions);
            if (this.unreversedPrefix.length() != 0) {
                addStrings(start, end, this.expansions);
            }
            handleCE32(start, end, e.value);
        }
        this.suffix = null;
    }

    void addExpansions(int start, int end) {
        if (this.unreversedPrefix.length() != 0 || this.suffix != null) {
            addStrings(start, end, this.expansions);
        } else if (this.expansions != null) {
            this.expansions.add(start, end);
        }
    }

    void addStrings(int start, int end, UnicodeSet set) {
        if (set != null) {
            CharSequence s = new StringBuilder(this.unreversedPrefix);
            do {
                s.appendCodePoint(start);
                if (this.suffix != null) {
                    s.append(this.suffix);
                }
                set.add(s);
                s.setLength(this.unreversedPrefix.length());
                start++;
            } while (start <= end);
        }
    }

    private void setPrefix(CharSequence pfx) {
        this.unreversedPrefix.setLength(0);
        this.unreversedPrefix.append(pfx).reverse();
    }

    private void resetPrefix() {
        this.unreversedPrefix.setLength(0);
    }
}
