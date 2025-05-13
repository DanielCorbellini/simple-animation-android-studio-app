package com.example.mygame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SonicView extends View implements Runnable {

    // Frame de corrida
    private Bitmap[] frames;
    // Frame parado
    private Bitmap idleFrame;

    private int frameIndex = 0;
    private final int frameCount = 6;
    private int frameWidth;
    private int frameHeight;
    private int posX = 0;
    private int posY;
    private int targetX = 0;
    private int targetY = 200;
    private int speed = 40;
    private boolean facingRight = true;
    private Thread animationThread;
    private boolean running = false;

    public SonicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Bitmap spriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.sonic_walk);
        frameWidth = spriteSheet.getWidth() / frameCount;
        frameHeight = spriteSheet.getHeight();

        // Carrega os frames de movimento
        frames = new Bitmap[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = Bitmap.createBitmap(spriteSheet, i * frameWidth, 0, frameWidth, frameHeight);
        }

        // Carrega o frame parado
        idleFrame = BitmapFactory.decodeResource(getResources(), R.drawable.sonic_stationary);

        posY = 200;
        targetX = posX;
        targetY = posY;
        startAnimation();
    }


    private void startAnimation() {
        if (animationThread == null || !animationThread.isAlive()) {
            running = true;
            animationThread = new Thread(this);
            animationThread.start();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                // Verifica se precisa mover no eixo X
                if (Math.abs(posX - targetX) > speed) {
                    if (posX < targetX) {
                        posX += speed;
                        facingRight = true;
                    } else if (posX > targetX) {
                        posX -= speed;
                        facingRight = false;
                    }
                } else {
                    posX = targetX; // Ajusta para a posição exata
                }

                // Verifica se precisa mover no eixo Y
                if (Math.abs(posY - targetY) > speed) {
                    if (posY < targetY) {
                        posY += speed;
                    } else if (posY > targetY) {
                        posY -= speed;
                    }
                } else {
                    posY = targetY; // Ajusta para a posição exata
                }

                // Atualiza o frame apenas se estiver se movendo
                if (posX != targetX || posY != targetY) {
                    frameIndex = (frameIndex + 1) % frameCount;
                }

                // Redesenha a tela
                postInvalidate();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap currentFrame;
        if (posX == targetX && posY == targetY) {
            currentFrame = idleFrame;
        } else {
            currentFrame = frames[frameIndex];
        }

        // Espelha o frame se necessário
        if (!facingRight && posX != targetX) {
            Matrix flipMatrix = new Matrix();
            flipMatrix.preScale(-1, 1);
            currentFrame = Bitmap.createBitmap(currentFrame, 0, 0, currentFrame.getWidth(), currentFrame.getHeight(), flipMatrix, false);
        }

        canvas.drawBitmap(currentFrame, posX, posY, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            targetX = (int) event.getX() - frameWidth / 2;
            targetY = (int) event.getY() - frameHeight / 2;
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        running = false;
        if (animationThread != null) {
            animationThread.interrupt();
        }
    }
}