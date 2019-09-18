package android.icu.impl.coll;

import android.icu.impl.ICUBinary;
import android.icu.impl.Trie2_32;
import android.icu.impl.USerializedSet;
import android.icu.text.DateTimePatternGenerator;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.ICUException;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;

final class CollationDataReader {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int DATA_FORMAT = 1430482796;
    private static final IsAcceptable IS_ACCEPTABLE = new IsAcceptable();
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

    private static final class IsAcceptable implements ICUBinary.Authenticate {
        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] version) {
            return version[0] == 5;
        }
    }

    static void read(CollationTailoring base, ByteBuffer inBytes, CollationTailoring tailoring) throws IOException {
        int length;
        int reorderCodesLength;
        int[] reorderCodes;
        int reorderRangesLength;
        CollationTailoring collationTailoring = base;
        ByteBuffer byteBuffer = inBytes;
        CollationTailoring collationTailoring2 = tailoring;
        collationTailoring2.version = ICUBinary.readHeader(byteBuffer, DATA_FORMAT, IS_ACCEPTABLE);
        if (collationTailoring == null || base.getUCAVersion() == tailoring.getUCAVersion()) {
            int inLength = inBytes.remaining();
            if (inLength >= 8) {
                int indexesLength = inBytes.getInt();
                if (indexesLength < 2 || inLength < indexesLength * 4) {
                    int i = indexesLength;
                    throw new ICUException("not enough indexes");
                }
                int[] inIndexes = new int[20];
                inIndexes[0] = indexesLength;
                int i2 = 1;
                while (i2 < indexesLength && i2 < inIndexes.length) {
                    inIndexes[i2] = inBytes.getInt();
                    i2++;
                }
                for (int i3 = indexesLength; i3 < inIndexes.length; i3++) {
                    inIndexes[i3] = -1;
                }
                if (indexesLength > inIndexes.length) {
                    ICUBinary.skipBytes(byteBuffer, (indexesLength - inIndexes.length) * 4);
                }
                if (indexesLength > 19) {
                    length = inIndexes[19];
                } else if (indexesLength > 5) {
                    length = inIndexes[indexesLength - 1];
                } else {
                    length = 0;
                }
                if (inLength >= length) {
                    CollationData baseData = collationTailoring == null ? null : collationTailoring.data;
                    int length2 = inIndexes[5 + 1] - inIndexes[5];
                    if (length2 < 4) {
                        reorderCodes = new int[0];
                        reorderCodesLength = 0;
                        ICUBinary.skipBytes(byteBuffer, length2);
                    } else if (baseData != null) {
                        int reorderCodesLength2 = length2 / 4;
                        reorderCodes = ICUBinary.getInts(byteBuffer, reorderCodesLength2, length2 & 3);
                        int reorderRangesLength2 = 0;
                        while (true) {
                            reorderRangesLength = reorderRangesLength2;
                            if (reorderRangesLength >= reorderCodesLength2 || (reorderCodes[(reorderCodesLength2 - reorderRangesLength) - 1] & -65536) == 0) {
                                reorderCodesLength = reorderCodesLength2 - reorderRangesLength;
                            } else {
                                reorderRangesLength2 = reorderRangesLength + 1;
                            }
                        }
                        reorderCodesLength = reorderCodesLength2 - reorderRangesLength;
                    } else {
                        throw new ICUException("Collation base data must not reorder scripts");
                    }
                    byte[] reorderTable = null;
                    int length3 = inIndexes[6 + 1] - inIndexes[6];
                    if (length3 >= 256) {
                        if (reorderCodesLength != 0) {
                            reorderTable = new byte[256];
                            byteBuffer.get(reorderTable);
                            length3 -= 256;
                        } else {
                            throw new ICUException("Reordering table without reordering codes");
                        }
                    }
                    ICUBinary.skipBytes(byteBuffer, length3);
                    if (baseData != null) {
                        int i4 = indexesLength;
                        int i5 = length3;
                        if (baseData.numericPrimary != (((long) inIndexes[1]) & 4278190080L)) {
                            throw new ICUException("Tailoring numeric primary weight differs from base data");
                        }
                    } else {
                        int i6 = length3;
                    }
                    CollationData data = null;
                    int length4 = inIndexes[7 + 1] - inIndexes[7];
                    if (length4 >= 8) {
                        tailoring.ensureOwnedData();
                        data = collationTailoring2.ownedData;
                        data.base = baseData;
                        data.numericPrimary = ((long) inIndexes[1]) & 4278190080L;
                        Trie2_32 createFromSerialized = Trie2_32.createFromSerialized(inBytes);
                        collationTailoring2.trie = createFromSerialized;
                        data.trie = createFromSerialized;
                        int trieLength = data.trie.getSerializedLength();
                        if (trieLength <= length4) {
                            length4 -= trieLength;
                        } else {
                            throw new ICUException("Not enough bytes for the mappings trie");
                        }
                    } else if (baseData != null) {
                        collationTailoring2.data = baseData;
                    } else {
                        throw new ICUException("Missing collation data mappings");
                    }
                    ICUBinary.skipBytes(byteBuffer, length4);
                    ICUBinary.skipBytes(byteBuffer, inIndexes[8 + 1] - inIndexes[8]);
                    int length5 = inIndexes[9 + 1] - inIndexes[9];
                    if (length5 < 8) {
                        ICUBinary.skipBytes(byteBuffer, length5);
                    } else if (data != null) {
                        data.ces = ICUBinary.getLongs(byteBuffer, length5 / 8, length5 & 7);
                    } else {
                        throw new ICUException("Tailored ces without tailored trie");
                    }
                    ICUBinary.skipBytes(byteBuffer, inIndexes[10 + 1] - inIndexes[10]);
                    int length6 = inIndexes[11 + 1] - inIndexes[11];
                    if (length6 < 4) {
                        ICUBinary.skipBytes(byteBuffer, length6);
                    } else if (data != null) {
                        data.ce32s = ICUBinary.getInts(byteBuffer, length6 / 4, length6 & 3);
                    } else {
                        throw new ICUException("Tailored ce32s without tailored trie");
                    }
                    int jamoCE32sStart = inIndexes[4];
                    if (jamoCE32sStart < 0) {
                        if (data != null) {
                            if (baseData != null) {
                                data.jamoCE32s = baseData.jamoCE32s;
                            } else {
                                int i7 = jamoCE32sStart;
                                throw new ICUException("Missing Jamo CE32s for Hangul processing");
                            }
                        }
                    } else if (data == null || data.ce32s == null) {
                        throw new ICUException("JamoCE32sStart index into non-existent ce32s[]");
                    } else {
                        data.jamoCE32s = new int[67];
                        int i8 = inLength;
                        System.arraycopy(data.ce32s, jamoCE32sStart, data.jamoCE32s, 0, 67);
                    }
                    int length7 = inIndexes[12 + 1] - inIndexes[12];
                    if (length7 >= 4) {
                        int rootElementsLength = length7 / 4;
                        if (data == null) {
                            throw new ICUException("Root elements but no mappings");
                        } else if (rootElementsLength > 4) {
                            data.rootElements = new long[rootElementsLength];
                            int i9 = 0;
                            while (i9 < rootElementsLength) {
                                data.rootElements[i9] = ((long) inBytes.getInt()) & 4294967295L;
                                i9++;
                                jamoCE32sStart = jamoCE32sStart;
                            }
                            if (data.rootElements[3] != 83887360) {
                                throw new ICUException("Common sec/ter weights in base data differ from the hardcoded value");
                            } else if ((data.rootElements[4] >>> 24) >= 69) {
                                length7 &= 3;
                            } else {
                                throw new ICUException("[fixed last secondary common byte] is too low");
                            }
                        } else {
                            throw new ICUException("Root elements array too short");
                        }
                    }
                    ICUBinary.skipBytes(byteBuffer, length7);
                    int length8 = inIndexes[13 + 1] - inIndexes[13];
                    if (length8 < 2) {
                        ICUBinary.skipBytes(byteBuffer, length8);
                    } else if (data != null) {
                        data.contexts = ICUBinary.getString(byteBuffer, length8 / 2, length8 & 1);
                    } else {
                        throw new ICUException("Tailored contexts without tailored trie");
                    }
                    int index = 14;
                    int offset = inIndexes[14];
                    int length9 = inIndexes[14 + 1] - offset;
                    if (length9 < 2) {
                        int i10 = offset;
                        if (data != null) {
                            if (baseData != null) {
                                data.unsafeBackwardSet = baseData.unsafeBackwardSet;
                            } else {
                                throw new ICUException("Missing unsafe-backward-set");
                            }
                        }
                    } else if (data != null) {
                        if (baseData == null) {
                            collationTailoring2.unsafeBackwardSet = new UnicodeSet((int) UTF16.TRAIL_SURROGATE_MIN_VALUE, 57343);
                            data.nfcImpl.addLcccChars(collationTailoring2.unsafeBackwardSet);
                        } else {
                            collationTailoring2.unsafeBackwardSet = baseData.unsafeBackwardSet.cloneAsThawed();
                        }
                        USerializedSet sset = new USerializedSet();
                        int length10 = 0;
                        sset.getSet(ICUBinary.getChars(byteBuffer, length9 / 2, length9 & 1), 0);
                        int count = sset.countRanges();
                        int[] range = new int[2];
                        int i11 = 0;
                        while (i11 < count) {
                            sset.getRange(i11, range);
                            collationTailoring2.unsafeBackwardSet.add(range[0], range[1]);
                            i11++;
                            index = index;
                            offset = offset;
                            length10 = length10;
                        }
                        int i12 = offset;
                        int length11 = length10;
                        int c = 65536;
                        int lead = 55296;
                        while (lead < 56320) {
                            if (!collationTailoring2.unsafeBackwardSet.containsNone(c, c + Opcodes.OP_NEW_INSTANCE_JUMBO)) {
                                collationTailoring2.unsafeBackwardSet.add(lead);
                            }
                            lead++;
                            c += 1024;
                        }
                        collationTailoring2.unsafeBackwardSet.freeze();
                        data.unsafeBackwardSet = collationTailoring2.unsafeBackwardSet;
                        length9 = length11;
                    } else {
                        int i13 = offset;
                        throw new ICUException("Unsafe-backward-set but no mappings");
                    }
                    ICUBinary.skipBytes(byteBuffer, length9);
                    int length12 = inIndexes[15 + 1] - inIndexes[15];
                    if (data != null) {
                        data.fastLatinTable = null;
                        data.fastLatinTableHeader = null;
                        if (((inIndexes[1] >> 16) & 255) == 2) {
                            if (length12 >= 2) {
                                char header0 = inBytes.getChar();
                                int headerLength = header0 & 255;
                                data.fastLatinTableHeader = new char[headerLength];
                                data.fastLatinTableHeader[0] = header0;
                                for (int i14 = 1; i14 < headerLength; i14++) {
                                    data.fastLatinTableHeader[i14] = inBytes.getChar();
                                }
                                data.fastLatinTable = ICUBinary.getChars(byteBuffer, (length12 / 2) - headerLength, length12 & 1);
                                length12 = 0;
                                if ((header0 >> 8) != 2) {
                                    throw new ICUException("Fast-Latin table version differs from version in data header");
                                }
                            } else if (baseData != null) {
                                data.fastLatinTable = baseData.fastLatinTable;
                                data.fastLatinTableHeader = baseData.fastLatinTableHeader;
                            }
                        }
                    }
                    ICUBinary.skipBytes(byteBuffer, length12);
                    int length13 = inIndexes[16 + 1] - inIndexes[16];
                    if (length13 >= 2) {
                        if (data != null) {
                            CharBuffer inChars = inBytes.asCharBuffer();
                            data.numScripts = inChars.get();
                            int scriptStartsLength = (length13 / 2) - ((data.numScripts + 1) + 16);
                            if (scriptStartsLength > 2) {
                                char[] cArr = new char[(data.numScripts + 16)];
                                data.scriptsIndex = cArr;
                                inChars.get(cArr);
                                char[] cArr2 = new char[scriptStartsLength];
                                data.scriptStarts = cArr2;
                                inChars.get(cArr2);
                                if (!(data.scriptStarts[0] == 0 && data.scriptStarts[1] == 768 && data.scriptStarts[scriptStartsLength - 1] == 65280)) {
                                    throw new ICUException("Script order data not valid");
                                }
                            } else {
                                throw new ICUException("Script order data too short");
                            }
                        } else {
                            throw new ICUException("Script order data but no mappings");
                        }
                    } else if (!(data == null || baseData == null)) {
                        data.numScripts = baseData.numScripts;
                        data.scriptsIndex = baseData.scriptsIndex;
                        data.scriptStarts = baseData.scriptStarts;
                    }
                    ICUBinary.skipBytes(byteBuffer, length13);
                    int length14 = inIndexes[17 + 1] - inIndexes[17];
                    if (length14 >= 256) {
                        if (data != null) {
                            data.compressibleBytes = new boolean[256];
                            for (int i15 = 0; i15 < 256; i15++) {
                                data.compressibleBytes[i15] = inBytes.get() != 0;
                            }
                            length14 -= 256;
                        } else {
                            throw new ICUException("Data for compressible primary lead bytes but no mappings");
                        }
                    } else if (data != null) {
                        if (baseData != null) {
                            data.compressibleBytes = baseData.compressibleBytes;
                        } else {
                            throw new ICUException("Missing data for compressible primary lead bytes");
                        }
                    }
                    ICUBinary.skipBytes(byteBuffer, length14);
                    int offset2 = inIndexes[18];
                    ICUBinary.skipBytes(byteBuffer, inIndexes[18 + 1] - offset2);
                    CollationSettings ts = collationTailoring2.settings.readOnly();
                    int options = inIndexes[1] & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
                    char[] fastLatinPrimaries = new char[CollationFastLatin.LATIN_LIMIT];
                    int fastLatinOptions = CollationFastLatin.getOptions(collationTailoring2.data, ts, fastLatinPrimaries);
                    if (options == ts.options) {
                        if (ts.variableTop != 0 && Arrays.equals(reorderCodes, ts.reorderCodes) && fastLatinOptions == ts.fastLatinOptions && (fastLatinOptions < 0 || Arrays.equals(fastLatinPrimaries, ts.fastLatinPrimaries))) {
                            return;
                        }
                    }
                    CollationSettings settings = collationTailoring2.settings.copyOnWrite();
                    settings.options = options;
                    int i16 = offset2;
                    CollationData collationData = data;
                    settings.variableTop = collationTailoring2.data.getLastPrimaryForGroup(4096 + settings.getMaxVariable());
                    if (settings.variableTop != 0) {
                        if (reorderCodesLength != 0) {
                            settings.aliasReordering(baseData, reorderCodes, reorderCodesLength, reorderTable);
                        }
                        settings.fastLatinOptions = CollationFastLatin.getOptions(collationTailoring2.data, settings, settings.fastLatinPrimaries);
                        return;
                    }
                    throw new ICUException("The maxVariable could not be mapped to a variableTop");
                }
                int i17 = indexesLength;
                throw new ICUException("not enough bytes");
            }
            throw new ICUException("not enough bytes");
        }
        throw new ICUException("Tailoring UCA version differs from base data UCA version");
    }

    private CollationDataReader() {
    }
}
