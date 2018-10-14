package idiocy.ui.palette

import java.awt.Color

/**
  * By Javier Guerrero
  */
object NYX8 {
  val color00: Color = PaletteUtils.hexToColor(0x08141e) //darkest; gets paler
  val color01: Color = PaletteUtils.hexToColor(0x0f2a3f)
  val color02: Color = PaletteUtils.hexToColor(0x20394f)
  val color03: Color = PaletteUtils.hexToColor(0xf6d6bd) //palest; gets darker
  val color04: Color = PaletteUtils.hexToColor(0xc3a38a)
  val color05: Color = PaletteUtils.hexToColor(0x997577)
  val color06: Color = PaletteUtils.hexToColor(0x816271)
  val color07: Color = PaletteUtils.hexToColor(0x4e495f)
}

object NYX8Light extends Palette{
  override val bgColor: Color = NYX8.color03
  override val staffColor: Color = NYX8.color02
  override val noteColor: Color = NYX8.color00
  override val spanColor: Color = NYX8.color01
  override val cursorColor: Color = NYX8.color00
  override val cursorBlinkColor: Color = NYX8.color02
  override val selectionBgColor: Color = NYX8.color04
  override val name: String = "NYX8 Light"
}

object NYX8Dark extends Palette{
  override val bgColor: Color = NYX8.color00
  override val staffColor: Color = NYX8.color06
  override val noteColor: Color = NYX8.color03
  override val spanColor: Color = NYX8.color05
  override val cursorColor: Color = NYX8.color03
  override val cursorBlinkColor: Color = NYX8.color06
  override val selectionBgColor: Color = NYX8.color01
  override val name: String = "NYX8 Dark"
}
