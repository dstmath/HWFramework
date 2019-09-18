package org.xmlpull.v1;

import android.icu.impl.number.Padder;
import java.io.PrintStream;

public class XmlPullParserException extends Exception {
    protected int column;
    protected Throwable detail;
    protected int row;

    public XmlPullParserException(String s) {
        super(s);
        this.row = -1;
        this.column = -1;
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public XmlPullParserException(String msg, XmlPullParser parser, Throwable chain) {
        super(r0.toString());
        String str;
        String str2;
        String str3;
        StringBuilder sb = new StringBuilder();
        if (msg == null) {
            str = "";
        } else {
            str = msg + Padder.FALLBACK_PADDING_STRING;
        }
        sb.append(str);
        if (parser == null) {
            str2 = "";
        } else {
            str2 = "(position:" + parser.getPositionDescription() + ") ";
        }
        sb.append(str2);
        if (chain == null) {
            str3 = "";
        } else {
            str3 = "caused by: " + chain;
        }
        sb.append(str3);
        this.row = -1;
        this.column = -1;
        if (parser != null) {
            this.row = parser.getLineNumber();
            this.column = parser.getColumnNumber();
        }
        this.detail = chain;
    }

    public Throwable getDetail() {
        return this.detail;
    }

    public int getLineNumber() {
        return this.row;
    }

    public int getColumnNumber() {
        return this.column;
    }

    public void printStackTrace() {
        if (this.detail == null) {
            super.printStackTrace();
            return;
        }
        synchronized (System.err) {
            PrintStream printStream = System.err;
            printStream.println(super.getMessage() + "; nested exception is:");
            this.detail.printStackTrace();
        }
    }
}
