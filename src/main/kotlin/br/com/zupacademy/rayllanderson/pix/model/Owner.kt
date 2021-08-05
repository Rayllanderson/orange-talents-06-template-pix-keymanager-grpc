package br.com.zupacademy.rayllanderson.pix.model

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Embeddable
class Owner(

    @field:NotBlank
    @Column(nullable = false)
    val id: String,

    @field:NotBlank
    @Column(nullable = false)
    val name: String,

    @field:NotBlank
    @Column(nullable = false)
    val cpf: String
)