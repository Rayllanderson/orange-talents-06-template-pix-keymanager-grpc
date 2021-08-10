package br.com.zupacademy.rayllanderson.pix.endpoints

import br.com.zupacademy.rayllanderson.PixKeyDeleteRequest
import br.com.zupacademy.rayllanderson.PixKeyDeleteResponse
import br.com.zupacademy.rayllanderson.PixKeyDeleteServiceGrpc
import br.com.zupacademy.rayllanderson.core.exceptions.NotFoundException
import br.com.zupacademy.rayllanderson.pix.clients.impl.BCBClientImp
import br.com.zupacademy.rayllanderson.pix.clients.impl.ItauClientImp
import br.com.zupacademy.rayllanderson.pix.extensions.validate
import br.com.zupacademy.rayllanderson.pix.model.PixKey
import br.com.zupacademy.rayllanderson.pix.repository.PixKeyRepository
import br.com.zupacademy.rayllanderson.pix.requests.BCBPixKeyDeleteRequest
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class KeyDeleteEndpoint(
    private val repository: PixKeyRepository,
    private val itauClient: ItauClientImp,
    private val bcbClient: BCBClientImp,
) : PixKeyDeleteServiceGrpc.PixKeyDeleteServiceImplBase() {

    private val logger = LoggerFactory.getLogger(KeyDeleteEndpoint::class.java)

    override fun delete(request: PixKeyDeleteRequest?, responseObserver: StreamObserver<PixKeyDeleteResponse>?) {
        request.validate()

        logger.info("Nova tentativa de deleção de pix de id ${request!!.pixId} para o cliente ${request.clientId}")

        val clientNotExists = !repository.existsByOwnerId(request.clientId)
        if (clientNotExists) {
            logger.info("Tentativa de deleção falhou, client id não existe")
            throw NotFoundException("client id não existe")
        }

        val pixKey: PixKey = repository.findById(request.pixId).orElseThrow {
            logger.info("Tentativa de deleção falhou, pix id não existe na base de dados")
            NotFoundException("pix id não existe")
        }

        val keyNotBelongToClient = !pixKey.belongToThatClient(request.clientId)
        if (keyNotBelongToClient) {
            logger.warn("Tentativa de deleção falhou, pix de id ${request.pixId} não pertence ao cliente ${request.clientId}")
            throw IllegalArgumentException("Pix id ${request.pixId} não pertence ao cliente ${request.clientId}")
        }

        logger.info("Tentando encontrar cliente no sistema Itaú...")
        val itauClientDetailsResponse = itauClient.findById(request.clientId)
        logger.info("Cliente encontrado!")

        logger.info("Tentando deletar chave no sistema do Banco Central...")
        val bcbRequest = BCBPixKeyDeleteRequest(pixKey.key, itauClientDetailsResponse.getAccountIspb())
        bcbClient.deleteKey(bcbRequest)

        repository.deleteById(request.pixId)

        logger.info("Chave ${pixKey.key} deletada com sucesso!")

        responseObserver?.onNext(PixKeyDeleteResponse.newBuilder()
            .setClientId(request.clientId)
            .setPixId(request.pixId)
            .build()
        )
        responseObserver?.onCompleted()
    }
}