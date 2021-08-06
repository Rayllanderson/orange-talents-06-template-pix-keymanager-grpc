package br.com.zupacademy.rayllanderson.pix.responses

import br.com.zupacademy.rayllanderson.KeyType
import br.com.zupacademy.rayllanderson.PixKeyDetailsResponse
import br.com.zupacademy.rayllanderson.pix.dtos.BCBBankAccountDto
import br.com.zupacademy.rayllanderson.pix.dtos.BCBOwnerDto
import java.time.LocalDateTime

data class BCBPixKeyDetailsResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BCBBankAccountDto,
    val owner: BCBOwnerDto,
    val createdAt: LocalDateTime
){
    fun toPixKeyDetailsResponse(bankAccountName: String, clientId: String, pixId: String): PixKeyDetailsResponse {
        return PixKeyDetailsResponse.newBuilder()
            .setAccount(this.bankAccount.toPixKeyDetailsAccount(bankAccountName))
            .setClientId(clientId)
            .setPixId(pixId)
            .setKey(this.key)
            .setKeyType(this.keyType)
            .setOwnerName(this.owner.name)
            .setOwnerCpf(this.owner.taxIdNumber)
            .build()
    }
}