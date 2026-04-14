// Diese Funktion erwartet ein Array mit den spezifischen Schritten der aktuellen Seite
function startAppTour(pageSteps) {
    // Prüfen, ob Schritte übergeben wurden
    if (!pageSteps || pageSteps.length === 0) {
        console.warn("Keine Tour-Schritte für diese Seite definiert.");
        return;
    }

    const driver = window.driver.js.driver;

    const tour = driver({
        showProgress: true,
        animate: true,
        progressText: 'Schritt {{current}} von {{total}}',
        nextBtnText: 'Weiter ❯',
        prevBtnText: '❮ Zurück',
        doneBtnText: 'Verstanden! ✓',
        // Hier laden wir dynamisch die Schritte der aktuellen Seite rein:
        steps: pageSteps
    });

    tour.drive();
}