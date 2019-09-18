package android.icu.impl.number;

import android.icu.impl.UCharacterProperty;
import android.icu.text.NumberFormat;
import android.icu.text.SymbolTable;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.FieldPosition;
import java.text.Format;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NumberStringBuilder implements CharSequence {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final NumberStringBuilder EMPTY = new NumberStringBuilder();
    private static final Map<NumberFormat.Field, Character> fieldToDebugChar = new HashMap();
    private char[] chars;
    private NumberFormat.Field[] fields;
    private int length;
    private int zero;

    static {
        fieldToDebugChar.put(NumberFormat.Field.SIGN, '-');
        fieldToDebugChar.put(NumberFormat.Field.INTEGER, Character.valueOf(UCharacterProperty.LATIN_SMALL_LETTER_I_));
        fieldToDebugChar.put(NumberFormat.Field.FRACTION, 'f');
        fieldToDebugChar.put(NumberFormat.Field.EXPONENT, 'e');
        fieldToDebugChar.put(NumberFormat.Field.EXPONENT_SIGN, '+');
        fieldToDebugChar.put(NumberFormat.Field.EXPONENT_SYMBOL, 'E');
        fieldToDebugChar.put(NumberFormat.Field.DECIMAL_SEPARATOR, '.');
        fieldToDebugChar.put(NumberFormat.Field.GROUPING_SEPARATOR, ',');
        fieldToDebugChar.put(NumberFormat.Field.PERCENT, '%');
        fieldToDebugChar.put(NumberFormat.Field.PERMILLE, 8240);
        fieldToDebugChar.put(NumberFormat.Field.CURRENCY, Character.valueOf(SymbolTable.SYMBOL_REF));
    }

    public NumberStringBuilder() {
        this(40);
    }

    public NumberStringBuilder(int capacity) {
        this.chars = new char[capacity];
        this.fields = new NumberFormat.Field[capacity];
        this.zero = capacity / 2;
        this.length = 0;
    }

    public NumberStringBuilder(NumberStringBuilder source) {
        copyFrom(source);
    }

    public void copyFrom(NumberStringBuilder source) {
        this.chars = Arrays.copyOf(source.chars, source.chars.length);
        this.fields = (NumberFormat.Field[]) Arrays.copyOf(source.fields, source.fields.length);
        this.zero = source.zero;
        this.length = source.length;
    }

    public int length() {
        return this.length;
    }

    public int codePointCount() {
        return Character.codePointCount(this, 0, length());
    }

    public char charAt(int index) {
        return this.chars[this.zero + index];
    }

    public NumberFormat.Field fieldAt(int index) {
        return this.fields[this.zero + index];
    }

    public int getFirstCodePoint() {
        if (this.length == 0) {
            return -1;
        }
        return Character.codePointAt(this.chars, this.zero, this.zero + this.length);
    }

    public int getLastCodePoint() {
        if (this.length == 0) {
            return -1;
        }
        return Character.codePointBefore(this.chars, this.zero + this.length, this.zero);
    }

    public int codePointAt(int index) {
        return Character.codePointAt(this.chars, this.zero + index, this.zero + this.length);
    }

    public int codePointBefore(int index) {
        return Character.codePointBefore(this.chars, this.zero + index, this.zero);
    }

    public NumberStringBuilder clear() {
        this.zero = getCapacity() / 2;
        this.length = 0;
        return this;
    }

    public int appendCodePoint(int codePoint, NumberFormat.Field field) {
        return insertCodePoint(this.length, codePoint, field);
    }

    public int insertCodePoint(int index, int codePoint, NumberFormat.Field field) {
        int count = Character.charCount(codePoint);
        int position = prepareForInsert(index, count);
        Character.toChars(codePoint, this.chars, position);
        this.fields[position] = field;
        if (count == 2) {
            this.fields[position + 1] = field;
        }
        return count;
    }

    public int append(CharSequence sequence, NumberFormat.Field field) {
        return insert(this.length, sequence, field);
    }

    public int insert(int index, CharSequence sequence, NumberFormat.Field field) {
        if (sequence.length() == 0) {
            return 0;
        }
        if (sequence.length() == 1) {
            return insertCodePoint(index, sequence.charAt(0), field);
        }
        return insert(index, sequence, 0, sequence.length(), field);
    }

    public int insert(int index, CharSequence sequence, int start, int end, NumberFormat.Field field) {
        int count = end - start;
        int position = prepareForInsert(index, count);
        for (int i = 0; i < count; i++) {
            this.chars[position + i] = sequence.charAt(start + i);
            this.fields[position + i] = field;
        }
        return count;
    }

    public int append(char[] chars2, NumberFormat.Field[] fields2) {
        return insert(this.length, chars2, fields2);
    }

    public int insert(int index, char[] chars2, NumberFormat.Field[] fields2) {
        int count = chars2.length;
        if (count == 0) {
            return 0;
        }
        int position = prepareForInsert(index, count);
        for (int i = 0; i < count; i++) {
            this.chars[position + i] = chars2[i];
            this.fields[position + i] = fields2 == null ? null : fields2[i];
        }
        return count;
    }

    public int append(NumberStringBuilder other) {
        return insert(this.length, other);
    }

    public int insert(int index, NumberStringBuilder other) {
        if (this != other) {
            int count = other.length;
            if (count == 0) {
                return 0;
            }
            int position = prepareForInsert(index, count);
            for (int i = 0; i < count; i++) {
                this.chars[position + i] = other.charAt(i);
                this.fields[position + i] = other.fieldAt(i);
            }
            return count;
        }
        throw new IllegalArgumentException("Cannot call insert/append on myself");
    }

    private int prepareForInsert(int index, int count) {
        if (index == 0 && this.zero - count >= 0) {
            this.zero -= count;
            this.length += count;
            return this.zero;
        } else if (index != this.length || this.zero + this.length + count >= getCapacity()) {
            return prepareForInsertHelper(index, count);
        } else {
            this.length += count;
            return (this.zero + this.length) - count;
        }
    }

    private int prepareForInsertHelper(int index, int count) {
        int oldCapacity = getCapacity();
        int oldZero = this.zero;
        char[] oldChars = this.chars;
        NumberFormat.Field[] oldFields = this.fields;
        if (this.length + count > oldCapacity) {
            int newCapacity = (this.length + count) * 2;
            int newZero = (newCapacity / 2) - ((this.length + count) / 2);
            char[] newChars = new char[newCapacity];
            NumberFormat.Field[] newFields = new NumberFormat.Field[newCapacity];
            System.arraycopy(oldChars, oldZero, newChars, newZero, index);
            System.arraycopy(oldChars, oldZero + index, newChars, newZero + index + count, this.length - index);
            System.arraycopy(oldFields, oldZero, newFields, newZero, index);
            System.arraycopy(oldFields, oldZero + index, newFields, newZero + index + count, this.length - index);
            this.chars = newChars;
            this.fields = newFields;
            this.zero = newZero;
            this.length += count;
        } else {
            int newZero2 = (oldCapacity / 2) - ((this.length + count) / 2);
            System.arraycopy(oldChars, oldZero, oldChars, newZero2, this.length);
            System.arraycopy(oldChars, newZero2 + index, oldChars, newZero2 + index + count, this.length - index);
            System.arraycopy(oldFields, oldZero, oldFields, newZero2, this.length);
            System.arraycopy(oldFields, newZero2 + index, oldFields, newZero2 + index + count, this.length - index);
            this.zero = newZero2;
            this.length += count;
        }
        return this.zero + index;
    }

    private int getCapacity() {
        return this.chars.length;
    }

    public CharSequence subSequence(int start, int end) {
        if (start < 0 || end > this.length || end < start) {
            throw new IndexOutOfBoundsException();
        }
        NumberStringBuilder other = new NumberStringBuilder(this);
        other.zero = this.zero + start;
        other.length = end - start;
        return other;
    }

    public String toString() {
        return new String(this.chars, this.zero, this.length);
    }

    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<NumberStringBuilder [");
        sb.append(toString());
        sb.append("] [");
        for (int i = this.zero; i < this.zero + this.length; i++) {
            if (this.fields[i] == null) {
                sb.append('n');
            } else {
                sb.append(fieldToDebugChar.get(this.fields[i]));
            }
        }
        sb.append("]>");
        return sb.toString();
    }

    public char[] toCharArray() {
        return Arrays.copyOfRange(this.chars, this.zero, this.zero + this.length);
    }

    public NumberFormat.Field[] toFieldArray() {
        return (NumberFormat.Field[]) Arrays.copyOfRange(this.fields, this.zero, this.zero + this.length);
    }

    public boolean contentEquals(char[] chars2, NumberFormat.Field[] fields2) {
        if (chars2.length != this.length || fields2.length != this.length) {
            return false;
        }
        for (int i = 0; i < this.length; i++) {
            if (this.chars[this.zero + i] != chars2[i] || this.fields[this.zero + i] != fields2[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean contentEquals(NumberStringBuilder other) {
        if (this.length != other.length) {
            return false;
        }
        for (int i = 0; i < this.length; i++) {
            if (charAt(i) != other.charAt(i) || fieldAt(i) != other.fieldAt(i)) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        throw new UnsupportedOperationException("Don't call #hashCode() or #equals() on a mutable.");
    }

    public boolean equals(Object other) {
        throw new UnsupportedOperationException("Don't call #hashCode() or #equals() on a mutable.");
    }

    public void populateFieldPosition(FieldPosition fp, int offset) {
        Format.Field rawField = fp.getFieldAttribute();
        if (rawField == null) {
            if (fp.getField() == 0) {
                rawField = NumberFormat.Field.INTEGER;
            } else if (fp.getField() == 1) {
                rawField = NumberFormat.Field.FRACTION;
            } else {
                return;
            }
        }
        if (rawField instanceof NumberFormat.Field) {
            NumberFormat.Field field = (NumberFormat.Field) rawField;
            boolean seenStart = false;
            int fractionStart = -1;
            int i = this.zero;
            while (true) {
                if (i > this.zero + this.length) {
                    break;
                }
                NumberFormat.Field _field = i < this.zero + this.length ? this.fields[i] : null;
                if (!seenStart || field == _field) {
                    if (!seenStart && field == _field) {
                        fp.setBeginIndex((i - this.zero) + offset);
                        seenStart = true;
                    }
                    if (_field == NumberFormat.Field.INTEGER || _field == NumberFormat.Field.DECIMAL_SEPARATOR) {
                        fractionStart = (i - this.zero) + 1;
                    }
                } else if (field != NumberFormat.Field.INTEGER || _field != NumberFormat.Field.GROUPING_SEPARATOR) {
                    fp.setEndIndex((i - this.zero) + offset);
                }
                i++;
            }
            fp.setEndIndex((i - this.zero) + offset);
            if (field == NumberFormat.Field.FRACTION && !seenStart) {
                fp.setBeginIndex(fractionStart + offset);
                fp.setEndIndex(fractionStart + offset);
            }
            return;
        }
        throw new IllegalArgumentException("You must pass an instance of android.icu.text.NumberFormat.Field as your FieldPosition attribute.  You passed: " + rawField.getClass().toString());
    }

    public AttributedCharacterIterator getIterator() {
        AttributedString as = new AttributedString(toString());
        NumberFormat.Field current = null;
        int currentStart = -1;
        for (int i = 0; i < this.length; i++) {
            NumberFormat.Field field = this.fields[this.zero + i];
            if (current == NumberFormat.Field.INTEGER && field == NumberFormat.Field.GROUPING_SEPARATOR) {
                as.addAttribute(NumberFormat.Field.GROUPING_SEPARATOR, NumberFormat.Field.GROUPING_SEPARATOR, i, i + 1);
            } else if (current != field) {
                if (current != null) {
                    as.addAttribute(current, current, currentStart, i);
                }
                current = field;
                currentStart = i;
            }
        }
        if (current != null) {
            as.addAttribute(current, current, currentStart, this.length);
        }
        return as.getIterator();
    }
}
