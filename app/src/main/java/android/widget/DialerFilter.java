package android.widget;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputFilter.AllCaps;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.text.method.KeyListener;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import com.android.internal.R;
import com.android.internal.telephony.RILConstants;
import com.hisi.perfhub.PerfHub;
import com.huawei.hwperformance.HwPerformance;

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

    protected void onFinishInflate() {
        super.onFinishInflate();
        InputFilter[] inputFilterArr = new InputFilter[DIGITS_AND_LETTERS];
        inputFilterArr[0] = new AllCaps();
        this.mInputFilters = inputFilterArr;
        this.mHint = (EditText) findViewById(R.id.hint);
        if (this.mHint == null) {
            throw new IllegalStateException("DialerFilter must have a child EditText named hint");
        }
        this.mHint.setFilters(this.mInputFilters);
        this.mLetters = this.mHint;
        this.mLetters.setKeyListener(TextKeyListener.getInstance());
        this.mLetters.setMovementMethod(null);
        this.mLetters.setFocusable(false);
        this.mPrimary = (EditText) findViewById(R.id.primary);
        if (this.mPrimary == null) {
            throw new IllegalStateException("DialerFilter must have a child EditText named primary");
        }
        this.mPrimary.setFilters(this.mInputFilters);
        this.mDigits = this.mPrimary;
        this.mDigits.setKeyListener(DialerKeyListener.getInstance());
        this.mDigits.setMovementMethod(null);
        this.mDigits.setFocusable(false);
        this.mIcon = (ImageView) findViewById(R.id.icon);
        setFocusable(true);
        this.mIsQwerty = true;
        setMode(DIGITS_AND_LETTERS);
    }

    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (this.mIcon != null) {
            this.mIcon.setVisibility(focused ? 0 : 8);
        }
    }

    public boolean isQwertyKeyboard() {
        return this.mIsQwerty;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        switch (keyCode) {
            case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
            case HwPerformance.PERF_TAG_TASK_FORK_ON_B_CLUSTER /*20*/:
            case HwPerformance.PERF_TAG_DEF_L_CPU_MIN /*21*/:
            case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
            case HwPerformance.PERF_TAG_DEF_B_CPU_MIN /*23*/:
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                break;
            case RILConstants.RIL_REQUEST_STK_GET_PROFILE /*67*/:
                switch (this.mMode) {
                    case DIGITS_AND_LETTERS /*1*/:
                        handled = this.mDigits.onKeyDown(keyCode, event) & this.mLetters.onKeyDown(keyCode, event);
                        break;
                    case DIGITS_AND_LETTERS_NO_DIGITS /*2*/:
                        handled = this.mLetters.onKeyDown(keyCode, event);
                        if (this.mLetters.getText().length() == this.mDigits.getText().length()) {
                            setMode(DIGITS_AND_LETTERS);
                            break;
                        }
                        break;
                    case DIGITS_AND_LETTERS_NO_LETTERS /*3*/:
                        if (this.mDigits.getText().length() == this.mLetters.getText().length()) {
                            this.mLetters.onKeyDown(keyCode, event);
                            setMode(DIGITS_AND_LETTERS);
                        }
                        handled = this.mDigits.onKeyDown(keyCode, event);
                        break;
                    case DIGITS_ONLY /*4*/:
                        handled = this.mDigits.onKeyDown(keyCode, event);
                        break;
                    case LETTERS_ONLY /*5*/:
                        handled = this.mLetters.onKeyDown(keyCode, event);
                        break;
                    default:
                        break;
                }
            default:
                switch (this.mMode) {
                    case DIGITS_AND_LETTERS /*1*/:
                        handled = this.mLetters.onKeyDown(keyCode, event);
                        if (!KeyEvent.isModifierKey(keyCode)) {
                            if (!(event.isPrintingKey() || keyCode == 62)) {
                                if (keyCode == 61) {
                                }
                            }
                            if (event.getMatch(DialerKeyListener.CHARACTERS) == '\u0000') {
                                setMode(DIGITS_AND_LETTERS_NO_DIGITS);
                                break;
                            }
                            handled &= this.mDigits.onKeyDown(keyCode, event);
                            break;
                        }
                        this.mDigits.onKeyDown(keyCode, event);
                        handled = true;
                        break;
                        break;
                    case DIGITS_AND_LETTERS_NO_DIGITS /*2*/:
                    case LETTERS_ONLY /*5*/:
                        handled = this.mLetters.onKeyDown(keyCode, event);
                        break;
                    case DIGITS_AND_LETTERS_NO_LETTERS /*3*/:
                    case DIGITS_ONLY /*4*/:
                        handled = this.mDigits.onKeyDown(keyCode, event);
                        break;
                }
                break;
        }
        if (handled) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return !this.mLetters.onKeyUp(keyCode, event) ? this.mDigits.onKeyUp(keyCode, event) : true;
    }

    public int getMode() {
        return this.mMode;
    }

    public void setMode(int newMode) {
        switch (newMode) {
            case DIGITS_AND_LETTERS /*1*/:
                makeDigitsPrimary();
                this.mLetters.setVisibility(0);
                this.mDigits.setVisibility(0);
                break;
            case DIGITS_AND_LETTERS_NO_DIGITS /*2*/:
                makeLettersPrimary();
                this.mLetters.setVisibility(0);
                this.mDigits.setVisibility(DIGITS_ONLY);
                break;
            case DIGITS_AND_LETTERS_NO_LETTERS /*3*/:
                makeDigitsPrimary();
                this.mLetters.setVisibility(DIGITS_ONLY);
                this.mDigits.setVisibility(0);
                break;
            case DIGITS_ONLY /*4*/:
                makeDigitsPrimary();
                this.mLetters.setVisibility(8);
                this.mDigits.setVisibility(0);
                break;
            case LETTERS_ONLY /*5*/:
                makeLettersPrimary();
                this.mLetters.setVisibility(0);
                this.mDigits.setVisibility(8);
                break;
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
        this.mLetters.setText((CharSequence) lettersText);
        lettersText = this.mLetters.getText();
        Selection.setSelection(lettersText, lettersText.length());
        this.mDigits.setKeyListener(digitsInput);
        this.mDigits.setText((CharSequence) digitsText);
        digitsText = this.mDigits.getText();
        Selection.setSelection(digitsText, digitsText.length());
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
        if (this.mMode != DIGITS_ONLY) {
            return getLetters();
        }
        return getDigits();
    }

    public void append(String text) {
        switch (this.mMode) {
            case DIGITS_AND_LETTERS /*1*/:
                this.mDigits.getText().append((CharSequence) text);
                this.mLetters.getText().append((CharSequence) text);
            case DIGITS_AND_LETTERS_NO_DIGITS /*2*/:
            case LETTERS_ONLY /*5*/:
                this.mLetters.getText().append((CharSequence) text);
            case DIGITS_AND_LETTERS_NO_LETTERS /*3*/:
            case DIGITS_ONLY /*4*/:
                this.mDigits.getText().append((CharSequence) text);
            default:
        }
    }

    public void clearText() {
        this.mLetters.getText().clear();
        this.mDigits.getText().clear();
        if (this.mIsQwerty) {
            setMode(DIGITS_AND_LETTERS);
        } else {
            setMode(DIGITS_ONLY);
        }
    }

    public void setLettersWatcher(TextWatcher watcher) {
        CharSequence text = this.mLetters.getText();
        ((Spannable) text).setSpan(watcher, 0, text.length(), 18);
    }

    public void setDigitsWatcher(TextWatcher watcher) {
        CharSequence text = this.mDigits.getText();
        ((Spannable) text).setSpan(watcher, 0, text.length(), 18);
    }

    public void setFilterWatcher(TextWatcher watcher) {
        if (this.mMode != DIGITS_ONLY) {
            setLettersWatcher(watcher);
        } else {
            setDigitsWatcher(watcher);
        }
    }

    public void removeFilterWatcher(TextWatcher watcher) {
        Spannable text;
        if (this.mMode != DIGITS_ONLY) {
            text = this.mLetters.getText();
        } else {
            text = this.mDigits.getText();
        }
        text.removeSpan(watcher);
    }

    protected void onModeChange(int oldMode, int newMode) {
    }
}
