package com.techhiking.domain.account.persistence

import com.techhiking.domain.account.domain.Account
import com.techhiking.domain.account.port.out.LoadAccountPort
import org.springframework.stereotype.Component

@Component
class AccountPersistenceAdapter(
    private val accountJpaRepository: AccountJpaRepository,
    private val accountMapper: AccountMapper,
): LoadAccountPort {
    override fun loadAccount(accountId: Long): Account {
        accountJpaRepository.findById(accountId)
            .orElseThrow { throw NoSuchElementException("Account not found") }
            .let { accountJpaEntity ->
                return accountMapper.toDomain(accountJpaEntity)
            }
    }
}