package common.model

import android.os.Parcel
import android.os.Parcelable

data class ServerParcelable(
    var country: String,
    var ip: String,
    var short: String,
    var speed: String,
    var configData: String,
    var city: String,
    var r: Int? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readValue(Int::class.java.classLoader) as? Int
    )


    override fun writeToParcel(parcel: Parcel, flags: Int) {
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

    companion object CREATOR : Parcelable.Creator<ServerParcelable> {
        override fun createFromParcel(parcel: Parcel): ServerParcelable {
            return ServerParcelable(parcel)
        }

        override fun newArray(size: Int): Array<ServerParcelable?> {
            return arrayOfNulls(size)
        }
    }
}