package android.content.res;

import android.content.res.Resources;

public abstract class ConstantState<T> {
    public abstract int getChangingConfigurations();

    public abstract T newInstance();

    public T newInstance(Resources res) {
        return newInstance();
    }

    public T newInstance(Resources res, Resources.Theme theme) {
        return newInstance(res);
    }
}
