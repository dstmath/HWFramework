package jcifs.util;

import java.io.PrintStream;

public class LogStream extends PrintStream {
    private static LogStream inst;
    public static int level = 1;

    public LogStream(PrintStream stream) {
        super(stream);
    }

    public static void setLevel(int level) {
        level = level;
    }

    public static void setInstance(PrintStream stream) {
        inst = new LogStream(stream);
    }

    public static LogStream getInstance() {
        if (inst == null) {
            setInstance(System.err);
        }
        return inst;
    }
}
