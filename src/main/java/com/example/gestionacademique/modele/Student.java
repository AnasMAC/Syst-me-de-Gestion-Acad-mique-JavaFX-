package com.example.gestionacademique.modele;

import lombok.*;

@Getter // Génère les Getters pour tous les champs
@ToString
@NoArgsConstructor
// @Setter -> ON L'ENLÈVE d'ici pour éviter de générer un setMoyenne vide de sens
public class Student {

    @Setter // On remet @Setter champ par champ pour ceux qui sont "sûrs"
    private Integer id;

    @Setter
    private String name;

    // Pas de @Setter ici, car on va le faire manuellement
    private Double moyenne;

    @Setter
    private Integer formationId; // Convention Java : camelCase (pas FormationId)

    // Constructeur personnalisé
    public Student(Integer id, String name, Double moyenne, Integer formationId) {
        this.id = id;
        this.name = name;
        this.formationId = formationId;

        // ASTUCE PRO : On appelle le setter ici !
        // Comme ça, la validation est écrite à un seul endroit.
        setMoyenne(moyenne);
    }

    // Setter manuel avec Validation
    public void setMoyenne(Double moyenne) {
        if (moyenne < 0 || moyenne > 20) {
            throw new IllegalArgumentException("La moyenne doit être comprise entre 0 et 20.");
        }
        this.moyenne = moyenne;
    }
}