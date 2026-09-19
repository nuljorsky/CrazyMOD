# CrazyMOD (Fabric 1.21.4)

Klawisz **N** (do zmiany: Ustawienia → Sterowanie → CrazyMOD) otwiera menu z kategorią **CRAZY**:
AUTO NIELOTKA, TRACERS, NAMETAG, KOPIOWANIE!

- LPM na module: włącz/wyłącz
- PPM na module: rozwiń/zwiń jego ustawienia
- Nagłówek "CRAZY": przeciągnij, żeby przesunąć cały panel (razem z modułami i ustawieniami)
- Stan zapisuje się w `config/crazymod.json`

## Budowanie

Wymagania: Java 21.

### Opcja 1: Gradle lokalnie
    gradle wrapper --gradle-version 8.12   (raz, jeśli masz zainstalowany Gradle)
    ./gradlew build                        (Windows: gradlew.bat build)
Gotowy plik: `build/libs/CrazyMOD-1.0.0.jar` -> wrzuć do `.minecraft/mods` razem z Fabric API 1.21.4.

### Opcja 2: IntelliJ IDEA
File -> Open -> wybierz folder projektu -> po zaimportowaniu Gradle uruchom zadanie `build`.

### Opcja 3: GitHub (bez instalowania czegokolwiek)
Wrzuć folder do nowego repo na GitHubie -> zakładka Actions -> "build" -> pobierz artefakt "CrazyMOD".
