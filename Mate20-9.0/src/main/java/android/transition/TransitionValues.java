package android.transition;

import android.util.ArrayMap;
import android.view.View;
import java.util.ArrayList;
import java.util.Map;

public class TransitionValues {
    final ArrayList<Transition> targetedTransitions = new ArrayList<>();
    public final Map<String, Object> values = new ArrayMap();
    public View view;

    public boolean equals(Object other) {
        if (!(other instanceof TransitionValues) || this.view != ((TransitionValues) other).view || !this.values.equals(((TransitionValues) other).values)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (31 * this.view.hashCode()) + this.values.hashCode();
    }

    public String toString() {
        String returnValue = "TransitionValues@" + Integer.toHexString(hashCode()) + ":\n";
        String returnValue2 = returnValue + "    view = " + this.view + "\n";
        String returnValue3 = returnValue2 + "    values:";
        for (String s : this.values.keySet()) {
            returnValue3 = returnValue3 + "    " + s + ": " + this.values.get(s) + "\n";
        }
        return returnValue3;
    }
}
