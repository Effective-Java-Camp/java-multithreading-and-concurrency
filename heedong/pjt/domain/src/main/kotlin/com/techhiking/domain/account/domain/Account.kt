package com.techhiking.domain.account.domain

data class Account(
    val id: Long? = null,
    val balance: Money = Money(),
    val status: AccountStatus = AccountStatus.ACTIVE,
)