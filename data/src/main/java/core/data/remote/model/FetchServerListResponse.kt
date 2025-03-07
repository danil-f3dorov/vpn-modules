package core.data.remote.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import core.domain.model.Server


data class FetchServerListResponse @JsonCreator constructor(
    @JsonProperty("result") val result: Int,
    @JsonProperty("version") val version: String?,
    @JsonProperty("servers") val server: List<Server>
)