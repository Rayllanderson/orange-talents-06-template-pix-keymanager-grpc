package br.com.zupacademy.rayllanderson.pix.clients

import br.com.zupacademy.rayllanderson.AccountType
import br.com.zupacademy.rayllanderson.pix.responses.ERPItauResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${erp.itau.client.url}/clientes")
interface ERPItauClient {

    @Get("/{clientId}/contas")
    fun findById(@PathVariable clientId: String, @QueryValue("tipo") accountType: AccountType): HttpResponse<ERPItauResponse>
}