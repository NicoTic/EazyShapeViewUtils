import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat

/**
 *
 * 类名： com.travelsky.mrt.aic.track.view.horizontal.extension
 * 描述：
 * 日期：2019/8/27 ${HORE}:12
 * @author  jiangxq
 */
inline fun Context.withStyledAttributes(
        set: AttributeSet? = null,
        attrs: IntArray,
        @AttrRes defStyleAttr: Int = 0,
        @StyleRes defStyleRes: Int = 0,
        block: TypedArray.() -> Unit
) {
    val typedArray = obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes)
    try {
        typedArray.block()
    } finally {
        typedArray.recycle()
    }
}

@ColorInt
inline fun Context.getColorCompat(@ColorRes colorRes: Int) : Int {
    return ContextCompat.getColor(this, colorRes)
}