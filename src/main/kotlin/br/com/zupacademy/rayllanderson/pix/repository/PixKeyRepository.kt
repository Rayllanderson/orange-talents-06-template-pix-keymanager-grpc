package br.com.zupacademy.rayllanderson.pix.repository

import br.com.zupacademy.rayllanderson.pix.model.PixKey
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface PixKeyRepository: JpaRepository<PixKey, String> {
    fun existsByKey(key: String): Boolean
}