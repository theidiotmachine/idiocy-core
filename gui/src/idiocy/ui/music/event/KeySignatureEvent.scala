package idiocy.ui.music.event
import upickle.default.{macroRW, ReadWriter => RW}
import idiocy.music.key.Key

object KeySignatureEvent{
  implicit def rw: RW[KeySignatureEvent] = macroRW
}
final case class KeySignatureEvent(key: Key) extends MusicEvent {
  override val lengthPips: Int = 0
}
