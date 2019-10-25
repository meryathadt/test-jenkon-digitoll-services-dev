package com.digitoll.commons.dto;

import com.digitoll.commons.model.Pos;
import com.digitoll.commons.util.BasicUtils;

public class PosDTO extends Pos {

    public PosDTO(){

    }

    public PosDTO(Pos pos){
        BasicUtils.copyNonNullProps(pos,this);
    }


}
