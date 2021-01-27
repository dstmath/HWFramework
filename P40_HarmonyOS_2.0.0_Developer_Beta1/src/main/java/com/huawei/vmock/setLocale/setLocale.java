package com.huawei.vmock.setLocale;

import com.android.internal.app.LocalePicker;
import java.io.PrintStream;
import java.util.Locale;

public class setLocale {
    public static void main(String[] args) {
        Locale locale = null;
        if (args.length == 1) {
            locale = new Locale(args[0]);
            PrintStream printStream = System.err;
            printStream.println("Lauguage:  " + args[0]);
        } else if (args.length == 2) {
            locale = new Locale(args[0], args[1]);
            PrintStream printStream2 = System.err;
            printStream2.println("Lauguage:  " + args[0]);
            PrintStream printStream3 = System.err;
            printStream3.println("Country:  " + args[1]);
        } else if (args.length == 3) {
            locale = new Locale(args[0], args[1], args[2]);
            PrintStream printStream4 = System.err;
            printStream4.println("Lauguage:  " + args[0]);
            PrintStream printStream5 = System.err;
            printStream5.println("Country:  " + args[1]);
            PrintStream printStream6 = System.err;
            printStream6.println("variant:  " + args[2]);
        }
        if (locale != null) {
            try {
                Locale.Builder builder = new Locale.Builder();
                builder.setLocale(locale);
                LocalePicker.updateLocale(builder.build());
            } catch (Exception e) {
                System.err.println("set locale fail.");
            }
        } else {
            System.err.println("Error: format error.");
        }
    }
}
