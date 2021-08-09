package br.com.zupacademy.rayllanderson.pix.creators.itau

import br.com.zupacademy.rayllanderson.AccountType
import br.com.zupacademy.rayllanderson.pix.responses.ERPItauAccountResponse
import br.com.zupacademy.rayllanderson.pix.responses.ERPItauClientAccountResponse
import br.com.zupacademy.rayllanderson.pix.responses.ERPItauOwnerResponse

fun createItauResponseValid(clientId: String, accountType: AccountType): ERPItauClientAccountResponse {
    return ERPItauClientAccountResponse(
        type = accountType,
        bankAccountResponse = createItauAccountResponseValid(),
        branch = "0001",
        accountNumber = "291900",
        owner = createItauOwnerResponseValid(clientId)
    )
}

fun createItauOwnerResponseValid(clientId: String): ERPItauOwnerResponse {
    return ERPItauOwnerResponse(
        clientId,
        "08018462104",
        "Kaguya Sama"
    )
}

fun createItauAccountResponseValid(): ERPItauAccountResponse {
    return ERPItauAccountResponse(
        "ITAÚ UNIBANCO S.A.",
        "60701190"
    )
}
