package android.icu.impl.coll;

import android.icu.impl.ICUBinary;
import android.icu.impl.ICUBinary.Authenticate;
import android.icu.impl.Trie2_32;
import android.icu.impl.USerializedSet;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.ICUException;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import dalvik.system.VMDebug;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import libcore.icu.DateUtilsBridge;
import org.w3c.dom.traversal.NodeFilter;

final class CollationDataReader {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int DATA_FORMAT = 1430482796;
    private static final IsAcceptable IS_ACCEPTABLE = null;
    static final int IX_CE32S_OFFSET = 11;
    static final int IX_CES_OFFSET = 9;
    static final int IX_COMPRESSIBLE_BYTES_OFFSET = 17;
    static final int IX_CONTEXTS_OFFSET = 13;
    static final int IX_FAST_LATIN_TABLE_OFFSET = 15;
    static final int IX_INDEXES_LENGTH = 0;
    static final int IX_JAMO_CE32S_START = 4;
    static final int IX_OPTIONS = 1;
    static final int IX_REORDER_CODES_OFFSET = 5;
    static final int IX_REORDER_TABLE_OFFSET = 6;
    static final int IX_RESERVED10_OFFSET = 10;
    static final int IX_RESERVED18_OFFSET = 18;
    static final int IX_RESERVED2 = 2;
    static final int IX_RESERVED3 = 3;
    static final int IX_RESERVED8_OFFSET = 8;
    static final int IX_ROOT_ELEMENTS_OFFSET = 12;
    static final int IX_SCRIPTS_OFFSET = 16;
    static final int IX_TOTAL_SIZE = 19;
    static final int IX_TRIE_OFFSET = 7;
    static final int IX_UNSAFE_BWD_OFFSET = 14;

