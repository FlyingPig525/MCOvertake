package io.github.flyingpig525.data.research.upgrade

import io.github.flyingpig525.data.player.permission.Permission
import io.github.flyingpig525.data.research.action.ActionData
import io.github.flyingpig525.data.research.currency.BasicResearch
import io.github.flyingpig525.data.research.currency.ResearchCurrency
import io.github.flyingpig525.permissionManager
import kotlinx.serialization.Serializable
import net.bladehunt.kotstom.dsl.item.item
import net.bladehunt.kotstom.dsl.item.itemName
import net.bladehunt.kotstom.dsl.item.lore
import net.bladehunt.kotstom.extension.adventure.asMini
import net.bladehunt.kotstom.extension.adventure.noItalic
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.utils.time.Cooldown
import java.time.Duration

@Serializable
class TestUpgrade : ResearchUpgrade() {

    override val name: String = "Test"
    override var maxLevel: Int = 1
    override val cost: Long
        get() = 600

    override fun item(currency: ResearchCurrency): ItemStack = item(Material.GOLDEN_HOE) {
        if ((currency as BasicResearch).noOp) return ItemStack.AIR
        itemName = "<gold><bold>Literally the best upgrade <gray>-<aqua> Level: $level/$maxLevel".asMini()
        lore {
            +"<dark_gray>Removes claim cooldown and cost".asMini().noItalic()
            +"<gold>Cost: $cost".asMini().noItalic()
        }
    }

    override fun onClaimLand(eventData: ActionData.ClaimLand): ActionData.ClaimLand? {
        if (level == 0) return null
        if (eventData.player != null && !permissionManager.hasPermission(eventData.player, Permission("data.research.op"))) return null
//        log("Claim Land")
        return eventData.apply {
            claimCooldown = Cooldown(Duration.ofMillis(100))
            claimCost = 0
        }
    }

    override fun onAttacked(eventData: ActionData.Attacked): ActionData.Attacked? {
        if (level == 0) return null
        if (eventData.player != null && !permissionManager.hasPermission(eventData.player, Permission("data.research.op"))) return null
//        log("Attacked")
        eventData.playerData.power += 12
        return eventData
    }

    override fun onBuildWall(eventData: ActionData.BuildWall): ActionData.BuildWall? {
        if (level == 0) return null
        if (eventData.player != null && !permissionManager.hasPermission(eventData.player, Permission("data.research.op"))) return null
//        log("Build Wall")
        eventData.cost = 0
        eventData.cooldown = Cooldown(Duration.ofMillis(100))
        return eventData
    }

    override fun onDestroyBuilding(eventData: ActionData.DestroyBuilding): ActionData.DestroyBuilding? {
        if (level == 0) return null
        if (eventData.player != null && !permissionManager.hasPermission(eventData.player, Permission("data.research.op"))) return null
//        log("Destroy Building")
        eventData.playerData.organicMatter += eventData.wallLevel * 5
        return eventData
    }

    override fun onPlaceColony(eventData: ActionData.PlaceColony): ActionData.PlaceColony? {
        if (level == 0) return null
        if (eventData.player != null && !permissionManager.hasPermission(eventData.player, Permission("data.research.op"))) return null
//        log("Place Colony")
        eventData.cost = 0
        eventData.cooldown = Cooldown(Duration.ofMillis(100))
        return eventData
    }

    override fun onPlaceRaft(eventData: ActionData.PlaceRaft): ActionData.PlaceRaft? {
        if (level == 0) return null
        if (eventData.player != null && !permissionManager.hasPermission(eventData.player, Permission("data.research.op"))) return null
//        log("Place Raft")
        eventData.cost = 0
        eventData.cooldown = Cooldown(Duration.ofMillis(100))
        return eventData
    }

    override fun onAttack(eventData: ActionData.Attack): ActionData.Attack? {
        if (level == 0) return null
        if (eventData.player != null && !permissionManager.hasPermission(eventData.player, Permission("data.research.op"))) return null
//        log("Post Attack")
        eventData.attackCost = 0
        eventData.attackCooldown = Cooldown(Duration.ofMillis(10))
        return eventData
    }

    override fun onAttackCostCalculation(eventData: ActionData.AttackCostCalculation): ActionData.AttackCostCalculation? {
        if (level == 0) return null
        if (eventData.player != null && !permissionManager.hasPermission(eventData.player, Permission("data.research.op"))) return null
//        log("Pre Attack")
        eventData.wallLevel = 0
        return eventData
    }

    override fun onUpgradeWall(eventData: ActionData.UpgradeWall): ActionData.UpgradeWall? {
        if (level == 0) return null
        if (eventData.player != null && !permissionManager.hasPermission(eventData.player, Permission("data.research.op"))) return null
//        log("Upgrade Wall")
        eventData.cost = 0
        eventData.cooldown = Cooldown(Duration.ofMillis(50))
        eventData.playerData.organicMatter += 1000000
        return eventData
    }
}