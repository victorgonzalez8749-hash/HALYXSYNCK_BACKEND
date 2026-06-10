package com.halyxsync

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

// 1. Modelos JSON (¡Agregamos el nombre a la respuesta para la app!)
@Serializable
data class LoginRequest(
    val correo: String,
    val contrasena: String
)

@Serializable
data class LoginResponse(
    val loginExitoso: Boolean,
    val mensaje: String,
    val rol: String? = null,
    val nombre: String? = null
)


object UsuariosTable : Table("usuarios") {
    val idUsuario = integer("id_usuario").autoIncrement()
    val nombreCompleto = varchar("nombre_completo", 255)
    val correo = varchar("correo", 100)
    val contrasena = varchar("contrasena", 255)
    val rol = varchar("rol", 50)

    override val primaryKey = PrimaryKey(idUsuario)
}

// 3. Configuración de la ruta
fun Application.configureRouting() {
    routing {

        post("/login") {
            try {
                val solicitud = call.receive<LoginRequest>()

                // Buscamos al usuario en Railway
                val usuarioEncontrado = transaction {
                    UsuariosTable.selectAll().where {
                        (UsuariosTable.correo eq solicitud.correo) and
                                (UsuariosTable.contrasena eq solicitud.contrasena)
                    }.singleOrNull()
                }

                if (usuarioEncontrado != null) {
                    val rolUsuario = usuarioEncontrado[UsuariosTable.rol]
                    val nombreUsuario = usuarioEncontrado[UsuariosTable.nombreCompleto]

                    call.respond(
                        HttpStatusCode.OK,
                        LoginResponse(
                            loginExitoso = true,
                            mensaje = "¡Bienvenido, $nombreUsuario!",
                            rol = rolUsuario,
                            nombre = nombreUsuario
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        LoginResponse(loginExitoso = false, mensaje = "Correo o contraseña incorrectos.")
                    )
                }

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    LoginResponse(loginExitoso = false, mensaje = "Error: ${e.localizedMessage}")
                )
            }
        }

    }
}