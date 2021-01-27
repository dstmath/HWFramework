package android.filterpacks.text;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.format.ObjectFormat;
import android.util.Log;

public class StringLogger extends Filter {
    public StringLogger(String name) {
        super(name);
    }

    @Override // android.filterfw.core.Filter
    public void setupPorts() {
        addMaskedInputPort("string", ObjectFormat.fromClass(Object.class, 1));
    }

    @Override // android.filterfw.core.Filter
    public void process(FilterContext env) {
        Log.i("StringLogger", pullInput("string").getObjectValue().toString());
    }
}
