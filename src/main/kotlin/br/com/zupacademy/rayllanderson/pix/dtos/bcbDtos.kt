package br.com.zupacademy.rayllanderson.pix.dtos

import br.com.zupacademy.rayllanderson.PixKeyDetailsResponse
import br.com.zupacademy.rayllanderson.pix.enums.BCBAccountType
import br.com.zupacademy.rayllanderson.pix.enums.BCBOwnerType
import br.com.zupacademy.rayllanderson.pix.model.BankAccount
import br.com.zupacademy.rayllanderson.pix.model.Owner
import br.com.zupacademy.rayllanderson.pix.responses.ERPItauClientAccountResponse

// Objetos válidos tanto para request, quanto para response

data class BCBBankAccountDto(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: BCBAccountType,
) {
    companion object {
        fun fromERPItauResponse(erpItauResponse: ERPItauClientAccountResponse): BCBBankAccountDto {
            return BCBBankAccountDto(
                erpItauResponse.getAccountIspb(),
                erpItauResponse.branch,
                erpItauResponse.accountNumber,
                BCBAccountType.fromAccountType(erpItauResponse.type)
            )
        }
    }

    fun toBankAccount(institutionName: String): BankAccount {
        return BankAccount(
            institutionName,
            this.participant,
            this.branch,
            this.accountNumber,
            this.accountType.modelAccountType,
        )
    }

    fun toPixKeyDetailsAccount(institutionName: String): PixKeyDetailsResponse.Account {
        return PixKeyDetailsResponse.Account.newBuilder()
            .setAccountType(this.accountType.modelAccountType)
            .setBranch(this.branch)
            .setName(institutionName)
            .setNumber(this.accountNumber)
            .build()
    }
}

data class BCBOwnerDto(
    val type: BCBOwnerType,
    val name: String,
    val taxIdNumber: String,
) {
    companion object {
        fun fromERPItauResponse(erpItauResponse: ERPItauClientAccountResponse): BCBOwnerDto {
            return BCBOwnerDto(BCBOwnerType.NATURAL_PERSON,
                erpItauResponse.getOwnerName(),
                erpItauResponse.getOwnerCpf())
        }
    }

    fun toOwner(clientId: String): Owner {
        return Owner(clientId, this.name, this.taxIdNumber)
    }
}