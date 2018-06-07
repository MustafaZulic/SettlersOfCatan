package io.swagslash.settlersofcatan.grid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Region;
import android.graphics.Shader;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.otaliastudios.zoom.ZoomLayout;

import java.util.ArrayList;
import java.util.List;

import io.swagslash.settlersofcatan.R;
import io.swagslash.settlersofcatan.SettlerApp;
import io.swagslash.settlersofcatan.controller.GameController;
import io.swagslash.settlersofcatan.pieces.Board;
import io.swagslash.settlersofcatan.pieces.Edge;
import io.swagslash.settlersofcatan.pieces.Hex;
import io.swagslash.settlersofcatan.pieces.Vertex;
import io.swagslash.settlersofcatan.pieces.utility.HexPoint;
import io.swagslash.settlersofcatan.utility.Pair;

/**
 * Created by thomas on 10.04.2018.
 */

public class HexView extends View {

    Board board;
    List<Hex> hexes;
    List<Region> regionList;
    WindowManager manager;
    int maxX;
    int maxY;
    int scale = 1;
    Pair<Integer, Integer> offset = null;
    List<Path> takenVertices;
    List<Path> freeVertices;
    List<Path> takenEdges;
    List<Path> freeEdges;
    ZoomLayout zoomLayout = null;
    //Hexagon paint
    Paint strokePaint;
    Paint fillPaint;
    // Vertex Paint
    Paint circlePaint;
    Paint edgePaint;
    Region clip;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleDetector;
    private Paint vertexClickPaint;

    public HexView(Context context) {
        super(context);

        hexes = new ArrayList<>();
        regionList = new ArrayList<>();

        this.strokePaint = new Paint();
        this.fillPaint = new Paint();

        this.gestureDetector = null;
        this.scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        this.vertexClickPaint = new Paint();
        vertexClickPaint.setColor(Color.WHITE);
        vertexClickPaint.setStyle(Paint.Style.STROKE);

        freeVertices = new ArrayList<>();
        takenVertices = new ArrayList<>();
        freeEdges = new ArrayList<>();
        takenEdges = new ArrayList<>();

        circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setStyle(Paint.Style.FILL);

        edgePaint = new Paint();
        edgePaint.setColor(Color.BLUE);
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(4);

    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public WindowManager getManager() {
        return manager;
    }

    public void setManager(WindowManager manager) {
        this.manager = manager;
    }

    public ZoomLayout getZoomLayout() {
        return zoomLayout;
    }

    public void setZoomLayout(ZoomLayout zoomLayout) {
        this.zoomLayout = zoomLayout;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void prepare() {
        hexes.addAll(board.getHexagons());

        Display mdisp = getManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        maxX = mdispSize.x;
        maxY = mdispSize.y;
        scale = Math.min(maxX, maxY) / 5;
        offset = new Pair<>(Math.min(maxX, maxY) / 2, Math.min(maxX, maxY) / 2);
        clip = new Region();

        for (Hex hex : hexes) {
            hex.calculatePath(offset, scale);
        }
        //TODO ADJUST MIN/MAX HEIGHT/WIDTH VIA PROPERTIES
        // OR GET IT FROM PARENT?
        setMinimumHeight(maxY);
        setMinimumWidth(maxX);

        // GENERATE PATHS und so
        for (Hex hex : hexes) {
            Path path = hex.getPath();
            Region r = new Region();
            r.setPath(path, clip);
            hex.setRegion(r);
        }

        generateVerticePaths();


        //ready to draw
        setWillNotDraw(false);
        invalidate();
    }

    public void generateVerticePaths() {
        for (Vertex v : board.getVerticesList()) {
            HexPoint drawPoint = v.getCoordinates().scale(offset, scale);
            v.calculatePath(offset, scale);
        }
    }

    protected void onDraw(Canvas c) {

        super.onDraw(c);

        clip.set(0, 0, c.getWidth(), c.getHeight());

        //Background white

        Paint bg = new Paint();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.water_texture);
        BitmapShader fillBMPshader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        bg.setShader(fillBMPshader);


        bg.setStyle(Paint.Style.FILL);
        //this.fillPaint.setColor(Color.parseColor("#138fdc"));
        c.drawPaint(bg);

        strokePaint.setStrokeWidth(3);
        strokePaint.setPathEffect(null);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStyle(Paint.Style.STROKE);

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.YELLOW);


        for (Hex hex : hexes) {
            Path path = hex.getPath();
            path.setFillType(Path.FillType.EVEN_ODD);
            fillPaint.setColor(hex.getTerrainColor());
            c.drawPath(path, fillPaint);
            c.drawPath(path, strokePaint);
            Region r = new Region();
            r.setPath(path, clip);
            hex.setRegion(r);

        }

        for (Hex h : board.getHexagons()) {
            for (Edge e : h.getEdges()) {
                HexPoint from = e.getCoordinates().first;
                HexPoint to = e.getCoordinates().first;
                HexPoint drawFrom = from.scale(offset, scale);
                HexPoint drawTo = to.scale(offset, scale);
                switch (e.getUnitType()) {
                    case ROAD:
                        edgePaint.setColor(e.getOwner().getColor());
                        c.drawLine((float) drawFrom.x, (float) drawFrom.y, (float) drawTo.x, (float) drawTo.y, edgePaint);
                        break;
                    case NONE:
                        //c.drawLine((float) drawFrom.x, (float) drawFrom.y, (float) drawTo.x, (float) drawTo.y, edgePaint);
                        break;
                    default:
                        break;
                }
            }
        }

        //TODO draw vertices

        for (Vertex vertex : board.getVerticesList()) {
            Region region = new Region();
            region.setPath(vertex.getPath(), clip);
            vertex.setRegion(region);
            switch (vertex.getUnitType()) {
                case NONE:
                    if (SettlerApp.board.getPhaseController().getCurrentPhase() == Board.Phase.SETUP_SETTLEMENT) {
                        c.drawPath(vertex.getPath(), vertexClickPaint);
                    }
                    break;
                case SETTLEMENT:
                    circlePaint.setColor(vertex.getOwner().getColor());
                    c.drawPath(vertex.getPath(), circlePaint);
                    break;
                case CITY:
                    circlePaint.setColor(vertex.getOwner().getColor());
                    c.drawPath(vertex.getPath(), circlePaint);
                    break;
            }

        }


    }

