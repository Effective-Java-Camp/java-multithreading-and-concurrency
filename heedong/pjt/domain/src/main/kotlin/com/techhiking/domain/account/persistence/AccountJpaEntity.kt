package com.techhiking.domain.account.persistence

import com.techhiking.domain.account.domain.AccountStatus
import jakarta.persistence.*
import java.math.BigInteger

@Entity
@Table(name = "account")
class AccountJpaEntity(
    @Column(name = "balance")
    val balance: BigInteger = BigInteger.ZERO,

    @Column(name = "status")
    val status: String = AccountStatus.initialStatusToString(),

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
)