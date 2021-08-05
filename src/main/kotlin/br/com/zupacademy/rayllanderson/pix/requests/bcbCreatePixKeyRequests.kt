package br.com.zupacademy.rayllanderson.pix.requests

import br.com.zupacademy.rayllanderson.AccountType
import br.com.zupacademy.rayllanderson.KeyType
import br.com.zupacademy.rayllanderson.PixKeyRequest
import br.com.zupacademy.rayllanderson.pix.responses.ERPItauResponse
import java.util.*

class BCBCreatePixKeyRequest(
    val keyType: KeyType,
    key: String,
    val bankAccount: BCBBankAccountRequest,
    val owner: BCBOwnerRequest,
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
            clientItauResponse: ERPItauResponse,
        ): BCBCreatePixKeyRequest {
            return BCBCreatePixKeyRequest(
                request.keyType,
                request.key,
                BCBBankAccountRequest.fromERPItauResponse(clientItauResponse),
                BCBOwnerRequest.fromERPItauResponse(clientItauResponse)
            )
        }
    }
}

data class BCBBankAccountRequest(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: BCBAccountTypeRequest,
) {
    companion object {
        fun fromERPItauResponse(erpItauResponse: ERPItauResponse): BCBBankAccountRequest {
            return BCBBankAccountRequest(
                erpItauResponse.getAccountIspb(),
                erpItauResponse.branch,
                erpItauResponse.accountNumber,
                BCBAccountTypeRequest.fromAccountType(erpItauResponse.type)
            )
        }
    }
}

data class BCBOwnerRequest(
    val type: BCBOwnerTypeRequest,
    val name: String,
    val taxIdNumber: String,
) {
    companion object {
        fun fromERPItauResponse(erpItauResponse: ERPItauResponse): BCBOwnerRequest {
            return BCBOwnerRequest(BCBOwnerTypeRequest.NATURAL_PERSON,
                erpItauResponse.getOwnerName(),
                erpItauResponse.getOwnerCpf())
        }
    }
}

enum class BCBOwnerTypeRequest {
    NATURAL_PERSON, LEGAL_PERSON
}

enum class BCBAccountTypeRequest {
    CACC, SVGS;

    companion object {
        fun fromAccountType(accountType: AccountType): BCBAccountTypeRequest {
            return if (accountType == AccountType.CONTA_CORRENTE) CACC else SVGS
        }
    }
}

