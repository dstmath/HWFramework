package android.icu.impl;

import android.icu.impl.ICUBinary;
import android.icu.impl.UCharacterName;
import android.icu.lang.UCharacterEnums;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

final class UCharacterNameReader implements ICUBinary.Authenticate {
    private static final int ALG_INFO_SIZE_ = 12;
    private static final int DATA_FORMAT_ID_ = 1970168173;
    private static final int GROUP_INFO_SIZE_ = 3;
    private int m_algnamesindex_;
    private ByteBuffer m_byteBuffer_;
    private int m_groupindex_;
    private int m_groupstringindex_;
    private int m_tokenstringindex_;

    public boolean isDataVersionAcceptable(byte[] version) {
        return version[0] == 1;
    }

    protected UCharacterNameReader(ByteBuffer bytes) throws IOException {
        ICUBinary.readHeader(bytes, DATA_FORMAT_ID_, this);
        this.m_byteBuffer_ = bytes;
    }

    /* access modifiers changed from: protected */
    public void read(UCharacterName data) throws IOException {
        this.m_tokenstringindex_ = this.m_byteBuffer_.getInt();
        this.m_groupindex_ = this.m_byteBuffer_.getInt();
        this.m_groupstringindex_ = this.m_byteBuffer_.getInt();
        this.m_algnamesindex_ = this.m_byteBuffer_.getInt();
        int i = 0;
        char[] token = ICUBinary.getChars(this.m_byteBuffer_, this.m_byteBuffer_.getChar(), 0);
        byte[] tokenstr = new byte[(this.m_groupindex_ - this.m_tokenstringindex_)];
        this.m_byteBuffer_.get(tokenstr);
        data.setToken(token, tokenstr);
        int count = this.m_byteBuffer_.getChar();
        data.setGroupCountSize(count, 3);
        char[] group = ICUBinary.getChars(this.m_byteBuffer_, count * 3, 0);
        byte[] groupstring = new byte[(this.m_algnamesindex_ - this.m_groupstringindex_)];
        this.m_byteBuffer_.get(groupstring);
        data.setGroup(group, groupstring);
        int count2 = this.m_byteBuffer_.getInt();
        UCharacterName.AlgorithmName[] alg = new UCharacterName.AlgorithmName[count2];
        while (i < count2) {
            UCharacterName.AlgorithmName an = readAlg();
            if (an != null) {
                alg[i] = an;
                i++;
            } else {
                throw new IOException("unames.icu read error: Algorithmic names creation error");
            }
        }
        data.setAlgorithm(alg);
    }

    /* access modifiers changed from: protected */
    public boolean authenticate(byte[] dataformatid, byte[] dataformatversion) {
        return Arrays.equals(ICUBinary.getVersionByteArrayFromCompactInt(DATA_FORMAT_ID_), dataformatid) && isDataVersionAcceptable(dataformatversion);
    }

    private UCharacterName.AlgorithmName readAlg() throws IOException {
        UCharacterName.AlgorithmName result = new UCharacterName.AlgorithmName();
        int rangestart = this.m_byteBuffer_.getInt();
        int rangeend = this.m_byteBuffer_.getInt();
        byte type = this.m_byteBuffer_.get();
        byte variant = this.m_byteBuffer_.get();
        if (!result.setInfo(rangestart, rangeend, type, variant)) {
            return null;
        }
        int size = this.m_byteBuffer_.getChar();
        if (type == 1) {
            result.setFactor(ICUBinary.getChars(this.m_byteBuffer_, variant, 0));
            size -= variant << 1;
        }
        StringBuilder prefix = new StringBuilder();
        char c = (char) (this.m_byteBuffer_.get() & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED);
        while (c != 0) {
            prefix.append(c);
            c = (char) (this.m_byteBuffer_.get() & UCharacterEnums.ECharacterDirection.DIRECTIONALITY_UNDEFINED);
        }
        result.setPrefix(prefix.toString());
        int size2 = size - ((12 + prefix.length()) + 1);
        if (size2 > 0) {
            byte[] string = new byte[size2];
            this.m_byteBuffer_.get(string);
            result.setFactorString(string);
        }
        return result;
    }
}
