package com.huawei.ace.runtime;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;

public final class DumpHelper {
    private static final String LOG_TAG = "DumpHelper";
    private static final Charset UTF8 = Charset.forName("UTF8");

    private static native void nativeDump(String str, FileDescriptor fileDescriptor, String[] strArr);

    public static void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String str2;
        if (fileDescriptor == null || strArr == null) {
            ALog.w(LOG_TAG, "dump failed, fd or args is null");
        } else if (strArr.length == 0) {
            printString(fileDescriptor, "Param length is 0, illegal!" + System.lineSeparator());
        } else {
            if ("-h".equals(strArr[0])) {
                str2 = "Usage:" + System.lineSeparator() + "     -element                      dump information of Element tree." + System.lineSeparator() + "     -render                       dump information of Render tree." + System.lineSeparator() + "     -layer                        dump information of Layer tree." + System.lineSeparator() + "     -focus                        dump information of Focus tree." + System.lineSeparator() + "     -frontend                     dump information of Frontend." + System.lineSeparator() + "     -memory                       dump information of Memory usage." + System.lineSeparator() + "     -drawcmd                      dump skia draw command to file." + System.lineSeparator() + "     -multimodal                   dump information of multimodal event." + System.lineSeparator() + "     -rotation <value>             handle the rotation event." + System.lineSeparator() + "     -accessibility                dump information of Accessibility tree." + System.lineSeparator() + "     -accessibility <ID>           dump the property of accessibility node with ID <ID>." + System.lineSeparator() + "     -accessibility <ID> <action>  handle the accessibility event. action mapping as below:" + System.lineSeparator() + "                                       2 --- custom event" + System.lineSeparator() + "                                       10 --- click" + System.lineSeparator() + "                                       11 --- long click" + System.lineSeparator() + "                                       12 --- scroll forward" + System.lineSeparator() + "                                       13 --- scroll backward" + System.lineSeparator() + "                                       14 --- focus" + System.lineSeparator();
            } else {
                str2 = "";
            }
            if (!Arrays.asList("-h", "-element", "-render", "-focus", "-memory", "-layer", "-frontend", "-multimodal", "-accessibility", "-rotation", "-drawcmd").contains(strArr[0])) {
                str2 = "'" + strArr[0] + "' is not a valid parameter, See '-h'." + System.lineSeparator();
            }
            if (!str2.isEmpty()) {
                printString(fileDescriptor, str2);
            } else {
                nativeDump(str, fileDescriptor, strArr);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0016, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001b, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001c, code lost:
        r2.addSuppressed(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001f, code lost:
        throw r3;
     */
    private static void printString(FileDescriptor fileDescriptor, String str) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileDescriptor);
            fileOutputStream.write(str.getBytes(UTF8));
            try {
                fileOutputStream.close();
            } catch (IOException | SecurityException unused) {
                ALog.w(LOG_TAG, "Dump faild, SecurityException or IOException.");
            }
        } catch (FileNotFoundException unused2) {
            ALog.w(LOG_TAG, "Dump faild, FileNotFoundException.");
        }
    }
}
