package br.com.zupacademy.rayllanderson.pix.model

import br.com.zupacademy.rayllanderson.KeyType
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class PixKey(

    @field:NotBlank
    @Column(nullable = false, unique = true)
    val key: String,

    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val keyType: KeyType,

    @field:NotNull
    @Embedded
    val bankAccount: BankAccount,

    @field:NotNull
    @Embedded
    val owner: Owner,

    @field:NotNull
    @Column(nullable = false)
    val createdAt: LocalDateTime,
) {

    @Id
    @Column(nullable = false, unique = true)
    val pixId: String = UUID.randomUUID().toString()
}