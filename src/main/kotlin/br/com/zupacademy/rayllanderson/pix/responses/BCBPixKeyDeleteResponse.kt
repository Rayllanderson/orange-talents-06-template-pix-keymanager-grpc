package br.com.zupacademy.rayllanderson.pix.responses

import java.time.LocalDateTime

class BCBPixKeyDeleteResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)