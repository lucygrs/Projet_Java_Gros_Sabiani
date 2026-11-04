package fr.efrei.idflm.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ResolutionFormulaireTest {

    private static ValidatorFactory fabrique;
    private static Validator validateur;

    @BeforeAll
    static void preparer() {
        fabrique = Validation.buildDefaultValidatorFactory();
        validateur = fabrique.getValidator();
    }

    @AfterAll
    static void fermer() {
        fabrique.close();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6})
    void noteHorsDeLIntervalle1A5EstRefusee(int note) {
        assertThat(validateur.validate(new ResolutionFormulaire(note, null)))
                .extracting(violation -> violation.getMessage())
                .containsExactly("La note doit être comprise entre 1 et 5");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5})
    void noteDansLIntervalle1A5EstAcceptee(int note) {
        assertThat(validateur.validate(new ResolutionFormulaire(note, null))).isEmpty();
    }
}
