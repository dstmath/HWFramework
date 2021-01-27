package android.provider;

public abstract class OneTimeUseBuilder<T> {
    private boolean used = false;

    public abstract T build();

    /* access modifiers changed from: protected */
    public void markUsed() {
        checkNotUsed();
        this.used = true;
    }

    /* access modifiers changed from: protected */
    public void checkNotUsed() {
        if (this.used) {
            throw new IllegalStateException("This Builder should not be reused. Use a new Builder instance instead");
        }
    }
}
