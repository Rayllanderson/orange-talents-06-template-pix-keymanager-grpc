package br.com.zupacademy.rayllanderson.pix.validators

import br.com.zupacademy.rayllanderson.KeyType
import br.com.zupacademy.rayllanderson.KeyType.*
import java.lang.IllegalArgumentException

/**
 * Verifica se a chave Pix está em um formato válido de acordo com o seu tipo.
 * @throws IllegalArgumentException Caso a chave não esteja em um formato válido
 */
fun assertThatKeyIsFormatted(key: String, keyType: KeyType) {
    when (keyType) {
        CPF -> {
            val isNotValidCpf = !CpfValidator()(key)
            if (isNotValidCpf) throw IllegalArgumentException("Cpf deve estar bem formatado. Por exemplo: 12345678901")
        }
        EMAIL -> {
            val isNotValidEmail = !EmailValidator()(key)
            if (isNotValidEmail) throw IllegalArgumentException("Email deve estar bem formatado.")
        }
        PHONE -> {
            val isNotValidPhone = !PhoneValidator()(key)
            if (isNotValidPhone) throw IllegalArgumentException("Telefone deve estar bem formatado. Por exemplo: +5585988714077")
        }
    }
}

/**
 * @param keyType para verificar se o tipo da chave não é random, que nesse caso, pode ser nula
 * @throws IllegalArgumentException Caso a chave seja blank ou null
 */
fun assertThatKeyIsNotNull(key: String?, keyType: KeyType) {
    val isNotRandomType = keyType != RANDOM
    if (key.isNullOrBlank() && isNotRandomType) throw IllegalArgumentException("Chave pix não pode ser nula ou vazia")
}

/**
 * @throws IllegalArgumentException Caso a chave seja maior que 77
 */
fun checkIfKeyIsGreaterThan77(key: String) {
    if (key.trim().length > 77) throw IllegalArgumentException("Chave pix precisa ser menor ou igual a 77")
}

/**
 * @throws IllegalArgumentException Caso o tipo da chave seja RANDOM e há valor preenchido na key
 */
fun checkIfKeyHasValueIfIsRandom(key: String, keyType: KeyType) {
    val isRandomType = keyType == RANDOM
    if (key.isNotEmpty() && isRandomType) {
        throw IllegalArgumentException("Valor da chave não deve ser preenchido quando o tipo da chave é $RANDOM")
    }
}