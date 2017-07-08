package android.icu.impl.coll;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Trie2_32;
import android.icu.impl.coll.SharedObject.Reference;
import android.icu.text.UnicodeSet;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import android.icu.util.VersionInfo;
import dalvik.bytecode.Opcodes;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public final class CollationTailoring {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public ULocale actualLocale;
    public CollationData data;
    public Map<Integer, Integer> maxExpansions;
    CollationData ownedData;
    private String rules;
    private UResourceBundle rulesResource;
    public Reference<CollationSettings> settings;
    Trie2_32 trie;
    UnicodeSet unsafeBackwardSet;
    public int version;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationTailoring.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationTailoring.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationTailoring.<clinit>():void");
    }

    CollationTailoring(Reference<CollationSettings> baseSettings) {
        this.actualLocale = ULocale.ROOT;
        this.version = 0;
        if (baseSettings != null) {
            if (!-assertionsDisabled) {
                if ((((CollationSettings) baseSettings.readOnly()).reorderCodes.length == 0 ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            if (!-assertionsDisabled) {
                if ((((CollationSettings) baseSettings.readOnly()).reorderTable == null ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            if (!-assertionsDisabled) {
                if ((((CollationSettings) baseSettings.readOnly()).minHighNoReorder == 0 ? 1 : 0) == 0) {
                    throw new AssertionError();
                }
            }
            this.settings = baseSettings.clone();
            return;
        }
        this.settings = new Reference(new CollationSettings());
    }

    void ensureOwnedData() {
        if (this.ownedData == null) {
            this.ownedData = new CollationData(Norm2AllModes.getNFCInstance().impl);
        }
        this.data = this.ownedData;
    }

    void setRules(String r) {
        Object obj = null;
        if (!-assertionsDisabled) {
            if (this.rules == null && this.rulesResource == null) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        this.rules = r;
    }

    void setRulesResource(UResourceBundle res) {
        Object obj = null;
        if (!-assertionsDisabled) {
            if (this.rules == null && this.rulesResource == null) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        this.rulesResource = res;
    }

    public String getRules() {
        if (this.rules != null) {
            return this.rules;
        }
        if (this.rulesResource != null) {
            return this.rulesResource.getString();
        }
        return XmlPullParser.NO_NAMESPACE;
    }

    static VersionInfo makeBaseVersion(VersionInfo ucaVersion) {
        return VersionInfo.getInstance(VersionInfo.UCOL_BUILDER_VERSION.getMajor(), (ucaVersion.getMajor() << 3) + ucaVersion.getMinor(), ucaVersion.getMilli() << 6, 0);
    }

    void setVersion(int baseVersion, int rulesVersion) {
        int r = (rulesVersion >> 16) & Normalizer2Impl.JAMO_VT;
        int s = (rulesVersion >> 16) & Opcodes.OP_CONST_CLASS_JUMBO;
        int q = rulesVersion & Opcodes.OP_CONST_CLASS_JUMBO;
        this.version = (((VersionInfo.UCOL_BUILDER_VERSION.getMajor() << 24) | (16760832 & baseVersion)) | (((r >> 6) + r) & 16128)) | ((((((s << 3) + (s >> 5)) + ((rulesVersion >> 8) & Opcodes.OP_CONST_CLASS_JUMBO)) + (q << 4)) + (q >> 4)) & Opcodes.OP_CONST_CLASS_JUMBO);
    }

    int getUCAVersion() {
        return ((this.version >> 12) & 4080) | ((this.version >> 14) & 3);
    }
}
