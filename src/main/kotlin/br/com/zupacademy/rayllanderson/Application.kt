package br.com.zupacademy.rayllanderson

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zupacademy.rayllanderson")
		.start()
}

