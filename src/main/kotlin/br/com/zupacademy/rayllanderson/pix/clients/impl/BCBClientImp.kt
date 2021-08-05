package br.com.zupacademy.rayllanderson.pix.clients.impl

import br.com.zupacademy.rayllanderson.core.exceptions.InternalServerErrorException
import br.com.zupacademy.rayllanderson.core.exceptions.PixKeyExistingException
import br.com.zupacademy.rayllanderson.pix.clients.BCBClient
import br.com.zupacademy.rayllanderson.pix.requests.BCBCreatePixKeyRequest
import br.com.zupacademy.rayllanderson.pix.responses.BCBCreatePixKeyResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class BCBClientImp(
    private val client: BCBClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Post
    fun create(request: BCBCreatePixKeyRequest): BCBCreatePixKeyResponse {
        try {
            val response = client.registerPixKey(request)
            return response.body()!!
        } catch (e: HttpClientResponseException) {
            when (e.status) {
                HttpStatus.UNPROCESSABLE_ENTITY -> {
                    logger.warn("Não foi possível cadastrar chave pix, já está cadastrada")
                    throw PixKeyExistingException("Chave pix já está cadastrada")
                }
                else -> {
                    logger.error("Erro criando chave pix ao tentar se comunicar com o serviço do Banco Central." +
                            "status = ${e.status}; mensagem = ${e.message} ")
                    throw InternalServerErrorException("Ocorreu um erro ao tentar criar chave pix. Tente novamente mais tarde")
                }
            }
        }
    }
}