package android.text;

public interface Editable extends CharSequence, GetChars, Spannable, Appendable {

    public static class Factory {
        private static Factory sInstance = new Factory();

        public static Factory getInstance() {
            return sInstance;
        }

        public Editable newEditable(CharSequence source) {
            return new SpannableStringBuilder(source);
        }
    }

    Editable append(char c);

    Editable append(CharSequence charSequence);

    Editable append(CharSequence charSequence, int i, int i2);

    void clear();

    void clearSpans();

    Editable delete(int i, int i2);

    InputFilter[] getFilters();

    Editable insert(int i, CharSequence charSequence);

    Editable insert(int i, CharSequence charSequence, int i2, int i3);

    Editable replace(int i, int i2, CharSequence charSequence);

    Editable replace(int i, int i2, CharSequence charSequence, int i3, int i4);

    void setFilters(InputFilter[] inputFilterArr);
}
