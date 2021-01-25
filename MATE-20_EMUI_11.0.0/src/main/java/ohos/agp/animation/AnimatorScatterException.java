package ohos.agp.animation;

public class AnimatorScatterException extends RuntimeException {
    private static final long serialVersionUID = 3381640254491196895L;

    public AnimatorScatterException(String str, Throwable th) {
        super(str, th);
    }

    public AnimatorScatterException(String str) {
        super(str);
    }
}
