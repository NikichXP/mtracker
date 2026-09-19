package com.nikichxp.mtracker.topgear

import com.nikichxp.mtracker.domain.topgear.StatType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

@Service
class PrimaryStatResolver {

    private val log = LoggerFactory.getLogger(javaClass)
    private val warnedSpecs = ConcurrentHashMap.newKeySet<String>()

    fun primaryFor(specName: String?, className: String?): StatType? {
        val classKey = className?.trim()?.lowercase(Locale.ROOT) ?: return null
        val specKey = specName?.trim()?.lowercase(Locale.ROOT) ?: return null
        val allSpecs = classWidePrimaries[classKey]
        if (allSpecs != null) {
            return allSpecs
        }
        val stat = specPrimaries[classKey to specKey]
        if (stat == null) {
            if (warnedSpecs.add("$classKey/$specKey")) {
                log.warn("Unknown spec for primary stat resolution: {} {}", className, specName)
            }
        }
        return stat
    }

    fun hybridFallback(options: Set<StatType>): StatType = when (options) {
        setOf(StatType.AGILITY, StatType.STRENGTH, StatType.INTELLECT) -> StatType.INTELLECT
        setOf(StatType.AGILITY, StatType.STRENGTH) -> StatType.AGILITY
        setOf(StatType.AGILITY, StatType.INTELLECT) -> StatType.AGILITY
        setOf(StatType.STRENGTH, StatType.INTELLECT) -> StatType.STRENGTH
        else -> options.first()
    }

    companion object {
        private val classWidePrimaries = mapOf(
            "hunter" to StatType.AGILITY,
            "rogue" to StatType.AGILITY,
            "mage" to StatType.INTELLECT,
            "priest" to StatType.INTELLECT,
            "warlock" to StatType.INTELLECT,
            "evoker" to StatType.INTELLECT,
        )

        private val specPrimaries = buildMap {
            putAll(
                mapOf(
                    ("warrior" to "arms") to StatType.STRENGTH,
                    ("warrior" to "fury") to StatType.STRENGTH,
                    ("warrior" to "protection") to StatType.STRENGTH,
                    ("paladin" to "protection") to StatType.STRENGTH,
                    ("paladin" to "retribution") to StatType.STRENGTH,
                    ("paladin" to "holy") to StatType.INTELLECT,
                    ("death knight" to "blood") to StatType.STRENGTH,
                    ("death knight" to "frost") to StatType.STRENGTH,
                    ("death knight" to "unholy") to StatType.STRENGTH,
                    ("monk" to "brewmaster") to StatType.AGILITY,
                    ("monk" to "windwalker") to StatType.AGILITY,
                    ("monk" to "mistweaver") to StatType.INTELLECT,
                    ("druid" to "feral") to StatType.AGILITY,
                    ("druid" to "guardian") to StatType.AGILITY,
                    ("druid" to "balance") to StatType.INTELLECT,
                    ("druid" to "restoration") to StatType.INTELLECT,
                    ("shaman" to "enhancement") to StatType.AGILITY,
                    ("shaman" to "elemental") to StatType.INTELLECT,
                    ("shaman" to "restoration") to StatType.INTELLECT,
                    ("demon hunter" to "havoc") to StatType.AGILITY,
                    ("demon hunter" to "vengeance") to StatType.AGILITY,
                    ("demon hunter" to "devourer") to StatType.AGILITY,
                )
            )
        }
    }
}
