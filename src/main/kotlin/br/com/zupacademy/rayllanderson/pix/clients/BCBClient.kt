package br.com.zupacademy.rayllanderson.pix.clients

import br.com.zupacademy.rayllanderson.pix.requests.BCBCreatePixKeyRequest
import br.com.zupacademy.rayllanderson.pix.responses.BCBCreatePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.client.url}/pix/keys")
interface BCBClient {

    @Post
    @Produces(MediaType.APPLICATION_XML)
    fun registerPixKey(@Body createPixRequest: BCBCreatePixKeyRequest): HttpResponse<BCBCreatePixKeyResponse>
}