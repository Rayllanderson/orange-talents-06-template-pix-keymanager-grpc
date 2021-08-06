package br.com.zupacademy.rayllanderson.pix.creators.model

import br.com.zupacademy.rayllanderson.AccountType
import br.com.zupacademy.rayllanderson.pix.model.BankAccount

fun createBankAccountValid(): BankAccount {
    return BankAccount(
        "ITAÚ UNIBANCO S.A.",
        "60701190",
        "607011545490",
        AccountType.CONTA_CORRENTE
    )
}