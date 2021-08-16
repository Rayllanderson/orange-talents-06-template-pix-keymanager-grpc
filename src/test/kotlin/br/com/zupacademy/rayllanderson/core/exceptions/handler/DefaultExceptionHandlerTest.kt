package br.com.zupacademy.rayllanderson.core.exceptions.handler

import br.com.zupacademy.rayllanderson.core.exceptions.ForbiddenException
import br.com.zupacademy.rayllanderson.core.exceptions.InternalServerErrorException
import br.com.zupacademy.rayllanderson.core.exceptions.NotFoundException
import br.com.zupacademy.rayllanderson.core.exceptions.PixKeyExistingException
import io.grpc.Status
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import java.lang.NullPointerException
import javax.validation.ConstraintViolationException

internal class DefaultExceptionHandlerTest{

    @Test
    fun `should return INVALID_ARGUMENT when exception is IllegalArgumentException`(){
        val thrownException = IllegalArgumentException("Dados inválidos")

        val grpcException = DefaultExceptionHandler().handle(thrownException)

        assertEquals(Status.INVALID_ARGUMENT.code, grpcException.status.code)
        assertEquals(thrownException.message, grpcException.status.description)
    }

    @Test
    fun `should return INVALID_ARGUMENT when exception is ConstraintViolationException`(){
        val thrownException = ConstraintViolationException(emptySet())

        val grpcException = DefaultExceptionHandler().handle(thrownException)

        assertEquals(Status.INVALID_ARGUMENT.code, grpcException.status.code)
        assertEquals(thrownException.message, grpcException.status.description)
    }

    @Test
    fun `should return ALREADY_EXISTS when exception is PixKeyExistingException`(){
        val thrownException = PixKeyExistingException("Chave já existe")

        val grpcException = DefaultExceptionHandler().handle(thrownException)

        assertEquals(Status.ALREADY_EXISTS.code, grpcException.status.code)
        assertEquals(thrownException.message, grpcException.status.description)
    }

    @Test
    fun `should return NOT_FOUND when exception is NotFoundException`(){
        val thrownException = NotFoundException("Não encontrado")

        val grpcException = DefaultExceptionHandler().handle(thrownException)

        assertEquals(Status.NOT_FOUND.code, grpcException.status.code)
        assertEquals(thrownException.message, grpcException.status.description)
    }

    @Test
    fun `should return PERMISSION_DENIED when exception is ForbiddenException`(){
        val thrownException = ForbiddenException("Não autorizado")

        val grpcException = DefaultExceptionHandler().handle(thrownException)

        assertEquals(Status.PERMISSION_DENIED.code, grpcException.status.code)
        assertEquals(thrownException.message, grpcException.status.description)
    }

    @Test
    fun `should return INTERNAL when exception is InternalServerErrorException`(){
        val thrownException = InternalServerErrorException("Erro interno")

        val grpcException = DefaultExceptionHandler().handle(thrownException)

        assertEquals(Status.INTERNAL.code, grpcException.status.code)
        assertEquals(thrownException.message, grpcException.status.description)
    }

    @Test
    fun `should return INTERNAL when exception is IllegalStateException`(){
        val thrownException = IllegalStateException("Erro")

        val grpcException = DefaultExceptionHandler().handle(thrownException)

        assertEquals(Status.FAILED_PRECONDITION.code, grpcException.status.code)
        assertEquals(thrownException.message, grpcException.status.description)
    }

    @Test
    fun `should return UNKNOWN when exception is not mapped`(){
        val thrownException = NullPointerException("Referência nula em algum lugar")

        val grpcException = DefaultExceptionHandler().handle(thrownException)

        assertEquals(Status.UNKNOWN.code, grpcException.status.code)
    }
}