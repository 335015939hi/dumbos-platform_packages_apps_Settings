package com.android.settings.password.generate

import android.os.Parcel
import android.os.Parcelable
import com.android.internal.widget.PasswordValidationError

/**
 * A Parcelable version of [PasswordValidationError] so that errors can be passed
 */
data class AOSPPasswordValidationError(val errorCode: Int, val requirement: Int) : Parcelable {
    fun toError() = PasswordValidationError(errorCode, requirement)

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(errorCode)
        parcel.writeInt(requirement)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AOSPPasswordValidationError> {
        @JvmStatic
        fun fromList(errors: List<PasswordValidationError>): Array<AOSPPasswordValidationError> {
            return Array(errors.size) { i ->
                AOSPPasswordValidationError(errors[i].errorCode, errors[i].requirement)
            }
        }

        override fun createFromParcel(parcel: Parcel): AOSPPasswordValidationError {
            return AOSPPasswordValidationError(parcel)
        }

        override fun newArray(size: Int): Array<AOSPPasswordValidationError?> {
            return arrayOfNulls(size)
        }
    }
}
