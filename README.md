# Android Calculator Application

This is a simple calculator application built with Android using Kotlin and XML. The application provides basic arithmetic operations, and factorial calculations, and displays a history of operations.

## Features

- Basic arithmetic operations (addition, subtraction, multiplication, division)
- Special functions: power, square root, pi, factorial, and the constant `e`
- Clear and backspace functionality
- Supports input of single digit, double zero, and decimal point
- Displays operation history
- Collapsible special operations panel

## Layout

The application layout is defined in XML using `ConstraintLayout` and `LinearLayout`. The UI comprises a `MaterialToolbar`, a panel for special operations, and a standard numeric keypad. The results and operation history are displayed using `TextView`.

### Main Layout (activity_main.xml)

The main layout includes:
- A `MaterialToolbar` for the app bar.
- A `LinearLayout` containing the special operations buttons can be toggled.
- Several `LinearLayout` elements for the numeric keypad and arithmetic operators.
- `TextView` elements to display the result and the current operation.
- A `ScrollView` to show the history of calculations.

### Menu Layout (activity_main_menu.xml)

The menu layout includes:
- A `ConstraintLayout` as the root layout.

## Functionality

### Special Operations Panel

- The panel includes buttons for power (`^`), square root (`√`), pi (`π`), factorial (`!`), and the constant `e`.
- The panel can be shown or hidden by pressing the collapse button.

### Numeric Keypad and Operators

- The keypad includes buttons for digits `0-9`, double zero (`00`), and decimal point (`.`).
- Operators include addition (`+`), subtraction (`-`), multiplication (`×`), division (`÷`), and modulo (`%`).
- `AC` button clears the current input.
- `back` button deletes the last character of the current input.

### Result and History Display

- The result of the calculation is displayed in a large `TextView` at the top.
- The current operation is displayed in another `TextView` above the result.
- Calculation history is shown in a `ScrollView` and can be toggled visible or hidden.

##Install apk file [Click here](https://github.com/Dinesh-singh-saini/Calculator_MK/blob/main/Calculator%40dinesh.apk)

##Usage

- Use the numeric keypad and operators to perform calculations.
- Press the special operations button to access more functions.
- View the calculation history by toggling the history button.

##Contributing

Contributions are welcome! Please fork the repository and create a pull request with your changes.

##License

This project is licensed under the MIT [License](licence).
