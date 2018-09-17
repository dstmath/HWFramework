package jcifs.smb;

import java.io.IOException;
import java.io.InputStream;
import jcifs.dcerpc.msrpc.samr;

public class TestLocking implements Runnable {
    long delay;
    long ltime;
    int numComplete;
    int numIter;
    int numThreads;
    String url;

    public TestLocking() {
        this.numThreads = 1;
        this.numIter = 1;
        this.delay = 100;
        this.url = null;
        this.numComplete = 0;
        this.ltime = 0;
    }

    public void run() {
        int i;
        try {
            SmbFile f = new SmbFile(this.url);
            SmbFile d = new SmbFile(f.getParent());
            byte[] buf = new byte[samr.ACB_AUTOLOCK];
            for (int ii = 0; ii < this.numIter; ii++) {
                synchronized (this) {
                    this.ltime = System.currentTimeMillis();
                    wait();
                }
                try {
                    double r = Math.random();
                    if (r < 0.333d) {
                        f.exists();
                    } else if (r < 0.667d) {
                        d.listFiles();
                    } else if (r < 1.0d) {
                        InputStream in = f.getInputStream();
                        do {
                        } while (in.read(buf) > 0);
                        in.close();
                    }
                } catch (IOException ioe) {
                    System.err.println(ioe.getMessage());
                }
            }
            i = this.numComplete;
        } catch (Exception e) {
            try {
                e.printStackTrace();
                i = this.numComplete;
            } catch (Throwable th) {
                this.numComplete++;
            }
        }
        this.numComplete = i + 1;
    }

    public static void main(String[] args) throws Exception {
        int ti;
        if (args.length < 1) {
            System.err.println("usage: TestLocking [-t <numThreads>] [-i <numIter>] [-d <delay>] url");
            System.exit(1);
        }
        TestLocking t = new TestLocking();
        t.ltime = System.currentTimeMillis();
        int ai = 0;
        while (ai < args.length) {
            if (args[ai].equals("-t")) {
                ai++;
                t.numThreads = Integer.parseInt(args[ai]);
            } else if (args[ai].equals("-i")) {
                ai++;
                t.numIter = Integer.parseInt(args[ai]);
            } else if (args[ai].equals("-d")) {
                ai++;
                t.delay = Long.parseLong(args[ai]);
            } else {
                t.url = args[ai];
            }
            ai++;
        }
        Thread[] threads = new Thread[t.numThreads];
        for (ti = 0; ti < t.numThreads; ti++) {
            threads[ti] = new Thread(t);
            System.out.print(threads[ti].getName());
            threads[ti].start();
        }
        while (t.numComplete < t.numThreads) {
            long delay;
            do {
                delay = 2;
                synchronized (t) {
                    long expire = t.ltime + t.delay;
                    long ctime = System.currentTimeMillis();
                    if (expire > ctime) {
                        delay = expire - ctime;
                    }
                }
                if (delay > 2) {
                    System.out.println("delay=" + delay);
                }
                Thread.sleep(delay);
            } while (delay > 2);
            synchronized (t) {
                t.notifyAll();
            }
        }
        for (ti = 0; ti < t.numThreads; ti++) {
            threads[ti].join();
            System.out.print(threads[ti].getName());
        }
        System.out.println();
    }
}
