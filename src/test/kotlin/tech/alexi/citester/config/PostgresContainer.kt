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

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

object PostgresContainer {

    private const val imageTag = "TC_IMAGE_TAG"
    val instance: PostgreSQLContainer<Nothing> by lazy { start().also { flyway(it) } }

    private val container: PostgreSQLContainer<Nothing> by lazy {
        PostgreSQLContainer<Nothing>("postgres:13-alpine")
            .apply {
                withUsername("ci")
                withPassword("ci")
                withDatabaseName("ci")
                withUrlParam(imageTag, "13.1")
            }
    }

    private fun start(): PostgreSQLContainer<Nothing> {
        container.start()
        container.waitingFor(Wait.forHealthcheck())
        return container
    }

    fun stop() {
        container.stop()
    }

    private fun flyway(pgContainer: PostgreSQLContainer<Nothing>) {
        with(pgContainer) {
            waitingFor(Wait.forHealthcheck())
            val url = "jdbc:postgresql://$host:$firstMappedPort/$databaseName"
            FluentConfiguration().dataSource(url, username, password)
                .locations("classpath:/db/migration")
                .apply { Flyway(this).migrate() }
        }
    }
}
