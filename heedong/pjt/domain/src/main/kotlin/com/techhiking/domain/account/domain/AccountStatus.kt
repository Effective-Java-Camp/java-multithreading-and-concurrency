package com.techhiking.domain.account.domain

enum class AccountStatus {
    ACTIVE, INACTIVE, SUSPENDED;

    fun isActive(): Boolean {
        return this == ACTIVE
    }

    fun isSuspended(): Boolean {
        return this == SUSPENDED
    }

    companion object {
        fun initialStatusToString(): String {
            return ACTIVE.name
        }
    }
}