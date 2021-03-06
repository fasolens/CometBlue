Cześć, 
Jakiś czas temu zakupiłem dwa „lidlowe” termostaty grzejnikowe RT2000BT (Comet Blue). W oryginalnej aplikacji EUROprog brakowało mi kilku funkcji. Wgryzłem się w temat „Bluetooth LE” i okazało się, że możliwe jest napisanie aplikacji na androida posiadającej funkcje, których nie ma fabryczny soft.

Moja aplikacja posiada następujące funkcje:

1.	Pokazanie aktualnej temperatury na czujniku termostatu (uwzględnia offset).
2.	Pokazanie aktualnie ustawionej temperatury.
3.	Pokazanie zaprogramowanych temperatur (oszczędzania i grzania).
4.	Możliwość ręcznej zmiany ustawionej temperatury.
5.	Możliwość zmiany trybu pracy auto/manual.
6.	Pokazanie stanu naładowania baterii.
7.	Aplikacja pamięta ostatnio wprowadzony PIN (jeżeli mamy kilka urządzeń, to najlepiej ustawić na nich ten sam PIN).

Aplikację testowałem na dwóch urządzeniach jedno z androidem 5.1, drugie z androidem 4.4. Jeżeli będziecie mieli jakieś problemy to dajcie znać.

## Zasada działania

Nie będę tu opisywał zasad komunikacji BLE i kodu aplikacji w javie, przedstawię tylko, w jaki sposób można odczytać/zapisać dane do Comet Blue, które są uniwersalne dla wszystkich platform. 
Listę charakterystyk zawierających interesujące nas dane otrzymałem na dwa sposoby:

