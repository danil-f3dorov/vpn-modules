package common.util.extensions

import common.domain.model.Server
import common.model.ServerParcelable

fun Server.toParcelable(): ServerParcelable {
    return ServerParcelable(
        this.country,
        this.ip,
        this.short,
        this.speed,
        this.configData,
        this.city,
        this.r
    )
}

fun ServerParcelable.toServer(): Server {
    return Server(
        this.country,
        this.ip,
        this.short,
        this.speed,
        this.configData,
        this.city,
        this.r
    )
}