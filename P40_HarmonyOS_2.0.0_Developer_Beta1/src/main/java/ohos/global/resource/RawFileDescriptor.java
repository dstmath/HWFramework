package ohos.global.resource;

import java.io.FileDescriptor;

public abstract class RawFileDescriptor extends BaseFileDescriptor {
    @Override // ohos.global.resource.BaseFileDescriptor
    public abstract FileDescriptor getFileDescriptor();

    @Override // ohos.global.resource.BaseFileDescriptor
    public abstract long getFileSize();

    @Override // ohos.global.resource.BaseFileDescriptor
    public abstract long getStartPosition();
}
