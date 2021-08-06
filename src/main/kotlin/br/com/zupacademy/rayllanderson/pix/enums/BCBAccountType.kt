package br.com.zupacademy.rayllanderson.pix.enums

import br.com.zupacademy.rayllanderson.AccountType

enum class BCBAccountType(
    val modelAccountType: AccountType,
) {
    CACC(AccountType.CONTA_CORRENTE), SVGS(AccountType.CONTA_POUPANCA);

    companion object {
        fun fromAccountType(accountType: AccountType): BCBAccountType {
            return if (accountType == AccountType.CONTA_CORRENTE) CACC else SVGS
        }
    }
}