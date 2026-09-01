package com.nikichxp.mtracker.web

import com.nikichxp.mtracker.domain.TrackedGuild
import com.nikichxp.mtracker.domain.TrackedGuildRepository
import com.nikichxp.mtracker.web.dto.GuildRequest
import com.nikichxp.mtracker.web.dto.TrackedGuildDto
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

/**
 * CRUD for [TrackedGuild] (which guilds' rosters get pulled from Raider.io). Gated by
 * [AdminAuthFilter] - see `MTRACKER_ADMIN_TOKEN`.
 */
@RestController
@RequestMapping("/api/v1/admin/guilds")
class GuildAdminController(private val repository: TrackedGuildRepository) {

    @GetMapping
    fun list(): List<TrackedGuildDto> = repository.findAll().map(::toDto)

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): TrackedGuildDto = findOrThrow(id).let(::toDto)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: GuildRequest): TrackedGuildDto {
        val (name, realm) = validated(request)
        return try {
            toDto(repository.save(TrackedGuild(name = name, realm = realm)))
        } catch (e: DuplicateKeyException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Guild '$name-$realm' is already tracked", e)
        }
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody request: GuildRequest): TrackedGuildDto {
        val existing = findOrThrow(id)
        val (name, realm) = validated(request)
        return try {
            toDto(repository.save(existing.copy(name = name, realm = realm)))
        } catch (e: DuplicateKeyException) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Guild '$name-$realm' is already tracked", e)
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: String) {
        findOrThrow(id)
        repository.deleteById(id)
    }

    private fun findOrThrow(id: String): TrackedGuild =
        repository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown guild: $id") }

    private fun validated(request: GuildRequest): Pair<String, String> {
        val name = request.name.trim()
        val realm = request.realm.trim()
        if (name.isBlank() || realm.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name and realm are required")
        }
        return name to realm
    }

    private fun toDto(guild: TrackedGuild) = TrackedGuildDto(
        id = requireNotNull(guild.id),
        name = guild.name,
        realm = guild.realm,
        addedAt = guild.addedAt,
    )
}
