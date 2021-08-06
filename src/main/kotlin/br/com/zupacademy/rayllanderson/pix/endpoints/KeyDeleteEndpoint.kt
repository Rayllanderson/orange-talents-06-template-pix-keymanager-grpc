package br.com.zupacademy.rayllanderson.pix.endpoints

import br.com.zupacademy.rayllanderson.PixDeleteKeyRequest
import br.com.zupacademy.rayllanderson.PixDeleteKeyResponse
import br.com.zupacademy.rayllanderson.PixDeleteKeyServiceGrpc
import br.com.zupacademy.rayllanderson.pix.clients.impl.BCBClientImp
import br.com.zupacademy.rayllanderson.pix.clients.impl.ERPItauClientImp
import br.com.zupacademy.rayllanderson.pix.extensions.validate
import br.com.zupacademy.rayllanderson.pix.model.PixKey
import br.com.zupacademy.rayllanderson.pix.repository.PixKeyRepository
import br.com.zupacademy.rayllanderson.pix.requests.BCBDeletePixKeyRequest
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class KeyDeleteEndpoint(
    private val repository: PixKeyRepository,
    private val erpItauClient: ERPItauClientImp,
    private val bcbClient: BCBClientImp,
) : PixDeleteKeyServiceGrpc.PixDeleteKeyServiceImplBase() {

    private val logger = LoggerFactory.getLogger(KeyDeleteEndpoint::class.java)

    override fun delete(request: PixDeleteKeyRequest?, responseObserver: StreamObserver<PixDeleteKeyResponse>?) {
        request.validate()

        logger.info("Nova tentativa de deleção de pix de id ${request!!.pixId} para o cliente ${request.clientId}")

        val pixKey: PixKey = repository.findById(request.pixId).orElseThrow {
            logger.info("Tentativa de deleção falhou, pix id não existe na base de dados")
            IllegalArgumentException("pix id não existe")
        }

        val keyNotBelongToClient = !pixKey.belongToThatClient(request.clientId)
        if (keyNotBelongToClient) {
            logger.warn("Tentativa de deleção falhou, pix de id ${request.pixId} não pertence ao cliente ${request.clientId}")
            throw IllegalArgumentException("Pix id ${request.pixId} não pertence ao cliente ${request.clientId}")
        }

        logger.info("Tentando encontrar cliente no sistema Itaú...")
        val itauResponse = erpItauClient.findById(request.clientId)
        logger.info("Cliente encontrado!")

        logger.info("Tentando deletar chave no sistema do Banco Central...")
        val bcbRequest = BCBDeletePixKeyRequest(pixKey.key, itauResponse.getAccountIspb())
        bcbClient.deleteKey(bcbRequest)

        repository.deleteById(request.pixId)

        logger.info("Chave ${pixKey.key} deletada com sucesso!")

        responseObserver?.onNext(PixDeleteKeyResponse.newBuilder()
            .setClientId(request.clientId)
            .setPixId(request.pixId)
            .build()
        )
        responseObserver?.onCompleted()
    }
}