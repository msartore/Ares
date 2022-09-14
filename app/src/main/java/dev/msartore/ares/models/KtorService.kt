package dev.msartore.ares.models

import android.app.*
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import dev.msartore.ares.MainActivity
import dev.msartore.ares.R
import dev.msartore.ares.models.KtorService.KtorServer.CHANNEL_ID
import dev.msartore.ares.models.KtorService.KtorServer.ONGOING_NOTIFICATION_ID
import dev.msartore.ares.models.KtorService.KtorServer.PORT
import dev.msartore.ares.models.KtorService.KtorServer.concurrentMutableList
import dev.msartore.ares.models.KtorService.KtorServer.isServerOn
import dev.msartore.ares.models.KtorService.KtorServer.server
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.html.*

class KtorService: Service() {

    object KtorServer {
        const val ONGOING_NOTIFICATION_ID = 123
        const val CHANNEL_ID = "server_id"
        const val PORT = 7070

        val concurrentMutableList = ConcurrentMutableList<FileData>()
        val isServerOn = mutableStateOf(false)

        var server: ApplicationEngine? = null
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
                get("/") {
                    call.respondHtml {
                        head {
                            style {
                                +"a { color:black; } .file { margin:10px; }"
                            }
                        }
                        body {
                            h1 {
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
                                                +"${getString(R.string.size)}: ${"%.2f".format((concurrentMutableList.list.elementAt(i).size ?: 1)/1000000.0)}MB"
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