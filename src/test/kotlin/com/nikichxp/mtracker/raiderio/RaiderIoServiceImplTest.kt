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
    fun fetchRunDetailsSuccess(): Unit = runBlocking {
        mockEngineHandler = { request ->
            assertThat(request.url.encodedPath).isEqualTo("/api/v1/mythic-plus/run-details")
            assertThat(request.url.parameters["season"]).isEqualTo("season-tww-1")
            assertThat(request.url.parameters["id"]).isEqualTo("22345")

            val json = """
                {
                    "season": "season-tww-1",
                    "keystone_run_id": 22345,
                    "mythic_level": 12,
                    "clear_time_ms": 1650000,
                    "keystone_time_ms": 1980000,
                    "completed_at": "2026-09-02T18:30:00.000Z",
                    "num_chests": 1,
                    "score": 185.2,
                    "dungeon": { "name": "The Stonevault", "short_name": "SV" },
                    "roster": [
                        {
                            "character": {
                                "name": "Arthas",
                                "class": { "name": "Death Knight", "slug": "death-knight" },
                                "spec": { "name": "Blood", "slug": "blood", "role": "tank" },
                                "realm": { "name": "Gordunni", "slug": "gordunni" },
                                "region": { "name": "Europe", "short_name": "EU", "slug": "eu" }
                            },
                            "role": "tank",
                            "guild": { "name": "Bloodline" },
                            "items": { "item_level_equipped": 635 },
                            "ranks": { "score": 2850.5 }
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

        val details = service.fetchRunDetails("season-tww-1", 22345)
        assertThat(details).isNotNull
        assertThat(details!!.keystoneRunId).isEqualTo(22345)
        assertThat(details.dungeon?.name).isEqualTo("The Stonevault")
        assertThat(details.numChests).isEqualTo(1)
        assertThat(details.roster).hasSize(1)
        val member = details.roster[0]
        assertThat(member.character?.name).isEqualTo("Arthas")
        assertThat(member.character?.realm?.name).isEqualTo("Gordunni")
        assertThat(member.character?.characterClass?.name).isEqualTo("Death Knight")
        assertThat(member.character?.spec?.name).isEqualTo("Blood")
        assertThat(member.guild?.name).isEqualTo("Bloodline")
        assertThat(member.items?.itemLevelEquipped).isEqualTo(635.0)
        assertThat(member.ranks?.score).isEqualTo(2850.5)
    }

    @Test
    fun fetchRunDetailsError(): Unit = runBlocking {
        mockEngineHandler = { _ ->
            respond(
                content = "Not Found",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "text/plain"),
            )
        }

        val details = service.fetchRunDetails("season-tww-1", 99999)
        assertThat(details).isNull()
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