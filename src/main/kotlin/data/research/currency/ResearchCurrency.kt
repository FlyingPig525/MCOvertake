package io.github.flyingpig525.data.research.currency

import io.github.flyingpig525.data.research.upgrade.ResearchUpgrade
import net.minestom.server.item.Material

interface ResearchCurrency : List<ResearchUpgrade> {
    val symbol: String
    val color: String
    val currencyLevel: Int
    val upgrades: List<ResearchUpgrade>
    val colorItem: Material
    var internalLevel: Int
    var count: Long

    fun upgradeByName(name: String) = upgrades.find { it.name == name }

    override fun iterator(): Iterator<ResearchUpgrade> = upgrades.iterator()
    override val size: Int
        get() = upgrades.size

    override fun contains(element: ResearchUpgrade): Boolean = upgrades.contains(element)
    override fun get(index: Int): ResearchUpgrade = upgrades[index]
    override fun containsAll(elements: Collection<ResearchUpgrade>): Boolean = upgrades.containsAll(elements)
    override fun indexOf(element: ResearchUpgrade): Int = upgrades.indexOf(element)
    override fun isEmpty(): Boolean = upgrades.isEmpty()
    override fun lastIndexOf(element: ResearchUpgrade): Int = upgrades.lastIndexOf(element)
    override fun listIterator(index: Int): ListIterator<ResearchUpgrade> = upgrades.listIterator(index)
    override fun listIterator(): ListIterator<ResearchUpgrade> = upgrades.listIterator()
    override fun subList(fromIndex: Int, toIndex: Int): List<ResearchUpgrade> = upgrades.subList(fromIndex, toIndex)

    companion object {
        val currencies = mutableSetOf<ResearchCurrency>()
    }
}