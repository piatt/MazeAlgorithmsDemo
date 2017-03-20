package com.slalom.bishop.maze;

import android.app.Activity;
import android.os.Bundle;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.slalom.bishop.R;
import com.slalom.bishop.maze.MazeOptions.MazeType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MazeOptionsActivity extends Activity {
    @BindView(R.id.rows_picker) NumberPicker rowsPicker;
    @BindView(R.id.columns_picker) NumberPicker columnsPicker;
    @BindView(R.id.speed_picker) NumberPicker speedPicker;
    @BindView(R.id.algorithm_group) RadioGroup algorithmGroup;
    @BindView(R.id.default_algorithm_button) RadioButton defaultAlgorithmButton;

    private String[] rowsPickerValues = new String[] {"6", "12", "24", "48", "60"};
    private String[] columnsPickerValues = new String[] {"5", "10", "20", "40", "50"};
    private String[] speedPickerValues = new String[] {"1", "50", "100", "250", "500", "1000"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maze_options_activity);
        ButterKnife.bind(this);
        configureView();
    }

    private void configureView() {
        rowsPicker.setMinValue(0);
        rowsPicker.setMaxValue(rowsPickerValues.length - 1);
        rowsPicker.setDisplayedValues(rowsPickerValues);

        columnsPicker.setMaxValue(0);
        columnsPicker.setMaxValue(columnsPickerValues.length - 1);
        columnsPicker.setDisplayedValues(columnsPickerValues);

        speedPicker.setMinValue(0);
        speedPicker.setMaxValue(speedPickerValues.length - 1);
        speedPicker.setDisplayedValues(speedPickerValues);

        algorithmGroup.check(defaultAlgorithmButton.getId());
    }

    private MazeOptions getOptions() {
        MazeOptions options = new MazeOptions();
        options.setRows(Integer.valueOf(rowsPickerValues[rowsPicker.getValue()]));
        options.setColumns(Integer.valueOf(columnsPickerValues[columnsPicker.getValue()]));
        options.setSpeed(Integer.valueOf(speedPickerValues[speedPicker.getValue()]));

        int selectedAlgorithmButton = algorithmGroup.indexOfChild(ButterKnife.findById(algorithmGroup, algorithmGroup.getCheckedRadioButtonId()));
        switch (selectedAlgorithmButton) {
            case 0: options.setAlgorithm(MazeOptions.MazeAlgorithm.BACKTRACK);
                break;
            case 1: options.setAlgorithm(MazeOptions.MazeAlgorithm.KRUSKAL);
                break;
            case 2: options.setAlgorithm(MazeOptions.MazeAlgorithm.PRIM);
                break;
        }

        return options;
    }

    @OnClick(R.id.animate_button)
    public void onAnimateButtonClick() {
        MazeOptions options = getOptions();
        options.setType(MazeType.ANIMATE);
        startActivity(MazeViewActivity.buildIntent(this, options));
    }

    @OnClick(R.id.generate_button)
    public void onGenerateButtonClick() {
        MazeOptions options = getOptions();
        options.setType(MazeType.GENERATE);
        startActivity(MazeViewActivity.buildIntent(this, options));
    }
}