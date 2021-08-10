package br.com.zupacademy.rayllanderson.pix.creators.bcb

import br.com.zupacademy.rayllanderson.pix.requests.BCBPixKeyRegisterRequest
import br.com.zupacademy.rayllanderson.pix.responses.BCBPixKeyRegisterResponse
import java.time.LocalDateTime
import java.util.*


fun createBCBPixKeyResponseValid(bcbRequest: BCBPixKeyRegisterRequest): BCBPixKeyRegisterResponse {
    return BCBPixKeyRegisterResponse(
        bcbRequest.keyType,
        key = bcbRequest.key ?: UUID.randomUUID().toString(),
        bcbRequest.bankAccount,
        bcbRequest.owner,
        LocalDateTime.now()
    )
}