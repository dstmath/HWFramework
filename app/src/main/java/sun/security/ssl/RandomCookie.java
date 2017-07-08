package sun.security.ssl;

import java.io.IOException;
import java.io.PrintStream;
import java.security.SecureRandom;
import sun.util.logging.PlatformLogger;

final class RandomCookie {
    byte[] random_bytes;

    RandomCookie(SecureRandom generator) {
        int gmt_unix_time;
        long temp = System.currentTimeMillis() / 1000;
        if (temp < 2147483647L) {
            gmt_unix_time = (int) temp;
        } else {
            gmt_unix_time = PlatformLogger.OFF;
        }
        this.random_bytes = new byte[32];
        generator.nextBytes(this.random_bytes);
        this.random_bytes[0] = (byte) (gmt_unix_time >> 24);
        this.random_bytes[1] = (byte) (gmt_unix_time >> 16);
        this.random_bytes[2] = (byte) (gmt_unix_time >> 8);
        this.random_bytes[3] = (byte) gmt_unix_time;
    }

    RandomCookie(HandshakeInStream m) throws IOException {
        this.random_bytes = new byte[32];
        m.read(this.random_bytes, 0, 32);
    }

    void send(HandshakeOutStream out) throws IOException {
        out.write(this.random_bytes, 0, 32);
    }

    void print(PrintStream s) {
        s.print("GMT: " + ((((this.random_bytes[0] << 24) + (this.random_bytes[1] << 16)) + (this.random_bytes[2] << 8)) + this.random_bytes[3]) + " ");
        s.print("bytes = { ");
        for (int i = 4; i < 32; i++) {
            if (i != 4) {
                s.print(", ");
            }
            s.print(this.random_bytes[i] & 255);
        }
        s.println(" }");
    }
}
