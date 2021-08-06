package br.com.zupacademy.rayllanderson.pix.validators

import br.com.zupacademy.rayllanderson.KeyType
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ValidateKeyTest {

    @Nested
    inner class CPF {
        @Test
        fun `should do nothing when key is well formatted`() {
            val key = "01060464535"
            checkIfKeyIsFormatted(key, KeyType.CPF)
        }

        @Test
        fun `should throw illegal argument exception when key contains letter`() {
            val key = "0106046453a"
            assertThrows<IllegalArgumentException> {
                checkIfKeyIsFormatted(key, KeyType.CPF)
            }
        }

        @Test
        fun `should throw illegal argument exception when key is not well formatted`() {
            val key = "0106046451243"
            assertThrows<IllegalArgumentException> {
                checkIfKeyIsFormatted(key, KeyType.CPF)
            }
        }
    }

    @Nested
    inner class EMAIL {
        @Test
        fun `should do nothing when key is well formatted`() {
            val key = "kaguya@sama.com"
            checkIfKeyIsFormatted(key, KeyType.EMAIL)
        }

        @Test
        fun `should throw illegal argument exception when key is empty`() {
            val key = ""

            assertThrows<IllegalArgumentException> {
                checkIfKeyIsFormatted(key, KeyType.EMAIL)
            }
        }

        @Test
        fun `should throw illegal argument exception when key is not well formatted`() {
            val key = "kaguya-sama.com"

            assertThrows<IllegalArgumentException> {
                checkIfKeyIsFormatted(key, KeyType.EMAIL)
            }
        }
    }

    @Nested
    inner class PHONE {
        @Test
        fun `should do nothing when key is well formatted`() {
            val key = "+5598993635483"
            checkIfKeyIsFormatted(key, KeyType.PHONE)
        }

        @Test
        fun `should throw illegal argument exception when key is not well formatted`() {
            val key = "98993624963"

            assertThrows<IllegalArgumentException> {
                checkIfKeyIsFormatted(key, KeyType.EMAIL)
            }
        }

        @Test
        fun `should throw illegal argument exception when key contains letter`() {
            val key = "+559899363548A"
            assertThrows<IllegalArgumentException> {
                checkIfKeyIsFormatted(key, KeyType.CPF)
            }
        }
    }

    @Nested
    inner class RANDOM {
        @Test
        fun `should do nothing when key is not present and key type is random`() {
            val key = ""
            checkIfKeyHasValueIfIsRandom(key, KeyType.RANDOM)
        }

        @Test
        fun `should throw illegal argument exception when key present and key type is random`() {
            val key = "kaguya@sama.com"

            assertThrows<IllegalArgumentException> {
                checkIfKeyHasValueIfIsRandom(key, KeyType.RANDOM)
            }
        }
    }

    @Nested
    inner class GERAL {

        @Test
        fun `should do nothing when key is less than 77 characters`() {
            val key = "kaguya@sama.com"
            checkIfKeyIsGreaterThan77(key)
        }

        @Test
        fun `should throw illegal argument exception when key is greater than 77 characters`() {
            val key = "1".repeat(78)

            assertThrows<IllegalArgumentException> {
                checkIfKeyIsGreaterThan77(key)
            }
        }

        @Test
        fun `should do nothing when key is not empty`() {
            val key = "kaguya@sama.com"
            checkIfKeyIsNull(key, KeyType.EMAIL)
        }

        @Test
        fun `should do nothing when key is empty and key type is random`() {
            val key = ""
            checkIfKeyIsNull(key, KeyType.RANDOM)
        }

        @Test
        fun `should throw illegal argument exception when key is empty`() {
            val key = ""

            assertThrows<IllegalArgumentException> {
                checkIfKeyIsNull(key, KeyType.PHONE)
            }
        }
    }
}