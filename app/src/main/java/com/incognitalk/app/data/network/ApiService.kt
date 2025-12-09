package com.incognitalk.app.data.network

import com.incognitalk.app.data.model.PreKeyBundleDto
import com.incognitalk.app.data.model.RegisterRequest
import com.incognitalk.app.data.model.ReplenishKeysRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

interface ApiService {
    suspend fun getPreKeyBundle(userId: String, deviceId: Int): PreKeyBundleDto

    suspend fun checkUsernameAvailability(username: String): Boolean

    suspend fun registerUser(request: RegisterRequest)

    suspend fun replenishKeys(request: ReplenishKeysRequest)
}

class ApiServiceImpl(private val client: HttpClient) : ApiService {
    private val domain = "http://10.0.2.2:8080"

    override suspend fun getPreKeyBundle(userId: String, deviceId: Int): PreKeyBundleDto {
        return client.get("$domain/keys/$userId/$deviceId").body()
    }

    override suspend fun checkUsernameAvailability(username: String): Boolean {
        val response: HttpResponse = client.get("$domain/users/available/$username")
        return response.status == HttpStatusCode.OK
    }

    override suspend fun registerUser(request: RegisterRequest) {
        client.post("$domain/users/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun replenishKeys(request: ReplenishKeysRequest) {
        client.post("$domain/keys/replenish") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}

object KtorClient {
    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
    }
}
