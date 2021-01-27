package android.filterpacks.base;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;

public class NullFilter extends Filter {
    public NullFilter(String name) {
        super(name);
    }

    @Override // android.filterfw.core.Filter
    public void setupPorts() {
        addInputPort("frame");
    }

    @Override // android.filterfw.core.Filter
    public void process(FilterContext context) {
        pullInput("frame");
    }
}
