package javax0.jamal.webeditor.jamalutils;

import javax0.jamal.api.Input;
import javax0.jamal.api.Position;

public class InterruptingInput implements Input {
    public static class Ended extends RuntimeException {
    }

    final int col;
    final int line;

    private final Input input;

    public InterruptingInput(Position terminatingPosition, Input input) {
        col = terminatingPosition.column;
        line = terminatingPosition.line;
        this.input = input;
    }

    @Override
    public StringBuilder getSB() {
        return input.getSB();
    }

    @Override
    public Position getPosition() {
        return input.getPosition();
    }

    @Override
    public String getReference() {
        return input.getReference();
    }

    @Override
    public int getLine() {
        return input.getLine();
    }

    @Override
    public int getColumn() {
        return input.getColumn();
    }

    @Override
    public void stepLine() {
        if (getLine() >= line) {
            throw new Ended();
        }
    }

    @Override
    public void stepColumn() {
        if (getLine() > line || (getLine() == line && getColumn() >= col)) {
            throw new Ended();
        }
    }
}
