package common.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class Server @JsonCreator constructor(
    @JsonProperty("id") var id: Int? = null,
    @JsonProperty("country") var country: String,
    @JsonProperty("ip") var ip: String,
    @JsonProperty("short") var short: String,
    @JsonProperty("speed") var speed: String,
    @JsonProperty("configData") var configData: String,
    @JsonProperty("city") var city: String,
    @JsonProperty("r") var r: Int? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(country)
        parcel.writeString(ip)
        parcel.writeString(short)
        parcel.writeString(speed)
        parcel.writeString(configData)
        parcel.writeString(city)
        parcel.writeValue(r)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Server> {
        override fun createFromParcel(parcel: Parcel): Server {
            return Server(parcel)
        }

        override fun newArray(size: Int): Array<Server?> {
            return arrayOfNulls(size)
        }
    }
}