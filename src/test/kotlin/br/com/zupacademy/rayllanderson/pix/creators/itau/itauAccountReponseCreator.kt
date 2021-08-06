package br.com.zupacademy.rayllanderson.pix.creators.itau

import br.com.zupacademy.rayllanderson.pix.responses.ERPItauAccountResponse

fun createItauAccountResponseValid(): ERPItauAccountResponse{
    return ERPItauAccountResponse(
        "ITAÚ UNIBANCO S.A.",
        "60701190"
    )
}