package edu.skku.sketchtogether;

public class FlipVerticallyEvent extends AbstractFlipEvent {

  @Override @edu.skku.sketchtogether.StickerView.Flip protected int getFlipDirection() {
    return edu.skku.sketchtogether.StickerView.FLIP_VERTICALLY;
  }
}
