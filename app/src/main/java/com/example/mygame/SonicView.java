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
    private Bitmap[] frames;
    private int frameIndex = 0;
    private final int frameCount = 6;
    private int frameWidth;
    private int posX = 0;
    private int posY;
    private int speedX = 10;
    private boolean facingRight = true;
    private Thread animationThread;
    private boolean running = false;
    private boolean isMoving = false;
    private long lastFrameChangeTime = 0;
    private static final long FRAME_DELAY = 100; // 100ms entre cada frame

    // Para controlar o toque longo
    private boolean isTouching = false;

    public SonicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SonicView(Context context) {
        super(context);
        init();
    }

    private void init() {
        Bitmap spriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.sonic_walk);
        if (spriteSheet != null) {
            frameWidth = spriteSheet.getWidth() / frameCount;
            int frameHeight = spriteSheet.getHeight();
            frames = new Bitmap[frameCount];

            // Criando os frames normais
            for (int i = 0; i < frameCount; i++) {
                frames[i] = Bitmap.createBitmap(spriteSheet, i * frameWidth, 0, frameWidth, frameHeight);
            }

            posY = 200;
            startAnimation();
        }
    }

    private void startAnimation() {
        if (animationThread == null || !animationThread.isAlive()) {
            running = true;
            animationThread = new Thread(this);
            animationThread.start();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Decide a direção com base na posição do toque
                if (touchX < getWidth() / 2) {
                    facingRight = false;
                    speedX = -Math.abs(speedX); // Negativo para ir para esquerda
                } else {
                    facingRight = true;
                    speedX = Math.abs(speedX); // Positivo para ir para direita
                }
                isMoving = true;
                isTouching = true;
                return true;

            case MotionEvent.ACTION_UP:
                // Para de mover após alguns passos se não estiver em toque longo
                isTouching = false;
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void run() {
        while (running) {
            try {
                long currentTime = System.currentTimeMillis();

                // Só move se estiver no estado de movimento
                if (isMoving) {
                    // Atualiza a posição
                    posX += speedX;

                    // Verifica os limites da tela
                    if (posX <= 0) {
                        posX = 0;
                        // Opcionalmente, pode virar para direita quando atingir o limite esquerdo
                        // facingRight = true;
                        // speedX = Math.abs(speedX);
                    } else if (posX + frameWidth >= getWidth()) {
                        posX = getWidth() - frameWidth;
                        // Opcionalmente, pode virar para esquerda quando atingir o limite direito
                        // facingRight = false;
                        // speedX = -Math.abs(speedX);
                    }

                    // Avança para o próximo frame com base no tempo
                    if (currentTime - lastFrameChangeTime > FRAME_DELAY) {
                        frameIndex = (frameIndex + 1) % frameCount;
                        lastFrameChangeTime = currentTime;
                    }

                    // Se não estiver tocando a tela, para de andar após completar um ciclo
                    if (!isTouching && frameIndex == 0) {
                        isMoving = false;
                    }
                } else {
                    // Quando não está se movendo, mantém o frame 0 (posição de parado)
                    frameIndex = 0;
                }

                postInvalidate();
                Thread.sleep(16); // Aproximadamente 60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (frames != null && frameIndex < frames.length) {
            Bitmap currentFrame = frames[frameIndex];

            if (!facingRight) {
                // Usar um cache para estas imagens invertidas melhoraria o desempenho
                Matrix flipMatrix = new Matrix();
                flipMatrix.preScale(-1, 1);
                Bitmap flippedFrame = Bitmap.createBitmap(currentFrame, 0, 0,
                        currentFrame.getWidth(), currentFrame.getHeight(),
                        flipMatrix, false);
                canvas.drawBitmap(flippedFrame, posX, posY, null);
                flippedFrame.recycle(); // Liberar memória
            } else {
                canvas.drawBitmap(currentFrame, posX, posY, null);
            }
        }
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