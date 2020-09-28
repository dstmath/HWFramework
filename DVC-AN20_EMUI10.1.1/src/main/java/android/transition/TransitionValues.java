package android.transition;

import android.util.ArrayMap;
import android.view.View;
import java.util.ArrayList;
import java.util.Map;

public class TransitionValues {
    final ArrayList<Transition> targetedTransitions = new ArrayList<>();
    public final Map<String, Object> values = new ArrayMap();
    public View view;

    @Deprecated
    public TransitionValues() {
    }

    public TransitionValues(View view2) {
        this.view = view2;
    }

    public boolean equals(Object other) {
        if (!(other instanceof TransitionValues) || this.view != ((TransitionValues) other).view || !this.values.equals(((TransitionValues) other).values)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (this.view.hashCode() * 31) + this.values.hashCode();
    }

    public String toString() {
        String returnValue = (("TransitionValues@" + Integer.toHexString(hashCode()) + ":\n") + "    view = " + this.view + "\n") + "    values:";
        for (String s : this.values.keySet()) {
            returnValue = returnValue + "    " + s + ": " + this.values.get(s) + "\n";
        }
        return returnValue;
    }
}
