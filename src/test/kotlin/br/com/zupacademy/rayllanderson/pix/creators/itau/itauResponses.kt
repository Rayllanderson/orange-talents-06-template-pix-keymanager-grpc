package br.com.zupacademy.rayllanderson.pix.creators.itau

import br.com.zupacademy.rayllanderson.AccountType
import br.com.zupacademy.rayllanderson.pix.responses.ItauAccountResponse
import br.com.zupacademy.rayllanderson.pix.responses.ItauClientAccountResponse
import br.com.zupacademy.rayllanderson.pix.responses.ItauOwnerResponse

fun createItauResponseValid(clientId: String, accountType: AccountType): ItauClientAccountResponse {
    return ItauClientAccountResponse(
        type = accountType,
        bankAccountResponse = createItauAccountResponseValid(),
        branch = "0001",
        accountNumber = "291900",
        owner = createItauOwnerResponseValid(clientId)
    )
}

fun createItauOwnerResponseValid(clientId: String): ItauOwnerResponse {
    return ItauOwnerResponse(
        clientId,
        "08018462104",
        "Kaguya Sama"
    )
}

fun createItauAccountResponseValid(): ItauAccountResponse {
    return ItauAccountResponse(
        "ITAÚ UNIBANCO S.A.",
        "60701190"
    )
}
