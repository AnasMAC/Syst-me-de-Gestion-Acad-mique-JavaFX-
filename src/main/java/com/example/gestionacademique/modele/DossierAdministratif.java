package com.example.gestionacademique.modele;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DossierAdministratif {
    private int id;
    private String numeroInscription;
    private Date dateCreation;
    private int studentId;
}
