package com.example.lestari.RecycleView

import android.os.Parcel
import android.os.Parcelable

data class animal(
    val animalId: String?,
    var animal_picture: String?,
    val city: String?,
    val date: String?,
    val desc: String?,
    val latin_name: String?,
    val local_name: String?,
    val latitude: String?,
    val longitude: String?,
    val uid: String?,
): Parcelable {

    constructor(parcel: Parcel) : this(
    parcel.readString(),
    parcel.readString(),
    parcel.readString(),
    parcel.readString(),
    parcel.readString(),
    parcel.readString(),
    parcel.readString(),
    parcel.readString(),
    parcel.readString(),
    parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(animalId)
        parcel.writeString(animal_picture)
        parcel.writeString(city)
        parcel.writeString(date)
        parcel.writeString(desc)
        parcel.writeString(latin_name)
        parcel.writeString(local_name)
        parcel.writeString(latitude)
        parcel.writeString(longitude)
        parcel.writeString(uid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<animal> {
        override fun createFromParcel(parcel: Parcel): animal {
            return animal(parcel)
        }

        override fun newArray(size: Int): Array<animal?> {
            return arrayOfNulls(size)
        }
    }
}
data class addAnimal(
    val animal_picture: String?,
    val city: String?,
    val date: String?,
    val desc: String?,
    val latin_name: String?,
    val local_name: String?,
    val latitude: String?,
    val longitude: String?,
    val uid: String?,
)
