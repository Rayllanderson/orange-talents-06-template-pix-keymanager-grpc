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
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Singleton

@Singleton
class KeyListEndpoint(
    private val repository: PixKeyRepository,
) : FindPixKeyListServiceGrpc.FindPixKeyListServiceImplBase() {

    private val logger = LoggerFactory.getLogger(KeyListEndpoint::class.java)

    override fun find(request: PixKeyListRequest?, responseObserver: StreamObserver<PixKeyListResponse>?) {
        request.validate()

        logger.info("Nova tentativa buscando lista de chave pix para o cliente ${request!!.clientId}")

        val clientNotExists = !repository.existsByOwnerId(request.clientId)
        if (clientNotExists) {
            logger.info("Tentativa de busca falhou, client id não existe")
            throw NotFoundException("cliente não encontrado")
        }

        val clientId = request.clientId
        val keys = repository.findAllByOwnerId(clientId).map {
            PixKeyListResponse.PixKey.newBuilder()
                .setClientId(it.owner.id)
                .setPixId(it.pixId)
                .setKey(it.key)
                .setKeyType(it.keyType)
                .setAccountType(it.bankAccount.bankAccountType)
                .setCreatedAt(it.createdAt.let { createdAt ->
                    createdAt.atZone(ZoneId.of("UTC")).toInstant().let { instant ->
                        Timestamp.newBuilder()
                            .setSeconds(instant.epochSecond)
                            .setNanos(instant.nano)
                            .build()
                    }
                })
                .build()
        }

        logger.info("Busca realizada com sucesso")

        responseObserver?.onNext(PixKeyListResponse.newBuilder().addAllKeys(keys).build())
        responseObserver?.onCompleted()
    }
}