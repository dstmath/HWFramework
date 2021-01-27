package ohos.global.icu.impl;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.FieldPosition;
import java.text.Format;
import java.util.List;
import ohos.global.icu.text.ConstrainedFieldPosition;

public class FormattedValueFieldPositionIteratorImpl {
    private FormattedValueFieldPositionIteratorImpl() {
    }

    private static class FieldWithValue extends Format.Field {
        private static final long serialVersionUID = -3850076447157793465L;
        public final Format.Field field;
        public final int value;

        public FieldWithValue(Format.Field field2, int i) {
            super(field2.toString());
            this.field = field2;
            this.value = i;
        }
    }

    public static boolean nextPosition(List<FieldPosition> list, ConstrainedFieldPosition constrainedFieldPosition) {
        int size = list.size();
        int int64IterationContext = (int) constrainedFieldPosition.getInt64IterationContext();
        while (true) {
            if (int64IterationContext >= size) {
                break;
            }
            FieldPosition fieldPosition = list.get(int64IterationContext);
            Format.Field fieldAttribute = fieldPosition.getFieldAttribute();
            Integer num = null;
            if (fieldAttribute instanceof FieldWithValue) {
                FieldWithValue fieldWithValue = (FieldWithValue) fieldAttribute;
                num = Integer.valueOf(fieldWithValue.value);
                fieldAttribute = fieldWithValue.field;
            }
            if (constrainedFieldPosition.matchesField(fieldAttribute, num)) {
                constrainedFieldPosition.setState(fieldAttribute, num, fieldPosition.getBeginIndex(), fieldPosition.getEndIndex());
                break;
            }
            int64IterationContext++;
        }
        constrainedFieldPosition.setInt64IterationContext(int64IterationContext == size ? (long) int64IterationContext : (long) (int64IterationContext + 1));
        return int64IterationContext < size;
    }

    public static AttributedCharacterIterator toCharacterIterator(CharSequence charSequence, List<FieldPosition> list) {
        Object obj;
        AttributedString attributedString = new AttributedString(charSequence.toString());
        for (int i = 0; i < list.size(); i++) {
            FieldPosition fieldPosition = list.get(i);
            Format.Field fieldAttribute = fieldPosition.getFieldAttribute();
            if (fieldAttribute instanceof FieldWithValue) {
                FieldWithValue fieldWithValue = (FieldWithValue) fieldAttribute;
                obj = Integer.valueOf(fieldWithValue.value);
                fieldAttribute = fieldWithValue.field;
            } else {
                obj = fieldAttribute;
            }
            attributedString.addAttribute(fieldAttribute, obj, fieldPosition.getBeginIndex(), fieldPosition.getEndIndex());
        }
        return attributedString.getIterator();
    }

    public static void addOverlapSpans(List<FieldPosition> list, Format.Field field, int i) {
        int size = list.size();
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = Integer.MAX_VALUE;
        int i6 = Integer.MAX_VALUE;
        while (i2 < size) {
            FieldPosition fieldPosition = list.get(i2);
            i2++;
            int i7 = i2;
            while (true) {
                if (i7 >= size) {
                    break;
                }
                FieldPosition fieldPosition2 = list.get(i7);
                if (fieldPosition.getFieldAttribute() == fieldPosition2.getFieldAttribute()) {
                    i5 = Math.min(i5, fieldPosition.getBeginIndex());
                    i3 = Math.max(i3, fieldPosition.getEndIndex());
                    i6 = Math.min(i6, fieldPosition2.getBeginIndex());
                    i4 = Math.max(i4, fieldPosition2.getEndIndex());
                    break;
                }
                i7++;
            }
        }
        if (i5 != Integer.MAX_VALUE) {
            FieldPosition fieldPosition3 = new FieldPosition(new FieldWithValue(field, i));
            fieldPosition3.setBeginIndex(i5);
            fieldPosition3.setEndIndex(i3);
            list.add(fieldPosition3);
            FieldPosition fieldPosition4 = new FieldPosition(new FieldWithValue(field, 1 - i));
            fieldPosition4.setBeginIndex(i6);
            fieldPosition4.setEndIndex(i4);
            list.add(fieldPosition4);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x007d A[SYNTHETIC] */
    public static void sort(List<FieldPosition> list) {
        boolean z;
        long j;
        int hashCode;
        int hashCode2;
        int size = list.size();
        do {
            int i = 0;
            z = true;
            while (i < size - 1) {
                FieldPosition fieldPosition = list.get(i);
                int i2 = i + 1;
                FieldPosition fieldPosition2 = list.get(i2);
                if (fieldPosition.getBeginIndex() != fieldPosition2.getBeginIndex()) {
                    hashCode = fieldPosition2.getBeginIndex();
                    hashCode2 = fieldPosition.getBeginIndex();
                } else if (fieldPosition.getEndIndex() != fieldPosition2.getEndIndex()) {
                    hashCode = fieldPosition.getEndIndex();
                    hashCode2 = fieldPosition2.getEndIndex();
                } else {
                    if (fieldPosition.getFieldAttribute() != fieldPosition2.getFieldAttribute()) {
                        boolean z2 = fieldPosition.getFieldAttribute() instanceof FieldWithValue;
                        boolean z3 = fieldPosition2.getFieldAttribute() instanceof FieldWithValue;
                        if (z2 && !z3) {
                            j = 1;
                        } else if (!z3 || z2) {
                            hashCode = fieldPosition.hashCode();
                            hashCode2 = fieldPosition2.hashCode();
                        } else {
                            j = -1;
                        }
                    } else {
                        j = 0;
                    }
                    if (j >= 0) {
                        list.set(i, fieldPosition2);
                        list.set(i2, fieldPosition);
                        z = false;
                    }
                    i = i2;
                }
                j = (long) (hashCode - hashCode2);
                if (j >= 0) {
                }
                i = i2;
            }
        } while (!z);
    }
}
