package ohos.sysappcomponents.contact.chain;

import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.PostalAddress;

public class PostalAddressInsertor extends Insertor {
    @Override // ohos.sysappcomponents.contact.chain.Insertor
    public void fillOperation(Contact contact, ArrayList<DataAbilityOperation> arrayList, Insertor.OperationType operationType) {
        if (!Stream.of(contact, arrayList).anyMatch($$Lambda$PostalAddressInsertor$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (contact.getPostalAddresses() == null || contact.getPostalAddresses().isEmpty()) {
                fillNextItem(contact, arrayList, operationType);
                return;
            }
            for (PostalAddress postalAddress : contact.getPostalAddresses()) {
                DataAbilityOperation.Builder predicatesBuilder = getPredicatesBuilder(Attribute.CommonDataKinds.PostalAddress.CONTENT_ITEM_TYPE, operationType, postalAddress.getId());
                if (!isEmpty(postalAddress.getPostalAddress())) {
                    fillStringContent(predicatesBuilder, "data1", postalAddress.getPostalAddress());
                }
                if (postalAddress.getLabelId() != -1) {
                    fillIntegerContent(predicatesBuilder, "data2", postalAddress.getLabelId());
                }
                if (postalAddress.getLabelId() == 0 && !isEmpty(postalAddress.getLabelName())) {
                    fillStringContent(predicatesBuilder, "data3", postalAddress.getLabelName());
                }
                if (!isEmpty(postalAddress.getStreet())) {
                    fillStringContent(predicatesBuilder, "data4", postalAddress.getStreet());
                }
                if (!isEmpty(postalAddress.getPobox())) {
                    fillStringContent(predicatesBuilder, "data5", postalAddress.getPobox());
                }
                if (!isEmpty(postalAddress.getNeighborhood())) {
                    fillStringContent(predicatesBuilder, "data6", postalAddress.getNeighborhood());
                }
                if (!isEmpty(postalAddress.getCity())) {
                    fillStringContent(predicatesBuilder, "data7", postalAddress.getCity());
                }
                if (!isEmpty(postalAddress.getRegion())) {
                    fillStringContent(predicatesBuilder, "data8", postalAddress.getRegion());
                }
                if (!isEmpty(postalAddress.getPostcode())) {
                    fillStringContent(predicatesBuilder, "data9", postalAddress.getPostcode());
                }
                if (!isEmpty(postalAddress.getCountry())) {
                    fillStringContent(predicatesBuilder, "data10", postalAddress.getCountry());
                }
                arrayList.add(predicatesBuilder.build());
            }
            fillNextItem(contact, arrayList, operationType);
        }
    }
}
