package android.text;

public abstract class LoginFilter implements InputFilter {
    private boolean mAppendInvalid;

    public static class PasswordFilterGMail extends LoginFilter {
        public PasswordFilterGMail() {
            super(false);
        }

        public PasswordFilterGMail(boolean appendInvalid) {
            super(appendInvalid);
        }

        public boolean isAllowed(char c) {
            if (' ' <= c && c <= 127) {
                return true;
            }
            if (160 > c || c > 255) {
                return false;
            }
            return true;
        }
    }

    public static class UsernameFilterGMail extends LoginFilter {
        public UsernameFilterGMail() {
            super(false);
        }

        public UsernameFilterGMail(boolean appendInvalid) {
            super(appendInvalid);
        }

        public boolean isAllowed(char c) {
            if ('0' <= c && c <= '9') {
                return true;
            }
            if ('a' <= c && c <= 'z') {
                return true;
            }
            if (('A' > c || c > 'Z') && '.' != c) {
                return false;
            }
            return true;
        }
    }

    public static class UsernameFilterGeneric extends LoginFilter {
        private static final String mAllowed = "@_-+.";

        public UsernameFilterGeneric() {
            super(false);
        }

        public UsernameFilterGeneric(boolean appendInvalid) {
            super(appendInvalid);
        }

        public boolean isAllowed(char c) {
            if ('0' <= c && c <= '9') {
                return true;
            }
            if ('a' <= c && c <= 'z') {
                return true;
            }
            if (('A' > c || c > 'Z') && mAllowed.indexOf(c) == -1) {
                return false;
            }
            return true;
        }
    }

    public abstract boolean isAllowed(char c);

    LoginFilter(boolean appendInvalid) {
        this.mAppendInvalid = appendInvalid;
    }

    LoginFilter() {
        this.mAppendInvalid = false;
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        onStart();
        for (int i = 0; i < dstart; i++) {
            char c = dest.charAt(i);
            if (!isAllowed(c)) {
                onInvalidCharacter(c);
            }
        }
        int modoff = 0;
        SpannableStringBuilder modification = null;
        for (int i2 = start; i2 < end; i2++) {
            char c2 = source.charAt(i2);
            if (isAllowed(c2)) {
                modoff++;
            } else {
                if (this.mAppendInvalid) {
                    modoff++;
                } else {
                    if (modification == null) {
                        modification = new SpannableStringBuilder(source, start, end);
                        modoff = i2 - start;
                    }
                    modification.delete(modoff, modoff + 1);
                }
                onInvalidCharacter(c2);
            }
        }
        for (int i3 = dend; i3 < dest.length(); i3++) {
            char c3 = dest.charAt(i3);
            if (!isAllowed(c3)) {
                onInvalidCharacter(c3);
            }
        }
        onStop();
        return modification;
    }

    public void onStart() {
    }

    public void onInvalidCharacter(char c) {
    }

    public void onStop() {
    }
}
