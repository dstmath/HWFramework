package ohos.extshb.agent;

import java.util.Arrays;
import ohos.annotation.SystemApi;

@SystemApi
public class CommandResult {
    private byte[] data;
    private int ret;

    public int getRet() {
        return this.ret;
    }

    public byte[] getData() {
        byte[] bArr = this.data;
        if (bArr == null) {
            return new byte[0];
        }
        return Arrays.copyOf(bArr, bArr.length);
    }

    public void setRet(int i) {
        this.ret = i;
    }

    public void setData(byte[] bArr) {
        if (bArr == null) {
            this.data = new byte[0];
        } else {
            this.data = Arrays.copyOf(bArr, bArr.length);
        }
    }
}
