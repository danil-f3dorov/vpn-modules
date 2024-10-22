package common.domain.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Server(
    @JsonProperty("country") var country: String,
    @JsonProperty("ip") var ip: String,
    @JsonProperty("short") var short: String,
    @JsonProperty("speed") var speed: String,
    @JsonProperty("configData") var configData: String,
    @JsonProperty("city") var city: String,
    @JsonProperty("r") var r: Int? = null
)