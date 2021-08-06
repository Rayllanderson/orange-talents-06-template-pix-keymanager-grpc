package br.com.zupacademy.rayllanderson.pix.extensions

import br.com.zupacademy.rayllanderson.PixDeleteKeyRequest
import br.com.zupacademy.rayllanderson.pix.validators.assertThatNotNull

fun PixDeleteKeyRequest?.validate() {
    assertThatNotNull(this?.clientId, "Cliente id")
    assertThatNotNull(this?.pixId, "Pix id")
}
