package br.com.zupacademy.rayllanderson.pix.requests

import br.com.zupacademy.rayllanderson.KeyType
import br.com.zupacademy.rayllanderson.PixKeyRequest
import br.com.zupacademy.rayllanderson.pix.dtos.BCBBankAccountDto
import br.com.zupacademy.rayllanderson.pix.dtos.BCBOwnerDto
import br.com.zupacademy.rayllanderson.pix.responses.ERPItauClientAccountResponse
import java.util.*

class BCBCreatePixKeyRequest(
    val keyType: KeyType,
    key: String,
    val bankAccount: BCBBankAccountDto,
    val owner: BCBOwnerDto,
) {
    var key = key
        private set

    init {
        if (keyType == KeyType.RANDOM) {
            this.key = generateRandomKey()
        }
    }

    private fun generateRandomKey(): String {
        return UUID.randomUUID().toString()
    }

    companion object {
        fun fromPixKeyRequestAndClientItauResponse(
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