package fr.efrei.idflm.repository;

import fr.efrei.idflm.modele.Profil;
import fr.efrei.idflm.modele.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    Optional<Utilisateur> findByIdentifiant(String identifiant);

    boolean existsByIdentifiant(String identifiant);

    long countByProfil(Profil profil);

    @Query("""
            SELECT u FROM Utilisateur u
            WHERE LOWER(CONCAT(u.prenom, ' ', u.nom)) LIKE LOWER(CONCAT('%', :recherche, '%'))
            OR LOWER(u.identifiant) LIKE LOWER(CONCAT('%', :recherche, '%'))
            ORDER BY u.nom, u.prenom
            """)
    Page<Utilisateur> rechercherUtilisateurs(String recherche, Pageable pagination);
}
