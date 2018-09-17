package tmsdkobf;

import com.qq.taf.jce.JceInputStream;
import com.qq.taf.jce.JceOutputStream;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;

public final class cl extends JceStruct {
    static ArrayList<cr> fd = new ArrayList();
    public ArrayList<cr> fc = null;

    static {
        fd.add(new cr());
    }

    public JceStruct newInit() {
        return new cl();
    }

    public void readFrom(JceInputStream jceInputStream) {
        this.fc = (ArrayList) jceInputStream.read(fd, 0, true);
    }

    public void writeTo(JceOutputStream jceOutputStream) {
        jceOutputStream.write(this.fc, 0);
    }
}
