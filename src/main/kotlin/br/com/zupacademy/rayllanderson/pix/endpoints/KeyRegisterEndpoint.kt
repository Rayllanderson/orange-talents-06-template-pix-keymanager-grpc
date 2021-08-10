package br.com.zupacademy.rayllanderson.pix.endpoints

import br.com.zupacademy.rayllanderson.PixKeyRegisterRequest
import br.com.zupacademy.rayllanderson.PixKeyRegisterResponse
import br.com.zupacademy.rayllanderson.PixKeyRegisterServiceGrpc
import br.com.zupacademy.rayllanderson.core.exceptions.PixKeyExistingException
import br.com.zupacademy.rayllanderson.pix.clients.impl.BCBClientImp
import br.com.zupacademy.rayllanderson.pix.clients.impl.ItauClientImp
import br.com.zupacademy.rayllanderson.pix.extensions.validate
import br.com.zupacademy.rayllanderson.pix.repository.PixKeyRepository
import br.com.zupacademy.rayllanderson.pix.requests.BCBPixKeyRegisterRequest
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class KeyRegisterEndpoint(
    private val repository: PixKeyRepository,
    private val itauClient: ItauClientImp,
    private val bcbClient: BCBClientImp,
) : PixKeyRegisterServiceGrpc.PixKeyRegisterServiceImplBase() {

    private val logger = LoggerFactory.getLogger(KeyRegisterEndpoint::class.java)

    override fun register(request: PixKeyRegisterRequest?, responseObserver: StreamObserver<PixKeyRegisterResponse>?) {
        request.validate()

        logger.info("Nova tentativa de criação de pix para o cliente ${request?.clientId}")

        val keyAlreadyExists = repository.existsByKey(request!!.key)
        if (keyAlreadyExists) {
            logger.warn("Erro na tentativa de criação da chave. Chave já cadastrada")
            throw PixKeyExistingException("Chave já está cadastrada")
        }

        logger.info("Tentando buscar cliente no sistema erp itaú...")

        val clientItauResponse = itauClient.findByIdAndAccountType(request.clientId, request.accountType)

        logger.info("Cliente encontrado!")

        logger.info("Tentando criar chave pix no sistema do banco central...")

        val bcbRequest = BCBPixKeyRegisterRequest.new(request, clientItauResponse)

        val bcbResponse = bcbClient.createKey(bcbRequest)

        val pixKey = bcbResponse.toPixKey(
            clientId = clientItauResponse.getOwnerId(),
            institutionName = clientItauResponse.getAccountName()
        )

        repository.save(pixKey)

        logger.info("Chave pix criada com sucesso para o cliente ${request.clientId}! Pix id ${pixKey.pixId}")

        responseObserver?.onNext(PixKeyRegisterResponse.newBuilder().setPixId(pixKey.pixId).build())
        responseObserver?.onCompleted()
    }
}