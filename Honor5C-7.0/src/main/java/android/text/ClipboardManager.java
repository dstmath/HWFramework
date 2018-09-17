package android.text;

@Deprecated
public abstract class ClipboardManager {
    public abstract CharSequence getText();

    public abstract boolean hasText();

    public abstract void setText(CharSequence charSequence);
}
