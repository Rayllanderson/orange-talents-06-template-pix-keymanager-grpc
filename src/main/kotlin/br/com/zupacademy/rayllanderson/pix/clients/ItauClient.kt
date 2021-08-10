package br.com.zupacademy.rayllanderson.pix.clients

import br.com.zupacademy.rayllanderson.AccountType
import br.com.zupacademy.rayllanderson.pix.responses.ItauClientAccountResponse
import br.com.zupacademy.rayllanderson.pix.responses.ItauClientDetailsResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${erp.itau.client.url}/clientes")
interface ItauClient {

    @Get("/{clientId}/contas")
    fun findByIdAndAccountType(@PathVariable clientId: String, @QueryValue("tipo") accountType: AccountType): HttpResponse<ItauClientAccountResponse>

    @Get("/{clientId}")
    fun findById(@PathVariable clientId: String): HttpResponse<ItauClientDetailsResponse>
}