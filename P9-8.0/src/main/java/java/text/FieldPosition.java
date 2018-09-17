package java.text;

import java.text.Format.Field;

public class FieldPosition {
    private Field attribute;
    int beginIndex;
    int endIndex;
    int field;

    private class Delegate implements FieldDelegate {
        private boolean encounteredField;

        /* synthetic */ Delegate(FieldPosition this$0, Delegate -this1) {
            this();
        }

        private Delegate() {
        }

        public void formatted(Field attr, Object value, int start, int end, StringBuffer buffer) {
            if (!this.encounteredField && FieldPosition.this.matchesField(attr)) {
                FieldPosition.this.setBeginIndex(start);
                FieldPosition.this.setEndIndex(end);
                this.encounteredField = start != end;
            }
        }

        public void formatted(int fieldID, Field attr, Object value, int start, int end, StringBuffer buffer) {
            if (!this.encounteredField && FieldPosition.this.matchesField(attr, fieldID)) {
                FieldPosition.this.setBeginIndex(start);
                FieldPosition.this.setEndIndex(end);
                this.encounteredField = start != end;
            }
        }
    }

    public FieldPosition(int field) {
        this.field = 0;
        this.endIndex = 0;
        this.beginIndex = 0;
        this.field = field;
    }

    public FieldPosition(Field attribute) {
        this(attribute, -1);
    }

    public FieldPosition(Field attribute, int fieldID) {
        this.field = 0;
        this.endIndex = 0;
        this.beginIndex = 0;
        this.attribute = attribute;
        this.field = fieldID;
    }

    public Field getFieldAttribute() {
        return this.attribute;
    }

    public int getField() {
        return this.field;
    }

    public int getBeginIndex() {
        return this.beginIndex;
    }

    public int getEndIndex() {
        return this.endIndex;
    }

    public void setBeginIndex(int bi) {
        this.beginIndex = bi;
    }

    public void setEndIndex(int ei) {
        this.endIndex = ei;
    }

    FieldDelegate getFieldDelegate() {
        return new Delegate(this, null);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !(obj instanceof FieldPosition)) {
            return false;
        }
        FieldPosition other = (FieldPosition) obj;
        if (this.attribute == null) {
            if (other.attribute != null) {
                return false;
            }
        } else if (!this.attribute.equals(other.attribute)) {
            return false;
        }
        if (this.beginIndex == other.beginIndex && this.endIndex == other.endIndex && this.field == other.field) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return ((this.field << 24) | (this.beginIndex << 16)) | this.endIndex;
    }

    public String toString() {
        return getClass().getName() + "[field=" + this.field + ",attribute=" + this.attribute + ",beginIndex=" + this.beginIndex + ",endIndex=" + this.endIndex + ']';
    }

    private boolean matchesField(Field attribute) {
        if (this.attribute != null) {
            return this.attribute.equals(attribute);
        }
        return false;
    }

    private boolean matchesField(Field attribute, int field) {
        if (this.attribute != null) {
            return this.attribute.equals(attribute);
        }
        return field == this.field;
    }
}
