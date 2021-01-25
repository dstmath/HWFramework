package ohos.global.resource;

import java.io.IOException;
import java.io.InputStream;
import ohos.global.innerkit.asset.Package;
import ohos.global.resource.Entry;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class RawFileEntryImpl extends RawFileEntry {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "RawFileEntryImpl");
    private String path;
    private Package resPackage;
    private Entry.Type type;

    public RawFileEntryImpl(Package r1, String str) {
        this.resPackage = r1;
        this.path = str;
        this.type = r1.getEntryType2(str);
    }

    @Override // ohos.global.resource.RawFileEntry
    public Resource openRawFile() throws IOException {
        InputStream open = this.resPackage.open(this.path);
        if (open == null) {
            return null;
        }
        return new ResourceImpl(open);
    }

    @Override // ohos.global.resource.RawFileEntry
    public RawFileDescriptor openRawFileDescriptor() throws IOException {
        RawFileDescriptor openRawFileDescriptor = this.resPackage.openRawFileDescriptor(this.path);
        if (openRawFileDescriptor == null) {
            return null;
        }
        return openRawFileDescriptor;
    }

    @Override // ohos.global.resource.RawFileEntry
    public Entry[] getEntries() throws IOException {
        String[] list = this.resPackage.list(this.path);
        int length = list.length;
        HiLog.debug(LABEL, "list length:%{public}d", Integer.valueOf(length));
        EntryImpl[] entryImplArr = new EntryImpl[length];
        for (int i = 0; i < length; i++) {
            HiLog.debug(LABEL, "file list:%{public}s", list[i]);
            entryImplArr[i] = new EntryImpl();
            entryImplArr[i].setPath(list[i]);
            entryImplArr[i].setType(this.resPackage.getEntryType2(this.path + "/" + list[i]));
        }
        return entryImplArr;
    }

    @Override // ohos.global.resource.RawFileEntry
    public Entry.Type getType() {
        return this.type;
    }
}
