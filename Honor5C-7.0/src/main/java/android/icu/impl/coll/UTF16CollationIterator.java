package android.icu.impl.coll;

public class UTF16CollationIterator extends CollationIterator {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    protected int limit;
    protected int pos;
    protected CharSequence seq;
    protected int start;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.UTF16CollationIterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.UTF16CollationIterator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.UTF16CollationIterator.<clinit>():void");
    }

    public UTF16CollationIterator(CollationData d) {
        super(d);
    }

    public UTF16CollationIterator(CollationData d, boolean numeric, CharSequence s, int p) {
        super(d, numeric);
        this.seq = s;
        this.start = 0;
        this.pos = p;
        this.limit = s.length();
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!super.equals(other)) {
            return false;
        }
        UTF16CollationIterator o = (UTF16CollationIterator) other;
        if (this.pos - this.start == o.pos - o.start) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        if (-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }

    public void resetToOffset(int newOffset) {
        reset();
        this.pos = this.start + newOffset;
    }

    public int getOffset() {
        return this.pos - this.start;
    }

    public void setText(boolean numeric, CharSequence s, int p) {
        reset(numeric);
        this.seq = s;
        this.start = 0;
        this.pos = p;
        this.limit = s.length();
    }

    public int nextCodePoint() {
        if (this.pos == this.limit) {
            return -1;
        }
        CharSequence charSequence = this.seq;
        int i = this.pos;
        this.pos = i + 1;
        char c = charSequence.charAt(i);
        if (Character.isHighSurrogate(c) && this.pos != this.limit) {
            char trail = this.seq.charAt(this.pos);
            if (Character.isLowSurrogate(trail)) {
                this.pos++;
                return Character.toCodePoint(c, trail);
            }
        }
        return c;
    }

    public int previousCodePoint() {
        if (this.pos == this.start) {
            return -1;
        }
        CharSequence charSequence = this.seq;
        int i = this.pos - 1;
        this.pos = i;
        char c = charSequence.charAt(i);
        if (Character.isLowSurrogate(c) && this.pos != this.start) {
            char lead = this.seq.charAt(this.pos - 1);
            if (Character.isHighSurrogate(lead)) {
                this.pos--;
                return Character.toCodePoint(lead, c);
            }
        }
        return c;
    }

    protected long handleNextCE32() {
        if (this.pos == this.limit) {
            return -4294967104L;
        }
        CharSequence charSequence = this.seq;
        int i = this.pos;
        this.pos = i + 1;
        char c = charSequence.charAt(i);
        return makeCodePointAndCE32Pair(c, this.trie.getFromU16SingleLead(c));
    }

    protected char handleGetTrailSurrogate() {
        if (this.pos == this.limit) {
            return '\u0000';
        }
        char trail = this.seq.charAt(this.pos);
        if (Character.isLowSurrogate(trail)) {
            this.pos++;
        }
        return trail;
    }

    protected void forwardNumCodePoints(int num) {
        while (num > 0 && this.pos != this.limit) {
            CharSequence charSequence = this.seq;
            int i = this.pos;
            this.pos = i + 1;
            num--;
            if (Character.isHighSurrogate(charSequence.charAt(i)) && this.pos != this.limit && Character.isLowSurrogate(this.seq.charAt(this.pos))) {
                this.pos++;
            }
        }
    }

    protected void backwardNumCodePoints(int num) {
        while (num > 0 && this.pos != this.start) {
            CharSequence charSequence = this.seq;
            int i = this.pos - 1;
            this.pos = i;
            num--;
            if (Character.isLowSurrogate(charSequence.charAt(i)) && this.pos != this.start && Character.isHighSurrogate(this.seq.charAt(this.pos - 1))) {
                this.pos--;
            }
        }
    }
}
