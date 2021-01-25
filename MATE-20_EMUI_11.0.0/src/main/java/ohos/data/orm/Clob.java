package ohos.data.orm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class Clob implements java.sql.Clob {
    private StringBuffer buffer;

    public Clob(String str) {
        this.buffer = new StringBuffer(str);
    }

    @Override // java.sql.Clob
    public long length() {
        StringBuffer stringBuffer = this.buffer;
        if (stringBuffer != null) {
            return (long) stringBuffer.length();
        }
        return 0;
    }

    @Override // java.sql.Clob
    public String getSubString(long j, int i) {
        int i2 = ((int) j) - 1;
        if (i2 >= 0) {
            int i3 = i + i2;
            if (this.buffer == null) {
                return null;
            }
            if (((long) i3) <= length()) {
                return this.buffer.substring(i2, i3);
            }
            throw new IllegalArgumentException();
        }
        throw new IllegalArgumentException();
    }

    @Override // java.sql.Clob
    public Reader getCharacterStream() {
        StringBuffer stringBuffer = this.buffer;
        if (stringBuffer != null) {
            return new StringReader(stringBuffer.toString());
        }
        return null;
    }

    @Override // java.sql.Clob
    public InputStream getAsciiStream() {
        StringBuffer stringBuffer = this.buffer;
        if (stringBuffer != null) {
            return new ByteArrayInputStream(stringBuffer.toString().getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }

    @Override // java.sql.Clob
    public long position(String str, long j) {
        int i = ((int) j) - 1;
        if (i >= 0) {
            StringBuffer stringBuffer = this.buffer;
            if (stringBuffer == null) {
                return -1;
            }
            if (i <= stringBuffer.length()) {
                int indexOf = this.buffer.indexOf(str, i);
                if (indexOf == -1) {
                    return -1;
                }
                return (long) (indexOf + 1);
            }
            throw new IllegalArgumentException();
        }
        throw new IllegalArgumentException();
    }

    @Override // java.sql.Clob
    public long position(java.sql.Clob clob, long j) {
        try {
            return position(clob.getSubString(1, (int) clob.length()), j);
        } catch (SQLException unused) {
            throw new IllegalArgumentException();
        }
    }

    @Override // java.sql.Clob
    public int setString(long j, String str) {
        int i = ((int) j) - 1;
        if (i < 0) {
            throw new IllegalArgumentException();
        } else if (str != null) {
            int length = str.length();
            this.buffer.replace(i, i + length, str);
            return length;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override // java.sql.Clob
    public int setString(long j, String str, int i, int i2) {
        int i3 = ((int) j) - 1;
        if (i3 < 0) {
            throw new IllegalArgumentException();
        } else if (str != null) {
            try {
                String substring = str.substring(i, i + i2);
                this.buffer.replace(i3, substring.length() + i3, substring);
                return i2;
            } catch (StringIndexOutOfBoundsException unused) {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override // java.sql.Clob
    public OutputStream setAsciiStream(long j) {
        int i = ((int) j) - 1;
        if (i >= 0) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            StringBuffer stringBuffer = this.buffer;
            if (stringBuffer != null) {
                byteArrayOutputStream.write(stringBuffer.toString().getBytes(StandardCharsets.UTF_8), 0, i);
            }
            return byteArrayOutputStream;
        }
        throw new IllegalArgumentException();
    }

    @Override // java.sql.Clob
    public Writer setCharacterStream(long j) {
        StringBuffer stringBuffer;
        int i = ((int) j) - 1;
        if (i >= 0) {
            CharArrayWriter charArrayWriter = new CharArrayWriter();
            if (i > 0 && (stringBuffer = this.buffer) != null) {
                charArrayWriter.write(stringBuffer.toString(), 0, i);
            }
            return charArrayWriter;
        }
        throw new IllegalArgumentException();
    }

    @Override // java.sql.Clob
    public void truncate(long j) {
        StringBuffer stringBuffer = this.buffer;
        if (stringBuffer == null) {
            return;
        }
        if (j <= ((long) stringBuffer.length())) {
            StringBuffer stringBuffer2 = this.buffer;
            stringBuffer2.delete((int) j, stringBuffer2.length());
            return;
        }
        throw new IllegalArgumentException();
    }

    @Override // java.sql.Clob
    public void free() {
        this.buffer = null;
    }

    @Override // java.sql.Clob
    public Reader getCharacterStream(long j, long j2) {
        return new StringReader(getSubString(j, (int) j2));
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Clob clob = (Clob) obj;
        StringBuffer stringBuffer = this.buffer;
        if (stringBuffer != null) {
            return stringBuffer.toString().equals(clob.buffer.toString());
        }
        return clob.buffer == null;
    }

    @Override // java.lang.Object
    public int hashCode() {
        StringBuffer stringBuffer = this.buffer;
        if (stringBuffer != null) {
            return stringBuffer.toString().hashCode();
        }
        return 0;
    }

    @Override // java.lang.Object
    public String toString() {
        return this.buffer.toString();
    }
}
