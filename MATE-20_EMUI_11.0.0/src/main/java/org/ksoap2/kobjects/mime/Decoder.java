package org.ksoap2.kobjects.mime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import org.ksoap2.kobjects.base64.Base64;

public class Decoder {
    String boundary;
    char[] buf;
    String characterEncoding;
    boolean consumed;
    boolean eof;
    Hashtable header;
    InputStream is;

    private final String readLine() throws IOException {
        int cnt = 0;
        while (true) {
            int i = this.is.read();
            if (i == -1 && cnt == 0) {
                return null;
            }
            if (i == -1 || i == 10) {
                break;
            } else if (i != 13) {
                char[] cArr = this.buf;
                if (cnt >= cArr.length) {
                    char[] tmp = new char[((cArr.length * 3) / 2)];
                    System.arraycopy(cArr, 0, tmp, 0, cArr.length);
                    this.buf = tmp;
                }
                this.buf[cnt] = (char) i;
                cnt++;
            }
        }
        return new String(this.buf, 0, cnt);
    }

    public static Hashtable getHeaderElements(String header2) {
        int pos;
        String key = "";
        int pos2 = 0;
        Hashtable result = new Hashtable();
        int len = header2.length();
        while (true) {
            if (pos2 >= len || header2.charAt(pos2) > ' ') {
                if (pos2 >= len) {
                    break;
                }
                if (header2.charAt(pos2) == '\"') {
                    int pos3 = pos2 + 1;
                    int cut = header2.indexOf(34, pos3);
                    if (cut != -1) {
                        result.put(key, header2.substring(pos3, cut));
                        pos = cut + 2;
                        if (pos >= len) {
                            break;
                        } else if (header2.charAt(pos - 1) != ';') {
                            throw new RuntimeException("; expected in " + header2);
                        }
                    } else {
                        throw new RuntimeException("End quote expected in " + header2);
                    }
                } else {
                    int cut2 = header2.indexOf(59, pos2);
                    if (cut2 == -1) {
                        result.put(key, header2.substring(pos2));
                        break;
                    }
                    result.put(key, header2.substring(pos2, cut2));
                    pos = cut2 + 1;
                }
                int cut3 = header2.indexOf(61, pos);
                if (cut3 == -1) {
                    break;
                }
                key = header2.substring(pos, cut3).toLowerCase().trim();
                pos2 = cut3 + 1;
            } else {
                pos2++;
            }
        }
        return result;
    }

    public Decoder(InputStream is2, String _bound) throws IOException {
        this(is2, _bound, null);
    }

    public Decoder(InputStream is2, String _bound, String characterEncoding2) throws IOException {
        String line;
        this.buf = new char[256];
        this.characterEncoding = characterEncoding2;
        this.is = is2;
        this.boundary = "--" + _bound;
        do {
            line = readLine();
            if (line == null) {
                throw new IOException("Unexpected EOF");
            }
        } while (!line.startsWith(this.boundary));
        if (line.endsWith("--")) {
            this.eof = true;
            is2.close();
        }
        this.consumed = true;
    }

    public Enumeration getHeaderNames() {
        return this.header.keys();
    }

    public String getHeader(String key) {
        return (String) this.header.get(key.toLowerCase());
    }

    public String readContent() throws IOException {
        String result;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        readContent(bos);
        if (this.characterEncoding == null) {
            result = new String(bos.toByteArray());
        } else {
            result = new String(bos.toByteArray(), this.characterEncoding);
        }
        PrintStream printStream = System.out;
        printStream.println("Field content: '" + result + "'");
        return result;
    }

    public void readContent(OutputStream os) throws IOException {
        String line;
        if (!this.consumed) {
            getHeader("Content-Type");
            if ("base64".equals(getHeader("Content-Transfer-Encoding"))) {
                new ByteArrayOutputStream();
                while (true) {
                    line = readLine();
                    if (line == null) {
                        throw new IOException("Unexpected EOF");
                    } else if (line.startsWith(this.boundary)) {
                        break;
                    } else {
                        Base64.decode(line, os);
                    }
                }
            } else {
                String deli = "\r\n" + this.boundary;
                int match = 0;
                while (true) {
                    int i = this.is.read();
                    if (i == -1) {
                        throw new RuntimeException("Unexpected EOF");
                    } else if (((char) i) == deli.charAt(match)) {
                        match++;
                        if (match == deli.length()) {
                            line = readLine();
                            break;
                        }
                    } else {
                        if (match > 0) {
                            for (int j = 0; j < match; j++) {
                                os.write((byte) deli.charAt(j));
                            }
                            int i2 = 0;
                            if (((char) i) == deli.charAt(0)) {
                                i2 = 1;
                            }
                            match = i2;
                        }
                        if (match == 0) {
                            os.write((byte) i);
                        }
                    }
                }
            }
            if (line.endsWith("--")) {
                this.eof = true;
            }
            this.consumed = true;
            return;
        }
        throw new RuntimeException("Content already consumed!");
    }
}
