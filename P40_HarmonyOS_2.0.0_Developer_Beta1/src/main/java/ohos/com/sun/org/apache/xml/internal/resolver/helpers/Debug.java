package ohos.com.sun.org.apache.xml.internal.resolver.helpers;

import java.io.PrintStream;

public class Debug {
    protected int debug = 0;

    public void setDebug(int i) {
        this.debug = i;
    }

    public int getDebug() {
        return this.debug;
    }

    public void message(int i, String str) {
        if (this.debug >= i) {
            System.out.println(str);
        }
    }

    public void message(int i, String str, String str2) {
        if (this.debug >= i) {
            PrintStream printStream = System.out;
            printStream.println(str + ": " + str2);
        }
    }

    public void message(int i, String str, String str2, String str3) {
        if (this.debug >= i) {
            PrintStream printStream = System.out;
            printStream.println(str + ": " + str2);
            PrintStream printStream2 = System.out;
            printStream2.println("\t" + str3);
        }
    }
}
