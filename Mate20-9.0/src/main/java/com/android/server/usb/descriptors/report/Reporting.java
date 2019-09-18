package com.android.server.usb.descriptors.report;

public interface Reporting {
    void report(ReportCanvas reportCanvas);

    void shortReport(ReportCanvas reportCanvas);
}
