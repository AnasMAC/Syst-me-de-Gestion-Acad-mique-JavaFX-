package com.example.gestionacademique.modele;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Formation {
    private Integer id;
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
