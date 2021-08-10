package br.com.zupacademy.rayllanderson.pix.endpoints

import br.com.zupacademy.rayllanderson.AccountType
import br.com.zupacademy.rayllanderson.KeyType
import br.com.zupacademy.rayllanderson.PixKeyRegisterRequest
import br.com.zupacademy.rayllanderson.PixKeyRegisterServiceGrpc
import br.com.zupacademy.rayllanderson.pix.clients.BCBClient
import br.com.zupacademy.rayllanderson.pix.clients.ItauClient
import br.com.zupacademy.rayllanderson.pix.creators.bcb.createBCBPixKeyResponseValid
import br.com.zupacademy.rayllanderson.pix.creators.bcb.createBcbPixRequestToBeSaved
import br.com.zupacademy.rayllanderson.pix.creators.bcb.createBcbPixRequestToBeSavedWithKeyRandom
import br.com.zupacademy.rayllanderson.pix.creators.itau.createItauResponseValid
import br.com.zupacademy.rayllanderson.pix.creators.model.createBankAccountValid
import br.com.zupacademy.rayllanderson.pix.creators.model.createOwnerValid
import br.com.zupacademy.rayllanderson.pix.model.PixKey
import br.com.zupacademy.rayllanderson.pix.repository.PixKeyRepository
import br.com.zupacademy.rayllanderson.pix.responses.ItauClientAccountResponse
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyRegisterEndpointTest(
    val repository: PixKeyRepository,
    val grpcClient: PixKeyRegisterServiceGrpc.PixKeyRegisterServiceBlockingStub,
    val itauClient: ItauClient,
    val bcbClient: BCBClient,
) {

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Nested
    inner class GERAL {

        @Test
        fun `shouldn't create new pix key when clientId is empty`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setKeyType(KeyType.CPF)
                .setKey("08018462104")
                .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
                assertEquals("Cliente id não pode ser nulo", this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }

        @Test
        fun `shouldn't create new pix key when key is greater than 77 characters`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setKeyType(KeyType.CPF)
                .setKey("0".repeat(78))
                .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
                assertEquals("Chave pix precisa ser menor ou igual a 77", this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }

        @Test
        fun `shouldn't create new pix key when key already exists`() {

            var savedPixKey: String

            PixKey(
                "08018462104",
                keyType = KeyType.CPF,
                bankAccount = createBankAccountValid(),
                owner = createOwnerValid(),
                createdAt = LocalDateTime.now()
            ).run {
                repository.save(this)
                savedPixKey = this.key
            }

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setKeyType(KeyType.CPF)
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKey(savedPixKey)
                .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.ALREADY_EXISTS.code, this.status.code)
                assertEquals("Chave já está cadastrada", this.status.description)
            }
        }
    }

    @Nested
    inner class CLIENTS {
        @Test
        fun `shouldn't create new pix key when itau client did not find the user`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setKeyType(KeyType.CPF)
                .setAccountType(AccountType.CONTA_POUPANCA)
                .setKey("08018462104")
                .build()

            BDDMockito.`when`(itauClient.findByIdAndAccountType(pixToBeSaved.clientId, pixToBeSaved.accountType))
                .thenReturn(HttpResponse.notFound())

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.NOT_FOUND.code, this.status.code)
                assertEquals("Cliente não encontrado", this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }

        @Test
        fun `shouldn't create new pix key when itau client is down`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setKeyType(KeyType.CPF)
                .setAccountType(AccountType.CONTA_POUPANCA)
                .setKey("08018462104")
                .build()

            val httpResponse: HttpResponse<Any> = HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            BDDMockito.`when`(itauClient.findByIdAndAccountType(pixToBeSaved.clientId, pixToBeSaved.accountType))
                .thenThrow(HttpClientResponseException("erro", httpResponse))

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INTERNAL.code, this.status.code)
                assertEquals("Ocorreu um erro ao cadastrar pix. Tente mais tarde", this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }

        @Test
        fun `shouldn't create new pix key when BCB client is down`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setKeyType(KeyType.CPF)
                .setAccountType(AccountType.CONTA_POUPANCA)
                .setKey("08018462104")
                .build()

            // montando uma request válida
            val expectedItauResponse = mockItauClientToValidRequest(pixToBeSaved)
            val bcbRequest = createBcbPixRequestToBeSaved(pixToBeSaved.keyType, pixToBeSaved.key, expectedItauResponse)

            val httpResponse: HttpResponse<Any> = HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
            BDDMockito.`when`(bcbClient.registerPixKey(bcbRequest))
                .thenThrow(HttpClientResponseException("erro", httpResponse))

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INTERNAL.code, this.status.code)
                assertEquals("Ocorreu um erro ao tentar criar chave pix. Tente novamente mais tarde", this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }

        @Test
        fun `shouldn't create new pix key when key already exists in BCB client`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setKeyType(KeyType.CPF)
                .setAccountType(AccountType.CONTA_POUPANCA)
                .setKey("08018462104")
                .build()

            // montando uma request válida
            val expectedItauResponse = mockItauClientToValidRequest(pixToBeSaved)
            val bcbRequest = createBcbPixRequestToBeSaved(pixToBeSaved.keyType, pixToBeSaved.key, expectedItauResponse)

            val httpResponse: HttpResponse<Any> = HttpResponse.status(HttpStatus.UNPROCESSABLE_ENTITY)
            BDDMockito.`when`(bcbClient.registerPixKey(bcbRequest))
                .thenThrow(HttpClientResponseException("erro", httpResponse))

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.ALREADY_EXISTS.code, this.status.code)
                assertEquals("Chave pix já está cadastrada", this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }
    }

    @Nested
    inner class TYPES {
        @Test
        fun `shouldn't create new pix key when account type is not setted`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setKeyType(KeyType.CPF)
                .setKey("08018462104")
                .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
                assertEquals("O tipo da conta não está setada ou não é válida.", this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }

        @Test
        fun `shouldn't create new pix key when account type is not valid`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setKeyType(KeyType.CPF)
                .setAccountType(AccountType.UNKNOWN_ACCOUNT)
                .setKey("08018462104")
                .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
                assertEquals("O tipo da conta não está setada ou não é válida.", this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }

        @Test
        fun `shouldn't create new pix key when key type is not setted`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKey("08018462104")
                .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
                assertEquals("O tipo da chave não está setado ou não é válido.", this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }

        @Test
        fun `shouldn't create new pix key when key type is not valid`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setKeyType(KeyType.UNKNOWN)
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKey("08018462104")
                .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
                assertEquals("O tipo da chave não está setado ou não é válido.", this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }
    }

    val emptyKeyDefaultMessage = "Chave pix não pode ser nula ou vazia"

    @Nested
    inner class CPF {
        @Test
        fun `should create new pix key with cpf`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKeyType(KeyType.CPF)
                .setKey("08018462104")
                .build()

            // Mockando cliente itaú para uma request válida
            val expectedItauResponse = mockItauClientToValidRequest(pixToBeSaved)

            // Mockando cliente Banco central para uma request válida
            mockBcbClientToValidRequest(pixToBeSaved, expectedItauResponse)

            val response = grpcClient.register(pixToBeSaved)

            with(response) {
                assertNotNull(this)
                assertNotNull(this.pixId)
                assertTrue(repository.existsById(this.pixId))
            }
        }

        @Test
        fun `shouldn't create new pix key when cpf is invalid`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKeyType(KeyType.CPF)
                .setKey("08018462104000000222")
                .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
                assertEquals("Cpf deve estar bem formatado. Por exemplo: 12345678901", this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }

        @Test
        fun `shouldn't create new pix key when cpf is empty`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKeyType(KeyType.CPF)
                .setKey("")
                .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
                assertEquals(emptyKeyDefaultMessage, this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }
    }

    @Nested
    inner class PHONE {
        @Test
        fun `should create new pix key with phone`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKeyType(KeyType.PHONE)
                .setKey("+559899999999")
                .build()

            // Mockando cliente itaú para uma request válida
            val expectedItauResponse = mockItauClientToValidRequest(pixToBeSaved)

            // Mockando cliente Banco central para uma request válida
            mockBcbClientToValidRequest(pixToBeSaved, expectedItauResponse)

            val response = grpcClient.register(pixToBeSaved)

            with(response) {
                assertNotNull(this)
                assertNotNull(this.pixId)
                assertTrue(repository.existsById(this.pixId))
            }
        }

        @Test
        fun `shouldn't create new pix key when phone is invalid`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKeyType(KeyType.PHONE)
                .setKey("989925248962")
                .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
                assertEquals("Telefone deve estar bem formatado. Por exemplo: +5585988714077", this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }

        @Test
        fun `shouldn't create new pix key when phone is empty`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKeyType(KeyType.PHONE)
                .setKey("")
                .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
                assertEquals(emptyKeyDefaultMessage, this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }
    }

    @Nested
    inner class EMAIL {
        @Test
        fun `should create new pix key with email`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKeyType(KeyType.EMAIL)
                .setKey("kaguya@sama.com")
                .build()

            // Mockando cliente itaú para uma request válida
            val expectedItauResponse = mockItauClientToValidRequest(pixToBeSaved)

            // Mockando cliente Banco central para uma request válida
            mockBcbClientToValidRequest(pixToBeSaved, expectedItauResponse)

            val response = grpcClient.register(pixToBeSaved)

            with(response) {
                assertNotNull(this)
                assertNotNull(this.pixId)
                assertTrue(repository.existsById(this.pixId))
            }
        }

        @Test
        fun `shouldn't create new pix key when email is invalid`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKeyType(KeyType.EMAIL)
                .setKey("kaguya-sama.com")
                .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
                assertEquals("Email deve estar bem formatado.", this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }

        @Test
        fun `shouldn't create new pix key when email is empty`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKeyType(KeyType.EMAIL)
                .setKey("")
                .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
                assertEquals(emptyKeyDefaultMessage, this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }
    }

    @Nested
    inner class RANDOM {
        @Test
        fun `should create new pix key with random`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKeyType(KeyType.RANDOM)
                .build()

            // Mockando cliente itaú para uma request válida
            val expectedItauResponse = mockItauClientToValidRequest(pixToBeSaved)

            // Mockando cliente Banco central para uma request válida
            val bcbRequest = createBcbPixRequestToBeSavedWithKeyRandom(pixToBeSaved.keyType, expectedItauResponse)
            val expectedBcbResponse = createBCBPixKeyResponseValid(bcbRequest)
            BDDMockito.`when`(bcbClient.registerPixKey(bcbRequest))
                .thenReturn(HttpResponse.created(expectedBcbResponse))

            val response = grpcClient.register(pixToBeSaved)

            with(response) {
                assertNotNull(this)
                assertNotNull(this.pixId)
                assertTrue(repository.existsById(this.pixId))
            }
        }

        @Test
        fun `shouldn't create new pix key when type is random and key is present`() {

            val pixToBeSaved = PixKeyRegisterRequest.newBuilder()
                .setClientId(UUID.randomUUID().toString())
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKeyType(KeyType.RANDOM)
                .setKey("any-key-here")
                .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.register(pixToBeSaved)
            }

            with(error) {
                assertEquals(Status.INVALID_ARGUMENT.code, this.status.code)
                assertEquals("Valor da chave não deve ser preenchido quando o tipo da chave é ${KeyType.RANDOM}",
                    this.status.description)
                assertFalse(repository.existsByKey(pixToBeSaved.key))
            }
        }
    }


    fun mockItauClientToValidRequest(pixToBeSaved: PixKeyRegisterRequest): ItauClientAccountResponse {
        val itauResponse = createItauResponseValid(pixToBeSaved.clientId, pixToBeSaved.accountType)
        BDDMockito.`when`(itauClient.findByIdAndAccountType(pixToBeSaved.clientId, pixToBeSaved.accountType))
            .thenReturn(HttpResponse.ok(itauResponse))
        return itauResponse
    }

    fun mockBcbClientToValidRequest(pixToBeSaved: PixKeyRegisterRequest, itauResponse: ItauClientAccountResponse) {
        val bcbRequest = createBcbPixRequestToBeSaved(pixToBeSaved.keyType, pixToBeSaved.key, itauResponse)
        val expectedBcbResponse = createBCBPixKeyResponseValid(bcbRequest)
        BDDMockito.`when`(bcbClient.registerPixKey(bcbRequest)).thenReturn(HttpResponse.created(expectedBcbResponse))
    }


    @MockBean(ItauClient::class)
    fun itauClientMock(): ItauClient {
        return Mockito.mock(ItauClient::class.java)
    }

    @MockBean(BCBClient::class)
    fun BCBClientMock(): BCBClient {
        return Mockito.mock(BCBClient::class.java)
    }
}

@Factory
class KeyRegisterClient {
    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
            PixKeyRegisterServiceGrpc.PixKeyRegisterServiceBlockingStub {
        return PixKeyRegisterServiceGrpc.newBlockingStub(channel)
    }
}