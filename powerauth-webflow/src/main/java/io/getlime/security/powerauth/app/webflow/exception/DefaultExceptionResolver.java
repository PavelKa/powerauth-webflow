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

package io.getlime.security.powerauth.app.webflow.exception;

import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthenticationManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller advice responsible for default exception resolving.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@ControllerAdvice
public class DefaultExceptionResolver {

    private AuthenticationManagementService authenticationManagementService;

    /**
     * Initialization of the class with default configuration.
     *
     * @param authenticationManagementService Service managing authentication context.
     */
    @Autowired
    public DefaultExceptionResolver(AuthenticationManagementService authenticationManagementService) {
        this.authenticationManagementService = authenticationManagementService;
    }

    /**
     * Default exception handler, for unexpected errors.
     *
     * @return Response with error details.
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleDefaultException(Throwable t) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred in Web Flow server", t);
        final Error error = new Error(Error.Code.ERROR_GENERIC, "error.unknown");
        return new ErrorResponse(error);
    }

    /**
     * Handling of unauthorized Exception.
     *
     * @return Response with error details.
     */
    @ExceptionHandler(InsufficientAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUnauthorizedException(InsufficientAuthenticationException ex) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred in Web Flow server", ex);
        authenticationManagementService.clearContext();
        return "redirect:/oauth/error";
    }

}
