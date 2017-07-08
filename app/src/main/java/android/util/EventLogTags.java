package android.util;

import java.io.BufferedReader;
import java.io.IOException;

@Deprecated
public class EventLogTags {

    public static class Description {
        public final String mName;
        public final int mTag;

        Description(int tag, String name) {
            this.mTag = tag;
            this.mName = name;
        }
    }

    public EventLogTags(BufferedReader input) throws IOException {
    }

    public Description get(String name) {
        return null;
    }

    public Description get(int tag) {
        return null;
    }
}
