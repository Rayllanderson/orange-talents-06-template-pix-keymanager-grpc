package br.com.zupacademy.rayllanderson.pix.model

import br.com.zupacademy.rayllanderson.AccountType
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Embeddable
class BankAccount(

    @field:NotBlank
    @Column(nullable = false)
    val participant: String,

    @field:NotBlank
    @Column(nullable = false)
    val branch: String,

    @field:NotBlank
    @Column(nullable = false)
    val accountNumber: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    val bankAccountType: AccountType
)