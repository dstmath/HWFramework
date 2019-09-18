package org.ccil.cowan.tagsoup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import org.xml.sax.SAXException;

public class PYXScanner implements Scanner {
    public void resetDocumentLocator(String publicid, String systemid) {
    }

    public void scan(Reader r, ScanHandler h) throws IOException, SAXException {
        BufferedReader br = new BufferedReader(r);
        char[] buff = null;
        boolean instag = false;
        while (true) {
            String readLine = br.readLine();
            String s = readLine;
            if (readLine != null) {
                int size = s.length();
                if (buff == null || buff.length < size) {
                    buff = new char[size];
                }
                s.getChars(0, size, buff, 0);
                char c = buff[0];
                if (c == '-') {
                    if (instag) {
                        h.stagc(buff, 0, 0);
                        instag = false;
                    }
                    if (s.equals("-\\n")) {
                        buff[0] = 10;
                        h.pcdata(buff, 0, 1);
                    } else {
                        h.pcdata(buff, 1, size - 1);
                    }
                } else if (c == '?') {
                    if (instag) {
                        h.stagc(buff, 0, 0);
                        instag = false;
                    }
                    h.pi(buff, 1, size - 1);
                } else if (c == 'A') {
                    int sp = s.indexOf(32);
                    h.aname(buff, 1, sp - 1);
                    h.aval(buff, sp + 1, (size - sp) - 1);
                } else if (c != 'E') {
                    switch (c) {
                        case '(':
                            if (instag) {
                                h.stagc(buff, 0, 0);
                            }
                            h.gi(buff, 1, size - 1);
                            instag = true;
                            break;
                        case ')':
                            if (instag) {
                                h.stagc(buff, 0, 0);
                                instag = false;
                            }
                            h.etag(buff, 1, size - 1);
                            break;
                    }
                } else {
                    if (instag) {
                        h.stagc(buff, 0, 0);
                        instag = false;
                    }
                    h.entity(buff, 1, size - 1);
                }
            } else {
                h.eof(buff, 0, 0);
                return;
            }
        }
    }

    public void startCDATA() {
    }

    public static void main(String[] argv) throws IOException, SAXException {
        new PYXScanner().scan(new InputStreamReader(System.in, "UTF-8"), new PYXWriter(new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"))));
    }
}
