package com.halyxsync
import kotlinx.serialization.Serializable

@Serializable
data class Paciente(
    val nombre: String,
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val fechaNacimiento: String = "",
    val curp: String = ""
)