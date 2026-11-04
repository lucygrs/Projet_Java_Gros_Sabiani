package fr.efrei.idflm.repository;

import fr.efrei.idflm.modele.Prestataire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestataireRepository extends JpaRepository<Prestataire, Integer> {

    List<Prestataire> findAllByOrderByNomAsc();

    Page<Prestataire> findByNomContainingIgnoreCase(String nom, Pageable pagination);
}
