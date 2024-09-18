package com.techhiking.domain.account.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface AccountJpaRepository: JpaRepository<AccountJpaEntity, Long> {
}