package br.com.zupacademy.rayllanderson.pix.responses

import br.com.zupacademy.rayllanderson.KeyType
import br.com.zupacademy.rayllanderson.pix.dtos.BCBBankAccountDto
import br.com.zupacademy.rayllanderson.pix.dtos.BCBOwnerDto
import br.com.zupacademy.rayllanderson.pix.model.PixKey
import java.time.LocalDateTime

data class BCBPixKeyRegisterResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BCBBankAccountDto,
    val owner: BCBOwnerDto,
    val createdAt: LocalDateTime,
) {
    fun toPixKey(clientId: String, institutionName: String): PixKey {
        return PixKey(
            this.key,
            this.keyType,
            this.bankAccount.toBankAccount(institutionName),
            this.owner.toOwner(clientId),
            this.createdAt
        )
    }
}