package io.getlime.security.powerauth.lib.nextstep.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Operation form data represents data visible to the user during the operation and collected responses.
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class OperationFormData {

    private OperationFormAttribute title;
    private OperationFormAttribute message;
    private List<OperationFormFieldAttribute> parameters;
    private boolean dynamicDataLoaded;
    private Map<String, String> userInput;

    public OperationFormData() {
        this.parameters = new ArrayList<>();
        this.userInput = new LinkedHashMap<>();
    }

    /**
     * Get form attributes.
     * @return Form attributes.
     */
    public List<OperationFormFieldAttribute> getParameters() {
        return parameters;
    }

    /**
     * Whether dynamic data is loaded.
     * @return True if dynamic data is loaded, otherwise false.
     */
    public boolean isDynamicDataLoaded() {
        return dynamicDataLoaded;
    }

    /**
     * Set whether dynamic data is loaded.
     * @param dynamicDataLoaded True if dynamic data is loaded, otherwise false.
     */
    public void setDynamicDataLoaded(boolean dynamicDataLoaded) {
        this.dynamicDataLoaded = dynamicDataLoaded;
    }

    /**
     * Get key-value map with user input.
     * @return User input.
     */
    public Map<String, String> getUserInput() {
        return userInput;
    }

    /**
     * Set title form attribute.
     * @param title Title form attribute.
     */
    public void setTitle(OperationFormAttribute title) {
        if (title == null) {
            // avoid JSON mapping null title
            return;
        }
        this.title = title;
    }

    /**
     * Set title.
     * @param titleId Title ID.
     */
    @JsonIgnore
    public void addTitle(String titleId) {
        OperationFormAttribute attr = new OperationFormAttribute();
        attr.setId(titleId);
        this.title = attr;
    }

    /**
     * Set localized title.
     * @param titleId Title ID.
     */
    @JsonIgnore
    public void addTitle(String titleId, String title) {
        OperationFormAttribute attr = new OperationFormAttribute();
        attr.setId(titleId);
        attr.setValue(title);
        this.title = attr;
    }

    /**
     * Get title form attribute.
     * @return Title form attribute.
     */
    public OperationFormAttribute getTitle() {
        return title;
    }

    /**
     * Set message form attribute.
     * @param message Message form attribute.
     */
    public void setMessage(OperationFormAttribute message) {
        if (message == null) {
            // avoid JSON mapping null title
            return;
        }
        this.message = message;
    }

    /**
     * Set message.
     * @param messageId Message ID.
     */
    @JsonIgnore
    public void addMessage(String messageId) {
        OperationFormAttribute attr = new OperationFormAttribute();
        attr.setId(messageId);
        this.message = attr;
    }

    /**
     * Set localized message.
     * @param messageId Message ID.
     */
    @JsonIgnore
    public void addMessage(String messageId, String message) {
        OperationFormAttribute attr = new OperationFormAttribute();
        attr.setId(messageId);
        attr.setValue(message);
        this.message = attr;
    }

    /**
     * Get message form attribute.
     * @return Message form attribute.
     */
    public OperationFormAttribute getMessage() {
        return message;
    }

    /**
     * Set amount.
     * @param amountId Amount ID.
     * @param amount Amount value.
     * @param currencyId Currency ID.
     * @param currency Amount currency.
     */
    @JsonIgnore
    public void addAmount(String amountId, BigDecimal amount, String currencyId, String currency) {
        OperationAmountFieldAttribute amountAttr = new OperationAmountFieldAttribute();
        amountAttr.setId(amountId);
        amountAttr.setAmount(amount);
        amountAttr.setCurrencyId(currencyId);
        amountAttr.setCurrency(currency);
        saveAttribute(amountAttr);
    }

    /**
     * Get amount.
     * @return Amount.
     */
    @JsonIgnore
    public OperationAmountFieldAttribute getAmount() {
        List<OperationFormFieldAttribute> amountAttrs = getAttributesByType(OperationFormFieldAttribute.Type.AMOUNT);
        if (amountAttrs.isEmpty()) {
            return null;
        }
        if (amountAttrs.size()>1) {
            throw new IllegalStateException("Multiple attributes of type AMOUNT found");
        }
        return (OperationAmountFieldAttribute) amountAttrs.get(0);
    }

    /**
     * Set localized note.
     * @param noteId Note ID.
     * @param note Localized note.
     */
    @JsonIgnore
    public void addNote(String noteId, String note) {
        OperationNoteFieldAttribute attr = new OperationNoteFieldAttribute();
        attr.setId(noteId);
        attr.setNote(note);
        saveAttribute(attr);
    }

    /**
     * Get note.
     * @return Note.
     */
    @JsonIgnore
    public OperationNoteFieldAttribute getNote() {
        List<OperationFormFieldAttribute> attrs = getAttributesByType(OperationFormFieldAttribute.Type.NOTE);
        if (attrs == null) {
            return null;
        }
        if (attrs.size()>1) {
            throw new IllegalStateException("Multiple attributes of type MESSAGE found");
        }
        return (OperationNoteFieldAttribute) attrs.get(0);
    }

    /**
     * Add a bank account choice.
     * @param id Bank account choice ID.
     * @param bankAccounts List of bank accounts.
     */
    @JsonIgnore
    public void addBankAccountChoice(String id, List<BankAccountDetail> bankAccounts) {
        OperationBankAccountChoiceFieldAttribute attr = new OperationBankAccountChoiceFieldAttribute();
        attr.setId(id);
        attr.setBankAccounts(bankAccounts);
        saveAttribute(attr);
    }

    /**
     * Add a key-value attribute.
     * @param id Attribute ID.
     * @param value Attribute value.
     */
    @JsonIgnore
    public void addKeyValue(String id, String value) {
        OperationKeyValueFieldAttribute attr = new OperationKeyValueFieldAttribute();
        attr.setId(id);
        attr.setValue(value);
        saveAttribute(attr);
    }

    /**
     * Get attribute by id.
     * @param id Attribute ID.
     * @return Attribute.
     */
    @JsonIgnore
    public OperationFormFieldAttribute getAttributeById(String id) {
        for (OperationFormFieldAttribute attr: parameters) {
            if (attr.getId().equals(id)) {
                return attr;
            }
        }
        return null;
    }

    /**
     * Get attribute by type.
     * @param type Attribute by type.
     * @return Attribute.
     */
    @JsonIgnore
    public List<OperationFormFieldAttribute> getAttributesByType(OperationFormFieldAttribute.Type type) {
        List<OperationFormFieldAttribute> attrs = new ArrayList<>();
        for (OperationFormFieldAttribute attr: parameters) {
            if (attr.getType() == type) {
                attrs.add(attr);
            }
        }
        return attrs;
    }

    /**
     * Add a key-value user input.
     * @param key User input key.
     * @param value User input value.
     */
    public void addUserInput(String key, String value) {
        userInput.put(key, value);
    }

    /**
     * Set user input map.
     * @param userInput User input.
     */
    public void setUserInput(Map<String, String> userInput) {
        this.userInput = userInput;
    }

    /**
     * Adds attribute or updates existing attribute based on its ID.
     * @param attributeToSave Attribute to save.
     */
    private void saveAttribute(OperationFormFieldAttribute attributeToSave) {
        if (attributeToSave == null || attributeToSave.getId() == null) {
            throw new IllegalArgumentException("Invalid attribute");
        }
        Integer existingIndex = null;
        int counter = 0;
        for (OperationFormFieldAttribute attr: parameters) {
            if (attr.getId().equals(attributeToSave.getId())) {
                existingIndex = counter;
                break;
            }
            counter++;
        }
        if (existingIndex != null) {
            parameters.set(existingIndex, attributeToSave);
        } else {
            parameters.add(attributeToSave);
        }
    }
}
