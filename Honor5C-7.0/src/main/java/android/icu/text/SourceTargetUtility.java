package android.icu.text;

import android.icu.lang.CharSequences;
import java.util.HashSet;
import java.util.Set;

class SourceTargetUtility {
    static Normalizer2 NFC;
    static final UnicodeSet NON_STARTERS = null;
    final UnicodeSet sourceCache;
    final Set<String> sourceStrings;
    final Transform<String, String> transform;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.SourceTargetUtility.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.SourceTargetUtility.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.SourceTargetUtility.<clinit>():void");
    }

    public SourceTargetUtility(Transform<String, String> transform) {
        this(transform, null);
    }

    public SourceTargetUtility(Transform<String, String> transform, Normalizer2 normalizer) {
        this.transform = transform;
        if (normalizer != null) {
            this.sourceCache = new UnicodeSet("[:^ccc=0:]");
        } else {
            this.sourceCache = new UnicodeSet();
        }
        this.sourceStrings = new HashSet();
        int i = 0;
        while (i <= UnicodeSet.MAX_VALUE) {
            boolean added = false;
            if (!CharSequences.equals(i, (String) transform.transform(UTF16.valueOf(i)))) {
                this.sourceCache.add(i);
                added = true;
            }
            if (normalizer != null) {
                String d = NFC.getDecomposition(i);
                if (d != null) {
                    if (!d.equals((String) transform.transform(d))) {
                        this.sourceStrings.add(d);
                    }
                    if (!(added || normalizer.isInert(i))) {
                        this.sourceCache.add(i);
                    }
                }
            }
            i++;
        }
        this.sourceCache.freeze();
    }

    public void addSourceTargetSet(Transliterator transliterator, UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet myFilter = transliterator.getFilterAsUnicodeSet(inputFilter);
        UnicodeSet<String> affectedCharacters = new UnicodeSet(this.sourceCache).retainAll(myFilter);
        sourceSet.addAll((UnicodeSet) affectedCharacters);
        for (String s : affectedCharacters) {
            targetSet.addAll((CharSequence) this.transform.transform(s));
        }
        for (CharSequence s2 : this.sourceStrings) {
            if (myFilter.containsAll((String) s2)) {
                CharSequence t = (String) this.transform.transform(s2);
                if (!s2.equals(t)) {
                    targetSet.addAll(t);
                    sourceSet.addAll(s2);
                }
            }
        }
    }
}
