package br.com.zupacademy.rayllanderson.pix.extensions

import br.com.zupacademy.rayllanderson.PixKeyListRequest
import br.com.zupacademy.rayllanderson.pix.validators.assertThatNotNull

fun PixKeyListRequest?.validate() {
    assertThatNotNull(this?.clientId, "Cliente id")
}
