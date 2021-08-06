package br.com.zupacademy.rayllanderson.pix.validators

fun assertThatNotNull(field: String?, fieldName: String){
    if (field.isNullOrBlank()) throw IllegalArgumentException("$fieldName não pode ser nulo ou vazio")
}