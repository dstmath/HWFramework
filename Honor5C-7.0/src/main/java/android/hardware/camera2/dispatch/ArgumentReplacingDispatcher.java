package android.hardware.camera2.dispatch;

import com.android.internal.util.Preconditions;
import java.lang.reflect.Method;

public class ArgumentReplacingDispatcher<T, TArg> implements Dispatchable<T> {
    private final int mArgumentIndex;
    private final TArg mReplaceWith;
    private final Dispatchable<T> mTarget;

    public ArgumentReplacingDispatcher(Dispatchable<T> target, int argumentIndex, TArg replaceWith) {
        this.mTarget = (Dispatchable) Preconditions.checkNotNull(target, "target must not be null");
        this.mArgumentIndex = Preconditions.checkArgumentNonnegative(argumentIndex, "argumentIndex must not be negative");
        this.mReplaceWith = Preconditions.checkNotNull(replaceWith, "replaceWith must not be null");
    }

    public Object dispatch(Method method, Object[] args) throws Throwable {
        if (args.length > this.mArgumentIndex) {
            args = arrayCopy(args);
            args[this.mArgumentIndex] = this.mReplaceWith;
        }
        return this.mTarget.dispatch(method, args);
    }

    private static Object[] arrayCopy(Object[] array) {
        int length = array.length;
        Object[] newArray = new Object[length];
        for (int i = 0; i < length; i++) {
            newArray[i] = array[i];
        }
        return newArray;
    }
}
