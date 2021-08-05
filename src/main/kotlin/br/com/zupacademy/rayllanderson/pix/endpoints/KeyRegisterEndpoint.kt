package br.com.zupacademy.rayllanderson.pix.endpoints

import br.com.zupacademy.rayllanderson.PixKeyRegisterServiceGrpc
import br.com.zupacademy.rayllanderson.PixKeyRequest
import br.com.zupacademy.rayllanderson.PixKeyResponse
import br.com.zupacademy.rayllanderson.core.exceptions.PixKeyExistingException
import br.com.zupacademy.rayllanderson.pix.clients.impl.ERPItauClientImp
import br.com.zupacademy.rayllanderson.pix.clients.impl.BCBClientImp
import br.com.zupacademy.rayllanderson.pix.extensions.validate
import br.com.zupacademy.rayllanderson.pix.repository.PixKeyRepository
import br.com.zupacademy.rayllanderson.pix.requests.BCBCreatePixKeyRequest
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class KeyRegisterEndpoint(
    private val repository: PixKeyRepository,
    private val erpItauClient: ERPItauClientImp,
    private val bcbClient: BCBClientImp,
) : PixKeyRegisterServiceGrpc.PixKeyRegisterServiceImplBase() {

    private val logger = LoggerFactory.getLogger(KeyRegisterEndpoint::class.java)

    override fun register(request: PixKeyRequest?, responseObserver: StreamObserver<PixKeyResponse>?) {
        request.validate()

        logger.info("Nova tentativa de criação de pix para o cliente ${request?.clientId}")

        val keyAlreadyExists = repository.existsByKey(request!!.key)
        if (keyAlreadyExists) {
            logger.warn("Erro na tentativa de criação da chave. Chave já cadastrada")
            throw PixKeyExistingException("Chave já está cadastrada")
        }

        logger.info("Tentando buscar cliente no sistema erp itaú...")

        val clientItauResponse = erpItauClient.findByIdAndAccountType(request.clientId, request.accountType)

        logger.info("Cliente encontrado!")

        logger.info("Tentando criar chave pix no sistema do banco central...")

        val bcbRequest = BCBCreatePixKeyRequest.fromPixKeyRequestAndClientItauResponse(request, clientItauResponse)

        val bcbResponse = bcbClient.create(bcbRequest)

        val pixKey = bcbResponse.toPixKey(clientId = clientItauResponse.getOwnerId())

        repository.save(pixKey)

        logger.info("Chave pix criada com sucesso para o cliente ${request.clientId}! Id do pix ${pixKey.pixId}")

        responseObserver?.onNext(PixKeyResponse.newBuilder().setPixId(pixKey.pixId).build())
        responseObserver?.onCompleted()
    }
}