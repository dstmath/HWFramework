package java.lang;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public abstract class Process {
    public abstract void destroy();

    public abstract int exitValue();

    public abstract InputStream getErrorStream();

    public abstract InputStream getInputStream();

    public abstract OutputStream getOutputStream();

    public abstract int waitFor() throws InterruptedException;

    public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
        long startTime = System.nanoTime();
        long rem = unit.toNanos(timeout);
        do {
            try {
                exitValue();
                return true;
            } catch (IllegalThreadStateException e) {
                if (rem > 0) {
                    Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100));
                }
                rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);
                if (rem <= 0) {
                    return false;
                }
            }
        } while (rem <= 0);
        return false;
    }

    public Process destroyForcibly() {
        destroy();
        return this;
    }

    public boolean isAlive() {
        try {
            exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }
}
