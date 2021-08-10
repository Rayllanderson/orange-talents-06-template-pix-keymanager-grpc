package br.com.zupacademy.rayllanderson.pix.utils

import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId

class LocalDateTimeConverter {
    companion object{
        fun toProtobufTimestamp(date: LocalDateTime): Timestamp{
            return date.atZone(ZoneId.of("UTC")).toInstant().let { instant ->
                Timestamp.newBuilder().setSeconds(instant.epochSecond).setNanos(instant.nano).build()
            }
        }
    }
}