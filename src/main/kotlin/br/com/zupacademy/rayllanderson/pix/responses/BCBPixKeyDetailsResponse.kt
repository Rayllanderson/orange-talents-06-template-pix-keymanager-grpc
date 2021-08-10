package br.com.zupacademy.rayllanderson.pix.responses

import br.com.zupacademy.rayllanderson.KeyType
import br.com.zupacademy.rayllanderson.PixKeyDetailsResponse
import br.com.zupacademy.rayllanderson.pix.dtos.BCBBankAccountDto
import br.com.zupacademy.rayllanderson.pix.dtos.BCBOwnerDto
import br.com.zupacademy.rayllanderson.pix.utils.LocalDateTimeConverter
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId

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
            .setCreatedAt(LocalDateTimeConverter.toProtobufTimestamp(createdAt))
            .build()
    }
}