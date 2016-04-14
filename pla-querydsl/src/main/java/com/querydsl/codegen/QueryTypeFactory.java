/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.querydsl.codegen;

import com.mysema.codegen.model.Type;

/**
 * {@code QueryTypeFactory} defines an interface for mapping domain types to Querydsl expression types
 *
 * @author tiwe
 *
 */
public interface QueryTypeFactory {

    /**
     * Create an expression type based on the given actual type
     *
     * @param type actual type
     * @return expression type
     */
    Type create(Type type);

}