package java.util.concurrent;

public class BrokenBarrierException extends Exception {
    private static final long serialVersionUID = 7117394618823254244L;

    public BrokenBarrierException(String message) {
        super(message);
    }
}
