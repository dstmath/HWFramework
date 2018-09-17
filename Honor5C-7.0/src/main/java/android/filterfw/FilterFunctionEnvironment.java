package android.filterfw;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterFactory;
import android.filterfw.core.FilterFunction;
import android.filterfw.core.FrameManager;

public class FilterFunctionEnvironment extends MffEnvironment {
    public FilterFunctionEnvironment() {
        super(null);
    }

    public FilterFunctionEnvironment(FrameManager frameManager) {
        super(frameManager);
    }

    public FilterFunction createFunction(Class filterClass, Object... parameters) {
        Filter filter = FilterFactory.sharedFactory().createFilterByClass(filterClass, "FilterFunction(" + filterClass.getSimpleName() + ")");
        filter.initWithAssignmentList(parameters);
        return new FilterFunction(getContext(), filter);
    }
}
