package br.com.zupacademy.rayllanderson.pix.clients.impl

import br.com.zupacademy.rayllanderson.AccountType
import br.com.zupacademy.rayllanderson.core.exceptions.InternalServerErrorException
import br.com.zupacademy.rayllanderson.core.exceptions.NotFoundException
import br.com.zupacademy.rayllanderson.pix.clients.ItauClient
import br.com.zupacademy.rayllanderson.pix.responses.ItauClientAccountResponse
import br.com.zupacademy.rayllanderson.pix.responses.ItauClientDetailsResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class ItauClientImp(
    private val itauClient: ItauClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun findByIdAndAccountType(clientId: String, accountType: AccountType): ItauClientAccountResponse {
        try {
            val response = itauClient.findByIdAndAccountType(clientId, accountType)

            // HTTP Not Found (404) responses for blocking return types is not considered an error
            // condition and the client exception will not be thrown
            if (response.status == HttpStatus.NOT_FOUND) {
                logger.warn("Não foi possível cadastrar pix. Cliente não encontrado.")
                throw NotFoundException("Cliente não encontrado")
            }

            return response.body()!!
        } catch (e: HttpClientResponseException) {
            logger.error("Ocorreu um erro ao tentar procurar cliente pelo id no sistema externo do ERP Itau. " +
                    "Status = ${e.status}; mensagem = ${e.message}")
            throw InternalServerErrorException("Ocorreu um erro ao cadastrar pix. Tente mais tarde")
        }
    }

    fun findById(clientId: String): ItauClientDetailsResponse {
        try {
            val response = itauClient.findById(clientId)

            if (response.status == HttpStatus.NOT_FOUND) {
                logger.warn("Não foi possível excluir pix. Cliente não encontrado.")
                throw NotFoundException("Cliente não encontrado")
            }

            return response.body()!!
        } catch (e: HttpClientResponseException) {
            logger.error("Ocorreu um erro ao tentar procurar cliente pelo id no sistema externo do ERP Itau. " +
                    "Status = ${e.status}; mensagem = ${e.message}")
            throw InternalServerErrorException("Ocorreu um erro. Tente mais tarde")
        }
    }
}