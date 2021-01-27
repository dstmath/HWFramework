package ohos.sysappcomponents.contact.chain;

import java.util.ArrayList;
import java.util.stream.Stream;
import ohos.aafwk.ability.DataAbilityOperation;
import ohos.sysappcomponents.contact.Attribute;
import ohos.sysappcomponents.contact.chain.Insertor;
import ohos.sysappcomponents.contact.entity.Contact;
import ohos.sysappcomponents.contact.entity.Organization;

public class OrganizationInsertor extends Insertor {
    @Override // ohos.sysappcomponents.contact.chain.Insertor
    public void fillOperation(Contact contact, ArrayList<DataAbilityOperation> arrayList, Insertor.OperationType operationType) {
        if (!Stream.of(contact, arrayList).anyMatch($$Lambda$OrganizationInsertor$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            if (contact.getOrganization() == null) {
                fillNextItem(contact, arrayList, operationType);
                return;
            }
            Organization organization = contact.getOrganization();
            DataAbilityOperation.Builder predicatesBuilder = getPredicatesBuilder(Attribute.CommonDataKinds.Organization.CONTENT_ITEM_TYPE, operationType, organization.getId());
            if (!isEmpty(organization.getName())) {
                fillStringContent(predicatesBuilder, "data1", organization.getName());
            }
            if (!isEmpty(organization.getTitle())) {
                fillStringContent(predicatesBuilder, "data4", organization.getTitle());
            }
            arrayList.add(predicatesBuilder.build());
            fillNextItem(contact, arrayList, operationType);
        }
    }
}
