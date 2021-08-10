package br.com.zupacademy.rayllanderson.pix.validators

import br.com.zupacademy.rayllanderson.PixKeyDeleteRequest
import br.com.zupacademy.rayllanderson.pix.extensions.validate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.*

class ValidatePixDeleteRequestTest {

    @Test
    fun `should do nothing when request is valid`() {
        val request = PixKeyDeleteRequest.newBuilder()
            .setClientId(UUID.randomUUID().toString())
            .setPixId(UUID.randomUUID().toString())
            .build()

        assertDoesNotThrow {
            request.validate()
        }
    }

    @Test
    fun `should throw illegal argument exception when client id is not present`() {
        val request = PixKeyDeleteRequest.newBuilder()
            .setPixId(UUID.randomUUID().toString())
            .build()

        val error = assertThrows<IllegalArgumentException> {
            request.validate()
        }

        Assertions.assertEquals("Cliente id não pode ser nulo ou vazio", error.message)
    }

    @Test
    fun `should throw illegal argument exception when pix id is not present`() {
        val request = PixKeyDeleteRequest.newBuilder()
            .setClientId(UUID.randomUUID().toString())
            .build()

        val error = assertThrows<IllegalArgumentException> {
            request.validate()
        }

        Assertions.assertEquals("Pix id não pode ser nulo ou vazio", error.message)
    }
}