package common.data.mapper

import common.data.local.entity.ServerEntity
import common.domain.model.Server


fun ServerEntity.toModel(): Server {
    return Server(
        country = this.country,
        ip = this.ip,
        short = this.short,
        speed = this.speed,
        configData = this.configData,
        city = this.city,
        r = this.r
    )
}

fun Server.toEntity(): ServerEntity {
    return ServerEntity(
        country = this.country,
        ip = this.ip,
        short = this.short,
        speed = this.speed,
        configData = this.configData,
        city = this.city,
        r = this.r
    )
}