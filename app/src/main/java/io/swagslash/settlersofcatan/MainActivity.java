package io.swagslash.settlersofcatan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esotericsoftware.kryonet.Connection;
import com.otaliastudios.zoom.ZoomEngine;
import com.otaliastudios.zoom.ZoomLayout;

import java.util.Random;

import io.swagslash.settlersofcatan.controller.GameController;
import io.swagslash.settlersofcatan.controller.TurnController;
import io.swagslash.settlersofcatan.controller.actions.DiceRollAction;
import io.swagslash.settlersofcatan.controller.actions.EdgeBuildAction;
import io.swagslash.settlersofcatan.controller.actions.GameAction;
import io.swagslash.settlersofcatan.controller.actions.TurnAction;
import io.swagslash.settlersofcatan.controller.actions.VertexBuildAction;
import io.swagslash.settlersofcatan.grid.HexView;
import io.swagslash.settlersofcatan.network.wifi.AbstractNetworkManager;
import io.swagslash.settlersofcatan.network.wifi.INetworkCallback;
import io.swagslash.settlersofcatan.pieces.Board;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, INetworkCallback {

    private final int FAB_MENU_DISTANCE = 145;

    protected Button cards;
    protected ImageButton dice, endOfTurn, trading;
    protected FloatingActionButton fab, fabSettlement, fabCity, fabStreet;
    protected LinearLayout layoutSettlement, layoutCity, layoutStreet;
    protected Animation openMenu, closeMenu;

    HexView hexView;
    private Board board;
    private AbstractNetworkManager network;

    //protected ArrayList<FloatingActionButton> fabOptions;
    protected boolean fabOpen;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setupHexView();
        network = SettlerApp.getManager();
        network.switchIn(this);
        //setContentView(R.layout.activity_main);


        TabLayout tabs = findViewById(R.id.tabs);
        for (View view : tabs.getTouchables()) {
            view.setClickable(false);
        }

        //fab menu animation
        this.fabOpen = false;
        openMenu = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.open_menu);
        closeMenu = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.close_menu);

        //fabs
        this.fab = findViewById(R.id.fab_build_options);

        this.fabSettlement = findViewById(R.id.fab_settlement);
        //this.fabOptions.add(this.fabSettlement);
        this.layoutSettlement = findViewById(R.id.layout_settlement);
        this.layoutSettlement.setVisibility(View.INVISIBLE);

        this.fabCity = findViewById(R.id.fab_city);
        //this.fabOptions.add(this.fabCity);
        this.layoutCity = findViewById(R.id.layout_city);
        this.layoutCity.setVisibility(View.INVISIBLE);

        this.fabStreet = findViewById(R.id.fab_street);
        //this.fabOptions.add(this.fabStreet);
        this.layoutStreet = findViewById(R.id.layout_street);
        this.layoutStreet.setVisibility(View.INVISIBLE);

        //image_btns
        this.dice = findViewById(R.id.dice);
        this.endOfTurn = findViewById(R.id.end_of_turn);
        this.trading = findViewById(R.id.trading);

        //btns
        this.cards = findViewById(R.id.cards);

        //listeners
        this.fab.setOnClickListener(this);
        this.fabSettlement.setOnClickListener(this);
        this.fabCity.setOnClickListener(this);
        this.fabStreet.setOnClickListener(this);
        this.dice.setOnClickListener(this);
        this.endOfTurn.setOnClickListener(this);
        this.trading.setOnClickListener(this);
        this.cards.setOnClickListener(this);


        if (SettlerApp.getManager().isHost()) {
            TurnController.getInstance().startPlayerTurn();
        }

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        TextView tv = findViewById(R.id.debug_view);
        switch (i) {
            case R.id.fab_build_options:
                tv.append("clicked!");
                this.toogleFabMenu();
                break;
            case R.id.fab_settlement:
                tv.append("settlement clicked!");
                if (SettlerApp.board.getPhaseController().getCurrentPhase() == Board.Phase.PLAYER_TURN) {
                    SettlerApp.board.getPhaseController().setCurrentPhase(Board.Phase.SETUP_SETTLEMENT);
                    hexView.showFreeSettlements();
                }
                break;
            case R.id.fab_city:
                tv.append("city clicked!");
                break;
            case R.id.fab_street:
                tv.append("street clicked!");
                break;
            case R.id.dice:
                if (SettlerApp.board.getPhaseController().getCurrentPhase() != Board.Phase.PRODUCTION)
                    return;
                tv.append("dice clicked!");
                Random random = new Random();
                Integer max = 6;
                Integer min = 1;
                int dice1 = random.nextInt((max - min) + 1) + min;
                int dice2 = random.nextInt((max - min) + 1) + min;
                DiceRollAction roll = new DiceRollAction(SettlerApp.getPlayer(), dice1, dice2);
                //GameController.getInstance().handleDiceRolls(dice1,dice2);
                SettlerApp.getManager().sendToAll(roll);
                Toast.makeText(this.getApplicationContext(), "ROLLED " + dice1 + dice2,
                        Toast.LENGTH_LONG).show();
                SettlerApp.board.getPhaseController().setCurrentPhase(Board.Phase.PLAYER_TURN);
                break;
            case R.id.end_of_turn:
                if (SettlerApp.board.getPhaseController().getCurrentPhase() == Board.Phase.PLAYER_TURN) {
                    SettlerApp.getManager().sendtoHost(new TurnAction(SettlerApp.getPlayer(), false, true));
                }
                break;
            case R.id.cards:
                tv.append("cards clicked!");
                Intent in = new Intent(this, DisplayCardsActivity.class);
                startActivity(in);
                break;
            case R.id.trading:
                tv.append("trading clicked!");
                Intent in2 = new Intent(this, TradingActivity.class);
                startActivity(in2);
                break;
            default:
                break;
        }
        tv.setText(""); //TODO remove when debuggin
    }

    private void toogleFabMenu() {
        if (this.fabOpen) {
            this.layoutSettlement.startAnimation(closeMenu);
            this.layoutSettlement.animate().translationY(0);
            this.layoutCity.startAnimation(closeMenu);
            this.layoutCity.animate().translationY(0);
            this.layoutStreet.startAnimation(closeMenu);
            this.layoutStreet.animate().translationY(0);
        } else {
            /*
            byte offset = 1;
            for(FloatingActionButton f : this.fabOptions){
                f.startAnimation(openMenu);
                f.animate().translationY(-1*(offset++*FAB_MENU_DISTANCE));
            }
            */
            byte offset = 1;
            this.layoutSettlement.startAnimation(openMenu);
            this.layoutSettlement.animate().translationY(-1 * (offset++ * FAB_MENU_DISTANCE));
            this.layoutCity.startAnimation(openMenu);
            this.layoutCity.animate().translationY(-1 * (offset++ * FAB_MENU_DISTANCE));
            this.layoutStreet.startAnimation(openMenu);
            this.layoutStreet.animate().translationY(-1 * (offset * FAB_MENU_DISTANCE));
        }
        this.fabOpen = !this.fabOpen;
    }

    private void setupHexView() {
        hexView = new HexView(getApplicationContext());

        if (BuildConfig.DEBUG) {
            // do something for a debug build
            //String[] array ={"P1", "P2"};
            //SettlerApp.generateBoard(new ArrayList<>(Arrays.asList(array)));
        }
        board = SettlerApp.board;

        hexView.setBoard(board);
        hexView.setManager(getWindowManager());

        setContentView(R.layout.activity_main);

        final ZoomLayout zl = (ZoomLayout) findViewById(R.id.zoomContainer);
        final LinearLayout container = (LinearLayout) findViewById(R.id.gridContainer);
        //Button btn = (Button) findViewById(R.id.button);
        zl.getEngine().setMinZoom(1, ZoomEngine.TYPE_REAL_ZOOM);
        Display mdisp = getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);

        System.out.println(android.os.Build.VERSION.SDK_INT);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || true){
            hexView.setZoomLayout(zl);
            hexView.prepare();
            zl.addView(hexView);
        } else {
            hexView.prepare();
            container.removeView(zl);
            container.addView(hexView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    @Override
    public void onBackPressed() {
        return; //TODO maybe dialog option to exit?
    }

    @Override
    public void received(Connection connection, Object object) {

        if (object instanceof GameAction) {
            if (object instanceof EdgeBuildAction) {
                hexView.redraw();
            } else if (object instanceof VertexBuildAction) {
                VertexBuildAction action = (VertexBuildAction) object;
                if (action.getType() == VertexBuildAction.ActionType.BUILD_SETTLEMENT) {
                    action.getAffectedVertex().buildSettlement(action.getActor());
                } else if (action.getType() == VertexBuildAction.ActionType.BUILD_CITY) {
                    action.getAffectedVertex().buildCity(action.getActor());
                }
                hexView.generateVerticePaths();
                hexView.redraw();
            } else if (object instanceof TurnAction) {
                final TurnAction turn = (TurnAction) object;
                if (!turn.isEndTurn()) {

                    final TabLayout tabs = findViewById(R.id.tabs);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tabs.getTabAt(turn.getActor().getPlayerNumber()).select();
                        }
                    });

                    if (turn.getActor().equals(SettlerApp.getPlayer())) {
                        if (turn.isInitialTurn()) {
                            SettlerApp.board.getPhaseController().setCurrentPhase(Board.Phase.FREE_SETTLEMENT);
                            hexView.showFreeSettlements();

                        } else {
                            SettlerApp.board.getPhaseController().setCurrentPhase(Board.Phase.PRODUCTION);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "YOUR TURN! " + SettlerApp.getPlayer().getPlayerNumber() + "/" + SettlerApp.board.getPhaseController().getCurrentPhase().toString(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                } else {
                    if (SettlerApp.getManager().isHost()) {
                        TurnController.getInstance().advancePlayer();
                        //TODO: check if player won
                        TurnController.getInstance().startPlayerTurn();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "end turn",
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                }
            } else if (object instanceof DiceRollAction) {
                DiceRollAction roll = (DiceRollAction) object;
                GameController.getInstance().handleDiceRolls(roll.getDic1(), roll.getDic2());
            }
        }


    }
}
