package br.com.zupacademy.rayllanderson.pix.extensions

import br.com.zupacademy.rayllanderson.PixKeyDetailsRequest
import br.com.zupacademy.rayllanderson.pix.validators.assertThatNotNull

fun PixKeyDetailsRequest?.validate() {
    assertThatNotNull(this?.clientId, "Cliente id")
    assertThatNotNull(this?.pixId, "Pix id")
}