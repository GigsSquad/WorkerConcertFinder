# WorkerConcertFinder

zapytan do bazy nie konczymy średnikiem

tu macie przykładowe jakieś komendy do workbencha żeby się pobawic :P

* INSERT INTO gigs.Concerts VALUE(1, 'JAKIS ARTYSTA', 'MIASTO', 'SPOT', 6, 11, 2014, 'AGENCJA', 'jakis-url.com')

* UPDATE table WHERE something = 'something'

* DELETE FROM gigs.Concerts WHERE ARTIST = 'B.O.K.'

[Jakiś tutorial, nawet po naszemu](http://php.net/manual/pl/function.mysql-query.php)

Do tworzenia bazy, żeby nie wyklikiwać tego miliony razy 
```
CREATE TABLE `gigs`.`Concerts` (
  `ORD` BIGINT(16) NOT NULL AUTO_INCREMENT,
  `ARTIST` VARCHAR(256) NULL DEFAULT NULL,
  `CITY` VARCHAR(64) NULL DEFAULT NULL,
  `SPOT` VARCHAR(64) NULL DEFAULT NULL,
  `DAY` INT NULL DEFAULT NULL,
  `MONTH` INT NULL DEFAULT NULL,
  `YEAR` INT NULL DEFAULT NULL,
  `AGENCY` VARCHAR(32) NULL DEFAULT NULL,
  `URL` VARCHAR(512) NULL DEFAULT NULL,
  `LAT` VARCHAR(16) NULL DEFAULT NULL,
  `LON` VARCHAR(16) NULL DEFAULT NULL,
  PRIMARY KEY (`ORD`));
```