    private static final class IsAcceptable implements Authenticate {
        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] version) {
            return version[CollationDataReader.IX_INDEXES_LENGTH] == CollationDataReader.IX_REORDER_CODES_OFFSET ? true : CollationDataReader.-assertionsDisabled;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationDataReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationDataReader.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationDataReader.<clinit>():void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void read(CollationTailoring base, ByteBuffer inBytes, CollationTailoring tailoring) throws IOException {
        tailoring.version = ICUBinary.readHeader(inBytes, DATA_FORMAT, IS_ACCEPTABLE);
        if (base == null || base.getUCAVersion() == tailoring.getUCAVersion()) {
            int inLength = inBytes.remaining();
            if (inLength < IX_RESERVED8_OFFSET) {
                throw new ICUException("not enough bytes");
            }
            int indexesLength = inBytes.getInt();
            if (indexesLength < IX_RESERVED2 || inLength < indexesLength * IX_JAMO_CE32S_START) {
                throw new ICUException("not enough indexes");
            }
            int length;
            int[] inIndexes = new int[20];
            inIndexes[IX_INDEXES_LENGTH] = indexesLength;
            int i = IX_OPTIONS;
            while (i < indexesLength && i < inIndexes.length) {
                inIndexes[i] = inBytes.getInt();
                i += IX_OPTIONS;
            }
            for (i = indexesLength; i < inIndexes.length; i += IX_OPTIONS) {
                inIndexes[i] = -1;
            }
            if (indexesLength > inIndexes.length) {
                ICUBinary.skipBytes(inBytes, (indexesLength - inIndexes.length) * IX_JAMO_CE32S_START);
            }
            if (indexesLength > IX_TOTAL_SIZE) {
                length = inIndexes[IX_TOTAL_SIZE];
            } else if (indexesLength > IX_REORDER_CODES_OFFSET) {
                length = inIndexes[indexesLength - 1];
            } else {
                length = IX_INDEXES_LENGTH;
            }
            if (inLength < length) {
                throw new ICUException("not enough bytes");
            }
            int[] reorderCodes;
            int reorderCodesLength;
            int i2;
            CollationData collationData = base == null ? null : base.data;
            length = inIndexes[IX_REORDER_TABLE_OFFSET] - inIndexes[IX_REORDER_CODES_OFFSET];
            if (length < IX_JAMO_CE32S_START) {
                reorderCodes = new int[IX_INDEXES_LENGTH];
                reorderCodesLength = IX_INDEXES_LENGTH;
                ICUBinary.skipBytes(inBytes, length);
            } else if (collationData == null) {
                throw new ICUException("Collation base data must not reorder scripts");
            } else {
                reorderCodesLength = length / IX_JAMO_CE32S_START;
                reorderCodes = ICUBinary.getInts(inBytes, reorderCodesLength, length & IX_RESERVED3);
                int reorderRangesLength = IX_INDEXES_LENGTH;
                while (reorderRangesLength < reorderCodesLength && (reorderCodes[(reorderCodesLength - reorderRangesLength) - 1] & -65536) != 0) {
                    reorderRangesLength += IX_OPTIONS;
                }
                if (!-assertionsDisabled) {
                    if ((reorderRangesLength < reorderCodesLength ? IX_OPTIONS : null) == null) {
                        throw new AssertionError();
                    }
                }
                reorderCodesLength -= reorderRangesLength;
            }
            byte[] bArr = null;
            length = inIndexes[IX_TRIE_OFFSET] - inIndexes[IX_REORDER_TABLE_OFFSET];
            if (length >= 256) {
                if (reorderCodesLength == 0) {
                    throw new ICUException("Reordering table without reordering codes");
                }
                bArr = new byte[NodeFilter.SHOW_DOCUMENT];
                inBytes.get(bArr);
                length -= 256;
            }
            ICUBinary.skipBytes(inBytes, length);
            if (collationData != null) {
                if (collationData.numericPrimary != (((long) inIndexes[IX_OPTIONS]) & 4278190080L)) {
                    throw new ICUException("Tailoring numeric primary weight differs from base data");
                }
            }
            CollationData collationData2 = null;
            length = inIndexes[IX_RESERVED8_OFFSET] - inIndexes[IX_TRIE_OFFSET];
            if (length >= IX_RESERVED8_OFFSET) {
                tailoring.ensureOwnedData();
                collationData2 = tailoring.ownedData;
                collationData2.base = collationData;
                collationData2.numericPrimary = ((long) inIndexes[IX_OPTIONS]) & 4278190080L;
                Trie2_32 createFromSerialized = Trie2_32.createFromSerialized(inBytes);
                tailoring.trie = createFromSerialized;
                collationData2.trie = createFromSerialized;
                int trieLength = collationData2.trie.getSerializedLength();
                if (trieLength > length) {
                    throw new ICUException("Not enough bytes for the mappings trie");
                }
                length -= trieLength;
            } else if (collationData != null) {
                tailoring.data = collationData;
            } else {
                throw new ICUException("Missing collation data mappings");
            }
            ICUBinary.skipBytes(inBytes, length);
            ICUBinary.skipBytes(inBytes, inIndexes[IX_CES_OFFSET] - inIndexes[IX_RESERVED8_OFFSET]);
            length = inIndexes[IX_RESERVED10_OFFSET] - inIndexes[IX_CES_OFFSET];
            if (length < IX_RESERVED8_OFFSET) {
                ICUBinary.skipBytes(inBytes, length);
            } else if (collationData2 == null) {
                throw new ICUException("Tailored ces without tailored trie");
            } else {
                collationData2.ces = ICUBinary.getLongs(inBytes, length / IX_RESERVED8_OFFSET, length & IX_TRIE_OFFSET);
            }
            ICUBinary.skipBytes(inBytes, inIndexes[IX_CE32S_OFFSET] - inIndexes[IX_RESERVED10_OFFSET]);
            length = inIndexes[IX_ROOT_ELEMENTS_OFFSET] - inIndexes[IX_CE32S_OFFSET];
            if (length < IX_JAMO_CE32S_START) {
                ICUBinary.skipBytes(inBytes, length);
            } else if (collationData2 == null) {
                throw new ICUException("Tailored ce32s without tailored trie");
            } else {
                collationData2.ce32s = ICUBinary.getInts(inBytes, length / IX_JAMO_CE32S_START, length & IX_RESERVED3);
            }
            int jamoCE32sStart = inIndexes[IX_JAMO_CE32S_START];
            if (jamoCE32sStart >= 0) {
                if (collationData2 == null || collationData2.ce32s == null) {
                    throw new ICUException("JamoCE32sStart index into non-existent ce32s[]");
                }
                collationData2.jamoCE32s = new int[67];
                System.arraycopy(collationData2.ce32s, jamoCE32sStart, collationData2.jamoCE32s, IX_INDEXES_LENGTH, 67);
            } else if (collationData2 != null) {
                if (collationData != null) {
                    collationData2.jamoCE32s = collationData.jamoCE32s;
                } else {
                    throw new ICUException("Missing Jamo CE32s for Hangul processing");
                }
            }
            length = inIndexes[IX_CONTEXTS_OFFSET] - inIndexes[IX_ROOT_ELEMENTS_OFFSET];
            if (length >= IX_JAMO_CE32S_START) {
                int rootElementsLength = length / IX_JAMO_CE32S_START;
                if (collationData2 == null) {
                    throw new ICUException("Root elements but no mappings");
                } else if (rootElementsLength <= IX_JAMO_CE32S_START) {
                    throw new ICUException("Root elements array too short");
                } else {
                    collationData2.rootElements = new long[rootElementsLength];
                    for (i = IX_INDEXES_LENGTH; i < rootElementsLength; i += IX_OPTIONS) {
                        collationData2.rootElements[i] = ((long) inBytes.getInt()) & 4294967295L;
                    }
                    if (collationData2.rootElements[IX_RESERVED3] != 83887360) {
                        throw new ICUException("Common sec/ter weights in base data differ from the hardcoded value");
                    }
                    if ((collationData2.rootElements[IX_JAMO_CE32S_START] >>> 24) < 69) {
                        throw new ICUException("[fixed last secondary common byte] is too low");
                    }
                    length &= IX_RESERVED3;
                }
            }
            ICUBinary.skipBytes(inBytes, length);
            length = inIndexes[IX_UNSAFE_BWD_OFFSET] - inIndexes[IX_CONTEXTS_OFFSET];
            if (length < IX_RESERVED2) {
                ICUBinary.skipBytes(inBytes, length);
            } else if (collationData2 == null) {
                throw new ICUException("Tailored contexts without tailored trie");
            } else {
                collationData2.contexts = ICUBinary.getString(inBytes, length / IX_RESERVED2, length & IX_OPTIONS);
            }
            length = inIndexes[IX_FAST_LATIN_TABLE_OFFSET] - inIndexes[IX_UNSAFE_BWD_OFFSET];
            if (length >= IX_RESERVED2) {
                if (collationData2 == null) {
                    throw new ICUException("Unsafe-backward-set but no mappings");
                }
                if (collationData == null) {
                    tailoring.unsafeBackwardSet = new UnicodeSet((int) UTF16.TRAIL_SURROGATE_MIN_VALUE, (int) UTF16.TRAIL_SURROGATE_MAX_VALUE);
                    collationData2.nfcImpl.addLcccChars(tailoring.unsafeBackwardSet);
                } else {
                    tailoring.unsafeBackwardSet = collationData.unsafeBackwardSet.cloneAsThawed();
                }
                USerializedSet sset = new USerializedSet();
                char[] unsafeData = ICUBinary.getChars(inBytes, length / IX_RESERVED2, length & IX_OPTIONS);
                length = IX_INDEXES_LENGTH;
                sset.getSet(unsafeData, IX_INDEXES_LENGTH);
                int count = sset.countRanges();
                int[] range = new int[IX_RESERVED2];
                for (i = IX_INDEXES_LENGTH; i < count; i += IX_OPTIONS) {
                    sset.getRange(i, range);
                    tailoring.unsafeBackwardSet.add(range[IX_INDEXES_LENGTH], range[IX_OPTIONS]);
                }
                int c = DateUtilsBridge.FORMAT_ABBREV_MONTH;
                int lead = UTF16.SURROGATE_MIN_VALUE;
                while (lead < 56320) {
                    if (!tailoring.unsafeBackwardSet.containsNone(c, c + Opcodes.OP_NEW_INSTANCE_JUMBO)) {
                        tailoring.unsafeBackwardSet.add(lead);
                    }
                    lead += IX_OPTIONS;
                    c += NodeFilter.SHOW_DOCUMENT_FRAGMENT;
                }
                tailoring.unsafeBackwardSet.freeze();
                collationData2.unsafeBackwardSet = tailoring.unsafeBackwardSet;
            } else if (collationData2 != null) {
                if (collationData != null) {
                    collationData2.unsafeBackwardSet = collationData.unsafeBackwardSet;
                } else {
                    throw new ICUException("Missing unsafe-backward-set");
                }
            }
            ICUBinary.skipBytes(inBytes, length);
            length = inIndexes[IX_SCRIPTS_OFFSET] - inIndexes[IX_FAST_LATIN_TABLE_OFFSET];
            if (collationData2 != null) {
                collationData2.fastLatinTable = null;
                collationData2.fastLatinTableHeader = null;
                i2 = (inIndexes[IX_OPTIONS] >> IX_SCRIPTS_OFFSET) & Opcodes.OP_CONST_CLASS_JUMBO;
                if (r0 == IX_RESERVED2) {
                    if (length >= IX_RESERVED2) {
                        char header0 = inBytes.getChar();
                        int headerLength = header0 & Opcodes.OP_CONST_CLASS_JUMBO;
                        collationData2.fastLatinTableHeader = new char[headerLength];
                        collationData2.fastLatinTableHeader[IX_INDEXES_LENGTH] = header0;
                        for (i = IX_OPTIONS; i < headerLength; i += IX_OPTIONS) {
                            collationData2.fastLatinTableHeader[i] = inBytes.getChar();
                        }
                        collationData2.fastLatinTable = ICUBinary.getChars(inBytes, (length / IX_RESERVED2) - headerLength, length & IX_OPTIONS);
                        length = IX_INDEXES_LENGTH;
                        if ((header0 >> IX_RESERVED8_OFFSET) != IX_RESERVED2) {
                            throw new ICUException("Fast-Latin table version differs from version in data header");
                        }
                    } else if (collationData != null) {
                        collationData2.fastLatinTable = collationData.fastLatinTable;
                        collationData2.fastLatinTableHeader = collationData.fastLatinTableHeader;
                    }
                }
            }
            ICUBinary.skipBytes(inBytes, length);
            length = inIndexes[IX_COMPRESSIBLE_BYTES_OFFSET] - inIndexes[IX_SCRIPTS_OFFSET];
            if (length >= IX_RESERVED2) {
                if (collationData2 == null) {
                    throw new ICUException("Script order data but no mappings");
                }
                int scriptsLength = length / IX_RESERVED2;
                CharBuffer inChars = inBytes.asCharBuffer();
                collationData2.numScripts = inChars.get();
                int scriptStartsLength = scriptsLength - ((collationData2.numScripts + IX_OPTIONS) + IX_SCRIPTS_OFFSET);
                if (scriptStartsLength <= IX_RESERVED2) {
                    throw new ICUException("Script order data too short");
                }
                char[] cArr = new char[(collationData2.numScripts + IX_SCRIPTS_OFFSET)];
                collationData2.scriptsIndex = cArr;
                inChars.get(cArr);
                cArr = new char[scriptStartsLength];
                collationData2.scriptStarts = cArr;
                inChars.get(cArr);
                if (collationData2.scriptStarts[IX_INDEXES_LENGTH] == '\u0000') {
                    if (collationData2.scriptStarts[IX_OPTIONS] == '\u0300') {
                    }
                }
                throw new ICUException("Script order data not valid");
            } else if (!(collationData2 == null || collationData == null)) {
                collationData2.numScripts = collationData.numScripts;
                collationData2.scriptsIndex = collationData.scriptsIndex;
                collationData2.scriptStarts = collationData.scriptStarts;
            }
            ICUBinary.skipBytes(inBytes, length);
            length = inIndexes[IX_RESERVED18_OFFSET] - inIndexes[IX_COMPRESSIBLE_BYTES_OFFSET];
            if (length >= 256) {
                if (collationData2 == null) {
                    throw new ICUException("Data for compressible primary lead bytes but no mappings");
                }
                collationData2.compressibleBytes = new boolean[NodeFilter.SHOW_DOCUMENT];
                for (i = IX_INDEXES_LENGTH; i < 256; i += IX_OPTIONS) {
                    collationData2.compressibleBytes[i] = inBytes.get() != null ? true : -assertionsDisabled;
                }
                length -= 256;
            } else if (collationData2 != null) {
                if (collationData != null) {
                    collationData2.compressibleBytes = collationData.compressibleBytes;
                } else {
                    throw new ICUException("Missing data for compressible primary lead bytes");
                }
            }
            ICUBinary.skipBytes(inBytes, length);
            ICUBinary.skipBytes(inBytes, inIndexes[IX_TOTAL_SIZE] - inIndexes[IX_RESERVED18_OFFSET]);
            CollationSettings ts = (CollationSettings) tailoring.settings.readOnly();
            int options = inIndexes[IX_OPTIONS] & DexFormat.MAX_TYPE_IDX;
            char[] fastLatinPrimaries = new char[CollationFastLatin.LATIN_LIMIT];
            int fastLatinOptions = CollationFastLatin.getOptions(tailoring.data, ts, fastLatinPrimaries);
            if (options == ts.options) {
                if (ts.variableTop != 0) {
                    if (Arrays.equals(reorderCodes, ts.reorderCodes)) {
                        i2 = ts.fastLatinOptions;
                        if (fastLatinOptions == r0) {
                            if (fastLatinOptions >= 0) {
                            }
                            return;
                        }
                    }
                }
            }
            CollationSettings settings = (CollationSettings) tailoring.settings.copyOnWrite();
            settings.options = options;
            settings.variableTop = tailoring.data.getLastPrimaryForGroup(settings.getMaxVariable() + VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS);
            if (settings.variableTop == 0) {
                throw new ICUException("The maxVariable could not be mapped to a variableTop");
            }
            if (reorderCodesLength != 0) {
                settings.aliasReordering(collationData, reorderCodes, reorderCodesLength, bArr);
            }
            settings.fastLatinOptions = CollationFastLatin.getOptions(tailoring.data, settings, settings.fastLatinPrimaries);
            return;
        }
        throw new ICUException("Tailoring UCA version differs from base data UCA version");
    }

    private CollationDataReader() {
    }
}
