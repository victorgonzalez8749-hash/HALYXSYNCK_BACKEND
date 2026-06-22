package com.halyxsync

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


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

@Serializable
data class Pacientes(
    val nombre: String,
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val fechaNacimiento: String = "",
    val curp: String = ""
)

object UsuariosTable : Table("usuarios") {
    val idUsuario = integer("id_usuario").autoIncrement()
    val nombreCompleto = varchar("nombre_completo", 255)
    val correo = varchar("correo", 100)
    val contrasena = varchar("contrasena", 255)
    val rol = varchar("rol", 50)

    override val primaryKey = PrimaryKey(idUsuario)
}

//  Configuración de la ruta
fun Application.configureRouting() {
    routing {

        post("/login") {
            //  desprotegemos el codigo quitando la parte del try-catch para que nuestro
            // programa sea vulnerable
            val solicitud = call.receive<LoginRequest>()

            val usuarioEncontrado = transaction {
                UsuariosTable.selectAll().where {
                    (UsuariosTable.correo eq solicitud.correo) and
                            (UsuariosTable.contrasena eq solicitud.contrasena)
                }.singleOrNull()
            }

            if (usuarioEncontrado != null) {
                call.respond(
                    HttpStatusCode.OK,
                    LoginResponse(
                        loginExitoso = true,
                        mensaje = "¡Bienvenido!",
                        rol = usuarioEncontrado[UsuariosTable.rol],
                        nombre = usuarioEncontrado[UsuariosTable.nombreCompleto]
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    LoginResponse(loginExitoso = false, mensaje = "Incorrecto.")
                )
            }
        }


        post("/pacientes/nuevo") {
            val paciente = call.receive<Paciente>()
            org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction(kotlinx.coroutines.Dispatchers.IO) {
                exec("INSERT INTO Pacientes (nombre) VALUES ('${paciente.nombre}')")
                commit()
            }
            call.respondText("Guardado", status = HttpStatusCode.Created)
        }

    }
}