package idiocy.ui.palette

import java.awt.Color

trait Palette {
  val bgColor: Color
  val staffColor: Color
  val noteColor: Color
  val spanColor: Color
  val cursorColor: Color
  val cursorBlinkColor: Color
  val selectionBgColor: Color
  val name: String
}
