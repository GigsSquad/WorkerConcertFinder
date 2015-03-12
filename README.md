# WorkerConcertFinder

### DB
* Nazwa bazy danych: musicart
* Typ bazy: MySQL 5.5
* Adres serwera baz danych: sql.musicart.nazwa.pl
* Port serwera baz danych: 3307
* Użytkownik bazy (login): musicart
* Pass: GIG$$$finder112!
* Panel zarządzania bazą danych: https://mysql.nazwa.pl/ 

### FTP
* Nazwa konta (login): musicart_android
* Serwer FTP do umieszczania stron: ftp.musicart.nazwa.pl
* Opis konta:	PHP do aplikacji
* Pass: GIG$$$finder112!
* Katalog: /android 

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
