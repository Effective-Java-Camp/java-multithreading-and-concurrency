package com.techhiking.domain.account.service

import com.techhiking.domain.account.port.`in`.ValidateAccountUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ValidateAccountService: ValidateAccountUseCase {
}