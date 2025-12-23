package com.example.gestionacademique.modele;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Cours {
    private int id;
    private String code;
    private String intitule;

    @Override
    public String toString() {
        return code + " - " + intitule; // Utile pour les ComboBox
    }
}
