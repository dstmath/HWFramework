package ohos.aafwk.ability;

import java.io.FileDescriptor;
import ohos.utils.PacMap;
import ohos.utils.net.Uri;

public interface PipeFileWriter<T> {
    void write(FileDescriptor fileDescriptor, Uri uri, String str, PacMap pacMap, T t);
}
