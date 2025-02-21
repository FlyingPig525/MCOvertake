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
val oil: String
    get() = "$oilColor$OIL_SYMBOL <bold>Oil</bold><gray>"
val plastic: String
    get() = "$plasticColor$PLASTIC_SYMBOL <bold>Plastic</bold><gray>"
val lubricant: String
    get() = "$lubricantColor$LUBRICANT_SYMBOL <bold>Lubricant</bold><gray>"

const val oilColor: String = "<#515151>"
const val plasticColor: String = "<#fff7a3>"
const val lubricantColor: String = "<#0e8700>"

fun ItemLore.amountOwned(count: Int) {
    +"<gray>Amount Owned: <gold><bold>$count".asMini().noItalic()
}
fun ItemLore.resourcesConsumed(amount: Int, count: Int) {
    +"<gray>Consumes $amount $disposableResources <dark_gray>(${count * amount})".asMini().noItalic()
}