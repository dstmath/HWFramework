package android.hsm;

public class HwAudioPermWrapper {
    private static final String TAG = HwAudioPermWrapper.class.getSimpleName();
    private boolean isAudioBlocked = false;

    public void confirmPermission() {
        this.isAudioBlocked = HwSystemManager.allowOp(128) ^ 1;
    }

    public boolean confirmPermissionWithResult() {
        this.isAudioBlocked = HwSystemManager.allowOp(128) ^ 1;
        return this.isAudioBlocked;
    }

    public boolean isBlocked() {
        return this.isAudioBlocked;
    }

    public int mockRead(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        for (int count = offsetInBytes; count < offsetInBytes + sizeInBytes; count++) {
            audioData[count] = (byte) 0;
        }
        return 0;
    }

    public int mockRead(short[] audioData, int offsetInShorts, int sizeInShorts) {
        for (int count = offsetInShorts; count < offsetInShorts + sizeInShorts; count++) {
            audioData[count] = (short) 0;
        }
        return 0;
    }
}
