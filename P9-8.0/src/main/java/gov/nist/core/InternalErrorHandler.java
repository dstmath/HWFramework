package gov.nist.core;

public class InternalErrorHandler {
    public static void handleException(Exception ex) throws RuntimeException {
        System.err.println("Unexpected internal error FIXME!! " + ex.getMessage());
        ex.printStackTrace();
        throw new RuntimeException("Unexpected internal error FIXME!! " + ex.getMessage(), ex);
    }

    public static void handleException(Exception ex, StackLogger stackLogger) {
        System.err.println("Unexpected internal error FIXME!! " + ex.getMessage());
        stackLogger.logError("UNEXPECTED INTERNAL ERROR FIXME " + ex.getMessage());
        ex.printStackTrace();
        stackLogger.logException(ex);
        throw new RuntimeException("Unexpected internal error FIXME!! " + ex.getMessage(), ex);
    }

    public static void handleException(String emsg) {
        new Exception().printStackTrace();
        System.err.println("Unexepcted INTERNAL ERROR FIXME!!");
        System.err.println(emsg);
        throw new RuntimeException(emsg);
    }

    public static void handleException(String emsg, StackLogger stackLogger) {
        stackLogger.logStackTrace();
        stackLogger.logError("Unexepcted INTERNAL ERROR FIXME!!");
        stackLogger.logFatalError(emsg);
        throw new RuntimeException(emsg);
    }
}
