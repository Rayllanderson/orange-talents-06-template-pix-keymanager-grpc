package br.com.zupacademy.rayllanderson.pix.validators

class CpfValidator: (String) -> Boolean {
    override fun invoke(cpf: String): Boolean = cpf.matches("^[0-9]{11}\$".toRegex())
}

class EmailValidator: (String) -> Boolean {
    override fun invoke(email: String): Boolean = email.matches(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+".toRegex())
}

class PhoneValidator: (String) -> Boolean {
    override fun invoke(phone: String): Boolean = phone.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
}
