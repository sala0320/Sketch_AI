package edu.skku.sketchtogether;

import android.view.MotionEvent;

public abstract class AbstractFlipEvent implements edu.skku.sketchtogether.StickerIconEvent {

  @Override public void onActionDown(edu.skku.sketchtogether.StickerView stickerView, MotionEvent event) {

  }

  @Override public void onActionMove(edu.skku.sketchtogether.StickerView stickerView, MotionEvent event) {

  }

  @Override public void onActionUp(edu.skku.sketchtogether.StickerView stickerView, MotionEvent event) {
    stickerView.flipCurrentSticker(getFlipDirection());
  }

  @edu.skku.sketchtogether.StickerView.Flip protected abstract int getFlipDirection();
}
