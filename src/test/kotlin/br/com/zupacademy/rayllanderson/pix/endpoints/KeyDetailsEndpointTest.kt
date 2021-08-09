package br.com.zupacademy.rayllanderson.pix.endpoints

import br.com.zupacademy.rayllanderson.FindPixKeyDetailsServiceGrpc
import br.com.zupacademy.rayllanderson.PixKeyDetailsRequest
import br.com.zupacademy.rayllanderson.pix.clients.BCBClient
import br.com.zupacademy.rayllanderson.pix.creators.bcb.createBCBBankAccountDto
import br.com.zupacademy.rayllanderson.pix.creators.bcb.createBCBOwnerDto
import br.com.zupacademy.rayllanderson.pix.creators.model.createAnotherPixKeyToBeSaved
import br.com.zupacademy.rayllanderson.pix.creators.model.createPixKeyWithCPFToBeSaved
import br.com.zupacademy.rayllanderson.pix.model.PixKey
import br.com.zupacademy.rayllanderson.pix.repository.PixKeyRepository
import br.com.zupacademy.rayllanderson.pix.responses.BCBPixKeyDetailsResponse
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
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito
import org.mockito.Mockito
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyDetailsEndpointTest(
    val repository: PixKeyRepository,
    val grpcClient: FindPixKeyDetailsServiceGrpc.FindPixKeyDetailsServiceBlockingStub,
    val bcbClient: BCBClient,
) {

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `should find pix key details`() {
        val savedKey = repository.save(createPixKeyWithCPFToBeSaved())

        val request = PixKeyDetailsRequest.newBuilder()
            .setClientId(savedKey.owner.id)
            .setPixId(savedKey.pixId)
            .build()

        mockBCBClientToValidRequest(savedKey)

        val response = grpcClient.find(request)

        val expectedKey = savedKey.key
        val expectedKeyType = savedKey.keyType
        val expectedOwnerName = savedKey.owner.name
        val expectedOwnerCpf = savedKey.owner.cpf
        val expectedBankAccountName = savedKey.bankAccount.name
        val expectedBankAccountBranch = savedKey.bankAccount.branch
        val expectedBankAccountType = savedKey.bankAccount.bankAccountType
        val expectedBankAccountNumber = savedKey.bankAccount.accountNumber


        with(response) {
            assertEquals(expectedKey, key)
            assertEquals(expectedKeyType, keyType)
            assertEquals(expectedOwnerName, ownerName)
            assertEquals(expectedOwnerCpf, ownerCpf)
            assertEquals(expectedBankAccountName, account.name)
            assertEquals(expectedBankAccountBranch, account.branch)
            assertEquals(expectedBankAccountType, account.accountType)
            assertEquals(expectedBankAccountNumber, account.number)
        }
    }

    @Test
    fun `shouldn't find pix key details when client id is not present`() {

        val savedKey = repository.save(createPixKeyWithCPFToBeSaved())

        val invalidRequest = PixKeyDetailsRequest.newBuilder()
            .setPixId(savedKey.pixId)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.find(invalidRequest)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente id não pode ser nulo ou vazio", status.description)
        }
    }

    @Test
    fun `shouldn't find pix key details when pix id is not present`() {

        val savedKey = repository.save(createPixKeyWithCPFToBeSaved())

        val invalidRequest = PixKeyDetailsRequest.newBuilder()
            .setClientId(savedKey.owner.id)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.find(invalidRequest)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Pix id não pode ser nulo ou vazio", status.description)
        }
    }

    @Test
    fun `shouldn't find pix key details when client not exists`() {

        val savedKey = repository.save(createPixKeyWithCPFToBeSaved())

        val request = PixKeyDetailsRequest.newBuilder()
            .setClientId("nonexistent-client-123")
            .setPixId(savedKey.pixId)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.find(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("client id não existe", status.description)
        }
    }

    @Test
    fun `shouldn't find pix key details when pix id not exists`() {

        val savedKey = repository.save(createPixKeyWithCPFToBeSaved())

        val request = PixKeyDetailsRequest.newBuilder()
            .setClientId(savedKey.owner.id)
            .setPixId("nonexistent-pix-id")
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.find(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("pix id não existe", status.description)
        }
    }


    @Test
    fun `shouldn't find pix key details when key not belong to client`() {

        val kaguyaKey = repository.save(createPixKeyWithCPFToBeSaved())
        val hayasakaKey = repository.save(createAnotherPixKeyToBeSaved())

        val request = PixKeyDetailsRequest.newBuilder()
            .setClientId(kaguyaKey.owner.id)
            .setPixId(hayasakaKey.pixId)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.find(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Pix id ${request.pixId} não pertence ao cliente ${request.clientId}", error.status.description)
        }
    }

    @Test
    fun `shouldn't find pix key details when bcb client cannot find key`() {

        val savedKey = repository.save(createPixKeyWithCPFToBeSaved())

        // request válida, existente na nossa base de dados
        val request = PixKeyDetailsRequest.newBuilder()
            .setClientId(savedKey.owner.id)
            .setPixId(savedKey.pixId)
            .build()

        // porém não existe na base de dados do Banco central
        BDDMockito.`when`(bcbClient.findPixKeyDetails(savedKey.key)).thenReturn(
            HttpResponse.notFound()
        )

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.find(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada", status.description)
        }
    }

    @Test
    fun `shouldn't find pix key details when bcb client is down`() {

        val savedKey = repository.save(createPixKeyWithCPFToBeSaved())

        val request = PixKeyDetailsRequest.newBuilder()
            .setClientId(savedKey.owner.id)
            .setPixId(savedKey.pixId)
            .build()

        val httpResponse: HttpResponse<Any> = HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
        BDDMockito.`when`(bcbClient.findPixKeyDetails(savedKey.key)).thenThrow(
            HttpClientResponseException("erro", httpResponse)
        )

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.find(request)
        }

        with(error) {
            assertEquals(Status.INTERNAL.code, status.code)
            assertEquals("Ocorreu um erro ao buscar chave pix. Tente novamente mais tarde", status.description)
        }
    }

    fun mockBCBClientToValidRequest(savedKey: PixKey) {
        BDDMockito.`when`(bcbClient.findPixKeyDetails(savedKey.key)).thenReturn(
            HttpResponse.ok(BCBPixKeyDetailsResponse(
                savedKey.keyType,
                savedKey.key,
                createBCBBankAccountDto(),
                createBCBOwnerDto(),
                savedKey.createdAt
            ))
        )
    }

    @MockBean(BCBClient::class)
    fun BCBClientMock(): BCBClient {
        return Mockito.mock(BCBClient::class.java)
    }
}
@Factory
class FindPixKeyClient {
    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
            FindPixKeyDetailsServiceGrpc.FindPixKeyDetailsServiceBlockingStub {
        return FindPixKeyDetailsServiceGrpc.newBlockingStub(channel)
    }
}