package android.icu.text;

import android.icu.lang.UCharacter;
import android.icu.lang.UScript;
import android.icu.text.SpoofChecker.RestrictionLevel;
import dalvik.bytecode.Opcodes;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

@Deprecated
public class IdentifierInfo {
    private static final UnicodeSet ASCII = null;
    @Deprecated
    public static final Comparator<BitSet> BITSET_COMPARATOR = null;
    private static final BitSet CHINESE = null;
    private static final BitSet CONFUSABLE_WITH_LATIN = null;
    private static final BitSet JAPANESE = null;
    private static final BitSet KOREAN = null;
    private final BitSet commonAmongAlternates;
    private String identifier;
    private final UnicodeSet identifierProfile;
    private final UnicodeSet numerics;
    private final BitSet requiredScripts;
    private final Set<BitSet> scriptSetSet;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.IdentifierInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.IdentifierInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.IdentifierInfo.<clinit>():void");
    }

    @Deprecated
    public IdentifierInfo() {
        this.requiredScripts = new BitSet();
        this.scriptSetSet = new HashSet();
        this.commonAmongAlternates = new BitSet();
        this.numerics = new UnicodeSet();
        this.identifierProfile = new UnicodeSet(0, (int) UnicodeSet.MAX_VALUE);
    }

    private IdentifierInfo clear() {
        this.requiredScripts.clear();
        this.scriptSetSet.clear();
        this.numerics.clear();
        this.commonAmongAlternates.clear();
        return this;
    }

    @Deprecated
    public IdentifierInfo setIdentifierProfile(UnicodeSet identifierProfile) {
        this.identifierProfile.set(identifierProfile);
        return this;
    }

    @Deprecated
    public UnicodeSet getIdentifierProfile() {
        return new UnicodeSet(this.identifierProfile);
    }

    @Deprecated
    public IdentifierInfo setIdentifier(String identifier) {
        this.identifier = identifier;
        clear();
        BitSet scriptsForCP = new BitSet();
        int i = 0;
        while (i < identifier.length()) {
            int cp = Character.codePointAt(identifier, i);
            if (UCharacter.getType(cp) == 9) {
                this.numerics.add(cp - UCharacter.getNumericValue(cp));
            }
            UScript.getScriptExtensions(cp, scriptsForCP);
            scriptsForCP.clear(0);
            scriptsForCP.clear(1);
            switch (scriptsForCP.cardinality()) {
                case XmlPullParser.START_DOCUMENT /*0*/:
                    break;
                case NodeFilter.SHOW_ELEMENT /*1*/:
                    this.requiredScripts.or(scriptsForCP);
                    break;
                default:
                    if (!this.requiredScripts.intersects(scriptsForCP) && this.scriptSetSet.add(scriptsForCP)) {
                        scriptsForCP = new BitSet();
                        break;
                    }
            }
            i += Character.charCount(cp);
        }
        if (this.scriptSetSet.size() > 0) {
            this.commonAmongAlternates.set(0, Opcodes.OP_SUB_FLOAT);
            Iterator<BitSet> it = this.scriptSetSet.iterator();
            while (it.hasNext()) {
                BitSet next = (BitSet) it.next();
                if (this.requiredScripts.intersects(next)) {
                    it.remove();
                } else {
                    this.commonAmongAlternates.and(next);
                    for (BitSet other : this.scriptSetSet) {
                        if (next != other && contains(next, other)) {
                            it.remove();
                        }
                    }
                }
            }
        }
        if (this.scriptSetSet.size() == 0) {
            this.commonAmongAlternates.clear();
        }
        return this;
    }

    @Deprecated
    public String getIdentifier() {
        return this.identifier;
    }

    @Deprecated
    public BitSet getScripts() {
        return (BitSet) this.requiredScripts.clone();
    }

    @Deprecated
    public Set<BitSet> getAlternates() {
        Set<BitSet> result = new HashSet();
        for (BitSet item : this.scriptSetSet) {
            result.add((BitSet) item.clone());
        }
        return result;
    }

    @Deprecated
    public UnicodeSet getNumerics() {
        return new UnicodeSet(this.numerics);
    }

    @Deprecated
    public BitSet getCommonAmongAlternates() {
        return (BitSet) this.commonAmongAlternates.clone();
    }

    @Deprecated
    public RestrictionLevel getRestrictionLevel() {
        int i = 1;
        if (!this.identifierProfile.containsAll(this.identifier) || getNumerics().size() > 1) {
            return RestrictionLevel.UNRESTRICTIVE;
        }
        if (ASCII.containsAll(this.identifier)) {
            return RestrictionLevel.ASCII;
        }
        int cardinality = this.requiredScripts.cardinality();
        if (this.commonAmongAlternates.cardinality() == 0) {
            i = this.scriptSetSet.size();
        }
        int cardinalityPlus = cardinality + i;
        if (cardinalityPlus < 2) {
            return RestrictionLevel.SINGLE_SCRIPT_RESTRICTIVE;
        }
        if (containsWithAlternates(JAPANESE, this.requiredScripts) || containsWithAlternates(CHINESE, this.requiredScripts) || containsWithAlternates(KOREAN, this.requiredScripts)) {
            return RestrictionLevel.HIGHLY_RESTRICTIVE;
        }
        if (cardinalityPlus == 2 && this.requiredScripts.get(25) && !this.requiredScripts.intersects(CONFUSABLE_WITH_LATIN)) {
            return RestrictionLevel.MODERATELY_RESTRICTIVE;
        }
        return RestrictionLevel.MINIMALLY_RESTRICTIVE;
    }

    @Deprecated
    public int getScriptCount() {
        return this.requiredScripts.cardinality() + (this.commonAmongAlternates.cardinality() == 0 ? this.scriptSetSet.size() : 1);
    }

    @Deprecated
    public String toString() {
        return this.identifier + ", " + this.identifierProfile.toPattern(false) + ", " + getRestrictionLevel() + ", " + displayScripts(this.requiredScripts) + ", " + displayAlternates(this.scriptSetSet) + ", " + this.numerics.toPattern(false);
    }

    private boolean containsWithAlternates(BitSet container, BitSet containee) {
        if (!contains(container, containee)) {
            return false;
        }
        for (BitSet alternatives : this.scriptSetSet) {
            if (!container.intersects(alternatives)) {
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public static String displayAlternates(Set<BitSet> alternates) {
        if (alternates.size() == 0) {
            return XmlPullParser.NO_NAMESPACE;
        }
        StringBuilder result = new StringBuilder();
        Set<BitSet> sorted = new TreeSet(BITSET_COMPARATOR);
        sorted.addAll(alternates);
        for (BitSet item : sorted) {
            if (result.length() != 0) {
                result.append("; ");
            }
            result.append(displayScripts(item));
        }
        return result.toString();
    }

    @Deprecated
    public static String displayScripts(BitSet scripts) {
        StringBuilder result = new StringBuilder();
        int i = scripts.nextSetBit(0);
        while (i >= 0) {
            if (result.length() != 0) {
                result.append(' ');
            }
            result.append(UScript.getShortName(i));
            i = scripts.nextSetBit(i + 1);
        }
        return result.toString();
    }

    @Deprecated
    public static BitSet parseScripts(String scriptsString) {
        BitSet result = new BitSet();
        for (String item : scriptsString.trim().split(",?\\s+")) {
            if (item.length() != 0) {
                result.set(UScript.getCodeFromName(item));
            }
        }
        return result;
    }

    @Deprecated
    public static Set<BitSet> parseAlternates(String scriptsSetString) {
        Set<BitSet> result = new HashSet();
        for (String item : scriptsSetString.trim().split("\\s*;\\s*")) {
            if (item.length() != 0) {
                result.add(parseScripts(item));
            }
        }
        return result;
    }

    @Deprecated
    public static final boolean contains(BitSet container, BitSet containee) {
        int i = containee.nextSetBit(0);
        while (i >= 0) {
            if (!container.get(i)) {
                return false;
            }
            i = containee.nextSetBit(i + 1);
        }
        return true;
    }

    @Deprecated
    public static final BitSet set(BitSet bitset, int... values) {
        for (int value : values) {
            bitset.set(value);
        }
        return bitset;
    }
}
