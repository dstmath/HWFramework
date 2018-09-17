package sun.net.www;

public class ApplicationLaunchException extends Exception {
    private static final long serialVersionUID = -4782286141289536883L;

    public ApplicationLaunchException(String reason) {
        super(reason);
    }
}
