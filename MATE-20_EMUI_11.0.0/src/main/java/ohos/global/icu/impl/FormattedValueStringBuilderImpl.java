package ohos.global.icu.impl;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.FieldPosition;
import java.text.Format;
import ohos.global.icu.impl.StaticUnicodeSets;
import ohos.global.icu.text.ConstrainedFieldPosition;
import ohos.global.icu.text.NumberFormat;
import ohos.global.icu.text.UnicodeSet;

public class FormattedValueStringBuilderImpl {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    public static boolean nextFieldPosition(FormattedStringBuilder formattedStringBuilder, FieldPosition fieldPosition) {
        Format.Field fieldAttribute = fieldPosition.getFieldAttribute();
        if (fieldAttribute == null) {
            if (fieldPosition.getField() == 0) {
                fieldAttribute = NumberFormat.Field.INTEGER;
            } else if (fieldPosition.getField() != 1) {
                return false;
            } else {
                fieldAttribute = NumberFormat.Field.FRACTION;
            }
        }
        if (fieldAttribute instanceof NumberFormat.Field) {
            ConstrainedFieldPosition constrainedFieldPosition = new ConstrainedFieldPosition();
            constrainedFieldPosition.constrainField(fieldAttribute);
            constrainedFieldPosition.setState(fieldAttribute, null, fieldPosition.getBeginIndex(), fieldPosition.getEndIndex());
            if (nextPosition(formattedStringBuilder, constrainedFieldPosition, null)) {
                fieldPosition.setBeginIndex(constrainedFieldPosition.getStart());
                fieldPosition.setEndIndex(constrainedFieldPosition.getLimit());
                return true;
            }
            if (fieldAttribute == NumberFormat.Field.FRACTION && fieldPosition.getEndIndex() == 0) {
                int i = formattedStringBuilder.zero;
                boolean z = false;
                while (i < formattedStringBuilder.zero + formattedStringBuilder.length) {
                    if (isIntOrGroup(formattedStringBuilder.fields[i]) || formattedStringBuilder.fields[i] == NumberFormat.Field.DECIMAL_SEPARATOR) {
                        z = true;
                    } else if (z) {
                        break;
                    }
                    i++;
                }
                fieldPosition.setBeginIndex(i - formattedStringBuilder.zero);
                fieldPosition.setEndIndex(i - formattedStringBuilder.zero);
            }
            return false;
        }
        throw new IllegalArgumentException("You must pass an instance of ohos.global.icu.text.NumberFormat.Field as your FieldPosition attribute.  You passed: " + fieldAttribute.getClass().toString());
    }

    public static AttributedCharacterIterator toCharacterIterator(FormattedStringBuilder formattedStringBuilder, Format.Field field) {
        ConstrainedFieldPosition constrainedFieldPosition = new ConstrainedFieldPosition();
        AttributedString attributedString = new AttributedString(formattedStringBuilder.toString());
        while (nextPosition(formattedStringBuilder, constrainedFieldPosition, field)) {
            attributedString.addAttribute(constrainedFieldPosition.getField(), constrainedFieldPosition.getField(), constrainedFieldPosition.getStart(), constrainedFieldPosition.getLimit());
        }
        return attributedString.getIterator();
    }

    /* access modifiers changed from: package-private */
    public static class NullField extends Format.Field {
        static final NullField END = new NullField("end");
        private static final long serialVersionUID = 1;

        private NullField(String str) {
            super(str);
        }
    }

    public static boolean nextPosition(FormattedStringBuilder formattedStringBuilder, ConstrainedFieldPosition constrainedFieldPosition, Format.Field field) {
        int limit = formattedStringBuilder.zero + constrainedFieldPosition.getLimit();
        int i = -1;
        NumberFormat.Field field2 = null;
        while (limit <= formattedStringBuilder.zero + formattedStringBuilder.length) {
            NullField nullField = limit < formattedStringBuilder.zero + formattedStringBuilder.length ? formattedStringBuilder.fields[limit] : NullField.END;
            if (field2 == null) {
                if (constrainedFieldPosition.matchesField(NumberFormat.Field.INTEGER, null) && limit > formattedStringBuilder.zero && limit - formattedStringBuilder.zero > constrainedFieldPosition.getLimit()) {
                    int i2 = limit - 1;
                    if (isIntOrGroup(formattedStringBuilder.fields[i2]) && !isIntOrGroup(nullField)) {
                        while (i2 >= formattedStringBuilder.zero && isIntOrGroup(formattedStringBuilder.fields[i2])) {
                            i2--;
                        }
                        constrainedFieldPosition.setState(NumberFormat.Field.INTEGER, null, (i2 - formattedStringBuilder.zero) + 1, limit - formattedStringBuilder.zero);
                        return true;
                    }
                }
                if (field != null && constrainedFieldPosition.matchesField(field, null) && limit > formattedStringBuilder.zero && (limit - formattedStringBuilder.zero > constrainedFieldPosition.getLimit() || constrainedFieldPosition.getField() != field)) {
                    int i3 = limit - 1;
                    if (isNumericField(formattedStringBuilder.fields[i3]) && !isNumericField(nullField)) {
                        while (i3 >= formattedStringBuilder.zero && isNumericField(formattedStringBuilder.fields[i3])) {
                            i3--;
                        }
                        constrainedFieldPosition.setState(field, null, (i3 - formattedStringBuilder.zero) + 1, limit - formattedStringBuilder.zero);
                        return true;
                    }
                }
                if (nullField == NumberFormat.Field.INTEGER) {
                    nullField = null;
                }
                if (!(nullField == null || nullField == NullField.END || !constrainedFieldPosition.matchesField(nullField, null))) {
                    i = limit - formattedStringBuilder.zero;
                    field2 = nullField;
                }
            } else if (field2 != nullField) {
                int i4 = limit - formattedStringBuilder.zero;
                if (field2 != NumberFormat.Field.GROUPING_SEPARATOR) {
                    i4 = trimBack(formattedStringBuilder, i4);
                }
                if (i4 <= i) {
                    limit--;
                    i = -1;
                    field2 = null;
                } else {
                    if (field2 != NumberFormat.Field.GROUPING_SEPARATOR) {
                        i = trimFront(formattedStringBuilder, i);
                    }
                    constrainedFieldPosition.setState(field2, null, i, i4);
                    return true;
                }
            } else {
                continue;
            }
            limit++;
        }
        return false;
    }

    private static boolean isIntOrGroup(Format.Field field) {
        return field == NumberFormat.Field.INTEGER || field == NumberFormat.Field.GROUPING_SEPARATOR;
    }

    private static boolean isNumericField(Format.Field field) {
        return field == null || NumberFormat.Field.class.isAssignableFrom(field.getClass());
    }

    private static int trimBack(FormattedStringBuilder formattedStringBuilder, int i) {
        return StaticUnicodeSets.get(StaticUnicodeSets.Key.DEFAULT_IGNORABLES).spanBack(formattedStringBuilder, i, UnicodeSet.SpanCondition.CONTAINED);
    }

    private static int trimFront(FormattedStringBuilder formattedStringBuilder, int i) {
        return StaticUnicodeSets.get(StaticUnicodeSets.Key.DEFAULT_IGNORABLES).span(formattedStringBuilder, i, UnicodeSet.SpanCondition.CONTAINED);
    }
}
