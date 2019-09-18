package android.widget;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.wifi.WifiEnterpriseConfig;
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

    /* access modifiers changed from: protected */
    public int compare(String word, String letter) {
        String firstLetter;
        if (word.length() == 0) {
            firstLetter = WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
        } else {
            firstLetter = word.substring(0, 1);
        }
        return this.mCollator.compare(firstLetter, letter);
    }

    public int getPositionForSection(int sectionIndex) {
        SparseIntArray alphaMap = this.mAlphaMap;
        Cursor cursor = this.mDataCursor;
        if (cursor == null || this.mAlphabet == null || sectionIndex <= 0) {
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
        int key = letter;
        int i = alphaMap.get(key, Integer.MIN_VALUE);
        int pos = i;
        if (Integer.MIN_VALUE != i) {
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
        int pos2 = (end + start) / 2;
        while (true) {
            if (pos2 >= end) {
                break;
            }
            cursor.moveToPosition(pos2);
            String curName = cursor.getString(this.mColumnIndex);
            if (curName != null) {
                int diff = compare(curName, targetLetter);
                if (diff != 0) {
                    if (diff < 0) {
                        start = pos2 + 1;
                        if (start >= count) {
                            pos2 = count;
                            break;
                        }
                    } else {
                        end = pos2;
                    }
                } else if (start == pos2) {
                    break;
                } else {
                    end = pos2;
                }
                pos2 = (start + end) / 2;
            } else if (pos2 == 0) {
                break;
            } else {
                pos2--;
            }
        }
        alphaMap.put(key, pos2);
        cursor.moveToPosition(savedCursorPos);
        return pos2;
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
