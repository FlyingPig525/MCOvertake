package io.github.flyingpig525.ksp

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class BuildingCompanion(val orderAfter: String, val category: KClass<*>, val propertyName: String = "")
