package v3anom.speechpaint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View{

    public int width;
    public int height;
    private int mBackgroundColor;
    private int mDrawColor;
    private int mEraserTemp;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private float mX, mY;
    private static final float TOLERRANCE = 5;
    Context context;


    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        mDrawColor = Color.BLACK;
        mBackgroundColor = Color.WHITE;

        mPath = new Path();

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(4f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(mBackgroundColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(mPath, mPaint);
    }

    private  void  startTouch(float x, float y){
        mPath.moveTo(x, y);

        mX = x;
        mY = y;
    }

    private void  moveTouch(float x, float y){
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if(dx >= TOLERRANCE || dy >= TOLERRANCE){
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    public void clearCanvas(){
        mPath.reset();
        invalidate();
    }

    private void upTouch(){
        mPath.lineTo(mX, mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                invalidate();
                break;
        }
        return true;

    }

    public void drawLine(float x1, float y1, float x2, float y2) {
        mPath.moveTo(x1, y1);
        mPath.lineTo(x2, y2);
        invalidate();
    }

    public void  drawCircle(float x, float y, float radius){
        mPath.moveTo(x,y);
        mPath.addCircle(x,y,radius, Path.Direction.CW);
        invalidate();
    }

    public  void changeBackgroundColor(int color){
        mBackgroundColor = color;
    }

    public void  changeForegroundColor(int color){
        mDrawColor = color;
        mPaint.setColor(mDrawColor);
    }

    public void startEraser(){
        mEraserTemp = mPaint.getColor();
        mPaint.setColor(mBackgroundColor);
    }

    public void stopEraser(){
        mPaint.setColor(mEraserTemp);
    }
}
