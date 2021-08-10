package br.com.zupacademy.rayllanderson.pix.endpoints

import br.com.zupacademy.rayllanderson.PixKeyFindListServiceGrpc
import br.com.zupacademy.rayllanderson.PixKeyListRequest
import br.com.zupacademy.rayllanderson.PixKeyListResponse
import br.com.zupacademy.rayllanderson.pix.extensions.validate
import br.com.zupacademy.rayllanderson.pix.repository.PixKeyRepository
import br.com.zupacademy.rayllanderson.pix.utils.LocalDateTimeConverter
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton

@Singleton
class KeyListEndpoint(
    private val repository: PixKeyRepository,
) : PixKeyFindListServiceGrpc.PixKeyFindListServiceImplBase() {

    private val logger = LoggerFactory.getLogger(KeyListEndpoint::class.java)

    override fun find(request: PixKeyListRequest?, responseObserver: StreamObserver<PixKeyListResponse>?) {
        request.validate()

        logger.info("Nova tentativa buscando lista de chave pix para o cliente ${request!!.clientId}")

        val clientId = request.clientId
        val keys = repository.findAllByOwnerId(clientId).map {
            PixKeyListResponse.PixKey.newBuilder()
                .setClientId(it.owner.id)
                .setPixId(it.pixId)
                .setKey(it.key)
                .setKeyType(it.keyType)
                .setAccountType(it.bankAccount.bankAccountType)
                .setCreatedAt(LocalDateTimeConverter.toProtobufTimestamp(it.createdAt))
                .build()
        }

        logger.info("Busca realizada com sucesso")

        responseObserver?.onNext(PixKeyListResponse.newBuilder().addAllKeys(keys).build())
        responseObserver?.onCompleted()
    }
}