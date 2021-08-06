package br.com.zupacademy.rayllanderson.pix.creators.itau
import br.com.zupacademy.rayllanderson.pix.responses.ERPItauOwnerResponse

fun createItauOwnerResponseValid(clientId: String): ERPItauOwnerResponse {
    return ERPItauOwnerResponse(
        clientId,
        "08018462104",
        "Kaguya Sama"
    )
}