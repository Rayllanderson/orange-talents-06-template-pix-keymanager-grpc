package br.com.zupacademy.rayllanderson.pix.requests

import br.com.zupacademy.rayllanderson.KeyType
import br.com.zupacademy.rayllanderson.PixKeyRegisterRequest
import br.com.zupacademy.rayllanderson.pix.dtos.BCBBankAccountDto
import br.com.zupacademy.rayllanderson.pix.dtos.BCBOwnerDto
import br.com.zupacademy.rayllanderson.pix.responses.ItauClientAccountResponse

class BCBPixKeyRegisterRequest(
    val keyType: KeyType,
    val key: String?,
    val bankAccount: BCBBankAccountDto,
    val owner: BCBOwnerDto,
) {

    companion object {
        fun new(
            request: PixKeyRegisterRequest,
            clientItauResponse: ItauClientAccountResponse,
        ): BCBPixKeyRegisterRequest {
            return BCBPixKeyRegisterRequest(
                request.keyType,
                request.key,
                BCBBankAccountDto.fromItauClientAccountResponse(clientItauResponse),
                BCBOwnerDto.fromERPItauResponse(clientItauResponse)
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BCBPixKeyRegisterRequest

        if (owner != other.owner) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}