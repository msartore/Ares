package dev.msartore.ares.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Environment
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.runtime.mutableStateOf
import dev.msartore.ares.MainActivity
import dev.msartore.ares.R
import dev.msartore.ares.models.ConcurrentMutableList
import dev.msartore.ares.models.FileData
import dev.msartore.ares.models.FileTransfer
import dev.msartore.ares.server.KtorService.KtorServer.CHANNEL_ID
import dev.msartore.ares.server.KtorService.KtorServer.ONGOING_NOTIFICATION_ID
import dev.msartore.ares.server.KtorService.KtorServer.PORT
import dev.msartore.ares.server.KtorService.KtorServer.concurrentMutableList
import dev.msartore.ares.server.KtorService.KtorServer.fileTransfer
import dev.msartore.ares.server.KtorService.KtorServer.isServerOn
import dev.msartore.ares.server.KtorService.KtorServer.server
import dev.msartore.ares.utils.getByteArrayFromDrawable
import dev.msartore.ares.utils.printableSize
import dev.msartore.ares.utils.toFileDataJson
import dev.msartore.ares.utils.toJsonArray
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.call
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.netty.Netty
import io.ktor.server.request.header
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.utils.io.consumeEachBufferRange
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import kotlinx.html.ButtonType
import kotlinx.html.FormEncType
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.dd
import kotlinx.html.div
import kotlinx.html.dl
import kotlinx.html.dt
import kotlinx.html.form
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.input
import kotlinx.html.li
import kotlinx.html.link
import kotlinx.html.ol
import kotlinx.html.style
import kotlinx.html.title
import java.io.File


@ExperimentalGetImage
class KtorService: Service() {

    object KtorServer {
        const val ONGOING_NOTIFICATION_ID = 123
        const val CHANNEL_ID = "server_id"
        const val PORT = 7070

        val concurrentMutableList = ConcurrentMutableList<FileData>()
        val isServerOn = mutableStateOf(false)

        var server: ApplicationEngine? = null
        var fileTransfer = FileTransfer()
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
            }

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.server))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(R.drawable.logo_ares)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.ticker_text))
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        server = embeddedServer(Netty, port = PORT) {
            routing {
                get ("/{name}") {

                    when(val fileIndex = call.parameters["name"]?.toInt()) {
                        null, !in 0 .. concurrentMutableList.size.value -> call.respond(HttpStatusCode.NotFound)
                        else -> {
                            val file = concurrentMutableList.list.elementAt(fileIndex)
                            val inputStream = contentResolver.openInputStream(file.uri)

                            call.response.header("Content-Disposition", "attachment; filename=\"${file.name}\"")

                            call.respondBytesWriter (
                                contentType = ContentType.Any,
                                contentLength = file.size?.toLong()
                            ) {
                                inputStream?.toByteReadChannel()?.consumeEachBufferRange { buffer, last ->
                                    writeFully(buffer)
                                    !last
                                }.also {
                                    inputStream?.close()
                                }
                            }
                        }
                    }
                }
                get("/info") {
                    call.respond(concurrentMutableList.list.mapIndexed { index, fileData ->
                        fileData.toFileDataJson(index)
                    }.toJsonArray().toString())
                }
                get("/favicon.png") {
                    getByteArrayFromDrawable(applicationContext, R.drawable.logo_ares)?.let { it1 ->
                        call.respondBytes(it1)
                    }
                }
                post("/upload") {

                    fileTransfer.pipelineContext = this

                    var fileName: String
                    var file: File? = null
                    val multipartData = call.receiveMultipart()
                    val contentLength = call.request.header(HttpHeaders.ContentLength)
                    var fileSizeRemaining = 0
                    var result = false

                    fileTransfer.isActive.value = true

                    runCatching {

                        multipartData.forEachPart { part ->

                            when (part) {

                                is PartData.FileItem -> {

                                    fileName = part.originalFileName as String

                                    file = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName")

                                    if (file?.exists() != true) {
                                        file?.createNewFile()

                                        fileTransfer.apply {

                                            if (name == null) {
                                                name = fileName
                                                size = contentLength?.toInt()
                                            }

                                            file?.outputStream()?.channel.use {
                                                part.streamProvider().toByteReadChannel().consumeEachBufferRange { buffer, last ->

                                                    it?.write(buffer)

                                                    fileSizeRemaining += buffer.capacity()

                                                    sizeTransferred.value = (fileSizeRemaining/(contentLength?.toFloatOrNull() ?: 1f))

                                                    !last
                                                }
                                            }
                                        }
                                    }

                                    part.dispose()
                                }

                                else -> {}
                            }
                        }

                        result = true
                    }.getOrElse {
                        it.printStackTrace()
                        file?.delete()
                    }

                    fileTransfer.apply {
                        isActive.value = false
                        sizeTransferred.value = 0f
                    }

                    call.respondRedirect("/?success=$result")
                }
                get("/") {
                    val result = runCatching {
                        call.parameters["success"]
                    }.getOrNull()

                    call.respondHtml {
                        head {
                            title(content = applicationContext.getString(R.string.ares_title_website))
                            link(rel = "icon", href = "/favicon.png")
                            style {
                                +"a { color:black; } .file { margin:10px; } .form { height: 250px; border: 2px solid white; border-radius: 50px ; background-color: coral; } .form form, .form b { position: relative; left: 5%; top: 25%; }"
                            }
                        }
                        body {
                            div(classes = "form") {
                                form(
                                    action = "/upload",
                                    method = FormMethod.post,
                                    encType = FormEncType.multipartFormData
                                ) {
                                    input(type=InputType.file, name="upload")
                                    button(name = "upload", type = ButtonType.submit) {
                                        a {
                                            +"Upload"
                                        }
                                    }
                                }
                                if (result != null)
                                    b {
                                        +"file sent: $result"
                                    }
                            }
                            h2 {
                                +getString(
                                    if (concurrentMutableList.list.isEmpty())
                                        R.string.no_file_available
                                    else
                                        R.string.file_available
                                )
                            }
                            ol {
                                for(i in 0 until concurrentMutableList.size.value) {
                                    li(classes = "file") {
                                        dl {
                                            dt {
                                                +"${getString(R.string.name)}: ${concurrentMutableList.list.elementAt(i).name}"
                                            }
                                            dd {
                                                +"${getString(R.string.size)}: ${(concurrentMutableList.list.elementAt(i).size?:1).printableSize()}"
                                            }
                                            dd {
                                                a(href = "/$i") {
                                                    b {
                                                        +getString(R.string.download)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.start(false)

        isServerOn.value = true
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop(0,0)
        isServerOn.value = false
    }
}