package android.icu.impl;

import android.icu.impl.Trie.DataManipulate;
import android.icu.text.UTF16;
import dalvik.bytecode.Opcodes;
import java.nio.ByteBuffer;
import org.w3c.dom.traversal.NodeFilter;

public class CharTrie extends Trie {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private char[] m_data_;
    private char m_initialValue_;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.CharTrie.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.CharTrie.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.CharTrie.<clinit>():void");
    }

    public CharTrie(ByteBuffer bytes, DataManipulate dataManipulate) {
        super(bytes, dataManipulate);
        if (!isCharTrie()) {
            throw new IllegalArgumentException("Data given does not belong to a char trie.");
        }
    }

    public CharTrie(int initialValue, int leadUnitValue, DataManipulate dataManipulate) {
        int i;
        super(new char[2080], NodeFilter.SHOW_DOCUMENT_TYPE, dataManipulate);
        int dataLength = NodeFilter.SHOW_DOCUMENT;
        if (leadUnitValue != initialValue) {
            dataLength = 288;
        }
        this.m_data_ = new char[dataLength];
        this.m_dataLength_ = dataLength;
        this.m_initialValue_ = (char) initialValue;
        for (i = 0; i < NodeFilter.SHOW_DOCUMENT; i++) {
            this.m_data_[i] = (char) initialValue;
        }
        if (leadUnitValue != initialValue) {
            char block = (char) 64;
            for (i = 1728; i < 1760; i++) {
                this.m_index_[i] = block;
            }
            for (i = NodeFilter.SHOW_DOCUMENT; i < 288; i++) {
                this.m_data_[i] = (char) leadUnitValue;
            }
        }
    }

    public final char getCodePointValue(int ch) {
        if (ch < 0 || ch >= UTF16.SURROGATE_MIN_VALUE) {
            int offset = getCodePointOffset(ch);
            return offset >= 0 ? this.m_data_[offset] : this.m_initialValue_;
        }
        return this.m_data_[(this.m_index_[ch >> 5] << 2) + (ch & 31)];
    }

    public final char getLeadValue(char ch) {
        return this.m_data_[getLeadOffset(ch)];
    }

    public final char getBMPValue(char ch) {
        return this.m_data_[getBMPOffset(ch)];
    }

    public final char getSurrogateValue(char lead, char trail) {
        int offset = getSurrogateOffset(lead, trail);
        if (offset > 0) {
            return this.m_data_[offset];
        }
        return this.m_initialValue_;
    }

    public final char getTrailValue(int leadvalue, char trail) {
        if (this.m_dataManipulate_ == null) {
            throw new NullPointerException("The field DataManipulate in this Trie is null");
        }
        int offset = this.m_dataManipulate_.getFoldingOffset(leadvalue);
        if (offset > 0) {
            return this.m_data_[getRawOffset(offset, (char) (trail & Opcodes.OP_NEW_INSTANCE_JUMBO))];
        }
        return this.m_initialValue_;
    }

    public final char getLatin1LinearValue(char ch) {
        return this.m_data_[(this.m_dataOffset_ + 32) + ch];
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!super.equals(other) || !(other instanceof CharTrie)) {
            return false;
        }
        if (this.m_initialValue_ == ((CharTrie) other).m_initialValue_) {
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

    protected final void unserialize(ByteBuffer bytes) {
        this.m_index_ = ICUBinary.getChars(bytes, this.m_dataOffset_ + this.m_dataLength_, 0);
        this.m_data_ = this.m_index_;
        this.m_initialValue_ = this.m_data_[this.m_dataOffset_];
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
}
