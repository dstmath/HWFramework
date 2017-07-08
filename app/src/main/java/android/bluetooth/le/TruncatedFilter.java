package android.bluetooth.le;

import java.util.List;

public final class TruncatedFilter {
    private final ScanFilter mFilter;
    private final List<ResultStorageDescriptor> mStorageDescriptors;

    public TruncatedFilter(ScanFilter filter, List<ResultStorageDescriptor> storageDescriptors) {
        this.mFilter = filter;
        this.mStorageDescriptors = storageDescriptors;
    }

    public ScanFilter getFilter() {
        return this.mFilter;
    }

    public List<ResultStorageDescriptor> getStorageDescriptors() {
        return this.mStorageDescriptors;
    }
}
