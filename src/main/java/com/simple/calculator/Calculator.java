package com.simple.calculator;

/**
 * Main class for calculator project
 */

import java.math.BigDecimal;
import javafx.application.Application;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

public class Calculator extends Application {

    Label outputLabel = null;           // shows result
    Label operatorLabel = null;         // shows operators
    TextField inputField = null;        // useri nput
    boolean operationStarted = false;   // flag, clear input if operator pressed

    static public void main(String args[]) {
        launch(args); // just launch application thread
    }

    // Start of application
    @Override
    public void start(Stage root) {

        // Grid pane to contain the button matrix
        GridPane grid = new GridPane();
        grid.setPadding(new javafx.geometry.Insets(10, 10, 10, 10));
        grid.setVgap(10);
        grid.setHgap(10);

        VBox vBox = new VBox(10);
        inputField = new TextField("0");

        // This field can only take numbers and period, nothing else
        inputField.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observableValue,
                 String oldValue,
                 String newValue) {
                if (!newValue.matches("-?[0-9]*\\.?[0-9]*")) {
                    ((StringProperty) observableValue).set(oldValue);
                }
            }
        });

        // Initialize labels
        outputLabel = new Label("");
        operatorLabel = new Label("");

        // vertical box to show first output->operator->input->keys
        vBox.getChildren().add(outputLabel);
        vBox.getChildren().add(operatorLabel);
        vBox.getChildren().add(inputField);
        vBox.getChildren().add(grid);

        // Initialize first the 1 - 9 buttons since it can be done on
        // a loop
        int row = 3;
        for (int i = 0; i < 9; i++) {
            int column = i % 3;
            if (column % 3 == 0)
                row--;
            Button button = new Button(Integer.toString(i + 1));
            button.setPrefSize(50, 50);
            button.setOnAction(digitHandler());
            grid.add(button, column, row);
        }

        // add 0 and period buttons
        Button zeroButton = new Button("0");
        Button periodButton = new Button(".");
        zeroButton.setPrefSize(50, 50);
        periodButton.setPrefSize(50, 50);
        zeroButton.setOnAction(digitHandler());
        periodButton.setOnAction(digitHandler());

        // add operators buttons
        Button timesButton = new Button("*");
        Button plusButton = new Button("+");
        Button minusButton = new Button("-");
        Button divisionButton = new Button("/");
        Button signToggleButton = new Button("+/-");
        Button equalButton = new Button("=");
        Button clearButton = new Button("Clear");

        // Set their sizes
        timesButton.setPrefSize(50, 50);
        plusButton.setPrefSize(50, 50);
        minusButton.setPrefSize(50, 50);
        divisionButton.setPrefSize(50, 50);
        signToggleButton.setPrefSize(50, 50);
        equalButton.setPrefSize(60, 100); // two rows tall
        clearButton.setPrefSize(60, 100); // two rows tall

        // Clean fields when pressed, first clean output field and then input field
        // second press
        clearButton.setOnAction(e -> {
            if (inputField.getText().equals("0") 
                    || inputField.getText().equals("")) {
                outputLabel.setText("");
                operatorLabel.setText("");
            }
            inputField.setText("0");
        });

        // All buttons to the grid pane
        grid.addColumn(2, periodButton);
        grid.addColumn(1, zeroButton);
        grid.addColumn(3, timesButton);
        grid.addColumn(3, divisionButton);
        grid.addColumn(3, minusButton);
        grid.addColumn(3, plusButton);
        grid.add(equalButton, 4, 2, 1, 2); // two rows
        grid.add(clearButton, 4, 0, 1, 2); // two rows
        grid.addColumn(0, signToggleButton);

        // All actions on these buttons are handled by operator handler
        timesButton.setOnAction(operatorHandler());
        divisionButton.setOnAction(operatorHandler());
        minusButton.setOnAction(operatorHandler());
        plusButton.setOnAction(operatorHandler());
        equalButton.setOnAction(operatorHandler());
        signToggleButton.setOnAction(operatorHandler());

        // Set scene and show window
        root.setScene(new Scene(vBox, 320, 320));
        root.setResizable(false); // to keep gui looking good avoid resizing
        root.setTitle("Simple Calculator");
        root.show();
    }

    /**
     * @returns  an EventHandler that handles all operations from the keypad
     */
    public EventHandler<ActionEvent> operatorHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Button button = (Button) event.getTarget();
                String operator = button.getText();
                String output = outputLabel.getText();
                String input = inputField.getText();

                // Use big decimal to handle precision w/out double limitations
                BigDecimal outputNumber;
                BigDecimal inputNumber;
                BigDecimal result;
                String outputOperator = operatorLabel.getText();

                // Initialization of big decimals
                outputNumber = output.equals("") 
                            || output == null ? new BigDecimal("0") 
                            : new BigDecimal(output);
                inputNumber = input.equals("") 
                            || input == null ? new BigDecimal("0") 
                            : new BigDecimal(input);

                try {

                    result = calculate(outputNumber, inputNumber, outputOperator);

                    // when = is pressed we need to show the calculated value
                    // and clear/set to clear the input field when next number
                    // is pressed
                    if (operator.equals("=")) {
                        if (outputOperator.equals("")) {
                            outputLabel.setText("");
                            return;
                        }
                        outputLabel.setText(result.toString());
                        setOperationStarted(true); 
                    }
                    // Just negate the inputs
                    else if (operator.equals("+/-")) {
                        if (input.contains("-")) {
                            inputField.setText(input.replace("-", ""));
                        } else {
                            if (input.equals("0"))
                                return;
                            inputField.setText("-" + input);
                        }
                    }
                    // After any other operator is pressed we display the result 
                    // on screen and clear input field when next number is pressed
                    else {
                        if (output.equals(""))
                            outputLabel.setText(result.toString());
                        operatorLabel.setText(operator);
                        // clear input field when next number is pressed
                        setOperationStarted(true); 

                    }

                } catch (Exception ex) {
                    inputField.setText("NaN");
                }
            }
        };
    }

    /**
     * Handle the entery of each number to ensure pressing a button adds 
     * the text to the input field
     * @returns EventHandler with input degit handling
     */
    public EventHandler<ActionEvent> digitHandler() {
        return new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Button button = (Button) event.getTarget();
                String number = button.getText();
                String text = inputField.getText();

                // redundant check, handled by inputfield
                if (text.contains(".") && number.equals(".")) {
                    event.consume();
                }
                if (text.equals("0")) {
                    text = "";
                }
                if (operationStarted()) {
                    text = "";
                }

                inputField.setText(text + number);
                // clear flag to clear input field when next number is pressed
                setOperationStarted(false); 
            }
        };
    }

    /**
     * Executes calculation
     * 
     * @params left decimal containing left of operand value
     * @params right decimal contaning right of operand value
     * @params operator operator for operation (+,-,*,/)
     * @returns result of operation (left [operator] right)
     */
    BigDecimal calculate(BigDecimal left, BigDecimal right, String operator) {
        BigDecimal result = null;

        if (operator.equals("+")) {
            result = left.add(right);
        } else if (operator.equals("-")) {
            result = left.subtract(right);
        } else if (operator.equals("*")) {
            result = left.multiply(right);
        } else if (operator.equals("/")) {
            result = left.divide(right);

        } else if (operator.equals("")) {
            result = right;
        }
        return result;
    }

    /**
     * Flag to keep track of operations strated This is to be set when an 
     * operator is pressed And to be checked when a number is pressed To 
     * determine if input field should be cleared
     * 
     * @params boolean value to set flag
     */
    public void setOperationStarted(boolean val) {
        this.operationStarted = val;
    }

    /**
     * @returns operationStarted flag, checked when clearing the input field.
     */
    public boolean operationStarted() {
        return operationStarted;
    }
}
