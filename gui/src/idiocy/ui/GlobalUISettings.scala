package idiocy.ui

import java.awt.{Color, Font}

import idiocy.ui.palette.{NYX8Light, Palette, SolarizedLight}

object GlobalUISettings{
  var palette: Palette = SolarizedLight
  def setPalette(p: Palette): Unit = palette = p

  var noteFont: Font = null

}
