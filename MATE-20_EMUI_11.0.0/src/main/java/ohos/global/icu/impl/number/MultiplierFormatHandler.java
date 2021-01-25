package ohos.global.icu.impl.number;

import ohos.global.icu.number.Scale;

public class MultiplierFormatHandler implements MicroPropsGenerator {
    final Scale multiplier;
    final MicroPropsGenerator parent;

    public MultiplierFormatHandler(Scale scale, MicroPropsGenerator microPropsGenerator) {
        this.multiplier = scale;
        this.parent = microPropsGenerator;
    }

    @Override // ohos.global.icu.impl.number.MicroPropsGenerator
    public MicroProps processQuantity(DecimalQuantity decimalQuantity) {
        MicroProps processQuantity = this.parent.processQuantity(decimalQuantity);
        this.multiplier.applyTo(decimalQuantity);
        return processQuantity;
    }
}
