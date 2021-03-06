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
package io.getlime.security.powerauth.app.nextstep.repository;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.StepDefinitionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CrudRepository for persistence of step definitions.
 *
 * @author Roman Strobl
 */
@Component
public interface StepDefinitionRepository extends CrudRepository<StepDefinitionEntity, Long> {

    /**
     * Finds all step definions for operation specified by its name.
     *
     * @param operationName name of the operation
     * @return List of step definitions
     */
    List<StepDefinitionEntity> findStepDefinitionsForOperation(String operationName);

    /**
     * Finds all distict operation names.
     *
     * @return List of operation names
     */
    List<String> findDistinctOperationNames();

}
