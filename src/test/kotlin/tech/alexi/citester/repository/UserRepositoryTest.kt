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

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import tech.alexi.citester.config.BaseIntegrationTest
import tech.alexi.citester.entity.UserEntity
import java.util.UUID

class UserRepositoryTest(
    private val userRepository: UserRepository,
    private val connectionFactory: ConnectionFactory,
) : BaseIntegrationTest() {
    private val client by lazy { DatabaseClient.create(connectionFactory) }

    init {

        afterEach {
            coroutineScope {
                client.sql("""delete from  users_table where  id is not null""").await()
            }
        }

        test("should insert user") {
            coroutineScope {
                userRepository.getAll().test {
                    expectNoEvents()
                }
                userRepository.insertOrUpdate(user1)
                userRepository.getAll().test {
                    awaitItem().email shouldBe user1.email
                    awaitComplete()
                }
            }
        }
        test("should not insert a duplicate user") {
            coroutineScope {
                userRepository.insertOrUpdate(user1)
                shouldThrowExactly<DataIntegrityViolationException> {
                    userRepository.insertOrUpdate(
                        user1.copy(id = UUID.randomUUID())
                    )
                }
                userRepository.getAll().test {
                    awaitItem().email shouldBe user1.email
                    awaitComplete()
                }
            }
        }

        test("should update user email") {
            coroutineScope {
                userRepository.insertOrUpdate(user1)
                userRepository.insertOrUpdate(user1.copy(email = "new@test.com"))
                userRepository.getAll().test {
                    val entity = awaitItem()
                    entity.email shouldBe "new@test.com"
                    entity.updatedAt shouldBeAfter user1.updatedAt
                    awaitComplete()
                }
            }
        }

        test("should find user by id") {
            coroutineScope {
                userRepository.insertOrUpdate(users)
                userRepository.findById(user2.id).test {
                    awaitItem()?.email shouldBe user2.email
                    awaitComplete()
                }
            }
        }

        test("should find user by email") {
            coroutineScope {
                userRepository.insertOrUpdate(users)
                userRepository.findByEmail(user3.email).test {
                    awaitItem()?.id shouldBe user3.id
                    awaitComplete()
                }
            }
        }

        test("should delete a user by id") {
            coroutineScope {
                userRepository.insertOrUpdate(users)
                userRepository.getAll().test {
                    delay(100)
                    expectMostRecentItem().email shouldBe user3.email
                }
                userRepository.deleteById(user3.id)
                userRepository.findById(user3.id).test {
                    awaitComplete()
                }
            }
        }
    }

    companion object {
        val user1 = UserEntity(
            name = "user1",
            email = "user1@test.com"
        )
        val user2 = UserEntity(
            name = "user2",
            email = "user2@test.com"
        )
        val user3 = UserEntity(
            name = "user3",
            email = "user3@test.com"
        )
        val users = listOf(user1, user2, user3)
    }
}
