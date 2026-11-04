package fr.efrei.idflm.controleur;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public record Pagination(long nombreResultats, int numeroPage, String urlPagePrecedente, String urlPageSuivante) {

    private static final int TAILLE_PAGE = 10;
    private static final String PARAMETRE_PAGE = "page";

    public static Pageable demanderPage(int numeroPage) {
        return PageRequest.of(Math.max(numeroPage, 0), TAILLE_PAGE);
    }

    // Les liens conservent la recherche et les filtres de la requête courante
    public static Pagination decrire(Page<?> page, HttpServletRequest requete) {
        String precedente = page.hasPrevious() ? lienVersPage(requete, page.getNumber() - 1) : null;
        String suivante = page.hasNext() ? lienVersPage(requete, page.getNumber() + 1) : null;
        return new Pagination(page.getTotalElements(), page.getNumber() + 1, precedente, suivante);
    }

    private static String lienVersPage(HttpServletRequest requete, int numeroPage) {
        return ServletUriComponentsBuilder.fromRequest(requete)
                .replaceQueryParam(PARAMETRE_PAGE, numeroPage)
                .build()
                .toUriString();
    }
}
