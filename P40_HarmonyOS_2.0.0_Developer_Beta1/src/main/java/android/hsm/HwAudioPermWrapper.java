package android.hsm;

public class HwAudioPermWrapper {
    private static final String TAG = HwAudioPermWrapper.class.getSimpleName();
    private boolean mIsAudioBlocked = false;

    public void confirmPermission() {
        this.mIsAudioBlocked = !HwSystemManager.allowOp(128);
    }

    public boolean confirmPermissionWithResult() {
        this.mIsAudioBlocked = !HwSystemManager.allowOp(128);
        return this.mIsAudioBlocked;
    }

    public boolean isBlocked() {
        return this.mIsAudioBlocked;
    }

    public int mockRead(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if (audioData == null) {
            return 0;
        }
        for (int count = offsetInBytes; count < offsetInBytes + sizeInBytes; count++) {
            audioData[count] = 0;
        }
        return 0;
    }

    public int mockRead(short[] audioData, int offsetInShorts, int sizeInShorts) {
        if (audioData == null) {
            return 0;
        }
        for (int count = offsetInShorts; count < offsetInShorts + sizeInShorts; count++) {
            audioData[count] = 0;
        }
        return 0;
    }
}
