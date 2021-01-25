package com.huawei.internal.util;

import com.android.internal.util.IndentingPrintWriter;
import java.io.PrintWriter;
import java.io.Writer;

public class IndentingPrintWriterEx {
    private IndentingPrintWriter mIndentingPrintWriter;

    public IndentingPrintWriterEx(Writer writer, String singleIndent) {
        this.mIndentingPrintWriter = new IndentingPrintWriter(writer, singleIndent);
    }

    public void println(String x) {
        this.mIndentingPrintWriter.println(x);
    }

    public void println() {
        this.mIndentingPrintWriter.println();
    }

    public IndentingPrintWriter getIndentingPrintWriter() {
        return this.mIndentingPrintWriter;
    }

    public IndentingPrintWriterEx increaseIndent() {
        this.mIndentingPrintWriter.increaseIndent();
        return this;
    }

    public IndentingPrintWriterEx decreaseIndent() {
        this.mIndentingPrintWriter.decreaseIndent();
        return this;
    }

    public void flush() {
        this.mIndentingPrintWriter.flush();
    }

    public PrintWriter getPrintWriter() {
        IndentingPrintWriter indentingPrintWriter = this.mIndentingPrintWriter;
        if (indentingPrintWriter != null) {
            return indentingPrintWriter;
        }
        return null;
    }
}
