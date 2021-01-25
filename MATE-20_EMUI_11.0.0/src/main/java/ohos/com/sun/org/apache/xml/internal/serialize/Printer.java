package ohos.com.sun.org.apache.xml.internal.serialize;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class Printer {
    private static final int BufferSize = 4096;
    private final char[] _buffer = new char[4096];
    protected Writer _docWriter;
    protected StringWriter _dtdWriter;
    protected IOException _exception;
    protected final OutputFormat _format;
    private int _pos = 0;
    protected Writer _writer;

    public int getNextIndent() {
        return 0;
    }

    public void indent() {
    }

    public void setNextIndent(int i) {
    }

    public void setThisIndent(int i) {
    }

    public void unindent() {
    }

    public Printer(Writer writer, OutputFormat outputFormat) {
        this._writer = writer;
        this._format = outputFormat;
        this._exception = null;
        this._dtdWriter = null;
        this._docWriter = null;
        this._pos = 0;
    }

    public IOException getException() {
        return this._exception;
    }

    public void enterDTD() throws IOException {
        if (this._dtdWriter == null) {
            flushLine(false);
            this._dtdWriter = new StringWriter();
            this._docWriter = this._writer;
            this._writer = this._dtdWriter;
        }
    }

    public String leaveDTD() throws IOException {
        if (this._writer != this._dtdWriter) {
            return null;
        }
        flushLine(false);
        this._writer = this._docWriter;
        return this._dtdWriter.toString();
    }

    public void printText(String str) throws IOException {
        try {
            int length = str.length();
            for (int i = 0; i < length; i++) {
                if (this._pos == 4096) {
                    this._writer.write(this._buffer);
                    this._pos = 0;
                }
                this._buffer[this._pos] = str.charAt(i);
                this._pos++;
            }
        } catch (IOException e) {
            if (this._exception == null) {
                this._exception = e;
            }
            throw e;
        }
    }

    public void printText(StringBuffer stringBuffer) throws IOException {
        try {
            int length = stringBuffer.length();
            for (int i = 0; i < length; i++) {
                if (this._pos == 4096) {
                    this._writer.write(this._buffer);
                    this._pos = 0;
                }
                this._buffer[this._pos] = stringBuffer.charAt(i);
                this._pos++;
            }
        } catch (IOException e) {
            if (this._exception == null) {
                this._exception = e;
            }
            throw e;
        }
    }

    public void printText(char[] cArr, int i, int i2) throws IOException {
        while (true) {
            int i3 = i2 - 1;
            if (i2 > 0) {
                try {
                    if (this._pos == 4096) {
                        this._writer.write(this._buffer);
                        this._pos = 0;
                    }
                    this._buffer[this._pos] = cArr[i];
                    i++;
                    this._pos++;
                    i2 = i3;
                } catch (IOException e) {
                    if (this._exception == null) {
                        this._exception = e;
                    }
                    throw e;
                }
            } else {
                return;
            }
        }
    }

    public void printText(char c) throws IOException {
        try {
            if (this._pos == 4096) {
                this._writer.write(this._buffer);
                this._pos = 0;
            }
            this._buffer[this._pos] = c;
            this._pos++;
        } catch (IOException e) {
            if (this._exception == null) {
                this._exception = e;
            }
            throw e;
        }
    }

    public void printSpace() throws IOException {
        try {
            if (this._pos == 4096) {
                this._writer.write(this._buffer);
                this._pos = 0;
            }
            this._buffer[this._pos] = ' ';
            this._pos++;
        } catch (IOException e) {
            if (this._exception == null) {
                this._exception = e;
            }
            throw e;
        }
    }

    public void breakLine() throws IOException {
        try {
            if (this._pos == 4096) {
                this._writer.write(this._buffer);
                this._pos = 0;
            }
            this._buffer[this._pos] = '\n';
            this._pos++;
        } catch (IOException e) {
            if (this._exception == null) {
                this._exception = e;
            }
            throw e;
        }
    }

    public void breakLine(boolean z) throws IOException {
        breakLine();
    }

    public void flushLine(boolean z) throws IOException {
        try {
            this._writer.write(this._buffer, 0, this._pos);
        } catch (IOException e) {
            if (this._exception == null) {
                this._exception = e;
            }
        }
        this._pos = 0;
    }

    public void flush() throws IOException {
        try {
            this._writer.write(this._buffer, 0, this._pos);
            this._writer.flush();
            this._pos = 0;
        } catch (IOException e) {
            if (this._exception == null) {
                this._exception = e;
            }
            throw e;
        }
    }
}
