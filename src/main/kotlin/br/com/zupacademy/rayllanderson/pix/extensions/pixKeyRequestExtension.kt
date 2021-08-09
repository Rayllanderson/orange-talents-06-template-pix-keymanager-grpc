package br.com.zupacademy.rayllanderson.pix.extensions

import br.com.zupacademy.rayllanderson.PixKeyRequest
import br.com.zupacademy.rayllanderson.pix.validators.*
import java.lang.IllegalArgumentException

fun PixKeyRequest?.validate() {

    if(this?.clientId.isNullOrBlank()) throw IllegalArgumentException("Cliente id não pode ser nulo")

    assertThatKeyIsNotNull(this?.key, this!!.keyType)
    checkIfKeyIsGreaterThan77(this.key)
    checkIfKeyHasValueIfIsRandom(this.key, this.keyType)

    checkIfKeyTypeIsValid(this.keyType)
    checkIfAccountTypeIsValid(this.accountType)

    assertThatKeyIsFormatted(this.key, this.keyType)
}
