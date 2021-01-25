package ohos.global.icu.text;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class CharsetMatch implements Comparable<CharsetMatch> {
    private String fCharsetName;
    private int fConfidence;
    private InputStream fInputStream = null;
    private String fLang;
    private byte[] fRawInput = null;
    private int fRawLength;

    public Reader getReader() {
        InputStream inputStream = this.fInputStream;
        if (inputStream == null) {
            inputStream = new ByteArrayInputStream(this.fRawInput, 0, this.fRawLength);
        }
        try {
            inputStream.reset();
            return new InputStreamReader(inputStream, getName());
        } catch (IOException unused) {
            return null;
        }
    }

    public String getString() throws IOException {
        return getString(-1);
    }

    public String getString(int i) throws IOException {
        if (this.fInputStream != null) {
            StringBuilder sb = new StringBuilder();
            char[] cArr = new char[1024];
            Reader reader = getReader();
            if (i < 0) {
                i = Integer.MAX_VALUE;
            }
            while (true) {
                int read = reader.read(cArr, 0, Math.min(i, 1024));
                if (read >= 0) {
                    sb.append(cArr, 0, read);
                    i -= read;
                } else {
                    reader.close();
                    return sb.toString();
                }
            }
        } else {
            String name = getName();
            String str = "_rtl";
            if (name.indexOf(str) < 0) {
                str = "_ltr";
            }
            int indexOf = name.indexOf(str);
            if (indexOf > 0) {
                name = name.substring(0, indexOf);
            }
            return new String(this.fRawInput, name);
        }
    }

    public int getConfidence() {
        return this.fConfidence;
    }

    public String getName() {
        return this.fCharsetName;
    }

    public String getLanguage() {
        return this.fLang;
    }

    public int compareTo(CharsetMatch charsetMatch) {
        int i = this.fConfidence;
        int i2 = charsetMatch.fConfidence;
        if (i > i2) {
            return 1;
        }
        return i < i2 ? -1 : 0;
    }

    CharsetMatch(CharsetDetector charsetDetector, CharsetRecognizer charsetRecognizer, int i) {
        this.fConfidence = i;
        if (charsetDetector.fInputStream == null) {
            this.fRawInput = charsetDetector.fRawInput;
            this.fRawLength = charsetDetector.fRawLength;
        }
        this.fInputStream = charsetDetector.fInputStream;
        this.fCharsetName = charsetRecognizer.getName();
        this.fLang = charsetRecognizer.getLanguage();
    }

    CharsetMatch(CharsetDetector charsetDetector, CharsetRecognizer charsetRecognizer, int i, String str, String str2) {
        this.fConfidence = i;
        if (charsetDetector.fInputStream == null) {
            this.fRawInput = charsetDetector.fRawInput;
            this.fRawLength = charsetDetector.fRawLength;
        }
        this.fInputStream = charsetDetector.fInputStream;
        this.fCharsetName = str;
        this.fLang = str2;
    }
}
