package br.com.zupacademy.rayllanderson.pix.creators.bcb

import br.com.zupacademy.rayllanderson.KeyType
import br.com.zupacademy.rayllanderson.pix.dtos.BCBBankAccountDto
import br.com.zupacademy.rayllanderson.pix.dtos.BCBOwnerDto
import br.com.zupacademy.rayllanderson.pix.requests.BCBCreatePixKeyRequest
import br.com.zupacademy.rayllanderson.pix.responses.ERPItauClientAccountResponse

fun createBcbPixRequestToBeSaved(
    keyType: KeyType,
    key: String,
    clientItauResponse: ERPItauClientAccountResponse,
): BCBCreatePixKeyRequest {
    return BCBCreatePixKeyRequest(
        keyType,
        key,
        BCBBankAccountDto.fromERPItauResponse(clientItauResponse),
        BCBOwnerDto.fromERPItauResponse(clientItauResponse)
    )
}

fun createBcbPixRequestToBeSavedWithKeyRandom(
    keyType: KeyType,
    clientItauResponse: ERPItauClientAccountResponse,
): BCBCreatePixKeyRequest {
    return BCBCreatePixKeyRequest(
        keyType,
        "",
        BCBBankAccountDto.fromERPItauResponse(clientItauResponse),
        BCBOwnerDto.fromERPItauResponse(clientItauResponse)
    )
}