package ohos.global.resource.solidxml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import ohos.global.innerkit.asset.Package;
import ohos.global.resource.ResourceManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;

public class SolidXmllmpl extends SolidXml {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "SolidXmllmpl");
    private static final float LOAD_FRACTOR = 1.0f;
    private static final int MAGIC_NAME_LENGTH = 12;
    private static final int MAX_CAPACITY = 5;
    private static String[] dictionaryFileNames = {"nodes.key", "attributes.key", "constants.key", "contents.key"};
    List<NodeImpl> nodeList;
    private String path;
    private ResourceManager resMgr;
    private Package resPackage;
    private String sxmlParentPath = "";

    public SolidXmllmpl(ResourceManager resourceManager, Package r3, String str) {
        this.path = str;
        this.resPackage = r3;
        this.resMgr = resourceManager;
        parseSxml();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0103, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0104, code lost:
        if (r1 != null) goto L_0x0106;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0106, code lost:
        $closeResource(r14, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0109, code lost:
        throw r2;
     */
    private void parseSxml() {
        try {
            InputStream open = this.resPackage.open(this.path);
            if (open == null) {
                HiLog.error(LABEL, "open sxml failed", new Object[0]);
                if (open != null) {
                    $closeResource(null, open);
                }
            } else if (open.skip(12) < 12) {
                HiLog.error(LABEL, "sxml length too short", new Object[0]);
                $closeResource(null, open);
            } else {
                byte[] bArr = new byte[open.available()];
                if (open.read(bArr) != bArr.length) {
                    HiLog.error(LABEL, "sxml content read failed", new Object[0]);
                    $closeResource(null, open);
                    return;
                }
                ByteBuffer wrap = ByteBuffer.wrap(bArr);
                wrap.order(ByteOrder.LITTLE_ENDIAN);
                wrap.getInt();
                int i = wrap.getInt();
                int i2 = wrap.getInt();
                if ((((long) i) * 28) + (((long) i2) * 12) + (((long) wrap.getInt()) * 8) != ((long) (bArr.length - 16))) {
                    HiLog.error(LABEL, "invalid sxml format", new Object[0]);
                    $closeResource(null, open);
                    return;
                }
                HashMap<String, String[]> keyInfo = getKeyInfo();
                String[] strArr = keyInfo.get("nodes.key");
                String[] strArr2 = keyInfo.get("attributes.key");
                String[] strArr3 = keyInfo.get("constants.key");
                if (strArr == null || strArr2 == null || strArr3 == null) {
                    HiLog.error(LABEL, "key does not load properly", new Object[0]);
                    $closeResource(null, open);
                    return;
                }
                this.nodeList = new ArrayList(i);
                readNodes(wrap, i, strArr, strArr3);
                List<TypedAttributeImpl> readAttributes = readAttributes(wrap, i2, strArr2, strArr3);
                int size = readAttributes.size();
                for (NodeImpl nodeImpl : this.nodeList) {
                    nodeImpl.attrs = new ArrayList(nodeImpl.attrCount);
                    int i3 = nodeImpl.attrIndex;
                    int i4 = 0;
                    while (i4 < nodeImpl.attrCount && i3 < size) {
                        nodeImpl.attrs.add(readAttributes.get(i3));
                        i4++;
                        i3++;
                    }
                }
                $closeResource(null, open);
            }
        } catch (IOException unused) {
            HiLog.error(LABEL, "load sxml failed.", new Object[0]);
        }
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th != null) {
            try {
                autoCloseable.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        } else {
            autoCloseable.close();
        }
    }

    private List<TypedAttributeImpl> readAttributes(ByteBuffer byteBuffer, int i, String[] strArr, String[] strArr2) {
        ArrayList arrayList = new ArrayList(i);
        for (int i2 = 0; i2 < i; i2++) {
            byteBuffer.getInt();
            int i3 = byteBuffer.getInt();
            String str = "";
            String str2 = (i3 < 0 || i3 >= strArr.length) ? str : strArr[i3];
            int i4 = byteBuffer.getInt();
            if (i4 >= 0 && i4 < strArr2.length) {
                str = strArr2[i4];
            }
            arrayList.add(new TypedAttributeImpl(this.resMgr, str2, str));
        }
        return arrayList;
    }

    private void readNodes(ByteBuffer byteBuffer, int i, String[] strArr, String[] strArr2) {
        for (int i2 = 0; i2 < i; i2++) {
            NodeImpl nodeImpl = new NodeImpl(this);
            byteBuffer.getInt();
            int i3 = byteBuffer.getInt();
            if (i3 >= 0 && i3 < strArr.length) {
                nodeImpl.name = strArr[i3];
            }
            int i4 = byteBuffer.getInt();
            if (i4 >= 0 && i4 < strArr2.length) {
                nodeImpl.value = strArr2[i4];
            }
            nodeImpl.child = byteBuffer.getInt();
            nodeImpl.brother = byteBuffer.getInt();
            nodeImpl.attrIndex = byteBuffer.getInt();
            nodeImpl.attrCount = byteBuffer.getInt();
            this.nodeList.add(nodeImpl);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005c, code lost:
        if (r6 != null) goto L_0x005e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005e, code lost:
        $closeResource(null, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0090, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0091, code lost:
        if (r6 != null) goto L_0x0093;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0093, code lost:
        $closeResource(r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0096, code lost:
        throw r7;
     */
    public HashMap<String, String[]> getKeyInfo() {
        String str;
        if (this.path.lastIndexOf(47) != -1) {
            String str2 = this.path;
            this.sxmlParentPath = str2.substring(0, str2.lastIndexOf(47));
        }
        HashMap<String, String[]> hashMap = new HashMap<>();
        String[] strArr = dictionaryFileNames;
        int length = strArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String str3 = strArr[i];
            if (this.sxmlParentPath.length() > 0) {
                str = this.sxmlParentPath + "/" + str3;
            } else {
                str = str3;
            }
            new ArrayList();
            try {
                InputStream open = this.resPackage.open(str);
                if (open == null) {
                    HiLog.error(LABEL, "open key file failed", new Object[0]);
                } else {
                    byte[] bArr = new byte[open.available()];
                    if (open.read(bArr) != bArr.length) {
                        HiLog.error(LABEL, "key content read failed", new Object[0]);
                        break;
                    }
                    hashMap.put(str3, new String(bArr, 0, bArr.length, ConstantValue.JPEG_FILE_NAME_ENCODE_CHARSET).split("\u0000"));
                    $closeResource(null, open);
                    i++;
                }
            } catch (IOException unused) {
                HiLog.error(LABEL, "SolidXmlImpl read key file failed.", new Object[0]);
            }
        }
        return hashMap;
    }

    @Override // ohos.global.resource.solidxml.SolidXml
    public NodeImpl getRoot() {
        List<NodeImpl> list = this.nodeList;
        if (list == null || list.size() == 0) {
            return null;
        }
        return this.nodeList.get(0);
    }
}
