package io.github.flyingpig525.building

import io.github.flyingpig525.ATTACK_SYMBOL
import io.github.flyingpig525.MATTER_SYMBOL
import io.github.flyingpig525.POWER_SYMBOL
import io.github.flyingpig525.RESOURCE_SYMBOL
import net.bladehunt.kotstom.dsl.item.ItemLore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic

val ItemLore.power: String
    get() = "<red>$POWER_SYMBOL <bold>Power</bold><gray>"
val ItemLore.organicMatter: String
    get() = "<green>$MATTER_SYMBOL <bold>Organic Matter</bold><gray>"
val ItemLore.attack: String
    get() = "<red>$ATTACK_SYMBOL <bold>Attack</bold><gray>"
val ItemLore.disposableResources: String
    get() = "<white>$RESOURCE_SYMBOL <bold>Disposable Resources</bold><gray>"

fun ItemLore.amountOwned(count: Int) {
    +"<dark_gray>Amount Owned: <gold>$count".asMini().noItalic()
}
fun ItemLore.resourcesConsumed(amount: Int) {
    +"<gray>Consumes $amount $disposableResources".asMini().noItalic()
}