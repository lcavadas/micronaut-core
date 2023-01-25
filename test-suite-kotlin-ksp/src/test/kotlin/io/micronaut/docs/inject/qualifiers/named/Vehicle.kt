/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.docs.inject.qualifiers.named

import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton

// tag::class[]
@Singleton
class Vehicle @Inject
constructor(@param:Named("v8") private val engine: Engine) { // <4>

    fun start(): String {
        return engine.start() // <5>
    }
}
// end::class[]