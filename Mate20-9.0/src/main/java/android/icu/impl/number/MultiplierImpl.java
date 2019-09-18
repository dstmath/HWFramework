package android.icu.impl.number;

import java.math.BigDecimal;

public class MultiplierImpl implements MicroPropsGenerator {
    final BigDecimal bigDecimalMultiplier;
    final int magnitudeMultiplier;
    final MicroPropsGenerator parent;

    public MultiplierImpl(int magnitudeMultiplier2) {
        this.magnitudeMultiplier = magnitudeMultiplier2;
        this.bigDecimalMultiplier = null;
        this.parent = null;
    }

    public MultiplierImpl(BigDecimal bigDecimalMultiplier2) {
        this.magnitudeMultiplier = 0;
        this.bigDecimalMultiplier = bigDecimalMultiplier2;
        this.parent = null;
    }

    private MultiplierImpl(MultiplierImpl base, MicroPropsGenerator parent2) {
        this.magnitudeMultiplier = base.magnitudeMultiplier;
        this.bigDecimalMultiplier = base.bigDecimalMultiplier;
        this.parent = parent2;
    }

    public MicroPropsGenerator copyAndChain(MicroPropsGenerator parent2) {
        return new MultiplierImpl(this, parent2);
    }

    public MicroProps processQuantity(DecimalQuantity quantity) {
        MicroProps micros = this.parent.processQuantity(quantity);
        quantity.adjustMagnitude(this.magnitudeMultiplier);
        if (this.bigDecimalMultiplier != null) {
            quantity.multiplyBy(this.bigDecimalMultiplier);
        }
        return micros;
    }
}
