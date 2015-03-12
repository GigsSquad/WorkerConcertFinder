# WorkerConcertFinder

musicart.nazwa.pl
p: GIG$$$find!
https://admin.nazwa.pl/

zapytan do bazy nie konczymy średnikiem

tu macie przykładowe jakieś komendy do workbencha żeby się pobawic :P

* INSERT INTO gigs.Concerts VALUE(1, 'JAKIS ARTYSTA', 'MIASTO', 'SPOT', 6, 11, 2014, 'AGENCJA', 'jakis-url.com')

* UPDATE table WHERE something = 'something'

* DELETE FROM gigs.Concerts WHERE ARTIST = 'B.O.K.'

[Jakiś tutorial, nawet po naszemu](http://php.net/manual/pl/function.mysql-query.php)

Do tworzenia bazy, żeby nie wyklikiwać tego miliony razy 
```
CREATE TABLE `gigs`.`concerts` (
  `ord` BIGINT(16) NOT NULL AUTO_INCREMENT,
  `artist` VARCHAR(256) NULL DEFAULT NULL,
  `city` VARCHAR(64) NULL DEFAULT NULL,
  `spot` VARCHAR(64) NULL DEFAULT NULL,
  `day` INT NULL DEFAULT NULL,
  `month` INT NULL DEFAULT NULL,
  `year` INT NULL DEFAULT NULL,
  `agency` VARCHAR(32) NULL DEFAULT NULL,
  `url` VARCHAR(512) NULL DEFAULT NULL,
  `lat` VARCHAR(16) NULL DEFAULT NULL,
  `lon` VARCHAR(16) NULL DEFAULT NULL,
  PRIMARY KEY (`ord`));
```
