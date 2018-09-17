package android.filterpacks.base;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;

public class NullFilter extends Filter {
    public NullFilter(String name) {
        super(name);
    }

    public void setupPorts() {
        addInputPort("frame");
    }

    public void process(FilterContext context) {
        pullInput("frame");
    }
}
