package android.icu.text;

import java.text.FieldPosition;
import java.text.Format;

@Deprecated
public class UFieldPosition extends FieldPosition {
    private int countVisibleFractionDigits = -1;
    private long fractionDigits = 0;

    @Deprecated
    public UFieldPosition() {
        super(-1);
    }

    @Deprecated
    public UFieldPosition(int field) {
        super(field);
    }

    @Deprecated
    public UFieldPosition(Format.Field attribute, int fieldID) {
        super(attribute, fieldID);
    }

    @Deprecated
    public UFieldPosition(Format.Field attribute) {
        super(attribute);
    }

    @Deprecated
    public void setFractionDigits(int countVisibleFractionDigits2, long fractionDigits2) {
        this.countVisibleFractionDigits = countVisibleFractionDigits2;
        this.fractionDigits = fractionDigits2;
    }

    @Deprecated
    public int getCountVisibleFractionDigits() {
        return this.countVisibleFractionDigits;
    }

    @Deprecated
    public long getFractionDigits() {
        return this.fractionDigits;
    }
}
