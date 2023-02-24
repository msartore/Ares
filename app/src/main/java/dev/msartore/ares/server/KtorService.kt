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
import dev.msartore.ares.models.FileType
import dev.msartore.ares.server.KtorService.KtorServer.CHANNEL_ID
import dev.msartore.ares.server.KtorService.KtorServer.ONGOING_NOTIFICATION_ID
import dev.msartore.ares.server.KtorService.KtorServer.concurrentMutableList
import dev.msartore.ares.server.KtorService.KtorServer.fileTransfer
import dev.msartore.ares.server.KtorService.KtorServer.isServerOn
import dev.msartore.ares.server.KtorService.KtorServer.port
import dev.msartore.ares.server.KtorService.KtorServer.server
import dev.msartore.ares.ui.theme.Theme.background
import dev.msartore.ares.ui.theme.Theme.container
import dev.msartore.ares.ui.theme.Theme.darkTheme
import dev.msartore.ares.utils.getByteArrayFromDrawable
import dev.msartore.ares.utils.printableSize
import dev.msartore.ares.utils.splitFileTypeFromName
import dev.msartore.ares.utils.toFileDataJson
import dev.msartore.ares.utils.toJsonArray
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.autohead.AutoHeadResponse
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
import kotlinx.html.InputFormEncType
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
import kotlinx.html.styleLink
import kotlinx.html.title
import kotlinx.html.unsafe
import java.io.File

@ExperimentalGetImage
class KtorService: Service() {

    object KtorServer {
        const val ONGOING_NOTIFICATION_ID = 123
        const val CHANNEL_ID = "server_id"
        var port = 7070

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
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.ticker_text))
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        server = embeddedServer(Netty, port = port) {
            install(AutoHeadResponse)
            routing {
                get ("/{name}") {
                    val streaming = call.parameters["streaming"]
                    val fileUUID = call.parameters["name"]
                    val file = concurrentMutableList.list.find { fileUUID == it.UUID.toString() }
                    val inputStream = file?.uri?.let { it1 ->
                        contentResolver.openInputStream(
                            it1
                        )
                    }

                    if (file == null) {
                        call.respond(HttpStatusCode.NotFound)
                    }
                    else {
                        call.response.header(
                            HttpHeaders.ContentDisposition,
                            "${
                                if (streaming != "true" || (file.fileType != FileType.IMAGE && file.fileType != FileType.VIDEO))
                                    "attachment"
                                else
                                    "inline"
                            }; filename=\"${file.name}\""
                        )

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
                get("/info") {
                    call.respond(concurrentMutableList.list.map { fileData ->
                        fileData.toFileDataJson()
                    }.toJsonArray().toString())
                }
                get("/favicon.png") {
                    getByteArrayFromDrawable(applicationContext, R.drawable.logo)?.let { it1 ->
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

                                    val (n, e) = splitFileTypeFromName(fileName)
                                    var counter = 1

                                    file = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName")

                                    while (file?.exists() == true) {
                                        file = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$n($counter)$e")
                                        counter++
                                    }

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

                                                sizeTransferred.value = fileSizeRemaining/(contentLength?.toFloatOrNull() ?: 1f)

                                                !last
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
                get("/style.css") {

                    val asset = assets.open("style.css")

                    call.respondBytesWriter (
                        contentType = ContentType.Text.CSS,
                    ) {
                        asset.toByteReadChannel().consumeEachBufferRange { buffer, last ->
                            writeFully(buffer)
                            !last
                        }.also {
                            asset.close()
                        }
                    }
                }
                get("/") {
                    val result = call.parameters["success"]
                    val textColor = if (darkTheme) "white" else "black"

                    call.respondHtml {
                        head {
                            title(content = applicationContext.getString(R.string.ares_title_website))
                            link(rel = "icon", href = "/favicon.png")
                            styleLink("/style.css")
                            style {
                                unsafe {
                                    +"body { background-color:${background.cssGenerator()}; color:$textColor; } .form { background-color: ${container.cssGenerator()}; } a { color:$textColor; }"
                                }
                            }
                        }
                        body {
                            div(classes = "form") {
                                form(
                                    action = "/upload",
                                    method = FormMethod.post,
                                    encType = FormEncType.multipartFormData
                                ) {
                                    div {
                                        input(type=InputType.file, name="upload", formEncType = InputFormEncType.multipartFormData)
                                    }

                                    button(type = ButtonType.submit) {
                                        +getString(R.string.upload)
                                    }
                                }

                                if (result != null)
                                    b {
                                        +(getString(R.string.file_sent) + ": $result")
                                    }
                            }
                            h2 {
                                +getString(
                                    if (concurrentMutableList.list.isEmpty())
                                        R.string.no_file_available
                                    else
                                        R.string.files
                                )
                            }
                            ol {
                                for(i in 0 until concurrentMutableList.size.value) {
                                    concurrentMutableList.list.elementAt(i).run {
                                        li(classes = "file") {
                                            dl {
                                                if (fileType != FileType.TEXT) {
                                                    dt {
                                                        +"${getString(R.string.name)}: $name"
                                                    }
                                                    dd {
                                                        +"${getString(R.string.size)}: ${(size?:1).printableSize()}"
                                                    }
                                                    dd {
                                                        a(href = "/$UUID") {
                                                            b {
                                                                +getString(R.string.download)
                                                            }
                                                        }
                                                    }
                                                    if (fileType == FileType.IMAGE || fileType == FileType.VIDEO)
                                                        dd {
                                                            a(href = "/$UUID?streaming=true") {
                                                                b {
                                                                    +getString(R.string.streaming)
                                                                }
                                                            }
                                                        }
                                                }
                                                else {
                                                    dt {
                                                        +"$text"
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