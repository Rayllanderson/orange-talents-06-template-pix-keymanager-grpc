package br.com.zupacademy.rayllanderson.pix.creators.bcb

import br.com.zupacademy.rayllanderson.pix.requests.BCBCreatePixKeyRequest
import br.com.zupacademy.rayllanderson.pix.responses.BCBCreatePixKeyResponse
import java.time.LocalDateTime


fun createBCBPixKeyResponseValid(bcbRequest: BCBCreatePixKeyRequest): BCBCreatePixKeyResponse {
    return BCBCreatePixKeyResponse(
        bcbRequest.keyType,
        bcbRequest.key,
        bcbRequest.bankAccount,
        bcbRequest.owner,
        LocalDateTime.now()
    )
}