package android.text;

import android.text.format.DateFormat;

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
            if (' ' <= c && c <= '\u007f') {
                return true;
            }
            if ('\u00a0' > c || c > '\u00ff') {
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
            if (DateFormat.AM_PM <= c && c <= DateFormat.TIME_ZONE) {
                return true;
            }
            if ((DateFormat.CAPITAL_AM_PM > c || c > 'Z') && '.' != c) {
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
            if (DateFormat.AM_PM <= c && c <= DateFormat.TIME_ZONE) {
                return true;
            }
            if ((DateFormat.CAPITAL_AM_PM > c || c > 'Z') && mAllowed.indexOf(c) == -1) {
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
        int i;
        onStart();
        for (i = 0; i < dstart; i++) {
            char c = dest.charAt(i);
            if (!isAllowed(c)) {
                onInvalidCharacter(c);
            }
        }
        SpannableStringBuilder modification = null;
        int modoff = 0;
        for (i = start; i < end; i++) {
            c = source.charAt(i);
            if (isAllowed(c)) {
                modoff++;
            } else {
                if (this.mAppendInvalid) {
                    modoff++;
                } else {
                    if (modification == null) {
                        modification = new SpannableStringBuilder(source, start, end);
                        modoff = i - start;
                    }
                    modification.delete(modoff, modoff + 1);
                }
                onInvalidCharacter(c);
            }
        }
        for (i = dend; i < dest.length(); i++) {
            c = dest.charAt(i);
            if (!isAllowed(c)) {
                onInvalidCharacter(c);
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
