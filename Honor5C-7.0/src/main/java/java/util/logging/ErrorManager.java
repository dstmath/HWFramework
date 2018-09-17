package java.util.logging;

public class ErrorManager {
    public static final int CLOSE_FAILURE = 3;
    public static final int FLUSH_FAILURE = 2;
    public static final int FORMAT_FAILURE = 5;
    public static final int GENERIC_FAILURE = 0;
    public static final int OPEN_FAILURE = 4;
    public static final int WRITE_FAILURE = 1;
    private boolean reported;

    public ErrorManager() {
        this.reported = false;
    }

    public synchronized void error(String msg, Exception ex, int code) {
        if (!this.reported) {
            this.reported = true;
            String text = "java.util.logging.ErrorManager: " + code;
            if (msg != null) {
                text = text + ": " + msg;
            }
            System.err.println(text);
            if (ex != null) {
                ex.printStackTrace();
            }
        }
    }
}
