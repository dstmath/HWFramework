package com.android.server.usb.descriptors.report;

import com.android.server.usb.descriptors.UsbDescriptorParser;

public final class HTMLReportCanvas extends ReportCanvas {
    private static final String TAG = "HTMLReportCanvas";
    private final StringBuilder mStringBuilder;

    public HTMLReportCanvas(UsbDescriptorParser parser, StringBuilder stringBuilder) {
        super(parser);
        this.mStringBuilder = stringBuilder;
    }

    public void write(String text) {
        this.mStringBuilder.append(text);
    }

    public void openHeader(int level) {
        StringBuilder sb = this.mStringBuilder;
        sb.append("<h");
        sb.append(level);
        sb.append('>');
    }

    public void closeHeader(int level) {
        StringBuilder sb = this.mStringBuilder;
        sb.append("</h");
        sb.append(level);
        sb.append('>');
    }

    public void openParagraph(boolean emphasis) {
        if (emphasis) {
            this.mStringBuilder.append("<p style=\"color:red\">");
        } else {
            this.mStringBuilder.append("<p>");
        }
    }

    public void closeParagraph() {
        this.mStringBuilder.append("</p>");
    }

    public void writeParagraph(String text, boolean inRed) {
        openParagraph(inRed);
        this.mStringBuilder.append(text);
        closeParagraph();
    }

    public void openList() {
        this.mStringBuilder.append("<ul>");
    }

    public void closeList() {
        this.mStringBuilder.append("</ul>");
    }

    public void openListItem() {
        this.mStringBuilder.append("<li>");
    }

    public void closeListItem() {
        this.mStringBuilder.append("</li>");
    }
}
