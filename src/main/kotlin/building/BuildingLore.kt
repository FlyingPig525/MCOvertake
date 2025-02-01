package io.github.flyingpig525.building

import io.github.flyingpig525.*
import net.bladehunt.kotstom.dsl.item.ItemLore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic

val power: String
    get() = "<red>$POWER_SYMBOL <bold>Power</bold><gray>"
val organicMatter: String
    get() = "<green>$MATTER_SYMBOL <bold>Organic Matter</bold><gray>"
val attack: String
    get() = "<red>$ATTACK_SYMBOL <bold>Attack</bold><gray>"
val disposableResources: String
    get() = "<aqua>$RESOURCE_SYMBOL <bold>Disposable Resource(s)</bold><gray>"
val mechanicalPart: String
    get() = "<white>$MECHANICAL_SYMBOL <bold>Mechanical Part(s)</bold><gray>"

const val oilColor: String = "<#22252A>"

fun ItemLore.amountOwned(count: Int) {
    +"<gray>Amount Owned: <gold><bold>$count".asMini().noItalic()
}
fun ItemLore.resourcesConsumed(amount: Int, count: Int) {
    +"<gray>Consumes $amount $disposableResources <dark_gray>(${count * amount})".asMini().noItalic()
}