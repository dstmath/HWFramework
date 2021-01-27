package ohos.sysappcomponents.contact.chain;

import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.HiCallDevice;

public class HiCallDeviceChain extends Insertor {
    @Override // ohos.sysappcomponents.contact.chain.Insertor
    public void fillOperation(Contact contact, ArrayList<DataAbilityOperation> arrayList, Insertor.OperationType operationType) {
        if (!Stream.of(contact, arrayList).anyMatch($$Lambda$HiCallDeviceChain$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (contact.getHiCallDevices() == null || contact.getHiCallDevices().isEmpty()) {
                fillNextItem(contact, arrayList, operationType);
                return;
            }
            for (HiCallDevice hiCallDevice : contact.getHiCallDevices()) {
                DataAbilityOperation.Builder predicatesBuilder = getPredicatesBuilder(Attribute.HiCallDevice.CONTENT_ITEM_TYPE, operationType, hiCallDevice.getId());
                if (!isEmpty(hiCallDevice.getDeviceCommuncationId())) {
                    fillStringContent(predicatesBuilder, "data4", hiCallDevice.getDeviceCommuncationId());
                }
                if (!isEmpty(hiCallDevice.getDeviceType())) {
                    fillStringContent(predicatesBuilder, "data5", hiCallDevice.getDeviceType());
                }
                if (!isEmpty(hiCallDevice.getPrivate())) {
                    fillStringContent(predicatesBuilder, "data6", hiCallDevice.getPrivate());
                }
                if (!isEmpty(hiCallDevice.getDeviceProfile())) {
                    fillStringContent(predicatesBuilder, "data7", hiCallDevice.getDeviceProfile());
                }
                if (!isEmpty(hiCallDevice.getSameVibration())) {
                    fillStringContent(predicatesBuilder, "data8", hiCallDevice.getSameVibration());
                }
                if (!isEmpty(hiCallDevice.getDeviceOrdinal())) {
                    fillStringContent(predicatesBuilder, "data9", hiCallDevice.getDeviceOrdinal());
                }
                if (!isEmpty(hiCallDevice.getDeviceModel())) {
                    fillStringContent(predicatesBuilder, "data10", hiCallDevice.getDeviceModel());
                }
                if (!isEmpty(hiCallDevice.getRemarkName())) {
                    fillStringContent(predicatesBuilder, "data11", hiCallDevice.getRemarkName());
                }
                if (!isEmpty(hiCallDevice.getUserName())) {
                    fillStringContent(predicatesBuilder, "data12", hiCallDevice.getUserName());
                }
                if (!isEmpty(hiCallDevice.getDeviceInfo())) {
                    fillStringContent(predicatesBuilder, "data13", hiCallDevice.getDeviceInfo());
                }
                arrayList.add(predicatesBuilder.build());
            }
            fillNextItem(contact, arrayList, operationType);
        }
    }
}
