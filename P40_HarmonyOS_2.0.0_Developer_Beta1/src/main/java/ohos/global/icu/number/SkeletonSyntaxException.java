package ohos.global.icu.number;

public class SkeletonSyntaxException extends IllegalArgumentException {
    private static final long serialVersionUID = 7733971331648360554L;

    public SkeletonSyntaxException(String str, CharSequence charSequence) {
        super("Syntax error in skeleton string: " + str + ": " + ((Object) charSequence));
    }

    public SkeletonSyntaxException(String str, CharSequence charSequence, Throwable th) {
        super("Syntax error in skeleton string: " + str + ": " + ((Object) charSequence), th);
    }
}
