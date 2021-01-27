package javax.obex;

public class ServerRequestHandler {
    private long mConnectionId = -1;

    protected ServerRequestHandler() {
    }

    public void setConnectionId(long connectionId) {
        if (connectionId < -1 || connectionId > 4294967295L) {
            throw new IllegalArgumentException("Illegal Connection ID");
        }
        this.mConnectionId = connectionId;
    }

    public long getConnectionId() {
        return this.mConnectionId;
    }

    public int onConnect(HeaderSet request, HeaderSet reply) {
        return ResponseCodes.OBEX_HTTP_OK;
    }

    public void onDisconnect(HeaderSet request, HeaderSet reply) {
    }

    public int onSetPath(HeaderSet request, HeaderSet reply, boolean backup, boolean create) {
        return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
    }

    public int onDelete(HeaderSet request, HeaderSet reply) {
        return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
    }

    public int onAbort(HeaderSet request, HeaderSet reply) {
        return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
    }

    public int onPut(Operation operation) {
        return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
    }

    public int onGet(Operation operation) {
        return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
    }

    public void onAuthenticationFailure(byte[] userName) {
    }

    public void updateStatus(String message) {
    }

    public void onClose() {
    }

    public boolean isSrmSupported() {
        return false;
    }
}
