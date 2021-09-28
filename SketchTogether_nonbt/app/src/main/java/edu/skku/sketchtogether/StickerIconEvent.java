package edu.skku.sketchtogether;

import android.view.MotionEvent;

public interface StickerIconEvent {
  void onActionDown(edu.skku.sketchtogether.StickerView stickerView, MotionEvent event);

  void onActionMove(edu.skku.sketchtogether.StickerView stickerView, MotionEvent event);

  void onActionUp(edu.skku.sketchtogether.StickerView stickerView, MotionEvent event);
}
