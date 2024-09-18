package com.techhiking.domain.account.domain

import java.math.BigInteger

data class Money(
    val amount: BigInteger = BigInteger.ZERO
) {
    fun of(value: Long): Money {
        return Money(BigInteger.valueOf(value))
    }

    fun isPositiveOrZero(): Boolean {
        return amount >= BigInteger.ZERO
    }

    fun isGreaterThanOrEqualTo(money: Money): Boolean {
        return amount >= money.amount
    }

    fun add(a: Money, b: Money): Money {
        return Money(a.amount.add(b.amount))
    }

    fun subtract(a: Money, b: Money): Money {
        return Money(a.amount.subtract(b.amount))
    }

    operator fun plus(money: Money): Money {
        return Money(amount.add(money.amount))
    }

    operator fun minus(money: Money): Money {
        return Money(amount.subtract(money.amount))
    }
}