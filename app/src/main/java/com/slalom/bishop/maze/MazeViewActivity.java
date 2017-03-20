package com.slalom.bishop.maze;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.slalom.bishop.R;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;

public class MazeViewActivity extends Activity {
    @BindView(R.id.algorithm_view) TextView algorithmView;
    @BindView(R.id.maze_view) MazeView mazeView;
    @BindView(R.id.info_view) TextView infoView;
    @BindView(R.id.solve_button) Button solveButton;
    @BindString(R.string.maze_algorithm_backtrack) String backtrackAlgorithmText;
    @BindString(R.string.maze_algorithm_kruskal) String kruskalAlgorithmText;
    @BindString(R.string.maze_algorithm_prim) String primAlgorithmText;
    @BindString(R.string.maze_backtrack_info_format) String backtrackInfoFormat;
    @BindString(R.string.maze_kruskal_info_format) String kruskalInfoFormat;
    @BindString(R.string.maze_prim_info_format) String primInfoFormat;
    @BindString(R.string.maze_solve_info_format) String solveInfoFormat;
    @BindString(R.string.maze_solve_error) String solveErrorText;

    private static final String MAZE_OPTIONS = "MAZE_OPTIONS";

    private Maze maze;
    private MazeOptions options;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public static Intent buildIntent(Context context, MazeOptions options) {
        Intent intent = new Intent(context, MazeViewActivity.class);
        intent.putExtra(MAZE_OPTIONS, options);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maze_view_activity);
        ButterKnife.bind(this);

        options = (MazeOptions) getIntent().getSerializableExtra(MAZE_OPTIONS);

        configureAlgorithmView();
        configureMazeView();
        configureSolveButton();
    }

    @Override
    protected void onDestroy() {
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    private void configureAlgorithmView() {
        switch (options.getAlgorithm()) {
            case BACKTRACK: algorithmView.setText(backtrackAlgorithmText);
                break;
            case KRUSKAL: algorithmView.setText(kruskalAlgorithmText);
                break;
            case PRIM: algorithmView.setText(primAlgorithmText);
                break;
        }
    }

    private void configureMazeView() {
        maze = new Maze(options);
        mazeView.setMaze(maze);

        switch (options.getType()) {
            case ANIMATE:
                switch (options.getAlgorithm()) {
                    case BACKTRACK: subscriptions.add(maze.animateViaRecursiveBacktrack()
                            .subscribe(result -> {
                                mazeView.invalidate();
                                infoView.setText(String.format(backtrackInfoFormat, result));
                            }, Throwable::printStackTrace, () -> solveButton.setVisibility(View.VISIBLE)));
                        break;
                    case KRUSKAL: subscriptions.add(maze.animateViaKruskalsAlgorithm()
                            .subscribe(result -> {
                                mazeView.invalidate();
                                infoView.setText(String.format(kruskalInfoFormat, result));
                            }, Throwable::printStackTrace, () -> solveButton.setVisibility(View.VISIBLE)));
                        break;
                    case PRIM: subscriptions.add(maze.animateViaPrimsAlgorithm()
                            .subscribe(result -> {
                                mazeView.invalidate();
                                infoView.setText(String.format(primInfoFormat, result));
                            }, Throwable::printStackTrace, () -> solveButton.setVisibility(View.VISIBLE)));
                        break;
                }
                break;
            case GENERATE:
                switch (options.getAlgorithm()) {
                    case BACKTRACK: maze.generateViaRecursiveBacktrack();
                        break;
                    case KRUSKAL: maze.generateViaKruskalsAlgorithm();
                        break;
                    case PRIM: maze.generateViaPrimsAlgorithm();
                        break;
                }
                solveButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void configureSolveButton() {
        subscriptions.add(RxView.clicks(solveButton).subscribe(clicked -> {
            solveButton.setVisibility(View.INVISIBLE);

            subscriptions.add(maze.solveViaRecursiveBacktrack()
                    .subscribe(result -> {
                        mazeView.invalidate();
                        int stepsToBacktrackSolution = (int) result;
                        String solveMessage = stepsToBacktrackSolution > 0 ? String.format(solveInfoFormat, stepsToBacktrackSolution) : solveErrorText;
                        infoView.setText(solveMessage);
                    }, Throwable::printStackTrace));
        }));
    }
}