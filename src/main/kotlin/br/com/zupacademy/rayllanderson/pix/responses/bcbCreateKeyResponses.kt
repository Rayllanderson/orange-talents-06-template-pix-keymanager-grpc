package br.com.zupacademy.rayllanderson.pix.responses

import br.com.zupacademy.rayllanderson.AccountType
import br.com.zupacademy.rayllanderson.KeyType
import br.com.zupacademy.rayllanderson.pix.model.BankAccount
import br.com.zupacademy.rayllanderson.pix.model.Owner
import br.com.zupacademy.rayllanderson.pix.model.PixKey
import java.time.LocalDateTime

data class BCBCreatePixKeyResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BCBBankAccountResponse,
    val owner: BCBOwnerResponse,
    val createdAt: LocalDateTime,
) {
    fun toPixKey(clientId: String): PixKey {
        return PixKey(
            this.key,
            this.keyType,
            this.bankAccount.toBankAccount(),
            this.owner.toOwner(clientId),
            this.createdAt
        )
    }
}

data class BCBBankAccountResponse(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: BCBAccountTypeResponse,
) {
    fun toBankAccount(): BankAccount {
        return BankAccount(
            this.participant,
            this.branch,
            this.accountNumber,
            this.accountType.modelAccountType,
        )
    }
}

data class BCBOwnerResponse(
    val type: String,
    val name: String,
    val taxIdNumber: String,
) {
    fun toOwner(clientId: String): Owner {
        return Owner(clientId, this.name, this.taxIdNumber)
    }
}

enum class BCBAccountTypeResponse(
    val modelAccountType: AccountType,
) {
    CACC(AccountType.CONTA_CORRENTE), SVGS(AccountType.CONTA_POUPANCA)
}