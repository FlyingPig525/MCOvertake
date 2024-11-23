package io.github.flyingpig525.data

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Config(
    @EncodeDefault val serverAddress: String = "0.0.0.0",
    @EncodeDefault val serverPort: Int = 25565)