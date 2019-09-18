package android.icu.util;

public class NoUnit extends MeasureUnit {
    public static final NoUnit BASE = ((NoUnit) MeasureUnit.internalGetInstance("none", "base"));
    public static final NoUnit PERCENT = ((NoUnit) MeasureUnit.internalGetInstance("none", "percent"));
    public static final NoUnit PERMILLE = ((NoUnit) MeasureUnit.internalGetInstance("none", "permille"));
    private static final long serialVersionUID = 2467174286237024095L;

    NoUnit(String subType) {
        super("none", subType);
    }
}
