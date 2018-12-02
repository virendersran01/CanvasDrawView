package mk.android.com.canvasdrawview.presenter;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import mk.android.com.canvasdrawview.model.Shape;
import mk.android.com.canvasdrawview.model.Shape.Type;
import mk.android.com.canvasdrawview.view.CustomView;

/**
 * Created by Mayuri Khinvasara on 02,December,2018
 */
public class ShapesPresenter {
    private static final String LOG_TAG = "canvas123";
    public static final int TOTAL_SHAPES = 3;
    private final CustomView canvas;
    public static final int RADIUS = 60;
    private int maxX;
    private int maxY;
    private static LinkedList<Shape> historyList = new LinkedList<>();
    private int actionSequence = 0;

    public ShapesPresenter(CustomView canvas) {
        this.canvas = canvas;
        canvas.setCanvasTouch(onTouchListener);
    }

    CanvasTouch onTouchListener = new CanvasTouch() {
        @Override
        public void onTouchEvent(MotionEvent event) {
            handleTouch(event);
        }
    };

    private void handleTouch(MotionEvent event) {
        int touchX = Math.round(event.getX());
        int touchY = Math.round(event.getY());
        //   Toast.makeText(this.getContext(), " Touch at " + touchX + " y= " + touchY, Toast.LENGTH_SHORT).show();
        int X1, Y1;
        double Dis = Integer.MAX_VALUE;

        LinkedList<Shape> list = getHistoryList();
        Shape newShape = null;
        for (int i = 0; i < list.size(); i++) {
            Shape oldShape = list.get(i);
            if(oldShape.isVisible()) {
                X1 = oldShape.getX();
                Y1 = oldShape.getY();

                if (RADIUS >= calculateDistanceBetweenPoints(X1, Y1, touchX, touchY)) {
                    tranformShape(oldShape, i, X1,Y1);
                    break;
                }
            }
        }
    }

    private void tranformShape(Shape oldShape, int index,int newX, int newY) {
        Log.d(LOG_TAG, " oldShape =  " + oldShape.getType());
        oldShape.setVisibility(false);
        getHistoryList().set(index, oldShape);
        Log.d(LOG_TAG, " HIDE oldShape =  " + oldShape.getType());
        //transorm object
        int newShapeType = (oldShape.getType().value + 1) % TOTAL_SHAPES;
        Type newshapeType = Type.values()[newShapeType];
        Log.d(LOG_TAG, " newshape =  " + newshapeType);

        Shape newShape = createShape(newshapeType, newX, newY);
        newShape.setLastTranformIndex(index);
        upDateCanvas(newShape);
    }

    public double calculateDistanceBetweenPoints(
            double x1,
            double y1,
            double x2,
            double y2) {
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    private int[] generateRandomXAndY() {
        int x,y;
        Random rn = new Random();
        int diff = maxX - RADIUS;
        x = rn.nextInt(diff + 1);
        x += RADIUS;

        rn = new Random();
        diff = maxY - RADIUS;
        y = rn.nextInt(diff + 1);
        y += RADIUS;
        Log.d(LOG_TAG, " Random x= " + x + "y" + y);
        return new int[]{x, y};
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public void addShapeRandom(Type type ) {
        int[] randomCordinates = generateRandomXAndY();
        Shape shape = createShape(type, randomCordinates[0], randomCordinates[1]);
        upDateCanvas(shape);
    }

    @NonNull
    private Shape createShape(Type type, int x, int y) {

        Shape shape = new Shape(x, y, RADIUS);
      //  historyList.add(new Circle(x,y,50));
        shape.setType(type);
        return shape;
    }

   /* private void updateShapeCount(Type type,  int actionShape) {
        int existingCnt =0 ;
        if( getShapeTypeCountMap().containsKey(type))
            existingCnt =  getShapeTypeCountMap().get(type);
        if(actionShape == ACTION_SHAPE_CREATE)
            existingCnt++;
        else
            existingCnt--;
        getShapeTypeCountMap().put(type,existingCnt);
    }*/

    public void undo() {

        if (getHistoryList().size() > 0) {
            actionSequence--;
            Shape toDeleteShape = getHistoryList().getLast();
            if(toDeleteShape.getLastTranformIndex() != Shape.STATE_CREATE) {
                int lastVisibleIndex = toDeleteShape.getLastTranformIndex();
                Shape lastVisibleShape = getHistoryList().get(lastVisibleIndex);
                lastVisibleShape.setVisibility(true);
                getHistoryList().set(lastVisibleIndex, lastVisibleShape);
            }
            getHistoryList().removeLast();
            canvas.setHistoryList(getHistoryList());
            canvas.invalidate();
        }
    }


    private void upDateCanvas(Shape shape) {
        Log.d(LOG_TAG, " upDateCanvas " + shape.getType() + " actiontype = "+ actionSequence);
        shape.setActionNumber(actionSequence++);
        getHistoryList().add(shape);
        canvas.setHistoryList(getHistoryList());
        canvas.invalidate();
    }

    public LinkedList<Shape> getHistoryList() {
        return historyList;
    }

    public void setHistoryList(LinkedList<Shape> historyList) {
        this.historyList = historyList;
    }

    public HashMap<Type, Integer> getCountByGroup()
    {
        HashMap<Type,Integer> shapeTypeCountMap = new HashMap<>();
        for(Shape shape : getHistoryList())
        {
            if(shape.isVisible())
            {
                Type shapeType = shape.getType();
                int existingCnt = 0;
                if( shapeTypeCountMap.containsKey(shape.getType()))
                    existingCnt =  shapeTypeCountMap.get(shape.getType());
                existingCnt++;
                shapeTypeCountMap.put(shapeType,existingCnt);
            }
        }
        return shapeTypeCountMap;
    }
}
