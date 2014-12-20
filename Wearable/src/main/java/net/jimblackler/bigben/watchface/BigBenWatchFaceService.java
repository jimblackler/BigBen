/*
 * Copyright (C) 2014 Jim Blackler  jimblackler@gmail.com
 * Licensed under the Apache License, Version 2.0 (see LICENSE)
 */

package net.jimblackler.bigben.watchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


public class BigBenWatchFaceService extends CanvasWatchFaceService {
  private class Engine extends CanvasWatchFaceService.Engine {
    static final int MSG_UPDATE_TIME = 0;

    Bitmap background;
    Bitmap minuteHand;
    Bitmap hourHand;

    Bitmap backgroundScaled;
    Bitmap minuteHandScaled;
    Bitmap hourHandScaled;

    Bitmap backgroundAmbient;
    Bitmap minuteHandAmbient;
    Bitmap hourHandAmbient;

    Bitmap backgroundScaledAmbient;
    Bitmap minuteHandScaledAmbient;
    Bitmap hourHandScaledAmbient;

    Paint handsPaint;

    boolean mute;
    boolean registeredTimeZoneReceiver = false;
    Time time;

    final BroadcastReceiver timeZoneReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        time.clear(intent.getStringExtra("time-zone"));
        time.setToNow();
      }
    };
    final Handler updateTimeHandler = new Handler() {
      @Override
      public void handleMessage(Message message) {
        if (message.what != MSG_UPDATE_TIME)
          return;
        invalidate();
        if (!timerShouldBeRunning())
          return;
        long updateRate = TimeUnit.SECONDS.toMillis(3);
        long timeMs = System.currentTimeMillis();
        long delayMs = updateRate - (timeMs % updateRate);
        updateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
      }
    };

    @Override
    public void onAmbientModeChanged(boolean inAmbientMode) {
      super.onAmbientModeChanged(inAmbientMode);
      invalidate();
      updateTimer();
    }

    @Override
    public void onCreate(SurfaceHolder holder) {
      super.onCreate(holder);

      setWatchFaceStyle(new WatchFaceStyle.Builder(BigBenWatchFaceService.this)
          .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
          .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
          .setViewProtection(WatchFaceStyle.PROTECT_STATUS_BAR |
              WatchFaceStyle.PROTECT_HOTWORD_INDICATOR)
          .build());

      background = ((BitmapDrawable)
          getResources().getDrawable(R.drawable.bb_face)).getBitmap();
      hourHand = ((BitmapDrawable)
          getResources().getDrawable(R.drawable.bb_hour_hand)).getBitmap();
      minuteHand = ((BitmapDrawable)
          getResources().getDrawable(R.drawable.bb_minute_hand)).getBitmap();
      backgroundAmbient = ((BitmapDrawable)
          getResources().getDrawable(R.drawable.bb_face_ambient)).getBitmap();
      hourHandAmbient = ((BitmapDrawable)
          getResources().getDrawable(R.drawable.bb_hour_hand_ambient)).getBitmap();
      minuteHandAmbient = ((BitmapDrawable)
          getResources().getDrawable(R.drawable.bb_minute_hand_ambient)).getBitmap();

      handsPaint = new Paint();
      handsPaint.setFilterBitmap(true);
      handsPaint.setAntiAlias(true);

      time = new Time();
    }

    @Override
    public void onDestroy() {
      updateTimeHandler.removeMessages(MSG_UPDATE_TIME);
      super.onDestroy();
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
      time.setToNow();

      int width = bounds.width();
      int height = bounds.height();

      float ratio = (float) width / background.getWidth();

      if (backgroundScaled == null
          || backgroundScaled.getWidth() != width
          || backgroundScaled.getHeight() != height) {
        backgroundScaled =
            Bitmap.createScaledBitmap(background,
                (int) (background.getWidth() * ratio),
                (int) (background.getHeight() * ratio), true);
        minuteHandScaled = Bitmap.createScaledBitmap(minuteHand,
            (int) (minuteHand.getWidth() * ratio),
            (int) (minuteHand.getHeight() * ratio), true);
        hourHandScaled = Bitmap.createScaledBitmap(hourHand,
            (int) (hourHand.getWidth() * ratio),
            (int) (hourHand.getHeight() * ratio), true);

        backgroundScaledAmbient =
            Bitmap.createScaledBitmap(backgroundAmbient,
                (int) (backgroundAmbient.getWidth() * ratio),
                (int) (backgroundAmbient.getHeight() * ratio), true);
        minuteHandScaledAmbient = Bitmap.createScaledBitmap(minuteHandAmbient,
            (int) (minuteHandAmbient.getWidth() * ratio),
            (int) (minuteHandAmbient.getHeight() * ratio), true);
        hourHandScaledAmbient = Bitmap.createScaledBitmap(hourHandAmbient,
            (int) (hourHandAmbient.getWidth() * ratio),
            (int) (hourHandAmbient.getHeight() * ratio), true);
      }
      canvas.drawBitmap(isInAmbientMode() ? backgroundScaledAmbient : backgroundScaled, 0, 0, null);

      float centerX = width / 2f;
      float centerY = height / 2f;

      float minuteRotation = time.minute / 30f * (float) Math.PI;
      float hourRotation = ((time.hour + time.minute / 60f) / 6f) * (float) Math.PI;

      // Shadow angle needs to be counter-rotated to compensate for canvas rotation.
      float shadowX = 4.0f;
      float shadowY = 4.0f;
      int shadowColor = 0x40000000;

      // Hour hand. (maybe not bother pre-scaling)
      float hourCenterX = 0.51f;
      float hourCenterY = 0.665f;
      canvas.save();
      canvas.translate(centerX, centerY);
      canvas.rotate(hourRotation / (float) Math.PI * 180);
      canvas.translate(- hourCenterX * hourHandScaled.getWidth(),
          - hourCenterY * hourHandScaled.getHeight());
      if (isInAmbientMode())
        handsPaint.clearShadowLayer();
      else
        handsPaint.setShadowLayer(50.0f,
            (float) (Math.cos(-hourRotation) * shadowX - Math.sin(-hourRotation) * shadowY),
            (float) (Math.cos(-hourRotation) * shadowY + Math.sin(-hourRotation) * shadowX),
            shadowColor);
      canvas.drawBitmap(isInAmbientMode() ? hourHandScaledAmbient : hourHandScaled,
                        0, 0, handsPaint);
      canvas.restore();

      // Minute hand.
      minuteRotation -= .42;
      float minuteCenterX = 0.34f;
      float minuteCenterY = 0.79f;
      canvas.save();
      canvas.translate(centerX, centerY);
      canvas.rotate(minuteRotation / (float) Math.PI * 180);
      canvas.translate(-minuteCenterX * minuteHandScaled.getWidth(),
          -minuteCenterY * minuteHandScaled.getHeight());
      if (isInAmbientMode())
        handsPaint.clearShadowLayer();
      else
        handsPaint.setShadowLayer(50.0f,
            (float) (Math.cos(-minuteRotation) * shadowX - Math.sin(-minuteRotation) * shadowY),
            (float) (Math.cos(-minuteRotation) * shadowY + Math.sin(-minuteRotation) * shadowX),
            shadowColor);
      canvas.drawBitmap(isInAmbientMode() ? minuteHandScaledAmbient: minuteHandScaled,
                        0, 0, handsPaint);
      canvas.restore();
    }

    @Override
    public void onInterruptionFilterChanged(int interruptionFilter) {
      super.onInterruptionFilterChanged(interruptionFilter);
      boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);
      if (mute == inMuteMode)
        return;
      mute = inMuteMode;
      invalidate();
    }

    @Override
    public void onTimeTick() {
      super.onTimeTick();
      invalidate();
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
      super.onVisibilityChanged(visible);

      if (visible) {
        registerReceiver();
        time.clear(TimeZone.getDefault().getID());
        time.setToNow();
      } else {
        unregisterReceiver();
      }

      updateTimer();
    }

    private void registerReceiver() {
      if (registeredTimeZoneReceiver)
        return;

      registeredTimeZoneReceiver = true;
      BigBenWatchFaceService.this.registerReceiver(timeZoneReceiver,
          new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));
    }

    private boolean timerShouldBeRunning() {
      return isVisible() && !isInAmbientMode();
    }

    private void unregisterReceiver() {
      if (!registeredTimeZoneReceiver)
        return;
      registeredTimeZoneReceiver = false;
      BigBenWatchFaceService.this.unregisterReceiver(timeZoneReceiver);
    }

    private void updateTimer() {
      updateTimeHandler.removeMessages(MSG_UPDATE_TIME);
      if (!timerShouldBeRunning())
        return;
      updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
    }
  }

  @Override
  public Engine onCreateEngine() {
    return new Engine();
  }
}
