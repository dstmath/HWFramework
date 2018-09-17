package org.apache.xml.utils;

import java.lang.reflect.Array;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class FastStringBuffer {
    private static final int CARRY_WS = 4;
    static final boolean DEBUG_FORCE_FIXED_CHUNKSIZE = true;
    static final int DEBUG_FORCE_INIT_BITS = 0;
    static final char[] SINGLE_SPACE = new char[]{' '};
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

    public FastStringBuffer(int initChunkBits, int maxChunkBits, int rebundleBits) {
        this.m_chunkBits = 15;
        this.m_maxChunkBits = 15;
        this.m_rebundleBits = 2;
        this.m_lastChunk = 0;
        this.m_firstFree = 0;
        this.m_innerFSB = null;
        maxChunkBits = initChunkBits;
        this.m_array = new char[16][];
        this.m_chunkBits = initChunkBits;
        this.m_maxChunkBits = maxChunkBits;
        this.m_rebundleBits = rebundleBits;
        this.m_chunkSize = 1 << initChunkBits;
        this.m_chunkMask = this.m_chunkSize - 1;
        this.m_array[0] = new char[this.m_chunkSize];
    }

    public FastStringBuffer(int initChunkBits, int maxChunkBits) {
        this(initChunkBits, maxChunkBits, 2);
    }

    public FastStringBuffer(int initChunkBits) {
        this(initChunkBits, 15, 2);
    }

    public FastStringBuffer() {
        this(10, 15, 2);
    }

    public final int size() {
        return (this.m_lastChunk << this.m_chunkBits) + this.m_firstFree;
    }

    public final int length() {
        return (this.m_lastChunk << this.m_chunkBits) + this.m_firstFree;
    }

    public final void reset() {
        this.m_lastChunk = 0;
        this.m_firstFree = 0;
        FastStringBuffer innermost = this;
        while (innermost.m_innerFSB != null) {
            innermost = innermost.m_innerFSB;
        }
        this.m_chunkBits = innermost.m_chunkBits;
        this.m_chunkSize = innermost.m_chunkSize;
        this.m_chunkMask = innermost.m_chunkMask;
        this.m_innerFSB = null;
        this.m_array = (char[][]) Array.newInstance(Character.TYPE, new int[]{16, 0});
        this.m_array[0] = new char[this.m_chunkSize];
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
        return getString(new StringBuffer(length), 0, 0, length).toString();
    }

    public final void append(char value) {
        char[] chunk;
        if (this.m_firstFree < this.m_chunkSize) {
            chunk = this.m_array[this.m_lastChunk];
        } else {
            int i = this.m_array.length;
            if (this.m_lastChunk + 1 == i) {
                char[][] newarray = new char[(i + 16)][];
                System.arraycopy(this.m_array, 0, newarray, 0, i);
                this.m_array = newarray;
            }
            char[][] cArr = this.m_array;
            int i2 = this.m_lastChunk + 1;
            this.m_lastChunk = i2;
            chunk = cArr[i2];
            if (chunk == null) {
                if (this.m_lastChunk == (1 << this.m_rebundleBits) && this.m_chunkBits < this.m_maxChunkBits) {
                    this.m_innerFSB = new FastStringBuffer(this);
                }
                chunk = new char[this.m_chunkSize];
                this.m_array[this.m_lastChunk] = chunk;
            }
            this.m_firstFree = 0;
        }
        int i3 = this.m_firstFree;
        this.m_firstFree = i3 + 1;
        chunk[i3] = value;
    }

    public final void append(String value) {
        if (value != null) {
            int strlen = value.length();
            if (strlen != 0) {
                int copyfrom = 0;
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
                        if (this.m_lastChunk + 1 == i) {
                            char[][] newarray = new char[(i + 16)][];
                            System.arraycopy(this.m_array, 0, newarray, 0, i);
                            this.m_array = newarray;
                        }
                        char[][] cArr = this.m_array;
                        int i2 = this.m_lastChunk + 1;
                        this.m_lastChunk = i2;
                        if (cArr[i2] == null) {
                            if (this.m_lastChunk == (1 << this.m_rebundleBits) && this.m_chunkBits < this.m_maxChunkBits) {
                                this.m_innerFSB = new FastStringBuffer(this);
                            }
                            this.m_array[this.m_lastChunk] = new char[this.m_chunkSize];
                        }
                        available = this.m_chunkSize;
                        this.m_firstFree = 0;
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
                int copyfrom = 0;
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
                        if (this.m_lastChunk + 1 == i) {
                            char[][] newarray = new char[(i + 16)][];
                            System.arraycopy(this.m_array, 0, newarray, 0, i);
                            this.m_array = newarray;
                        }
                        char[][] cArr = this.m_array;
                        int i2 = this.m_lastChunk + 1;
                        this.m_lastChunk = i2;
                        if (cArr[i2] == null) {
                            if (this.m_lastChunk == (1 << this.m_rebundleBits) && this.m_chunkBits < this.m_maxChunkBits) {
                                this.m_innerFSB = new FastStringBuffer(this);
                            }
                            this.m_array[this.m_lastChunk] = new char[this.m_chunkSize];
                        }
                        available = this.m_chunkSize;
                        this.m_firstFree = 0;
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
                    if (this.m_lastChunk + 1 == i) {
                        char[][] newarray = new char[(i + 16)][];
                        System.arraycopy(this.m_array, 0, newarray, 0, i);
                        this.m_array = newarray;
                    }
                    char[][] cArr = this.m_array;
                    int i2 = this.m_lastChunk + 1;
                    this.m_lastChunk = i2;
                    if (cArr[i2] == null) {
                        if (this.m_lastChunk == (1 << this.m_rebundleBits) && this.m_chunkBits < this.m_maxChunkBits) {
                            this.m_innerFSB = new FastStringBuffer(this);
                        }
                        this.m_array[this.m_lastChunk] = new char[this.m_chunkSize];
                    }
                    available = this.m_chunkSize;
                    this.m_firstFree = 0;
                }
            }
            this.m_firstFree += available;
        }
    }

    public final void append(FastStringBuffer value) {
        if (value != null) {
            int strlen = value.length();
            if (strlen != 0) {
                int copyfrom = 0;
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
                        System.arraycopy(value.m_array[sourcechunk + 1], 0, this.m_array[this.m_lastChunk], this.m_firstFree + runlength, available - runlength);
                    }
                    strlen -= available;
                    copyfrom += available;
                    if (strlen > 0) {
                        int i = this.m_array.length;
                        if (this.m_lastChunk + 1 == i) {
                            char[][] newarray = new char[(i + 16)][];
                            System.arraycopy(this.m_array, 0, newarray, 0, i);
                            this.m_array = newarray;
                        }
                        char[][] cArr = this.m_array;
                        int i2 = this.m_lastChunk + 1;
                        this.m_lastChunk = i2;
                        if (cArr[i2] == null) {
                            if (this.m_lastChunk == (1 << this.m_rebundleBits) && this.m_chunkBits < this.m_maxChunkBits) {
                                this.m_innerFSB = new FastStringBuffer(this);
                            }
                            this.m_array[this.m_lastChunk] = new char[this.m_chunkSize];
                        }
                        available = this.m_chunkSize;
                        this.m_firstFree = 0;
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
            sourcechunk++;
            sourcecolumn = 0;
            available = this.m_chunkSize;
        }
        return true;
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
        for (int i = startChunk; i < stopChunk; i++) {
            if (i != 0 || this.m_innerFSB == null) {
                sb.append(this.m_array[i], startColumn, this.m_chunkSize - startColumn);
            } else {
                this.m_innerFSB.getString(sb, startColumn, this.m_chunkSize - startColumn);
            }
            startColumn = 0;
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
            for (int i = startChunk; i < stopChunk; i++) {
                if (i != 0 || this.m_innerFSB == null) {
                    ch.characters(this.m_array[i], startColumn, this.m_chunkSize - startColumn);
                } else {
                    this.m_innerFSB.sendSAXcharacters(ch, startColumn, this.m_chunkSize - startColumn);
                }
                startColumn = 0;
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
        int stateForNextChunk = 1;
        int stop = start + length;
        int startColumn = start & this.m_chunkMask;
        int stopChunk = stop >>> this.m_chunkBits;
        int stopColumn = stop & this.m_chunkMask;
        for (int i = start >>> this.m_chunkBits; i < stopChunk; i++) {
            if (i != 0 || this.m_innerFSB == null) {
                stateForNextChunk = sendNormalizedSAXcharacters(this.m_array[i], startColumn, this.m_chunkSize - startColumn, ch, stateForNextChunk);
            } else {
                stateForNextChunk = this.m_innerFSB.sendNormalizedSAXcharacters(ch, startColumn, this.m_chunkSize - startColumn);
            }
            startColumn = 0;
        }
        if (stopChunk == 0 && this.m_innerFSB != null) {
            return this.m_innerFSB.sendNormalizedSAXcharacters(ch, startColumn, stopColumn - startColumn);
        }
        if (stopColumn > startColumn) {
            return sendNormalizedSAXcharacters(this.m_array[stopChunk], startColumn, stopColumn - startColumn, ch, stateForNextChunk | 2);
        }
        return stateForNextChunk;
    }

    static int sendNormalizedSAXcharacters(char[] ch, int start, int length, ContentHandler handler, int edgeTreatmentFlags) throws SAXException {
        int i = 0;
        boolean processingLeadingWhitespace = (edgeTreatmentFlags & 1) != 0;
        boolean seenWhitespace = (edgeTreatmentFlags & 4) != 0;
        int currPos = start;
        int limit = start + length;
        if (processingLeadingWhitespace) {
            while (currPos < limit && XMLCharacterRecognizer.isWhiteSpace(ch[currPos])) {
                currPos++;
            }
            if (currPos == limit) {
                return edgeTreatmentFlags;
            }
        }
        while (currPos < limit) {
            int startNonWhitespace = currPos;
            while (currPos < limit && (XMLCharacterRecognizer.isWhiteSpace(ch[currPos]) ^ 1) != 0) {
                currPos++;
            }
            if (startNonWhitespace != currPos) {
                if (seenWhitespace) {
                    handler.characters(SINGLE_SPACE, 0, 1);
                    seenWhitespace = false;
                }
                handler.characters(ch, startNonWhitespace, currPos - startNonWhitespace);
            }
            int i2 = currPos;
            while (currPos < limit && XMLCharacterRecognizer.isWhiteSpace(ch[currPos])) {
                currPos++;
            }
            if (i2 != currPos) {
                seenWhitespace = true;
            }
        }
        if (seenWhitespace) {
            i = 4;
        }
        return i | (edgeTreatmentFlags & 2);
    }

    public static void sendNormalizedSAXcharacters(char[] ch, int start, int length, ContentHandler handler) throws SAXException {
        sendNormalizedSAXcharacters(ch, start, length, handler, 3);
    }

    public void sendSAXComment(LexicalHandler ch, int start, int length) throws SAXException {
        ch.comment(getString(start, length).toCharArray(), 0, length);
    }

    private void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
    }

    private FastStringBuffer(FastStringBuffer source) {
        this.m_chunkBits = 15;
        this.m_maxChunkBits = 15;
        this.m_rebundleBits = 2;
        this.m_lastChunk = 0;
        this.m_firstFree = 0;
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
        source.m_lastChunk = 1;
        source.m_firstFree = 0;
        source.m_chunkBits += this.m_rebundleBits;
        source.m_chunkSize = 1 << source.m_chunkBits;
        source.m_chunkMask = source.m_chunkSize - 1;
    }
}
