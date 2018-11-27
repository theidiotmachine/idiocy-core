package idiocy.ui.music

import idiocy.ui.music.event.MusicNote

class StemCalculator {
  var outerUp: Int = -1
  var outerDown: Int = -1
  var numUp: Int = 0
  var numDown: Int = 0
  var numCentre: Int = 0
  var num: Int = 0

  def addNote(musicNote: MusicNote, middleLine: Int): Unit = {
    if(musicNote.barLocation > middleLine){
      numUp += 1
      if(musicNote.barLocation - middleLine > outerUp)
        outerUp = musicNote.barLocation - middleLine
    } else if(musicNote.barLocation < middleLine){
      numDown += 1
      if(middleLine - musicNote.barLocation > outerDown)
        outerDown = middleLine - musicNote.barLocation
    } else
      numCentre += 1
    num += 1
  }

  var stemUp: Boolean = false

  def calculate(): Unit = {
    if(num == 1){
      if(numCentre == 1 || numUp == 1)
        stemUp = false
      else
        stemUp = true
    } else if(num == 2){
      if(outerUp >= outerDown)
        stemUp = false
      else
        stemUp = true
    } else {
      //3 or more
      if(outerUp > outerDown)
        stemUp = false
      else if(outerUp < outerDown)
        stemUp = true
      else{
        if(numUp >= numDown)
          stemUp = false
        else
          stemUp = true
      }
    }
  }
}
