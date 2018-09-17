package java.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

class DeleteOnExitHook {
    private static LinkedHashSet<String> files = new LinkedHashSet();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                DeleteOnExitHook.runHooks();
            }
        });
    }

    private DeleteOnExitHook() {
    }

    static synchronized void add(String file) {
        synchronized (DeleteOnExitHook.class) {
            if (files == null) {
                throw new IllegalStateException("Shutdown in progress");
            }
            files.add(file);
        }
    }

    static void runHooks() {
        Collection theFiles;
        synchronized (DeleteOnExitHook.class) {
            theFiles = files;
            files = null;
        }
        ArrayList<String> toBeDeleted = new ArrayList(theFiles);
        Collections.reverse(toBeDeleted);
        for (String filename : toBeDeleted) {
            new File(filename).delete();
        }
    }
}
