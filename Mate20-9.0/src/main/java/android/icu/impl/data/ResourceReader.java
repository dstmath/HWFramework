package android.icu.impl.data;

import android.icu.impl.ICUData;
import android.icu.impl.PatternProps;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class ResourceReader implements Closeable {
    private String encoding;
    private int lineNo;
    private BufferedReader reader;
    private String resourceName;
    private Class<?> root;

    public ResourceReader(String resourceName2, String encoding2) throws UnsupportedEncodingException {
        this((Class<?>) ICUData.class, "data/" + resourceName2, encoding2);
    }

    public ResourceReader(String resourceName2) {
        this((Class<?>) ICUData.class, "data/" + resourceName2);
    }

    public ResourceReader(Class<?> rootClass, String resourceName2, String encoding2) throws UnsupportedEncodingException {
        this.reader = null;
        this.root = rootClass;
        this.resourceName = resourceName2;
        this.encoding = encoding2;
        this.lineNo = -1;
        _reset();
    }

    public ResourceReader(InputStream is, String resourceName2, String encoding2) {
        InputStreamReader isr;
        this.reader = null;
        this.root = null;
        this.resourceName = resourceName2;
        this.encoding = encoding2;
        this.lineNo = -1;
        if (encoding2 == null) {
            try {
                isr = new InputStreamReader(is);
            } catch (UnsupportedEncodingException e) {
                return;
            }
        } else {
            isr = new InputStreamReader(is, encoding2);
        }
        this.reader = new BufferedReader(isr);
        this.lineNo = 0;
    }

    public ResourceReader(InputStream is, String resourceName2) {
        this(is, resourceName2, (String) null);
    }

    public ResourceReader(Class<?> rootClass, String resourceName2) {
        this.reader = null;
        this.root = rootClass;
        this.resourceName = resourceName2;
        this.encoding = null;
        this.lineNo = -1;
        try {
            _reset();
        } catch (UnsupportedEncodingException e) {
        }
    }

    public String readLine() throws IOException {
        if (this.lineNo == 0) {
            this.lineNo++;
            String line = this.reader.readLine();
            if (line != null && (line.charAt(0) == 65519 || line.charAt(0) == 65279)) {
                line = line.substring(1);
            }
            return line;
        }
        this.lineNo++;
        return this.reader.readLine();
    }

    public String readLineSkippingComments(boolean trim) throws IOException {
        while (true) {
            String line = readLine();
            if (line == null) {
                return line;
            }
            int pos = PatternProps.skipWhiteSpace(line, 0);
            if (pos != line.length() && line.charAt(pos) != '#') {
                if (trim) {
                    line = line.substring(pos);
                }
                return line;
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
        } catch (UnsupportedEncodingException e) {
        }
    }

    private void _reset() throws UnsupportedEncodingException {
        InputStreamReader isr;
        try {
            close();
        } catch (IOException e) {
        }
        if (this.lineNo != 0) {
            InputStream is = ICUData.getStream(this.root, this.resourceName);
            if (is != null) {
                if (this.encoding == null) {
                    isr = new InputStreamReader(is);
                } else {
                    isr = new InputStreamReader(is, this.encoding);
                }
                this.reader = new BufferedReader(isr);
                this.lineNo = 0;
                return;
            }
            throw new IllegalArgumentException("Can't open " + this.resourceName);
        }
    }

    public void close() throws IOException {
        if (this.reader != null) {
            this.reader.close();
            this.reader = null;
        }
    }
}
