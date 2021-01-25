package ohos.global.icu.text;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import ohos.global.icu.impl.CharTrie;
import ohos.global.icu.impl.ICUBinary;
import ohos.global.icu.impl.StringPrepDataReader;
import ohos.global.icu.impl.UBiDiProps;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.util.ICUUncheckedIOException;
import ohos.global.icu.util.VersionInfo;

public final class StringPrep {
    public static final int ALLOW_UNASSIGNED = 1;
    private static final WeakReference<StringPrep>[] CACHE = new WeakReference[14];
    private static final int CHECK_BIDI_ON = 2;
    public static final int DEFAULT = 0;
    private static final int DELETE = 3;
    private static final int FOUR_UCHARS_MAPPING_INDEX_START = 6;
    private static final int INDEX_MAPPING_DATA_SIZE = 1;
    private static final int INDEX_TOP = 16;
    private static final int MAP = 1;
    private static final int MAX_INDEX_VALUE = 16319;
    private static final int MAX_PROFILE = 13;
    private static final int NORMALIZATION_ON = 1;
    private static final int NORM_CORRECTNS_LAST_UNI_VERSION = 2;
    private static final int ONE_UCHAR_MAPPING_INDEX_START = 3;
    private static final int OPTIONS = 7;
    private static final String[] PROFILE_NAMES = {"rfc3491", "rfc3530cs", "rfc3530csci", "rfc3491", "rfc3530mixp", "rfc3491", "rfc3722", "rfc3920node", "rfc3920res", "rfc4011", "rfc4013", "rfc4505", "rfc4518", "rfc4518ci"};
    private static final int PROHIBITED = 2;
    public static final int RFC3491_NAMEPREP = 0;
    public static final int RFC3530_NFS4_CIS_PREP = 3;
    public static final int RFC3530_NFS4_CS_PREP = 1;
    public static final int RFC3530_NFS4_CS_PREP_CI = 2;
    public static final int RFC3530_NFS4_MIXED_PREP_PREFIX = 4;
    public static final int RFC3530_NFS4_MIXED_PREP_SUFFIX = 5;
    public static final int RFC3722_ISCSI = 6;
    public static final int RFC3920_NODEPREP = 7;
    public static final int RFC3920_RESOURCEPREP = 8;
    public static final int RFC4011_MIB = 9;
    public static final int RFC4013_SASLPREP = 10;
    public static final int RFC4505_TRACE = 11;
    public static final int RFC4518_LDAP = 12;
    public static final int RFC4518_LDAP_CI = 13;
    private static final int THREE_UCHARS_MAPPING_INDEX_START = 5;
    private static final int TWO_UCHARS_MAPPING_INDEX_START = 4;
    private static final int TYPE_LIMIT = 4;
    private static final int TYPE_THRESHOLD = 65520;
    private static final int UNASSIGNED = 0;
    private UBiDiProps bdp;
    private boolean checkBiDi;
    private boolean doNFKC;
    private int[] indexes;
    private char[] mappingData;
    private VersionInfo normCorrVer;
    private CharTrie sprepTrie;
    private VersionInfo sprepUniVer;

    private char getCodePointValue(int i) {
        return this.sprepTrie.getCodePointValue(i);
    }

    private static VersionInfo getVersionInfo(int i) {
        int i2 = i & 255;
        int i3 = (i >> 16) & 255;
        return VersionInfo.getInstance((i >> 24) & 255, i3, (i >> 8) & 255, i2);
    }

    private static VersionInfo getVersionInfo(byte[] bArr) {
        if (bArr.length != 4) {
            return null;
        }
        return VersionInfo.getInstance(bArr[0], bArr[1], bArr[2], bArr[3]);
    }

    public StringPrep(InputStream inputStream) throws IOException {
        this(ICUBinary.getByteBufferFromInputStreamAndCloseStream(inputStream));
    }

    private StringPrep(ByteBuffer byteBuffer) throws IOException {
        StringPrepDataReader stringPrepDataReader = new StringPrepDataReader(byteBuffer);
        this.indexes = stringPrepDataReader.readIndexes(16);
        this.sprepTrie = new CharTrie(byteBuffer, null);
        this.mappingData = stringPrepDataReader.read(this.indexes[1] / 2);
        boolean z = false;
        this.doNFKC = (this.indexes[7] & 1) > 0;
        this.checkBiDi = (this.indexes[7] & 2) > 0 ? true : z;
        this.sprepUniVer = getVersionInfo(stringPrepDataReader.getUnicodeVersion());
        this.normCorrVer = getVersionInfo(this.indexes[2]);
        VersionInfo unicodeVersion = UCharacter.getUnicodeVersion();
        if (unicodeVersion.compareTo(this.sprepUniVer) < 0 && unicodeVersion.compareTo(this.normCorrVer) < 0 && (this.indexes[7] & 1) > 0) {
            throw new IOException("Normalization Correction version not supported");
        } else if (this.checkBiDi) {
            this.bdp = UBiDiProps.INSTANCE;
        }
    }

