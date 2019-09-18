package android.nfc.tech;

import android.nfc.Tag;
import android.os.Bundle;
import android.os.RemoteException;
import java.io.IOException;

public final class NfcB extends BasicTagTechnology {
    public static final String EXTRA_APPDATA = "appdata";
    public static final String EXTRA_PROTINFO = "protinfo";
    private byte[] mAppData;
    private byte[] mProtInfo;

    public /* bridge */ /* synthetic */ void close() throws IOException {
        super.close();
    }

    public /* bridge */ /* synthetic */ void connect() throws IOException {
        super.connect();
    }

    public /* bridge */ /* synthetic */ Tag getTag() {
        return super.getTag();
    }

    public /* bridge */ /* synthetic */ boolean isConnected() {
        return super.isConnected();
    }

    public /* bridge */ /* synthetic */ void reconnect() throws IOException {
        super.reconnect();
    }

    public static NfcB get(Tag tag) {
        if (!tag.hasTech(2)) {
            return null;
        }
        try {
            return new NfcB(tag);
        } catch (RemoteException e) {
            return null;
        }
    }

    public NfcB(Tag tag) throws RemoteException {
        super(tag, 2);
        Bundle extras = tag.getTechExtras(2);
        this.mAppData = extras.getByteArray(EXTRA_APPDATA);
        this.mProtInfo = extras.getByteArray(EXTRA_PROTINFO);
    }

    public byte[] getApplicationData() {
        return this.mAppData;
    }

    public byte[] getProtocolInfo() {
        return this.mProtInfo;
    }

    public byte[] transceive(byte[] data) throws IOException {
        return transceive(data, true);
    }

    public int getMaxTransceiveLength() {
        return getMaxTransceiveLengthInternal();
    }
}
