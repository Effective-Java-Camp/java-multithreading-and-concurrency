package com.techhiking.domain.account.persistence

import com.techhiking.domain.account.domain.Account
import com.techhiking.domain.account.domain.AccountStatus
import com.techhiking.domain.account.domain.Money
import org.springframework.stereotype.Component

@Component
class AccountMapper {
    fun toDomain(accountJpaEntity: AccountJpaEntity): Account {
        return Account(
            id = accountJpaEntity.id,
            balance = Money(accountJpaEntity.balance),
            status = AccountStatus.valueOf(accountJpaEntity.status)
        )
    }

    fun toEntity(account: Account): AccountJpaEntity {
        return AccountJpaEntity(
            id = account.id,
            balance = account.balance.amount,
            status = account.status.name
        )
    }
}