package ohos.sysappcomponents.contact.chain;

import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.SipAddress;

public class SipAddressInsertor extends Insertor {
    @Override // ohos.sysappcomponents.contact.chain.Insertor
    public void fillOperation(Contact contact, ArrayList<DataAbilityOperation> arrayList, Insertor.OperationType operationType) {
        if (!Stream.of(contact, arrayList).anyMatch($$Lambda$SipAddressInsertor$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (contact.getSipAddresses() == null || contact.getSipAddresses().isEmpty()) {
                fillNextItem(contact, arrayList, operationType);
                return;
            }
            for (SipAddress sipAddress : contact.getSipAddresses()) {
                DataAbilityOperation.Builder predicatesBuilder = getPredicatesBuilder(Attribute.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE, operationType, sipAddress.getId());
                if (!isEmpty(sipAddress.getSipAddress())) {
                    fillStringContent(predicatesBuilder, "data1", sipAddress.getSipAddress());
                }
                if (sipAddress.getLabelId() != -1) {
                    fillIntegerContent(predicatesBuilder, "data2", sipAddress.getLabelId());
                }
                if (sipAddress.getLabelId() == 0 && !isEmpty(sipAddress.getLabelName())) {
                    fillStringContent(predicatesBuilder, "data3", sipAddress.getLabelName());
                }
                arrayList.add(predicatesBuilder.build());
            }
            fillNextItem(contact, arrayList, operationType);
        }
    }
}
