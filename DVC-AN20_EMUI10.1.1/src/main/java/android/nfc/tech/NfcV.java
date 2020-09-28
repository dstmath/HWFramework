package android.nfc.tech;

import android.nfc.Tag;
import android.os.Bundle;
import android.os.RemoteException;
import java.io.IOException;

public final class NfcV extends BasicTagTechnology {
    public static final String EXTRA_DSFID = "dsfid";
    public static final String EXTRA_RESP_FLAGS = "respflags";
    private byte mDsfId;
    private byte mRespFlags;

    @Override // android.nfc.tech.BasicTagTechnology, java.io.Closeable, android.nfc.tech.TagTechnology, java.lang.AutoCloseable
    public /* bridge */ /* synthetic */ void close() throws IOException {
        super.close();
    }

    @Override // android.nfc.tech.BasicTagTechnology, android.nfc.tech.TagTechnology
    public /* bridge */ /* synthetic */ void connect() throws IOException {
        super.connect();
    }

    @Override // android.nfc.tech.BasicTagTechnology, android.nfc.tech.TagTechnology
    public /* bridge */ /* synthetic */ Tag getTag() {
        return super.getTag();
    }

    @Override // android.nfc.tech.BasicTagTechnology, android.nfc.tech.TagTechnology
    public /* bridge */ /* synthetic */ boolean isConnected() {
        return super.isConnected();
    }

    @Override // android.nfc.tech.BasicTagTechnology, android.nfc.tech.TagTechnology
    public /* bridge */ /* synthetic */ void reconnect() throws IOException {
        super.reconnect();
    }

    public static NfcV get(Tag tag) {
        if (!tag.hasTech(5)) {
            return null;
        }
        try {
            return new NfcV(tag);
        } catch (RemoteException e) {
            return null;
        }
    }

    public NfcV(Tag tag) throws RemoteException {
        super(tag, 5);
        Bundle extras = tag.getTechExtras(5);
        this.mRespFlags = extras.getByte(EXTRA_RESP_FLAGS);
        this.mDsfId = extras.getByte(EXTRA_DSFID);
    }

    public byte getResponseFlags() {
        return this.mRespFlags;
    }

    public byte getDsfId() {
        return this.mDsfId;
    }

    public byte[] transceive(byte[] data) throws IOException {
        return transceive(data, true);
    }

    public int getMaxTransceiveLength() {
        return getMaxTransceiveLengthInternal();
    }
}
