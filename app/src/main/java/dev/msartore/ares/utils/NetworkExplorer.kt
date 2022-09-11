package dev.msartore.ares.utils

import android.content.Context
import android.widget.Toast
import dev.msartore.ares.MainActivity.MActivity.ipSearchData
import dev.msartore.ares.MainActivity.MActivity.networkInfo
import dev.msartore.ares.R
import dev.msartore.ares.models.KtorService
import dev.msartore.ares.models.KtorService.KtorServer.PORT
import dev.msartore.ares.models.Settings
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

fun Context.findServers(
    settings: Settings?
) {

    if (ipSearchData.isSearching.value == 0) {

        val list = networkInfo.ipAddress.value.split(".")

        ipSearchData.apply {
            ipList.clear()
            isSearching.value = 1
            ipLeft.value = 254

            if (list.size == 4) {
                val firstThree = "${list[0]}.${list[1]}.${list[2]}."
                val last = list.last()

                if (last.isNotEmpty()) {

                    job = work {
                        for (i in 1..254) {

                            if (job.isCancelled)
                                break

                            val ip = firstThree + i

                            ipLeft.value--

                            if (!last.contains(i.toString()))
                                runCatching {
                                    val client = Socket()
                                    client.connect(InetSocketAddress(ip, PORT), settings?.ipTimeout?.value ?: 150)
                                        ipSearchData.ipList.add(firstThree + i)
                                }
                        }

                        isSearching.value--
                    }
                }
            }
        }
    }
    else
        Toast.makeText(this, getString(R.string.wait_still_searching), Toast.LENGTH_SHORT).show()
}