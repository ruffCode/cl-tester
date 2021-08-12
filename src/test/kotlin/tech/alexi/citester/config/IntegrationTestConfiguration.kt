/*
 * Copyright 2021 Alexi Bre
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.alexi.citester.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.PostgreSQLContainer

@Configuration
class IntegrationTestConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    fun postgresqlContainer(): PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>("postgres:13-alpine").apply {
        withUsername("ci")
        withPassword("ci")
        withDatabaseName("ci")
        withUrlParam("TC_IMAGE_TAG", "13.1")
    }
}
