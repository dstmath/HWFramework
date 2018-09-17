package android.content;

public class ActivityNotFoundException extends RuntimeException {
    public ActivityNotFoundException(String name) {
        super(name);
    }
}
