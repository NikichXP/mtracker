package com.nikichxp.mtracker.web

import com.nikichxp.mtracker.domain.topgear.SpecOverviewRow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SpecOverviewConverterTest {

    private val converter = SpecOverviewConverter()

    @Test
    fun `converts row to dto with all fields mapped`() {
        val row = SpecOverviewRow(
            specId = 269,
            specName = "Windwalker",
            specSlug = "windwalker",
            className = "Monk",
            role = "dps",
            parseCount = 42L,
            characterCount = 17L,
            avgItemLevel = 252.5,
        )

        val dto = converter.convert(row)

        assertThat(dto.specId).isEqualTo(269)
        assertThat(dto.specName).isEqualTo("Windwalker")
        assertThat(dto.specSlug).isEqualTo("windwalker")
        assertThat(dto.className).isEqualTo("Monk")
        assertThat(dto.role).isEqualTo("dps")
        assertThat(dto.parseCount).isEqualTo(42)
        assertThat(dto.characterCount).isEqualTo(17)
        assertThat(dto.avgItemLevel).isEqualTo(252.5)
    }

    @Test
    fun `converts null average item level`() {
        val row = SpecOverviewRow(
            specId = 251,
            specName = "Frost",
            specSlug = "frost",
            className = "Death Knight",
            role = "dps",
            parseCount = 1L,
            characterCount = 1L,
            avgItemLevel = null,
        )

        assertThat(converter.convert(row).avgItemLevel).isNull()
    }
}
