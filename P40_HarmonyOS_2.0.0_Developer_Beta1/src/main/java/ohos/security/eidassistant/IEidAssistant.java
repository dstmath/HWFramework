package ohos.security.eidassistant;

import ohos.rpc.IRemoteBroker;

/* access modifiers changed from: package-private */
public interface IEidAssistant extends IRemoteBroker {
    public static final String DESCRIPTOR = "IEidAssistant";

    int eidFinish();

    int eidGetCertificateRequestMessage(EidInfoExtendEntity eidInfoExtendEntity);

    int eidGetFaceIsChanged(int i);

    int eidGetIdentityInformation(EidInfoExtendEntity eidInfoExtendEntity);

    int eidGetImage(ControlWordEntity controlWordEntity, EidInfoEntity eidInfoEntity, EidInfoExtendEntity eidInfoExtendEntity, EidInfoExtendEntity eidInfoExtendEntity2);

    int eidGetSecImgZip(CoordinateEntity coordinateEntity, ZipInfoEntity zipInfoEntity, OutZipEntity outZipEntity);

    int eidGetUnsecImage(EidInfoEntity eidInfoEntity, ControlWordEntity controlWordEntity, EidInfoEntity eidInfoEntity2, EidInfoExtendEntity eidInfoExtendEntity, EidInfoExtendEntity eidInfoExtendEntity2);

    int eidGetUnsecImgZip(ZipInfoEntity zipInfoEntity, OutZipEntity outZipEntity);

    String eidGetVersion();

    int eidInit(EidInfoEntity eidInfoEntity, EidInfoEntity eidInfoEntity2, EidInfoEntity eidInfoEntity3);

    int eidSignInfo(ControlWordEntity controlWordEntity, EidInfoEntity eidInfoEntity, EidInfoExtendEntity eidInfoExtendEntity);
}
