[README.md](https://github.com/user-attachments/files/23699775/README.md)
# Adatgyűjtő Android kliens

Ez az Android alkalmazás egy Windows alatt futó adatgyűjtő programhoz kapcsolódik.
Önállóan nem használható, mert a cikktörzset HTTP-n keresztül egy PC-s szerverprogramtól kapja.

## Kapcsolat a szerverrel

A kliens a beállított IP-címen és porton kommunikál:

- `GET /ping` → a szerver elérhetőségének ellenőrzése.
- `GET /wplrad` → cikktörzs export (WPLRAD.TXT tartalma)
- `POST /vrhad` → megrendelés adatok (rendeles.txt)
- `POST /leltar` → leltár adatok (leltar.txt)

A szerverbeállítás az alkalmazáson belül adható meg:
**Főmenü → Szerver beállítása**.

A cikktörzset a készletkezelő program állítja elő. A leltár és megrendelés fájlokat megfelelő
formátum esetén beolvassa.

## Build

- Android Studio (Giraffe/Flamingo/Koala stb.) + Gradle wrapper
- Min SDK: 26
- Target SDK: 36
- Nyelvek: Kotlin

## Futatás

1. Klónozd a repo-t
2. Nyisd meg Android Studio-ban
3. Indítsd el emulátoron vagy eszközön
4. Állítsd be a szerver IP/portot a **Szerver beállítása** menüben
5. A szerver oldali programon engedélyezd a fenti HTTP végpontokat. (A szerver program automatikusan
   megpróbálja beállítani az IP címet és a portot, illetve engedélyezi a Windows tűzfaban)

## Ajánlott fájlszerkezet

1. C:\PLR\wadatgy\wadatgy.exe
2. C:\PLR\wadatgy\wadatgy.ico
3. C:\PLR\wadatgy\config.xml
4. C:\PLR\exp\wplrad.txt
5. C:\PLR\imp\rendeles.txt (nem szükséges, adatküldéskor létrejön)
6. C:\PLR\imp\leltar.txt (nem szükséges, adatküldéskor létrejön)

A leltar.txt és rendeles.txt jelenleg név+dátum formátumban jön létre a felülírás elkerülésének
érdekében.
