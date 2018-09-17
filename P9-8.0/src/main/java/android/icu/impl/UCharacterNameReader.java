package android.icu.impl;

import android.icu.impl.ICUBinary.Authenticate;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

final class UCharacterNameReader implements Authenticate {
    private static final int ALG_INFO_SIZE_ = 12;
    private static final int DATA_FORMAT_ID_ = 1970168173;
    private static final int GROUP_INFO_SIZE_ = 3;
    private int m_algnamesindex_;
    private ByteBuffer m_byteBuffer_;
    private int m_groupindex_;
    private int m_groupstringindex_;
    private int m_tokenstringindex_;

    public boolean isDataVersionAcceptable(byte[] version) {
        return version[0] == (byte) 1;
    }

    protected UCharacterNameReader(ByteBuffer bytes) throws IOException {
        ICUBinary.readHeader(bytes, DATA_FORMAT_ID_, this);
        this.m_byteBuffer_ = bytes;
    }

    protected void read(UCharacterName data) throws IOException {
        this.m_tokenstringindex_ = this.m_byteBuffer_.getInt();
        this.m_groupindex_ = this.m_byteBuffer_.getInt();
        this.m_groupstringindex_ = this.m_byteBuffer_.getInt();
        this.m_algnamesindex_ = this.m_byteBuffer_.getInt();
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
        count = this.m_byteBuffer_.getInt();
        AlgorithmName[] alg = new AlgorithmName[count];
        for (int i = 0; i < count; i++) {
            AlgorithmName an = readAlg();
            if (an == null) {
                throw new IOException("unames.icu read error: Algorithmic names creation error");
            }
            alg[i] = an;
        }
        data.setAlgorithm(alg);
    }

    protected boolean authenticate(byte[] dataformatid, byte[] dataformatversion) {
        if (Arrays.equals(ICUBinary.getVersionByteArrayFromCompactInt(DATA_FORMAT_ID_), dataformatid)) {
            return isDataVersionAcceptable(dataformatversion);
        }
        return false;
    }

    private AlgorithmName readAlg() throws IOException {
        AlgorithmName result = new AlgorithmName();
        int rangestart = this.m_byteBuffer_.getInt();
        int rangeend = this.m_byteBuffer_.getInt();
        byte type = this.m_byteBuffer_.get();
        byte variant = this.m_byteBuffer_.get();
        if (!result.setInfo(rangestart, rangeend, type, variant)) {
            return null;
        }
        int size = this.m_byteBuffer_.getChar();
        if (type == (byte) 1) {
            result.setFactor(ICUBinary.getChars(this.m_byteBuffer_, variant, 0));
            size -= variant << 1;
        }
        StringBuilder prefix = new StringBuilder();
        char c = (char) (this.m_byteBuffer_.get() & 255);
        while (c != 0) {
            prefix.append(c);
            c = (char) (this.m_byteBuffer_.get() & 255);
        }
        result.setPrefix(prefix.toString());
        size -= (prefix.length() + 12) + 1;
        if (size > 0) {
            byte[] string = new byte[size];
            this.m_byteBuffer_.get(string);
            result.setFactorString(string);
        }
        return result;
    }
}
