package javax.obex;

import java.io.IOException;
import java.io.InputStream;

public class ObexPacket {
    public int mHeaderId;
    public int mLength;
    public byte[] mPayload = null;

    private ObexPacket(int headerId, int length) {
        this.mHeaderId = headerId;
        this.mLength = length;
    }

    public static ObexPacket read(InputStream is) throws IOException {
        return read(is.read(), is);
    }

    public static ObexPacket read(int headerId, InputStream is) throws IOException {
        int length = (is.read() << 8) + is.read();
        ObexPacket newPacket = new ObexPacket(headerId, length);
        byte[] temp = null;
        if (length > 3) {
            temp = new byte[(length - 3)];
            int bytesReceived = is.read(temp);
            while (bytesReceived != temp.length) {
                bytesReceived += is.read(temp, bytesReceived, temp.length - bytesReceived);
            }
        }
        newPacket.mPayload = temp;
        return newPacket;
    }
}
