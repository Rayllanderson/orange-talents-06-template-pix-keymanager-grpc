package br.com.zupacademy.rayllanderson.pix.creators.bcb

import br.com.zupacademy.rayllanderson.pix.creators.model.createBankAccountValid
import br.com.zupacademy.rayllanderson.pix.creators.model.createPixKeyToBeSaved
import br.com.zupacademy.rayllanderson.pix.dtos.BCBBankAccountDto
import br.com.zupacademy.rayllanderson.pix.dtos.BCBOwnerDto
import br.com.zupacademy.rayllanderson.pix.enums.BCBAccountType
import br.com.zupacademy.rayllanderson.pix.enums.BCBOwnerType

fun createBCBBankAccountDto(): BCBBankAccountDto {
    val bankAccount = createBankAccountValid()
    return BCBBankAccountDto(
        bankAccount.name,
        bankAccount.branch,
        bankAccount.accountNumber,
        BCBAccountType.CACC
    )
}

fun createBCBOwnerDto(): BCBOwnerDto {
    val owner = createPixKeyToBeSaved().owner
    return BCBOwnerDto(
        BCBOwnerType.NATURAL_PERSON,
        owner.name,
        owner.cpf
    )
}