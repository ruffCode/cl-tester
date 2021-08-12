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
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import java.util.UUID

@Configuration
class R2dbcConfig {

    private val converters = listOf<Converter<*, *>?>(
        UUIDWritingConverter()
    )

    @WritingConverter
    class UUIDWritingConverter : Converter<UUID, UUID> {
        override fun convert(source: UUID): UUID = source
    }

    @Bean
    fun r2dbcCustomConversions(): R2dbcCustomConversions = R2dbcCustomConversions(converters)
}
