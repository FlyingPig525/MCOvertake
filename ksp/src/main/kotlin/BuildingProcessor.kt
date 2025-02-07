package io.github.flyingpig525.ksp

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStream

class BuildingProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {
    /**
     * Category to (order to declaration)
     */
    private val initialized: MutableMap<String, MutableList<Pair<String, KSClassDeclaration>>> = mutableMapOf()
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) return emptyList()
        val itemSymbols = resolver.getSymbolsWithAnnotation("io.github.flyingpig525.ksp.BuildingCompanion")
            .filterIsInstance<KSClassDeclaration>()
        logger.info("symbols")

        if (!itemSymbols.iterator().hasNext()) return emptyList()
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray()),
            packageName = "io.github.flyingpig525.ksp",
            fileName = "BuildingInitFunction"
        )
        file += "package io.github.flyingpig525.ksp\n\n"
        file += "import io.github.flyingpig525.building.category.*\n"
        file += "import io.github.flyingpig525.building.*\n\n"
        file += "fun initBuildingCompanions() {\n"
        itemSymbols.forEach {
            it.accept(BuildingVisitor(file, logger, initialized), Unit)
        }
//        var i = 0
//        var last: Pair<KSClassDeclaration, Pair<String, String>> = initialized.filter { it.value.first == "first" }.entries.first().toPair()
//        file += "\t${last.first.cutName}.menuSlot = 0\n"
        for ((category, list) in initialized) {
            var i = 0
            var last = list.first { it.first == "first" }
            file += "\t$category.buildings += ${last.second.cutName}\n"
            while (list.isNotEmpty()) try {
                i++
                val it = list.first { it.first + "Companion" == last.second.simpleName.getShortName()}
                file += "\t$category.buildings += ${it.second.cutName}\n"
                last = it
                list.remove(it)
            } catch (e: Exception) {
                break
            }
        }
//        while (initialized.toList().isNotEmpty()) try {
//            i++
//            val it = initialized.filter { it.value.first + "Companion" == last.first.simpleName.getShortName() }.entries.first().toPair()
//            file += "\t${it.first.cutName}.menuSlot = $i\n"
//            last = it
//            initialized.remove(last.first)
//        } catch (e: Exception) {
//            break
//        }

        file += "}"
        file.close()
        createBuildingContainerClass(itemSymbols.toSet(), codeGenerator, resolver)
        invoked = true
        val unableToProcess = itemSymbols.filterNot { it.validate() }.toList()
        return unableToProcess
    }

}

fun createBuildingContainerClass(buildings: Set<KSClassDeclaration>, codeGenerator: CodeGenerator, resolver: Resolver) {
    val file = codeGenerator.createNewFile(
        dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray()),
        packageName = "io.github.flyingpig525.ksp",
        fileName = "PlayerBuildings"
    )
    file += "package io.github.flyingpig525.ksp\n\n"
    file += "import io.github.flyingpig525.building.*\n"
    file += "import kotlinx.serialization.Serializable\n\n"
    file += "@Serializable\n"
    file += "class PlayerBuildings {\n"
    for (building in buildings) {
        val annotation: KSAnnotation = building.annotations.first {
            it.shortName.asString() == "BuildingCompanion"
        }
        val propertyNameArg: KSValueArgument = annotation.arguments
            .first { arg -> arg.name?.asString() == "propertyName" }
        val propertyName = propertyNameArg.value as String
        val lowerName: String = if (propertyName != "") propertyName
            else building.simpleName.getShortName().let {
                it[0].lowercase() + it.drop(1).replace("Companion", "s")
            }
        file += "\tval $lowerName = ${building.simpleName.getShortName().replace("Companion", "")}()\n"
    }
    file += "}"
    file.close()
}

val KSClassDeclaration.cutName: String?
    get() = qualifiedName?.asString()?.replace("io.github.flyingpig525.building.", "")

class BuildingVisitor(
    private val file: OutputStream,
    private val logger: KSPLogger,
    private val initialized: MutableMap<String, MutableList<Pair<String, KSClassDeclaration>>>
) : KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        if (classDeclaration.classKind != ClassKind.OBJECT || !classDeclaration.isCompanionObject) {
            logger.error("Only companion objects can be annotated with @BuildingCompanion")
            return
        }
        if (!classDeclaration.getAllSuperTypes().any { it.declaration.simpleName.getShortName() == "BuildingCompanion" }) {
            logger.error(
                "Companion objects annotated with @BuildingCompanion must extend io.github.flyingpig525.building.Building.BuildingCompanion"
            )
            return
        }
        if (classDeclaration.simpleName.getShortName() == "Companion") {
            logger.error("Companion objects annotated with @BuildingCompanion must be named")
            return
        }
        file += "\tBuilding.BuildingCompanion.registry.add(${classDeclaration.qualifiedName?.asString()?.replace("io.github.flyingpig525.building.", "")})\n"
        file += "\tcz.lukynka.prettylog.log(\"${classDeclaration.simpleName.asString()} initialized...\")\n"
        val annotation: KSAnnotation = classDeclaration.annotations.first {
            logger.info(it.shortName.asString())
            it.shortName.asString() == "BuildingCompanion"
        }
        val orderAfterArg: KSValueArgument = annotation.arguments
            .first { arg -> arg.name?.asString() == "orderAfter" }
        val orderAfter = orderAfterArg.value as String
        val categoryArg = annotation.arguments
            .first { arg -> arg.name?.asString() == "category" }
        val category = (categoryArg.value as KSType).declaration.simpleName.getShortName()
        if (initialized[category] == null) {
            initialized[category] = mutableListOf()
        }
        initialized[category]!! += orderAfter to classDeclaration
    }
}

class BuildingProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return BuildingProcessor(
            environment.codeGenerator,
            environment.logger,
            environment.options
        )
    }
}