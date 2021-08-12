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
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment

@Profile("!test")
@Configuration
class FlywayConfig(
    private val env: Environment,
) {
    @Bean(initMethod = "migrate")
    fun flyway(): Flyway {
        val url = "jdbc:" + env.getRequiredProperty("db.url")
        val user = env.getRequiredProperty("db.username")
        val password = env.getRequiredProperty("db.password")
        val config = Flyway
            .configure()
            .baselineOnMigrate(true)
            .dataSource(url, user, password)
        return Flyway(config)
    }
}
