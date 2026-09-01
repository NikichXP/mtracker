package com.nikichxp.mtracker.web

import com.nikichxp.mtracker.domain.TrackedPlayer
import com.nikichxp.mtracker.domain.TrackedPlayerRepository
import com.nikichxp.mtracker.web.dto.TrackedPlayerDto
import com.nikichxp.mtracker.web.dto.TrackedPlayerRequest
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

@RestController
@RequestMapping("/api/v1/admin/tracked-players")
class TrackedPlayerAdminController(private val repository: TrackedPlayerRepository) {

    @GetMapping
    fun list(): List<TrackedPlayerDto> = repository.findAll().map(::toDto)

    @GetMapping("/{id}")
    fun get(@PathVariable id: String): TrackedPlayerDto = findOrThrow(id).let(::toDto)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: TrackedPlayerRequest): TrackedPlayerDto {
        val (displayName, characterKeys) = validated(request)
        return toDto(
            repository.save(
                TrackedPlayer(displayName = displayName, characterKeys = characterKeys, isFriend = request.isFriend),
            ),
        )
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody request: TrackedPlayerRequest): TrackedPlayerDto {
        val existing = findOrThrow(id)
        val (displayName, characterKeys) = validated(request)
        return toDto(
            repository.save(
                existing.copy(displayName = displayName, characterKeys = characterKeys, isFriend = request.isFriend),
            ),
        )
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: String) {
        findOrThrow(id)
        repository.deleteById(id)
    }

    private fun findOrThrow(id: String): TrackedPlayer =
        repository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown tracked player: $id") }

    private fun validated(request: TrackedPlayerRequest): Pair<String, List<String>> {
        val displayName = request.displayName.trim()
        val characterKeys = request.characterKeys.map { it.trim() }.filter { it.isNotBlank() }
        if (displayName.isBlank() || characterKeys.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "displayName and at least one characterKey are required")
        }
        return displayName to characterKeys
    }

    private fun toDto(player: TrackedPlayer) = TrackedPlayerDto(
        id = requireNotNull(player.id),
        displayName = player.displayName,
        characterKeys = player.characterKeys,
        isFriend = player.isFriend,
        addedAt = player.addedAt,
    )
}
