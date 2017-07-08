package sun.security.action;

import java.security.PrivilegedAction;

public class LoadLibraryAction implements PrivilegedAction<Void> {
    private String theLib;

    public LoadLibraryAction(String theLib) {
        this.theLib = theLib;
    }

    public Void run() {
        System.loadLibrary(this.theLib);
        return null;
    }
}
