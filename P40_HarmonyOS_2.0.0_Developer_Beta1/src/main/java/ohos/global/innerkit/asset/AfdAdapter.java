package ohos.global.innerkit.asset;

import android.content.res.AssetFileDescriptor;
import java.io.FileDescriptor;
import java.io.IOException;

public class AfdAdapter {
    private AssetFileDescriptor assetFileDesc;

    public AfdAdapter(AssetFileDescriptor assetFileDescriptor) {
        this.assetFileDesc = assetFileDescriptor;
    }

    public FileDescriptor getFileDescriptor() {
        return this.assetFileDesc.getFileDescriptor();
    }

    public long getStartOffset() {
        return this.assetFileDesc.getStartOffset();
    }

    public long getDeclaredLength() {
        return this.assetFileDesc.getDeclaredLength();
    }

    public void close() throws IOException {
        this.assetFileDesc.close();
    }
}
