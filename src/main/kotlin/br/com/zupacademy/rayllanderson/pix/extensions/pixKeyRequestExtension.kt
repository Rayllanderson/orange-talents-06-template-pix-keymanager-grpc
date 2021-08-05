package br.com.zupacademy.rayllanderson.pix.extensions

import br.com.zupacademy.rayllanderson.PixKeyRequest
import br.com.zupacademy.rayllanderson.pix.validators.*

fun PixKeyRequest?.validate() {
    checkIfKeyIsNull(this?.key, this!!.keyType)
    checkIfKeyIsGreaterThan77(this.key)
    checkIfKeyHasValueIfIsRandom(this.key, this.keyType)

    checkIfKeyTypeIsValid(this.keyType)
    checkIfAccountTypeIsValid(this.accountType)

    checkIfKeyIsFormatted(this.key, this.keyType)
}
