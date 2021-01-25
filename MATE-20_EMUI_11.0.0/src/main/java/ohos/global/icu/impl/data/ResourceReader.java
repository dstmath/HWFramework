package ohos.global.icu.impl.data;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.PatternProps;

public class ResourceReader implements Closeable {
    private String encoding;
    private int lineNo;
    private BufferedReader reader;
    private String resourceName;
    private Class<?> root;

    public ResourceReader(String str, String str2) throws UnsupportedEncodingException {
        this(ICUData.class, "data/" + str, str2);
    }

    public ResourceReader(String str) {
        this(ICUData.class, "data/" + str);
    }

    public ResourceReader(Class<?> cls, String str, String str2) throws UnsupportedEncodingException {
        this.reader = null;
        this.root = cls;
        this.resourceName = str;
        this.encoding = str2;
        this.lineNo = -1;
        _reset();
    }

    public ResourceReader(InputStream inputStream, String str, String str2) {
        InputStreamReader inputStreamReader;
        this.reader = null;
        this.root = null;
        this.resourceName = str;
        this.encoding = str2;
        this.lineNo = -1;
        if (str2 == null) {
            try {
                inputStreamReader = new InputStreamReader(inputStream);
            } catch (UnsupportedEncodingException unused) {
                return;
            }
        } else {
            inputStreamReader = new InputStreamReader(inputStream, str2);
        }
        this.reader = new BufferedReader(inputStreamReader);
        this.lineNo = 0;
    }

    public ResourceReader(InputStream inputStream, String str) {
        this(inputStream, str, (String) null);
    }

    public ResourceReader(Class<?> cls, String str) {
        this.reader = null;
        this.root = cls;
        this.resourceName = str;
        this.encoding = null;
        this.lineNo = -1;
        try {
            _reset();
        } catch (UnsupportedEncodingException unused) {
        }
    }

    public String readLine() throws IOException {
        int i = this.lineNo;
        if (i == 0) {
            this.lineNo = i + 1;
            String readLine = this.reader.readLine();
            if (readLine != null) {
                return (readLine.charAt(0) == 65519 || readLine.charAt(0) == 65279) ? readLine.substring(1) : readLine;
            }
            return readLine;
        }
        this.lineNo = i + 1;
        return this.reader.readLine();
    }

    public String readLineSkippingComments(boolean z) throws IOException {
        while (true) {
            String readLine = readLine();
            if (readLine == null) {
                return readLine;
            }
            int skipWhiteSpace = PatternProps.skipWhiteSpace(readLine, 0);
            if (skipWhiteSpace != readLine.length() && readLine.charAt(skipWhiteSpace) != '#') {
                return z ? readLine.substring(skipWhiteSpace) : readLine;
            }
        }
    }

    public String readLineSkippingComments() throws IOException {
        return readLineSkippingComments(false);
    }

    public int getLineNumber() {
        return this.lineNo;
    }

    public String describePosition() {
        return this.resourceName + ':' + this.lineNo;
    }

    public void reset() {
        try {
            _reset();
        } catch (UnsupportedEncodingException unused) {
        }
    }

    private void _reset() throws UnsupportedEncodingException {
        InputStreamReader inputStreamReader;
        try {
            close();
        } catch (IOException unused) {
        }
        if (this.lineNo != 0) {
            InputStream stream = ICUData.getStream(this.root, this.resourceName);
            if (stream != null) {
                String str = this.encoding;
                if (str == null) {
                    inputStreamReader = new InputStreamReader(stream);
                } else {
                    inputStreamReader = new InputStreamReader(stream, str);
                }
                this.reader = new BufferedReader(inputStreamReader);
                this.lineNo = 0;
                return;
            }
            throw new IllegalArgumentException("Can't open " + this.resourceName);
        }
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        BufferedReader bufferedReader = this.reader;
        if (bufferedReader != null) {
            bufferedReader.close();
            this.reader = null;
        }
    }
}
