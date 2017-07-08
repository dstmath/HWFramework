package org.apache.xml.utils;

import java.lang.reflect.Array;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class FastStringBuffer {
    private static final int CARRY_WS = 4;
    static final boolean DEBUG_FORCE_FIXED_CHUNKSIZE = true;
    static final int DEBUG_FORCE_INIT_BITS = 0;
    static final char[] SINGLE_SPACE = null;
    public static final int SUPPRESS_BOTH = 3;
    public static final int SUPPRESS_LEADING_WS = 1;
    public static final int SUPPRESS_TRAILING_WS = 2;
    char[][] m_array;
    int m_chunkBits;
    int m_chunkMask;
    int m_chunkSize;
    int m_firstFree;
    FastStringBuffer m_innerFSB;
    int m_lastChunk;
    int m_maxChunkBits;
    int m_rebundleBits;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xml.utils.FastStringBuffer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xml.utils.FastStringBuffer.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xml.utils.FastStringBuffer.<clinit>():void");
    }

    public FastStringBuffer(int initChunkBits, int maxChunkBits, int rebundleBits) {
        this.m_chunkBits = 15;
        this.m_maxChunkBits = 15;
        this.m_rebundleBits = SUPPRESS_TRAILING_WS;
        this.m_lastChunk = DEBUG_FORCE_INIT_BITS;
        this.m_firstFree = DEBUG_FORCE_INIT_BITS;
        this.m_innerFSB = null;
        maxChunkBits = initChunkBits;
        this.m_array = new char[16][];
        if (initChunkBits > initChunkBits) {
            this.m_chunkBits = initChunkBits;
            this.m_maxChunkBits = maxChunkBits;
            this.m_rebundleBits = rebundleBits;
            this.m_chunkSize = SUPPRESS_LEADING_WS << initChunkBits;
            this.m_chunkMask = this.m_chunkSize - 1;
            this.m_array[DEBUG_FORCE_INIT_BITS] = new char[this.m_chunkSize];
        } else {
            this.m_chunkBits = initChunkBits;
            this.m_maxChunkBits = maxChunkBits;
            this.m_rebundleBits = rebundleBits;
            this.m_chunkSize = SUPPRESS_LEADING_WS << initChunkBits;
            this.m_chunkMask = this.m_chunkSize - 1;
            this.m_array[DEBUG_FORCE_INIT_BITS] = new char[this.m_chunkSize];
        }
    }

    public FastStringBuffer(int initChunkBits, int maxChunkBits) {
        this(initChunkBits, maxChunkBits, SUPPRESS_TRAILING_WS);
    }

    public FastStringBuffer(int initChunkBits) {
        this(initChunkBits, 15, SUPPRESS_TRAILING_WS);
    }

    public FastStringBuffer() {
        this(10, 15, SUPPRESS_TRAILING_WS);
    }

    public final int size() {
        return (this.m_lastChunk << this.m_chunkBits) + this.m_firstFree;
    }

    public final int length() {
        return (this.m_lastChunk << this.m_chunkBits) + this.m_firstFree;
    }

    public final void reset() {
        this.m_lastChunk = DEBUG_FORCE_INIT_BITS;
        this.m_firstFree = DEBUG_FORCE_INIT_BITS;
        FastStringBuffer innermost = this;
        while (innermost.m_innerFSB != null) {
            innermost = innermost.m_innerFSB;
        }
        this.m_chunkBits = innermost.m_chunkBits;
        this.m_chunkSize = innermost.m_chunkSize;
        this.m_chunkMask = innermost.m_chunkMask;
        this.m_innerFSB = null;
        this.m_array = (char[][]) Array.newInstance(Character.TYPE, new int[]{16, DEBUG_FORCE_INIT_BITS});
        this.m_array[DEBUG_FORCE_INIT_BITS] = new char[this.m_chunkSize];
    }

    public final void setLength(int l) {
        this.m_lastChunk = l >>> this.m_chunkBits;
        if (this.m_lastChunk != 0 || this.m_innerFSB == null) {
            this.m_firstFree = this.m_chunkMask & l;
            if (this.m_firstFree == 0 && this.m_lastChunk > 0) {
                this.m_lastChunk--;
                this.m_firstFree = this.m_chunkSize;
                return;
            }
            return;
        }
        this.m_innerFSB.setLength(l, this);
    }

    private final void setLength(int l, FastStringBuffer rootFSB) {
        this.m_lastChunk = l >>> this.m_chunkBits;
        if (this.m_lastChunk != 0 || this.m_innerFSB == null) {
            rootFSB.m_chunkBits = this.m_chunkBits;
            rootFSB.m_maxChunkBits = this.m_maxChunkBits;
            rootFSB.m_rebundleBits = this.m_rebundleBits;
            rootFSB.m_chunkSize = this.m_chunkSize;
            rootFSB.m_chunkMask = this.m_chunkMask;
            rootFSB.m_array = this.m_array;
            rootFSB.m_innerFSB = this.m_innerFSB;
            rootFSB.m_lastChunk = this.m_lastChunk;
            rootFSB.m_firstFree = this.m_chunkMask & l;
            return;
        }
        this.m_innerFSB.setLength(l, rootFSB);
    }

    public final String toString() {
        int length = (this.m_lastChunk << this.m_chunkBits) + this.m_firstFree;
        return getString(new StringBuffer(length), DEBUG_FORCE_INIT_BITS, DEBUG_FORCE_INIT_BITS, length).toString();
    }

    public final void append(char value) {
        char[] chunk;
        if (this.m_firstFree < this.m_chunkSize) {
            chunk = this.m_array[this.m_lastChunk];
        } else {
            int i = this.m_array.length;
            if (this.m_lastChunk + SUPPRESS_LEADING_WS == i) {
                char[][] newarray = new char[(i + 16)][];
                System.arraycopy(this.m_array, DEBUG_FORCE_INIT_BITS, newarray, DEBUG_FORCE_INIT_BITS, i);
                this.m_array = newarray;
            }
            char[][] cArr = this.m_array;
            int i2 = this.m_lastChunk + SUPPRESS_LEADING_WS;
            this.m_lastChunk = i2;
            chunk = cArr[i2];
            if (chunk == null) {
                if (this.m_lastChunk == (SUPPRESS_LEADING_WS << this.m_rebundleBits) && this.m_chunkBits < this.m_maxChunkBits) {
                    this.m_innerFSB = new FastStringBuffer(this);
                }
                chunk = new char[this.m_chunkSize];
                this.m_array[this.m_lastChunk] = chunk;
            }
            this.m_firstFree = DEBUG_FORCE_INIT_BITS;
        }
        int i3 = this.m_firstFree;
        this.m_firstFree = i3 + SUPPRESS_LEADING_WS;
        chunk[i3] = value;
    }

    public final void append(String value) {
        if (value != null) {
            int strlen = value.length();
            if (strlen != 0) {
                int copyfrom = DEBUG_FORCE_INIT_BITS;
                char[] chunk = this.m_array[this.m_lastChunk];
                int available = this.m_chunkSize - this.m_firstFree;
                while (strlen > 0) {
                    if (available > strlen) {
                        available = strlen;
                    }
                    value.getChars(copyfrom, copyfrom + available, this.m_array[this.m_lastChunk], this.m_firstFree);
                    strlen -= available;
                    copyfrom += available;
                    if (strlen > 0) {
                        int i = this.m_array.length;
                        if (this.m_lastChunk + SUPPRESS_LEADING_WS == i) {
                            char[][] newarray = new char[(i + 16)][];
                            System.arraycopy(this.m_array, DEBUG_FORCE_INIT_BITS, newarray, DEBUG_FORCE_INIT_BITS, i);
                            this.m_array = newarray;
                        }
                        char[][] cArr = this.m_array;
                        int i2 = this.m_lastChunk + SUPPRESS_LEADING_WS;
                        this.m_lastChunk = i2;
                        if (cArr[i2] == null) {
                            if (this.m_lastChunk == (SUPPRESS_LEADING_WS << this.m_rebundleBits) && this.m_chunkBits < this.m_maxChunkBits) {
                                this.m_innerFSB = new FastStringBuffer(this);
                            }
                            this.m_array[this.m_lastChunk] = new char[this.m_chunkSize];
                        }
                        available = this.m_chunkSize;
                        this.m_firstFree = DEBUG_FORCE_INIT_BITS;
                    }
                }
                this.m_firstFree += available;
            }
        }
    }

    public final void append(StringBuffer value) {
        if (value != null) {
            int strlen = value.length();
            if (strlen != 0) {
                int copyfrom = DEBUG_FORCE_INIT_BITS;
                char[] chunk = this.m_array[this.m_lastChunk];
                int available = this.m_chunkSize - this.m_firstFree;
                while (strlen > 0) {
                    if (available > strlen) {
                        available = strlen;
                    }
                    value.getChars(copyfrom, copyfrom + available, this.m_array[this.m_lastChunk], this.m_firstFree);
                    strlen -= available;
                    copyfrom += available;
                    if (strlen > 0) {
                        int i = this.m_array.length;
                        if (this.m_lastChunk + SUPPRESS_LEADING_WS == i) {
                            char[][] newarray = new char[(i + 16)][];
                            System.arraycopy(this.m_array, DEBUG_FORCE_INIT_BITS, newarray, DEBUG_FORCE_INIT_BITS, i);
                            this.m_array = newarray;
                        }
                        char[][] cArr = this.m_array;
                        int i2 = this.m_lastChunk + SUPPRESS_LEADING_WS;
                        this.m_lastChunk = i2;
                        if (cArr[i2] == null) {
                            if (this.m_lastChunk == (SUPPRESS_LEADING_WS << this.m_rebundleBits) && this.m_chunkBits < this.m_maxChunkBits) {
                                this.m_innerFSB = new FastStringBuffer(this);
                            }
                            this.m_array[this.m_lastChunk] = new char[this.m_chunkSize];
                        }
                        available = this.m_chunkSize;
                        this.m_firstFree = DEBUG_FORCE_INIT_BITS;
                    }
                }
                this.m_firstFree += available;
            }
        }
    }

    public final void append(char[] chars, int start, int length) {
        int strlen = length;
        if (length != 0) {
            int copyfrom = start;
            char[] chunk = this.m_array[this.m_lastChunk];
            int available = this.m_chunkSize - this.m_firstFree;
            while (strlen > 0) {
                if (available > strlen) {
                    available = strlen;
                }
                System.arraycopy(chars, copyfrom, this.m_array[this.m_lastChunk], this.m_firstFree, available);
                strlen -= available;
                copyfrom += available;
                if (strlen > 0) {
                    int i = this.m_array.length;
                    if (this.m_lastChunk + SUPPRESS_LEADING_WS == i) {
                        char[][] newarray = new char[(i + 16)][];
                        System.arraycopy(this.m_array, DEBUG_FORCE_INIT_BITS, newarray, DEBUG_FORCE_INIT_BITS, i);
                        this.m_array = newarray;
                    }
                    char[][] cArr = this.m_array;
                    int i2 = this.m_lastChunk + SUPPRESS_LEADING_WS;
                    this.m_lastChunk = i2;
                    if (cArr[i2] == null) {
                        if (this.m_lastChunk == (SUPPRESS_LEADING_WS << this.m_rebundleBits) && this.m_chunkBits < this.m_maxChunkBits) {
                            this.m_innerFSB = new FastStringBuffer(this);
                        }
                        this.m_array[this.m_lastChunk] = new char[this.m_chunkSize];
                    }
                    available = this.m_chunkSize;
                    this.m_firstFree = DEBUG_FORCE_INIT_BITS;
                }
            }
            this.m_firstFree += available;
        }
    }

    public final void append(FastStringBuffer value) {
        if (value != null) {
            int strlen = value.length();
            if (strlen != 0) {
                int copyfrom = DEBUG_FORCE_INIT_BITS;
                char[] chunk = this.m_array[this.m_lastChunk];
                int available = this.m_chunkSize - this.m_firstFree;
                while (strlen > 0) {
                    if (available > strlen) {
                        available = strlen;
                    }
                    int sourcechunk = ((value.m_chunkSize + copyfrom) - 1) >>> value.m_chunkBits;
                    int sourcecolumn = copyfrom & value.m_chunkMask;
                    int runlength = value.m_chunkSize - sourcecolumn;
                    if (runlength > available) {
                        runlength = available;
                    }
                    System.arraycopy(value.m_array[sourcechunk], sourcecolumn, this.m_array[this.m_lastChunk], this.m_firstFree, runlength);
                    if (runlength != available) {
                        System.arraycopy(value.m_array[sourcechunk + SUPPRESS_LEADING_WS], DEBUG_FORCE_INIT_BITS, this.m_array[this.m_lastChunk], this.m_firstFree + runlength, available - runlength);
                    }
                    strlen -= available;
                    copyfrom += available;
                    if (strlen > 0) {
                        int i = this.m_array.length;
                        if (this.m_lastChunk + SUPPRESS_LEADING_WS == i) {
                            char[][] newarray = new char[(i + 16)][];
                            System.arraycopy(this.m_array, DEBUG_FORCE_INIT_BITS, newarray, DEBUG_FORCE_INIT_BITS, i);
                            this.m_array = newarray;
                        }
                        char[][] cArr = this.m_array;
                        int i2 = this.m_lastChunk + SUPPRESS_LEADING_WS;
                        this.m_lastChunk = i2;
                        if (cArr[i2] == null) {
                            if (this.m_lastChunk == (SUPPRESS_LEADING_WS << this.m_rebundleBits) && this.m_chunkBits < this.m_maxChunkBits) {
                                this.m_innerFSB = new FastStringBuffer(this);
                            }
                            this.m_array[this.m_lastChunk] = new char[this.m_chunkSize];
                        }
                        available = this.m_chunkSize;
                        this.m_firstFree = DEBUG_FORCE_INIT_BITS;
                    }
                }
                this.m_firstFree += available;
            }
        }
    }

    public boolean isWhitespace(int start, int length) {
        int sourcechunk = start >>> this.m_chunkBits;
        int sourcecolumn = start & this.m_chunkMask;
        int available = this.m_chunkSize - sourcecolumn;
        while (length > 0) {
            boolean chunkOK;
            int runlength = length <= available ? length : available;
            if (sourcechunk != 0 || this.m_innerFSB == null) {
                chunkOK = XMLCharacterRecognizer.isWhiteSpace(this.m_array[sourcechunk], sourcecolumn, runlength);
            } else {
                chunkOK = this.m_innerFSB.isWhitespace(sourcecolumn, runlength);
            }
            if (!chunkOK) {
                return false;
            }
            length -= runlength;
            sourcechunk += SUPPRESS_LEADING_WS;
            sourcecolumn = DEBUG_FORCE_INIT_BITS;
            available = this.m_chunkSize;
        }
        return DEBUG_FORCE_FIXED_CHUNKSIZE;
    }

    public String getString(int start, int length) {
        int startColumn = start & this.m_chunkMask;
        int startChunk = start >>> this.m_chunkBits;
        if (startColumn + length >= this.m_chunkMask || this.m_innerFSB != null) {
            return getString(new StringBuffer(length), startChunk, startColumn, length).toString();
        }
        return getOneChunkString(startChunk, startColumn, length);
    }

    protected String getOneChunkString(int startChunk, int startColumn, int length) {
        return new String(this.m_array[startChunk], startColumn, length);
    }

    StringBuffer getString(StringBuffer sb, int start, int length) {
        return getString(sb, start >>> this.m_chunkBits, this.m_chunkMask & start, length);
    }

    StringBuffer getString(StringBuffer sb, int startChunk, int startColumn, int length) {
        int stop = ((startChunk << this.m_chunkBits) + startColumn) + length;
        int stopChunk = stop >>> this.m_chunkBits;
        int stopColumn = stop & this.m_chunkMask;
        for (int i = startChunk; i < stopChunk; i += SUPPRESS_LEADING_WS) {
            if (i != 0 || this.m_innerFSB == null) {
                sb.append(this.m_array[i], startColumn, this.m_chunkSize - startColumn);
            } else {
                this.m_innerFSB.getString(sb, startColumn, this.m_chunkSize - startColumn);
            }
            startColumn = DEBUG_FORCE_INIT_BITS;
        }
        if (stopChunk == 0 && this.m_innerFSB != null) {
            this.m_innerFSB.getString(sb, startColumn, stopColumn - startColumn);
        } else if (stopColumn > startColumn) {
            sb.append(this.m_array[stopChunk], startColumn, stopColumn - startColumn);
        }
        return sb;
    }

    public char charAt(int pos) {
        int startChunk = pos >>> this.m_chunkBits;
        if (startChunk != 0 || this.m_innerFSB == null) {
            return this.m_array[startChunk][this.m_chunkMask & pos];
        }
        return this.m_innerFSB.charAt(this.m_chunkMask & pos);
    }

    public void sendSAXcharacters(ContentHandler ch, int start, int length) throws SAXException {
        int startChunk = start >>> this.m_chunkBits;
        int startColumn = start & this.m_chunkMask;
        if (startColumn + length >= this.m_chunkMask || this.m_innerFSB != null) {
            int stop = start + length;
            int stopChunk = stop >>> this.m_chunkBits;
            int stopColumn = stop & this.m_chunkMask;
            for (int i = startChunk; i < stopChunk; i += SUPPRESS_LEADING_WS) {
                if (i != 0 || this.m_innerFSB == null) {
                    ch.characters(this.m_array[i], startColumn, this.m_chunkSize - startColumn);
                } else {
                    this.m_innerFSB.sendSAXcharacters(ch, startColumn, this.m_chunkSize - startColumn);
                }
                startColumn = DEBUG_FORCE_INIT_BITS;
            }
            if (stopChunk == 0 && this.m_innerFSB != null) {
                this.m_innerFSB.sendSAXcharacters(ch, startColumn, stopColumn - startColumn);
            } else if (stopColumn > startColumn) {
                ch.characters(this.m_array[stopChunk], startColumn, stopColumn - startColumn);
            }
            return;
        }
        ch.characters(this.m_array[startChunk], startColumn, length);
    }

    public int sendNormalizedSAXcharacters(ContentHandler ch, int start, int length) throws SAXException {
        int stateForNextChunk = SUPPRESS_LEADING_WS;
        int stop = start + length;
        int startColumn = start & this.m_chunkMask;
        int stopChunk = stop >>> this.m_chunkBits;
        int stopColumn = stop & this.m_chunkMask;
        for (int i = start >>> this.m_chunkBits; i < stopChunk; i += SUPPRESS_LEADING_WS) {
            if (i != 0 || this.m_innerFSB == null) {
                stateForNextChunk = sendNormalizedSAXcharacters(this.m_array[i], startColumn, this.m_chunkSize - startColumn, ch, stateForNextChunk);
            } else {
                stateForNextChunk = this.m_innerFSB.sendNormalizedSAXcharacters(ch, startColumn, this.m_chunkSize - startColumn);
            }
            startColumn = DEBUG_FORCE_INIT_BITS;
        }
        if (stopChunk == 0 && this.m_innerFSB != null) {
            return this.m_innerFSB.sendNormalizedSAXcharacters(ch, startColumn, stopColumn - startColumn);
        }
        if (stopColumn > startColumn) {
            return sendNormalizedSAXcharacters(this.m_array[stopChunk], startColumn, stopColumn - startColumn, ch, stateForNextChunk | SUPPRESS_TRAILING_WS);
        }
        return stateForNextChunk;
    }

    static int sendNormalizedSAXcharacters(char[] ch, int start, int length, ContentHandler handler, int edgeTreatmentFlags) throws SAXException {
        int i = DEBUG_FORCE_INIT_BITS;
        boolean processingLeadingWhitespace = (edgeTreatmentFlags & SUPPRESS_LEADING_WS) != 0 ? DEBUG_FORCE_FIXED_CHUNKSIZE : false;
        boolean seenWhitespace = (edgeTreatmentFlags & CARRY_WS) != 0 ? DEBUG_FORCE_FIXED_CHUNKSIZE : false;
        int currPos = start;
        int limit = start + length;
        if (processingLeadingWhitespace) {
            while (currPos < limit && XMLCharacterRecognizer.isWhiteSpace(ch[currPos])) {
                currPos += SUPPRESS_LEADING_WS;
            }
            if (currPos == limit) {
                return edgeTreatmentFlags;
            }
        }
        while (currPos < limit) {
            int startNonWhitespace = currPos;
            while (currPos < limit && !XMLCharacterRecognizer.isWhiteSpace(ch[currPos])) {
                currPos += SUPPRESS_LEADING_WS;
            }
            if (startNonWhitespace != currPos) {
                if (seenWhitespace) {
                    handler.characters(SINGLE_SPACE, DEBUG_FORCE_INIT_BITS, SUPPRESS_LEADING_WS);
                    seenWhitespace = false;
                }
                handler.characters(ch, startNonWhitespace, currPos - startNonWhitespace);
            }
            int i2 = currPos;
            while (currPos < limit && XMLCharacterRecognizer.isWhiteSpace(ch[currPos])) {
                currPos += SUPPRESS_LEADING_WS;
            }
            if (i2 != currPos) {
                seenWhitespace = DEBUG_FORCE_FIXED_CHUNKSIZE;
            }
        }
        if (seenWhitespace) {
            i = CARRY_WS;
        }
        return i | (edgeTreatmentFlags & SUPPRESS_TRAILING_WS);
    }

    public static void sendNormalizedSAXcharacters(char[] ch, int start, int length, ContentHandler handler) throws SAXException {
        sendNormalizedSAXcharacters(ch, start, length, handler, SUPPRESS_BOTH);
    }

    public void sendSAXComment(LexicalHandler ch, int start, int length) throws SAXException {
        ch.comment(getString(start, length).toCharArray(), DEBUG_FORCE_INIT_BITS, length);
    }

    private void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
    }

    private FastStringBuffer(FastStringBuffer source) {
        this.m_chunkBits = 15;
        this.m_maxChunkBits = 15;
        this.m_rebundleBits = SUPPRESS_TRAILING_WS;
        this.m_lastChunk = DEBUG_FORCE_INIT_BITS;
        this.m_firstFree = DEBUG_FORCE_INIT_BITS;
        this.m_innerFSB = null;
        this.m_chunkBits = source.m_chunkBits;
        this.m_maxChunkBits = source.m_maxChunkBits;
        this.m_rebundleBits = source.m_rebundleBits;
        this.m_chunkSize = source.m_chunkSize;
        this.m_chunkMask = source.m_chunkMask;
        this.m_array = source.m_array;
        this.m_innerFSB = source.m_innerFSB;
        this.m_lastChunk = source.m_lastChunk - 1;
        this.m_firstFree = source.m_chunkSize;
        source.m_array = new char[16][];
        source.m_innerFSB = this;
        source.m_lastChunk = SUPPRESS_LEADING_WS;
        source.m_firstFree = DEBUG_FORCE_INIT_BITS;
        source.m_chunkBits += this.m_rebundleBits;
        source.m_chunkSize = SUPPRESS_LEADING_WS << source.m_chunkBits;
        source.m_chunkMask = source.m_chunkSize - 1;
    }
}
