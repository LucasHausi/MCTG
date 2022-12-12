Ideen:

Eine Klasse zum Userstate-Management:
online
offline
in-game
waiting for game -> liste
    wenn 2 player warten, soll das game gestartet werden

Schritte zum login:
1. Create user (wenn nicht vorhanden) Hier fehler falls ein user bereits existiert 
2. Login mit Usernamen und Passwort Hier fehler falls keine korrekten Daten gesendet werden
3. Returniert einen Token

Erst nach einem Login können weitere Funktionen aufegrufen werden? -> immer mithilfe des Tokens

Bekomme einen request
behandle ihn
Fälle:
1. ~~users~~
2. ~~sessions~~
3. packages
4. /transactions/packages -> zum Kaufen
5. packages -> zum admin anlegen
6. cards
7. deck
8. /users/username ->editieren der userdaten
9. stats
10. score
11. battles
12. tradings