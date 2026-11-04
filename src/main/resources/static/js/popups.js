const ENTETE_FETCH = {"X-Requested-With": "fetch"};
const DELAI_FERMETURE_LISTE_MS = 150;

function popup() {
    return document.getElementById("popup");
}

function afficherDansPopup(html) {
    const contenu = popup().querySelector(".popup-contenu");
    contenu.innerHTML = html;
    contenu.querySelectorAll("select[data-recherche]").forEach(rendreRecherchable);
    popup().classList.replace("hidden", "flex");
}

function fermerPopup() {
    popup().classList.replace("flex", "hidden");
}

async function ouvrirPopup(url) {
    const reponse = await fetch(url, {headers: ENTETE_FETCH});
    afficherDansPopup(await reponse.text());
}

async function envoyer(url, donnees) {
    const reponse = await fetch(url, {method: "POST", body: donnees, headers: ENTETE_FETCH});
    if (reponse.ok) {
        window.location.reload();
        return;
    }
    afficherDansPopup(await reponse.text());
}

function supprimer(declencheur) {
    if (!window.confirm(declencheur.dataset.confirmation)) {
        return;
    }
    const donnees = new FormData();
    donnees.append("_method", "DELETE");
    donnees.append(document.querySelector("meta[name=csrf-parametre]").content,
        document.querySelector("meta[name=csrf-jeton]").content);
    envoyer(declencheur.dataset.suppression, donnees);
}

function convertirImageEnBase64(champ) {
    const lecteur = new FileReader();
    lecteur.onload = () => {
        champ.form.querySelector("input[name=image]").value = lecteur.result;
    };
    lecteur.readAsDataURL(champ.files[0]);
}

function normaliser(texte) {
    return texte.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase();
}

// Remplace la liste déroulante par un champ de recherche, la liste d'origine reste celle qui est envoyée
function rendreRecherchable(liste) {
    const conteneur = document.createElement("div");
    conteneur.className = "relative";
    const recherche = document.createElement("input");
    recherche.type = "text";
    recherche.autocomplete = "off";
    recherche.className = "champ-saisie";
    recherche.placeholder = liste.dataset.recherche;
    recherche.value = liste.selectedIndex > 0 ? liste.options[liste.selectedIndex].text : "";
    const propositions = document.createElement("ul");
    propositions.className = "absolute z-20 mt-1 hidden max-h-56 w-full overflow-auto rounded bg-surface py-1";
    liste.after(conteneur);
    conteneur.append(recherche, propositions, liste);
    liste.classList.add("hidden");

    const afficherPropositions = () => {
        const filtre = normaliser(recherche.value);
        propositions.replaceChildren(...Array.from(liste.options)
            .filter(option => option.value && normaliser(option.text).includes(filtre))
            .map(option => creerProposition(option, liste, recherche, propositions)));
        propositions.classList.toggle("hidden", propositions.children.length === 0);
    };
    recherche.addEventListener("focus", () => {
        recherche.select();
        afficherPropositions();
    });
    recherche.addEventListener("input", () => {
        liste.value = "";
        afficherPropositions();
    });
    recherche.addEventListener("blur", () => {
        setTimeout(() => propositions.classList.add("hidden"), DELAI_FERMETURE_LISTE_MS);
    });
}

function creerProposition(option, liste, recherche, propositions) {
    const proposition = document.createElement("li");
    proposition.className = "cursor-pointer px-3 py-1.5 hover:bg-vert-clair";
    proposition.textContent = option.text;
    proposition.addEventListener("mousedown", () => {
        liste.value = option.value;
        recherche.value = option.text;
        propositions.classList.add("hidden");
    });
    return proposition;
}

// Masque les signalements qui dépassent la hauteur disponible du menu latéral
function ajusterSignalements() {
    const liste = document.querySelector("[data-signalements]");
    if (!liste) {
        return;
    }
    const elements = Array.from(liste.children);
    elements.forEach(element => element.classList.remove("hidden"));
    elements.forEach(element => {
        const depasse = element.offsetTop + element.offsetHeight > liste.clientHeight;
        element.classList.toggle("hidden", depasse);
    });
}

document.addEventListener("click", evenement => {
    const ouverture = evenement.target.closest("[data-popup]");
    if (ouverture) {
        evenement.preventDefault();
        ouvrirPopup(ouverture.dataset.popup);
        return;
    }
    const suppression = evenement.target.closest("[data-suppression]");
    if (suppression) {
        supprimer(suppression);
        return;
    }
    if (evenement.target.closest("[data-fermer-popup]") || evenement.target === popup()) {
        fermerPopup();
    }
});

document.addEventListener("submit", evenement => {
    const formulaire = evenement.target;
    if (formulaire.matches("[data-formulaire-popup]")) {
        evenement.preventDefault();
        envoyer(formulaire.action, new FormData(formulaire));
    }
});

document.addEventListener("change", evenement => {
    const champ = evenement.target;
    if (champ.matches("[data-soumission-auto]")) {
        champ.form.submit();
    }
    if (champ.matches("[data-image-base64]") && champ.files.length > 0) {
        convertirImageEnBase64(champ);
    }
});

window.addEventListener("resize", ajusterSignalements);
window.addEventListener("load", ajusterSignalements);
