package ohos.global.icu.impl.coll;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import ohos.global.icu.impl.ICUBinary;
import ohos.global.icu.impl.Trie2_32;
import ohos.global.icu.impl.UCharacterProperty;
import ohos.global.icu.impl.USerializedSet;
import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.ICUException;

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

    static void read(CollationTailoring collationTailoring, ByteBuffer byteBuffer, CollationTailoring collationTailoring2) throws IOException {
        int i;
        CollationData collationData;
        int i2;
        int[] iArr;
        byte[] bArr;
        CollationData collationData2;
        int[] iArr2;
        int i3;
        collationTailoring2.version = ICUBinary.readHeader(byteBuffer, DATA_FORMAT, IS_ACCEPTABLE);
        if (collationTailoring == null || collationTailoring.getUCAVersion() == collationTailoring2.getUCAVersion()) {
            int remaining = byteBuffer.remaining();
            if (remaining >= 8) {
                int i4 = byteBuffer.getInt();
                if (i4 < 2 || remaining < i4 * 4) {
                    throw new ICUException("not enough indexes");
                }
                int[] iArr3 = new int[20];
                iArr3[0] = i4;
                int i5 = 1;
                while (i5 < i4 && i5 < iArr3.length) {
                    iArr3[i5] = byteBuffer.getInt();
                    i5++;
                }
                for (int i6 = i4; i6 < iArr3.length; i6++) {
                    iArr3[i6] = -1;
                }
                if (i4 > iArr3.length) {
                    ICUBinary.skipBytes(byteBuffer, (i4 - iArr3.length) * 4);
                }
                if (i4 > 19) {
                    i = iArr3[19];
                } else {
                    i = i4 > 5 ? iArr3[i4 - 1] : 0;
                }
                if (remaining >= i) {
                    if (collationTailoring == null) {
                        collationData = null;
                    } else {
                        collationData = collationTailoring.data;
                    }
                    int i7 = iArr3[6] - iArr3[5];
                    if (i7 < 4) {
                        ICUBinary.skipBytes(byteBuffer, i7);
                        iArr = new int[0];
                        i2 = 0;
                    } else if (collationData != null) {
                        int i8 = i7 / 4;
                        iArr = ICUBinary.getInts(byteBuffer, i8, i7 & 3);
                        int i9 = 0;
                        while (i9 < i8 && (iArr[(i8 - i9) - 1] & -65536) != 0) {
                            i9++;
                        }
                        i2 = i8 - i9;
                    } else {
                        throw new ICUException("Collation base data must not reorder scripts");
                    }
                    int i10 = iArr3[7] - iArr3[6];
                    if (i10 < 256) {
                        bArr = null;
                    } else if (i2 != 0) {
                        bArr = new byte[256];
                        byteBuffer.get(bArr);
                        i10 -= 256;
                    } else {
                        throw new ICUException("Reordering table without reordering codes");
                    }
                    ICUBinary.skipBytes(byteBuffer, i10);
                    if (collationData == null || collationData.numericPrimary == (((long) iArr3[1]) & 4278190080L)) {
                        int i11 = iArr3[8] - iArr3[7];
                        if (i11 >= 8) {
                            collationTailoring2.ensureOwnedData();
                            collationData2 = collationTailoring2.ownedData;
                            collationData2.base = collationData;
                            collationData2.numericPrimary = ((long) iArr3[1]) & 4278190080L;
                            Trie2_32 createFromSerialized = Trie2_32.createFromSerialized(byteBuffer);
                            collationTailoring2.trie = createFromSerialized;
                            collationData2.trie = createFromSerialized;
                            int serializedLength = collationData2.trie.getSerializedLength();
                            if (serializedLength <= i11) {
                                i11 -= serializedLength;
                            } else {
                                throw new ICUException("Not enough bytes for the mappings trie");
                            }
                        } else if (collationData != null) {
                            collationTailoring2.data = collationData;
                            collationData2 = null;
                        } else {
                            throw new ICUException("Missing collation data mappings");
                        }
                        ICUBinary.skipBytes(byteBuffer, i11);
                        ICUBinary.skipBytes(byteBuffer, iArr3[9] - iArr3[8]);
                        int i12 = iArr3[10] - iArr3[9];
                        if (i12 < 8) {
                            ICUBinary.skipBytes(byteBuffer, i12);
                        } else if (collationData2 != null) {
                            collationData2.ces = ICUBinary.getLongs(byteBuffer, i12 / 8, i12 & 7);
                        } else {
                            throw new ICUException("Tailored ces without tailored trie");
                        }
                        ICUBinary.skipBytes(byteBuffer, iArr3[11] - iArr3[10]);
                        int i13 = iArr3[12] - iArr3[11];
                        if (i13 < 4) {
                            ICUBinary.skipBytes(byteBuffer, i13);
                        } else if (collationData2 != null) {
                            collationData2.ce32s = ICUBinary.getInts(byteBuffer, i13 / 4, i13 & 3);
                        } else {
                            throw new ICUException("Tailored ce32s without tailored trie");
                        }
                        int i14 = iArr3[4];
                        if (i14 >= 0) {
                            if (collationData2 == null || collationData2.ce32s == null) {
                                throw new ICUException("JamoCE32sStart index into non-existent ce32s[]");
                            }
                            collationData2.jamoCE32s = new int[67];
                            System.arraycopy(collationData2.ce32s, i14, collationData2.jamoCE32s, 0, 67);
                        } else if (collationData2 != null) {
                            if (collationData != null) {
                                collationData2.jamoCE32s = collationData.jamoCE32s;
                            } else {
                                throw new ICUException("Missing Jamo CE32s for Hangul processing");
                            }
                        }
                        int i15 = iArr3[13] - iArr3[12];
                        if (i15 >= 4) {
                            int i16 = i15 / 4;
                            if (collationData2 == null) {
                                throw new ICUException("Root elements but no mappings");
                            } else if (i16 > 4) {
                                collationData2.rootElements = new long[i16];
                                int i17 = 0;
                                while (i17 < i16) {
                                    collationData2.rootElements[i17] = ((long) byteBuffer.getInt()) & 4294967295L;
                                    i17++;
                                    iArr3 = iArr3;
                                }
                                iArr2 = iArr3;
                                if (collationData2.rootElements[3] != 83887360) {
                                    throw new ICUException("Common sec/ter weights in base data differ from the hardcoded value");
                                } else if ((collationData2.rootElements[4] >>> 24) >= 69) {
                                    i15 &= 3;
                                } else {
                                    throw new ICUException("[fixed last secondary common byte] is too low");
                                }
                            } else {
                                throw new ICUException("Root elements array too short");
                            }
                        } else {
                            iArr2 = iArr3;
                        }
                        ICUBinary.skipBytes(byteBuffer, i15);
                        int i18 = iArr2[14] - iArr2[13];
                        if (i18 < 2) {
                            ICUBinary.skipBytes(byteBuffer, i18);
                        } else if (collationData2 != null) {
                            collationData2.contexts = ICUBinary.getString(byteBuffer, i18 / 2, i18 & 1);
                        } else {
                            throw new ICUException("Tailored contexts without tailored trie");
                        }
                        int i19 = iArr2[15] - iArr2[14];
                        if (i19 >= 2) {
                            if (collationData2 != null) {
                                if (collationData == null) {
                                    collationTailoring2.unsafeBackwardSet = new UnicodeSet(56320, 57343);
                                    collationData2.nfcImpl.addLcccChars(collationTailoring2.unsafeBackwardSet);
                                } else {
                                    collationTailoring2.unsafeBackwardSet = collationData.unsafeBackwardSet.cloneAsThawed();
                                }
                                USerializedSet uSerializedSet = new USerializedSet();
                                char c = 0;
                                uSerializedSet.getSet(ICUBinary.getChars(byteBuffer, i19 / 2, i19 & 1), 0);
                                int countRanges = uSerializedSet.countRanges();
                                int[] iArr4 = new int[2];
                                int i20 = 0;
                                while (i20 < countRanges) {
                                    uSerializedSet.getRange(i20, iArr4);
                                    collationTailoring2.unsafeBackwardSet.add(iArr4[c], iArr4[1]);
                                    i20++;
                                    c = 0;
                                }
                                int i21 = 65536;
                                int i22 = 55296;
                                while (i22 < 56320) {
                                    if (!collationTailoring2.unsafeBackwardSet.containsNone(i21, i21 + UCharacterProperty.MAX_SCRIPT)) {
                                        collationTailoring2.unsafeBackwardSet.add(i22);
                                    }
                                    i22++;
                                    i21 += 1024;
                                }
                                collationTailoring2.unsafeBackwardSet.freeze();
                                collationData2.unsafeBackwardSet = collationTailoring2.unsafeBackwardSet;
                                i19 = 0;
                            } else {
                                throw new ICUException("Unsafe-backward-set but no mappings");
                            }
                        } else if (collationData2 != null) {
                            if (collationData != null) {
                                collationData2.unsafeBackwardSet = collationData.unsafeBackwardSet;
                            } else {
                                throw new ICUException("Missing unsafe-backward-set");
                            }
                        }
                        ICUBinary.skipBytes(byteBuffer, i19);
                        int i23 = iArr2[16] - iArr2[15];
                        if (collationData2 != null) {
                            collationData2.fastLatinTable = null;
                            collationData2.fastLatinTableHeader = null;
                            if (((iArr2[1] >> 16) & 255) == 2) {
                                if (i23 >= 2) {
                                    char c2 = byteBuffer.getChar();
                                    int i24 = c2 & 255;
                                    collationData2.fastLatinTableHeader = new char[i24];
                                    collationData2.fastLatinTableHeader[0] = c2;
                                    for (int i25 = 1; i25 < i24; i25++) {
                                        collationData2.fastLatinTableHeader[i25] = byteBuffer.getChar();
                                    }
                                    collationData2.fastLatinTable = ICUBinary.getChars(byteBuffer, (i23 / 2) - i24, i23 & 1);
                                    if ((c2 >> '\b') == 2) {
                                        i23 = 0;
                                    } else {
                                        throw new ICUException("Fast-Latin table version differs from version in data header");
                                    }
                                } else if (collationData != null) {
                                    collationData2.fastLatinTable = collationData.fastLatinTable;
                                    collationData2.fastLatinTableHeader = collationData.fastLatinTableHeader;
                                }
                            }
                        }
                        ICUBinary.skipBytes(byteBuffer, i23);
                        int i26 = iArr2[17] - iArr2[16];
                        if (i26 < 2) {
                            i3 = false;
                            if (!(collationData2 == null || collationData == null)) {
                                collationData2.numScripts = collationData.numScripts;
                                collationData2.scriptsIndex = collationData.scriptsIndex;
                                collationData2.scriptStarts = collationData.scriptStarts;
                            }
                        } else if (collationData2 != null) {
                            CharBuffer asCharBuffer = byteBuffer.asCharBuffer();
                            collationData2.numScripts = asCharBuffer.get();
                            int i27 = (i26 / 2) - ((collationData2.numScripts + 1) + 16);
                            if (i27 > 2) {
                                char[] cArr = new char[(collationData2.numScripts + 16)];
                                collationData2.scriptsIndex = cArr;
                                asCharBuffer.get(cArr);
                                char[] cArr2 = new char[i27];
                                collationData2.scriptStarts = cArr2;
                                asCharBuffer.get(cArr2);
                                i3 = false;
                                if (!(collationData2.scriptStarts[0] == 0 && collationData2.scriptStarts[1] == 768 && collationData2.scriptStarts[i27 - 1] == 65280)) {
                                    throw new ICUException("Script order data not valid");
                                }
                            } else {
                                throw new ICUException("Script order data too short");
                            }
                        } else {
                            throw new ICUException("Script order data but no mappings");
                        }
                        ICUBinary.skipBytes(byteBuffer, i26);
                        int i28 = iArr2[18] - iArr2[17];
                        if (i28 >= 256) {
                            if (collationData2 != null) {
                                collationData2.compressibleBytes = new boolean[256];
                                for (int i29 = i3; i29 < 256; i29++) {
                                    collationData2.compressibleBytes[i29] = byteBuffer.get() != 0 ? true : i3;
                                }
                                i28 -= 256;
                            } else {
                                throw new ICUException("Data for compressible primary lead bytes but no mappings");
                            }
                        } else if (collationData2 != null) {
                            if (collationData != null) {
                                collationData2.compressibleBytes = collationData.compressibleBytes;
                            } else {
                                throw new ICUException("Missing data for compressible primary lead bytes");
                            }
                        }
                        ICUBinary.skipBytes(byteBuffer, i28);
                        ICUBinary.skipBytes(byteBuffer, iArr2[19] - iArr2[18]);
                        CollationSettings readOnly = collationTailoring2.settings.readOnly();
                        int i30 = iArr2[1] & 65535;
                        char[] cArr3 = new char[384];
                        int options = CollationFastLatin.getOptions(collationTailoring2.data, readOnly, cArr3);
                        if (i30 != readOnly.options || readOnly.variableTop == 0 || !Arrays.equals(iArr, readOnly.reorderCodes) || options != readOnly.fastLatinOptions || (options >= 0 && !Arrays.equals(cArr3, readOnly.fastLatinPrimaries))) {
                            CollationSettings copyOnWrite = collationTailoring2.settings.copyOnWrite();
                            copyOnWrite.options = i30;
                            copyOnWrite.variableTop = collationTailoring2.data.getLastPrimaryForGroup(copyOnWrite.getMaxVariable() + 4096);
                            if (copyOnWrite.variableTop != 0) {
                                if (i2 != 0) {
                                    copyOnWrite.aliasReordering(collationData, iArr, i2, bArr);
                                }
                                copyOnWrite.fastLatinOptions = CollationFastLatin.getOptions(collationTailoring2.data, copyOnWrite, copyOnWrite.fastLatinPrimaries);
                                return;
                            }
                            throw new ICUException("The maxVariable could not be mapped to a variableTop");
                        }
                        return;
                    }
                    throw new ICUException("Tailoring numeric primary weight differs from base data");
                }
                throw new ICUException("not enough bytes");
            }
            throw new ICUException("not enough bytes");
        }
        throw new ICUException("Tailoring UCA version differs from base data UCA version");
    }

    /* access modifiers changed from: private */
    public static final class IsAcceptable implements ICUBinary.Authenticate {
        private IsAcceptable() {
        }

        @Override // ohos.global.icu.impl.ICUBinary.Authenticate
        public boolean isDataVersionAcceptable(byte[] bArr) {
            return bArr[0] == 5;
        }
    }

    private CollationDataReader() {
    }
}
