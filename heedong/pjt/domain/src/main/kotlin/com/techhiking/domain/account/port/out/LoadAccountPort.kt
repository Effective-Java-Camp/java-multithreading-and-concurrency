package com.techhiking.domain.account.port.out

import com.techhiking.domain.account.domain.Account

interface LoadAccountPort {
    fun loadAccount(accountId: Long): Account
}