package android.icu.text;

import java.util.Iterator;

public class UnicodeSetIterator {
    public static int IS_STRING;
    public int codepoint;
    public int codepointEnd;
    @Deprecated
    protected int endElement;
    private int endRange;
    @Deprecated
    protected int nextElement;
    private int range;
    private UnicodeSet set;
    public String string;
    private Iterator<String> stringIterator;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.UnicodeSetIterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.UnicodeSetIterator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.UnicodeSetIterator.<clinit>():void");
    }

    public UnicodeSetIterator(UnicodeSet set) {
        this.endRange = 0;
        this.range = 0;
        this.stringIterator = null;
        reset(set);
    }

    public UnicodeSetIterator() {
        this.endRange = 0;
        this.range = 0;
        this.stringIterator = null;
        reset(new UnicodeSet());
    }

    public boolean next() {
        int i;
        if (this.nextElement <= this.endElement) {
            i = this.nextElement;
            this.nextElement = i + 1;
            this.codepointEnd = i;
            this.codepoint = i;
            return true;
        } else if (this.range < this.endRange) {
            i = this.range + 1;
            this.range = i;
            loadRange(i);
            i = this.nextElement;
            this.nextElement = i + 1;
            this.codepointEnd = i;
            this.codepoint = i;
            return true;
        } else if (this.stringIterator == null) {
            return false;
        } else {
            this.codepoint = IS_STRING;
            this.string = (String) this.stringIterator.next();
            if (!this.stringIterator.hasNext()) {
                this.stringIterator = null;
            }
            return true;
        }
    }

    public boolean nextRange() {
        if (this.nextElement <= this.endElement) {
            this.codepointEnd = this.endElement;
            this.codepoint = this.nextElement;
            this.nextElement = this.endElement + 1;
            return true;
        } else if (this.range < this.endRange) {
            int i = this.range + 1;
            this.range = i;
            loadRange(i);
            this.codepointEnd = this.endElement;
            this.codepoint = this.nextElement;
            this.nextElement = this.endElement + 1;
            return true;
        } else if (this.stringIterator == null) {
            return false;
        } else {
            this.codepoint = IS_STRING;
            this.string = (String) this.stringIterator.next();
            if (!this.stringIterator.hasNext()) {
                this.stringIterator = null;
            }
            return true;
        }
    }

    public void reset(UnicodeSet uset) {
        this.set = uset;
        reset();
    }

    public void reset() {
        this.endRange = this.set.getRangeCount() - 1;
        this.range = 0;
        this.endElement = -1;
        this.nextElement = 0;
        if (this.endRange >= 0) {
            loadRange(this.range);
        }
        this.stringIterator = null;
        if (this.set.strings != null) {
            this.stringIterator = this.set.strings.iterator();
            if (!this.stringIterator.hasNext()) {
                this.stringIterator = null;
            }
        }
    }

    public String getString() {
        if (this.codepoint != IS_STRING) {
            return UTF16.valueOf(this.codepoint);
        }
        return this.string;
    }

    @Deprecated
    public UnicodeSet getSet() {
        return this.set;
    }

    @Deprecated
    protected void loadRange(int aRange) {
        this.nextElement = this.set.getRangeStart(aRange);
        this.endElement = this.set.getRangeEnd(aRange);
    }
}
