package ohos.security.eidassistant;

import android.eidservice.HwEidServiceManager;
import java.util.Objects;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;

/* access modifiers changed from: package-private */
public class EidAssistantProxy implements IEidAssistant {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218115840, "EidAssistantProxy");
    private static final int SET_EXCEPTION = -1;
    private final IRemoteObject mRemote;

    EidAssistantProxy(IRemoteObject iRemoteObject) {
        this.mRemote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.mRemote;
    }

    @Override // ohos.security.eidassistant.IEidAssistant
    public int eidInit(EidInfoEntity eidInfoEntity, EidInfoEntity eidInfoEntity2, EidInfoEntity eidInfoEntity3) {
        return HwEidServiceManager.getInstance().eid_init(eidInfoEntity.getContent(), eidInfoEntity.getContentLen(), eidInfoEntity2.getContent(), eidInfoEntity2.getContentLen(), eidInfoEntity3.getContent(), eidInfoEntity3.getContentLen());
    }

    @Override // ohos.security.eidassistant.IEidAssistant
    public int eidFinish() {
        return HwEidServiceManager.getInstance().eid_finish();
    }

    @Override // ohos.security.eidassistant.IEidAssistant
    public int eidGetImage(ControlWordEntity controlWordEntity, EidInfoEntity eidInfoEntity, EidInfoExtendEntity eidInfoExtendEntity, EidInfoExtendEntity eidInfoExtendEntity2) {
        return HwEidServiceManager.getInstance().eid_get_image(controlWordEntity.getTransportCounter(), controlWordEntity.getEncryptionMethod(), eidInfoEntity.getContent(), eidInfoEntity.getContentLen(), eidInfoExtendEntity.getContent(), eidInfoExtendEntity.getContentLen(), eidInfoExtendEntity2.getContent(), eidInfoExtendEntity2.getContentLen());
    }

    @Override // ohos.security.eidassistant.IEidAssistant
    public int eidGetUnsecImage(EidInfoEntity eidInfoEntity, ControlWordEntity controlWordEntity, EidInfoEntity eidInfoEntity2, EidInfoExtendEntity eidInfoExtendEntity, EidInfoExtendEntity eidInfoExtendEntity2) {
        return HwEidServiceManager.getInstance().eid_get_unsec_image(eidInfoEntity.getContent(), eidInfoEntity.getContentLen(), controlWordEntity.getTransportCounter(), controlWordEntity.getEncryptionMethod(), eidInfoEntity2.getContent(), eidInfoEntity2.getContentLen(), eidInfoExtendEntity.getContent(), eidInfoExtendEntity.getContentLen(), eidInfoExtendEntity2.getContent(), eidInfoExtendEntity2.getContentLen());
    }

    @Override // ohos.security.eidassistant.IEidAssistant
    public int eidGetCertificateRequestMessage(EidInfoExtendEntity eidInfoExtendEntity) {
        return HwEidServiceManager.getInstance().eid_get_certificate_request_message(eidInfoExtendEntity.getContent(), eidInfoExtendEntity.getContentLen());
    }

    @Override // ohos.security.eidassistant.IEidAssistant
    public int eidSignInfo(ControlWordEntity controlWordEntity, EidInfoEntity eidInfoEntity, EidInfoExtendEntity eidInfoExtendEntity) {
        return HwEidServiceManager.getInstance().eid_sign_info(controlWordEntity.getTransportCounter(), controlWordEntity.getEncryptionMethod(), eidInfoEntity.getContent(), eidInfoEntity.getContentLen(), eidInfoExtendEntity.getContent(), eidInfoExtendEntity.getContentLen());
    }

    @Override // ohos.security.eidassistant.IEidAssistant
    public int eidGetIdentityInformation(EidInfoExtendEntity eidInfoExtendEntity) {
        return HwEidServiceManager.getInstance().eid_get_identity_information(eidInfoExtendEntity.getContent(), eidInfoExtendEntity.getContentLen());
    }

    @Override // ohos.security.eidassistant.IEidAssistant
    public int eidGetFaceIsChanged(int i) {
        return HwEidServiceManager.getInstance().eid_get_face_is_changed(i);
    }

    @Override // ohos.security.eidassistant.IEidAssistant
    public String eidGetVersion() {
        return HwEidServiceManager.getInstance().eid_get_version();
    }

    @Override // ohos.security.eidassistant.IEidAssistant
    public int eidGetSecImgZip(CoordinateEntity coordinateEntity, ZipInfoEntity zipInfoEntity, OutZipEntity outZipEntity) {
        HwEidServiceManager instance = HwEidServiceManager.getInstance();
        Objects.requireNonNull(instance);
        HwEidServiceManager.CutCoordinate cutCoordinate = new HwEidServiceManager.CutCoordinate(instance);
        cutCoordinate.up = coordinateEntity.getUp();
        cutCoordinate.down = coordinateEntity.getDown();
        cutCoordinate.left = coordinateEntity.getLeft();
        cutCoordinate.right = coordinateEntity.getRight();
        HwEidServiceManager instance2 = HwEidServiceManager.getInstance();
        Objects.requireNonNull(instance2);
        HwEidServiceManager.ImageZipContainer imageZipContainer = new HwEidServiceManager.ImageZipContainer(instance2);
        HwEidServiceManager instance3 = HwEidServiceManager.getInstance();
        Objects.requireNonNull(instance3);
        HwEidServiceManager.EncryptionFactor encryptionFactor = new HwEidServiceManager.EncryptionFactor(instance3);
        if (fillZipInputInfo(imageZipContainer, encryptionFactor, zipInfoEntity) != 0) {
            return -1;
        }
        HwEidServiceManager instance4 = HwEidServiceManager.getInstance();
        Objects.requireNonNull(instance4);
        HwEidServiceManager.SecImageZip secImageZip = new HwEidServiceManager.SecImageZip(instance4);
        int eidGetSecImageZip = HwEidServiceManager.getInstance().eidGetSecImageZip(imageZipContainer, cutCoordinate, encryptionFactor, secImageZip);
        fillZipOutputInfo(outZipEntity, secImageZip);
        return eidGetSecImageZip;
    }

    @Override // ohos.security.eidassistant.IEidAssistant
    public int eidGetUnsecImgZip(ZipInfoEntity zipInfoEntity, OutZipEntity outZipEntity) {
        HwEidServiceManager instance = HwEidServiceManager.getInstance();
        Objects.requireNonNull(instance);
        HwEidServiceManager.ImageZipContainer imageZipContainer = new HwEidServiceManager.ImageZipContainer(instance);
        HwEidServiceManager instance2 = HwEidServiceManager.getInstance();
        Objects.requireNonNull(instance2);
        HwEidServiceManager.EncryptionFactor encryptionFactor = new HwEidServiceManager.EncryptionFactor(instance2);
        if (fillZipInputInfo(imageZipContainer, encryptionFactor, zipInfoEntity) != 0) {
            return -1;
        }
        HwEidServiceManager instance3 = HwEidServiceManager.getInstance();
        Objects.requireNonNull(instance3);
        HwEidServiceManager.SecImageZip secImageZip = new HwEidServiceManager.SecImageZip(instance3);
        int eidGetUnsecImageZip = HwEidServiceManager.getInstance().eidGetUnsecImageZip(imageZipContainer, encryptionFactor, secImageZip);
        fillZipOutputInfo(outZipEntity, secImageZip);
        return eidGetUnsecImageZip;
    }

    private int fillZipInputInfo(HwEidServiceManager.ImageZipContainer imageZipContainer, HwEidServiceManager.EncryptionFactor encryptionFactor, ZipInfoEntity zipInfoEntity) {
        try {
            imageZipContainer.setHash(zipInfoEntity.getHash().getContent());
            imageZipContainer.setHashLen(zipInfoEntity.getHash().getContentLen());
            imageZipContainer.setimageZip(zipInfoEntity.getImageZip().getContent());
            imageZipContainer.setImageZipLen(zipInfoEntity.getImageZip().getContentLen());
            encryptionFactor.setEncryptionMethod(zipInfoEntity.getEncryptionMethod());
            encryptionFactor.setCertificate(zipInfoEntity.getCertificate().getContent());
            encryptionFactor.setCertificateLen(zipInfoEntity.getCertificate().getContentLen());
            return 0;
        } catch (Exception e) {
            HiLog.error(LABEL, "set exceprion %{public}s.", e.getMessage());
            return -1;
        }
    }

    private void fillZipOutputInfo(OutZipEntity outZipEntity, HwEidServiceManager.SecImageZip secImageZip) {
        outZipEntity.getOutImage().setContentLen(secImageZip.secImageLen);
        outZipEntity.getDeSkey().setContentLen(secImageZip.deSkeyLen);
        System.arraycopy(secImageZip.secImage, 0, outZipEntity.getOutImage().getContent(), 0, secImageZip.secImageLen[0]);
        System.arraycopy(secImageZip.deSkey, 0, outZipEntity.getDeSkey().getContent(), 0, secImageZip.deSkeyLen[0]);
    }
}