    public void showFreeSettlements() {
        redraw();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //TODO remove debug data and handle touches properly

        if (gestureDetector == null) {
            this.gestureDetector = new GestureDetector(this.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent event) {
                    // triggers first for both single tap and long press
                    //redraw();
                    return true;
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    handleVertexClick(e);
                    showHexDetailFromMotionEvent(e, "Single Tap");

                    return true;
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    showHexDetailFromMotionEvent(e, "Long Press");
                }

                // Keep this in for future use
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    showHexDetailFromMotionEvent(e, "Double Tap");
                    return false;
                }
            });
        }
        //detect touch
        gestureDetector.onTouchEvent(event);
        scaleDetector.onTouchEvent(event);


        return true;
    }

    private Pair<Integer, Integer> getCoordinates(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (zoomLayout != null) {
            // invert the coordinates so they map back to our regions when using a zoom engine
            //x = x * (1/zoomLayout.getEngine().getRealZoom()) - zoomLayout.getEngine().getPanX();
            //y = y * (1/ zoomLayout.getEngine().getRealZoom()) - zoomLayout.getEngine().getPanY();
        }
        return new Pair<>((int) x, (int) y);
    }

    private Hex getHexFromCoordinates(int x, int y) {
        for (int i = 0; i < hexes.size(); i++) {
            Region r = hexes.get(i).getRegion();
            if (r.contains(x, y)) {
                return hexes.get(i);
            }
        }
        return null;
    }

    private Vertex getVertexFromCoordinates(int x, int y) {
        final ArrayList<Vertex> vertices = new ArrayList<>(SettlerApp.board.getVerticesList());
        for (int i = 0; i < vertices.size(); i++) {
            Region r = vertices.get(i).getRegion();
            if (r.contains(x, y)) {
                return vertices.get(i);
            }
        }
        return null;
    }

    private void showHexDetailFromMotionEvent(MotionEvent event, String msg) {
        Pair<Integer, Integer> coordinates = getCoordinates(event);
        Hex hex = getHexFromCoordinates(coordinates.first, coordinates.second);
        if (hex == null) return;

        System.out.println(hex.toString());
        Toast.makeText(getContext().getApplicationContext(), hex.toString() + " ~ " + msg,
                Toast.LENGTH_SHORT).show();
    }

    private void handleVertexClick(MotionEvent event) {
        Pair<Integer, Integer> coordinates = getCoordinates(event);
        Vertex vertex = getVertexFromCoordinates(coordinates.first, coordinates.second);
        if (vertex == null) return;

        System.out.println(vertex.toString());
        Toast.makeText(getContext().getApplicationContext(), vertex.toString() + " ~ ",
                Toast.LENGTH_SHORT).show();
        switch (SettlerApp.board.getPhaseController().getCurrentPhase()) {

            case SETUP_SETTLEMENT:
                GameController.getInstance().buildSettlement(vertex, SettlerApp.getPlayer());
                SettlerApp.board.getPhaseController().setCurrentPhase(Board.Phase.PLAYER_TURN);
                generateVerticePaths();
                redraw();
                break;
            case FREE_SETTLEMENT:
                GameController.getInstance().buildFreeSettlement(vertex, SettlerApp.getPlayer());
                SettlerApp.board.getPhaseController().setCurrentPhase(Board.Phase.FREE_ROAD);
                generateVerticePaths();
                redraw();
                break;
            case SETUP_CITY:
                break;
            case PRODUCTION:
                break;
            case PLAYER_TURN:
                break;
            case MOVING_ROBBER:
                break;
            case TRADE_PROPOSED:
                break;
            case TRADE_RESPONDED:
                break;
            case FINISHED_GAME:
                break;
        }
    }

    public void redraw() {
        this.invalidate();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            redraw();
            return true;

        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            redraw();
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            redraw();
            return true;
        }
    }


}
