package android.icu.impl;

import android.icu.impl.Trie.DataManipulate;
import android.icu.text.UTF16;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.w3c.dom.traversal.NodeFilter;

public class IntTrie extends Trie {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private int[] m_data_;
    private int m_initialValue_;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.IntTrie.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.IntTrie.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.IntTrie.<clinit>():void");
    }

    public IntTrie(ByteBuffer bytes, DataManipulate dataManipulate) throws IOException {
        super(bytes, dataManipulate);
        if (!isIntTrie()) {
            throw new IllegalArgumentException("Data given does not belong to a int trie.");
        }
    }

    public IntTrie(int initialValue, int leadUnitValue, DataManipulate dataManipulate) {
        int i;
        super(new char[2080], NodeFilter.SHOW_DOCUMENT_TYPE, dataManipulate);
        int dataLength = NodeFilter.SHOW_DOCUMENT;
        if (leadUnitValue != initialValue) {
            dataLength = 288;
        }
        this.m_data_ = new int[dataLength];
        this.m_dataLength_ = dataLength;
        this.m_initialValue_ = initialValue;
        for (i = 0; i < NodeFilter.SHOW_DOCUMENT; i++) {
            this.m_data_[i] = initialValue;
        }
        if (leadUnitValue != initialValue) {
            char block = (char) 64;
            for (i = 1728; i < 1760; i++) {
                this.m_index_[i] = block;
            }
            for (i = NodeFilter.SHOW_DOCUMENT; i < 288; i++) {
                this.m_data_[i] = leadUnitValue;
            }
        }
    }

    public final int getCodePointValue(int ch) {
        if (ch < 0 || ch >= UTF16.SURROGATE_MIN_VALUE) {
            int offset = getCodePointOffset(ch);
            return offset >= 0 ? this.m_data_[offset] : this.m_initialValue_;
        }
        return this.m_data_[(this.m_index_[ch >> 5] << 2) + (ch & 31)];
    }

    public final int getLeadValue(char ch) {
        return this.m_data_[getLeadOffset(ch)];
    }

    public final int getBMPValue(char ch) {
        return this.m_data_[getBMPOffset(ch)];
    }

    public final int getSurrogateValue(char lead, char trail) {
        if (UTF16.isLeadSurrogate(lead) && UTF16.isTrailSurrogate(trail)) {
            int offset = getSurrogateOffset(lead, trail);
            if (offset > 0) {
                return this.m_data_[offset];
            }
            return this.m_initialValue_;
        }
        throw new IllegalArgumentException("Argument characters do not form a supplementary character");
    }

    public final int getTrailValue(int leadvalue, char trail) {
        if (this.m_dataManipulate_ == null) {
            throw new NullPointerException("The field DataManipulate in this Trie is null");
        }
        int offset = this.m_dataManipulate_.getFoldingOffset(leadvalue);
        if (offset > 0) {
            return this.m_data_[getRawOffset(offset, (char) (trail & Opcodes.OP_NEW_INSTANCE_JUMBO))];
        }
        return this.m_initialValue_;
    }

    public final int getLatin1LinearValue(char ch) {
        return this.m_data_[ch + 32];
    }

    public boolean equals(Object other) {
        if (!super.equals(other) || !(other instanceof IntTrie)) {
            return false;
        }
        IntTrie othertrie = (IntTrie) other;
        if (this.m_initialValue_ == othertrie.m_initialValue_ && Arrays.equals(this.m_data_, othertrie.m_data_)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        if (-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }

    protected final void unserialize(ByteBuffer bytes) {
        super.unserialize(bytes);
        this.m_data_ = ICUBinary.getInts(bytes, this.m_dataLength_, 0);
        this.m_initialValue_ = this.m_data_[0];
    }

    protected final int getSurrogateOffset(char lead, char trail) {
        if (this.m_dataManipulate_ == null) {
            throw new NullPointerException("The field DataManipulate in this Trie is null");
        }
        int offset = this.m_dataManipulate_.getFoldingOffset(getLeadValue(lead));
        if (offset > 0) {
            return getRawOffset(offset, (char) (trail & Opcodes.OP_NEW_INSTANCE_JUMBO));
        }
        return -1;
    }

    protected final int getValue(int index) {
        return this.m_data_[index];
    }

    protected final int getInitialValue() {
        return this.m_initialValue_;
    }

    IntTrie(char[] index, int[] data, int initialvalue, int options, DataManipulate datamanipulate) {
        super(index, options, datamanipulate);
        this.m_data_ = data;
        this.m_dataLength_ = this.m_data_.length;
        this.m_initialValue_ = initialvalue;
    }
}
