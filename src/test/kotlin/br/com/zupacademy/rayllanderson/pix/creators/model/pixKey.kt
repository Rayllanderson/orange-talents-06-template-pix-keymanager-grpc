package br.com.zupacademy.rayllanderson.pix.creators.model

import br.com.zupacademy.rayllanderson.KeyType
import br.com.zupacademy.rayllanderson.pix.model.PixKey
import java.time.LocalDateTime

fun createPixKeyToBeSaved (): PixKey {
    return PixKey(
        key = "kaguya@sama.com",
        keyType = KeyType.EMAIL,
        bankAccount = createBankAccountValid(),
        owner = createOwnerValid(),
        createdAt = LocalDateTime.now()
    )
}

fun createAnotherPixKeyToBeSaved (): PixKey {
    return PixKey(
        key = "hayasaka@sama.com",
        keyType = KeyType.EMAIL,
        bankAccount = createBankAccountValid(),
        owner = createAnotherOwnerValid(),
        createdAt = LocalDateTime.now()
    )
}