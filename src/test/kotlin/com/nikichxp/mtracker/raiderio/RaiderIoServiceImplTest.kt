package com.nikichxp.mtracker.raiderio

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nikichxp.mtracker.config.Beans
import com.nikichxp.mtracker.config.MtrackerProperties
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.statement.HttpResponse
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.JacksonConverter
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RaiderIoServiceImplTest {

    private val objectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    @Mock
    private lateinit var props: MtrackerProperties

    @Mock
    private lateinit var eventLimiter: EventLimiter

    @InjectMocks
    private lateinit var service: RaiderIoServiceImpl

    private var mockEngineHandler: (suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData)? = null

    @Spy
    private var httpClient: HttpClient = HttpClient(MockEngine { request ->
        mockEngineHandler!!(request)
    }) {
        install(ContentNegotiation) {
            register(io.ktor.http.ContentType.Application.Json, JacksonConverter(objectMapper))
        }
    }

    @BeforeEach
    fun setup() {
        mockEngineHandler = null
        runBlocking {
            whenever(props.region) doReturn "eu"
            whenever(props.raiderio) doReturn MtrackerProperties.RaiderIo()
            whenever(eventLimiter.invoke<HttpResponse>(any<String>(), any())) doAnswer { invocation ->
                val action = invocation.getArgument<Any>(1) as suspend () -> HttpResponse
                runBlocking { action() }
            }
        }
    }

    @Test
    fun fetchCharacterProfileSuccess(): Unit = runBlocking {
        mockEngineHandler = { request ->
            assertThat(request.url.encodedPath).isEqualTo("/api/v1/characters/profile")
            assertThat(request.url.parameters["name"]).isEqualTo("Arthas")
            assertThat(request.url.parameters["realm"]).isEqualTo("Gordunni")
            assertThat(request.url.parameters["region"]).isEqualTo("eu")

            val json = """
                {
                    "name": "Arthas",
                    "realm": "Gordunni",
                    "class": "Death Knight",
                    "active_spec_name": "Blood",
                    "active_spec_role": "tank",
                    "gear": { "item_level_equipped": 630.0 }
                }
            """.trimIndent()

            respond(
                content = json,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val profile = service.fetchCharacterProfile("Arthas", "Gordunni")
        assertThat(profile).isNotNull
        assertThat(profile!!.name).isEqualTo("Arthas")
        assertThat(profile.realm).isEqualTo("Gordunni")
        assertThat(profile.characterClass).isEqualTo("Death Knight")
        assertThat(profile.activeSpecName).isEqualTo("Blood")
        assertThat(profile.gear?.itemLevelEquipped).isEqualTo(630.0)
    }

    @Test
    fun fetchCharacterProfileNotFound(): Unit = runBlocking {
        mockEngineHandler = { _ ->
            respond(
                content = "Not Found",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "text/plain"),
            )
        }

        val profile = service.fetchCharacterProfile("Unknown", "Gordunni")
        assertThat(profile).isNull()
    }

    @Test
    fun fetchGuildRosterSuccess(): Unit = runBlocking {
        mockEngineHandler = { request ->
            assertThat(request.url.encodedPath).isEqualTo("/api/v1/guilds/profile")
            assertThat(request.url.parameters["name"]).isEqualTo("Bloodline")
            assertThat(request.url.parameters["realm"]).isEqualTo("Gordunni")

            val json = """
                {
                    "name": "Bloodline",
                    "realm": "Gordunni",
                    "members": [
                        {
                            "rank": 0,
                            "character": {
                                "name": "Arthas",
                                "realm": "Gordunni",
                                "class": "Death Knight"
                            }
                        }
                    ]
                }
            """.trimIndent()

            respond(
                content = json,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        val roster = service.fetchGuildRoster("Bloodline", "Gordunni")
        assertThat(roster).hasSize(1)
        assertThat(roster[0].character?.name).isEqualTo("Arthas")
    }

    @Test
    fun fetchGuildRosterError(): Unit = runBlocking {
        mockEngineHandler = { _ ->
            respond(
                content = "Server Error",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "text/plain"),
            )
        }

        val roster = service.fetchGuildRoster("Bloodline", "Gordunni")
        assertThat(roster).isEmpty()
    }

    @Test
    fun beansHttpClientUsesCioEngine() {
        val beans = Beans()
        val client = beans.httpClient(objectMapper)
        try {
            assertThat(client.engine.javaClass.name).contains("CIO")
        } finally {
            client.close()
        }
    }
}