package android.filterpacks.base;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterContext;
import android.filterfw.core.GenerateFieldPort;

public class FrameStore extends Filter {
    @GenerateFieldPort(name = "key")
    private String mKey;

    public FrameStore(String name) {
        super(name);
    }

    public void setupPorts() {
        addInputPort("frame");
    }

    public void process(FilterContext context) {
        context.storeFrame(this.mKey, pullInput("frame"));
    }
}
