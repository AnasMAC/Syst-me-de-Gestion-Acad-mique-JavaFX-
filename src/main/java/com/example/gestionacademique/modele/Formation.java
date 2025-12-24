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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Formation formation = (Formation) o;
        return id == formation.id;
    }

}
