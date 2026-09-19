package com.nikichxp.mtracker.topgear

import com.nikichxp.mtracker.domain.topgear.StatType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PrimaryStatResolverTest {

    private val resolver = PrimaryStatResolver()

    @Test
    fun `maps specs to primary stats`() {
        assertThat(resolver.primaryFor("Arms", "Warrior")).isEqualTo(StatType.STRENGTH)
        assertThat(resolver.primaryFor("Unholy", "Death Knight")).isEqualTo(StatType.STRENGTH)
        assertThat(resolver.primaryFor("Retribution", "Paladin")).isEqualTo(StatType.STRENGTH)
        assertThat(resolver.primaryFor("Holy", "Paladin")).isEqualTo(StatType.INTELLECT)
        assertThat(resolver.primaryFor("Mistweaver", "Monk")).isEqualTo(StatType.INTELLECT)
        assertThat(resolver.primaryFor("Windwalker", "Monk")).isEqualTo(StatType.AGILITY)
        assertThat(resolver.primaryFor("Enhancement", "Shaman")).isEqualTo(StatType.AGILITY)
        assertThat(resolver.primaryFor("Restoration", "Shaman")).isEqualTo(StatType.INTELLECT)
        assertThat(resolver.primaryFor("Devourer", "Demon Hunter")).isEqualTo(StatType.AGILITY)
        assertThat(resolver.primaryFor("Fire", "Mage")).isEqualTo(StatType.INTELLECT)
        assertThat(resolver.primaryFor("Guardian", "Druid")).isEqualTo(StatType.AGILITY)
        assertThat(resolver.primaryFor("Balance", "Druid")).isEqualTo(StatType.INTELLECT)
    }

    @Test
    fun `hybridFallback maps option sets to deterministic primaries`() {
        assertThat(resolver.hybridFallback(setOf(StatType.AGILITY, StatType.INTELLECT)))
            .isEqualTo(StatType.AGILITY)
        assertThat(resolver.hybridFallback(setOf(StatType.AGILITY, StatType.STRENGTH)))
            .isEqualTo(StatType.AGILITY)
        assertThat(resolver.hybridFallback(setOf(StatType.STRENGTH, StatType.INTELLECT)))
            .isEqualTo(StatType.STRENGTH)
        assertThat(resolver.hybridFallback(setOf(StatType.AGILITY, StatType.STRENGTH, StatType.INTELLECT)))
            .isEqualTo(StatType.INTELLECT)
    }
}
