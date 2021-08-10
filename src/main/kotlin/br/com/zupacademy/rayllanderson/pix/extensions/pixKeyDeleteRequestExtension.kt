package br.com.zupacademy.rayllanderson.pix.extensions

import br.com.zupacademy.rayllanderson.PixKeyDeleteRequest
import br.com.zupacademy.rayllanderson.pix.validators.assertThatNotNull

fun PixKeyDeleteRequest?.validate() {
    assertThatNotNull(this?.clientId, "Cliente id")
    assertThatNotNull(this?.pixId, "Pix id")
}
