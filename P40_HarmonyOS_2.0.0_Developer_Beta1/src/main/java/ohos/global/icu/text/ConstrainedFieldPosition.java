package ohos.global.icu.text;

import java.text.Format;
import java.util.Objects;

public class ConstrainedFieldPosition {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private Class<?> fClassConstraint;
    private ConstraintType fConstraint;
    private long fContext;
    private Format.Field fField;
    private int fLimit;
    private int fStart;
    private Object fValue;

    /* access modifiers changed from: private */
    public enum ConstraintType {
        NONE,
        CLASS,
        FIELD,
        VALUE
    }

    public ConstrainedFieldPosition() {
        reset();
    }

    public void reset() {
        this.fConstraint = ConstraintType.NONE;
        this.fClassConstraint = Object.class;
        this.fField = null;
        this.fValue = null;
        this.fStart = 0;
        this.fLimit = 0;
        this.fContext = 0;
    }

    public void constrainField(Format.Field field) {
        if (field != null) {
            this.fConstraint = ConstraintType.FIELD;
            this.fClassConstraint = Object.class;
            this.fField = field;
            this.fValue = null;
            return;
        }
        throw new IllegalArgumentException("Cannot constrain on null field");
    }

    public void constrainClass(Class<?> cls) {
        if (cls != null) {
            this.fConstraint = ConstraintType.CLASS;
            this.fClassConstraint = cls;
            this.fField = null;
            this.fValue = null;
            return;
        }
        throw new IllegalArgumentException("Cannot constrain on null field class");
    }

    @Deprecated
    public void constrainFieldAndValue(Format.Field field, Object obj) {
        this.fConstraint = ConstraintType.VALUE;
        this.fClassConstraint = Object.class;
        this.fField = field;
        this.fValue = obj;
    }

    public Format.Field getField() {
        return this.fField;
    }

    public int getStart() {
        return this.fStart;
    }

    public int getLimit() {
        return this.fLimit;
    }

    public Object getFieldValue() {
        return this.fValue;
    }

    public long getInt64IterationContext() {
        return this.fContext;
    }

    public void setInt64IterationContext(long j) {
        this.fContext = j;
    }

    public void setState(Format.Field field, Object obj, int i, int i2) {
        this.fField = field;
        this.fValue = obj;
        this.fStart = i;
        this.fLimit = i2;
    }

    /* renamed from: ohos.global.icu.text.ConstrainedFieldPosition$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$text$ConstrainedFieldPosition$ConstraintType = new int[ConstraintType.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$text$ConstrainedFieldPosition$ConstraintType[ConstraintType.NONE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$ConstrainedFieldPosition$ConstraintType[ConstraintType.CLASS.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$ConstrainedFieldPosition$ConstraintType[ConstraintType.FIELD.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$ConstrainedFieldPosition$ConstraintType[ConstraintType.VALUE.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public boolean matchesField(Format.Field field, Object obj) {
        if (field != null) {
            int i = AnonymousClass1.$SwitchMap$ohos$global$icu$text$ConstrainedFieldPosition$ConstraintType[this.fConstraint.ordinal()];
            if (i == 1) {
                return true;
            }
            if (i == 2) {
                return this.fClassConstraint.isAssignableFrom(field.getClass());
            }
            if (i == 3) {
                return this.fField == field;
            }
            if (i == 4) {
                return this.fField == field && Objects.equals(this.fValue, obj);
            }
            throw new AssertionError();
        }
        throw new IllegalArgumentException("field must not be null");
    }

    public String toString() {
        return "CFPos[" + this.fStart + '-' + this.fLimit + ' ' + this.fField + ']';
    }
}
