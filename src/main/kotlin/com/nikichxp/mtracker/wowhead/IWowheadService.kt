package com.nikichxp.mtracker.wowhead

interface IWowheadService {
    suspend fun fetchItem(itemId: Int): WowheadItemDto?
    suspend fun fetchItemTooltip(itemId: Int, bonusIds: List<Int>): WowheadTooltipDto?
}
