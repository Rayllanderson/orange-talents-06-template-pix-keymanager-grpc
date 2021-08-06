package br.com.zupacademy.rayllanderson.pix.creators.model

import br.com.zupacademy.rayllanderson.pix.model.Owner
import java.util.*

fun createOwnerValid(): Owner {
    return Owner(
        UUID.randomUUID().toString(),
        "Kaguya Sama",
        "08062171618",
    )
}