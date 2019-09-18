package android.media.effect;

public abstract class Effect {
    public abstract void apply(int i, int i2, int i3, int i4);

    public abstract String getName();

    public abstract void release();

    public abstract void setParameter(String str, Object obj);

    public void setUpdateListener(EffectUpdateListener listener) {
    }
}
