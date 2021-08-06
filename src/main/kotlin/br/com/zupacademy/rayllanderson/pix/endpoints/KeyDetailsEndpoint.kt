package br.com.zupacademy.rayllanderson.pix.endpoints

import br.com.zupacademy.rayllanderson.*
import br.com.zupacademy.rayllanderson.core.exceptions.NotFoundException
import br.com.zupacademy.rayllanderson.core.exceptions.PixKeyExistingException
import br.com.zupacademy.rayllanderson.pix.clients.impl.ERPItauClientImp
import br.com.zupacademy.rayllanderson.pix.clients.impl.BCBClientImp
import br.com.zupacademy.rayllanderson.pix.extensions.validate
import br.com.zupacademy.rayllanderson.pix.model.PixKey
import br.com.zupacademy.rayllanderson.pix.repository.PixKeyRepository
import br.com.zupacademy.rayllanderson.pix.requests.BCBCreatePixKeyRequest
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class KeyDetailsEndpoint(
    private val repository: PixKeyRepository,
    private val bcbClient: BCBClientImp,
) : FindPixKeyDetailsServiceGrpc.FindPixKeyDetailsServiceImplBase() {

    private val logger = LoggerFactory.getLogger(KeyDetailsEndpoint::class.java)

    override fun find(request: PixKeyDetailsRequest?, responseObserver: StreamObserver<PixKeyDetailsResponse>?) {
        request.validate()

        logger.info("Nova tentativa buscando detalhes da chave pix para o cliente ${request!!.clientId}")

        val clientNotExists = !repository.existsByOwnerId(request.clientId)
        if (clientNotExists) {
            logger.info("Tentativa de busca falhou, client id não existe")
            throw NotFoundException("client id não existe")
        }

        val pixKey: PixKey = repository.findById(request.pixId).orElseThrow {
            logger.info("Tentativa de busca falhou, pix id não existe na base de dados")
            NotFoundException("pix id não existe")
        }

        val keyNotBelongToClient = !pixKey.belongToThatClient(request.clientId)
        if (keyNotBelongToClient) {
            logger.warn("Tentativa de busca falhou, pix de id ${request.pixId} não pertence ao cliente ${request.clientId}")
            throw IllegalArgumentException("Pix id ${request.pixId} não pertence ao cliente ${request.clientId}")
        }

        logger.info("Buscando detalhes da chave pix no banco central...")
        val bcbResponse = bcbClient.findPixKeyDetails(pixKey.key)

        responseObserver?.onNext(bcbResponse.toPixKeyDetailsResponse(
            pixKey.bankAccount.name,
            request.clientId,
            request.pixId
        ))
        responseObserver?.onCompleted()
    }
}