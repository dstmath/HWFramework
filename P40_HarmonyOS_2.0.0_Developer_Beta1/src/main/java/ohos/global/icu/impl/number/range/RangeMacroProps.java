package ohos.global.icu.impl.number.range;

import java.util.Objects;
import ohos.global.icu.number.NumberRangeFormatter;
import ohos.global.icu.number.UnlocalizedNumberFormatter;
import ohos.global.icu.util.ULocale;

public class RangeMacroProps {
    public NumberRangeFormatter.RangeCollapse collapse;
    public UnlocalizedNumberFormatter formatter1;
    public UnlocalizedNumberFormatter formatter2;
    public NumberRangeFormatter.RangeIdentityFallback identityFallback;
    public ULocale loc;
    public int sameFormatters = -1;

    public int hashCode() {
        return Objects.hash(this.formatter1, this.formatter2, this.collapse, this.identityFallback, this.loc);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RangeMacroProps)) {
            return false;
        }
        RangeMacroProps rangeMacroProps = (RangeMacroProps) obj;
        return Objects.equals(this.formatter1, rangeMacroProps.formatter1) && Objects.equals(this.formatter2, rangeMacroProps.formatter2) && Objects.equals(this.collapse, rangeMacroProps.collapse) && Objects.equals(this.identityFallback, rangeMacroProps.identityFallback) && Objects.equals(this.loc, rangeMacroProps.loc);
    }
}
