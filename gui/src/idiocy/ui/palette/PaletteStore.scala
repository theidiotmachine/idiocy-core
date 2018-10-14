package idiocy.ui.palette

object PaletteStore {
  private val palettes = Map(
    (ENOS16Light.name, ENOS16Light),
    (SolarizedLight.name, SolarizedLight),
    (NYX8Light.name, NYX8Light),
    (NYX8Dark.name, NYX8Dark)
  )
  def getPaletteNames: Array[String] = {
    palettes.keys.toArray
  }

  def getPaletteByName(name: String): Palette = palettes(name)
}
