package ohos.rpc;

public class RemoteException extends Exception {
    private static final long serialVersionUID = 1;

    public RemoteException() {
    }

    public RemoteException(String str) {
        super(str);
    }
}
