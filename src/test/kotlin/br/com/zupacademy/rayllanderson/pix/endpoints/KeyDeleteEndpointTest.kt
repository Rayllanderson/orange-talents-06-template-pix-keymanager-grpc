package br.com.zupacademy.rayllanderson.pix.endpoints

import br.com.zupacademy.rayllanderson.PixDeleteKeyRequest
import br.com.zupacademy.rayllanderson.PixDeleteKeyServiceGrpc
import br.com.zupacademy.rayllanderson.pix.clients.BCBClient
import br.com.zupacademy.rayllanderson.pix.clients.ERPItauClient
import br.com.zupacademy.rayllanderson.pix.creators.itau.createItauAccountResponseValid
import br.com.zupacademy.rayllanderson.pix.creators.model.createAnotherPixKeyToBeSaved
import br.com.zupacademy.rayllanderson.pix.creators.model.createPixKeyToBeSaved
import br.com.zupacademy.rayllanderson.pix.model.PixKey
import br.com.zupacademy.rayllanderson.pix.repository.PixKeyRepository
import br.com.zupacademy.rayllanderson.pix.requests.BCBDeletePixKeyRequest
import br.com.zupacademy.rayllanderson.pix.responses.BCBDeletePixKeyResponse
import br.com.zupacademy.rayllanderson.pix.responses.ERPItauClientResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.Assert.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito
import org.mockito.Mockito
import java.time.LocalDateTime
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyDeleteEndpointTest(
    val repository: PixKeyRepository,
    val grpcClient: PixDeleteKeyServiceGrpc.PixDeleteKeyServiceBlockingStub,
    val itauClient: ERPItauClient,
    val bcbClient: BCBClient,
) {

    @BeforeEach
    fun setup(){
        repository.deleteAll()
    }

    @Test
    fun `should delete pix key`() {
        // salvar
        val pixSaved = repository.save(createPixKeyToBeSaved())

        //montar a request
        val request = PixDeleteKeyRequest.newBuilder()
            .setClientId(pixSaved.owner.id)
            .setPixId(pixSaved.pixId)
            .build()

        //mockando itaú
        mockItauClientToValidDeleteRequest(pixSaved)

        //mockando banco central do brasil
        mockBcbClientToValidDeleteRequest(pixSaved)

        val response = grpcClient.delete(request)

        with(response) {
            assertNotNull(this)
            assertEquals(pixSaved.pixId, this.pixId)
            assertEquals(pixSaved.owner.id, this.clientId)
            assertFalse(repository.existsById(this.pixId))
        }
    }

    @Test
    fun `shouldn't delete pix key when client id does not exist`() {
        // salvar
        val pixSaved = repository.save(createPixKeyToBeSaved())

        //montar a request
        val request = PixDeleteKeyRequest.newBuilder()
            .setClientId("cliente-id-nao-existente")
            .setPixId(pixSaved.pixId)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.delete(request)
        }

        with(error) {
            Assertions.assertEquals(Status.NOT_FOUND.code, this.status.code)
            Assertions.assertEquals("client id não existe", status.description)
            assertTrue(repository.existsById(pixSaved.pixId))
        }
    }

    @Test
    fun `shouldn't delete pix key when client id is not present`() {
        // salvar
        val pixSaved = repository.save(createPixKeyToBeSaved())

        //montar a request
        val request = PixDeleteKeyRequest.newBuilder()
            .setPixId(pixSaved.pixId)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.delete(request)
        }

        with(error) {
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            Assertions.assertEquals("Cliente id não pode ser nulo ou vazio", status.description)
            assertTrue(repository.existsById(pixSaved.pixId))
        }
    }

    @Test
    fun `shouldn't delete pix key when pix id is not present`() {
        // salvar
        val pixSaved = repository.save(createPixKeyToBeSaved())

        //montar a request
        val request = PixDeleteKeyRequest.newBuilder()
            .setClientId(pixSaved.owner.id)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.delete(request)
        }

        with(error) {
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            Assertions.assertEquals("Pix id não pode ser nulo ou vazio", status.description)
            assertTrue(repository.existsById(pixSaved.pixId))
        }
    }

    @Test
    fun `shouldn't delete pix key when pix id does not exist`() {
        // salvar
        val pixSaved = repository.save(createPixKeyToBeSaved())

        //montar a request
        val request = PixDeleteKeyRequest.newBuilder()
            .setClientId(pixSaved.owner.id)
            .setPixId("key-que-nao-existe")
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.delete(request)
        }

        with(error) {
            Assertions.assertEquals(Status.NOT_FOUND.code, this.status.code)
            Assertions.assertEquals("pix id não existe", status.description)
            assertTrue(repository.existsById(pixSaved.pixId))
        }
    }

    @Test
    fun `shouldn't delete pix key when pix id does not belong to client`() {
        // salvando chave para o cliente 1
        val kaguyaKey = repository.save(createPixKeyToBeSaved())

        // salvando chave para o cliente 2
        val hayasakaKey = repository.save(createAnotherPixKeyToBeSaved())

        //montar a request
        val request = PixDeleteKeyRequest.newBuilder()
            .setClientId(kaguyaKey.owner.id)
            .setPixId(hayasakaKey.pixId)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.delete(request)
        }

        with(error) {
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
            Assertions.assertEquals("Pix id ${request.pixId} não pertence ao cliente ${request.clientId}",
                status.description)
            assertTrue(repository.existsById(hayasakaKey.pixId))
        }
    }

    @Test
    fun `shouldn't delete pix key when client does not exist in Itau Client`() {
        // salvando no nosso sistema, porém não existe no cliente itaú
        val pixSaved = repository.save(createPixKeyToBeSaved())

        //montar a request
        val request = PixDeleteKeyRequest.newBuilder()
            .setClientId(pixSaved.owner.id)
            .setPixId(pixSaved.pixId)
            .build()

        BDDMockito.`when`(itauClient.findById(pixSaved.owner.id)).thenReturn(
            HttpResponse.notFound()
        )

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.delete(request)
        }

        with(error) {
            Assertions.assertEquals(Status.NOT_FOUND.code, this.status.code)
            Assertions.assertEquals("Cliente não encontrado", status.description)
            assertTrue(repository.existsById(pixSaved.pixId))
        }
    }

    @Test
    fun `shouldn't delete pix key when Itau Client is down`() {
        // salvando no nosso sistema
        val pixSaved = repository.save(createPixKeyToBeSaved())

        //montar a request
        val request = PixDeleteKeyRequest.newBuilder()
            .setClientId(pixSaved.owner.id)
            .setPixId(pixSaved.pixId)
            .build()

        val httpResponse: HttpResponse<Any> = HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
        BDDMockito.`when`(itauClient.findById(pixSaved.owner.id))
            .thenThrow(HttpClientResponseException("erro", httpResponse))


        val error = assertThrows<StatusRuntimeException> {
            grpcClient.delete(request)
        }

        with(error) {
            Assertions.assertEquals(Status.INTERNAL.code, this.status.code)
            Assertions.assertEquals("Ocorreu um erro. Tente mais tarde", status.description)
            assertTrue(repository.existsById(pixSaved.pixId))
        }
    }

    @Test
    fun `shouldn't delete pix key when key does not exist in BCB Client`() {
        // salvando no nosso sistema, porém não existe no cliente do banco central
        val pixSaved = repository.save(createPixKeyToBeSaved())

        //montar a request
        val request = PixDeleteKeyRequest.newBuilder()
            .setClientId(pixSaved.owner.id)
            .setPixId(pixSaved.pixId)
            .build()


        //mockando pra passar de boas no cliente itaú
        mockItauClientToValidDeleteRequest(pixSaved)

        //motando request válida
        val key = pixSaved.key
        val bcbRequest = BCBDeletePixKeyRequest(
            key,
            createItauAccountResponseValid().bankAccountIspb
        )

        BDDMockito.`when`(bcbClient.deletePixKey(key, bcbRequest)).thenReturn(
            HttpResponse.notFound()
        )

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.delete(request)
        }

        with(error) {
            Assertions.assertEquals(Status.NOT_FOUND.code, this.status.code)
            Assertions.assertEquals("Chave pix não encontrada", status.description)
            assertTrue(repository.existsById(pixSaved.pixId))
        }
    }

    @Test
    fun `shouldn't delete pix key when BCB Client is down`() {
        // salvando no nosso sistema, porém não existe no cliente do banco central
        val pixSaved = repository.save(createPixKeyToBeSaved())

        //montar a request
        val request = PixDeleteKeyRequest.newBuilder()
            .setClientId(pixSaved.owner.id)
            .setPixId(pixSaved.pixId)
            .build()


        //mockando pra passar de boas no cliente itaú
        mockItauClientToValidDeleteRequest(pixSaved)

        //motando request válida
        val key = pixSaved.key
        val bcbRequest = BCBDeletePixKeyRequest(
            key,
            createItauAccountResponseValid().bankAccountIspb
        )

        val httpResponse: HttpResponse<Any> = HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
        BDDMockito.`when`(bcbClient.deletePixKey(key, bcbRequest))
            .thenThrow(HttpClientResponseException("erro", httpResponse))


        val error = assertThrows<StatusRuntimeException> {
            grpcClient.delete(request)
        }

        with(error) {
            Assertions.assertEquals(Status.INTERNAL.code, this.status.code)
            Assertions.assertEquals("Ocorreu um erro. Tente novamente mais tarde", status.description)
            assertTrue(repository.existsById(pixSaved.pixId))
        }
    }

    fun mockItauClientToValidDeleteRequest(pixSaved: PixKey) {
        val clientId = pixSaved.owner.id
        BDDMockito.`when`(itauClient.findById(clientId))
            .thenReturn(HttpResponse.ok(ERPItauClientResponse(
                clientId,
                pixSaved.owner.name,
                pixSaved.owner.cpf,
                createItauAccountResponseValid()
            )))
    }

    fun mockBcbClientToValidDeleteRequest(pixSaved: PixKey) {
        val key = pixSaved.key
        val participant = createItauAccountResponseValid().bankAccountIspb
        BDDMockito.`when`(bcbClient.deletePixKey(pixSaved.key, BCBDeletePixKeyRequest(
            key,
            participant
        ))).thenReturn(HttpResponse.ok(
            BCBDeletePixKeyResponse(
                key,
                participant,
                LocalDateTime.now()
            )
        ))
    }

    @MockBean(ERPItauClient::class)
    fun ERPItauClientMock(): ERPItauClient {
        return Mockito.mock(ERPItauClient::class.java)
    }

    @MockBean(BCBClient::class)
    fun BCBClientMock(): BCBClient {
        return Mockito.mock(BCBClient::class.java)
    }
}

@Factory
class KeyDeleteClient {
    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
            PixDeleteKeyServiceGrpc.PixDeleteKeyServiceBlockingStub {
        return PixDeleteKeyServiceGrpc.newBlockingStub(channel)
    }
}
