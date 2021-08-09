package br.com.zupacademy.rayllanderson.pix.endpoints

import br.com.zupacademy.rayllanderson.FindPixKeyListServiceGrpc
import br.com.zupacademy.rayllanderson.PixKeyListRequest
import br.com.zupacademy.rayllanderson.pix.creators.model.createAnotherPixKeyToBeSaved
import br.com.zupacademy.rayllanderson.pix.creators.model.createPixKeyToBeSaved
import br.com.zupacademy.rayllanderson.pix.creators.model.createPixKeyWithCPFToBeSaved
import br.com.zupacademy.rayllanderson.pix.model.PixKey
import br.com.zupacademy.rayllanderson.pix.repository.PixKeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class KeyListEndpointTest(
    val repository: PixKeyRepository,
    val grpcClient: FindPixKeyListServiceGrpc.FindPixKeyListServiceBlockingStub,
) {

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `should return list of pix keys`() {
        // ambas as keys são do mesmo cliente
        val keyOne = repository.save(createPixKeyToBeSaved())

        val keyTwo = repository.save(createPixKeyWithCPFToBeSaved().let {
            PixKey(it.key, it.keyType, it.bankAccount, keyOne.owner, it.createdAt)
        })

        val request = PixKeyListRequest.newBuilder().setClientId(keyOne.owner.id).build()

        val response = grpcClient.find(request)

        val expectedSize = 2
        val expectedClientId = keyTwo.owner.id

        val expectedFirstKey = keyOne.key
        val expectedSecondKey = keyTwo.key

        val expectedFirstKeyType = keyOne.keyType
        val expectedSecondKeyType = keyTwo.keyType

        val expectedAccountTypeFromFirstKey = keyOne.bankAccount.bankAccountType
        val expectedAccountTypeFromSecondKey = keyTwo.bankAccount.bankAccountType

        with(response) {
            assertThat(keysList).isNotNull.isNotEmpty.hasSize(expectedSize)

            //chave 1
            val firstKey = keysList[0]
            assertThat(firstKey).isNotNull
            assertThat(firstKey.clientId).isEqualTo(expectedClientId)
            assertThat(firstKey.key).isEqualTo(expectedFirstKey)
            assertThat(firstKey.keyType).isEqualTo(expectedFirstKeyType)
            assertThat(firstKey.accountType).isEqualTo(expectedAccountTypeFromFirstKey)

            //chave 2
            val secondKey = keysList[1]
            assertThat(secondKey).isNotNull
            assertThat(secondKey.clientId).isEqualTo(expectedClientId)
            assertThat(secondKey.key).isEqualTo(expectedSecondKey)
            assertThat(secondKey.keyType).isEqualTo(expectedSecondKeyType)
            assertThat(secondKey.accountType).isEqualTo(expectedAccountTypeFromSecondKey)
        }
    }

    @Test
    fun `should return list of pix keys from client`() {
        // salvando chave para a cliente kaguya
        val kaguyaKey = repository.save(createPixKeyToBeSaved())

        // salvando outra chave, porém de um cliente diferente
        repository.save(createAnotherPixKeyToBeSaved())

        val request = PixKeyListRequest.newBuilder().setClientId(kaguyaKey.owner.id).build()

        val response = grpcClient.find(request)

        val expectedSize = 1
        val expectedClientId = kaguyaKey.owner.id
        val expectedKey = kaguyaKey.key
        val expectedKeyType = kaguyaKey.keyType
        val expectedAccountTypeFromKey = kaguyaKey.bankAccount.bankAccountType

        with(response) {
            assertThat(keysList).isNotNull.isNotEmpty.hasSize(expectedSize)

            val key = keysList[0]
            assertThat(key).isNotNull
            assertThat(key.clientId).isEqualTo(expectedClientId)
            assertThat(key.key).isEqualTo(expectedKey)
            assertThat(key.keyType).isEqualTo(expectedKeyType)
            assertThat(key.accountType).isEqualTo(expectedAccountTypeFromKey)
        }
    }

    @Test
    fun `should return a empty list when client doesn't have registered pix key`() {
        val request = PixKeyListRequest.newBuilder().setClientId(UUID.randomUUID().toString()).build()

        val response = grpcClient.find(request)

        with(response) {
            assertThat(keysList).isNotNull.isEmpty()
        }
    }

    @Test
    fun `shouldn't return the list when client id is not present`() {
        val request = PixKeyListRequest.newBuilder().build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.find(request)
        }

        with(error) {
            assertThat(status.code).isEqualTo(Status.INVALID_ARGUMENT.code)
            assertThat(status.description).isEqualTo("Cliente id não pode ser nulo ou vazio")
        }
    }

    @Factory
    class KeyListClient {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                FindPixKeyListServiceGrpc.FindPixKeyListServiceBlockingStub {
            return FindPixKeyListServiceGrpc.newBlockingStub(channel)
        }
    }

}