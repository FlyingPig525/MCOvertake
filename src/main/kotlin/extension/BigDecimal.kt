package io.github.flyingpig525.extension

import java.math.BigDecimal
import java.math.MathContext


// cant just do Number as the arg because BigDecimal extends number and causes recursion
operator fun BigDecimal.div(other: Long): BigDecimal = this.divide(other.toBigDecimal(this.precision()))
operator fun BigDecimal.plus(other: Long): BigDecimal = this.add(other.toBigDecimal(this.precision()))
operator fun BigDecimal.minus(other: Long): BigDecimal = this.minus(other.toBigDecimal(this.precision()))
operator fun BigDecimal.times(other: Long): BigDecimal = this.times(other.toBigDecimal(this.precision()))
operator fun BigDecimal.div(other: Int): BigDecimal = this.divide(other.toBigDecimal(this.precision()))
operator fun BigDecimal.plus(other: Int): BigDecimal = this.add(other.toBigDecimal(this.precision()))
operator fun BigDecimal.minus(other: Int): BigDecimal = this.minus(other.toBigDecimal(this.precision()))
operator fun BigDecimal.times(other: Int): BigDecimal = this.times(other.toBigDecimal(this.precision()))
operator fun BigDecimal.div(other: Double): BigDecimal = this.divide(other.toBigDecimal(this.precision()))
operator fun BigDecimal.plus(other: Double): BigDecimal = this.add(other.toBigDecimal(this.precision()))
operator fun BigDecimal.minus(other: Double): BigDecimal = this.minus(other.toBigDecimal(this.precision()))
operator fun BigDecimal.times(other: Double): BigDecimal = this.times(other.toBigDecimal(this.precision()))

operator fun BigDecimal.compareTo(other: Number): Int = compareTo(other.toBigDecimal(this.precision()))

fun Number.toBigDecimal(precision: Int = 2) = BigDecimal(toDouble(), MathContext(precision))