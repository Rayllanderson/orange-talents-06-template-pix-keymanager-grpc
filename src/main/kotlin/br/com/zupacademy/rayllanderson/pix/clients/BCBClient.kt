package br.com.zupacademy.rayllanderson.pix.clients

import br.com.zupacademy.rayllanderson.pix.requests.BCBCreatePixKeyRequest
import br.com.zupacademy.rayllanderson.pix.requests.BCBDeletePixKeyRequest
import br.com.zupacademy.rayllanderson.pix.responses.BCBCreatePixKeyResponse
import br.com.zupacademy.rayllanderson.pix.responses.BCBDeletePixKeyResponse
import br.com.zupacademy.rayllanderson.pix.responses.BCBPixKeyDetailsResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.client.url}/pix/keys")
interface BCBClient {

    @Post
    @Produces(MediaType.APPLICATION_XML)
    fun registerPixKey(@Body createPixRequest: BCBCreatePixKeyRequest): HttpResponse<BCBCreatePixKeyResponse>

    @Delete("/{key}")
    @Produces(MediaType.APPLICATION_XML)
    fun deletePixKey(@PathVariable key: String, @Body deletePixKeyRequest: BCBDeletePixKeyRequest): HttpResponse<BCBDeletePixKeyResponse>

    @Get("/{key}")
    @Produces(MediaType.APPLICATION_XML)
    fun findPixKeyDetails(@PathVariable key: String): HttpResponse<BCBPixKeyDetailsResponse>
}