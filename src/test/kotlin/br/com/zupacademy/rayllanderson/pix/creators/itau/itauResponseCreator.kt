package br.com.zupacademy.rayllanderson.pix.creators.itau

import br.com.zupacademy.rayllanderson.AccountType
import br.com.zupacademy.rayllanderson.pix.responses.ERPItauClientAccountResponse

fun createItauResponseValid(clientId: String, accountType: AccountType): ERPItauClientAccountResponse {
    return ERPItauClientAccountResponse(
        type = accountType,
        bankAccountResponse = createItauAccountResponseValid(),
        branch = "0001",
        accountNumber = "291900",
        owner = createItauOwnerResponseValid(clientId)
    )
}
