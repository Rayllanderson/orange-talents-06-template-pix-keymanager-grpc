package br.com.zupacademy.rayllanderson.pix.responses

import br.com.zupacademy.rayllanderson.AccountType
import com.fasterxml.jackson.annotation.JsonProperty

data class ERPItauResponse(
    @JsonProperty("tipo")
    val type: AccountType,

    @JsonProperty("instituicao")
    val bankAccountResponse: ERPItauAccountResponse,

    @JsonProperty("agencia")
    val branch: String,

    @JsonProperty("numero")
    val accountNumber: String,

    @JsonProperty("titular")
    val owner: ERPItauOwnerResponse,
) {
    fun getAccountName(): String {
        return this.bankAccountResponse.bankAccountName
    }

    fun getAccountIspb(): String {
        return this.bankAccountResponse.bankAccountIspb
    }

    fun getOwnerName(): String {
        return this.owner.name
    }

    fun getOwnerCpf(): String {
        return this.owner.cpf
    }

    fun getOwnerId(): String {
        return this.owner.id
    }
}

data class ERPItauAccountResponse(

    @JsonProperty("nome")
    val bankAccountName: String,

    @JsonProperty("ispb")
    val bankAccountIspb: String,
)

data class ERPItauOwnerResponse(
    val id: String,
    val cpf: String,
    @JsonProperty("nome")
    val name: String,
)