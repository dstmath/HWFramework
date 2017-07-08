package android.icu.text;

import android.icu.impl.CharTrie;
import android.icu.impl.ICUBinary;
import android.icu.impl.StringPrepDataReader;
import android.icu.impl.UBiDiProps;
import android.icu.lang.UCharacter;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.VersionInfo;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public final class StringPrep {
    public static final int ALLOW_UNASSIGNED = 1;
    private static final WeakReference<StringPrep>[] CACHE = null;
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
    private static final String[] PROFILE_NAMES = null;
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
            this.value = StringPrep.RFC3491_NAMEPREP;
            this.type = -1;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.StringPrep.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.StringPrep.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.StringPrep.<clinit>():void");
    }

    private char getCodePointValue(int ch) {
        return this.sprepTrie.getCodePointValue(ch);
    }

    private static VersionInfo getVersionInfo(int comp) {
        return VersionInfo.getInstance((comp >> 24) & Opcodes.OP_CONST_CLASS_JUMBO, (comp >> INDEX_TOP) & Opcodes.OP_CONST_CLASS_JUMBO, (comp >> RFC3920_RESOURCEPREP) & Opcodes.OP_CONST_CLASS_JUMBO, comp & Opcodes.OP_CONST_CLASS_JUMBO);
    }

    private static VersionInfo getVersionInfo(byte[] version) {
        if (version.length != TYPE_LIMIT) {
            return null;
        }
        return VersionInfo.getInstance(version[RFC3491_NAMEPREP], version[RFC3530_NFS4_CS_PREP], version[RFC3530_NFS4_CS_PREP_CI], version[RFC3530_NFS4_CIS_PREP]);
    }

    public StringPrep(InputStream inputStream) throws IOException {
        this(ICUBinary.getByteBufferFromInputStreamAndCloseStream(inputStream));
    }

    private StringPrep(ByteBuffer bytes) throws IOException {
        boolean z;
        boolean z2 = true;
        StringPrepDataReader reader = new StringPrepDataReader(bytes);
        this.indexes = reader.readIndexes(INDEX_TOP);
        this.sprepTrie = new CharTrie(bytes, null);
        this.mappingData = reader.read(this.indexes[RFC3530_NFS4_CS_PREP] / RFC3530_NFS4_CS_PREP_CI);
        reader.getDataFormatVersion();
        if ((this.indexes[RFC3920_NODEPREP] & RFC3530_NFS4_CS_PREP) > 0) {
            z = true;
        } else {
            z = false;
        }
        this.doNFKC = z;
        if ((this.indexes[RFC3920_NODEPREP] & RFC3530_NFS4_CS_PREP_CI) <= 0) {
            z2 = false;
        }
        this.checkBiDi = z2;
        this.sprepUniVer = getVersionInfo(reader.getUnicodeVersion());
        this.normCorrVer = getVersionInfo(this.indexes[RFC3530_NFS4_CS_PREP_CI]);
        VersionInfo normUniVer = UCharacter.getUnicodeVersion();
        if (normUniVer.compareTo(this.sprepUniVer) < 0 && normUniVer.compareTo(this.normCorrVer) < 0 && (this.indexes[RFC3920_NODEPREP] & RFC3530_NFS4_CS_PREP) > 0) {
            throw new IOException("Normalization Correction version not supported");
        } else if (this.checkBiDi) {
            this.bdp = UBiDiProps.INSTANCE;
        }
    }

    public static StringPrep getInstance(int profile) {
        Throwable th;
        if (profile < 0 || profile > RFC4518_LDAP_CI) {
            throw new IllegalArgumentException("Bad profile type");
        }
        synchronized (CACHE) {
            try {
                StringPrep instance;
                StringPrep instance2;
                WeakReference<StringPrep> ref = CACHE[profile];
                if (ref != null) {
                    instance = (StringPrep) ref.get();
                } else {
                    instance = null;
                }
                if (instance == null) {
                    try {
                        ByteBuffer bytes = ICUBinary.getRequiredData(PROFILE_NAMES[profile] + ".spp");
                        if (bytes != null) {
                            instance2 = new StringPrep(bytes);
                        } else {
                            instance2 = instance;
                        }
                        if (instance2 != null) {
                            CACHE[profile] = new WeakReference(instance2);
                        }
                    } catch (Throwable e) {
                        throw new ICUUncheckedIOException(e);
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                instance2 = instance;
                return instance2;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    private static final void getValues(char trieWord, Values values) {
        values.reset();
        if (trieWord == '\u0000') {
            values.type = TYPE_LIMIT;
        } else if (trieWord >= '\ufff0') {
            values.type = trieWord - TYPE_THRESHOLD;
        } else {
            values.type = RFC3530_NFS4_CS_PREP;
            if ((trieWord & RFC3530_NFS4_CS_PREP_CI) > 0) {
                values.isIndex = true;
                values.value = trieWord >> RFC3530_NFS4_CS_PREP_CI;
            } else {
                values.isIndex = false;
                values.value = (trieWord << INDEX_TOP) >> INDEX_TOP;
                values.value >>= RFC3530_NFS4_CS_PREP_CI;
            }
            if ((trieWord >> RFC3530_NFS4_CS_PREP_CI) == MAX_INDEX_VALUE) {
                values.type = RFC3530_NFS4_CIS_PREP;
                values.isIndex = false;
                values.value = RFC3491_NAMEPREP;
            }
        }
    }

    private StringBuffer map(UCharacterIterator iter, int options) throws StringPrepParseException {
        Values val = new Values();
        StringBuffer dest = new StringBuffer();
        boolean allowUnassigned = (options & RFC3530_NFS4_CS_PREP) > 0;
        while (true) {
            int ch = iter.nextCodePoint();
            if (ch != -1) {
                getValues(getCodePointValue(ch), val);
                if (val.type == 0 && !allowUnassigned) {
                    break;
                }
                if (val.type == RFC3530_NFS4_CS_PREP) {
                    if (val.isIndex) {
                        int length;
                        int index = val.value;
                        if (index >= this.indexes[RFC3530_NFS4_CIS_PREP] && index < this.indexes[TYPE_LIMIT]) {
                            length = RFC3530_NFS4_CS_PREP;
                        } else if (index >= this.indexes[TYPE_LIMIT] && index < this.indexes[THREE_UCHARS_MAPPING_INDEX_START]) {
                            length = RFC3530_NFS4_CS_PREP_CI;
                        } else if (index < this.indexes[THREE_UCHARS_MAPPING_INDEX_START] || index >= this.indexes[RFC3722_ISCSI]) {
                            int index2 = index + RFC3530_NFS4_CS_PREP;
                            length = this.mappingData[index];
                            index = index2;
                        } else {
                            length = RFC3530_NFS4_CIS_PREP;
                        }
                        dest.append(this.mappingData, index, length);
                    } else {
                        ch -= val.value;
                    }
                } else if (val.type == RFC3530_NFS4_CIS_PREP) {
                }
                UTF16.append(dest, ch);
            } else {
                return dest;
            }
        }
        throw new StringPrepParseException("An unassigned code point was found in the input", RFC3530_NFS4_CIS_PREP, iter.getText(), iter.getIndex());
    }

    private StringBuffer normalize(StringBuffer src) {
        return new StringBuffer(Normalizer.normalize(src.toString(), Normalizer.NFKC, 32));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            int ch = iter.nextCodePoint();
            if (ch == -1) {
                break;
            }
            getValues(getCodePointValue(ch), val);
            int i = val.type;
            if (r0 == RFC3530_NFS4_CS_PREP_CI) {
                break;
            } else if (this.checkBiDi) {
                direction = this.bdp.getClass(ch);
                if (firstCharDir == 23) {
                    firstCharDir = direction;
                }
                if (direction == 0) {
                    leftToRight = true;
                    ltrPos = iter.getIndex() - 1;
                }
                if (direction == RFC3530_NFS4_CS_PREP || direction == RFC4518_LDAP_CI) {
                    rightToLeft = true;
                    rtlPos = iter.getIndex() - 1;
                }
            }
        }
        if (this.checkBiDi) {
            String str;
            String text;
            if (leftToRight && rightToLeft) {
                str = "The input does not conform to the rules for BiDi code points.";
                text = iter.getText();
                if (rtlPos <= ltrPos) {
                    rtlPos = ltrPos;
                }
                throw new StringPrepParseException(str, TYPE_LIMIT, text, rtlPos);
            } else if (rightToLeft && !((firstCharDir == RFC3530_NFS4_CS_PREP || firstCharDir == RFC4518_LDAP_CI) && (direction == RFC3530_NFS4_CS_PREP || direction == RFC4518_LDAP_CI))) {
                str = "The input does not conform to the rules for BiDi code points.";
                text = iter.getText();
                if (rtlPos <= ltrPos) {
                    rtlPos = ltrPos;
                }
                throw new StringPrepParseException(str, TYPE_LIMIT, text, rtlPos);
            }
        }
        return normOut;
    }

    public String prepare(String src, int options) throws StringPrepParseException {
        return prepare(UCharacterIterator.getInstance(src), options).toString();
    }
}
