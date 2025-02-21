package com.surendramaran.yolov8tflite

import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable

data class BoundingBox(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val cx: Float,
    val cy: Float,
    val w: Float,
    val h: Float,
    val cnf: Float,
    val cls: Int,
    val clsName: String
) : Parcelable {

    // Tạo RectF để dễ dàng vẽ lên Canvas
    val rect: RectF
        get() = RectF(x1, y1, x2, y2)

    // Viết Parcelable để truyền qua Intent
    constructor(parcel: Parcel) : this(
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(x1)
        parcel.writeFloat(y1)
        parcel.writeFloat(x2)
        parcel.writeFloat(y2)
        parcel.writeFloat(cx)
        parcel.writeFloat(cy)
        parcel.writeFloat(w)
        parcel.writeFloat(h)
        parcel.writeFloat(cnf)
        parcel.writeInt(cls)
        parcel.writeString(clsName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BoundingBox> {
        override fun createFromParcel(parcel: Parcel): BoundingBox {
            return BoundingBox(parcel)
        }

        override fun newArray(size: Int): Array<BoundingBox?> {
            return arrayOfNulls(size)
        }
    }
}
