package com.nikichxp.mtracker.web

import com.nikichxp.mtracker.domain.topgear.SpecOverviewRow
import com.nikichxp.mtracker.web.dto.SpecOverviewDto
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class SpecOverviewConverter : Converter<SpecOverviewRow, SpecOverviewDto> {
    override fun convert(source: SpecOverviewRow): SpecOverviewDto = SpecOverviewDto(
        specId = source.specId,
        specName = source.specName,
        specSlug = source.specSlug,
        className = source.className,
        role = source.role,
        parseCount = source.parseCount.toInt(),
        characterCount = source.characterCount.toInt(),
        avgItemLevel = source.avgItemLevel,
    )
}
