package idiocy.ui.palette

import java.awt.Color

import idiocy.ui.GlobalUISettings

object ENOS16{
  val trueWhite: Color = Color.WHITE //not part of the palette, but notes indicate to use it
  val white: Color = PaletteUtils.hexToColor(0xfafafa)
  val paleGrey: Color = PaletteUtils.hexToColor(0xd4d4d4)
  val darkGrey: Color = PaletteUtils.hexToColor(0x9d9d9d)
  val black: Color = PaletteUtils.hexToColor(0x4b4b4b)
  val lightYellow: Color = PaletteUtils.hexToColor(0xf9d381)
  val darkYellow: Color = PaletteUtils.hexToColor(0xeaaf4d)
  val lightRed: Color = PaletteUtils.hexToColor(0xf9938a)
  val darkRed: Color = PaletteUtils.hexToColor(0xe75952)
  val lightBlue: Color = PaletteUtils.hexToColor(0x9ad1f9)
  val darkBlue: Color = PaletteUtils.hexToColor(0x58aeee)
  val lightGreen: Color = PaletteUtils.hexToColor(0x8deda7)
  val darkGreen: Color = PaletteUtils.hexToColor(0x44c55b)
  val lightPurple: Color = PaletteUtils.hexToColor(0xc3a7e1)
  val darkPurple: Color = PaletteUtils.hexToColor(0x9569c8)
  val lightBrown: Color = PaletteUtils.hexToColor(0xbab5aa)
  val darkBrown: Color = PaletteUtils.hexToColor(0x948e82)
}

object ENOS16Light extends Palette{
  override val bgColor: Color = ENOS16.trueWhite
  override val staffColor: Color = ENOS16.darkGrey
  override val noteColor: Color = ENOS16.black
  override val spanColor: Color = ENOS16.black
  override val cursorColor: Color = ENOS16.black
  override val cursorBlinkColor: Color = ENOS16.paleGrey
  override val selectionBgColor: Color = ENOS16.lightYellow
  override val name: String = "ENOS16 Light"
}
