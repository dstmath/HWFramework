package android.text;

public interface Spannable extends Spanned {

    public static class Factory {
        private static Factory sInstance = new Factory();

        public static Factory getInstance() {
            return sInstance;
        }

        public Spannable newSpannable(CharSequence source) {
            return new SpannableString(source);
        }
    }

    void removeSpan(Object obj);

    void setSpan(Object obj, int i, int i2, int i3);
}
