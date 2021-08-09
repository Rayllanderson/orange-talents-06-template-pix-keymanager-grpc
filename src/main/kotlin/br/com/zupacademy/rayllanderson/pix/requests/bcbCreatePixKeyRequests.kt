package br.com.zupacademy.rayllanderson.pix.requests

import br.com.zupacademy.rayllanderson.KeyType
import br.com.zupacademy.rayllanderson.PixKeyRequest
import br.com.zupacademy.rayllanderson.pix.dtos.BCBBankAccountDto
import br.com.zupacademy.rayllanderson.pix.dtos.BCBOwnerDto
import br.com.zupacademy.rayllanderson.pix.responses.ERPItauClientAccountResponse

class BCBCreatePixKeyRequest(
    val keyType: KeyType,
    val key: String?,
    val bankAccount: BCBBankAccountDto,
    val owner: BCBOwnerDto,
) {

    companion object {
        fun fromPixKeyRequestAndItauClientAccountResponse(
            request: PixKeyRequest,
            clientItauResponse: ERPItauClientAccountResponse,
        ): BCBCreatePixKeyRequest {
            return BCBCreatePixKeyRequest(
                request.keyType,
                request.key,
                BCBBankAccountDto.fromERPItauResponse(clientItauResponse),
                BCBOwnerDto.fromERPItauResponse(clientItauResponse)
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BCBCreatePixKeyRequest

        if (owner != other.owner) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}