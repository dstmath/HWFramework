package com.huawei.odmf.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.utils.LOG;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.SQLException;

public class Clob implements java.sql.Clob, Parcelable {
    public static final Parcelable.Creator<Clob> CREATOR = new Parcelable.Creator<Clob>() {
        public Clob createFromParcel(Parcel in) {
            return new Clob(in);
        }

        public Clob[] newArray(int size) {
            return new Clob[size];
        }
    };
    private String charData;

    public Clob(String charDataInit) {
        this.charData = charDataInit;
    }

    public long length() throws SQLException {
        if (this.charData != null) {
            return (long) this.charData.length();
        }
        return 0;
    }

    public String getSubString(long startPos, int length) throws SQLException {
        if (startPos < 1) {
            throw new ODMFIllegalArgumentException();
        }
        int adjustedStartPos = ((int) startPos) - 1;
        int adjustedEndIndex = adjustedStartPos + length;
        if (this.charData == null) {
            return null;
        }
        if (adjustedEndIndex <= this.charData.length()) {
            return this.charData.substring(adjustedStartPos, adjustedEndIndex);
        }
        throw new ODMFIllegalArgumentException();
    }

    public Reader getCharacterStream() throws SQLException {
        if (this.charData != null) {
            return new StringReader(this.charData);
        }
        return null;
    }

    public InputStream getAsciiStream() throws SQLException {
        if (this.charData == null) {
            return null;
        }
        try {
            return new ByteArrayInputStream(this.charData.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.logE("The encoding UTF-8 is not support");
            throw new ODMFIllegalArgumentException("The encoding UTF-8 is not support");
        }
    }

    public long position(String stringToFind, long startPos) throws SQLException {
        if (startPos < 1) {
            throw new ODMFIllegalArgumentException();
        } else if (this.charData == null) {
            return -1;
        } else {
            if (startPos - 1 > ((long) this.charData.length())) {
                throw new ODMFIllegalArgumentException();
            }
            int pos = this.charData.indexOf(stringToFind, (int) (startPos - 1));
            if (pos == -1) {
                return -1;
            }
            return (long) (pos + 1);
        }
    }

    public long position(java.sql.Clob clob, long l) throws SQLException {
        return position(clob.getSubString(1, (int) clob.length()), l);
    }

    public int setString(long pos, String str) throws SQLException {
        if (pos < 1) {
            throw new ODMFIllegalArgumentException();
        } else if (str == null) {
            throw new ODMFIllegalArgumentException();
        } else {
            StringBuilder charBuf = new StringBuilder(this.charData);
            long pos2 = pos - 1;
            int strLength = str.length();
            charBuf.replace((int) pos2, (int) (((long) strLength) + pos2), str);
            this.charData = charBuf.toString();
            return strLength;
        }
    }

    public int setString(long pos, String str, int offset, int len) throws SQLException {
        if (pos < 1) {
            throw new ODMFIllegalArgumentException();
        } else if (str == null) {
            throw new ODMFIllegalArgumentException();
        } else {
            StringBuilder charBuf = new StringBuilder(this.charData);
            long pos2 = pos - 1;
            try {
                String replaceString = str.substring(offset, offset + len);
                charBuf.replace((int) pos2, (int) (((long) replaceString.length()) + pos2), replaceString);
                this.charData = charBuf.toString();
                return len;
            } catch (StringIndexOutOfBoundsException e) {
                throw new ODMFIllegalArgumentException();
            }
        }
    }

    public OutputStream setAsciiStream(long indexToWriteAt) throws SQLException {
        if (indexToWriteAt < 1) {
            throw new ODMFIllegalArgumentException();
        }
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        if (this.charData != null) {
            try {
                bytesOut.write(this.charData.getBytes("UTF-8"), 0, (int) (indexToWriteAt - 1));
            } catch (UnsupportedEncodingException e) {
                LOG.logE("The encoding UTF-8 is not support");
                throw new ODMFIllegalArgumentException("The encoding UTF-8 is not support");
            }
        }
        return bytesOut;
    }

    public Writer setCharacterStream(long indexToWriteAt) throws SQLException {
        if (indexToWriteAt < 1) {
            throw new ODMFIllegalArgumentException();
        }
        CharArrayWriter writer = new CharArrayWriter();
        if (indexToWriteAt > 1 && this.charData != null) {
            writer.write(this.charData, 0, (int) (indexToWriteAt - 1));
        }
        return writer;
    }

    public void truncate(long length) throws SQLException {
        if (this.charData == null) {
            return;
        }
        if (length > ((long) this.charData.length())) {
            throw new ODMFIllegalArgumentException();
        }
        this.charData = this.charData.substring(0, (int) length);
    }

    public void free() throws SQLException {
        this.charData = null;
    }

    public Reader getCharacterStream(long pos, long length) throws SQLException {
        return new StringReader(getSubString(pos, (int) length));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Clob clob = (Clob) o;
        if (this.charData != null) {
            return this.charData.equals(clob.charData);
        }
        if (clob.charData != null) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        if (this.charData != null) {
            return this.charData.hashCode();
        }
        return 0;
    }

    public String toString() {
        return this.charData;
    }

    public Clob(Parcel in) {
        this.charData = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.charData);
    }
}
