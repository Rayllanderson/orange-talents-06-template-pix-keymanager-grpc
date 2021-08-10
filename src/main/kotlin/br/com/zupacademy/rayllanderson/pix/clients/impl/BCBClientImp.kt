package br.com.zupacademy.rayllanderson.pix.clients.impl

import br.com.zupacademy.rayllanderson.core.exceptions.ForbiddenException
import br.com.zupacademy.rayllanderson.core.exceptions.InternalServerErrorException
import br.com.zupacademy.rayllanderson.core.exceptions.NotFoundException
import br.com.zupacademy.rayllanderson.core.exceptions.PixKeyExistingException
import br.com.zupacademy.rayllanderson.pix.clients.BCBClient
import br.com.zupacademy.rayllanderson.pix.requests.BCBPixKeyRegisterRequest
import br.com.zupacademy.rayllanderson.pix.requests.BCBPixKeyDeleteRequest
import br.com.zupacademy.rayllanderson.pix.responses.BCBPixKeyRegisterResponse
import br.com.zupacademy.rayllanderson.pix.responses.BCBPixKeyDeleteResponse
import br.com.zupacademy.rayllanderson.pix.responses.BCBPixKeyDetailsResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class BCBClientImp(
    private val client: BCBClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun createKey(request: BCBPixKeyRegisterRequest): BCBPixKeyRegisterResponse {
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

    fun deleteKey(request: BCBPixKeyDeleteRequest): BCBPixKeyDeleteResponse {
        try {
            val response = client.deletePixKey(request.key, request)

            if (response.status == HttpStatus.NOT_FOUND) {
                logger.error("Não foi possível deletar chave pix no banco central. Chave pix não encontrada")
                throw NotFoundException("Chave pix não encontrada")
            }

            return response.body()!!
        } catch (e: HttpClientResponseException) {
            when (e.status) {
                HttpStatus.FORBIDDEN -> {
                    logger.error("Não foi possível deletar chave pix no banco central. Proibido realizar operação")
                    throw ForbiddenException("Proibido realizar operação")
                }
                else -> {
                    logger.error("Erro deletando chave pix ao tentar se comunicar com o serviço do Banco Central." +
                            "status = ${e.status}; mensagem = ${e.message} ")
                    throw InternalServerErrorException("Ocorreu um erro. Tente novamente mais tarde")
                }
            }
        }
    }

    fun findPixKeyDetails(key: String): BCBPixKeyDetailsResponse {
        try {
            val response = client.findPixKeyDetails(key)

            if (response.status == HttpStatus.NOT_FOUND) {
                logger.error("Não foi possível buscar chave pix no banco central. Chave pix não encontrada")
                throw NotFoundException("Chave pix não encontrada")
            }

            return response.body()!!
        } catch (e: HttpClientResponseException) {
            logger.error("Erro buscando chave pix ao tentar se comunicar com o serviço do Banco Central." +
                    "status = ${e.status}; mensagem = ${e.message} ")
            throw InternalServerErrorException("Ocorreu um erro ao buscar chave pix. Tente novamente mais tarde")
        }
    }
}
