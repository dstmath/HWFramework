package android.icu.text;

import android.icu.impl.CharTrie;
import android.icu.impl.ICUBinary;
import android.icu.impl.StringPrepDataReader;
import android.icu.impl.UBiDiProps;
import android.icu.lang.UCharacter;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.VersionInfo;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

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

    private static final class Values {
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

    private char getCodePointValue(int ch) {
        return this.sprepTrie.getCodePointValue(ch);
    }

    private static VersionInfo getVersionInfo(int comp) {
        return VersionInfo.getInstance((comp >> 24) & 255, (comp >> 16) & 255, (comp >> 8) & 255, comp & 255);
    }

    private static VersionInfo getVersionInfo(byte[] version) {
        if (version.length != 4) {
            return null;
        }
        return VersionInfo.getInstance(version[0], version[1], version[2], version[3]);
    }

    public StringPrep(InputStream inputStream) throws IOException {
        this(ICUBinary.getByteBufferFromInputStreamAndCloseStream(inputStream));
    }

    private StringPrep(ByteBuffer bytes) throws IOException {
        StringPrepDataReader reader = new StringPrepDataReader(bytes);
        this.indexes = reader.readIndexes(16);
        this.sprepTrie = new CharTrie(bytes, null);
        this.mappingData = reader.read(this.indexes[1] / 2);
        boolean z = false;
        this.doNFKC = (this.indexes[7] & 1) > 0;
        this.checkBiDi = (this.indexes[7] & 2) > 0 ? true : z;
        this.sprepUniVer = getVersionInfo(reader.getUnicodeVersion());
        this.normCorrVer = getVersionInfo(this.indexes[2]);
        VersionInfo normUniVer = UCharacter.getUnicodeVersion();
        if (normUniVer.compareTo(this.sprepUniVer) < 0 && normUniVer.compareTo(this.normCorrVer) < 0 && (1 & this.indexes[7]) > 0) {
            throw new IOException("Normalization Correction version not supported");
        } else if (this.checkBiDi) {
            this.bdp = UBiDiProps.INSTANCE;
        }
    }

    public static StringPrep getInstance(int profile) {
        if (profile < 0 || profile > 13) {
            throw new IllegalArgumentException("Bad profile type");
        }
        StringPrep instance = null;
        synchronized (CACHE) {
            WeakReference<StringPrep> ref = CACHE[profile];
            if (ref != null) {
                instance = (StringPrep) ref.get();
            }
            if (instance == null) {
                ByteBuffer bytes = ICUBinary.getRequiredData(PROFILE_NAMES[profile] + ".spp");
                if (bytes != null) {
                    try {
                        instance = new StringPrep(bytes);
                    } catch (IOException e) {
                        throw new ICUUncheckedIOException((Throwable) e);
                    }
                }
                if (instance != null) {
                    CACHE[profile] = new WeakReference<>(instance);
                }
            }
        }
        return instance;
    }

    private static final void getValues(char trieWord, Values values) {
        values.reset();
        if (trieWord == 0) {
            values.type = 4;
        } else if (trieWord >= TYPE_THRESHOLD) {
            values.type = trieWord - TYPE_THRESHOLD;
        } else {
            values.type = 1;
            if ((trieWord & 2) > 0) {
                values.isIndex = true;
                values.value = trieWord >> 2;
            } else {
                values.isIndex = false;
                values.value = (trieWord << 16) >> 16;
                values.value >>= 2;
            }
            if ((trieWord >> 2) == MAX_INDEX_VALUE) {
                values.type = 3;
                values.isIndex = false;
                values.value = 0;
            }
        }
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r7v9, types: [char] */
    private StringBuffer map(UCharacterIterator iter, int options) throws StringPrepParseException {
        int length;
        Values val = new Values();
        StringBuffer dest = new StringBuffer();
        boolean allowUnassigned = (options & 1) > 0;
        while (true) {
            int nextCodePoint = iter.nextCodePoint();
            int ch = nextCodePoint;
            if (nextCodePoint == -1) {
                return dest;
            }
            getValues(getCodePointValue(ch), val);
            if (val.type != 0 || allowUnassigned) {
                if (val.type == 1) {
                    if (val.isIndex) {
                        int index = val.value;
                        if (index >= this.indexes[3] && index < this.indexes[4]) {
                            length = 1;
                        } else if (index >= this.indexes[4] && index < this.indexes[5]) {
                            length = 2;
                        } else if (index < this.indexes[5] || index >= this.indexes[6]) {
                            length = this.mappingData[index];
                            index++;
                        } else {
                            length = 3;
                        }
                        dest.append(this.mappingData, index, length);
                    } else {
                        ch -= val.value;
                    }
                } else if (val.type == 3) {
                }
                UTF16.append(dest, ch);
            } else {
                throw new StringPrepParseException("An unassigned code point was found in the input", 3, iter.getText(), iter.getIndex());
            }
        }
    }

    private StringBuffer normalize(StringBuffer src) {
        return new StringBuffer(Normalizer.normalize(src.toString(), Normalizer.NFKC, 32));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0092, code lost:
        if (r6 == 13) goto L_0x0097;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0099, code lost:
        if (r5 != r4) goto L_0x009b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x009b, code lost:
        r11 = r3.getText();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a1, code lost:
        if (r7 <= r8) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00a3, code lost:
        r13 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00a5, code lost:
        r13 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00ab, code lost:
        throw new android.icu.text.StringPrepParseException("The input does not conform to the rules for BiDi code points.", 4, r11, r13);
     */
    public StringBuffer prepare(UCharacterIterator src, int options) throws StringPrepParseException {
        StringBuffer mapOut = map(src, options);
        StringBuffer normOut = mapOut;
        if (this.doNFKC) {
            normOut = normalize(mapOut);
        }
        UCharacterIterator iter = UCharacterIterator.getInstance(normOut);
        Values val = new Values();
        int direction = 23;
        int firstCharDir = 23;
        int rtlPos = -1;
        int ltrPos = -1;
        boolean rightToLeft = false;
        boolean leftToRight = false;
        while (true) {
            int nextCodePoint = iter.nextCodePoint();
            int ch = nextCodePoint;
            if (nextCodePoint != -1) {
                getValues(getCodePointValue(ch), val);
                if (val.type == 2) {
                    StringBuffer stringBuffer = mapOut;
                    Values values = val;
                    throw new StringPrepParseException("A prohibited code point was found in the input", 2, iter.getText(), val.value);
                } else if (this.checkBiDi) {
                    direction = this.bdp.getClass(ch);
                    if (firstCharDir == 23) {
                        firstCharDir = direction;
                    }
                    if (direction == 0) {
                        leftToRight = true;
                        ltrPos = iter.getIndex() - 1;
                    }
                    if (direction == 1 || direction == 13) {
                        rightToLeft = true;
                        rtlPos = iter.getIndex() - 1;
                    }
                }
            } else {
                Values values2 = val;
                if (this.checkBiDi) {
                    if (leftToRight && rightToLeft) {
                        throw new StringPrepParseException("The input does not conform to the rules for BiDi code points.", 4, iter.getText(), rtlPos > ltrPos ? rtlPos : ltrPos);
                    } else if (rightToLeft) {
                        int i = firstCharDir != 1 ? 13 : 13;
                        if (direction != 1) {
                        }
                    }
                }
                return normOut;
            }
        }
    }

    public String prepare(String src, int options) throws StringPrepParseException {
        return prepare(UCharacterIterator.getInstance(src), options).toString();
    }
}
