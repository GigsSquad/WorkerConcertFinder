# WorkerConcertFinder

### DB
* Nazwa bazy danych: musicart
* Adres serwera baz danych: __sql.musicart.nazwa.pl__
* Port serwera baz danych: __3307__
* Użytkownik bazy (login): __musicart__
* Pass: __GIG$$$finder112!__

### FTP
* Nazwa konta (login): __musicart_android__
* Serwer FTP do umieszczania stron: __ftp.musicart.nazwa.pl__
* Pass: __GIG$$$finder112!__
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
