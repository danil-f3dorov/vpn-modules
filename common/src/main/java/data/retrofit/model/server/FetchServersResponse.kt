package data.retrofit.model.server

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import data.room.entity.Server

data class FetchServersResponse @JsonCreator constructor(
    @JsonProperty("result") val result: Int,
    @JsonProperty("version") val version: String?,
    @JsonProperty("servers") val servers: List<Server>
)