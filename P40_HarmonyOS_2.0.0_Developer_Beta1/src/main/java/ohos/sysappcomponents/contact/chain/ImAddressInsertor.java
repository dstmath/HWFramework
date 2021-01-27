package ohos.sysappcomponents.contact.chain;

import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.ImAddress;

public class ImAddressInsertor extends Insertor {
    @Override // ohos.sysappcomponents.contact.chain.Insertor
    public void fillOperation(Contact contact, ArrayList<DataAbilityOperation> arrayList, Insertor.OperationType operationType) {
        if (!Stream.of(contact, arrayList).anyMatch($$Lambda$ImAddressInsertor$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (contact.getImAddresses() == null || contact.getImAddresses().isEmpty()) {
                fillNextItem(contact, arrayList, operationType);
                return;
            }
            for (ImAddress imAddress : contact.getImAddresses()) {
                DataAbilityOperation.Builder predicatesBuilder = getPredicatesBuilder(Attribute.CommonDataKinds.ImAddress.CONTENT_ITEM_TYPE, operationType, imAddress.getId());
                if (!isEmpty(imAddress.getImAddress())) {
                    fillStringContent(predicatesBuilder, "data1", imAddress.getImAddress());
                }
                if (imAddress.getLabelId() != -2) {
                    fillIntegerContent(predicatesBuilder, "data5", imAddress.getLabelId());
                }
                if (imAddress.getLabelId() == -1 && !isEmpty(imAddress.getLabelName())) {
                    fillStringContent(predicatesBuilder, "data6", imAddress.getLabelName());
                }
                arrayList.add(predicatesBuilder.build());
            }
            fillNextItem(contact, arrayList, operationType);
        }
    }
}
