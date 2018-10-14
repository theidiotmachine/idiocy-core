package idiocy.ui.palette

import java.awt.Color

object PaletteUtils {
  def hexToColor(hex: Int): Color = {
    var r = (hex >> 16) & 255
    var g = (hex >> 8) & 255
    var b = hex & 255

    new Color(r, g , b)
  }
}
