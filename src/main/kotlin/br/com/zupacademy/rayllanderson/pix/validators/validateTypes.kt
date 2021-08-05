package br.com.zupacademy.rayllanderson.pix.validators

import br.com.zupacademy.rayllanderson.AccountType
import br.com.zupacademy.rayllanderson.KeyType

/**
 * @throws IllegalArgumentException caso o tipo da chave não seja válido
 */
fun checkIfKeyTypeIsValid(keyType: KeyType){
    if(keyType == KeyType.UNKNOWN) {
        throw IllegalArgumentException("O tipo da chave não está setado ou não é válido.")
    }
}

/**
 * @throws IllegalArgumentException caso o tipo da conta não seja válido
 */
fun checkIfAccountTypeIsValid(accountType: AccountType){
    if(accountType == AccountType.UNKNOWN_ACCOUNT){
        throw IllegalArgumentException("O tipo da conta não está setada ou não é válida.")
    }
}