package io.github.flyingpig525.ksp

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStream

class ItemProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val itemSymbols = resolver.getSymbolsWithAnnotation("io.github.flyingpig525.ksp.Item")
            .filterIsInstance<KSClassDeclaration>()
        logger.info("symbols")

        if (!itemSymbols.iterator().hasNext()) return emptyList()
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray()),
            packageName = "io.github.flyingpig525.ksp",
            fileName = "ItemInitFunctions"
        )
        file += "package io.github.flyingpig525.ksp\n\n"
        file += "fun initItems() {\n"
        logger.info("file start")
        itemSymbols.forEach { it.accept(ItemVisitor(file, logger), Unit) }
        file += "}"
        file.close()
        val unableToProcess = itemSymbols.filterNot { it.validate() }.toList()
        return unableToProcess
    }

}
operator fun OutputStream.plusAssign(str: String) {
    this.write(str.toByteArray())
}

class ItemVisitor(private val file: OutputStream, private val logger: KSPLogger) : KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        logger.info(classDeclaration.simpleName.asString())
        if (classDeclaration.classKind != ClassKind.OBJECT) {
            logger.error("Only objects can be annotated with @Item", classDeclaration)
            return
        }
        logger.info("is obj")
        if (!classDeclaration.getAllSuperTypes().any { it.declaration.simpleName.getShortName() == "Actionable" }) {
            logger.error("Objects annotated with @Item must implement io.github.flyingpig525.item.Actionable")
        }
        logger.info("is actionable")
        val annotation: KSAnnotation = classDeclaration.annotations.first {
            it.shortName.asString() == "Item"
        }
        logger.info("got annotation")
        val persistentArgument: KSValueArgument = annotation.arguments
            .first { arg -> arg.name?.asString() == "persistent" }
        logger.info("got persistence arg")
        val persistence = persistentArgument.value as Boolean
        logger.info("got persistence")

        file += "\t${classDeclaration.qualifiedName?.asString()}.apply {\n"
        logger.info("wrote qualified name")
        file += "\t\tio.github.flyingpig525.item.Actionable.registry.add(this)\n"
        if (persistence) {
            file += "\t\tio.github.flyingpig525.item.Actionable.persistentRegistry.add(this)\n"
        }
        file += "\t}\n"
        file += "\tcz.lukynka.prettylog.log(\"${classDeclaration.simpleName.getShortName()} initialized...\")\n"
        logger.info("wrote log")
    }
}

class ItemProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ItemProcessor(
            environment.codeGenerator,
            environment.logger,
            environment.options
        )
    }
}