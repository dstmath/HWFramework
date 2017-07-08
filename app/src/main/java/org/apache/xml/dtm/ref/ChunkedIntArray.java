package org.apache.xml.dtm.ref;

import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;

final class ChunkedIntArray {
    static final int chunkalloc = 1024;
    static final int lowbits = 10;
    static final int lowmask = 1023;
    ChunksVector chunks;
    final int[] fastArray;
    int lastUsed;
    final int slotsize;

    class ChunksVector {
        final int BLOCKSIZE;
        int[][] m_map;
        int m_mapSize;
        int pos;

        ChunksVector() {
            this.BLOCKSIZE = 64;
            this.m_map = new int[64][];
            this.m_mapSize = 64;
            this.pos = 0;
        }

        final int size() {
            return this.pos;
        }

        void addElement(int[] value) {
            if (this.pos >= this.m_mapSize) {
                int orgMapSize = this.m_mapSize;
                while (this.pos >= this.m_mapSize) {
                    this.m_mapSize += 64;
                }
                int[][] newMap = new int[this.m_mapSize][];
                System.arraycopy(this.m_map, 0, newMap, 0, orgMapSize);
                this.m_map = newMap;
            }
            this.m_map[this.pos] = value;
            this.pos++;
        }

        final int[] elementAt(int pos) {
            return this.m_map[pos];
        }
    }

    ChunkedIntArray(int slotsize) {
        this.slotsize = 4;
        this.chunks = new ChunksVector();
        this.fastArray = new int[chunkalloc];
        this.lastUsed = 0;
        if (4 < slotsize) {
            throw new ArrayIndexOutOfBoundsException(XMLMessages.createXMLMessage(XMLErrorResources.ER_CHUNKEDINTARRAY_NOT_SUPPORTED, new Object[]{Integer.toString(slotsize)}));
        }
        if (4 > slotsize) {
            System.out.println("*****WARNING: ChunkedIntArray(" + slotsize + ") wasting " + (4 - slotsize) + " words per slot");
        }
        this.chunks.addElement(this.fastArray);
    }

    int appendSlot(int w0, int w1, int w2, int w3) {
        int newoffset = (this.lastUsed + 1) * 4;
        int chunkpos = newoffset >> lowbits;
        int slotpos = newoffset & lowmask;
        if (chunkpos > this.chunks.size() - 1) {
            this.chunks.addElement(new int[chunkalloc]);
        }
        int[] chunk = this.chunks.elementAt(chunkpos);
        chunk[slotpos] = w0;
        chunk[slotpos + 1] = w1;
        chunk[slotpos + 2] = w2;
        chunk[slotpos + 3] = w3;
        int i = this.lastUsed + 1;
        this.lastUsed = i;
        return i;
    }

    int readEntry(int position, int offset) throws ArrayIndexOutOfBoundsException {
        if (offset >= 4) {
            throw new ArrayIndexOutOfBoundsException(XMLMessages.createXMLMessage(XMLErrorResources.ER_OFFSET_BIGGER_THAN_SLOT, null));
        }
        position *= 4;
        return this.chunks.elementAt(position >> lowbits)[(position & lowmask) + offset];
    }

    int specialFind(int startPos, int position) {
        int ancestor = startPos;
        while (ancestor > 0) {
            ancestor *= 4;
            ancestor = this.chunks.elementAt(ancestor >> lowbits)[(ancestor & lowmask) + 1];
            if (ancestor == position) {
                break;
            }
        }
        if (ancestor <= 0) {
            return position;
        }
        return -1;
    }

    int slotsUsed() {
        return this.lastUsed;
    }

    void discardLast() {
        this.lastUsed--;
    }

    void writeEntry(int position, int offset, int value) throws ArrayIndexOutOfBoundsException {
        if (offset >= 4) {
            throw new ArrayIndexOutOfBoundsException(XMLMessages.createXMLMessage(XMLErrorResources.ER_OFFSET_BIGGER_THAN_SLOT, null));
        }
        position *= 4;
        this.chunks.elementAt(position >> lowbits)[(position & lowmask) + offset] = value;
    }

    void writeSlot(int position, int w0, int w1, int w2, int w3) {
        position *= 4;
        int chunkpos = position >> lowbits;
        int slotpos = position & lowmask;
        if (chunkpos > this.chunks.size() - 1) {
            this.chunks.addElement(new int[chunkalloc]);
        }
        int[] chunk = this.chunks.elementAt(chunkpos);
        chunk[slotpos] = w0;
        chunk[slotpos + 1] = w1;
        chunk[slotpos + 2] = w2;
        chunk[slotpos + 3] = w3;
    }

    void readSlot(int position, int[] buffer) {
        position *= 4;
        int chunkpos = position >> lowbits;
        int slotpos = position & lowmask;
        if (chunkpos > this.chunks.size() - 1) {
            this.chunks.addElement(new int[chunkalloc]);
        }
        System.arraycopy(this.chunks.elementAt(chunkpos), slotpos, buffer, 0, 4);
    }
}
