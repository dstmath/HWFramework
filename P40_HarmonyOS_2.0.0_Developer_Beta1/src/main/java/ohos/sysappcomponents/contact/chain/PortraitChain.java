package ohos.sysappcomponents.contact.chain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.app.Context;
import ohos.net.UriConverter;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.LogUtil;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.Portrait;

public class PortraitChain extends Insertor {
    private static final String TAG = "PortraitChain";
    private Context context;

    public PortraitChain(Context context2) {
        this.context = context2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0064, code lost:
        if (r4 != null) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x006c, code lost:
        if (r4 == null) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0072, code lost:
        ohos.sysappcomponents.contact.LogUtil.error(ohos.sysappcomponents.contact.chain.PortraitChain.TAG, "InputStream close error");
     */
    @Override // ohos.sysappcomponents.contact.chain.Insertor
    public void fillOperation(Contact contact, ArrayList<DataAbilityOperation> arrayList, Insertor.OperationType operationType) {
        if (!Stream.of(contact, arrayList, this.context).anyMatch($$Lambda$PortraitChain$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (contact.getPortrait() == null) {
                fillNextItem(contact, arrayList, operationType);
                return;
            }
            Portrait portrait = contact.getPortrait();
            DataAbilityOperation.Builder predicatesBuilder = getPredicatesBuilder(Attribute.CommonDataKinds.Portrait.CONTENT_ITEM_TYPE, operationType, portrait.getId());
            if (portrait.getUri() != null) {
                InputStream inputStream = null;
                try {
                    inputStream = Attribute.getAplatFromContext(this.context).getContentResolver().openInputStream(UriConverter.convertToAndroidUri(portrait.getUri()));
                    fillBlobContent(predicatesBuilder, "data15", getBytes(inputStream));
                    arrayList.add(predicatesBuilder.build());
                } catch (IOException unused) {
                    LogUtil.error(TAG, "InputStream close error");
                } catch (Throwable th) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException unused2) {
                            LogUtil.error(TAG, "InputStream close error");
                        }
                    }
                    throw th;
                }
            }
            fillNextItem(contact, arrayList, operationType);
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[1024];
        while (true) {
            int read = inputStream.read(bArr);
            if (read == -1) {
                return byteArrayOutputStream.toByteArray();
            }
            byteArrayOutputStream.write(bArr, 0, read);
        }
    }
}
