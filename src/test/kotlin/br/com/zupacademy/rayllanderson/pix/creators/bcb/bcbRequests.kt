package br.com.zupacademy.rayllanderson.pix.creators.bcb

import br.com.zupacademy.rayllanderson.KeyType
import br.com.zupacademy.rayllanderson.pix.dtos.BCBBankAccountDto
import br.com.zupacademy.rayllanderson.pix.dtos.BCBOwnerDto
import br.com.zupacademy.rayllanderson.pix.requests.BCBPixKeyRegisterRequest
import br.com.zupacademy.rayllanderson.pix.responses.ItauClientAccountResponse

fun createBcbPixRequestToBeSaved(
    keyType: KeyType,
    key: String,
    clientItauResponse: ItauClientAccountResponse,
): BCBPixKeyRegisterRequest {
    return BCBPixKeyRegisterRequest(
        keyType,
        key,
        BCBBankAccountDto.fromItauClientAccountResponse(clientItauResponse),
        BCBOwnerDto.fromERPItauResponse(clientItauResponse)
    )
}

fun createBcbPixRequestToBeSavedWithKeyRandom(
    keyType: KeyType,
    clientItauResponse: ItauClientAccountResponse,
): BCBPixKeyRegisterRequest {
    return BCBPixKeyRegisterRequest(
        keyType,
        null,
        BCBBankAccountDto.fromItauClientAccountResponse(clientItauResponse),
        BCBOwnerDto.fromERPItauResponse(clientItauResponse)
    )
}