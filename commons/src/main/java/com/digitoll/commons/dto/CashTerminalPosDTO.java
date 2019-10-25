package com.digitoll.commons.dto;

import com.digitoll.commons.model.CashTerminalPos;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class CashTerminalPosDTO {
    private ArrayList<HashMap<String, CashTerminalPos>> terminals;

    public ArrayList<HashMap<String, CashTerminalPos>> getTerminals() {
        return terminals;
    }

    public void setTerminals(ArrayList<HashMap<String, CashTerminalPos>> terminals) {
        this.terminals = terminals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CashTerminalPosDTO that = (CashTerminalPosDTO) o;
        return Objects.equals(terminals, that.terminals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(terminals);
    }
}