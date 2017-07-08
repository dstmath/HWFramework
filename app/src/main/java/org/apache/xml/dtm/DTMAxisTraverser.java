package org.apache.xml.dtm;

public abstract class DTMAxisTraverser {
    public abstract int next(int i, int i2);

    public abstract int next(int i, int i2, int i3);

    public int first(int context) {
        return next(context, context);
    }

    public int first(int context, int extendedTypeID) {
        return next(context, context, extendedTypeID);
    }
}
