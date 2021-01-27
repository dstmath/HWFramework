package ohos.global.resource;

import java.io.IOException;
import ohos.global.resource.Entry;

public abstract class RawFileEntry {
    public abstract Entry[] getEntries() throws IOException;

    public abstract Entry.Type getType();

    public abstract Resource openRawFile() throws IOException;

    public abstract RawFileDescriptor openRawFileDescriptor() throws IOException;
}