    public static StringPrep getInstance(int i) {
        if (i < 0 || i > 13) {
            throw new IllegalArgumentException("Bad profile type");
        }
        StringPrep stringPrep = null;
        synchronized (CACHE) {
            WeakReference<StringPrep> weakReference = CACHE[i];
            if (weakReference != null) {
                stringPrep = weakReference.get();
            }
            if (stringPrep == null) {
                ByteBuffer requiredData = ICUBinary.getRequiredData(PROFILE_NAMES[i] + ".spp");
                if (requiredData != null) {
                    try {
                        stringPrep = new StringPrep(requiredData);
                    } catch (IOException e) {
                        throw new ICUUncheckedIOException(e);
                    }
                }
                if (stringPrep != null) {
                    CACHE[i] = new WeakReference<>(stringPrep);
                }
            }
        }
        return stringPrep;
    }

    /* access modifiers changed from: private */
    public static final class Values {
        boolean isIndex;
        int type;
        int value;

        private Values() {
        }

        public void reset() {
            this.isIndex = false;
            this.value = 0;
            this.type = -1;
        }
    }

    private static final void getValues(char c, Values values) {
        values.reset();
        if (c == 0) {
            values.type = 4;
        } else if (c >= TYPE_THRESHOLD) {
            values.type = c - TYPE_THRESHOLD;
        } else {
            values.type = 1;
            if ((c & 2) > 0) {
                values.isIndex = true;
                values.value = c >> 2;
            } else {
                values.isIndex = false;
                values.value = (c << 16) >> 16;
                values.value >>= 2;
            }
            if ((c >> 2) == MAX_INDEX_VALUE) {
                values.type = 3;
                values.isIndex = false;
                values.value = 0;
            }
        }
    }

    private StringBuffer map(UCharacterIterator uCharacterIterator, int i) throws StringPrepParseException {
        char c;
        Values values = new Values();
        StringBuffer stringBuffer = new StringBuffer();
        boolean z = (i & 1) > 0;
        while (true) {
            int nextCodePoint = uCharacterIterator.nextCodePoint();
            if (nextCodePoint == -1) {
                return stringBuffer;
            }
            getValues(getCodePointValue(nextCodePoint), values);
            if (values.type != 0 || z) {
                if (values.type == 1) {
                    if (values.isIndex) {
                        int i2 = values.value;
                        int[] iArr = this.indexes;
                        if (i2 < iArr[3] || i2 >= iArr[4]) {
                            int[] iArr2 = this.indexes;
                            if (i2 < iArr2[4] || i2 >= iArr2[5]) {
                                int[] iArr3 = this.indexes;
                                if (i2 < iArr3[5] || i2 >= iArr3[6]) {
                                    c = this.mappingData[i2];
                                    i2++;
                                } else {
                                    c = 3;
                                }
                            } else {
                                c = 2;
                            }
                        } else {
                            c = 1;
                        }
                        stringBuffer.append(this.mappingData, i2, c);
                    } else {
                        nextCodePoint -= values.value;
                    }
                } else if (values.type == 3) {
                }
                UTF16.append(stringBuffer, nextCodePoint);
            } else {
                throw new StringPrepParseException("An unassigned code point was found in the input", 3, uCharacterIterator.getText(), uCharacterIterator.getIndex());
            }
        }
    }

    private StringBuffer normalize(StringBuffer stringBuffer) {
        return new StringBuffer(Normalizer.normalize(stringBuffer.toString(), Normalizer.NFKC, 32));
    }

    public StringBuffer prepare(UCharacterIterator uCharacterIterator, int i) throws StringPrepParseException {
        StringBuffer map = map(uCharacterIterator, i);
        if (this.doNFKC) {
            map = normalize(map);
        }
        UCharacterIterator instance = UCharacterIterator.getInstance(map);
        Values values = new Values();
        boolean z = false;
        int i2 = -1;
        boolean z2 = false;
        int i3 = -1;
        int i4 = -1;
        int i5 = 23;
        int i6 = 23;
        while (true) {
            int nextCodePoint = instance.nextCodePoint();
            if (nextCodePoint != i2) {
                getValues(getCodePointValue(nextCodePoint), values);
                if (values.type != 2) {
                    if (this.checkBiDi) {
                        i6 = this.bdp.getClass(nextCodePoint);
                        if (i5 == 23) {
                            i5 = i6;
                        }
                        if (i6 == 0) {
                            i4 = instance.getIndex() - 1;
                            z = true;
                        }
                        if (i6 == 1 || i6 == 13) {
                            i3 = instance.getIndex() - 1;
                            z2 = true;
                        }
                    }
                    i2 = -1;
                } else {
                    throw new StringPrepParseException("A prohibited code point was found in the input", 2, instance.getText(), values.value);
                }
            } else {
                if (this.checkBiDi) {
                    if (z && z2) {
                        String text = instance.getText();
                        if (i3 <= i4) {
                            i3 = i4;
                        }
                        throw new StringPrepParseException("The input does not conform to the rules for BiDi code points.", 4, text, i3);
                    } else if (z2 && !((i5 == 1 || i5 == 13) && (i6 == 1 || i6 == 13))) {
                        String text2 = instance.getText();
                        if (i3 <= i4) {
                            i3 = i4;
                        }
                        throw new StringPrepParseException("The input does not conform to the rules for BiDi code points.", 4, text2, i3);
                    }
                }
                return map;
            }
        }
    }

    public String prepare(String str, int i) throws StringPrepParseException {
        return prepare(UCharacterIterator.getInstance(str), i).toString();
    }
}
