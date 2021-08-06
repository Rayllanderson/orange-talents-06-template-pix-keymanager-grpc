package br.com.zupacademy.rayllanderson.core.exceptions

class PixKeyExistingException(
    override val message: String
) : RuntimeException(message)


class NotFoundException(
    override val message: String
) : RuntimeException(message)

class InternalServerErrorException(
    override val message: String
) : RuntimeException(message)

class ForbiddenException(
    override val message: String
) : RuntimeException(message)