- analiza logów z pliku btsnoop_hci.log za pomocą programu Wireshark.
- za pomocą aplikacji na androida: [nRF Master Control Panel (BLE)](https://play.google.com/store/apps/details?id=no.nordicsemi.android.mcp)

Najważniejsze dane są zawarte w charakterystykach serwisu 47e9ee00-47e9-11e4-8939-164230d1df67. Odczyt poniższych charakterystyk jest możliwy po podaniu PIN. W tym celu do charakterystyki `47e9ee30-47e9-11e4-8939-164230d1df67` musimy zapisać aktualny PIN ustawiony w aplikacji EUROprog.

## Lista dostępnych charakterystyk

`Service: 47e9ee00-47e9-11e4-8939-164230d1df67`

| Characteristic                       | Data        | Value                      |
| ------------------------------------ | :-----------| --------------------------:|
| 47e9ee01-47e9-11e4-8939-164230d1df67 | Actual time | 37:11:12:01:10             |
| 47e9ee10-47e9-11e4-8939-164230d1df67 | Monday      | 25:84:00:00:00:00:00:00    |
| 47e9ee11-47e9-11e4-8939-164230d1df67 | Tuesday     | 25:84:00:00:00:00:00:00    |
| 47e9ee12-47e9-11e4-8939-164230d1df67 | Wednesday   | 25:84:00:00:00:00:00:00    |
| 47e9ee13-47e9-11e4-8939-164230d1df67 | Thursday    | 25:84:00:00:00:00:00:00    |
| 47e9ee14-47e9-11e4-8939-164230d1df67 | Friday      | 25:84:00:00:00:00:00:00    |
| 47e9ee15-47e9-11e4-8939-164230d1df67 | Saturday    | 2a:84:00:00:00:00:00:00    |
| 47e9ee16-47e9-11e4-8939-164230d1df67 | Sunday      | 2a:84:00:00:00:00:00:00    |
| 47e9ee20-47e9-11e4-8939-164230d1df67 | Holiday_1   | 09:06:02:10:09:0d:02:10:00 |
| 47e9ee21-47e9-11e4-8939-164230d1df67 | Holiday_2   | 00:00:00:00:00:00:00:00:00 |
| 47e9ee22-47e9-11e4-8939-164230d1df67 | Holiday_3   | 00:00:00:00:00:00:00:00:00 |
| 47e9ee23-47e9-11e4-8939-164230d1df67 | Holiday_4   | 00:00:00:00:00:00:00:00:00 |
| 47e9ee24-47e9-11e4-8939-164230d1df67 | Holiday_5   | 00:00:00:00:00:00:00:00:00 |
| 47e9ee25-47e9-11e4-8939-164230d1df67 | Holiday_6   | 00:00:00:00:00:00:00:00:00 |
| 47e9ee26-47e9-11e4-8939-164230d1df67 | Holiday_7   | 00:00:00:00:00:00:00:00:00 |
| 47e9ee27-47e9-11e4-8939-164230d1df67 | Holiday_8   | 00:00:00:00:00:00:00:00:00 |
| 47e9ee2a-47e9-11e4-8939-164230d1df67 | status      | 00:00:08                   |
| 47e9ee2b-47e9-11e4-8939-164230d1df67 | Settings    | 34:2f:21:2f:fc:04:0a       |
| 47e9ee2c-47e9-11e4-8939-164230d1df67 | Battery     | 43                         |
| 47e9ee2d-47e9-11e4-8939-164230d1df67 | Soft        | soft cobl0126              |
| 47e9ee2e-47e9-11e4-8939-164230d1df67 | unknown     | "1e-00"                    |
| 47e9ee30-47e9-11e4-8939-164230d1df67 | pin         | 43:b1:01:00                |

Jak zinterpretować dane zapisane w Hex:
1. Aktualny czas (47e9ee01-47e9-11e4-8939-164230d1df67):

| Name | Value |Data |
|------|-------|-----|
|Actual time|37:11:12:01:10 | 16.01.18 17:55 |

Wartości zamieniamy na DEC i czytamy od prawej.

2. Dni tygodnia (4 przedziału czasu grzania, 2 bajty każdy przedział)

|Name|Value|Data|
|----|-----|----|
|Mo-Su | 25:84:00:00:00:00:00:00 | 06:10-22:00 |

Zamieniam na DEC i mnożymy przez 10 min:
h25 = d37 37*10min=370min=6h10min
h84=d132 132*10min=1320min=22:00

3. Urlop

|Name|Value|Data|
|----|-----|----|
|Holiday 1-8 | 09:06:02:10:09:0d:02:10:18 | 9:00 6.02.16 do 9:00 13.02.16; 12°C |

Wartości zamieniamy na DEC i czytamy od lewej, ostatni bajt pomnożony przez 0,5 to temperatura.

4. Aktualny status urządzenia (47e9ee2a-47e9-11e4-8939-164230d1df67) 

|Name|Value|
|----|-----|
|Status | 00:00:08|

Tej charakterystyki nie mam rozszyfrowanej w 100%, to co wiem to opiszę:

- pierwszy bajt od lewej

  - 00 - tryb automatyczny
  - 01 - tryb manualny
  - 08 - blokada rodzicielska
  - 10 - ochrona przed zamarzaniem

- drugi bajt
  - 00 - silnik w spoczynku
  - 01 - silnik w ruchu

5. Ustawienia (47e9ee2b-47e9-11e4-8939-164230d1df67) 

|Name|Value|
|----|-----|
|Settings | 34:2f:21:2f:fc:04:0a|

| bajt | hex | dec | wartość | opis |
| ---- | --- | --- | ------- | ---- |
| 0    | 0a  | 10  | 10      | czas otwarte okno[min] |
| 1    | 04  | 04  | 04      | czułość "okno" możliwe ustawienia 04,08,0c |
| 2    | fc  | 252 | -2,5    | offset |
| 3    | 2f  | 47  | 23,5    | T comfort |
| 4    | 21  | 33  | 16,5    | T economy |
| 5    | 39  | 57  | 28,5    | T target |
| 6    | 32  | 50  | 64      | T actual |

- Bajty 0 i 1 - ustawienia funkcji „otwarte okno:
- Bajt 2 – ustawienia offsetu temperatury mierzonej.
- Bajt 3 – zaprogramowana temperatura grzania/komfortowa
- Bajt 4 – zaprogramowana temperatura oszczędzania/ekonomiczna
- Bajt 5 – docelowa temperatura w danym momencie
- Bajt 6 – aktualnie zmierzona temperatura

W przypadku temperatur wartości otrzymujemy mnożąc prze 0.5.

6. Bateria (47e9ee2c-47e9-11e4-8939-164230d1df67)

|Name | Value | Data |
|---- | ----- | ---- |
|Battery | 43 | 67% |

7. PIN (47e9ee30-47e9-11e4-8939-164230d1df67)

|Name | Value | Data |
|---- | ----- | ---- |
|pin  | 43:b1:01:00 | 110 915 |

Czytamy bajty od prawej: 0001b143, zamieniamy na DEC i otrzymujemy pin jaki ustawiliśmy w aplikacji EUROprog.

## Problemy/rzeczy do zrobienia

1. Niestety nie udało mi się w pełni rozszyfrować danych z charakterystyki „status”. Pokazywały się tam czasami inne wartości, ale jak na razie nie umiem ich zinterpretować. W lewym dolnym rogu aplikacji wyświetlają się aktualne wartości tej charakterystyki, więc może uda się kiedyś ustalić, co oznaczają te dane.
2. Nie wiem, czy to wina mojej aplikacji, czy softu w termostacie, ale czasami samoistnie zmieniają się ustawienia zaprogramowanej temperatury dla soboty i niedzieli. Zapisane dane zachowują się jakby zostały przesunięte o jeden bajt.

Kod źródłowy:
Cały kod aplikacji udostępniam na GitHub. Może ktoś będzie miał chęć coś zmienić/dodać/ulepszyć.
W celach niekomercyjnych, możecie korzystać do woli, ale jak coś wymyślicie to się pochwalcie ;)

## Źródła, które wykorzystałem w aplikacji
- [BLE](http://developer.android.com/guide/topics/connectivity/bluetooth-le.html)
- ["licznik"](https://jetcracker.wordpress.com/2014/02/28/speedometer-gauge-with-needle-for-android/)

## source

[elektroda](https://www.elektroda.pl/rtvforum/topic3178274.html#15522271)

## Future work
- connect to [web](https://www.supla.org) [github](https://github.com/SUPLA)
- connect to domoticz
- [openhub](http://www.openhab.org)
- [home assistant](https://home-assistant.io)
- [iobroker](http://www.iobroker.net)

