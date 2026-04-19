package dev.msartore.ares.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.msartore.ares.models.NetworkDiscoveryService
import dev.msartore.ares.models.NetworkInfo
import dev.msartore.ares.utils.findFreePort
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient =
        HttpClient(CIO) {
            install(HttpTimeout)
        }

    @Provides
    @Singleton
    fun provideNetworkInfo(): NetworkInfo = NetworkInfo()

    @Provides
    @Singleton
    fun provideNsdServersFlow(): MutableSharedFlow<android.net.nsd.NsdServiceInfo?> =
        MutableStateFlow(null)

    @Provides
    @Singleton
    fun provideNetworkDiscoveryService(
        nsdFlow: MutableSharedFlow<android.net.nsd.NsdServiceInfo?>
    ): NetworkDiscoveryService =
        NetworkDiscoveryService(nsdFlow)
}