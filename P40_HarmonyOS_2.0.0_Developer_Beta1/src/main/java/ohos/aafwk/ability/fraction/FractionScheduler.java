package ohos.aafwk.ability.fraction;

public abstract class FractionScheduler {
    public abstract FractionScheduler add(int i, Fraction fraction);

    public abstract FractionScheduler add(int i, Fraction fraction, String str);

    public abstract FractionScheduler hide(Fraction fraction);

    public abstract FractionScheduler pushIntoStack(String str);

    public abstract FractionScheduler remove(Fraction fraction);

    public abstract FractionScheduler replace(int i, Fraction fraction);

    public abstract FractionScheduler show(Fraction fraction);

    public abstract int submit();
}
