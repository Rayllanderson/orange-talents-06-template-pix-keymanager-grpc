package br.com.zupacademy.rayllanderson.pix.clients

import br.com.zupacademy.rayllanderson.pix.requests.BCBPixKeyRegisterRequest
import br.com.zupacademy.rayllanderson.pix.requests.BCBPixKeyDeleteRequest
import br.com.zupacademy.rayllanderson.pix.responses.BCBPixKeyRegisterResponse
import br.com.zupacademy.rayllanderson.pix.responses.BCBPixKeyDeleteResponse
import br.com.zupacademy.rayllanderson.pix.responses.BCBPixKeyDetailsResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.client.url}/pix/keys")
interface BCBClient {

    @Post
    @Produces(MediaType.APPLICATION_XML)
    fun registerPixKey(@Body createPixRequest: BCBPixKeyRegisterRequest): HttpResponse<BCBPixKeyRegisterResponse>

    @Delete("/{key}")
    @Produces(MediaType.APPLICATION_XML)
    fun deletePixKey(@PathVariable key: String, @Body deletePixKeyRequest: BCBPixKeyDeleteRequest): HttpResponse<BCBPixKeyDeleteResponse>

    @Get("/{key}")
    @Produces(MediaType.APPLICATION_XML)
    fun findPixKeyDetails(@PathVariable key: String): HttpResponse<BCBPixKeyDetailsResponse>
}