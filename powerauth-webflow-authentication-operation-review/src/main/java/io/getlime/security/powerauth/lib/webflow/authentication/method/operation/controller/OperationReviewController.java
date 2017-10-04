/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getlime.security.powerauth.lib.webflow.authentication.method.operation.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.bankadapter.client.BankAdapterClient;
import io.getlime.security.powerauth.lib.bankadapter.client.BankAdapterClientErrorException;
import io.getlime.security.powerauth.lib.bankadapter.model.entity.AuthMethodChoiceEntity;
import io.getlime.security.powerauth.lib.bankadapter.model.entity.BankAccountChoiceEntity;
import io.getlime.security.powerauth.lib.bankadapter.model.entity.BankAccountEntity;
import io.getlime.security.powerauth.lib.bankadapter.model.response.BankAccountListResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.BankAccountDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.request.OperationReviewRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.request.UpdateOperationFormDataRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.response.OperationReviewDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.response.OperationReviewResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.response.UpdateOperationFormDataResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/auth/operation")
public class OperationReviewController extends AuthMethodController<OperationReviewRequest, OperationReviewResponse, AuthStepException> {

    private final String FIELD_CHOSEN_BANK_ACCOUNT_NUMBER = "chosenBankAccountNumber";
    private final String FIELD_CHOSEN_AUTH_METHOD = "chosenAuthMethod";

    @Autowired
    private BankAdapterClient bankAdapterClient;

    @Autowired
    private NextStepClient nextStepClient;

    @Override
    protected String authenticate(OperationReviewRequest request) throws AuthStepException {
        //TODO: Check pre-authenticated user here
        return getOperation().getUserId();
    }

    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.SHOW_OPERATION_DETAIL;
    }

    @RequestMapping(value = "/detail")
    public @ResponseBody OperationReviewDetailResponse getOperationDetails() {
        final GetOperationDetailResponse operation = getOperation();
        if (operation != null) {
            OperationReviewDetailResponse response = new OperationReviewDetailResponse();
            response.setData(operation.getOperationData());
            response.setFormData(loadFormData(operation));
            return response;
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody OperationReviewResponse getOperationDetails(@RequestBody OperationReviewRequest request) {
        try {
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                @Override
                public OperationReviewResponse doneAuthentication(String userId) {
                    authenticateCurrentBrowserSession();
                    final OperationReviewResponse response = new OperationReviewResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    return response;
                }

                @Override
                public OperationReviewResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final OperationReviewResponse response = new OperationReviewResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    return response;
                }

                @Override
                public OperationReviewResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    final OperationReviewResponse response = new OperationReviewResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    return response;
                }
            });
        } catch (AuthStepException e) {
            final OperationReviewResponse response = new OperationReviewResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public @ResponseBody OperationReviewResponse cancelAuthentication() {
        try {
            cancelAuthorization(getOperation().getOperationId(), null, OperationCancelReason.UNKNOWN, null);
            final OperationReviewResponse response = new OperationReviewResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            return response;
        } catch (NextStepServiceException e) {
            final OperationReviewResponse response = new OperationReviewResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    @RequestMapping(value = "/formData", method = RequestMethod.PUT)
    public @ResponseBody
    UpdateOperationFormDataResponse updateFormData(@RequestBody UpdateOperationFormDataRequest request) {
        final GetOperationDetailResponse operation = getOperation();
        try {
            // update formData in Next Step server
            nextStepClient.updateOperationFormData(operation.getOperationId(), request.getFormData());
            // Send notification to Bank Adapter if the bank account has changed.
            // In case there is no bank account choice, the notification is not performed.
            if (request.getFormData().getUserInput().containsKey(FIELD_CHOSEN_BANK_ACCOUNT_NUMBER)) {
                BankAccountChoiceEntity bankAccountChoice = new BankAccountChoiceEntity();
                bankAccountChoice.setBankAccountNumber(request.getFormData().getUserInput().get(FIELD_CHOSEN_BANK_ACCOUNT_NUMBER));
                bankAdapterClient.sendFormDataChangedNotification(bankAccountChoice, operation.getUserId(), operation.getOperationId());
            }
            if (request.getFormData().getUserInput().containsKey(FIELD_CHOSEN_AUTH_METHOD)) {
                AuthMethodChoiceEntity authMethodChoice = new AuthMethodChoiceEntity();
                String chosenAuthMethod = request.getFormData().getUserInput().get(FIELD_CHOSEN_AUTH_METHOD);
                authMethodChoice.setChosenAuthMethod(AuthMethodChoiceEntity.ChosenAuthMethod.valueOf(chosenAuthMethod));
                bankAdapterClient.sendFormDataChangedNotification(authMethodChoice, operation.getUserId(), operation.getOperationId());
            }
            return new UpdateOperationFormDataResponse();
        } catch (NextStepServiceException | BankAdapterClientErrorException e) {
            final UpdateOperationFormDataResponse response = new UpdateOperationFormDataResponse();
            response.setResult(AuthStepResult.AUTH_METHOD_FAILED);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    private OperationFormData loadFormData(GetOperationDetailResponse operation) {
        OperationFormData formData = operation.getFormData();
        if (formData==null || operation.getUserId()==null) {
            return formData;
        }
        if (!formData.isDynamicDataLoaded()) {
            // Dynamic data has not been loaded yet. At this point the user is authenticated, so we can
            // load dynamic data based on user id. For now dynamic data contains the bank account list,
            // however it can be easily extended in the future.
            try {
                ObjectResponse<BankAccountListResponse> response = bankAdapterClient.fetchBankAccounts(operation.getUserId());
                List<BankAccountEntity> bankAccountEntities = response.getResponseObject().getBankAccounts();
                List<BankAccountDetail> bankAccountDetails = convertBankAccountEntities(bankAccountEntities);
                if (!bankAccountDetails.isEmpty()) {
                    formData.addBankAccountChoice("bankAccountChoice", bankAccountDetails, null);
                }
                formData.setDynamicDataLoaded(true);
            } catch (BankAdapterClientErrorException e) {
                // Failed to load dynamic data, log the error. The UI will handle missing dynamic data error separately.
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Failed to load dynamic operation data", e);
            }
        }
        return formData;
    }

    private List<BankAccountDetail> convertBankAccountEntities(List<BankAccountEntity> bankAccountEntities) {
        // TODO - move to a converter class
        List<BankAccountDetail> bankAccountDetails = new ArrayList<>();
        if (bankAccountEntities==null || bankAccountEntities.isEmpty()) {
            return bankAccountDetails;
        }
        for (BankAccountEntity bankAccountEntity: bankAccountEntities) {
            BankAccountDetail bankAccount = new BankAccountDetail();
            bankAccount.setName(bankAccountEntity.getName());
            bankAccount.setNumber(bankAccountEntity.getNumber());
            bankAccount.setBalance(bankAccountEntity.getBalance());
            bankAccount.setCurrency(bankAccountEntity.getCurrency());
            bankAccount.setUsableForPayment(bankAccountEntity.isUsableForPayment());
            bankAccount.setUnusableForPaymentReason(bankAccountEntity.getUnusableForPaymentReason());
            bankAccountDetails.add(bankAccount);
        }
        return bankAccountDetails;
    }

}