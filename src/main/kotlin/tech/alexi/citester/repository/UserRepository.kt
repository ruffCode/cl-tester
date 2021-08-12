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
package tech.alexi.citester.repository

import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.stereotype.Repository
import tech.alexi.citester.entity.UserEntity
import java.util.UUID

interface UserRepository {
    suspend fun insertOrUpdate(user: UserEntity)
    suspend fun insertOrUpdate(users: List<UserEntity>)
    fun getAll(): Flow<UserEntity>
    fun findById(id: UUID): Flow<UserEntity?>
    fun findByEmail(email: String): Flow<UserEntity?>
    suspend fun deleteById(id: UUID)
}

@Repository
class UserRepositoryImpl(
    private val connectionFactory: ConnectionFactory,
    private val r2dbcConverter: R2dbcConverter,
) : UserRepository {

    private val client: DatabaseClient by lazy {
        DatabaseClient.create(connectionFactory)
    }

    override suspend fun insertOrUpdate(user: UserEntity) {
        client.sql(
            """
            insert into users_table (id,name,email) values (:id,:name,:email)
            on conflict (id) do update set name = :name ,email = :email
            """.trimIndent()
        ).bind("id", user.id)
            .bind("name", user.name)
            .bind("email", user.email).await()
    }

    override suspend fun insertOrUpdate(users: List<UserEntity>) {
        users.forEach {
            insertOrUpdate(it)
        }
    }

    override fun getAll(): Flow<UserEntity> =
        client.sql(
            """
            select * from users_table
            """.trimIndent()
        ).map { row -> r2dbcConverter.read(UserEntity::class.java, row) }.all().asFlow()

    override fun findById(id: UUID): Flow<UserEntity?> =
        client.sql(
            """
            select * from users_table where id = :id
            """.trimIndent()
        ).bind("id", id).map { row -> r2dbcConverter.read(UserEntity::class.java, row) }
            .one().asFlow()

    override fun findByEmail(email: String): Flow<UserEntity?> =
        client.sql(
            """
            select * from users_table where email = :email
            """.trimIndent()
        ).bind("email", email).map { row -> r2dbcConverter.read(UserEntity::class.java, row) }
            .one().asFlow()

    override suspend fun deleteById(id: UUID) {
        client.sql(
            """
           delete from users_table where id = :id
            """.trimIndent()
        ).bind("id", id).await()
    }
}
