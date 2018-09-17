package android.widget;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.util.SparseIntArray;
import java.text.Collator;

public class AlphabetIndexer extends DataSetObserver implements SectionIndexer {
    private SparseIntArray mAlphaMap;
    protected CharSequence mAlphabet;
    private String[] mAlphabetArray = new String[this.mAlphabetLength];
    private int mAlphabetLength;
    private Collator mCollator;
    protected int mColumnIndex;
    protected Cursor mDataCursor;

    public AlphabetIndexer(Cursor cursor, int sortedColumnIndex, CharSequence alphabet) {
        this.mDataCursor = cursor;
        this.mColumnIndex = sortedColumnIndex;
        this.mAlphabet = alphabet;
        this.mAlphabetLength = alphabet.length();
        for (int i = 0; i < this.mAlphabetLength; i++) {
            this.mAlphabetArray[i] = Character.toString(this.mAlphabet.charAt(i));
        }
        this.mAlphaMap = new SparseIntArray(this.mAlphabetLength);
        if (cursor != null) {
            cursor.registerDataSetObserver(this);
        }
        this.mCollator = Collator.getInstance();
        this.mCollator.setStrength(0);
    }

    public Object[] getSections() {
        return this.mAlphabetArray;
    }

    public void setCursor(Cursor cursor) {
        if (this.mDataCursor != null) {
            this.mDataCursor.unregisterDataSetObserver(this);
        }
        this.mDataCursor = cursor;
        if (cursor != null) {
            this.mDataCursor.registerDataSetObserver(this);
        }
        this.mAlphaMap.clear();
    }

    protected int compare(String word, String letter) {
        String firstLetter;
        if (word.length() == 0) {
            firstLetter = " ";
        } else {
            firstLetter = word.substring(0, 1);
        }
        return this.mCollator.compare(firstLetter, letter);
    }

    public int getPositionForSection(int sectionIndex) {
        SparseIntArray alphaMap = this.mAlphaMap;
        Cursor cursor = this.mDataCursor;
        if (cursor == null || this.mAlphabet == null) {
            return 0;
        }
        if (sectionIndex <= 0) {
            return 0;
        }
        if (sectionIndex >= this.mAlphabetLength) {
            sectionIndex = this.mAlphabetLength - 1;
        }
        int savedCursorPos = cursor.getPosition();
        int count = cursor.getCount();
        int start = 0;
        int end = count;
        char letter = this.mAlphabet.charAt(sectionIndex);
        String targetLetter = Character.toString(letter);
        char key = letter;
        int pos = alphaMap.get(key, Integer.MIN_VALUE);
        if (Integer.MIN_VALUE != pos) {
            if (pos >= 0) {
                return pos;
            }
            end = -pos;
        }
        if (sectionIndex > 0) {
            int prevLetterPos = alphaMap.get(this.mAlphabet.charAt(sectionIndex - 1), Integer.MIN_VALUE);
            if (prevLetterPos != Integer.MIN_VALUE) {
                start = Math.abs(prevLetterPos);
            }
        }
        pos = (end + start) / 2;
        while (pos < end) {
            cursor.moveToPosition(pos);
            String curName = cursor.getString(this.mColumnIndex);
            if (curName != null) {
                int diff = compare(curName, targetLetter);
                if (diff == 0) {
                    if (start == pos) {
                        break;
                    }
                    end = pos;
                } else if (diff < 0) {
                    start = pos + 1;
                    if (start >= count) {
                        pos = count;
                        break;
                    }
                } else {
                    end = pos;
                }
                pos = (start + end) / 2;
            } else if (pos == 0) {
                break;
            } else {
                pos--;
            }
        }
        alphaMap.put(key, pos);
        cursor.moveToPosition(savedCursorPos);
        return pos;
    }

    public int getSectionForPosition(int position) {
        int savedCursorPos = this.mDataCursor.getPosition();
        this.mDataCursor.moveToPosition(position);
        String curName = this.mDataCursor.getString(this.mColumnIndex);
        this.mDataCursor.moveToPosition(savedCursorPos);
        for (int i = 0; i < this.mAlphabetLength; i++) {
            if (compare(curName, Character.toString(this.mAlphabet.charAt(i))) == 0) {
                return i;
            }
        }
        return 0;
    }

    public void onChanged() {
        super.onChanged();
        this.mAlphaMap.clear();
    }

    public void onInvalidated() {
        super.onInvalidated();
        this.mAlphaMap.clear();
    }
}
