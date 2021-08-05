package br.com.zupacademy.rayllanderson.core.exceptions.handler

import br.com.zupacademy.rayllanderson.core.exceptions.handler.ExceptionHandler.StatusWithDetails
import com.google.protobuf.Any
import com.google.rpc.BadRequest
import com.google.rpc.Code
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

/**
 * Handles the Bean Validation errors adding theirs violations into request trailers (metadata)
 */
@Singleton
class ConstraintViolationExceptionHandler : ExceptionHandler<ConstraintViolationException> {

    override fun handle(e: ConstraintViolationException): StatusWithDetails {

        val details = BadRequest.newBuilder()
            .addAllFieldViolations(e.constraintViolations.map {
                BadRequest.FieldViolation.newBuilder()
                    .setField(it.propertyPath.last().name ?: "?? key ??") // still thinking how to solve this case
                    .setDescription(it.message)
                    .build()
            })
            .build()

        val statusProto = com.google.rpc.Status.newBuilder()
                    .setCode(Code.INVALID_ARGUMENT_VALUE)
                    .setMessage("Request with invalid data")
                    .addDetails(Any.pack(details))
                    .build()

        return StatusWithDetails(statusProto)
    }

    override fun supports(e: Exception): Boolean {
        return e is ConstraintViolationException
    }

}