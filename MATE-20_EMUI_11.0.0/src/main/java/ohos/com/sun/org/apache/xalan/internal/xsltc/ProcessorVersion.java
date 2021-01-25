package ohos.com.sun.org.apache.xalan.internal.xsltc;

import java.io.PrintStream;

public class ProcessorVersion {
    private static int DELTA = 0;
    private static int MAJOR = 1;
    private static int MINOR;

    public static void main(String[] strArr) {
        String str;
        PrintStream printStream = System.out;
        StringBuilder sb = new StringBuilder();
        sb.append("XSLTC version ");
        sb.append(MAJOR);
        sb.append(".");
        sb.append(MINOR);
        if (DELTA > 0) {
            str = "." + DELTA;
        } else {
            str = "";
        }
        sb.append(str);
        printStream.println(sb.toString());
    }
}
