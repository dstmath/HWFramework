package android.widget;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.text.method.KeyListener;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;

@Deprecated
public class DialerFilter extends RelativeLayout {
    public static final int DIGITS_AND_LETTERS = 1;
    public static final int DIGITS_AND_LETTERS_NO_DIGITS = 2;
    public static final int DIGITS_AND_LETTERS_NO_LETTERS = 3;
    public static final int DIGITS_ONLY = 4;
    public static final int LETTERS_ONLY = 5;
    EditText mDigits;
    EditText mHint;
    ImageView mIcon;
    InputFilter[] mInputFilters;
    private boolean mIsQwerty;
    EditText mLetters;
    int mMode;
    EditText mPrimary;

    public DialerFilter(Context context) {
        super(context);
    }

    public DialerFilter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mInputFilters = new InputFilter[]{new InputFilter.AllCaps()};
        this.mHint = (EditText) findViewById(16908293);
        EditText editText = this.mHint;
        if (editText != null) {
            editText.setFilters(this.mInputFilters);
            this.mLetters = this.mHint;
            this.mLetters.setKeyListener(TextKeyListener.getInstance());
            this.mLetters.setMovementMethod(null);
            this.mLetters.setFocusable(false);
            this.mPrimary = (EditText) findViewById(16908300);
            EditText editText2 = this.mPrimary;
            if (editText2 != null) {
                editText2.setFilters(this.mInputFilters);
                this.mDigits = this.mPrimary;
                this.mDigits.setKeyListener(DialerKeyListener.getInstance());
                this.mDigits.setMovementMethod(null);
                this.mDigits.setFocusable(false);
                this.mIcon = (ImageView) findViewById(16908294);
                setFocusable(true);
                this.mIsQwerty = true;
                setMode(1);
                return;
            }
            throw new IllegalStateException("DialerFilter must have a child EditText named primary");
        }
        throw new IllegalStateException("DialerFilter must have a child EditText named hint");
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        ImageView imageView = this.mIcon;
        if (imageView != null) {
            imageView.setVisibility(focused ? 0 : 8);
        }
    }

    public boolean isQwertyKeyboard() {
        return this.mIsQwerty;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001b, code lost:
        if (r1 != 5) goto L_0x00d6;
     */
    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        if (keyCode != 66) {
            if (keyCode != 67) {
                switch (keyCode) {
                    case 19:
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                        break;
                    default:
                        int i = this.mMode;
                        if (i == 1) {
                            handled = this.mLetters.onKeyDown(keyCode, event);
                            if (!KeyEvent.isModifierKey(keyCode)) {
                                if (event.isPrintingKey() || keyCode == 62 || keyCode == 61) {
                                    if (event.getMatch(DialerKeyListener.CHARACTERS) == 0) {
                                        setMode(2);
                                        break;
                                    } else {
                                        handled &= this.mDigits.onKeyDown(keyCode, event);
                                        break;
                                    }
                                }
                            } else {
                                this.mDigits.onKeyDown(keyCode, event);
                                handled = true;
                                break;
                            }
                        } else {
                            if (i != 2) {
                                if (i == 3 || i == 4) {
                                    handled = this.mDigits.onKeyDown(keyCode, event);
                                    break;
                                }
                            }
                            handled = this.mLetters.onKeyDown(keyCode, event);
                            break;
                        }
                }
            } else {
                int i2 = this.mMode;
                if (i2 == 1) {
                    handled = this.mDigits.onKeyDown(keyCode, event) & this.mLetters.onKeyDown(keyCode, event);
                } else if (i2 == 2) {
                    handled = this.mLetters.onKeyDown(keyCode, event);
                    if (this.mLetters.getText().length() == this.mDigits.getText().length()) {
                        setMode(1);
                    }
                } else if (i2 == 3) {
                    if (this.mDigits.getText().length() == this.mLetters.getText().length()) {
                        this.mLetters.onKeyDown(keyCode, event);
                        setMode(1);
                    }
                    handled = this.mDigits.onKeyDown(keyCode, event);
                } else if (i2 == 4) {
                    handled = this.mDigits.onKeyDown(keyCode, event);
                } else if (i2 == 5) {
                    handled = this.mLetters.onKeyDown(keyCode, event);
                }
            }
        }
        if (!handled) {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override // android.view.KeyEvent.Callback, android.view.View
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return this.mLetters.onKeyUp(keyCode, event) || this.mDigits.onKeyUp(keyCode, event);
    }

    public int getMode() {
        return this.mMode;
    }

    public void setMode(int newMode) {
        if (newMode == 1) {
            makeDigitsPrimary();
            this.mLetters.setVisibility(0);
            this.mDigits.setVisibility(0);
        } else if (newMode == 2) {
            makeLettersPrimary();
            this.mLetters.setVisibility(0);
            this.mDigits.setVisibility(4);
        } else if (newMode == 3) {
            makeDigitsPrimary();
            this.mLetters.setVisibility(4);
            this.mDigits.setVisibility(0);
        } else if (newMode == 4) {
            makeDigitsPrimary();
            this.mLetters.setVisibility(8);
            this.mDigits.setVisibility(0);
        } else if (newMode == 5) {
            makeLettersPrimary();
            this.mLetters.setVisibility(0);
            this.mDigits.setVisibility(8);
        }
        int oldMode = this.mMode;
        this.mMode = newMode;
        onModeChange(oldMode, newMode);
    }

    private void makeLettersPrimary() {
        if (this.mPrimary == this.mDigits) {
            swapPrimaryAndHint(true);
        }
    }

    private void makeDigitsPrimary() {
        if (this.mPrimary == this.mLetters) {
            swapPrimaryAndHint(false);
        }
    }

    private void swapPrimaryAndHint(boolean makeLettersPrimary) {
        Editable lettersText = this.mLetters.getText();
        Editable digitsText = this.mDigits.getText();
        KeyListener lettersInput = this.mLetters.getKeyListener();
        KeyListener digitsInput = this.mDigits.getKeyListener();
        if (makeLettersPrimary) {
            this.mLetters = this.mPrimary;
            this.mDigits = this.mHint;
        } else {
            this.mLetters = this.mHint;
            this.mDigits = this.mPrimary;
        }
        this.mLetters.setKeyListener(lettersInput);
        this.mLetters.setText(lettersText);
        Editable lettersText2 = this.mLetters.getText();
        Selection.setSelection(lettersText2, lettersText2.length());
        this.mDigits.setKeyListener(digitsInput);
        this.mDigits.setText(digitsText);
        Editable digitsText2 = this.mDigits.getText();
        Selection.setSelection(digitsText2, digitsText2.length());
        this.mPrimary.setFilters(this.mInputFilters);
        this.mHint.setFilters(this.mInputFilters);
    }

    public CharSequence getLetters() {
        if (this.mLetters.getVisibility() == 0) {
            return this.mLetters.getText();
        }
        return "";
    }

    public CharSequence getDigits() {
        if (this.mDigits.getVisibility() == 0) {
            return this.mDigits.getText();
        }
        return "";
    }

    public CharSequence getFilterText() {
        if (this.mMode != 4) {
            return getLetters();
        }
        return getDigits();
    }

    public void append(String text) {
        int i = this.mMode;
        if (i != 1) {
            if (i != 2) {
                if (i == 3 || i == 4) {
                    this.mDigits.getText().append((CharSequence) text);
                    return;
                } else if (i != 5) {
                    return;
                }
            }
            this.mLetters.getText().append((CharSequence) text);
            return;
        }
        this.mDigits.getText().append((CharSequence) text);
        this.mLetters.getText().append((CharSequence) text);
    }

    public void clearText() {
        this.mLetters.getText().clear();
        this.mDigits.getText().clear();
        if (this.mIsQwerty) {
            setMode(1);
        } else {
            setMode(4);
        }
    }

    public void setLettersWatcher(TextWatcher watcher) {
        Spannable text = this.mLetters.getText();
        text.setSpan(watcher, 0, text.length(), 18);
    }

    public void setDigitsWatcher(TextWatcher watcher) {
        Spannable text = this.mDigits.getText();
        text.setSpan(watcher, 0, text.length(), 18);
    }

    public void setFilterWatcher(TextWatcher watcher) {
        if (this.mMode != 4) {
            setLettersWatcher(watcher);
        } else {
            setDigitsWatcher(watcher);
        }
    }

    public void removeFilterWatcher(TextWatcher watcher) {
        Spannable text;
        if (this.mMode != 4) {
            text = this.mLetters.getText();
        } else {
            text = this.mDigits.getText();
        }
        text.removeSpan(watcher);
    }

    /* access modifiers changed from: protected */
    public void onModeChange(int oldMode, int newMode) {
    }
}
